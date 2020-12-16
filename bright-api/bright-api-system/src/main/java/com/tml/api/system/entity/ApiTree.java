package com.tml.api.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tml.common.core.entity.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author JacksonTu
 * @version 1.0
 * @description API树
 * @since 2020/10/18 14:39
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiTree extends TreeNode<SysApi> {

    /**
     * 服务ID
     */
    private String serviceId;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 响应类型
     */
    private String contentType;
}
