package com.mobigen.datafabric.jpaSample;

import com.mobigen.datafabric.jpaSample.service.JpaService;
import dto.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.UUID;

@Slf4j
@SpringBootApplication
@ComponentScan("com.mobigen.datafabric")
public class JpaSampleApplication implements CommandLineRunner {
    private final JpaService jpaService;

    @Autowired
    public JpaSampleApplication(JpaService jpaService) {
        this.jpaService = jpaService;
    }

    public static void main(String[] args) {
        SpringApplication.run(JpaSampleApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        var tag = Tag.builder()
                .tagId(UUID.randomUUID())
                .tagValue("Jpa Sample Tag value")
                .build();

        jpaService.saveTag(tag);
    }
}