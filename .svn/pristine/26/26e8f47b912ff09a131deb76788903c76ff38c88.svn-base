package com.kkl.kklplus.b2b.sf.http.command;

import com.kkl.kklplus.b2b.sf.http.request.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class OperationCommand {

    public enum OperationCode {


        FOP_RECE_FIS_OPEARTION_UPLOAD(1001, "操作节点回传接口", "FOP_RECE_FIS_OPEARTION_UPLOAD",OrderOperationRequestParam.class),
        ORDER_PICTURE_UPLOAD(1002,"图片回传接口","ORDER_PICTURE_UPLOAD",OrderPictureRequestParam.class);
        public int code;
        public String name;
        public String serviceCode;
        public Class reqBodyClass;

        private OperationCode(int code, String name, String serviceCode, Class reqBodyClass) {
            this.code = code;
            this.name = name;
            this.serviceCode = serviceCode;
            this.reqBodyClass = reqBodyClass;
        }
    }

    @Getter
    @Setter
    private OperationCode opCode;

    @Getter
    @Setter
    private RequestParam reqBody;

    public static OperationCommand newInstance(OperationCode opCode, RequestParam reqBody) {
        OperationCommand command = new OperationCommand();
        command.opCode = opCode;
        command.reqBody = reqBody;
        return command;
    }
}
