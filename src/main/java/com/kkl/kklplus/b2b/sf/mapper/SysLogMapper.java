package com.kkl.kklplus.b2b.sf.mapper;

import com.kkl.kklplus.b2b.sf.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysLogMapper {

    Integer insert(SysLog sysLog);

    SysLog get(Long id);
}
