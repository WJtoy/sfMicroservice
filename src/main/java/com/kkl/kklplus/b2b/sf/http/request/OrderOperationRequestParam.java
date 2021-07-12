package com.kkl.kklplus.b2b.sf.http.request;

import lombok.Data;

import java.util.Date;

/**
 * describe:
 *
 * @author chenxj
 * @date 2020/11/23
 */
@Data
public class OrderOperationRequestParam extends RequestParam{

    private String waybillNo;

    private String taskCode;

    private Date operateTime;

    private String content;

    private Integer operateCode;

    private Date appTime;

    private String installContact;

    private String installMaster;

    private String city;
}
