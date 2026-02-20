package org.backend.batch.job.memberfeature;

import lombok.RequiredArgsConstructor;
import org.backend.batch.job.memberfeature.processor.ConsultationProcessor;
import org.backend.batch.job.memberfeature.processor.MemberLifecycleProcessor;
import org.backend.batch.job.memberfeature.processor.MonetaryProcessor;
import org.backend.batch.job.memberfeature.processor.UsageProcessor;
import org.backend.entity.Member;
import org.backend.entity.feature.ConsultationBasics;
import org.backend.entity.feature.FeatureUsage;
import org.backend.entity.feature.Lifecycle;
import org.backend.entity.feature.Monetary;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class MemberFeatureJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JpaPagingItemReader<Member> memberReader;
    private final JpaItemWriter<Object> jpaWriter;

    @Bean
    public Job memberFeatureJob(Step consultationStep,
                                Step lifecycleStep,
                                Step monetaryStep,
                                Step usageStep) {
        return new JobBuilder("memberFeatureJob", jobRepository)
                .start(consultationStep)
                .next(lifecycleStep)
                .next(monetaryStep)
                .next(usageStep)
                .build();
    }

    @Bean
    public Step consultationStep(ConsultationProcessor processor) {
        return new StepBuilder("consultationStep", jobRepository)
                .<Member, ConsultationBasics>chunk(1000, transactionManager)
                .reader(memberReader)
                .processor(processor)
                .writer((JpaItemWriter) jpaWriter)
                .build();
    }

    @Bean
    public Step lifecycleStep(MemberLifecycleProcessor processor) {
        return new StepBuilder("lifecycleStep", jobRepository)
                .<Member, Lifecycle>chunk(1000, transactionManager)
                .reader(memberReader)
                .processor(processor)
                .writer((JpaItemWriter) jpaWriter)
                .build();
    }

    @Bean
    public Step monetaryStep(MonetaryProcessor processor) {
        return new StepBuilder("monetaryStep", jobRepository)
                .<Member, Monetary>chunk(1000, transactionManager)
                .reader(memberReader)
                .processor(processor)
                .writer((JpaItemWriter) jpaWriter)
                .build();
    }

    @Bean
    public Step usageStep(UsageProcessor processor) {
        return new StepBuilder("usageStep", jobRepository)
                .<Member, FeatureUsage>chunk(1000, transactionManager)
                .reader(memberReader)
                .processor(processor)
                .writer((JpaItemWriter) jpaWriter)
                .build();
    }
}







