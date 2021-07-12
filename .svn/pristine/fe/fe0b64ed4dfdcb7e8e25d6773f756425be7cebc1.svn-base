package com.kkl.kklplus.b2b.sf.service;

import com.kkl.kklplus.b2b.sf.entity.*;
import com.kkl.kklplus.b2b.sf.http.utils.RetryUtils;
import com.kkl.kklplus.b2b.sf.mapper.OrderExpressMapper;
import com.kkl.kklplus.b2b.sf.mq.sender.B2BCenterOrderExpressArrivalMQSender;
import com.kkl.kklplus.b2b.sf.utils.GsonUtils;
import com.kkl.kklplus.b2b.sf.utils.QuarterUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BCenterOrderExpressArrivalMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *工单物流
 * @author chenxj
 * @date 2020/11/24
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderExpressService {

    @Autowired
    private B2BCenterOrderExpressArrivalMQSender expressArrivalMQSender;

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private RetryUtils retryUtils;

    @Resource
    private OrderExpressMapper orderExpressMapper;

    public ApiOperationResponseData processExpress(String msgData) {
        ApiOperationResponseData responseData = new ApiOperationResponseData();
        try {
            OrderExpressRequestData expressRequestData = GsonUtils.getInstance().fromJson(msgData, OrderExpressRequestData.class);
            MSResponse msResponse = validate(expressRequestData);
            if(!MSResponse.isSuccessCode(msResponse)){
                responseData.setSuccess(false);
                responseData.setErrorMessage(msResponse.getMsg());
                return responseData;
            }
            OrderInfo oInfo = orderInfoService.findOrderInfo(expressRequestData.getTaskCode());
            if(oInfo != null){
                OrderExpress express = parseExpress(expressRequestData);
                express.setB2bOrderId(oInfo.getId());
                this.insert(express);
                Long kklOrderId = oInfo.getKklOrderId();
                // 取消通知
                if(kklOrderId != null && kklOrderId > 0){
                    express.setKklOrderId(kklOrderId);
                    //判断是否是本系统的单，不是则转发给对应系统
                    boolean thisSystem =
                            retryUtils.isThisSystem(oInfo.getSystemId(), "FOP_PUSH_FIS_DELIVERY_NOTICE", express.getId(),express);
                    if(thisSystem) {
                        sendExpressMQ(express);
                    }
                }
            }else {
                responseData.setSuccess(false);
                responseData.setErrorMessage("没有找到对应工单");
            }
        }catch (Exception e){
            log.error("物流到货处理异常:{}",msgData,e);
            responseData.setSuccess(false);
            responseData.setErrorMessage("json解析异常");
        }
        return responseData;
    }

    public void sendExpressMQ(OrderExpress express) {
        Long arrivalTime;
        try {
            String formatStr = "yyyy-MM-dd";
            String time = express.getTime();
            if(time.length()==19){
                formatStr = "yyyy-MM-dd HH:mm:ss";
            }
            SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
            Date arrivalDate = sdf.parse(express.getTime());
            arrivalTime = arrivalDate.getTime();
            MQB2BCenterOrderExpressArrivalMessage.B2BCenterOrderExpressArrivalMessage arrivalMessage =
                    MQB2BCenterOrderExpressArrivalMessage.B2BCenterOrderExpressArrivalMessage.newBuilder()
                            .setMessageId(express.getId())
                            .setDataSource(B2BDataSourceEnum.SF.id)
                            .setKklOrderId(express.getKklOrderId())
                            .setArrivalTime(arrivalTime).build();
            expressArrivalMQSender.send(arrivalMessage);
        } catch (ParseException e) {
            log.error("操作时间格式化异常:{}={}",express.getTaskCode(),express.getTime());
        }
    }

    private MSResponse validate(OrderExpressRequestData expressRequestData) {
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(expressRequestData == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("到货资料不能为空！");
            return msResponse;
        }
        String waybillNo = expressRequestData.getWaybillNo();
        if(StringUtils.isBlank(waybillNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("母单号waybillNo不能为空！");
            return msResponse;
        }
        String taskCode = expressRequestData.getTaskCode();
        if(StringUtils.isBlank(taskCode)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("任务编码taskCode不能为空！");
            return msResponse;
        }
        String time = expressRequestData.getTime();
        if(StringUtils.isBlank(time)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("到货时间time不能为空！");
            return msResponse;
        }
        return msResponse;
    }

    private OrderExpress parseExpress(OrderExpressRequestData expressRequestData) {
        OrderExpress orderExpress = new OrderExpress();
        orderExpress.setWaybillNo(expressRequestData.getWaybillNo());
        orderExpress.setTaskCode(expressRequestData.getTaskCode());
        orderExpress.setTime(expressRequestData.getTime());
        orderExpress.setPickupZone(StringUtils.trimToEmpty(expressRequestData.getPickupZone()));
        orderExpress.setPickupContact(StringUtils.trimToEmpty(expressRequestData.getPickupContact()));
        orderExpress.setPickupPhone(StringUtils.trimToEmpty(expressRequestData.getPickupPhone()));
        orderExpress.setPickupAddress(StringUtils.trimToEmpty(expressRequestData.getPickupAddress()));
        orderExpress.setPickupContact(StringUtils.trimToEmpty(expressRequestData.getPickupContact()));
        orderExpress.setReceiveContact(expressRequestData.getReceiveContact());
        orderExpress.setReceivePhone(expressRequestData.getReceivePhone());
        orderExpress.setReceiveProvince(expressRequestData.getReceiveProvince());
        orderExpress.setReceiveCity(expressRequestData.getReceiveCity());
        orderExpress.setReceiveCounty(StringUtils.trimToEmpty(expressRequestData.getReceiveCounty()));
        orderExpress.setReceiveAddress(expressRequestData.getReceiveAddress());
        orderExpress.setServiceType(expressRequestData.getServiceType());
        orderExpress.preInsert();
        orderExpress.setCreateById(1L);
        orderExpress.setQuarter(QuarterUtils.getQuarter(orderExpress.getCreateDt()));
        return orderExpress;
    }

    private void insert(OrderExpress express) {
        orderExpressMapper.insert(express);
    }
}
