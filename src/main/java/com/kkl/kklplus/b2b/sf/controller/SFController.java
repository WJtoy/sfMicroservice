package com.kkl.kklplus.b2b.sf.controller;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.sf.entity.*;
import com.kkl.kklplus.b2b.sf.http.config.B2BSFProperties;
import com.kkl.kklplus.b2b.sf.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.sf.service.B2BProcesslogService;
import com.kkl.kklplus.b2b.sf.service.OrderExpressService;
import com.kkl.kklplus.b2b.sf.service.OrderInfoService;
import com.kkl.kklplus.b2b.sf.service.SysLogService;
import com.kkl.kklplus.b2b.sf.utils.GsonUtils;
import com.kkl.kklplus.b2b.sf.utils.QuarterUtils;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Slf4j
@RestController
@RequestMapping("/sf/")
public class SFController {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private OrderExpressService orderExpressService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private B2BProcesslogService b2BProcesslogService;

    @Autowired
    private B2BSFProperties sfProperties;

    @PostMapping("/orderArrivalExpress")
    public ApiResponseData orderArrivalExpress(HttpServletRequest req) throws IOException {
        return commonProcess(req,"/orderArrivalExpress");
    }

    @PostMapping("/newOrder")
    public ApiResponseData pushOrder(HttpServletRequest req) throws IOException {
        return commonProcess(req,"/newOrder");
    }

    public ApiResponseData commonProcess(HttpServletRequest req,String url) throws IOException {
        String json = GsonUtils.getInstance().toGson(req.getParameterMap());
        String serviceCode = req.getParameter("serviceCode");
        String logisticID = req.getParameter("logisticID");
        String msgData = req.getParameter("msgData");
        String msgDigest = req.getParameter("msgDigest");
        String timestamp = req.getParameter("timestamp");
        try {
            if(StringUtils.isBlank(logisticID) ||
                    StringUtils.isBlank(serviceCode) ||
                    StringUtils.isBlank(msgData) ||
                    StringUtils.isBlank(timestamp)){
                log.error("数据验证:参数缺失:{}",json);
                sysLogService.insert(1L, json, "参数缺失",
                        "数据验证",url, "POST");
                return new ApiResponseData(ApiErrorCode.A1001);
            }
            boolean serviceCodeFlag = validServiceCode(serviceCode,url);
            if(!serviceCodeFlag){
                log.error("数据验证:serviceCode错误:{}",json);
                sysLogService.insert(1L, json, "serviceCode错误",
                        "数据验证",url, "POST");
                return new ApiResponseData(ApiErrorCode.A1004);
            }
            String msgDigestV = OkHttpUtils.genDigest(timestamp, msgData,
                    sfProperties.getDataSourceConfig().getMd5Key());
            if(msgDigest == null || !msgDigest.equals(msgDigestV)){
                log.error("数据验证:签名错误:{}",json);
                sysLogService.insert(1L, json, "签名错误",
                        "数据验证",url, "POST");
                return new ApiResponseData(ApiErrorCode.A1006);
            }
        }catch (Exception e){
            log.error("数据验证异常:{}",json,e);
            sysLogService.insert(1L, json, e.getMessage(),
                    "数据验证异常",url, "POST");
            return new ApiResponseData(ApiErrorCode.A1008);
        }
        // 保存SF发送过来的原始数据
        B2BOrderProcesslog processlog = new B2BOrderProcesslog();
        try {
            processlog.setInterfaceName(serviceCode);
            processlog.setInfoJson(msgData);
            processlog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_ACCEPT.value);
            processlog.preInsert();
            processlog.setCreateById(1L);
            processlog.setQuarter(QuarterUtils.getQuarter(processlog.getCreateDt()));
            b2BProcesslogService.insert(processlog);
        } catch(Exception ex) {
            log.error("info:{}",msgData,ex);
            sysLogService.insert(1L, msgData, ex.getMessage(),
                    "推送数据保存异常",serviceCode, "POST");
        }
        ApiResponseData responseData = new ApiResponseData(ApiErrorCode.A1000);
        ApiOperationResponseData operationResponseData;
        if("FOP_PUSH_FIS_ORDER".equals(serviceCode)){
            operationResponseData = orderInfoService.processOrder(msgData);
        }else if("FOP_PUSH_FIS_DELIVERY_NOTICE".equals(serviceCode)){
            operationResponseData = orderExpressService.processExpress(msgData);
        }else{
            operationResponseData = new ApiOperationResponseData();
        }
        b2BProcesslogService.updateProcessResult(processlog,operationResponseData);
        responseData.setApiResultData(processlog.getResultJson());
        return responseData;
    }

    private boolean validServiceCode(String serviceCode,String url) {
        if("FOP_PUSH_FIS_ORDER".equals(serviceCode) && url.equals("/newOrder")){
            return true;
        }else if("FOP_PUSH_FIS_DELIVERY_NOTICE".equals(serviceCode)&& url.equals("/orderArrivalExpress")){
            return true;
        }
        return false;
    }

    private String getRequestJson(HttpServletRequest req) throws IOException {
        // 读取参数
        InputStream inputStream;
        StringBuffer sb = new StringBuffer();
        inputStream = req.getInputStream();
        String s;
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        while ((s = in.readLine()) != null) {
            sb.append(s);
        }
        in.close();
        inputStream.close();
        String json = sb.toString();
        return json;
    }
}
