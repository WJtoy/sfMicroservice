package com.kkl.kklplus.b2b.sf.entity;

import com.kkl.kklplus.entity.b2b.common.B2BBase;
import lombok.Data;

import java.util.List;

/**
 * @Auther wj
 * @Date 2020/11/24 11:00
 */
@Data
public class SfOrderHandle  extends B2BBase<SfOrderHandle> {

    private String waybillNo;

    private String taskCode;

    private String verifyCancelCode;

    private Integer operateCode;

    private String operateTime;

    private String installMaster;

    private String installContact;

    private Integer exceptionCode;

    private String content;

    private String city;

    private String appTime;

    private List<String> pics;

}
