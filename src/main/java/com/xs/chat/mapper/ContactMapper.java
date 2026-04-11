package com.xs.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xs.chat.pojo.DO.ContactsDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContactMapper extends BaseMapper<ContactsDO> {
}
