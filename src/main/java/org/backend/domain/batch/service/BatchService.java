package org.backend.domain.batch.service;

import lombok.RequiredArgsConstructor;
import org.backend.domain.batch.dto.response.BatchStatusResponse;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final JobExplorer jobExplorer;

    public BatchStatusResponse getBatchStatusById(String batchId) {
        // 1. "memberFeatureJob"이라는 이름의 Job 인스턴스들을 조회 (최근 100건)
        List<JobInstance> jobInstances = jobExplorer.getJobInstances("memberFeatureJob", 0, 100);

        // 2. JobParameters 내의 batchId가 일치하는 실행 내역 찾기
        JobExecution execution = jobInstances.stream()
                .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
                .filter(ex -> batchId.equals(ex.getJobParameters().getString("batchId")))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("배치를 찾을 수 없습니다."));

        // 3. 각 스텝의 카운트를 합계 (Read: 처리수, Write: 성공수, Filter/Skip: 실패수로 간주)
        int totalRead = 0;
        int totalWrite = 0;
        int totalFail = 0;

        for (StepExecution step : execution.getStepExecutions()) {
            totalRead += (int) step.getReadCount();
            totalWrite += (int) step.getWriteCount();
            // 필터링되었거나 스킵된 건수를 실패로 산정 (비즈니스 로직에 따라 조정 가능)
            totalFail += (int) (step.getFilterCount() + step.getProcessSkipCount() + step.getWriteSkipCount());
        }

        // 4. 응답 객체 생성
        return BatchStatusResponse.builder()
                .batchId(batchId)
                .batchStatus(execution.getStatus().toString()) // RUNNING, COMPLETED, FAILED 등
                .featureBaseDate(execution.getJobParameters().getString("featureBaseDate"))
                .totalTargetCount(100000) // 실제로는 시작 시 파라미터로 넘겨받은 값을 사용 권장
                .processedCount(totalRead)
                .successCount(totalWrite)
                .failCount(totalFail)
                .startTime(execution.getStartTime())
                .endTime(execution.getEndTime())
                .build();
    }
}