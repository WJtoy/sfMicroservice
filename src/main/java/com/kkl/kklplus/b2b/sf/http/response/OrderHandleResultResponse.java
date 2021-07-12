package com.kkl.kklplus.b2b.sf.http.response;

import lombok.Data;

/**
 * @Auther wj
 * @Date 2020/11/25 10:01
 */
@Data
public class OrderHandleResultResponse {

    private String errorMessage;

    private String errorCode;

    private Boolean success;

}
