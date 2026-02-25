package org.backend.domain.batch.job.memberfeature;

import lombok.RequiredArgsConstructor;
import org.backend.domain.batch.job.memberfeature.reader.MemberReaderConfig;
import org.backend.domain.member.entity.Member;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
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


     // TaskExecutor 설정
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);    // 기본 스레드 4개
        executor.setMaxPoolSize(8);     // 최대 8개까지 확장
        executor.setQueueCapacity(500); // 대기열 설정
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
    public Step consultationStep(ItemProcessor<Member, ?> consultationProcessor,
                                 ItemWriter<Object> memberWriter) {
        return new StepBuilder("consultationStep", jobRepository)
                .<Member, Object>chunk(1000, transactionManager)
//                .reader(memberReaderConfig.memberReader())
                .reader(memberReaderConfig.memberReaderTest())
                .processor((ItemProcessor<? super Member, ?>) consultationProcessor)
                .writer(memberWriter)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step lifecycleStep(ItemProcessor<Member, ?> memberLifecycleProcessor,
                              ItemWriter<Object> memberWriter) {
        return new StepBuilder("lifecycleStep", jobRepository)
                .<Member, Object>chunk(1000, transactionManager)
//                .reader(memberReaderConfig.memberReader())
                .reader(memberReaderConfig.memberReaderTest())
                .processor((ItemProcessor<? super Member, ?>) memberLifecycleProcessor)
                .writer(memberWriter)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step monetaryStep(ItemProcessor<Member, ?> monetaryProcessor,
                             ItemWriter<Object> memberWriter) {
        return new StepBuilder("monetaryStep", jobRepository)
                .<Member, Object>chunk(1000, transactionManager)
//                .reader(memberReaderConfig.memberReader())
                .reader(memberReaderConfig.memberReaderTest())
                .processor((ItemProcessor<? super Member, ?>) monetaryProcessor)
                .writer(memberWriter)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Step usageStep(ItemProcessor<Member, ?> usageProcessor,
                          ItemWriter<Object> memberWriter) {
        return new StepBuilder("usageStep", jobRepository)
                .<Member, Object>chunk(1000, transactionManager)
//                .reader(memberReaderConfig.memberReader())
                .reader(memberReaderConfig.memberReaderTest())
                .processor((ItemProcessor<? super Member, ?>) usageProcessor)
                .writer(memberWriter)
                .taskExecutor(taskExecutor())
                .build();
    }
}