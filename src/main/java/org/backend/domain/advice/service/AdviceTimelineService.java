package org.backend.domain.advice.service;

import lombok.RequiredArgsConstructor;
import org.backend.domain.advice.dto.AdviceTimelineResponse;
import org.backend.domain.advice.entity.Advice;
import org.backend.domain.advice.repository.AdviceTimelineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdviceTimelineService {

    private final AdviceTimelineRepository adviceTimelineRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.KOREA);

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

        String normalized = raw
                .replace("(CALL)", "(전화)")
                .replace("(SMS)", "(문자)")
                .replace("(APP)", "(앱)");

        // 1. 요금/청구 문의 패턴
        String billingFormatted = tryFormatBillingContent(normalized);
        if (billingFormatted != null) {
            return billingFormatted;
        }

        // 2. 연체/미납 안내 패턴
        String overdueFormatted = tryFormatOverdueContent(normalized);
        if (overdueFormatted != null) {
            return overdueFormatted;
        }

        // 3. 그 외에는 원문 반환
        return normalized;
    }

    private String tryFormatBillingContent(String raw) {
        Pattern pattern = Pattern.compile(
                "^(.*?):\\s*billed=(\\d+),\\s*paid_at=([0-9T:-]+)\\s*/\\s*(.*)$"
        );
        Matcher matcher = pattern.matcher(raw);

        if (!matcher.matches()) {
            return null;
        }

        String title = matcher.group(1).trim();
        String billed = formatAmount(matcher.group(2));
        String paidAt = formatDateTime(matcher.group(3));
        String productInfo = matcher.group(4).trim();

        return String.format("%s: %s이 %s에 납부되었습니다. / %s",
                title, billed, paidAt, productInfo);
    }

    private String tryFormatOverdueContent(String raw) {
        Pattern pattern = Pattern.compile(
                "^(.*?):\\s*base_month=(\\d{6}),\\s*due=([0-9-]+),\\s*billed=(\\d+),\\s*overdue=(\\d+)\\s*/\\s*(.*)$"
        );
        Matcher matcher = pattern.matcher(raw);

        if (!matcher.matches()) {
            return null;
        }

        String title = matcher.group(1).trim();
        String baseMonth = formatBaseMonth(matcher.group(2));
        String dueDate = matcher.group(3).trim();
        String billed = formatAmount(matcher.group(4));
        String overdue = formatAmount(matcher.group(5));
        String productInfo = matcher.group(6).trim();

        return String.format("%s: %s 기준 청구금액은 %s이며, 납기일(%s) 이후 미납금액은 %s입니다. / %s",
                title, baseMonth, billed, dueDate, overdue, productInfo);
    }

    private String formatAmount(String amount) {
        try {
            long value = Long.parseLong(amount);
            return NUMBER_FORMAT.format(value) + "원";
        } catch (NumberFormatException e) {
            return amount;
        }
    }

    private String formatDateTime(String value) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(value);
            return dateTime.format(DATE_TIME_FORMATTER);
        } catch (Exception e) {
            return value;
        }
    }

    private String formatBaseMonth(String baseMonth) {
        if (baseMonth == null || baseMonth.length() != 6) {
            return baseMonth;
        }
        return baseMonth.substring(0, 4) + "-" + baseMonth.substring(4, 6);
    }

    private Long formatSatisfactionScore(Long satisfactionScore) {
        if (satisfactionScore == null || satisfactionScore == 0) {
            return 0L; // 미평가 유지
        }
        if (satisfactionScore < 1) {
            return 1L;
        }
        if (satisfactionScore > 5) {
            return 5L;
        }
        return satisfactionScore;
    }
}