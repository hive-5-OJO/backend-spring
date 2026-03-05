package org.backend.domain.batch.job.memberfeature.processor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.backend.domain.batch.entity.Monetary;
import org.backend.domain.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@StepScope
public class MonetaryProcessor implements ItemProcessor<Member, Monetary> {

    @PersistenceContext
    private final EntityManager em;
    private final LocalDate featureBaseDate;

    public MonetaryProcessor(
            EntityManager em,
            @Value("#{jobParameters['featureBaseDate']}") String baseDateStr) {
        this.em = em;
        this.featureBaseDate = (baseDateStr != null) ? LocalDate.parse(baseDateStr) : LocalDate.now();
    }

    @Override
    public Monetary process(Member member) {
        Long memberId = member.getId();
        LocalDate targetDate = this.featureBaseDate;
        LocalDateTime targetLimit = targetDate.atTime(23, 59, 59);
        LocalDateTime sixMonthsAgo = targetDate.minusMonths(6).atStartOfDay();

        // 1. 전체 매출 통계
        List<Object[]> totalStatsList = em.createQuery(
                        "SELECT SUM(i.billedAmount), AVG(i.billedAmount), COUNT(i.id) " +
                                "FROM Invoice i WHERE i.member.id = :memberId AND i.createdAt <= :targetLimit", Object[].class)
                .setParameter("memberId", memberId)
                .setParameter("targetLimit", targetLimit)
                .getResultList();

        Object[] totalStats = (!totalStatsList.isEmpty()) ? totalStatsList.get(0) : new Object[]{0L, 0.0, 0L};
        long totalRevenue = (totalStats[0] != null) ? ((Number) totalStats[0]).longValue() : 0L;
        double avgOrderVal = (totalStats[1] != null) ? ((Number) totalStats[1]).doubleValue() : 0.0;

        // 2. 최근 6개월 평균 청구액
        List<Double> sixMonthAvgList = em.createQuery(
                        "SELECT AVG(i.billedAmount) FROM Invoice i " +
                                "WHERE i.member.id = :memberId AND i.createdAt BETWEEN :sixMonthsAgo AND :targetLimit", Double.class)
                .setParameter("memberId", memberId)
                .setParameter("sixMonthsAgo", sixMonthsAgo)
                .setParameter("targetLimit", targetLimit)
                .getResultList();
        double avgMonthlyBill6m = (!sixMonthAvgList.isEmpty() && sixMonthAvgList.get(0) != null) ? sixMonthAvgList.get(0) : 0.0;

        // 3. 마지막 결제 정보
        List<Object[]> lastPaymentList = em.createQuery(
                        "SELECT p.paidAmount, p.paidAt FROM Payment p JOIN p.invoice i " +
                                "WHERE i.member.id = :memberId AND p.paidAt <= :targetLimit ORDER BY p.paidAt DESC", Object[].class)
                .setParameter("memberId", memberId)
                .setParameter("targetLimit", targetLimit)
                .setMaxResults(1)
                .getResultList();

        long lastAmount = 0L;
        LocalDate lastDate = null;
        if (!lastPaymentList.isEmpty()) {
            lastAmount = (lastPaymentList.get(0)[0] != null) ? ((Number) lastPaymentList.get(0)[0]).longValue() : 0L;
            lastDate = (lastPaymentList.get(0)[1] != null) ? ((LocalDateTime) lastPaymentList.get(0)[1]).toLocalDate() : null;
        }

        // 4. 연체 횟수
        List<Long> delayCountList = em.createQuery(
                        "SELECT COUNT(i.id) FROM Invoice i WHERE i.member.id = :memberId AND i.overdueAmount > 0 AND i.createdAt <= :targetLimit", Long.class)
                .setParameter("memberId", memberId)
                .setParameter("targetLimit", targetLimit)
                .getResultList();
        int delayCount = (!delayCountList.isEmpty() && delayCountList.get(0) != null) ? delayCountList.get(0).intValue() : 0;

        // 5. [수정] 구매 주기 계산: 엔티티 연관관계 없이 직접 조인 쿼리 수행
        int purchaseCycle = 0;
        try {
            // InvoiceDetail(id)을 직접 명시하여 조인 오류 해결
            List<Object[]> addonPaymentStats = em.createQuery(
                            "SELECT COUNT(DISTINCT p.id), MIN(p.paidAt), MAX(p.paidAt) " +
                                    "FROM Payment p " +
                                    "JOIN p.invoice i, InvoiceDetail id " + // 연관관계 대신 직접 테이블 나열 (Cross Join + Where)
                                    "JOIN id.product pr " +
                                    "WHERE i.member.id = :memberId " +
                                    "AND id.invoice = i " + // 여기서 직접 연결
                                    "AND pr.productCategory != 'BASE' " +
                                    "AND p.paidAt <= :targetLimit", Object[].class)
                    .setParameter("memberId", memberId)
                    .setParameter("targetLimit", targetLimit)
                    .getResultList();

            if (!addonPaymentStats.isEmpty() && addonPaymentStats.get(0)[0] != null) {
                long payCount = (long) addonPaymentStats.get(0)[0];
                LocalDateTime minPaid = (LocalDateTime) addonPaymentStats.get(0)[1];
                LocalDateTime maxPaid = (LocalDateTime) addonPaymentStats.get(0)[2];

                if (payCount >= 2 && minPaid != null && maxPaid != null) {
                    long totalDays = ChronoUnit.DAYS.between(minPaid.toLocalDate(), maxPaid.toLocalDate());
                    purchaseCycle = (int) (totalDays / (payCount - 1));
                }
            }
        } catch (Exception e) {
            purchaseCycle = 0; // 에러 시 건너뜀
        }

        // 6. 최근 6개월 결제 건수
        Long payCount6m = em.createQuery(
                        "SELECT COUNT(p.id) FROM Payment p JOIN p.invoice i " +
                                "WHERE i.member.id = :memberId AND p.paidAt BETWEEN :sixMonthsAgo AND :targetLimit", Long.class)
                .setParameter("memberId", memberId)
                .setParameter("sixMonthsAgo", sixMonthsAgo)
                .setParameter("targetLimit", targetLimit)
                .getSingleResult();

        // 7. 당월/전월 매출
        String currentMonthStr = targetDate.getYear() + String.format("%02d", targetDate.getMonthValue());
        String prevMonthStr = targetDate.minusMonths(1).getYear() + String.format("%02d", targetDate.minusMonths(1).getMonthValue());

        List<Long> currRes = em.createQuery("SELECT SUM(i.billedAmount) FROM Invoice i WHERE i.member.id = :memberId AND i.baseMonth = :bm", Long.class)
                .setParameter("memberId", memberId).setParameter("bm", currentMonthStr).getResultList();
        long monthlyRevenue = (!currRes.isEmpty() && currRes.get(0) != null) ? currRes.get(0) : 0L;

        List<Long> prevRes = em.createQuery("SELECT SUM(i.billedAmount) FROM Invoice i WHERE i.member.id = :memberId AND i.baseMonth = :bm", Long.class)
                .setParameter("memberId", memberId).setParameter("bm", prevMonthStr).getResultList();
        long prevMonthlyRevenue = (!prevRes.isEmpty() && prevRes.get(0) != null) ? prevRes.get(0) : 0L;

        return Monetary.builder()
                .memberId(memberId)
                .featureBaseDate(targetDate)
                .totalRevenue(totalRevenue)
                .lastPaymentAmount(lastAmount)
                .avgMonthlyBill((float)avgMonthlyBill6m)
                .lastPaymentDate(lastDate)
                .paymentCount6m(payCount6m != null ? payCount6m.intValue() : 0)
                .monthlyRevenue(monthlyRevenue)
                .paymentDelayCount(delayCount)
                .prevMonthlyRevenue(prevMonthlyRevenue)
                .purchaseCycle(purchaseCycle)
                .vipPrevMonth(prevMonthlyRevenue >= 100000)
                .avgOrderVal((float)avgOrderVal)
                .build();
    }
}