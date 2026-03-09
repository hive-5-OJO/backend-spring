package org.backend.domain.analysis.repository;

import org.backend.domain.analysis.dto.RfmSegmentResponseDto;
import org.backend.domain.analysis.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    Optional<Analysis> findByMemberId(Long memberId);

    @Query("SELECT MAX(a.createdAt) FROM Analysis a")
    LocalDateTime findLatestCreatedAt();

    // 전체 세그먼트별 RFM 지표 조회
    @Query("""
        SELECT new org.backend.domain.analysis.dto.RfmSegmentResponseDto$SegmentDetail(
                a.type, COUNT(a), 0.0,
                AVG(FUNCTION('DATEDIFF', a.createdAt, r.recency)),
                AVG(r.frequency),
                AVG(r.monetary)
            )
        FROM Analysis a JOIN Rfm r on a.member.id = r.memberId
        WHERE FUNCTION('DATE_FORMAT', a.createdAt, '%Y%m') = :baseMonth
        GROUP BY a.type
    """)
    List<RfmSegmentResponseDto.SegmentDetail> findRfmStatisticsBySegment(@Param("baseMonth") String baseMonth);

    // 대시보드용 - 전체 고객 ltv, 등급별 고객수 등
}
