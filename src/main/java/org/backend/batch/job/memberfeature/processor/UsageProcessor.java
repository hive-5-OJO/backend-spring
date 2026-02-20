package org.backend.batch.job.memberfeature.processor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.backend.entity.Member;
import org.backend.entity.feature.FeatureUsage;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UsageProcessor implements ItemProcessor<Member, FeatureUsage> {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public FeatureUsage process(Member member) {
        Long memberId = member.getId();
        LocalDate today = LocalDate.now();

        // 1. 총 사용량, 일평균 사용량, 최대 사용량 조회
        Object[] usageStats = em.createQuery(
                        "SELECT SUM(d.usageAmount), AVG(d.usageAmount), MAX(d.usageAmount), MAX(d.usageDate) " +
                                "FROM DataUsage d WHERE d.member.id = :memberId", Object[].class)
                .setParameter("memberId", memberId)
                .getSingleResult();

        Long totalUsage = (usageStats[0] != null) ? (Long) usageStats[0] : 0L;
        Double avgUsage = (usageStats[1] != null) ? (Double) usageStats[1] : 0.0;
        Long maxUsage = (usageStats[2] != null) ? (Long) usageStats[2] : 0L;
        LocalDate lastActivity = (usageStats[3] != null) ? (LocalDate) usageStats[3] : null;

        // 2. 가장 많이 사용한 시간대 (Peak Hour)
        List<Integer> peakHourList = em.createQuery(
                        "SELECT d.usageTime FROM DataUsage d WHERE d.member.id = :memberId " +
                                "GROUP BY d.usageTime ORDER BY COUNT(d.id) DESC", Integer.class)
                .setParameter("memberId", memberId)
                .setMaxResults(1)
                .getResultList();
        Integer peakHour = peakHourList.isEmpty() ? null : peakHourList.get(0);

        // 3. 프리미엄 서비스 이용 건수 (InvoiceDetail에서 'PLAN' 타입 중 고가 상품 등 조건)
        Long premiumCount = em.createQuery(
                        "SELECT COUNT(id.id) FROM InvoiceDetail id JOIN id.invoice i " +
                                "WHERE i.member.id = :memberId AND id.productType = 'PLAN' AND id.totalPrice >= 70000", Long.class)
                .setParameter("memberId", memberId)
                .getSingleResult();

        return FeatureUsage.builder()
                .memberId(memberId)
                .featureBaseDate(today)
                .totalUsageAmount(totalUsage)
                .avgDailyUsage(avgUsage.floatValue())
                .maxUsageAmount(maxUsage)
                .usagePeakHour(peakHour)
                .premiumServiceCount(premiumCount.intValue())
                .lastActivityDate(lastActivity)
                .usageActiveDays30d(30) // 실제 쿼리로 count(distinct usage_date) 필요
                .build();
    }
}