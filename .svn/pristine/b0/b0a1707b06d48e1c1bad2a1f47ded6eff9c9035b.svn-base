package com.kkl.kklplus.b2b.sf.entity;

/**
 * describe:
 *
 * @author chenxj
 * @date 2020/11/23
 */
public enum ApiErrorCode {

    A1000("A1000","成功","校验成功"),
    A1001("A1001","必传参数不可为空","必传参数不可为空"),
    A1002("A1002","请求时效已过期","请求时效已过期"),
    A1003("A1003","IP无效","IP无效"),
    A1004("A1004","无对应服务权限","无对应服务权限"),
    A1005("A1005","流量受控","流量受控"),
    A1006("A1006","数字签名无效","数字签名无效"),
    A1007("A1007","重复请求","重复请求"),
    A1008("A1008","数据解密失败","数据解密失败"),
    A1009("A1009","目标服务异常或不可达","目标服务异常或不可达"),
    A1099("A1099","系统异常","系统异常");

    public String errorCode;
    public String errorMsg;
    public String description;

    private ApiErrorCode(String errorCode, String errorMsg, String description) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.description = description;
    }
}
