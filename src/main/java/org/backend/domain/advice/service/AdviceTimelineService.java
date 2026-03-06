package org.backend.domain.advice.service;

import lombok.RequiredArgsConstructor;
import org.backend.domain.advice.dto.AdviceTimelineResponse;
import org.backend.domain.advice.entity.Advice;
import org.backend.domain.advice.repository.AdviceTimelineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdviceTimelineService {

    private final AdviceTimelineRepository adviceTimelineRepository;

    public AdviceTimelineResponse getAdviceTimeline(Long memberId) {

        List<AdviceTimelineResponse.TimelineItem> timeline = adviceTimelineRepository
                .findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(this::toTimelineItem)
                .toList();

        return AdviceTimelineResponse.builder()
                .memberId(memberId)
                .timeline(timeline)
                .build();
    }

    private AdviceTimelineResponse.TimelineItem toTimelineItem(Advice advice) {
        return AdviceTimelineResponse.TimelineItem.builder()
                .date(advice.getCreatedAt() != null ? advice.getCreatedAt().toLocalDate() : null)
                .category(advice.getCategory() != null ? advice.getCategory().getCategoryName() : null)
                .promotionName(advice.getPromotion() != null ? advice.getPromotion().getPromotionName() : null)
                .direction(mapDirection(advice.getDirection()))
                .content(advice.getAdviceContent())
                .satisfactionScore(advice.getSatisfactionScore())
                .build();
    }

    private String mapDirection(String direction) {
        if (direction == null) {
            return null;
        }

        return switch (direction) {
            case "INBOUND" -> "IN";
            case "OUTBOUND" -> "OUT";
            default -> direction;
        };
    }
}