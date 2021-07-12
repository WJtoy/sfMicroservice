package com.kkl.kklplus.b2b.sf.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * describe:
 *
 * @author chenxj
 * @date 2020/11/23
 */
@Data
public class OrderExpressRequestData implements Serializable {
    /**
     * 母单号
     */
    private String waybillNo;
    /**
     * 任务编码
     */
    private String taskCode;
    /**
     * 到货时间
     */
    private String time;
    /**
     * 提货网点
     */
    private String pickupZone;
    /**
     * 提货联系人
     */
    private String pickupContact;
    /**
     * 提货联系电话
     */
    private String pickupPhone;
    /**
     * 提货地址
     */
    private String pickupAddress;


    /**
     * 收件人
     */
    private String receiveContact;
    /**
     * 收件人电话
     */
    private String receivePhone;
    /**
     * 收件人地址的省
     */
    private String receiveProvince;
    /**
     * 收件人地址的市
     */
    private String receiveCity;
    /**
     * 收件人地址的区
     */
    private String receiveCounty;
    /**
     * 收件人地址
     */
    private String receiveAddress;
    /**
     * 服务类型
     */
    private Integer serviceType;

}
