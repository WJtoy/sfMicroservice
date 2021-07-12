package com.kkl.kklplus.b2b.sf.utils;

public class SFUtils {

    public static final String RE_HTML_MARK = "(<[^<]*?>)|(<[\\s]*?/[^<]*?>)|(<[^<]*?/[\\s]*?>)";

    public final static String REQUESTMETHOD = "POST";
    /**
     * 分页查询的地址
     */
    public final static String ORDERLIST = "/orderInfo/getList";
    /**
     * 检查工单的地址
     */
    public final static String CHECKPROCESSFLAG = "/orderInfo/checkWorkcardProcessFlag";
    /**
     * 更新工单状态的地址
     */
    public final static String UPDATETRANSFERRESULT = "/orderInfo/updateTransferResult";

}
