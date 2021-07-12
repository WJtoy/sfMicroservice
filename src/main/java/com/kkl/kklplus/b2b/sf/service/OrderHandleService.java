package com.kkl.kklplus.b2b.sf.service;

import com.kkl.kklplus.b2b.sf.entity.ApiErrorCode;
import com.kkl.kklplus.b2b.sf.entity.OrderChangeTypeEnum;
import com.kkl.kklplus.b2b.sf.entity.OrderInfo;
import com.kkl.kklplus.b2b.sf.entity.OrderPic;
import com.kkl.kklplus.b2b.sf.http.command.OperationCommand;
import com.kkl.kklplus.b2b.sf.http.config.B2BSFProperties;
import com.kkl.kklplus.b2b.sf.http.request.OrderOperationRequestParam;
import com.kkl.kklplus.b2b.sf.http.request.OrderPictureRequestParam;
import com.kkl.kklplus.b2b.sf.http.response.OrderHandleResponse;
import com.kkl.kklplus.b2b.sf.http.response.OrderHandleResultResponse;
import com.kkl.kklplus.b2b.sf.http.response.ResponseBody;
import com.kkl.kklplus.b2b.sf.http.utils.HttpUtils;
import com.kkl.kklplus.b2b.sf.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.sf.mapper.SFOrderHandleMapper;
import com.kkl.kklplus.b2b.sf.utils.GsonUtils;
import com.kkl.kklplus.b2b.sf.utils.QuarterUtils;
import com.kkl.kklplus.b2b.sf.utils.SFUtils;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.sf.sd.SfOrderHandle;
import com.sun.javafx.binding.StringFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Auther wj
 * @Date 2020/11/24 11:46
 */

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderHandleService {

    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private B2BProcesslogService b2BProcesslogService;
    @Resource
    private SFOrderHandleMapper sfOrderHandleMapper;
    @Autowired
    private SysLogService sysLogService;
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    private OrderPicService orderPicService;

    @Autowired
    private B2BSFProperties sfProperties;
    /**
     * 预约成功
     * @param sfOrderHandle
     * @return
     */
    public MSResponse appointment(SfOrderHandle sfOrderHandle) {

        String strMsg  = validateOrderHandle(sfOrderHandle);
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(strMsg)) {
            stringBuilder.append(strMsg);
        }
        if (stringBuilder.length() >0) {
            return new MSResponse(new MSErrorCode(MSErrorCode.FAILURE.getCode(),stringBuilder.toString()));
        }
        OrderInfo orderInfo =orderInfoService.findByOrderId(sfOrderHandle.getB2bOrderId());
        if (orderInfo == null) {
            log.error("预约异常:没找到对应工单:{}:{}",sfOrderHandle.getB2bOrderId(),sfOrderHandle.toString());
            return new MSResponse(MSErrorCode.FAILURE);
        }
            sfOrderHandle.setTaskCode(orderInfo.getTaskCode());
            sfOrderHandle.setWaybillNo(orderInfo.getWaybillNo());
            return common(sfOrderHandle, "appointment");

    }



    private String validateOrderHandle(SfOrderHandle sfOrderHandle) {
        StringBuilder stringBuilder = new StringBuilder();
        if (sfOrderHandle.getB2bOrderId() == null) {
            stringBuilder.append("B2BOrderId不能为空.");
        }

        if (sfOrderHandle.getAppTime() == null || sfOrderHandle.getAppTime() == 0){
            stringBuilder.append("预约时间不能为空.");
        }
        if (sfOrderHandle.getCreateById() == null) {
            stringBuilder.append("createById不能为空.");
        }
        if (sfOrderHandle.getCreateDt() == null) {
            stringBuilder.append("createDt不能为空.");
        }
        return stringBuilder.toString();
    }


    /**
     *安装异常
     * @param sfOrderHandle
     * @return
     */
    public MSResponse installException(SfOrderHandle sfOrderHandle){
        String strMsg  = validate(sfOrderHandle);
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(strMsg)) {
            stringBuilder.append(strMsg);
        }
        if(StringUtils.isBlank(sfOrderHandle.getCity())){
            stringBuilder.append("city不能为空.");
        }
        if (stringBuilder.length() >0) {
            return new MSResponse(new MSErrorCode(MSErrorCode.FAILURE.getCode(),stringBuilder.toString()));
        }
        OrderInfo orderInfo =orderInfoService.findByOrderId(sfOrderHandle.getB2bOrderId());
        if (orderInfo == null) {
            log.error("退单异常:没找到对应工单:{}:{}",sfOrderHandle.getB2bOrderId(),sfOrderHandle.toString());
            return new MSResponse(MSErrorCode.FAILURE);
        }
        sfOrderHandle.setTaskCode(orderInfo.getTaskCode());
        sfOrderHandle.setWaybillNo(orderInfo.getWaybillNo());
        return common(sfOrderHandle,"installException");
    }

    private String validate(SfOrderHandle sfOrderHandle){
        StringBuilder stringBuilder = new StringBuilder();
        if (sfOrderHandle.getB2bOrderId() == null) {
            stringBuilder.append("B2BOrderId不能为空.");
        }

        if (sfOrderHandle.getOperateCode().equals(OrderChangeTypeEnum.DISPATCH_INSTALLER.value)){
            if (sfOrderHandle.getInstallMaster() == null || sfOrderHandle.getInstallContact()==null){
                stringBuilder.append("分配师傅和联系方式不能为空.");
            }
        }
        if (sfOrderHandle.getCreateById() == null) {
            stringBuilder.append("createById不能为空.");
        }
        if (sfOrderHandle.getCreateDt() == null) {
            stringBuilder.append("createDt不能为空.");
        }
        return stringBuilder.toString();
    }

    /**
     *分配安装师傅
     * @param sfOrderHandle
     * @return
     */
    public MSResponse installMaster(SfOrderHandle sfOrderHandle){
        String strMsg  = validate(sfOrderHandle);
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(strMsg)) {
            stringBuilder.append(strMsg);
        }
        if (stringBuilder.length() >0) {
            return new MSResponse(new MSErrorCode(MSErrorCode.FAILURE.getCode(),stringBuilder.toString()));
        }
        OrderInfo orderInfo =orderInfoService.findByOrderId(sfOrderHandle.getB2bOrderId());
        if (orderInfo == null) {
            log.error("派单异常:没找到对应工单:{}:{}",sfOrderHandle.getB2bOrderId(),sfOrderHandle.toString());
            return new MSResponse(MSErrorCode.FAILURE);
        }
        sfOrderHandle.setTaskCode(orderInfo.getTaskCode());
        sfOrderHandle.setWaybillNo(orderInfo.getWaybillNo());
        return common(sfOrderHandle,"installMaster");
    }


    public MSResponse finish(SfOrderHandle sfOrderHandle){
        String strMsg  = validate(sfOrderHandle);
        StringBuilder stringBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(strMsg)) {
            stringBuilder.append(strMsg);
        }
        if (stringBuilder.length() >0) {
            return new MSResponse(new MSErrorCode(MSErrorCode.FAILURE.getCode(),stringBuilder.toString()));
        }
        OrderInfo orderInfo =orderInfoService.findByOrderId(sfOrderHandle.getB2bOrderId());
        if (orderInfo == null) {
            log.error("完工异常:没找到对应工单:{}:{}",sfOrderHandle.getB2bOrderId(),sfOrderHandle.toString());
            return new MSResponse(MSErrorCode.FAILURE);
        }
        sfOrderHandle.setWaybillNo(orderInfo.getWaybillNo());
        sfOrderHandle.setTaskCode(orderInfo.getTaskCode());
        try {

            List<MultipartFile> list = new ArrayList<>();
            MSResponse msResponse = common(sfOrderHandle, "finish");
            if (sfOrderHandle.getPics().size()>0) {
                taskExecutor.execute(()->{
                    OrderPic orderPic = new OrderPic();
                    String pic = GsonUtils.getInstance().toGson(sfOrderHandle.getPics());
                    orderPic.setPicture(pic);
                    orderPic.setB2bOrderId(sfOrderHandle.getB2bOrderId());
                    orderPic.setWaybillNo(sfOrderHandle.getWaybillNo());
                    orderPic.setCreateById(sfOrderHandle.getCreateById());
                    orderPic.setCreateDt(sfOrderHandle.getCreateDt());
                    orderPic.setTaskCode(sfOrderHandle.getTaskCode());
                    orderPicService.insert(orderPic);
                    for (String picture : sfOrderHandle.getPics()){
                        if (StringUtils.isNotBlank(picture)) {
                            MultipartFile file= OkHttpUtils.getRequestFile(picture);
                            list.add(file);
                        }
                    }
                    ResponseBody<OrderHandleResultResponse> responseBody = null;
                    try {
                        OrderPictureRequestParam orderPictureRequestParam = new OrderPictureRequestParam();
                        orderPictureRequestParam.setImageType(68);
                        orderPictureRequestParam.setTaskCode(sfOrderHandle.getTaskCode());
                        orderPictureRequestParam.setWaybillNo(sfOrderHandle.getWaybillNo());
                        orderPictureRequestParam.setImages(list);
                        orderPictureRequestParam.setDigest(OkHttpUtils.getDigest(list.size(),orderPictureRequestParam.getImageType(),
                                        orderPictureRequestParam.getTaskCode(),orderPictureRequestParam.getWaybillNo()));
                        OperationCommand operationCommand = new OperationCommand();
                        operationCommand.setOpCode(OperationCommand.OperationCode.ORDER_PICTURE_UPLOAD);
                        operationCommand.setReqBody(orderPictureRequestParam);
                        responseBody  =
                                HttpUtils.postSyncGeneric(operationCommand,orderPictureRequestParam,OrderHandleResultResponse.class);
                        OrderHandleResultResponse data =responseBody.getData();
                        if (responseBody.getErrorCode() == 0 && data != null && data.getSuccess()){
                            orderPic.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                            orderPic.setProcessComment(data.getErrorMessage());
                            orderPicService.updateProcessResult(orderPic);
                        }else {
                            String errorStr = "";
                            if (data != null) {
                                errorStr = data.getErrorMessage();
                            } else {
                                errorStr = responseBody.getErrorMsg();
                            }
                            orderPic.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
                            orderPic.setProcessComment(errorStr);
                            orderPicService.updateProcessResult(orderPic);
                        }
                    }catch (Exception e){
                        log.info("请求异常-->{}:{}",orderPic.toString(),responseBody != null ? responseBody.getOriginalJson():"",e);
                        sysLogService.insert(1L,GsonUtils.getInstance().toGson(orderPic),
                                e.getMessage(),"图片回传请求异常", "pictureUpload", SFUtils.REQUESTMETHOD);
                    }
                });
            }
            return msResponse;
        }catch (Exception e){
            log.info("请求异常-->{}",e);
            sysLogService.insert(1L,GsonUtils.getInstance().toGson(sfOrderHandle.getPics()),
                    e.getMessage(),"请求异常", "finish", SFUtils.REQUESTMETHOD);
            return new MSResponse<>(new MSErrorCode(1000, StringUtils.left(e.getMessage(),255)));
        }
    }




    public MSResponse common(SfOrderHandle sfOrderHandle,String interfaceName){
        B2BOrderProcesslog b2BProcesslog = new B2BOrderProcesslog();
        b2BProcesslog.setUpdateById(sfOrderHandle.getCreateById());
        b2BProcesslog.setInterfaceName(OperationCommand.OperationCode.FOP_RECE_FIS_OPEARTION_UPLOAD.serviceCode);
        b2BProcesslog.setCreateById(sfOrderHandle.getCreateById());
        b2BProcesslog.setCreateDt(System.currentTimeMillis());
        b2BProcesslog.setQuarter(QuarterUtils.getQuarter(b2BProcesslog.getCreateDt()));

        sfOrderHandle.setOperateTime(sfOrderHandle.getCreateDt());
        sfOrderHandle.setUpdateById(sfOrderHandle.getCreateById());
        sfOrderHandle.setUpdateDt(System.currentTimeMillis());
        sfOrderHandle.setQuarter(QuarterUtils.getQuarter(sfOrderHandle.getCreateDt()));
        sfOrderHandleMapper.insert(sfOrderHandle);
        OrderOperationRequestParam orderOperationRequestParam= new OrderOperationRequestParam();
        try {
            orderOperationRequestParam.setWaybillNo(sfOrderHandle.getWaybillNo());
            orderOperationRequestParam.setOperateCode(sfOrderHandle.getOperateCode());
            orderOperationRequestParam.setTaskCode(sfOrderHandle.getTaskCode());
            orderOperationRequestParam.setOperateTime(new Date(sfOrderHandle.getOperateTime()));
            orderOperationRequestParam.setContent(sfOrderHandle.getContent());
            Integer operateCode = sfOrderHandle.getOperateCode();
            String content = sfOrderHandle.getContent();
            //预约时必填
            if(operateCode.equals(OrderChangeTypeEnum.SAPPOINTMENT_ED_STATE.value)){
                orderOperationRequestParam.setAppTime(new Date(sfOrderHandle.getAppTime()));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    String format = simpleDateFormat.format(orderOperationRequestParam.getAppTime());
                    if(StringUtils.isNotBlank(sfOrderHandle.getInstallContact())) {
                        orderOperationRequestParam.setContent(String.format("安装师傅:%s,联系方式:%s,预约时间:%s", sfOrderHandle.getInstallMaster(),
                                sfOrderHandle.getInstallContact(), format));
                    }else{
                        orderOperationRequestParam.setContent("预约时间:"+format);
                    }
                }catch (Exception e){
                    log.error("预约时间格式化异常:{}:{}",sfOrderHandle.getB2bOrderId(),sfOrderHandle.getAppTime());
                    orderOperationRequestParam.setContent("师傅预约");
                }
                //分配安装师傅时必填
            }else if (operateCode.equals(OrderChangeTypeEnum.DISPATCH_INSTALLER.value)){
                orderOperationRequestParam.setInstallContact(sfOrderHandle.getInstallContact());
                orderOperationRequestParam.setInstallMaster(sfOrderHandle.getInstallMaster());
                orderOperationRequestParam.setContent
                        (String.format("安装师傅:%s,联系方式:%s",sfOrderHandle.getInstallMaster(),sfOrderHandle.getInstallContact()));
                //安装异常
            }else if(operateCode.equals(OrderChangeTypeEnum.INSTALL_EXCEPTION.value)){
                orderOperationRequestParam.setCity(sfOrderHandle.getCity());
                orderOperationRequestParam.setContent(StringUtils.isNotBlank(content)?"取消原因:"+ content:"服务单安装异常");
            }else if(operateCode.equals(OrderChangeTypeEnum.SJOB_DONE_STATE.value)){
                orderOperationRequestParam.setContent("师傅已服务完工");
            }
            OperationCommand command  =new OperationCommand();
            command.setReqBody(orderOperationRequestParam);
            command.setOpCode(OperationCommand.OperationCode.FOP_RECE_FIS_OPEARTION_UPLOAD);

            String reqbodyJson = GsonUtils.getInstance().toGson(orderOperationRequestParam);
            b2BProcesslog.setInfoJson(reqbodyJson);
            b2BProcesslogService.insert(b2BProcesslog);
            ResponseBody<OrderHandleResponse> responseBody = OkHttpUtils.postSyncGenericNew(command,OrderHandleResponse.class);
            b2BProcesslog.setResultJson(responseBody.getOriginalJson());
            OrderHandleResponse data = responseBody.getData();
            OrderHandleResultResponse handleResultResponse = null;

            if (responseBody.getErrorCode() == 0 && data != null && data.getApiResultCode().equals(ApiErrorCode.A1000.errorCode)){

                handleResultResponse = GsonUtils.getInstance().fromJson(data.getApiResultData(), OrderHandleResultResponse.class);

                if(handleResultResponse !=null && handleResultResponse.getSuccess()) {
                    b2BProcesslog.setUpdateDt(System.currentTimeMillis());
                    b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    b2BProcesslogService.updateProcessFlag(b2BProcesslog);
                    sfOrderHandle.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
                    sfOrderHandle.setUpdateDt(System.currentTimeMillis());
                    sfOrderHandleMapper.updateProcessResult(sfOrderHandle);
                    return new MSResponse(MSErrorCode.SUCCESS);
                }
            }
            MSResponse msResponse = new MSResponse();
            String errorStr = "";
            if(handleResultResponse != null){
                errorStr = handleResultResponse.getErrorMessage();
            }else if(data != null){
                errorStr = data.getApiErrorMsg();
            }else {
                errorStr =responseBody.getErrorMsg();
                msResponse.setErrorCode(new MSErrorCode(10000,errorStr));
            }
            msResponse.setThirdPartyErrorCode(new MSErrorCode(10000,errorStr));
            b2BProcesslog.setProcessComment(errorStr);
            b2BProcesslog.setUpdateDt(System.currentTimeMillis());
            b2BProcesslog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            b2BProcesslogService.updateProcessFlag(b2BProcesslog);

            sfOrderHandle.setProcessComment(errorStr);
            sfOrderHandle.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            sfOrderHandle.setUpdateDt(System.currentTimeMillis());
            sfOrderHandleMapper.updateProcessResult(sfOrderHandle);
            return msResponse;
        }catch (Exception e){
            log.error("请求异常-->{}",e);
            sysLogService.insert(1L,GsonUtils.getInstance().toGson(orderOperationRequestParam),
                    e.getMessage(),"请求异常", interfaceName, SFUtils.REQUESTMETHOD);
            return new MSResponse<>(new MSErrorCode(1000, StringUtils.left(e.getMessage(),255)));

        }
    }
}
