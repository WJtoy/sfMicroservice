package com.kkl.kklplus.b2b.sf.mapper;

import com.kkl.kklplus.entity.sf.sd.SfOrderHandle;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther wj
 * @Date 2020/11/24 13:57
 */
@Mapper
public interface SFOrderHandleMapper {

    Long insert(SfOrderHandle sfOrderHandle);

    Integer updateProcessResult(SfOrderHandle sfOrderHandle);


}
