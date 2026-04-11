package com.xs.chat.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xs.chat.context.BaseContext;
import com.xs.chat.mapper.ContactMapper;
import com.xs.chat.mapper.UserInfoMapper;
import com.xs.chat.pojo.DO.ContactsDO;
import com.xs.chat.pojo.DO.UserDO;
import com.xs.chat.pojo.DTO.ContactsDTO;
import com.xs.chat.pojo.VO.ContactsVO;
import com.xs.chat.service.ContactService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContactImpl implements ContactService {
    @Resource
    private ContactMapper contactsMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Override
    public List<ContactsVO> getAllById() {
        LambdaQueryWrapper<ContactsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContactsDO::getUserId, BaseContext.getCurrentId());
        Map<String,String> map = contactsMapper.selectList(queryWrapper).stream().collect(Collectors.toMap(ContactsDO::getContactId, ContactsDO::getAlias));
        List<String> idList = map.keySet().stream().toList();
        if (idList.isEmpty()){
            return List.of();
        }
        List<ContactsVO> contactsVOList = userInfoMapper.selectBatchIds(idList).stream().map(item -> {
            ContactsVO contactsVO = new ContactsVO();
            contactsVO.setUserId(BaseContext.getCurrentId());
            contactsVO.setContactId(item.getId());
            contactsVO.setContactNickname(item.getNickname());
            contactsVO.setContactAvatar(item.getAvatar());
            contactsVO.setContactDescription(item.getDescription());
            contactsVO.setAlias(map.get(item.getId()));
            contactsVO.setContactOnlineStatus(item.getOnlineStatus());
            contactsVO.setLastLoginAt(item.getLastLoginAt());
            contactsVO.setEmail(item.getEmail());
            return contactsVO;
        }).collect(Collectors.toList());
        log.info("查询联系人成功:{}",contactsVOList);
        return contactsVOList;
    }


    @Override
    public void addContact(ContactsDTO contactsDTO) {
        if (contactsDTO.getAlias() == null){
            contactsDTO.setAlias(contactsDTO.getContactNickname());
        }
        ContactsDO contactsDO = new ContactsDO();
        contactsDO.setUserId(BaseContext.getCurrentId());
        contactsDO.setAlias(contactsDTO.getAlias());
        contactsDO.setContactId(contactsDTO.getContactId());
        int row =contactsMapper.insert(contactsDO);
        if (row > 0){
            log.info("添加联系人成功:{}",contactsDO);
        }else {
            log.warn("添加联系人失败:{}",contactsDO);
        }
    }

    @Override
    public void deleteContact(String contactId) {
        LambdaQueryWrapper<ContactsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContactsDO::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(ContactsDO::getContactId, contactId);
        int row = contactsMapper.delete(queryWrapper);
        if (row > 0){
            log.info("删除联系人成功:{}",contactId);
        }else {
            log.warn("删除联系人失败:{}",contactId);
        }
    }

    @Override
    public boolean updateContact(ContactsDTO contactsDTO) {
        LambdaQueryWrapper<ContactsDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContactsDO::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(ContactsDO::getContactId, contactsDTO.getContactId());
        ContactsDO contactsDO = new ContactsDO();
        BeanUtils.copyProperties(contactsDTO, contactsDO);
        int row = contactsMapper.update(contactsDO, queryWrapper);
        if (row > 0){
            log.info("更新联系人成功:{}",contactsDO);
            return true;
        }
        log.warn("更新联系人失败:{}",contactsDO);
        return false;
    }
}
