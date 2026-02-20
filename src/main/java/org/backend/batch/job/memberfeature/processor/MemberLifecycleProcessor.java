package org.backend.batch.job.memberfeature.processor;

import org.backend.entity.Member;
import org.backend.entity.feature.Lifecycle;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class MemberLifecycleProcessor implements ItemProcessor<Member, Lifecycle> {

    @Override
    public Lifecycle process(Member member) {
        LocalDate today = LocalDate.now();
        LocalDate signupDate = member.getCreatedAt().toLocalDate();

        // 가입 후 경과일
        int lifetimeDays = (int) ChronoUnit.DAYS.between(signupDate, today);

        return Lifecycle.builder()
                .memberId(member.getId())
                .featureBaseDate(today)
                .signupDate(signupDate)
                .memberLifetimeDays(lifetimeDays)
                .isNewCustomerFlag(lifetimeDays <= 30) // 가입 30일 이내
                .isDormantFlag("DORMANT".equalsIgnoreCase(member.getStatus()))
                .isTerminatedFlag("TERMINATED".equalsIgnoreCase(member.getStatus()))
                .daysSinceLastActivity(0) // UsageProcessor에서 활동일을 관리하므로 여기서는 0 또는 별도 조회
                .contractEndDaysLeft(365) // 실제 계약 정보 테이블이 있다면 연동
                .build();
    }
}