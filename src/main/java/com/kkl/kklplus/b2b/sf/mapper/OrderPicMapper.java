package com.kkl.kklplus.b2b.sf.mapper;

import com.kkl.kklplus.b2b.sf.entity.OrderPic;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther wj
 * @Date 2020/11/26 16:34
 */
@Mapper
public interface OrderPicMapper {

    Integer insert(OrderPic orderPic);


    void updateProcessResult(OrderPic orderPic);

}
