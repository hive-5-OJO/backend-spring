package org.backend.domain.batch.job.memberfeature.processor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.backend.domain.batch.entity.FeatureUsage;
import org.backend.domain.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@StepScope
public class UsageProcessor implements ItemProcessor<Member, FeatureUsage> {

    @PersistenceContext
    private final EntityManager em;
    private final LocalDate featureBaseDate;

    public UsageProcessor(
            EntityManager em,
            @Value("#{jobParameters['featureBaseDate']}") String baseDateStr) {
        this.em = em;
        this.featureBaseDate = (baseDateStr != null) ? LocalDate.parse(baseDateStr) : LocalDate.now();
    }

    @Override
    public FeatureUsage process(Member member) {
        Long memberId = member.getId();
        LocalDate targetDate = this.featureBaseDate;

        // 1. 전체 사용량 통계
        List<Object[]> statsList = em.createQuery(
                        "SELECT SUM(d.usageAmount), AVG(d.usageAmount), MAX(d.usageAmount), MAX(d.usageDate) " +
                                "FROM DataUsage d WHERE d.member.id = :memberId AND d.usageDate <= :targetDate", Object[].class)
                .setParameter("memberId", memberId)
                .setParameter("targetDate", targetDate)
                .getResultList();

        Object[] usageStats = (!statsList.isEmpty()) ? statsList.get(0) : new Object[]{0L, 0.0, 0L, null};
        long totalUsage = (usageStats[0] != null) ? ((Number) usageStats[0]).longValue() : 0L;
        double avgUsage = (usageStats[1] != null) ? ((Number) usageStats[1]).doubleValue() : 0.0;
        long maxUsage = (usageStats[2] != null) ? ((Number) usageStats[2]).longValue() : 0L;
        LocalDate lastActivity = (usageStats[3] != null) ? (LocalDate) usageStats[3] : null;

        // 2. 활동 일수 (최근 30일)
        LocalDate thirtyDaysAgo = targetDate.minusDays(30);
        Long activeDaysCount = em.createQuery(
                        "SELECT COUNT(DISTINCT d.usageDate) FROM DataUsage d " +
                                "WHERE d.member.id = :memberId AND d.usageDate BETWEEN :startDate AND :endDate", Long.class)
                .setParameter("memberId", memberId)
                .setParameter("startDate", thirtyDaysAgo)
                .setParameter("endDate", targetDate)
                .getSingleResult();
        int activeDays = (activeDaysCount != null) ? activeDaysCount.intValue() : 0;

        // 3. 프리미엄 서비스 카운트 (설계 반영)
        // 기본 요금제(BASE)를 제외한 모든 유료 부가 상품(결합 기기, 부가서비스, 일시불 상품)의 개수를 집계
        // InvoiceDetail이 Product 정보를 직접 들고 있거나 Join이 가능하다는 전제하에 작성
        List<Long> premiumList = em.createQuery(
                        "SELECT COUNT(id.id) FROM InvoiceDetail id JOIN id.invoice i " +
                                "WHERE i.member.id = :memberId " +
                                "AND id.productType NOT IN ('BASE')", Long.class) // BASE만 제외하고 모두 카운트
                .setParameter("memberId", memberId)
                .getResultList();
        int premiumCount = (!premiumList.isEmpty() && premiumList.get(0) != null) ? premiumList.get(0).intValue() : 0;

        // 4. 피크 타임
        List<Integer> peakHourList = em.createQuery(
                        "SELECT d.usageTime FROM DataUsage d WHERE d.member.id = :memberId AND d.usageDate <= :targetDate " +
                                "GROUP BY d.usageTime ORDER BY COUNT(d.id) DESC", Integer.class)
                .setParameter("memberId", memberId)
                .setParameter("targetDate", targetDate)
                .setMaxResults(1)
                .getResultList();
        Integer peakHour = (peakHourList != null && !peakHourList.isEmpty()) ? peakHourList.get(0) : 0;

        return FeatureUsage.builder()
                .memberId(memberId)
                .featureBaseDate(targetDate)
                .totalUsageAmount(totalUsage)
                .avgDailyUsage((float)avgUsage)
                .maxUsageAmount(maxUsage)
                .usagePeakHour(peakHour)
                .premiumServiceCount(premiumCount)
                .lastActivityDate(lastActivity)
                .usageActiveDays30d(activeDays)
                .build();
    }
}