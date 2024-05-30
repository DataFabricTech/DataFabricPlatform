package com.mobigen.dolphin.service;

import com.mobigen.dolphin.antlr.ModelSqlLexer;
import com.mobigen.dolphin.antlr.ModelSqlParser;
import com.mobigen.dolphin.antlr.ModelSqlParsingVisitor;
import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.response.QueryResultDTO;
import com.mobigen.dolphin.entity.local.JobEntity;
import com.mobigen.dolphin.repository.local.JobRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * <p>
 * Created by fwani.
 *
 * @version 0.0.1
 * @since 0.0.1
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class QueryService {
    private final DolphinConfiguration dolphinConfiguration;
    private final TrinoRepository trinoRepository;
    private final JobRepository jobRepository;

    @Transactional
    public JobEntity createJob(String sql) {
        log.info("Create job. origin sql: {}", sql);
        var lexer = new ModelSqlLexer(CharStreams.fromString(sql));
        var tokens = new CommonTokenStream(lexer);
        var parser = new ModelSqlParser(tokens);
        var visitor = new ModelSqlParsingVisitor(dolphinConfiguration);
        var parseTree = parser.parse();
        var convertedQuery = visitor.visit(parseTree);
        log.info("Converted sql: {}", convertedQuery);
        var job = JobEntity.builder()
                .status(JobEntity.JobStatus.QUEUED)
                .userQuery(sql)
                .convertedQuery(convertedQuery)
                .build();
        jobRepository.save(job);
        return job;
    }

    @Transactional
    public QueryResultDTO execute(String sql) {
        var job = createJob(sql);
        job.setStatus(JobEntity.JobStatus.RUNNING);
        jobRepository.save(job);
        var result = trinoRepository.executeQuery2(job.getConvertedQuery());
        job.setStatus(JobEntity.JobStatus.FINISHED);
        jobRepository.save(job);
        return result;
    }

    public Object executeAsync(String sql) {
        var job = createJob(sql);
        return trinoRepository.executeQuery("", job.getConvertedQuery());
    }

    public Object status(UUID jobId) {
        var job = jobRepository.findById(jobId);
        if (job.isEmpty()) {
            throw new RuntimeException("job not found");
        }
        return job.get().getStatus();
    }
}
