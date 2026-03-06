package org.backend.domain.advice.repository;

import org.backend.domain.advice.dto.AdviceCategoryRatioRow;
import org.backend.domain.advice.dto.AdvicePerformanceRow;
import org.backend.domain.advice.entity.Advice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdviceStatisticsRepository extends JpaRepository<Advice, Long> {

    @Query(value = """
   SELECT
     c.category_id   AS categoryId,
     c.category_name AS categoryName,
     COUNT(*)        AS count,
     SUM(COUNT(*)) OVER() AS totalCount,
     ROUND((COUNT(*) / SUM(COUNT(*)) OVER()) * 100, 2) AS ratio
   FROM advice a
   JOIN categories c ON c.category_id = a.category_id
   WHERE a.created_at >= :startDt
     AND a.created_at <  :endDt
   GROUP BY c.category_id, c.category_name
   ORDER BY count DESC
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

    // 상담사별 성과
    // advice 테이블에 root category id를 넣는 방식으로 하면 재귀 쿼리는 없어질 수 있지만
    // 더미로 진행하기에 수정하지 않음
    @Query(value = """
        WITH RECURSIVE CategoryPath as(
            SELECT category_id, category_name, category_id as root_id, category_name as root_name, parent_id
            FROM categories
            WHERE parent_id IS NULL
            UNION ALL
            SELECT c.category_id, c.category_name, cp.root_id, cp.root_name, c.parent_id
            FROM categories c JOIN CategoryPath cp ON c.parent_id = cp.category_id
        ),
                
        Stats as(
            SELECT
                a.admin_id, COUNT(*) as cnt, AVG(a.satisfactionScore) as avg_sat,
                AVG(TIMESTAMPDIFF(SECOND, a.startAt, a.endAt)) as avg_dur,
                SUM(CASE WHEN a.direction = 'IN' THEN 1 ELSE 0 END) as in_cnt,
                SUM(CASE WHEN a.direction = 'OUT' THEN 1 ELSE 0 END) as out_cnt
            FROM advice a
            WHERE a.created_at BETWEEN :start AND :end
            GROUP BY a.admin_id
        ),
                                                
        MainCategory as(
            SELECT admin_id, root_name, category_cnt
            FROM (
                SELECT a.admin_id, cp.root_name, COUNT(*) as category_cnt,
                       ROW_NUMBER() OVER(PARTITION BY a.admin_id ORDER BY COUNT(*) DESC) as rnk
                FROM advice a JOIN CategoryPath cp on a.category_id = cp.category_id
                GROUP BY a.admin_id, cp.root_name
            ) t WHERE rnk = 1
        )
                           
        SELECT ad.admin_id as adminId, ad.name as adminName, ad.role as role,
               COALESCE(s.cnt, 0) as totalCount,
               COALESCE(s.avg_sat, 0.0) as avgSatisfaction,
               COALESCE(s.avg_dur, 0.0) as avgDurationSeconds,
               COALESCE(s.in_cnt, 0) as inboundCount,
               COALESCE(s.out_cnt, 0) as outboundCount,
               mc.root_name as mainCategory,
               COALESCE(mc.category_cnt, 0) as mainCategoryCount
        FROM admin ad
            LEFT JOIN Stats s ON ad.admin_id = s.admin_id
            LEFT JOIN MainCategory mc ON ad.admin_id = mc.admin_id
        WHERE ad.status = 'ACTIVE'
    """, nativeQuery = true)
    List<AdvicePerformanceRow> getPerformanceByAdmin(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);
}