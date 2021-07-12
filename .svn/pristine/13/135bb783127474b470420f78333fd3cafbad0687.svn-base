package com.kkl.kklplus.b2b.sf.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OrderInfo extends B2BBase<OrderInfo> {

    private Integer systemId;

    private Integer saleChannel;

    private String kklOrderNo;

    private Long kklOrderId;

    /**
     * 母单号
     */
    private String waybillNo;
    /**
     * 任务编码
     */
    private String taskCode;
    /**
     * 子单号用逗号（,）隔开，此字段可以用于提货+安装业务的提货包裹数的校验。
     */
    private String subWaybillNos;
    /**
     * 订单来源，如京东
     */
    private String orderSource;
    /**
     * 客户订单号
     */
    private String customOrderId;
    /**
     * 货物名称
     */
    private String cargoName;
    /**
     * 货物的计费体积
     */
    private String totalVolume;
    /**
     * 整票的计费重量
     */
    private String totalWeight;
    /**
     * 收件人
     */
    private String receiverName;
    /**
     * 收件人电话
     */
    private String receiverPhone;
    /**
     * 收件人地址
     */
    private String receiverAddress;
    /**
     * 子母件数
     */
    private Integer packageCount;
    /**
     * 版本号
     */
    private Integer version;
    /**
     * 订单的状态1：新增/修改 2：取消，可以使用version判断数据版本号
     */
    private String status;
    /**
     * 服务类型
     */
    private Integer serviceType;

    private String remark;
    /**
     * 要安装的品类
     */
    private List<InstallTypeDto> installTypes;

    private String installTypesJson;

    private Map<String,String> extendInfo;

    private String extendInfoJson;

}
