package org.backend.domain.advice.service;

import org.backend.domain.advice.dto.AdviceCategoryRatioItem;
import org.backend.domain.advice.dto.AdviceCategoryRatioResponse;
import org.backend.domain.advice.dto.AdviceCategoryRatioRow;
import org.backend.domain.advice.dto.AdvicePerformanceRow;
import org.backend.domain.advice.repository.AdviceStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdviceStatisticsService {

    private final AdviceStatisticsRepository repo;

    public AdviceStatisticsService(AdviceStatisticsRepository adviceStatisticsRepository) {
        this.repo = adviceStatisticsRepository;
    }

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

    // 기본 한달치로 상담사 성과 조회
    public List<AdvicePerformanceRow> getAdminPerformance(LocalDate from, LocalDate to) {
        LocalDate startDate = (from != null) ? from : LocalDate.now().minusMonths(1);
        LocalDate endDate = (to != null) ? to : LocalDate.now();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return repo.getPerformanceByAdmin(start, end);
    }
}