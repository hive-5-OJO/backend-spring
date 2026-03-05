package org.backend.domain.batch.job.memberfeature.processor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.backend.domain.batch.entity.Lifecycle;
import org.backend.domain.member.entity.Member;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
@StepScope
public class MemberLifecycleProcessor implements ItemProcessor<Member, Lifecycle> {

    @PersistenceContext
    private final EntityManager em;
    private final LocalDate featureBaseDate;

    public MemberLifecycleProcessor(
            EntityManager em,
            @Value("#{jobParameters['featureBaseDate']}") String baseDateStr) {
        this.em = em;
        this.featureBaseDate = (baseDateStr != null) ? LocalDate.parse(baseDateStr) : LocalDate.now();
    }

    @Override
    public Lifecycle process(Member member) {
        LocalDate today = this.featureBaseDate;
        LocalDate signupDate = member.getCreatedAt().toLocalDate();

        // 1. 가입 후 경과일
        int lifetimeDays = (int) ChronoUnit.DAYS.between(signupDate, today);

        // 2. 마지막 활동일 조회 (기준일 이전 데이터 중 가장 최근 날짜)
        LocalDate lastActivityDate = null;
        try {
            lastActivityDate = em.createQuery(
                            "SELECT MAX(d.usageDate) FROM DataUsage d WHERE d.member.id = :memberId AND d.usageDate <= :today", LocalDate.class)
                    .setParameter("memberId", member.getId())
                    .setParameter("today", today)
                    .getSingleResult();
        } catch (Exception e) { }

        // 3. 마지막 이용 후 경과일 계산
        int daysSinceLastActivity = (lastActivityDate != null)
                ? (int) ChronoUnit.DAYS.between(lastActivityDate, today)
                : lifetimeDays;

        // 4. 남은 약정 기간 (가상 로직: 가입일로부터 2년(730일) 약정 기준)
        int totalContractDays = 730;
        int contractEndDaysLeft = Math.max(0, totalContractDays - lifetimeDays);

        return Lifecycle.builder()
                .memberId(member.getId())
                .featureBaseDate(today)
                .signupDate(signupDate)
                .memberLifetimeDays(lifetimeDays)
                .isNewCustomerFlag(lifetimeDays <= 30)
                .isDormantFlag("DORMANT".equalsIgnoreCase(member.getStatus()))
                .isTerminatedFlag("TERMINATED".equalsIgnoreCase(member.getStatus()))
                .daysSinceLastActivity(daysSinceLastActivity)
                .contractEndDaysLeft(contractEndDaysLeft)
                .build();
    }
}