package com.kkl.kklplus.b2b.sf.mapper;

import com.kkl.kklplus.b2b.sf.entity.OrderExpress;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author cxj
 * 物流
 */
@Mapper
public interface OrderExpressMapper {

    Integer insert(OrderExpress express);
}
