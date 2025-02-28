package com.mobigen.monitoring.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // Services
    // History
    // Connect
    CONNECTION_FAIL(1, "ConnectionDao Fail, Services Name : "),
    CONNECTION_CLOSE_FAIL(1, "ConnectionDao Close Fail, Services Name: "),
    EXECUTE_FAIL(1, "Execute Fail, DB Type:  "),
    MEASURE_FAIL(1, "Execute measure Fail, DB Type : "),
    CUSTOM_EXCEPTION_CODE_TEST(1, "Test용 ErrorCode"),
    UNSUPPORTED_DB_TYPE(1, "지원하지 않는 DB Type: "),
    UNKNOWN(500, "알려지지 않는 Exception"),
    // ModelRegistration
    // Scheduler
    // OpenMetadata
    GET_TOKEN_FAIL(2, "OpenMetadata의 Token을 가져오기에 실패하였습니다."),
    JSON_MAPPER_FAIL(2, "Json String을 Json으로 변환하는데 실패하였습니다."),
    GET_FAIL(2, "OpenMetadata의 Get Method가 실패하였습니다."),

    ;

    private final int status;
    private final String message;
}
