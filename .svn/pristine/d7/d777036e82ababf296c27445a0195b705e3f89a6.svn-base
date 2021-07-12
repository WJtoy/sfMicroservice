package com.kkl.kklplus.b2b.sf.service;

import com.kkl.kklplus.b2b.sf.entity.OrderPic;
import com.kkl.kklplus.b2b.sf.entity.SysLog;
import com.kkl.kklplus.b2b.sf.mapper.OrderPicMapper;
import com.kkl.kklplus.b2b.sf.utils.QuarterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Auther wj
 * @Date 2020/11/26 16:42
 */
@Service
@Slf4j
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OrderPicService {

    @Autowired
    private OrderPicMapper orderPicMapper;


    public void insert(OrderPic orderPic){

        orderPic.setUpdateById(orderPic.getCreateById());
        orderPic.setUpdateDt(System.currentTimeMillis());
        orderPic.setQuarter(QuarterUtils.getQuarter(new Date(orderPic.getCreateDt())));
        try {
            orderPicMapper.insert(orderPic);
        }catch (Exception e){
            log.error("报错信息记录失败:{}",e.getMessage());
        }
    }

    public void updateProcessResult(OrderPic orderPic){

        orderPic.setUpdateDt(System.currentTimeMillis());
        try {
            orderPicMapper.updateProcessResult(orderPic);
        }catch (Exception e){
            log.error("报错信息更新失败:{}",e.getMessage());
        }

    }



}
