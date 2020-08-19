package com.tml.server.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tml.api.system.entity.SysUserConnection;

import java.util.List;

/**
 * @author JacksonTu
 * @version 1.0
 * @description
 * @since 2020-08-10 20:30
 */
public interface ISysUserConnectionService extends IService<SysUserConnection> {

    /**
     * 根据条件查询关联关系
     *
     * @param providerName   平台名称
     * @param providerUserId 平台用户ID
     * @return 关联关系
     */
    SysUserConnection selectByCondition(String providerName, String providerUserId);

    /**
     * 根据条件查询关联关系
     *
     * @param username 用户名
     * @return 关联关系
     */
    List<SysUserConnection> selectByCondition(String username);

    /**
     * 新增
     *
     * @param sysUserConnection userConnection
     */
    void createUserConnection(SysUserConnection sysUserConnection);

    /**
     * 删除
     *
     * @param username     username 用户名
     * @param providerName providerName 平台名称
     */
    void deleteByCondition(String username, String providerName);
}
