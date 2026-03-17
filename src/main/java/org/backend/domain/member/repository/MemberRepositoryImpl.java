package org.backend.domain.member.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.backend.domain.advice.entity.QAdvice;
import org.backend.domain.analysis.entity.QAnalysis;
import org.backend.domain.analysis.entity.QRfm;
import org.backend.domain.member.dto.CustomerFilterRequest;
import org.backend.domain.member.dto.CustomerFilterResponse;
import org.backend.domain.member.entity.QMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CustomerFilterResponse> findFilteredCustomers(CustomerFilterRequest request, Pageable pageable) {
        QMember member = QMember.member;
        QAnalysis analysis = QAnalysis.analysis;
        QRfm rfm = QRfm.rfm;
        QAdvice advice = QAdvice.advice;

        List<CustomerFilterResponse> content = queryFactory
                .select(Projections.constructor(CustomerFilterResponse.class,
                        member.id,
                        member.name,
                        Expressions.nullExpression(String.class),
                        Expressions.nullExpression(String.class),
                        member.createdAt,
                        com.querydsl.core.types.ExpressionUtils.as(JPAExpressions.select(advice.id.count()).from(advice).where(advice.member.eq(member)), "frequency"),
                        Expressions.nullExpression(String.class)))
                .from(member)
                .leftJoin(analysis).on(analysis.member.eq(member))
                .leftJoin(rfm).on(rfm.member.eq(member))
                .where(
                        segmentEq(analysis, request.getSegment()),
                        frequencyEq(advice, member, request.getFrequency()),
                        categoryEq(advice, member, request.getCategoryId()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalValue = queryFactory
                .select(member.count())
                .from(member)
                .leftJoin(analysis).on(analysis.member.eq(member))
                .leftJoin(rfm).on(rfm.member.eq(member))
                .where(
                        segmentEq(analysis, request.getSegment()),
                        frequencyEq(advice, member, request.getFrequency()),
                        categoryEq(advice, member, request.getCategoryId()))
                .fetchOne();

        long total = totalValue != null ? totalValue : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression segmentEq(QAnalysis analysis, String segment) {
        return StringUtils.hasText(segment) ? analysis.type.eq(segment) : null;
    }

    private BooleanExpression frequencyEq(QAdvice advice, QMember member, String frequency) {
        if (!StringUtils.hasText(frequency)) {
            return null;
        }
        
        var countQuery = JPAExpressions.select(advice.id.count()).from(advice).where(advice.member.eq(member));
        
        return switch (frequency.toUpperCase()) {
            case "LOW" -> Expressions.asNumber(countQuery).loe(2L);
            case "MEDIUM" -> Expressions.asNumber(countQuery).between(3L, 5L);
            case "HIGH" -> Expressions.asNumber(countQuery).goe(6L);
            default -> null;
        };
    }

    private BooleanExpression categoryEq(QAdvice advice, QMember member, Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        
        org.backend.domain.advice.entity.QCategories category = org.backend.domain.advice.entity.QCategories.categories;
        return member.id.in(
                JPAExpressions
                        .select(advice.member.id)
                        .from(advice)
                        .join(advice.category, category)
                        .where(category.id.eq(categoryId).or(category.parentId.eq(categoryId)))
        );
    }
}
