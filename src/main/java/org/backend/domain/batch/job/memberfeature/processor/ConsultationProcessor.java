package org.backend.domain.batch.job.memberfeature.processor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.backend.domain.advice.entity.Advice;
import org.backend.domain.batch.entity.ConsultationBasics;
import org.backend.domain.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@StepScope
public class ConsultationProcessor implements ItemProcessor<Member, ConsultationBasics> {

    @PersistenceContext
    private final EntityManager em;

    private final LocalDate featureBaseDate;

    public ConsultationProcessor(
            EntityManager em,
            @Value("#{jobParameters['featureBaseDate']}") String baseDateStr) {
        this.em = em;
        this.featureBaseDate = (baseDateStr != null) ? LocalDate.parse(baseDateStr) : LocalDate.now();
    }

    @Override
    public ConsultationBasics process(Member member) {
        LocalDate targetDate = this.featureBaseDate;

        ConsultationBasics basics;
        try {
            basics = em.createQuery(
                            "SELECT c FROM ConsultationBasics c WHERE c.memberId = :memberId AND c.featureBaseDate = :targetDate",
                            ConsultationBasics.class)
                    .setParameter("memberId", member.getId())
                    .setParameter("targetDate", targetDate)
                    .getSingleResult();
        } catch (NoResultException e) {
            basics = new ConsultationBasics();
            basics.setMemberId(member.getId());
            basics.setFeatureBaseDate(targetDate);
        }

        // 상담 내역 조회 (카테고리 포함)
        List<Advice> adviceList = em.createQuery(
                        "SELECT a FROM Advice a JOIN FETCH a.category WHERE a.member = :member", Advice.class)
                .setParameter("member", member)
                .getResultList();

        updateBasicsFields(basics, adviceList, targetDate);

        return basics;
    }

    private void updateBasicsFields(ConsultationBasics basics, List<Advice> adviceList, LocalDate targetDate) {
        if (adviceList.isEmpty()) {
            basics.setTotalConsultCount(0);
            basics.setLast7dConsultCount(0);
            basics.setLast30dConsultCount(0);
            basics.setAvgMonthlyConsultCount(0f);
            basics.setLastConsultDate(null);
            basics.setTopConsultCategory("None");
            basics.setTotalComplaintCount(0);
            basics.setLastConsultDaysAgo(999);
            basics.setNightConsultCount(0);
            basics.setWeekendConsultCount(0);
            return;
        }

        LocalDateTime sevenDaysAgo = targetDate.minusDays(7).atStartOfDay();
        LocalDateTime thirtyDaysAgo = targetDate.minusDays(30).atStartOfDay();

        int totalCount = adviceList.size();
        long last7d = adviceList.stream().filter(a -> a.getCreatedAt().isAfter(sevenDaysAgo)).count();
        long last30d = adviceList.stream().filter(a -> a.getCreatedAt().isAfter(thirtyDaysAgo)).count();

        LocalDateTime lastDateTime = adviceList.stream().map(Advice::getCreatedAt).max(LocalDateTime::compareTo).orElse(null);
        LocalDate lastDate = (lastDateTime != null) ? lastDateTime.toLocalDate() : null;
        int daysAgo = (lastDate != null) ? (int) ChronoUnit.DAYS.between(lastDate, targetDate) : 999;

        // 야간 상담 (22시 ~ 06시)
        int nightCount = (int) adviceList.stream()
                .filter(a -> a.getCreatedAt().getHour() >= 22 || a.getCreatedAt().getHour() < 6).count();

        // 주말 상담
        int weekendCount = (int) adviceList.stream()
                .filter(a -> {
                    DayOfWeek dow = a.getCreatedAt().getDayOfWeek();
                    return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
                }).count();

        // 가장 많이 상담한 카테고리
        String topCategory = adviceList.stream()
                .filter(a -> a.getCategory() != null)
                .collect(Collectors.groupingBy(a -> a.getCategory().getCategoryName(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        // 실제 카테고리 데이터를 기반으로 한 불만(Complaint) 집계
        // 장애, 해지, 환불, 정정, 연체, 미납 키워드가 포함된 상담을 불만 건수로 집계
        int complaintCount = (int) adviceList.stream()
                .filter(a -> {
                    if (a.getCategory() == null || a.getCategory().getCategoryName() == null) return false;
                    String categoryName = a.getCategory().getCategoryName();
                    return categoryName.contains("장애") ||
                            categoryName.contains("품질") ||
                            categoryName.contains("해지") ||
                            categoryName.contains("환불") ||
                            categoryName.contains("정정") ||
                            categoryName.contains("연체") ||
                            categoryName.contains("미납");
                })
                .count();

        basics.setTotalConsultCount(totalCount);
        basics.setLast7dConsultCount((int) last7d);
        basics.setLast30dConsultCount((int) last30d);
        basics.setAvgMonthlyConsultCount(totalCount / 12.0f); // 연간 평균 개념
        basics.setLastConsultDate(lastDate);
        basics.setTopConsultCategory(topCategory);
        basics.setTotalComplaintCount(complaintCount);
        basics.setLastConsultDaysAgo(daysAgo);
        basics.setNightConsultCount(nightCount);
        basics.setWeekendConsultCount(weekendCount);
    }
}