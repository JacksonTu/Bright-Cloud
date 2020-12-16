package com.tml.common.starter.redis.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author JacksonTu
 * @version 1.0
 * @description 消息模型
 * @since 2020/8/15 10:48
 */
public class RedisSimpleMessage implements Serializable {
    /**
     * 发布者
     */
    private String publisher;
    /**
     * 内容
     */
    private String content;
    /**
     * 状态 1-添加，2-修改，3-删除
     */
    private int status;
    /**
     * 时间
     */
    private Date createTime;

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
