package com.kkl.kklplus.b2b.sf.controller;

import com.google.gson.Gson;
import com.kkl.kklplus.b2b.sf.entity.OrderExpress;
import com.kkl.kklplus.b2b.sf.entity.OrderInfo;
import com.kkl.kklplus.b2b.sf.entity.RetryResponseData;
import com.kkl.kklplus.b2b.sf.entity.SysLog;
import com.kkl.kklplus.b2b.sf.http.config.B2BSFProperties;
import com.kkl.kklplus.b2b.sf.service.OrderExpressService;
import com.kkl.kklplus.b2b.sf.service.OrderInfoService;
import com.kkl.kklplus.b2b.sf.service.SysLogService;
import com.kkl.kklplus.b2b.sf.utils.GsonUtils;
import com.kkl.kklplus.b2b.sf.utils.QuarterUtils;
import com.kkl.kklplus.b2b.sf.utils.SFUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BSystemCodeEnum;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrder;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import com.kkl.kklplus.entity.common.MSPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/orderInfo")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private B2BSFProperties sfProperties;

    @Autowired
    private OrderExpressService orderExpressService;


    /**
     * 获取工单(分页)
     * @param orderSearchModel
     * @return
     */
    @PostMapping("/getList")
    public MSResponse<MSPage<B2BOrder>> getList(@RequestBody B2BOrderSearchModel orderSearchModel) {
        try {
            String systemCode = sfProperties.getSite().getCode();
            B2BSystemCodeEnum thisSystemCodeEnum = B2BSystemCodeEnum.get(systemCode);
            orderSearchModel.setSystemId(thisSystemCodeEnum.id);
            MSPage<B2BOrder> returnPage = orderInfoService.getList(orderSearchModel);
            return new MSResponse<>(MSErrorCode.SUCCESS, returnPage);
        } catch (Exception e) {
            log.error("查询工单失败:{}", e.getMessage());
            sysLogService.insert(1L,GsonUtils.getInstance().toGson(orderSearchModel),
                     e.getMessage(),"查询工单失败", SFUtils.ORDERLIST, SFUtils.REQUESTMETHOD);
            return new MSResponse<>(new MSErrorCode(1000, StringUtils.left(e.getMessage(),255)));
        }
    }

    /**
     * 检查工单是否可以转换
     * @param orderNos
     * @return
     */
    @PostMapping("/checkWorkcardProcessFlag")
    public MSResponse checkWorkcardProcessFlag(@RequestBody List<B2BOrderTransferResult> orderNos){
        try {
            if(orderNos == null){
                return new MSResponse(new MSErrorCode(1000, "参数错误，工单编号不能为空"));
            }
            //查询出对应工单的状态
            List<OrderInfo> orderInfos = orderInfoService.findOrdersProcessFlag(orderNos);
            if(orderInfos == null){
                return new MSResponse(MSErrorCode.FAILURE);
            }
            for (OrderInfo orderInfo : orderInfos) {
                if (orderInfo.getStatus() != null && Integer.valueOf(orderInfo.getStatus()) == 2) {
                    return new MSResponse(new MSErrorCode(1000, orderInfo.getCustomOrderId()+"工单已经取消,无法进行转换"));
                }
                if (orderInfo.getProcessFlag() != null && orderInfo.getProcessFlag() == B2BProcessFlag.PROCESS_FLAG_SUCESS.value) {
                    return new MSResponse(new MSErrorCode(1000, orderInfo.getCustomOrderId()+"工单已经转换成功,不能重复转换"));
                }
            }
            return new MSResponse(MSErrorCode.SUCCESS);
        }catch (Exception e){
            log.error("检查工单失败:{}", e.getMessage());
            sysLogService.insert(1L,GsonUtils.getInstance().toGson(orderNos),e.getMessage(),
                    "检查工单失败", SFUtils.CHECKPROCESSFLAG, SFUtils.REQUESTMETHOD);
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),255)));
        }
    }

    @PostMapping("/updateTransferResult")
    public MSResponse updateTransferResult(@RequestBody List<B2BOrderTransferResult> transferResults) {
        try {
            //根据数据源分组，在根据B2BOrderID分组
            Map<Long, B2BOrderTransferResult> b2bOrderIdMap = transferResults.stream().collect(
                            Collectors.toMap(B2BOrderTransferResult::getB2bOrderId, Function.identity(),(key1, key2) -> key2));
            //查询出需要转换的工单
            List<OrderInfo> orderInfos = orderInfoService.findOrdersProcessFlag(transferResults);
            //用来存放各个数据源转换成功的数量
            Map<Integer,Integer> count = new HashMap<>();
            //存放需要转换的工单集合
            List<OrderInfo> wis = new ArrayList<>();
            for(OrderInfo orderInfo :orderInfos){
                //为转换的工单才进行更新
                if(orderInfo.getProcessFlag() != B2BProcessFlag.PROCESS_FLAG_SUCESS.value){
                    B2BOrderTransferResult transferResult = b2bOrderIdMap.get(orderInfo.getId());
                    if(transferResult != null){
                        orderInfo.setProcessFlag(transferResult.getProcessFlag());
                        orderInfo.setKklOrderId(transferResult.getOrderId());
                        orderInfo.setKklOrderNo(transferResult.getKklOrderNo());
                        orderInfo.setUpdateDt(transferResult.getUpdateDt());
                        orderInfo.setProcessComment(transferResult.getProcessComment());
                        wis.add(orderInfo);
                    }
                }
            }
            orderInfoService.updateTransferResult(wis);
            return new MSResponse(MSErrorCode.SUCCESS);
        } catch (Exception e) {
            log.error("工单转换失败:{}", e.getMessage());
            sysLogService.insert(1L, GsonUtils.getInstance().toGson(transferResults),
                     e.getMessage(),"工单转换失败", SFUtils.UPDATETRANSFERRESULT, SFUtils.REQUESTMETHOD);
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),255)));

        }
    }


    @PostMapping("/cancelOrderTransition")
    public MSResponse cancelOrderTransition(@RequestBody B2BOrderTransferResult transferResult) {
        try {
            orderInfoService.cancelledOrder(transferResult);
            return new MSResponse(MSErrorCode.SUCCESS);
        }catch (Exception e){
            log.error("取消工单失败:{}", e.getMessage());
            sysLogService.insert(1L,GsonUtils.getInstance().toGson(transferResult),"取消工单失败：" + e.getMessage(),
                    "取消工单失败", SFUtils.CHECKPROCESSFLAG, SFUtils.REQUESTMETHOD);
            return new MSResponse(new MSErrorCode(1000, StringUtils.left(e.getMessage(),255)));
        }
    }

    @GetMapping("/updateSystemIdAll")
    public MSResponse updateSystemIdAll() {
        try {
            return orderInfoService.updateSystemIdAll();
        }catch (Exception e){
            log.error("更新所有标识系统的工单", e.getMessage());
            sysLogService.insert(1L,null, e.getMessage(),
                    "更新所有标识系统的工单","updateSystemIdAll", "POST");
            return new MSResponse(new MSErrorCode(10000, e.getMessage()));
        }
    }

    /**
     * 获取未知工单列表
     * @param b2BOrderSearchModel
     * @return
     */
    @PostMapping("getListUnknownOrder")
    public MSResponse<MSPage<B2BOrder>> getListUnknownOrder(@RequestBody B2BOrderSearchModel b2BOrderSearchModel) {
        try {
            b2BOrderSearchModel.setSystemId(B2BSystemCodeEnum.UNKNOWN.id);
            MSPage<B2BOrder> returnPage = orderInfoService.getList(b2BOrderSearchModel);
            return new MSResponse<>(MSErrorCode.SUCCESS, returnPage);
        } catch (Exception e) {
            log.error("获取未识别工单列表:", e.getMessage());
            sysLogService.insert(1L,new Gson().toJson(b2BOrderSearchModel),
                    "获取未识别工单列表：" + e.getMessage(),
                    "获取未识别工单列表", "getListUnknownOrder", "POST");
            return new MSResponse<>(new MSErrorCode(1000, e.getMessage()));
        }
    }



    @PostMapping("relayMessage")
    public void relayMessage(@RequestBody RetryResponseData data){
        SysLog sysLog = new SysLog();
        sysLog.setRequestUri("/orderInfo/relayMessage");
        sysLog.setParams(GsonUtils.getInstance().toGson(data));
        sysLog.setType(1);
        sysLog.setTitle("处理转发消息");
        sysLog.setCreateById(1L);
        sysLog.setCreateDt(System.currentTimeMillis());
        sysLog.preInsert();
        sysLog.setQuarter(QuarterUtils.getQuarter(sysLog.getCreateDate()));
        if (data == null || data.getId() == null || data.getJson() == null) {
            sysLog.setException("处理转发消息:请求内容null");
            log.warn(sysLog.getException());
            sysLogService.insert(sysLog);
            return;
        }
        String thisSite = sfProperties.getSite().getCode();
        String site = data.getSite();
        if(!thisSite.equalsIgnoreCase(site)){
            sysLog.setException("处理转发消息:site不匹配,本系统:" + thisSite +",请求:" + site);
            log.warn(sysLog.getException());
            sysLogService.insert(sysLog);
            //return;
        }
        if("FOP_PUSH_FIS_ORDER".equals(data.getType())){
            orderInfoService.processRetryMsg(GsonUtils.getInstance().fromJson(data.getJson(),OrderInfo.class));
        }else if("FOP_PUSH_FIS_DELIVERY_NOTICE".equals(data.getType())){
            orderExpressService.sendExpressMQ(GsonUtils.getInstance().fromJson(data.getJson(),OrderExpress.class));
        }else{
            log.error("relayMessage:没找到匹配的类型:{}",data.toString());
        }
    }
}
