package org.backend.domain.advice.service;

import org.backend.domain.advice.dto.*;
import org.backend.domain.advice.repository.AdviceStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

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
                        r.getRatio()))
                .toList();

        return new AdviceCategoryRatioResponse(total, items);
    }

    // 상담 만족도 통계
    public AdviceSatisfactionResponse getSatisfactionStatistics(LocalDate from, LocalDate to) {
        LocalDateTime start = from != null ? from.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = to != null ? to.atTime(23, 59, 59) : LocalDateTime.now();

        Map<String, Object> summary = repo.findSatisfaction(start, end);
        Double score = ((Number) summary.get("averageScore")).doubleValue();
        Long cnt = ((Number) summary.get("totalCount")).longValue();

        List<Object[]> rawDistribution = repo.findScoreDistribution(start, end);

        Map<Long, Long> scoreMap = rawDistribution.stream().collect(
                Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()));

        // 없는 거는 0으로 처리
        List<AdviceSatisfactionResponse.SatisfactionScoreCount> dis = LongStream.rangeClosed(1, 10)
                .mapToObj(s -> new AdviceSatisfactionResponse.SatisfactionScoreCount(
                        s,
                        scoreMap.getOrDefault(s, 0L)))
                .toList();

        return new AdviceSatisfactionResponse(score, cnt, dis);
    }

    // 기본 한달치로 상담사 성과 조회
    public List<AdvicePerformanceRow> getAdminPerformance(LocalDate from, LocalDate to) {
        LocalDate startDate = (from != null) ? from : LocalDate.now().minusMonths(1);
        LocalDate endDate = (to != null) ? to : LocalDate.now();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        return repo.getPerformanceByAdmin(start, end);
    }

    public List<HourlyConsultationDto> getHourlyStatistics() {
        List<Object[]> results = repo.findHourlyStatistics();
        return results.stream().map(row -> new HourlyConsultationDto(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue())).collect(Collectors.toList());
    }
}
