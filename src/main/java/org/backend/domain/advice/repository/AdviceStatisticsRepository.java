package org.backend.domain.advice.repository;

import org.backend.domain.advice.dto.AdviceCategoryRatioRow;
import org.backend.domain.advice.entity.Advice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdviceStatisticsRepository extends JpaRepository<Advice, Long> {

    @Query(value = """
   SELECT
     c.category_name AS category,
     COUNT(*) AS cnt,
     SUM(COUNT(*)) OVER() AS totalCount,
     ROUND((COUNT(*) / SUM(COUNT(*)) OVER()) * 100, 2) AS ratio
   FROM advice a
   JOIN categories c ON c.category_id = a.category_id
   WHERE a.created_at >= :startDt
     AND a.created_at <  :endDt
   GROUP BY c.category_name
   ORDER BY cnt DESC
   """, nativeQuery = true)
    List<AdviceCategoryRatioRow> findCategoryRatio(
            @Param("startDt") LocalDateTime startDt,
            @Param("endDt") LocalDateTime endDt
    );

    @Query("""
        select count(a)
        from Advice a
        where (:from is null or a.createdAt >= :from)
          and (:toExclusive is null or a.createdAt < :toExclusive)
    """)
    long totalCount(
            @Param("from") LocalDateTime from,
            @Param("toExclusive") LocalDateTime toExclusive
    );
}