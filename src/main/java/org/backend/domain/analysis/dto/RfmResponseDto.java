package org.backend.domain.analysis.dto;

import java.time.LocalDateTime;

public record RfmResponseDto (
  Long memberId,
  RfmDetail rfmDetail
) {
    public record RfmDetail(
        LocalDateTime recency,
        int frequency,
        Long monetary,
        LocalDateTime updatedAt,
        int rfmScore
    ){}
}


