package com.kkl.kklplus.b2b.sf.entity;

public enum OrderChangeTypeEnum {

    STAKE_CARGO_STATE(  9, "提货"),
    SAPPOINTMENT_ED_STATE(  10, "预约成功"),
    SCANCEL_VERIFICATION_STATE(  11, "核销"),
    SJOB_DONE_STATE(  3, "完工"),
    SEXCEPTION_STATE( 15, "安装异常"),
    DISPATCH_INSTALLER(  1, "分配安装师傅"),
    INSTALL_EXCEPTION(  13, "安装异常"),
    APPOINTMENT_FAILED(  14, "预约失败");

    public int value;

    public String name;

    private OrderChangeTypeEnum(int value,String name){
        this.value = value;
        this.name = name;
    }
}
