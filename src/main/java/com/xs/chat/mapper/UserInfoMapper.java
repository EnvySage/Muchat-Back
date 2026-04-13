package com.xs.chat.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xs.chat.pojo.DO.UserDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserDO> {

    UserDO selectByEmail(String email);

    void insertUserInfo(UserDO userDO);

    UserDO selectByNickname(String nickname);

    UserDO selectById(String idFromToken);

    int updateUserInfo(UserDO userDO);

    List<UserDO> selectBatchIds(List<String> idList);

}
