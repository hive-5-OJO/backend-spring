package org.backend.domain.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job memberFeatureJob;
    private final Job preAnalysisJob;
    private final Job postAnalysisJob;

    @PostMapping("/run")
    public String runBatch() throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(memberFeatureJob, params);

        return "Batch Started";
    }

    @PostMapping("/run/pre")
    public String runPreAnalysis(@RequestParam String baseMonth) throws Exception{
        JobParameters params = new JobParametersBuilder()
                .addString("baseMonth", baseMonth)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(preAnalysisJob, params);

        if (execution.getStatus().isUnsuccessful()) {
            // 실패 원인 중 첫 번째를 에러 메시지로
            Throwable firstException = execution.getAllFailureExceptions().get(0);
            throw new RuntimeException("배치 실패 원인: " + firstException.getMessage());
        }

        return "rfm batch started for " + baseMonth;
    }

    @PostMapping("/run/post")
    public String runPostAnalysis(@RequestParam String baseMonth) throws Exception{
        JobParameters params = new JobParametersBuilder()
                .addString("baseMonth", baseMonth)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobLauncher.run(postAnalysisJob, params);

        if(execution.getStatus().isUnsuccessful()){
            Throwable firstException = execution.getAllFailureExceptions().get(0);
            throw new RuntimeException("배치 실패 원인: "+ firstException.getMessage());
        }

        return "kpi and snapshot batch started for " + baseMonth;
    }
}