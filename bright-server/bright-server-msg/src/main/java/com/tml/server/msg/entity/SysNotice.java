package com.tml.server.msg.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.format.annotation.DateTimeFormat;

/**
* 系统通告表 Entity
*
* @author JacksonTu
* @date 2020-10-12 15:02:54
*/
@Data
@TableName("t_sys_notice")
public class SysNotice {

    /**
     * 
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    @TableField("TITLE")
    private String title;

    /**
     * 摘要
     */
    @TableField("MSG_ABSTRACT")
    private String msgAbstract;

    /**
     * 内容
     */
    @TableField("MSG_CONTENT")
    private String msgContent;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("START_TIME")
    private Date startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("END_TIME")
    private Date endTime;

    /**
     * 优先级（L低，M中，H高）
     */
    @TableField("PRIORITY")
    private String priority;

    /**
     * 消息类型1:通知公告2:系统消息
     */
    @TableField("MSG_CATEGORY")
    private String msgCategory;

    /**
     * 通告对象类型（USER:指定用户，ALL:全体用户）
     */
    @TableField("MSG_TYPE")
    private String msgType;

    /**
     * 发布状态（0未发布，1已发布，2已撤销）
     */
    @TableField("SEND_STATUS")
    private String sendStatus;

    /**
     * 发布时间
     */
    @TableField("SEND_TIME")
    private Date sendTime;

    /**
     * 撤销时间
     */
    @TableField("CANCEL_TIME")
    private Date cancelTime;

    /**
     * 指定用户
     */
    @TableField("USER_IDS")
    private String userIds;

    /**
     * 业务类型(email:邮件 bpm:流程)
     */
    @TableField("BUS_TYPE")
    private String busType;

    /**
     * 业务id
     */
    @TableField("BUS_ID")
    private String busId;

    /**
     * 打开方式(组件：component 路由：url)
     */
    @TableField("OPEN_TYPE")
    private String openType;

    /**
     * 组件/路由 地址
     */
    @TableField("OPEN_PAGE")
    private String openPage;

    /**
     * 创建人
     */
    @TableField("CREATE_USER")
    private String createUser;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    private Date createTime;

    /**
     * 更新人
     */
    @TableField("UPDATE_USER")
    private String updateUser;

    /**
     * 更新时间
     */
    @TableField("UPDATE_TIME")
    private Date updateTime;

    /**
     * 删除状态（0，正常，1已删除）
     */
    @TableField("DEL_FLAG")
    private String delFlag;

    /**
     * 通知ID
     */
    @TableField(exist = false)
    private Long noticeId;

    /**
     * 用户ID
     */
    @TableField(exist = false)
    private Long userId;

    /**
     * 阅读标志
     */
    @TableField(exist = false)
    private String readFlag;

}