package org.backend.batch.config.writer;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.backend.entity.feature.Monetary;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MonetaryWriterConfig {

    private final EntityManagerFactory emf;

    @Bean
    public JpaItemWriter<Monetary> monetaryWriter() {
        JpaItemWriter<Monetary> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }
}
