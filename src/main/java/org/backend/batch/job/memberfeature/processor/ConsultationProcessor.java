//package org.backend.batch.job.memberfeature.processor;
//
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import lombok.RequiredArgsConstructor;
//import org.backend.entity.Advice;
//import org.backend.entity.Member;
//import org.backend.entity.feature.ConsultationBasics;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Component
//@RequiredArgsConstructor
//public class ConsultationProcessor implements ItemProcessor<Member, ConsultationBasics> {
//
//    @PersistenceContext
//    private final EntityManager em;
//
//    @Override
//    public ConsultationBasics process(Member member) {
//        // 해당 회원의 상담 이력 조회 (카테고리 정보 포함)
//        List<Advice> adviceList = em.createQuery(
//                        "SELECT a FROM Advice a JOIN FETCH a.category WHERE a.member = :member", Advice.class)
//                .setParameter("member", member)
//                .getResultList();
//
//        // 상담 내역이 전혀 없는 경우
//        if (adviceList.isEmpty()) {
//            return createEmptyBasics(member);
//        }
//
//        LocalDate today = LocalDate.now();
//        LocalDateTime sevenDaysAgo = today.minusDays(7).atStartOfDay();
//        LocalDateTime thirtyDaysAgo = today.minusDays(30).atStartOfDay();
//
//        // 1. 전체 상담 횟수
//        int totalCount = adviceList.size();
//
//        // 2. 최근 7일 / 30일 상담 횟수
//        long last7d = adviceList.stream()
//                .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(sevenDaysAgo)).count();
//
//        long last30d = adviceList.stream()
//                .filter(a -> a.getCreatedAt() != null && a.getCreatedAt().isAfter(thirtyDaysAgo)).count();
//
//        // 3. 마지막 상담일 및 경과 일수
//        LocalDateTime lastConsultDateTime = adviceList.stream()
//                .map(Advice::getCreatedAt)
//                .max(LocalDateTime::compareTo)
//                .orElse(null);
//
//        LocalDate lastConsultDate = (lastConsultDateTime != null) ? lastConsultDateTime.toLocalDate() : null;
//        int daysAgo = (lastConsultDate != null) ? (int) ChronoUnit.DAYS.between(lastConsultDate, today) : 999;
//
//        // 4. 주상담 카테고리 (가장 많이 상담한 카테고리 명)
//        String topCategory = adviceList.stream()
//                .filter(a -> a.getCategory() != null)
//                .collect(Collectors.groupingBy(a -> a.getCategory().getCategoryName(), Collectors.counting()))
//                .entrySet().stream()
//                .max(Map.Entry.comparingByValue())
//                .map(Map.Entry::getKey)
//                .orElse("None");
//
//        // 결과 객체 생성 (모든 필드를 명시적으로 채움)
//        return ConsultationBasics.builder()
//                .memberId(member.getId())
//                .featureBaseDate(today)
//                .totalConsultCount(totalCount)
//                .last7dConsultCount((int) last7d)
//                .last30dConsultCount((int) last30d)
//                .avgMonthlyConsultCount(totalCount / 12.0f) // 예시 로직
//                .lastConsultDate(lastConsultDate)
//                .topConsultCategory(topCategory)
//                .totalComplaintCount(0) // 에러 발생 지점: 반드시 0이라도 넣어줘야 함
//                .lastConsultDaysAgo(daysAgo)
//                .build();
//    }
//
//    // 상담 내역이 없는 신규 고객 등을 위한 기본 데이터 생성기
//    private ConsultationBasics createEmptyBasics(Member member) {
//        return ConsultationBasics.builder()
//                .memberId(member.getId())
//                .featureBaseDate(LocalDate.now())
//                .totalConsultCount(0)
//                .last7dConsultCount(0)
//                .last30dConsultCount(0)
//                .avgMonthlyConsultCount(0f)
//                .lastConsultDate(null)
//                .topConsultCategory("None")
//                .totalComplaintCount(0) // 에러 방지를 위해 0으로 초기화
//                .lastConsultDaysAgo(999)
//                .build();
//    }
//}



