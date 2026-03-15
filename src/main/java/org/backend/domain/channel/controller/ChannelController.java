package org.backend.domain.channel.controller;

import lombok.RequiredArgsConstructor;
import org.backend.common.CommonResponse;
import org.backend.domain.channel.dto.*;
import org.backend.domain.channel.service.ChannelService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/channels")
public class ChannelController {

    private final ChannelService channelService;

    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증 정보가 없습니다. 로그인이 필요합니다.");
        }
        return Long.parseLong((String) authentication.getPrincipal());
    }

    // ── 채널 CRUD ──────────────────────────────────────────────

    @PostMapping
    public CommonResponse<ChannelResponse> createChannel(@RequestBody ChannelRequest request) {
        try {
            Long adminId = getCurrentAdminId();
            return CommonResponse.success(channelService.createChannel(adminId, request), "채널이 생성되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    @GetMapping
    public CommonResponse<List<ChannelResponse>> getAllChannels() {
        try {
            return CommonResponse.success(channelService.getAllChannels(), "채널 목록 조회 성공");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/{channelId}")
    public CommonResponse<ChannelResponse> getChannel(@PathVariable Long channelId) {
        try {
            return CommonResponse.success(channelService.getChannel(channelId), "채널 조회 성공");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    @PatchMapping("/{channelId}")
    public CommonResponse<ChannelResponse> updateChannel(
            @PathVariable Long channelId,
            @RequestBody ChannelUpdateRequest request) {
        try {
            return CommonResponse.success(channelService.updateChannel(channelId, request), "채널이 수정되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    @DeleteMapping("/{channelId}")
    public CommonResponse<Void> deleteChannel(@PathVariable Long channelId) {
        try {
            channelService.deleteChannel(channelId);
            return CommonResponse.success(null, "채널이 삭제되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // ── 채널 멤버 관리 ──────────────────────────────────────────

    @PostMapping("/{channelId}/members")
    public CommonResponse<List<ChannelMemberResponse>> addMembers(
            @PathVariable Long channelId,
            @RequestBody ChannelMemberRequest request) {
        try {
            return CommonResponse.success(channelService.addMembers(channelId, request), "고객이 채널에 추가되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    @GetMapping("/{channelId}/members")
    public CommonResponse<List<ChannelMemberResponse>> getMembers(@PathVariable Long channelId) {
        try {
            return CommonResponse.success(channelService.getMembers(channelId), "채널 고객 목록 조회 성공");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // 단일 고객 제거
    @DeleteMapping("/{channelId}/members/{memberId}")
    public CommonResponse<Void> removeMember(
            @PathVariable Long channelId,
            @PathVariable Long memberId) {
        try {
            channelService.removeMember(channelId, memberId);
            return CommonResponse.success(null, "채널에서 고객이 제거되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // 여러 고객 한번에 제거
    @DeleteMapping("/{channelId}/members")
    public CommonResponse<Void> removeMembers(
            @PathVariable Long channelId,
            @RequestBody ChannelMemberRequest request) {
        try {
            channelService.removeMembers(channelId, request);
            return CommonResponse.success(null, "채널에서 고객들이 제거되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }

    // 채널 전체 비우기 (채널 유지, 고객 목록만 초기화)
    @DeleteMapping("/{channelId}/members/all")
    public CommonResponse<Void> clearMembers(@PathVariable Long channelId) {
        try {
            channelService.clearMembers(channelId);
            return CommonResponse.success(null, "채널 고객 목록이 초기화되었습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommonResponse.fail(e.getMessage());
        }
    }
}