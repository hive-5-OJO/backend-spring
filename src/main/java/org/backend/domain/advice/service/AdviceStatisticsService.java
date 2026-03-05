package org.backend.domain.advice.service;

import org.backend.domain.advice.dto.AdviceCategoryRatioItem;
import org.backend.domain.advice.dto.AdviceCategoryRatioResponse;
import org.backend.domain.advice.dto.AdviceCategoryRatioRow;
import org.backend.domain.advice.repository.AdviceStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdviceStatisticsService {

    private final AdviceStatisticsRepository repo;

    public AdviceStatisticsService(AdviceStatisticsRepository adviceStatisticsRepository) {
        this.repo = adviceStatisticsRepository;
    }

    @Transactional(readOnly = true)
    public AdviceCategoryRatioResponse getCategoryRatios(LocalDate from, LocalDate to) {
        LocalDate start = (from != null) ? from : (to != null ? to : LocalDate.now());
        LocalDate end = (to != null) ? to : start;

        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.plusDays(1).atStartOfDay(); // [start, end)

        List<AdviceCategoryRatioRow> rows = repo.findCategoryRatio(startDt, endDt);

        long total = rows.isEmpty() ? 0L : rows.get(0).getTotalCount();

        List<AdviceCategoryRatioItem> items = rows.stream()
                .map(r -> new AdviceCategoryRatioItem(
                        r.getCategoryId(),
                        r.getCategoryName(),
                        r.getCount(),
                        r.getRatio()
                ))
                .toList();

        return new AdviceCategoryRatioResponse(total, items);
    }
}