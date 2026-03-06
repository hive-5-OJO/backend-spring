package org.backend.domain.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberFeatureScheduler {

    private final JobLauncher jobLauncher;
    private final Job memberFeatureJob;

    // 매일 새벽 2시에 실행 (Cron 표현식: 초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 2 * * *")
//    @Scheduled(cron = "0 * * * * *")
    public void runMemberFeatureJob() {
        try {
            // 어제 날짜를 기준일로 설정 (데이터 정합성 측면에서 유리)
            String targetDateStr = LocalDate.now().minusDays(1).toString();
            log.info("########## [SCHEDULE] BATCH START FOR DATE: {} ##########", targetDateStr);

            JobParameters params = new JobParametersBuilder()
                    .addString("featureBaseDate", targetDateStr)
                    .addLong("run.id", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(memberFeatureJob, params);
            
            log.info("########## [SCHEDULE] BATCH COMPLETED SUCCESSFULLY ##########");
        } catch (Exception e) {
            log.error("########## [SCHEDULE] BATCH FAILED! ##########", e);
        }
    }
}