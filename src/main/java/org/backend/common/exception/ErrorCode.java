package org.backend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "활성화된 구독 정보가 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다.");
    ;
    private final HttpStatus status;
    private final String message;
}
