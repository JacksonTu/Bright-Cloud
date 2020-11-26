package com.tml.common.core.entity.constant;

/**
 * @author JacksonTu
 * @version 1.0
 * @description Websocket常量类
 * @since 2020/10/15 17:02
 */
public interface WebsocketConstant {

    /**
     * 消息json key:cmd
     */
    String MSG_CMD = "cmd";

    /**
     * 消息json key:msgId
     */
    String MSG_ID = "msgId";

    /**
     * 消息json key:msgTitle
     */
    String MSG_TITLE = "msgTitle";

    /**
     * 消息json key:msgTxt
     */
    String MSG_TXT = "msgTxt";

    /**
     * 消息json key:userId
     */
    String MSG_USER_ID = "userId";

    /**
     * 消息类型 heartCheck
     */
    String CMD_CHECK = "heartCheck";

    /**
     * 消息类型 user 用户消息
     */
    String CMD_USER = "user";

    /**
     * 消息类型 revoke 撤销
     */
    String CMD_REVOKE = "revoke";

    /**
     * 消息类型 topic 系统通知
     */
    String CMD_TOPIC = "topic";

    /**
     * 消息类型 email
     */
    String CMD_EMAIL = "email";

    /**
     * 消息类型 sign 会议签到
     */
    String CMD_SIGN = "sign";

    /**
     * 消息类型 新闻发布/取消
     */
    String NEWS_PUBLISH = "publish";
}
