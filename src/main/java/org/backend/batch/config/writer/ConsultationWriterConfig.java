package org.backend.batch.config.writer;

import jakarta.persistence.EntityManagerFactory;
import org.backend.entity.feature.ConsultationBasics;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsultationWriterConfig {

    @Bean
    public JpaItemWriter<ConsultationBasics> consultationWriter(
            EntityManagerFactory emf
    ) {

        JpaItemWriter<ConsultationBasics> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);

        return writer;
    }
}


