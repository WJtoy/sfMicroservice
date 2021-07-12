package com.kkl.kklplus.b2b.sf.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RetryResponseData {

    //id
    private Long id;

    private String json;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 子系统标识
     */
    private String site;
}
