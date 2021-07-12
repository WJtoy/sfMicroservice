package com.kkl.kklplus.b2b.sf.http.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kkl.kklplus.b2b.sf.http.command.OperationCommand;
import com.kkl.kklplus.b2b.sf.http.config.B2BSFProperties;
import com.kkl.kklplus.b2b.sf.http.request.OrderPictureRequestParam;
import com.kkl.kklplus.b2b.sf.http.response.OrderHandleResponse;
import com.kkl.kklplus.b2b.sf.http.response.OrderHandleResultResponse;
import com.kkl.kklplus.b2b.sf.http.response.ResponseBody;
import com.kkl.kklplus.b2b.sf.utils.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.Charset;
import java.util.UUID;

/**
 * @Auther wj
 * @Date 2020/11/27 11:12
 */
@Slf4j
public class HttpUtils {
    private static OkHttpClient okHttpClient = SpringContextHolder.getBean(OkHttpClient.class);
    private static B2BSFProperties sfProperties = SpringContextHolder.getBean(B2BSFProperties.class);
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static <T> ResponseBody<T> postSyncGeneric
            (OperationCommand command, OrderPictureRequestParam orderPictureRequestParam,Class<T> dataClass) {
        ResponseBody<T> responseBody = null;
        B2BSFProperties.DataSourceConfig dataSourceConfig = sfProperties.getDataSourceConfig();
        if (dataSourceConfig != null && command != null && command.getOpCode() != null &&
                command.getReqBody() != null && command.getReqBody().getClass().getName().equals(command.getOpCode().reqBodyClass.getName())) {
            // 系统参数
            MultipartBody.Builder param = new MultipartBody.Builder().setType(MultipartBody.FORM);;
            try {
                for (MultipartFile file : orderPictureRequestParam.getImages()) {
                    param.addFormDataPart("images",file.getOriginalFilename(),RequestBody.create(MediaType.parse("application/octet-stream"), file.getBytes()));
                }
                param.addFormDataPart("imageType",orderPictureRequestParam.getImageType().toString());
                param.addFormDataPart("taskCode",orderPictureRequestParam.getTaskCode());
                param.addFormDataPart("waybillNo",orderPictureRequestParam.getWaybillNo());
                param.addFormDataPart("digest",orderPictureRequestParam.getDigest());
                param.addFormDataPart("clientCode", dataSourceConfig.getClientCode());
            }catch (Exception e){
                return new ResponseBody<>(ResponseBody.ErrorCode.DATA_PARSE_FAILURE, e);
            }
            Request request = new Request.Builder()
                    .url(dataSourceConfig.getPicUrl())
                    .post(param.build())
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBodyJson = response.body().string();
                        try {
                            responseBody = new ResponseBody<>();
                            responseBody.setOriginalJson(responseBodyJson);
                            T data = gson.fromJson(responseBodyJson, dataClass);
                            responseBody.setData(data);
                        } catch (Exception e) {
                            log.error("图片请求JSON解析异常:{}",responseBodyJson);
                            responseBody = new ResponseBody<>(ResponseBody.ErrorCode.JSON_PARSE_FAILURE, e);
                            responseBody.setOriginalJson(responseBodyJson);
                            return responseBody;
                        }
                    } else {
                        responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_RESPONSE_BODY_ERROR);
                    }
                } else {
                    responseBody = new ResponseBody<>(ResponseBody.ErrorCode.HTTP_STATUS_CODE_ERROR);
                }
            } catch (Exception e) {
                return new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_INVOCATION_FAILURE, e);
            }
        } else {
            responseBody = new ResponseBody<>(ResponseBody.ErrorCode.REQUEST_PARAMETER_FORMAT_ERROR);
        }

        return responseBody;
    }


}
