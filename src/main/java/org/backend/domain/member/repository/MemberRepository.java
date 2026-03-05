package org.backend.domain.member.repository;

import org.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 고객별 기본 정보 조회
    @EntityGraph(attributePaths = "consent")
    Optional<Member> findWithConsentById(Long id);

    // 배치를 위해
    @Query("SELECT m.id FROM Member m")
    List<Long> findAllIds();
}