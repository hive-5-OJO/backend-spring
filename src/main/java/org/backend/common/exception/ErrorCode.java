package org.backend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 고객
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),

    // 구독
    SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "활성화된 구독 정보가 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다."),

    // 청구서
    INVOICE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 월의 청구 정보가 존재하지 않습니다."),

    // rfm 관련
    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "분석 데이터를 찾을 수 없습니다."),
    KPI_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 월의 KPI 데이터가 존재하지 않습니다."),

    // 배치
    BATCH_EXECUTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "배치 작업 실행 중 오류가 발생했습니다."),
    DATA_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 삭제 작업에 실패했습니다.");
    ;
    private final HttpStatus status;
    private final String message;
}
