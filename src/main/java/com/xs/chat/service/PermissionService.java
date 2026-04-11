package com.xs.chat.service;

import com.xs.chat.enumeration.permission.GroupPermissionEnum;
import com.xs.chat.enumeration.permission.GroupRoleEnum;

public interface PermissionService {

    void checkPermission(Long chatRoomId, String userId, GroupPermissionEnum permission);

    GroupRoleEnum getRole(Long chatRoomId, String userId);

    void cleanRoleCache(Long chatRoomId, String userId);

    void cleanRoomCache(Long chatRoomId);
}
