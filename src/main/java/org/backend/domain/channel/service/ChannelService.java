package org.backend.domain.channel.service;

import lombok.RequiredArgsConstructor;
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

    // ── 채널 CRUD ──────────────────────────────────────────────

    @Transactional
    public ChannelResponse createChannel(Long adminId, ChannelRequest request) {
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
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다. ID: " + channelId));
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
            throw new IllegalArgumentException("존재하지 않는 회원입니다. IDs: " + notFound);
        }

        Set<Long> alreadyInChannel = channelMemberRepository.findExistingMemberIds(channelId, memberIds);

        return memberIds.stream()
                .filter(memberId -> !alreadyInChannel.contains(memberId))
                .map(memberId -> ChannelMemberResponse.from(
                        channelMemberRepository.save(
                                ChannelMember.builder()
                                        .channel(channel)
                                        .memberId(memberId)
                                        .build()
                        )
                ))
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
                .orElseThrow(() -> new IllegalArgumentException("채널에 해당 고객이 존재하지 않습니다."));
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
            throw new IllegalArgumentException("채널에 존재하지 않는 고객입니다. IDs: " + notFound);
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
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채널입니다. ID: " + channelId));
    }

    private void checkChannelExists(Long channelId) {
        if (!channelRepository.existsById(channelId)) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다. ID: " + channelId);
        }
    }
}