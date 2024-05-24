package com.mobigen.dolphin.service;

import com.mobigen.dolphin.antlr.ModelSqlLexer;
import com.mobigen.dolphin.antlr.ModelSqlParser;
import com.mobigen.dolphin.antlr.ModelSqlParsingVisitor;
import com.mobigen.dolphin.config.DolphinConfiguration;
import com.mobigen.dolphin.repository.local.JobRepository;
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

    private final JobRepository jobRepository;

    public Object execute(String sql) {
        log.info("origin sql: {}", sql);
        var lexer = new ModelSqlLexer(CharStreams.fromString(sql));
        var tokens = new CommonTokenStream(lexer);
        var parser = new ModelSqlParser(tokens);
        var visitor = new ModelSqlParsingVisitor(dolphinConfiguration);
        var parseTree = parser.parse();
        sql = visitor.visit(parseTree);
        log.info("converted sql: {}", sql);
        return sql;
    }

    public Object status(UUID jobId) {
        var job = jobRepository.findById(jobId);
        if (job.isEmpty()) {
            throw new RuntimeException("job not found");
        }
        return job.get().getStatus();
    }
}
