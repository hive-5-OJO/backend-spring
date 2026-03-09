package org.backend.domain.advice.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.backend.domain.advice.entity.Advice;

@Getter
@Builder
public class AdviceTimelineResponse {
    private Long id;
    private String adminName;
    private String categoryName;
    private String adviceContent;
    private String channel;
    private LocalDateTime createdAt;

    public static AdviceTimelineResponse from(Advice advice) {
        return AdviceTimelineResponse.builder()
                .id(advice.getId())
                .adminName(advice.getAdmin().getName())
                .categoryName(advice.getCategory().getCategoryName())
                .adviceContent(advice.getAdviceContent())
                .channel(advice.getChannel())
                .createdAt(advice.getCreatedAt())
                .build();
    }
}