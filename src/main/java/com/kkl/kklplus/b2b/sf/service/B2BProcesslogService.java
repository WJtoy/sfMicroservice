package com.kkl.kklplus.b2b.sf.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.kkl.kklplus.b2b.sf.entity.ApiOperationResponseData;
import com.kkl.kklplus.b2b.sf.entity.SysLog;
import com.kkl.kklplus.b2b.sf.mapper.B2BProcesslogMapper;
import com.kkl.kklplus.b2b.sf.mapper.SysLogMapper;
import com.kkl.kklplus.b2b.sf.utils.GsonUtils;
import com.kkl.kklplus.b2b.sf.utils.QuarterUtils;
import com.kkl.kklplus.b2b.sf.utils.SFUtils;
import com.kkl.kklplus.entity.b2b.common.B2BProcessFlag;
import com.kkl.kklplus.entity.b2bcenter.md.B2BInterfaceIdEnum;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BOrderProcesslog;
import com.kkl.kklplus.entity.b2bcenter.rpt.B2BProcessLogSearchModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class B2BProcesslogService {

    @Resource
    private B2BProcesslogMapper b2BProcesslogMapper;

    @Autowired
    private SysLogService sysLogService;

    /**
     * 添加原始数据
     * @param processlog
     */
    public void insert(B2BOrderProcesslog processlog){
        b2BProcesslogMapper.insert(processlog);
    }

    public void updateProcessFlag(B2BOrderProcesslog processlog) {
        try{
            processlog.preUpdate();
            b2BProcesslogMapper.updateProcessFlag(processlog);
        }catch (Exception e) {
            log.error("原始数据结果修改错误:{}\n{}", processlog.toString(),e.getMessage());
            sysLogService.insert(1L, GsonUtils.getInstance().toGson(processlog),
                    e.getMessage(),"原始数据结果修改错误", "PUSH", SFUtils.REQUESTMETHOD);
        }
    }

    public Page<B2BOrderProcesslog> getList(B2BProcessLogSearchModel processLogSearchModel, String code) {
        if (processLogSearchModel.getPage() != null) {
            PageHelper.startPage(processLogSearchModel.getPage().getPageNo(), processLogSearchModel.getPage().getPageSize());
            Page<B2BOrderProcesslog> list = b2BProcesslogMapper.getList(processLogSearchModel,code);
            for(B2BOrderProcesslog log : list){
                log.setInterfaceName(
                        B2BInterfaceIdEnum.getByCode(log.getInterfaceName()).description);
            }
            return list;
        }
        else {
            return null;
        }
    }

    public Integer updateProcessResult(B2BOrderProcesslog processlog,
                                       ApiOperationResponseData operationResponseData) {
        try{
            if(operationResponseData.getSuccess()){
                processlog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_SUCESS.value);
            }else{
                operationResponseData.setErrorCode("400");
                processlog.setProcessComment(operationResponseData.getErrorMessage());
                processlog.setProcessFlag(B2BProcessFlag.PROCESS_FLAG_FAILURE.value);
            }
            processlog.setResultJson(GsonUtils.getInstance().toGson(operationResponseData));
            processlog.preUpdate();
            return b2BProcesslogMapper.updateProcessFlag(processlog);
        }catch (Exception e) {
            log.error("原始数据结果修改错误:{}\n{}\n{}", processlog.toString(),
                    operationResponseData.toString(),
                    e.getMessage());
            sysLogService.insert(1L, GsonUtils.getInstance().toGson(processlog),
                    e.getMessage(),"原始数据结果修改错误", "PUSH", SFUtils.REQUESTMETHOD);
            return 0;
        }
    }
}
