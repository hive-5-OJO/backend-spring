//package org.backend.domain.analysis.batch.job.rfm;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.Step;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@RequiredArgsConstructor
//public class RfmJobConfig {
//
//    private final JobRepository jobRepository;
//
//    @Bean
//    public Step rfmCalculateStep(){
//        return StepBuilder("rfmCalculateStep", jobRepository)
//                // step - transactionManager, reader, processor, writer
//                .build();
//    }
//
//    @Bean
//    public Step billingSnapshotStep(){
//        return StepBuilder("billingSnapshotStep", jobRepository)
//                .build();
//    }
//}
