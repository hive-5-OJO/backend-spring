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
                .direction(formatDirection(advice.getDirection()))
                .content(formatContent(advice))
                .satisfactionScore(formatSatisfactionScore(advice.getSatisfactionScore()))
                .build();
    }

    private String formatDirection(String direction) {
        if (direction == null || direction.isBlank()) {
            return null;
        }

        return switch (direction.toUpperCase()) {
            case "IN" -> "인바운드";
            case "OUT" -> "아웃바운드";
            default -> direction;
        };
    }

    private String formatContent(Advice advice) {
        String raw = advice.getAdviceContent();
        if (raw == null || raw.isBlank()) {
            return "상담 내용이 없습니다.";
        }

        return raw
                .replace("(CALL)", "(전화)")
                .replace("(SMS)", "(문자)")
                .replace("(APP)", "(앱)");
    }

    private Long formatSatisfactionScore(Long satisfactionScore) {
        return satisfactionScore != null ? satisfactionScore : 0L;
    }
}