package org.backend.domain.batch.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.domain.batch.dto.response.ApiResponse;
import org.backend.domain.batch.dto.response.BatchHistoryListResponse;
import org.backend.domain.batch.dto.response.BatchStatusDetailResponse;
import org.backend.domain.batch.service.BatchResetService;
import org.backend.domain.batch.service.BatchService;
import org.backend.domain.member.repository.MemberRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
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
    private final MemberRepository memberRepository;
    private final BatchService batchService;

    private final Job preAnalysisJob;
    private final Job postAnalysisJob;
    private final BatchResetService batchResetService;


    /**
     * 배치 실행 (40,000건 기준)
     */
    @PostMapping("/run")
    public ApiResponse<String> runBatch() throws Exception {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        long instanceCount = jobExplorer.getJobInstanceCount("memberFeatureJob");
        String batchId = today + "_" + String.format("%04d", instanceCount + 1);

        long memberCount = memberRepository.count();
        int stepCount = 4; // 4개 스텝
        long totalTargetCount = memberCount * stepCount;

        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .addString("batchId", batchId)
                .addString("featureBaseDate", LocalDate.now().toString())
                .addLong("totalTargetCount", totalTargetCount)
                .toJobParameters();

        jobLauncher.run(memberFeatureJob, params);

        return ApiResponse.success("Batch Started", batchId);
    }

    /**
     * 배치 실행 이력 목록 조회
     */
    @GetMapping("/history")
    public ApiResponse<BatchHistoryListResponse> getHistory() {
        BatchHistoryListResponse data = batchService.getBatchHistory();
        return ApiResponse.success("배치 이력 조회 성공", data);
    }

    /**
     *  batchId 기반 상세 상태 조회
     * 예: /batch/status/20260306_0001
     */
    @GetMapping("/status/{batchId}")
    public ApiResponse<BatchStatusDetailResponse> getStatus(@PathVariable String batchId) {
        BatchStatusDetailResponse data = batchService.getBatchDetailStatusByCustomId(batchId);
        if (data == null) {
            return ApiResponse.fail("해당 batchId의 실행 이력을 찾을 수 없습니다.");
        }
        return ApiResponse.success("배치 상태 조회 성공", data);
    }

    /**
     * 커스텀 batchId 기반 배치 중지
     */
    @PostMapping("/stop/{batchId}")
    public ApiResponse<String> stopJob(@PathVariable String batchId) {
        try {
            boolean stopped = batchService.stopJobByCustomId(batchId);
            return stopped ? ApiResponse.success("중지 요청 성공", "Batch ID: " + batchId)
                    : ApiResponse.fail("중지 가능한 상태(RUNNING)가 아니거나 존재하지 않습니다.");
        } catch (Exception e) {
            return ApiResponse.fail("중지 중 오류 발생: " + e.getMessage());
        }
    }

    @PostMapping("/run/test")
    public String runBatchTest() throws Exception {

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

    @DeleteMapping("/del")
    public ResponseEntity<CommonResponse<String>> deleteOldData(){
        int count = batchResetService.deleteOldData();
        return ResponseEntity.ok(CommonResponse.success(null, "5년 이상 된 데이터 " + count + "건 삭제 완료"));
    }

    // 분석 데이터 초기화
    @DeleteMapping("/reset")
    public ResponseEntity<CommonResponse<String>> resetBatchData(
            @RequestParam(required = false) List<String> months,
            @RequestParam(defaultValue = "false") boolean isAll
    ){
        batchResetService.resetBatchData(months, isAll);
        String msg = isAll ? "전체 데이터 초기화 완료" : months + "달 데이터 초기화 완료";
        return ResponseEntity.ok(CommonResponse.success(null, msg));
    }
}