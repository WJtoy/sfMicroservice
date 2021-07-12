package com.kkl.kklplus.b2b.sf.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * describe:
 *
 * @author chenxj
 * @date 2020/11/23
 */
@Data
public class ApiRequestData implements Serializable {
    /**
     * 物流公司代码，SF代表顺丰
     */
    private String logisticID;
    /**
     * 请求唯一号UUID
     */
    private String requestID;

    /**
     * 接口服务代码
     */
    private String serviceCode;
    /**
     * 调用接口时间戳
     */
    private Long timestamp;
    /**
     * 数字签名
     */
    private String msgDigest;

    private String msgData;

    private int nonce;
}
