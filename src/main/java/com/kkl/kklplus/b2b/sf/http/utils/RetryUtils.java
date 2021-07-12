package com.kkl.kklplus.b2b.sf.http.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.b2b.sf.entity.RetryResponseData;
import com.kkl.kklplus.b2b.sf.http.config.B2BSFProperties;
import com.kkl.kklplus.b2b.sf.utils.GsonUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BSystemCodeEnum;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Slf4j
@Component
public class RetryUtils {

    @Autowired
    private B2BSFProperties sfProperties;

    @Autowired
    private OkHttpClient okHttpClient;

    public boolean isThisSystem(Integer systemId, String type, Long id,Object obj) {
        if(systemId == 0){
            return false;
        }
        String systemCode = sfProperties.getSite().getCode();
        B2BSystemCodeEnum thisSystemCodeEnum = B2BSystemCodeEnum.get(systemCode);
        if(thisSystemCodeEnum == B2BSystemCodeEnum.UNKNOWN){
            log.error("没找到本系统枚举,systemCode:"+systemCode);
            return false;
        }
        if(systemId != thisSystemCodeEnum.id){
            B2BSystemCodeEnum systemCodeEnum = B2BSystemCodeEnum.get(systemId);
            if(systemCodeEnum == B2BSystemCodeEnum.UNKNOWN){
                log.error("没找到对应的系统,id:"+systemId);
                return false;
            }
            String url = sfProperties.getSite().getOtherSites().get(systemCodeEnum.code);
            if(url == null){
                log.error("没找到对应的系统的转发地址，id:"+systemId);
                return false;
            }
            RetryResponseData responseData = new RetryResponseData();
            responseData.setId(id);
            responseData.setType(type);
            responseData.setSite(systemCodeEnum.code);
            responseData.setJson(GsonUtils.getInstance().toGson(obj));
            this.transferToOtherSite(GsonUtils.getInstance().toGson(responseData),url);
            return false;
        }
        return true;
    }

    public <T> MSResponse<T> transferToOtherSite(String json, String url){
        MediaType parse = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(parse, json);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        return syncGenericNewCall(request,new TypeToken<MSResponse>(){}.getType());
    }

    public <T> MSResponse<T> syncGenericNewCall(Request request, Type typeOfT) {
        MSResponse responseBody = null;
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBodyJson = response.body().string();
                responseBody = new Gson().fromJson(responseBodyJson, typeOfT);
                if (responseBody == null) {
                    return new MSResponse<>(MSErrorCode.FAILURE);
                }
                return responseBody;
            }
            return new MSResponse<>(response.code(), response.message(), null, null);
        } catch (Exception e) {
            log.error("syncGenericNewCall", e);
            return new MSResponse<>(MSErrorCode.FAILURE.getCode(), e.getMessage(), null, null);
        }
    }
}
