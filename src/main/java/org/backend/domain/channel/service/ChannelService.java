package org.backend.domain.channel.service;

import lombok.RequiredArgsConstructor;
import org.backend.common.exception.CustomException;
import org.backend.common.exception.ErrorCode;
import org.backend.domain.admin.repository.AdminRepository;
import org.backend.domain.channel.dto.*;
import org.backend.domain.channel.entity.Channel;
import org.backend.domain.channel.entity.ChannelMember;
import org.backend.domain.channel.repository.ChannelMemberRepository;
import org.backend.domain.channel.repository.ChannelRepository;
import org.backend.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;

    // ── 채널 CRUD ──────────────────────────────────────────────

    @Transactional
    public ChannelResponse createChannel(Long adminId, ChannelRequest request) {
        if (!adminRepository.existsById(adminId)) {
            throw new CustomException(ErrorCode.ADMIN_NOT_FOUND_FOR_ME);
        }
        Channel channel = Channel.builder()
                .adminId(adminId)
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return ChannelResponse.from(channelRepository.save(channel));
    }

    public List<ChannelResponse> getAllChannels() {
        return channelRepository.findAll().stream()
                .map(ChannelResponse::from)
                .toList();
    }

    public ChannelResponse getChannel(Long channelId) {
        return ChannelResponse.from(findChannelWithMembersById(channelId));
    }

    @Transactional
    public ChannelResponse updateChannel(Long channelId, ChannelUpdateRequest request) {
        Channel channel = findChannelWithMembersById(channelId);
        channel.update(request.getName(), request.getDescription(), request.getStatus());
        return ChannelResponse.from(channel);
    }

    @Transactional
    public void deleteChannel(Long channelId) {
        // cascade = ALL이라 channelMembers 함께 삭제되므로 fetch 불필요
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
        channelRepository.delete(channel);
    }

    // ── 채널 멤버 관리 ──────────────────────────────────────────

    @Transactional
    public List<ChannelMemberResponse> addMembers(Long channelId, ChannelMemberRequest request) {
        Channel channel = findChannelWithMembersById(channelId);
        List<Long> memberIds = request.getMemberIds();

        Set<Long> existingMemberIds = memberRepository.findExistingIds(memberIds);
        List<Long> notFound = memberIds.stream()
                .filter(id -> !existingMemberIds.contains(id))
                .toList();
        if (!notFound.isEmpty()) {
            throw new CustomException(ErrorCode.MEMBERS_NOT_FOUND);
        }

        Set<Long> alreadyInChannel = channelMemberRepository.findExistingMemberIds(channelId, memberIds);

        List<ChannelMember> newMembers = memberIds.stream()
                .filter(memberId -> !alreadyInChannel.contains(memberId))
                .map(memberId -> ChannelMember.builder()
                        .channel(channel)
                        .memberId(memberId)
                        .build())
                .toList();

        return channelMemberRepository.saveAll(newMembers).stream()
                .map(ChannelMemberResponse::from)
                .toList();
    }

    public List<ChannelMemberResponse> getMembers(Long channelId) {
        checkChannelExists(channelId);
        return channelMemberRepository.findByChannelId(channelId).stream()
                .map(ChannelMemberResponse::from)
                .toList();
    }

    // 단일 고객 제거
    @Transactional
    public void removeMember(Long channelId, Long memberId) {
        checkChannelExists(channelId);
        ChannelMember channelMember = channelMemberRepository
                .findByChannelIdAndMemberId(channelId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_MEMBER_NOT_FOUND));
        channelMemberRepository.delete(channelMember);
    }

    // 여러 고객 한번에 제거
    @Transactional
    public void removeMembers(Long channelId, ChannelMemberRequest request) {
        checkChannelExists(channelId);
        List<Long> memberIds = request.getMemberIds();

        Set<Long> existingInChannel = channelMemberRepository.findExistingMemberIds(channelId, memberIds);
        List<Long> notFound = memberIds.stream()
                .filter(id -> !existingInChannel.contains(id))
                .toList();
        if (!notFound.isEmpty()) {
            throw new CustomException(ErrorCode.CHANNEL_MEMBERS_NOT_FOUND);
        }

        channelMemberRepository.deleteByChannelIdAndMemberIdIn(channelId, memberIds);
    }

    // 채널 전체 비우기 (채널은 유지, 고객 목록만 초기화)
    @Transactional
    public void clearMembers(Long channelId) {
        checkChannelExists(channelId);
        channelMemberRepository.deleteAllByChannelId(channelId);
    }

    // ── 공통 ────────────────────────────────────────────────────

    private Channel findChannelWithMembersById(Long channelId) {
        return channelRepository.findByIdWithMembers(channelId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHANNEL_NOT_FOUND));
    }

    private void checkChannelExists(Long channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new CustomException(ErrorCode.CHANNEL_NOT_FOUND);
        }
    }
}