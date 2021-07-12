package com.kkl.kklplus.b2b.sf.controller;
import com.kkl.kklplus.b2b.sf.entity.OrderChangeTypeEnum;
import com.kkl.kklplus.b2b.sf.http.request.UrlRequestParam;
import com.kkl.kklplus.b2b.sf.http.utils.OkHttpUtils;
import com.kkl.kklplus.b2b.sf.service.OrderHandleService;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.sf.sd.SfOrderHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * describe:
 *
 * @author chenxj
 * @date 2020/11/24
 */

@Slf4j
@RestController
@RequestMapping("/orderHandle")
public class OrderHandleController {
    @Autowired
    private OrderHandleService orderHandleService;

    @PostMapping("/appointment")
    public MSResponse appointment(@RequestBody SfOrderHandle sfOrderHandle){
        sfOrderHandle.setOperateCode(OrderChangeTypeEnum.SAPPOINTMENT_ED_STATE.value);  // 10
        return  orderHandleService.appointment(sfOrderHandle);
    }



    public Long autoBookDateNextHours() {
        Calendar createCalendar = Calendar.getInstance();
        createCalendar.setTime(new Date(System.currentTimeMillis()));
        createCalendar.add(Calendar.HOUR_OF_DAY, 1);
        createCalendar.set(Calendar.MINUTE, 0);
        createCalendar.set(Calendar.SECOND, 0);
        createCalendar.set(Calendar.MILLISECOND, 0);
        return createCalendar.getTimeInMillis();
    }
    @PostMapping("/cancel")
    public MSResponse cancel(@RequestBody SfOrderHandle sfOrderHandle){
        SfOrderHandle handle = new SfOrderHandle();
        handle.setB2bOrderId(sfOrderHandle.getB2bOrderId());
        handle.setUniqueId(sfOrderHandle.getUniqueId()-1);
        handle.setWaybillNo(sfOrderHandle.getWaybillNo());
        handle.setTaskCode(sfOrderHandle.getTaskCode());
        handle.setOperateCode(OrderChangeTypeEnum.SAPPOINTMENT_ED_STATE.value);  // 10
        handle.setContent("");
        handle.setInstallMaster("");
        handle.setInstallContact("");
        handle.setCreateDt(sfOrderHandle.getCreateDt());
        handle.setCreateById(sfOrderHandle.getCreateById());
        handle.setAppTime(autoBookDateNextHours());
        orderHandleService.appointment(handle);
        sfOrderHandle.setCreateDt(System.currentTimeMillis());
        sfOrderHandle.setOperateCode(OrderChangeTypeEnum.INSTALL_EXCEPTION.value);  // 13
        return orderHandleService.installException(sfOrderHandle);
    }


    @PostMapping("/planned")
    public MSResponse planned(@RequestBody SfOrderHandle sfOrderHandle){
        sfOrderHandle.setOperateCode(OrderChangeTypeEnum.DISPATCH_INSTALLER.value); // 1
        return orderHandleService.installMaster(sfOrderHandle);
    }



    @PostMapping("finish")
    public MSResponse finish(@RequestBody SfOrderHandle sfOrderHandle){
        sfOrderHandle.setOperateCode(OrderChangeTypeEnum.SJOB_DONE_STATE.value); // 3
        return orderHandleService.finish(sfOrderHandle);
    }



    @PostMapping("testPicture")
    public MSResponse testPicture(@RequestBody UrlRequestParam url){
        OkHttpUtils.getRequestFile(url.getUrl());
        return new MSResponse();
    }

}
