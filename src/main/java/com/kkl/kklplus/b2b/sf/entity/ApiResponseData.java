package com.kkl.kklplus.b2b.sf.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * describe:
 *
 * @author chenxj
 * @date 2020/11/23
 */
@Data
public class ApiResponseData implements Serializable {
    private String apiResultCode;
    private String apiErrorMsg;
    private String apiResponseID;
    private String apiResultData;

    public ApiResponseData(){
        this.apiResponseID = UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public ApiResponseData(ApiErrorCode errorCode){
        this.apiResultCode = errorCode.errorCode;
        this.apiErrorMsg = errorCode.errorMsg;
        this.apiResponseID = UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
