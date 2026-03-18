package org.backend.domain.channel.repository;

import org.backend.domain.channel.dto.ChannelMemberResponse;
import org.backend.domain.channel.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {

    @Query("""
        SELECT new org.backend.domain.channel.dto.ChannelMemberResponse(
            cm.id,
            cm.channel.id,
            m.id,
            m.name,
            m.phone,
            m.email,
            null,
            null,
            null,
            null,
            null,
            cm.createdAt
        )
        FROM ChannelMember cm
        JOIN Member m ON m.id = cm.memberId
        WHERE cm.channel.id = :channelId
        ORDER BY cm.id ASC
    """)
    List<ChannelMemberResponse> findMemberDetailsByChannelId(@Param("channelId") Long channelId);

    Optional<ChannelMember> findByChannelIdAndMemberId(Long channelId, Long memberId);

    boolean existsByChannelIdAndMemberId(Long channelId, Long memberId);

    @Query("SELECT cm.memberId FROM ChannelMember cm WHERE cm.channel.id = :channelId AND cm.memberId IN :memberIds")
    Set<Long> findExistingMemberIds(@Param("channelId") Long channelId, @Param("memberIds") List<Long> memberIds);

    @Modifying
    @Query("DELETE FROM ChannelMember cm WHERE cm.channel.id = :channelId AND cm.memberId IN :memberIds")
    void deleteByChannelIdAndMemberIdIn(@Param("channelId") Long channelId, @Param("memberIds") List<Long> memberIds);

    @Modifying
    @Query("DELETE FROM ChannelMember cm WHERE cm.channel.id = :channelId")
    void deleteAllByChannelId(@Param("channelId") Long channelId);
}