package org.backend.domain.batch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchStatusResponse {
    private String batchId;
    private String batchStatus;
    private String featureBaseDate;
    private int totalTargetCount;
    private int processedCount;
    private int successCount;
    private int failCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}