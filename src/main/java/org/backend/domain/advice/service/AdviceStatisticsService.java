package org.backend.domain.advice.service;

import org.backend.domain.advice.dto.AdviceCategoryRatioItem;
import org.backend.domain.advice.dto.AdviceCategoryRatioResponse;
import org.backend.domain.advice.repository.AdviceStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdviceStatisticsService {

    private final AdviceStatisticsRepository adviceStatisticsRepository;

    public AdviceStatisticsService(AdviceStatisticsRepository adviceStatisticsRepository) {
        this.adviceStatisticsRepository = adviceStatisticsRepository;
    }

    @Transactional(readOnly = true)
    public AdviceCategoryRatioResponse getCategoryRatios(LocalDate from, LocalDate to) {
        // startInclusive
        LocalDateTime fromDt = (from == null) ? null : from.atStartOfDay();

        // endExclusive (to 날짜 포함을 위해 다음날 0시로)
        LocalDateTime toExclusive = (to == null) ? null : to.plusDays(1).atStartOfDay();

        long total = adviceStatisticsRepository.totalCount(fromDt, toExclusive);
        List<AdviceCategoryRatioItem> raw = adviceStatisticsRepository.countByCategory(fromDt, toExclusive);

        // ratio 계산
        List<AdviceCategoryRatioItem> items = raw.stream()
                .map(i -> new AdviceCategoryRatioItem(
                        i.categoryId(),
                        i.categoryName(),
                        i.count(),
                        total == 0 ? 0.0 : (i.count() * 100.0 / total)
                ))
                .toList();

        return new AdviceCategoryRatioResponse(total, items);
    }
}