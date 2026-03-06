package org.backend.domain.batch.job.memberfeature;

import lombok.RequiredArgsConstructor;
import org.backend.domain.batch.entity.ConsultationBasics;
import org.backend.domain.batch.entity.FeatureUsage;
import org.backend.domain.batch.entity.Lifecycle;
import org.backend.domain.batch.entity.Monetary;
import org.backend.domain.batch.job.memberfeature.reader.MemberReaderConfig;
import org.backend.domain.member.entity.Member;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class MemberFeatureJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MemberReaderConfig memberReaderConfig;


     // TaskExecutor 설정 (멀티스레드 처리용)
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("batch-thread-");
        executor.initialize();
        return executor;
    }


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
    public Step consultationStep(
            ItemProcessor<Member, ConsultationBasics> consultationProcessor,
            JpaItemWriter<ConsultationBasics> consultationWriter) {

        return new StepBuilder("consultationStep", jobRepository)
                .<Member, ConsultationBasics>chunk(1000, transactionManager)
                .reader(memberReaderConfig.memberReader())
                .processor(consultationProcessor)
                .writer(consultationWriter)
                .taskExecutor(taskExecutor())
                .build();
    }


    @Bean
    public Step lifecycleStep(
            ItemProcessor<Member, Lifecycle> memberLifecycleProcessor,
            JpaItemWriter<Lifecycle> lifecycleWriter) {

        return new StepBuilder("lifecycleStep", jobRepository)
                .<Member, Lifecycle>chunk(1000, transactionManager)
                .reader(memberReaderConfig.memberReader())
                .processor(memberLifecycleProcessor)
                .writer(lifecycleWriter)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step monetaryStep(
            ItemProcessor<Member, Monetary> monetaryProcessor,
            JpaItemWriter<Monetary> monetaryWriter) {

        return new StepBuilder("monetaryStep", jobRepository)
                .<Member, Monetary>chunk(1000, transactionManager)
                .reader(memberReaderConfig.memberReader())
                .processor(monetaryProcessor)
                .writer(monetaryWriter)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step usageStep(
            ItemProcessor<Member, FeatureUsage> usageProcessor,
            JpaItemWriter<FeatureUsage> usageWriter) {

        return new StepBuilder("usageStep", jobRepository)
                .<Member, FeatureUsage>chunk(1000, transactionManager)
                .reader(memberReaderConfig.memberReader())
                .processor(usageProcessor)
                .writer(usageWriter)
                .taskExecutor(taskExecutor())
                .build();
    }
}