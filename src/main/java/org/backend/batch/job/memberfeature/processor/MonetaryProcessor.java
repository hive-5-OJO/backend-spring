package org.backend.batch.job.memberfeature.processor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.backend.entity.Member;
import org.backend.entity.feature.Monetary;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonetaryProcessor implements ItemProcessor<Member, Monetary> {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public Monetary process(Member member) {
        Long memberId = member.getId();
        LocalDate today = LocalDate.now();

        // 1. 전체 매출 합계 및 월평균 청구액 (Invoice 기반)
        // total_revenue, avg_monthly_bill
        Object[] invoiceStats = em.createQuery(
                        "SELECT SUM(i.billedAmount), AVG(i.billedAmount) FROM Invoice i WHERE i.member.id = :memberId", Object[].class)
                .setParameter("memberId", memberId)
                .getSingleResult();

        Long totalRevenue = (invoiceStats[0] != null) ? (Long) invoiceStats[0] : 0L;
        Double avgMonthlyBill = (invoiceStats[1] != null) ? (Double) invoiceStats[1] : 0.0;

        // 2. 마지막 결제 정보 및 6개월 내 결제 횟수 (Payment 기반)
        // last_payment_amount, last_payment_date, payment_count_6m
        LocalDateTime sixMonthsAgo = today.minusMonths(6).atStartOfDay();

        List<Object[]> paymentStats = em.createQuery(
                        "SELECT p.paidAmount, p.paidAt FROM Payment p " +
                                "JOIN p.invoice i WHERE i.member.id = :memberId " +
                                "ORDER BY p.paidAt DESC", Object[].class)
                .setParameter("memberId", memberId)
                .setMaxResults(1)
                .getResultList();

        Long lastAmount = 0L;
        LocalDate lastDate = null;
        if (!paymentStats.isEmpty()) {
            lastAmount = (Long) paymentStats.get(0)[0];
            lastDate = ((LocalDateTime) paymentStats.get(0)[1]).toLocalDate();
        }

        Long payCount6m = em.createQuery(
                        "SELECT COUNT(p.id) FROM Payment p JOIN p.invoice i " +
                                "WHERE i.member.id = :memberId AND p.paidAt >= :sixMonthsAgo", Long.class)
                .setParameter("memberId", memberId)
                .setParameter("sixMonthsAgo", sixMonthsAgo)
                .getSingleResult();

        // 3. 당월 매출 및 전월 매출 (Invoice 기준)
        String currentMonth = today.getYear() + String.format("%02d", today.getMonthValue());
        String prevMonth = today.minusMonths(1).getYear() + String.format("%02d", today.minusMonths(1).getMonthValue());

        Long monthlyRevenue = em.createQuery(
                        "SELECT SUM(i.billedAmount) FROM Invoice i WHERE i.member.id = :memberId AND i.baseMonth = :baseMonth", Long.class)
                .setParameter("memberId", memberId)
                .setParameter("baseMonth", currentMonth)
                .getSingleResult();

        Long prevMonthlyRevenue = em.createQuery(
                        "SELECT SUM(i.billedAmount) FROM Invoice i WHERE i.member.id = :memberId AND i.baseMonth = :baseMonth", Long.class)
                .setParameter("memberId", memberId)
                .setParameter("baseMonth", prevMonth)
                .getSingleResult();

        // 4. 연체 횟수 (overdue_amount 가 0보다 큰 Invoice 개수)
        Long delayCount = em.createQuery(
                        "SELECT COUNT(i.id) FROM Invoice i WHERE i.member.id = :memberId AND i.overdueAmount > 0", Long.class)
                .setParameter("memberId", memberId)
                .getSingleResult();

        return Monetary.builder()
                .memberId(memberId)
                .featureBaseDate(today)
                .totalRevenue(totalRevenue)
                .lastPaymentAmount(lastAmount)
                .avgMonthlyBill(avgMonthlyBill.floatValue())
                .lastPaymentDate(lastDate)
                .paymentCount6m(payCount6m.intValue())
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : 0L)
                .paymentDelayCount(delayCount.intValue())
                .prevMonthlyRevenue(prevMonthlyRevenue != null ? prevMonthlyRevenue : 0L)
                .isVipPrevMonth(totalRevenue > 500000) // 누적 50만 이상 시 임의로 VIP 판정
                .avgOrderVal(avgMonthlyBill.floatValue()) // Invoice 기반이라 동일하게 설정
                .purchaseCycle(30) // 기본 30일 주기 설정
                .build();
    }
}