package com.kkl.kklplus.b2b.sf.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * describe:
 *
 * @author chenxj
 * @date 2020/12/07
 */
@Data
public class OrderListRequestData implements Serializable {

    private List<OrderRequestData> orderList;
}
