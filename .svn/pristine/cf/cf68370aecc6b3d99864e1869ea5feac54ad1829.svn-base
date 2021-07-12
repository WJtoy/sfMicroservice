package com.kkl.kklplus.b2b.sf.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kkl.kklplus.b2b.sf.entity.*;
import com.kkl.kklplus.b2b.sf.feign.B2BConfigRoutingFeign;
import com.kkl.kklplus.b2b.sf.http.config.B2BSFProperties;
import com.kkl.kklplus.b2b.sf.http.utils.RetryUtils;
import com.kkl.kklplus.b2b.sf.mapper.OrderInfoMapper;
import com.kkl.kklplus.b2b.sf.mq.sender.B2BCenterOrderProcessMQSend;
import com.kkl.kklplus.b2b.sf.mq.sender.B2BOrderMQSender;
import com.kkl.kklplus.b2b.sf.utils.GsonUtils;
import com.kkl.kklplus.b2b.sf.utils.QuarterUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bcenter.md.B2BDataSourceEnum;
import com.kkl.kklplus.entity.b2bcenter.md.B2BShopEnum;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderMessage;
import com.kkl.kklplus.entity.b2bcenter.pb.MQB2BOrderProcessMessage;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderActionEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.b2bconfig.sd.B2BConfigRouting;
import com.kkl.kklplus.entity.common.MSPage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderInfoService {

    @Autowired
    private B2BCenterOrderProcessMQSend orderProcessMQSend;

    @Autowired
    private B2BOrderMQSender b2BOrderMQSender;

    @Autowired
    private B2BSFProperties sfProperties;

    @Autowired
    private B2BConfigRoutingFeign b2BConfigRoutingFeign;

    @Autowired
    private RetryUtils retryUtils;

    @Resource
    private OrderInfoMapper orderInfoMapper;



    /**
     * 获取某一页的工单信息
     * @param orderSearchModel
     * @return
     */
    public MSPage<B2BOrder> getList(B2BOrderSearchModel orderSearchModel) {
        if (orderSearchModel.getPage() != null) {
            PageHelper.startPage(orderSearchModel.getPage().getPageNo(), orderSearchModel.getPage().getPageSize());
            Page<OrderInfo> orderInfoPage = orderInfoMapper.getList(orderSearchModel);
            Page<B2BOrder> customerPoPage = new Page<>();
            for(OrderInfo orderInfo:orderInfoPage){
                B2BOrder customerPo = new B2BOrder();
                customerPo.setId(orderInfo.getId());
                customerPo.setB2bOrderId(orderInfo.getId());
                customerPo.setDataSource(B2BDataSourceEnum.SF.id);
                customerPo.setOrderNo(orderInfo.getTaskCode());
                customerPo.setParentBizOrderId(orderInfo.getCustomOrderId());
                //顺丰店铺
                customerPo.setShopId(B2BShopEnum.SF.id);
                customerPo.setSaleChannel(orderInfo.getSaleChannel());
                customerPo.setUserName(orderInfo.getReceiverName());
                customerPo.setUserMobile(orderInfo.getReceiverPhone());
                customerPo.setUserAddress(orderInfo.getReceiverAddress());
                String remark = StringUtils.trimToEmpty(orderInfo.getRemark());
                customerPo.setDescription(remark);
                customerPo.setProcessFlag(orderInfo.getProcessFlag());
                customerPo.setProcessTime(orderInfo.getProcessTime());
                customerPo.setProcessComment(orderInfo.getProcessComment());
                customerPo.setQuarter(orderInfo.getQuarter());
                List<InstallTypeDto> products = GsonUtils.getInstance().getGson().fromJson(orderInfo.getInstallTypesJson(),
                        new TypeToken<List<InstallTypeDto>>() {
                }.getType());
                for(InstallTypeDto product : products){
                    B2BOrder.B2BOrderItem orderItem = new B2BOrder.B2BOrderItem();
                    orderItem.setProductName(product.getInstallTypeName());
                    orderItem.setProductCode(product.getInstallTypeCode());
                    orderItem.setQty(product.getCount());
                    orderItem.setServiceType(orderInfo.getServiceType().toString());
                    orderItem.setWarrantyType("保内");
                    customerPo.getItems().add(orderItem);
                }
                customerPoPage.add(customerPo);
            }
            MSPage<B2BOrder> returnPage = new MSPage<>();
            returnPage.setPageNo(orderInfoPage.getPageNum());
            returnPage.setPageSize(orderInfoPage.getPageSize());
            returnPage.setPageCount(orderInfoPage.getPages());
            returnPage.setRowCount((int) orderInfoPage.getTotal());
            returnPage.setList(customerPoPage.getResult());
            return returnPage;
        }else {
            return null;
        }
    }

    public List<OrderInfo> findOrdersProcessFlag(List<B2BOrderTransferResult> orderTransferResults) {
        List<Long> ids = new ArrayList<>();
        for(B2BOrderTransferResult orderTransferResult : orderTransferResults){
            Long id = orderTransferResult.getB2bOrderId();
            ids.add(id);
        }
        return orderInfoMapper.findOrdersProcessFlagByIds(ids);
    }

    @Transactional()
    public void updateTransferResult(List<OrderInfo> wis) {
        for(OrderInfo orderInfo:wis){
            orderInfoMapper.updateTransferResult(orderInfo);
        }
    }

    public void insert(OrderInfo orderInfo){
        orderInfoMapper.insert(orderInfo);
    }

    /**
     * 判断订单号是否存在
     * @param taskCode   订单号
     * @return 1:存在  0:不存在
     */
    public OrderInfo findOrderInfo(String taskCode) {
        return orderInfoMapper.findOrderByTaskCode(taskCode);
    }

    public MSResponse validate(OrderRequestData order) {
        MSResponse msResponse = new MSResponse(MSErrorCode.SUCCESS);
        if(order == null){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("工单资料不能为空！");
            return msResponse;
        }
        String waybillNo = order.getWaybillNo();
        if(StringUtils.isBlank(waybillNo)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("母单号waybillNo不能为空！");
            return msResponse;
        }
        String taskCode = order.getTaskCode();
        if(StringUtils.isBlank(taskCode)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("任务编码taskCode不能为空！");
            return msResponse;
        }
        String receiverName = order.getReceiverName();
        if(StringUtils.isBlank(receiverName)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("收件人receiverName不能为空！");
            return msResponse;
        }
        String receiverPhone = order.getReceiverPhone();
        if(StringUtils.isBlank(receiverPhone)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("收件人电话receiverPhone不能为空！");
            return msResponse;
        }
        String receiverAddress = order.getReceiverAddress();
        if(StringUtils.isBlank(receiverAddress)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("收件人地址receiverAddress不能为空！");
            return msResponse;
        }
        Integer serviceType = order.getServiceType();
        if(serviceType == null){
            order.setServiceType(1);
        }
        String subWaybillNos = order.getSubWaybillNos();
        if(StringUtils.isBlank(subWaybillNos)){
            order.setSubWaybillNos("");
        }
        String status = order.getSupplierOrderStatus();
        if(StringUtils.isBlank(status)){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("订单的状态supplierOrderStatus不能为空！");
            return msResponse;
        }
        List<InstallTypeDto> installTypes = order.getInstallTypes();
        if(installTypes == null || installTypes.size() == 0){
            msResponse.setErrorCode(MSErrorCode.FAILURE);
            msResponse.setMsg("要安装的品类installTypes不能为空！");
            return msResponse;
        }
        for(InstallTypeDto item : installTypes){
            String installTypeCode = item.getInstallTypeCode();
            if(StringUtils.isBlank(installTypeCode)){
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg("安装品类编码installTypeCode不能为空！");
                return msResponse;
            }
            String installTypeName = item.getInstallTypeName();
            if(StringUtils.isBlank(installTypeName)){
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg("安装品类名installTypeName不能为空！");
                return msResponse;
            }
            Integer count = item.getCount();
            if(count == null || count <= 0){
                msResponse.setErrorCode(MSErrorCode.FAILURE);
                msResponse.setMsg("安装品类数量count不能为空或小于等于0！");
                return msResponse;
            }
        }
        return msResponse;
    }

    public void cancelledOrder(B2BOrderTransferResult workcardTransferResults) {
        orderInfoMapper.cancelledOrder(workcardTransferResults);
    }

    /**
     * 取消工单（B2B取消）
     * @param remark
     * @param id
     * @param processFlag
     * @param updateDt
     */
    public void cancelOrderFormB2B(String remark, Long id, int processFlag,Long updateDt) {
        orderInfoMapper.cancelOrderFormB2B(remark,id,processFlag,updateDt);
    }

    public ApiOperationResponseData processOrder(String msgData) {
        ApiOperationResponseData responseData = new ApiOperationResponseData();
        try {
            OrderListRequestData orderListRequestData = GsonUtils.getInstance().getGson().fromJson(msgData, OrderListRequestData.class);
            if(orderListRequestData == null){
                responseData.setSuccess(false);
                responseData.setErrorMessage("工单数据不能为空！");
                return responseData;
            }
            List<OrderRequestData> orderList = orderListRequestData.getOrderList();
            if(orderList == null || orderList.size() == 0 || orderList.size() > 1){
                responseData.setSuccess(false);
                responseData.setErrorMessage("推送接口只支持单笔工单！");
                return responseData;
            }
            OrderRequestData orderRequestData = orderList.get(0);
            MSResponse msResponse = validate(orderRequestData);
            if(!MSResponse.isSuccessCode(msResponse)){
                responseData.setSuccess(false);
                responseData.setErrorMessage(msResponse.getMsg());
                return responseData;
            }
            OrderInfo oInfo = findOrderInfo(orderRequestData.getTaskCode());
            String status = orderRequestData.getSupplierOrderStatus();
            if(oInfo != null){
                if("1".equals(status)){
                    return responseData;
                }
                Long kklOrderId = oInfo.getKklOrderId();
                // 取消通知
                if("2".equals(status)) {
                    if (kklOrderId != null && kklOrderId > 0) {
                        oInfo.setStatus(status);
                        boolean thisSystem =
                                retryUtils.isThisSystem(oInfo.getSystemId(), "FOP_PUSH_FIS_ORDER", oInfo.getId(),oInfo);
                        if(thisSystem) {
                            sendAutoCancel(oInfo);
                        }
                    } else {
                        cancelOrderFormB2B("顺丰主动取消", oInfo.getId(), 5, System.currentTimeMillis());
                    }
                }
            }else {
                OrderInfo orderInfo = parseOrderInfo(orderRequestData);
                this.insert(orderInfo);
                B2BSFProperties.DataSourceConfig dataSourceConfig = sfProperties.getDataSourceConfig();
                if("1".equals(status) && dataSourceConfig != null && dataSourceConfig.getOrderMqEnabled()) {
                    //判断是否是本系统的单，不是则转发给对应系统
                    boolean thisSystem =
                            retryUtils.isThisSystem(orderInfo.getSystemId(), "FOP_PUSH_FIS_ORDER", orderInfo.getId(),orderInfo);
                    if(thisSystem) {
                        sendAutoTransfer(orderInfo);
                    }
                }
            }
        }catch (Exception e){
            log.error("工单接入异常:{}",msgData,e);
            responseData.setSuccess(false);
            responseData.setErrorMessage("json解析异常");
        }
        return responseData;
    }

    public void sendAutoCancel(OrderInfo oInfo) {
        MQB2BOrderProcessMessage.B2BOrderProcessMessage processMessage =
                MQB2BOrderProcessMessage.B2BOrderProcessMessage.newBuilder()
                        .setMessageId(System.currentTimeMillis())
                        .setB2BOrderNo(oInfo.getWaybillNo())
                        .setKklOrderId(oInfo.getKklOrderId())
                        .setB2BOrderId(oInfo.getId())
                        .setDataSource(B2BDataSourceEnum.SF.id)
                        .setActionType(B2BOrderActionEnum.CONVERTED_CANCEL.value).build();
        orderProcessMQSend.send(processMessage);
    }

    /**
     * 自动转单
     * @param orderInfo
     */
    public void sendAutoTransfer(OrderInfo orderInfo) {
        String orderSource = orderInfo.getOrderSource();
        Integer saleChannel = 0;
        if(StringUtils.isNotBlank(orderSource)) {
            saleChannel = orderInfoMapper.getSaleChannel(orderInfo.getOrderSource());
        }
        MQB2BOrderMessage.B2BOrderMessage.Builder builder = MQB2BOrderMessage.B2BOrderMessage.newBuilder()
                .setId(orderInfo.getId())
                .setDataSource(B2BDataSourceEnum.SF.id)
                .setOrderNo(orderInfo.getTaskCode())
                .setParentBizOrderId(StringUtils.trimToEmpty(orderInfo.getCustomOrderId()))
                .setShopId(B2BShopEnum.SF.id)
                .setUserName(orderInfo.getReceiverName())
                .setUserMobile(orderInfo.getReceiverPhone())
                .setUserAddress(orderInfo.getReceiverName())
                .setDescription(StringUtils.trimToEmpty(orderInfo.getRemark()))
                .setQuarter(orderInfo.getQuarter());
        if(saleChannel != null && saleChannel > 0){
            builder.setSaleChannel(saleChannel);
        }
        for(InstallTypeDto product:orderInfo.getInstallTypes()){
            MQB2BOrderMessage.B2BOrderItem b2BOrderItem = MQB2BOrderMessage.B2BOrderItem.newBuilder()
                    .setProductCode(product.getInstallTypeCode())
                    .setProductName(product.getInstallTypeName())
                    .setServiceType(orderInfo.getServiceType().toString())
                    .setWarrantyType("保内")
                    .setQty(product.getCount())
                    .build();
            builder.addB2BOrderItem(b2BOrderItem);
        }
        MQB2BOrderMessage.B2BOrderMessage b2BOrderMessage = builder.build();
        //调用转单队列
        b2BOrderMQSender.send(b2BOrderMessage);
    }

    private OrderInfo parseOrderInfo(OrderRequestData data) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setWaybillNo(data.getWaybillNo());
        orderInfo.setTaskCode(data.getTaskCode());
        orderInfo.setSubWaybillNos(data.getSubWaybillNos());
        orderInfo.setOrderSource(StringUtils.trimToEmpty(data.getOrderSource()));
        orderInfo.setCustomOrderId(StringUtils.trimToEmpty(data.getCustomOrderId()));
        orderInfo.setCargoName(StringUtils.trimToEmpty(data.getCargoName()));
        orderInfo.setTotalVolume(StringUtils.trimToEmpty(data.getTotalVolume()));
        orderInfo.setTotalWeight(StringUtils.trimToEmpty(data.getTotalWeight()));
        orderInfo.setReceiverName(data.getReceiverName());
        orderInfo.setReceiverPhone(data.getReceiverPhone());
        orderInfo.setReceiverAddress(data.getReceiverAddress());
        orderInfo.setPackageCount(data.getPackageCount());
        orderInfo.setVersion(data.getVersion());
        orderInfo.setStatus(data.getSupplierOrderStatus());
        orderInfo.setServiceType(data.getServiceType());
        orderInfo.setRemark(StringUtils.trimToEmpty(data.getRemark()));

        Map<String, String> extendInfo = data.getExtendInfo();
        if(extendInfo != null && extendInfo.size() > 0){
            orderInfo.setExtendInfo(data.getExtendInfo());
            orderInfo.setExtendInfoJson(GsonUtils.getInstance().toGson(extendInfo));
        }
        orderInfo.preInsert();
        orderInfo.setCreateById(1L);
        orderInfo.setQuarter(QuarterUtils.getQuarter(orderInfo.getCreateDt()));
        int dataSourceId = B2BDataSourceEnum.SF.id;
        String shopId = B2BShopEnum.SF.id;
        List<InstallTypeDto> dtos = data.getInstallTypes();
        orderInfo.setInstallTypes(data.getInstallTypes());
        orderInfo.setInstallTypesJson(GsonUtils.getInstance().toGson(data.getInstallTypes()));
        Map<String, Integer> systemIdMap = findSystemIds(dtos, shopId);
        int orderSystemId = 0;
        if(systemIdMap != null && systemIdMap.size() > 0 ) {
            for (InstallTypeDto dto : data.getInstallTypes()) {
                String installTypeCode = dto.getInstallTypeCode();
                if (systemIdMap != null && systemIdMap.size() > 0) {
                    String key = String.format("%s:%s:%s", dataSourceId, shopId, installTypeCode);
                    Integer systemId = systemIdMap.get(key);
                    // 没有找到对应系统编码
                    if (systemId == null) {
                        break;
                    }
                    // 系统编码与上一产品编码不匹配
                    if (orderSystemId != 0 && orderSystemId != systemId) {
                        orderSystemId = 0;
                        break;
                    } else {
                        orderSystemId = systemId;
                    }
                }
            }
        }
        orderInfo.setSystemId(orderSystemId);
        return orderInfo;
    }


    public OrderInfo findByOrderId(Long orderId){

      return  orderInfoMapper.findOrderById(orderId);
    }


    /**
     * 查询orders里各个产品对应的系统编号
     * @param dtos
     * @param shopId
     * @return
     */
    public Map<String,Integer> findSystemIds(List<InstallTypeDto> dtos,String shopId) {
        List<B2BConfigRouting> configRoutings = new ArrayList<>();
            for(InstallTypeDto dto : dtos) {
                B2BConfigRouting configRouting = new B2BConfigRouting();
                configRouting.setDataSource(B2BDataSourceEnum.SF.id);
                configRouting.setShopId(shopId);
                configRouting.setCustomerCategoryId(dto.getInstallTypeCode());
                configRoutings.add(configRouting);
            }
        MSResponse<Map<String, Integer>> msResponse = b2BConfigRoutingFeign.getSystemIdList(configRoutings);
        return msResponse.getData();
    }


    public MSResponse updateSystemIdAll() {
        MSResponse response = new MSResponse(MSErrorCode.SUCCESS);
        //获取所有未转换，并且未标志系统的工单
        List<OrderInfo> orders = orderInfoMapper.getNoConversionAndSystemIdOrder();
        int dataSource = B2BDataSourceEnum.SF.id;
        String shopId = B2BShopEnum.SF.id;
        List<B2BConfigRouting> configRoutings = new ArrayList<>();
        for(OrderInfo order : orders){
            List<InstallTypeDto> dtos =
                    GsonUtils.getInstance().getGson().fromJson(order.getInstallTypesJson(),new TypeToken<List<InstallTypeDto>>() {
                    }.getType());
            order.setInstallTypes(dtos);
            for(InstallTypeDto dto : dtos) {
                B2BConfigRouting configRouting = new B2BConfigRouting();
                configRouting.setDataSource(dataSource);
                configRouting.setShopId(shopId);
                configRouting.setCustomerCategoryId(dto.getInstallTypeCode());
                configRoutings.add(configRouting);
            }
        }
        MSResponse<Map<String, Integer>> msResponse;
        //b2b-config-routing部署在厨卫系统，可以直接微服务调用
        if("CW".equals(sfProperties.getSite().getCode())){
            msResponse = b2BConfigRoutingFeign.getSystemIdList(configRoutings);
        }else{
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(mediaType, new Gson().toJson(configRoutings));
            Request request = new Request.Builder()
                    .url(sfProperties.getB2bConfigUrl()+"/b2bConfigRouting/getSystemIdList")
                    .post(requestBody)
                    .build();
            msResponse = retryUtils.syncGenericNewCall(request,
                    new TypeToken<MSResponse<Map<String, Integer>>>(){}.getType());
        }
        if (msResponse.getCode() == MSErrorCode.SUCCESS.getCode()) {
            Map<String, Integer> configRoutingMap = msResponse.getData();
            if(configRoutingMap != null && configRoutingMap.size() > 0) {
                for(OrderInfo order : orders){
                    Long id = order.getId();
                    List<InstallTypeDto> dtos = order.getInstallTypes();
                    Integer orderSystemId = 0;
                    for(InstallTypeDto dto : dtos){
                        orderSystemId = 0;
                        String key = String.format ("%s:%s:%s", dataSource ,shopId,dto.getInstallTypeCode());
                        Integer systemId = configRoutingMap.get(key);
                        // 没有找到对应系统编码
                        if (systemId == null) {
                            break;
                        }
                        // 系统编码与上一产品编码不匹配
                        if (orderSystemId != 0 && orderSystemId != systemId) {
                            orderSystemId = 0;
                            break;
                        } else {
                            orderSystemId = systemId;
                        }
                    }
                    if(orderSystemId != 0){
                        orderInfoMapper.updateOrderSystemId(id,orderSystemId);
                    }
                }
            }
        } else {
            log.error("调用微服务后，ERROR:msResponse：{}",GsonUtils.getInstance().toGson(msResponse));
            response.setCode(10000);
            response.setMsg("b2b-config-routing微服务请求错误，原因：" + msResponse.getMsg());
        }
        return response;
    }

    public void processRetryMsg(OrderInfo orderInfo) {
        String status = orderInfo.getStatus();
        // 取消通知
        if("2".equals(status)) {
            sendAutoCancel(orderInfo);
        }else{
            sendAutoTransfer(orderInfo);
        }
    }
}
