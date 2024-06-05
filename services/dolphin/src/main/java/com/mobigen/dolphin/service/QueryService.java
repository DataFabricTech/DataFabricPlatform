package com.mobigen.dolphin.service;

import com.mobigen.dolphin.antlr.ModelSqlLexer;
import com.mobigen.dolphin.antlr.ModelSqlParser;
import com.mobigen.dolphin.antlr.ModelSqlParsingVisitor;
import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.dto.request.ExecuteDto;
import com.mobigen.dolphin.dto.response.QueryResultDTO;
import com.mobigen.dolphin.entity.local.JobEntity;
import com.mobigen.dolphin.repository.local.JobRepository;
import com.mobigen.dolphin.repository.openmetadata.OpenMetadataRepository;
import com.mobigen.dolphin.repository.trino.TrinoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private final OpenMetadataRepository openMetadataRepository;
    private final DolphinConfiguration dolphinConfiguration;
    private final TrinoRepository trinoRepository;
    private final JobRepository jobRepository;

    private final AsyncService asyncService;

    public JobEntity createJob(ExecuteDto executeDto) {
        log.info("Create job. origin sql: {}", executeDto.getQuery());
        var lexer = new ModelSqlLexer(CharStreams.fromString(executeDto.getQuery()));
        var tokens = new CommonTokenStream(lexer);
        var parser = new ModelSqlParser(tokens);
        var visitor = new ModelSqlParsingVisitor(openMetadataRepository, dolphinConfiguration, executeDto.getReferenceModels());
        var parseTree = parser.parse();
        var convertedQuery = visitor.visit(parseTree);
        log.info("Converted sql: {}", convertedQuery);
        var job = JobEntity.builder()
                .status(JobEntity.JobStatus.QUEUED)
                .userQuery(executeDto.getQuery())
                .convertedQuery(convertedQuery)
                .build();
        jobRepository.save(job);
        return job;
    }

    public QueryResultDTO execute(ExecuteDto executeDto) {
        var job = createJob(executeDto);
        job.setStatus(JobEntity.JobStatus.RUNNING);
        jobRepository.save(job);
        var result = trinoRepository.executeQuery2(job.getConvertedQuery());
        job.setStatus(JobEntity.JobStatus.FINISHED);
        jobRepository.save(job);
        return result;
    }

    public QueryResultDTO executeAsync(ExecuteDto executeDto) {
        var job = createJob(executeDto);
        asyncService.executeAsync(job);
        return QueryResultDTO.builder()
                .jobId(job.getId())
                .build();
    }

    public Object status(UUID jobId) {
        var job = jobRepository.findById(jobId);
        if (job.isEmpty()) {
            throw new RuntimeException("job not found");
        }
        return job.get().getStatus();
    }

    public QueryResultDTO read(UUID jobId) {
        var job = jobRepository.findById(jobId);
        if (job.isEmpty()) {
            throw new RuntimeException("job not found");
        }
        List<List<Object>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(job.get().getResultPath()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return QueryResultDTO.builder()
                .rows(records)
                .build();
    }
}
