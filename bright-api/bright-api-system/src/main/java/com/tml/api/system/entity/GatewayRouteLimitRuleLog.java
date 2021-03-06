package com.tml.api.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 限流规则日志 Entity
 *
 * @author JacksonTu
 * @date 2020-08-13 09:47:12
 */
@Data
@TableName("t_gateway_route_limit_rule_log")
public class GatewayRouteLimitRuleLog {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 黑名单IP
     */
    @TableField("ip")
    private String ip;

    /**
     * 请求URI
     */
    @TableField("request_uri")
    private String requestUri;

    /**
     * 请求方法
     */
    @TableField("request_method")
    private String requestMethod;

    /**
     * IP对应地址
     */
    @TableField("location")
    private String location;

    /**
     * 拦截时间点
     */
    @TableField("create_time")
    private Date createTime;

    @TableField(exist = false)
    private String createTimeFrom;

    @TableField(exist = false)
    private String createTimeTo;

}