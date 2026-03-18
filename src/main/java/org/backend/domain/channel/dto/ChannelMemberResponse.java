package org.backend.domain.channel.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChannelMemberResponse {

    private final Long id;
    private final Long channelId;
    private final Long memberId;

    private final String name;
    private final String phone;
    private final String email;

    // 고객 목록 화면에서 바로 쓸 수 있도록 추가
    private final String service;
    private final String servicePeriod;
    private final String consultCategory;
    private final String consultFrequency;
    private final String vip;

    private final LocalDateTime createdAt;

    public ChannelMemberResponse(
            Long id,
            Long channelId,
            Long memberId,
            String name,
            String phone,
            String email,
            String service,
            String servicePeriod,
            String consultCategory,
            String consultFrequency,
            String vip,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.channelId = channelId;
        this.memberId = memberId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.service = service;
        this.servicePeriod = servicePeriod;
        this.consultCategory = consultCategory;
        this.consultFrequency = consultFrequency;
        this.vip = vip;
        this.createdAt = createdAt;
    }
}