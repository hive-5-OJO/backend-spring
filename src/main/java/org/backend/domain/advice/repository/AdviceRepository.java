package org.backend.domain.advice.repository;

import java.util.List;
import org.backend.domain.advice.entity.Advice;
import org.backend.domain.advice.dto.AdviceTimeStatResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdviceRepository extends JpaRepository<Advice, Long> {

    // 고객별 상담 타임라인 (최신순 정렬)
    @Query("SELECT a FROM Advice a JOIN FETCH a.admin JOIN FETCH a.category WHERE a.member.id = :memberId ORDER BY a.createdAt DESC")
    List<Advice> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    // 시간대별 상담 통계
    @Query(value = "SELECT HOUR(created_at) AS hour, COUNT(advice_id) AS count " +
            "FROM advice " +
            "GROUP BY HOUR(created_at) " +
            "ORDER BY hour ASC", nativeQuery = true)
    List<AdviceTimeStatResponse> countAdviceByHour();
}