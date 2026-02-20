package org.backend.batch.job.memberfeature.reader;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.backend.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MemberReaderConfig {

    private final EntityManagerFactory emf;

    @Bean
    @StepScope
    public JpaPagingItemReader<Member> memberReader() {

        return new JpaPagingItemReaderBuilder<Member>()
                .name("memberReader")
                .entityManagerFactory(emf)
                .queryString("SELECT m FROM Member m")
                .pageSize(1000)
                .build();
    }
}
