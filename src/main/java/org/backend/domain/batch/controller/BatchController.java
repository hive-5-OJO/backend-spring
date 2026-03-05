////package org.backend.domain.batch.controller;
////
////import lombok.RequiredArgsConstructor;
////import org.springframework.batch.core.Job;
////import org.springframework.batch.core.JobParameters;
////import org.springframework.batch.core.JobParametersBuilder;
////import org.springframework.batch.core.launch.JobLauncher;
////import org.springframework.web.bind.annotation.PostMapping;
////import org.springframework.web.bind.annotation.RequestMapping;
////import org.springframework.web.bind.annotation.RestController;
////
////@RestController
////@RequiredArgsConstructor
////@RequestMapping("/batch")
////public class BatchController {
////
////    private final JobLauncher jobLauncher;
////    private final Job memberFeatureJob;
////
////    @PostMapping("/run")
////    public String runBatch() throws Exception {
////
////        JobParameters params = new JobParametersBuilder()
////                .addLong("time", System.currentTimeMillis())
////                .toJobParameters();
////
////        jobLauncher.run(memberFeatureJob, params);
////
////        return "Batch Started";
////    }
////}


package org.backend.domain.batch.controller;

import lombok.RequiredArgsConstructor;
import org.backend.domain.batch.dto.response.ApiResponse;
import org.backend.domain.batch.dto.response.BatchStatusResponse;
import org.backend.domain.batch.repository.MemberRepository;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job memberFeatureJob;
    private final JobExplorer jobExplorer;
    private final MemberRepository memberRepository; // 전체 건수 조회를 위해 주입

    @PostMapping("/run")
    public ApiResponse<String> runBatch() throws Exception {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 1. 오늘 실행된 인스턴스 개수 기반 순번 생성
        long count = jobExplorer.getJobInstanceCount("memberFeatureJob");
        String batchId = today + "_" + String.format("%04d", count + 1);

        // 2. 실제 DB의 전체 회원 수 조회 (말이 안 되는 1000 대신 실제 건수 사용)
        long actualTotalCount = memberRepository.count();

        // 3. 파라미터 구성
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("batchId", batchId)
                .addString("featureBaseDate", LocalDate.now().toString())
                .addLong("totalTargetCount", actualTotalCount) // 동적으로 계산된 건수 전달
                .toJobParameters();

        jobLauncher.run(memberFeatureJob, params);

        return ApiResponse.success("Batch Started", batchId);
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<BatchStatusResponse>> getBatchStatus(@RequestParam String batchId) {
        // ... (상태 조회 로직은 이전과 동일)
        List<JobInstance> instances = jobExplorer.getJobInstances("memberFeatureJob", 0, 100);
        JobExecution execution = instances.stream()
                .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                .filter(ex -> batchId.equals(ex.getJobParameters().getString("batchId")))
                .findFirst()
                .orElse(null);

        if (execution == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("존재하지 않는 배치입니다."));
        }

        int processedCount = 0;
        int successCount = 0;
        int failCount = 0;

        for (StepExecution se : execution.getStepExecutions()) {
            processedCount += (int) se.getReadCount();
            successCount += (int) se.getWriteCount();
            failCount += (int) (se.getReadSkipCount() + se.getProcessSkipCount() + se.getWriteSkipCount());
        }

        Long targetCount = execution.getJobParameters().getLong("totalTargetCount");
        String baseDate = execution.getJobParameters().getString("featureBaseDate");

        BatchStatusResponse data = BatchStatusResponse.builder()
                .batchId(batchId)
                .batchStatus(execution.getStatus().toString())
                .featureBaseDate(baseDate)
                .totalTargetCount(targetCount != null ? targetCount.intValue() : 0)
                .processedCount(processedCount)
                .successCount(successCount)
                .failCount(failCount)
                .startTime(execution.getStartTime())
                .endTime(execution.getEndTime())
                .build();

        return ResponseEntity.ok(ApiResponse.success("배치 상태 조회 성공", data));
    }
}