package org.backend.batch.job.memberfeature.processor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.backend.entity.Advice;
import org.backend.entity.Member;
import org.backend.entity.feature.ConsultationBasics;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConsultationProcessor implements ItemProcessor<Member, ConsultationBasics> {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public ConsultationBasics process(Member member) {
        LocalDate today = LocalDate.now();

        //  오늘 날짜의 해당 회원 데이터가 이미 있는지 DB에서 먼저 조회
        ConsultationBasics basics;
        try {
            basics = em.createQuery(
                            "SELECT c FROM ConsultationBasics c WHERE c.memberId = :memberId AND c.featureBaseDate = :today",
                            ConsultationBasics.class)
                    .setParameter("memberId", member.getId())
                    .setParameter("today", today)
                    .getSingleResult();
        } catch (NoResultException e) {
            // 없으면 새로 생성 (INSERT 준비)
            basics = new ConsultationBasics();
            basics.setMemberId(member.getId());
            basics.setFeatureBaseDate(today);
        }

        // 1. 상담 이력 조회
        List<Advice> adviceList = em.createQuery(
                        "SELECT a FROM Advice a JOIN FETCH a.category WHERE a.member = :member", Advice.class)
                .setParameter("member", member)
                .getResultList();

        // 2. 상담 이력에 따른 필드 계산 및 세팅 (기존 객체 혹은 새 객체의 필드를 업데이트)
        updateBasicsFields(basics, adviceList, today);

        return basics; // ID가 포함된 객체를 반환하면 Writer가 자동으로 UPDATE를 수행함
    }

    private void updateBasicsFields(ConsultationBasics basics, List<Advice> adviceList, LocalDate today) {
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

        LocalDateTime sevenDaysAgo = today.minusDays(7).atStartOfDay();
        LocalDateTime thirtyDaysAgo = today.minusDays(30).atStartOfDay();

        // 1. 카운트 및 날짜 계산
        int totalCount = adviceList.size();
        long last7d = adviceList.stream().filter(a -> a.getCreatedAt().isAfter(sevenDaysAgo)).count();
        long last30d = adviceList.stream().filter(a -> a.getCreatedAt().isAfter(thirtyDaysAgo)).count();

        LocalDateTime lastDateTime = adviceList.stream().map(Advice::getCreatedAt).max(LocalDateTime::compareTo).orElse(null);
        LocalDate lastDate = (lastDateTime != null) ? lastDateTime.toLocalDate() : null;
        int daysAgo = (lastDate != null) ? (int) ChronoUnit.DAYS.between(lastDate, today) : 999;

        // 2. 야간 및 주말 상담 계산 (추가된 필드 로직)
        int nightCount = (int) adviceList.stream()
                .filter(a -> a.getCreatedAt().getHour() >= 22 || a.getCreatedAt().getHour() < 6).count();

        int weekendCount = (int) adviceList.stream()
                .filter(a -> {
                    DayOfWeek dow = a.getCreatedAt().getDayOfWeek();
                    return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
                }).count();

        // 3. 주상담 카테고리
        String topCategory = adviceList.stream()
                .filter(a -> a.getCategory() != null)
                .collect(Collectors.groupingBy(a -> a.getCategory().getCategoryName(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        // 4. 불만 상담 건수 (카테고리명이 '불만'인 경우 예시)
        int complaintCount = (int) adviceList.stream()
                .filter(a -> a.getCategory() != null && "불만".equals(a.getCategory().getCategoryName()))
                .count();

        // 객체 필드 업데이트
        basics.setTotalConsultCount(totalCount);
        basics.setLast7dConsultCount((int) last7d);
        basics.setLast30dConsultCount((int) last30d);
        basics.setAvgMonthlyConsultCount(totalCount / 12.0f);
        basics.setLastConsultDate(lastDate);
        basics.setTopConsultCategory(topCategory);
        basics.setTotalComplaintCount(complaintCount);
        basics.setLastConsultDaysAgo(daysAgo);
        basics.setNightConsultCount(nightCount);
        basics.setWeekendConsultCount(weekendCount);
    }
}