package com.tml.server.msg.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tml.common.core.entity.constant.BrightConstant;
import com.tml.common.core.entity.constant.WebsocketConstant;
import com.tml.server.msg.entity.SysNotice;
import com.tml.server.msg.entity.SysNoticeSend;
import com.tml.server.msg.service.ISysNoticeSendService;
import com.tml.server.msg.service.ISysNoticeService;
import com.tml.common.core.entity.CommonResult;
import com.tml.common.core.entity.QueryRequest;
import com.tml.common.core.exception.BrightException;
import com.tml.common.core.utils.BrightUtil;
import com.tml.common.core.entity.constant.StringConstant;
import com.tml.server.msg.websocket.CommonWebSocket;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统通告表 Controller
 *
 * @author JacksonTu
 * @date 2020-10-12 15:02:54
 */
@Slf4j
@Validated
@RestController
@RequestMapping("notice")
@RequiredArgsConstructor
public class SysNoticeController {

    private final ISysNoticeService sysNoticeService;

    private final ISysNoticeSendService sysNoticeSendService;

    private final CommonWebSocket webSocket;


    @GetMapping
    @PreAuthorize("hasAuthority('notice:list')")
    public CommonResult listSysNotice(SysNotice sysNotice) {
        return new CommonResult().data(sysNoticeService.listSysNotice(sysNotice));
    }

    @GetMapping("list")
    @PreAuthorize("hasAuthority('notice:list')")
    public CommonResult pageSysNotice(QueryRequest request, SysNotice sysNotice) {
        Map<String, Object> dataTable = BrightUtil.getDataTable(this.sysNoticeService.pageSysNotice(request, sysNotice));
        return new CommonResult().data(dataTable);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('notice:add')")
    public void saveSysNotice(@Valid SysNotice sysNotice) throws BrightException {
        try {
            sysNotice.setCreateUser(BrightUtil.getCurrentUsername());
            sysNotice.setCreateTime(new Date());
            this.sysNoticeService.saveSysNotice(sysNotice);
        } catch (Exception e) {
            String message = "新增失败";
            log.error(message, e);
            throw new BrightException(message);
        }
    }

    @DeleteMapping("{ids}")
    @PreAuthorize("hasAuthority('notice:delete')")
    public void deleteSysNotice(@NotBlank(message = "{required}") @PathVariable String ids) throws BrightException {
        try {
            String[] idArray = ids.split(StringConstant.COMMA);
            this.sysNoticeService.deleteSysNotice(idArray);
        } catch (Exception e) {
            String message = "删除失败";
            log.error(message, e);
            throw new BrightException(message);
        }
    }

    @PutMapping
    @PreAuthorize("hasAuthority('notice:update')")
    public void updateSysNotice(SysNotice sysNotice) throws BrightException {
        try {
            sysNotice.setUpdateUser(BrightUtil.getCurrentUsername());
            sysNotice.setUpdateTime(new Date());
            this.sysNoticeService.updateSysNotice(sysNotice);
        } catch (Exception e) {
            String message = "修改失败";
            log.error(message, e);
            throw new BrightException(message);
        }
    }

    /**
     * 发布操作
     * @return
     */
    @GetMapping("release/{id}")
    @PreAuthorize("hasAuthority('notice:release')")
    public CommonResult doReleaseData(@PathVariable("id") Long id) throws BrightException{
        try {
            String message="成功";
            SysNotice notice=this.sysNoticeService.getById(id);
            if(notice==null){
                throw new BrightException("未找到对应实体");
            }else{
                notice.setSendStatus("1");
                notice.setCreateUser(BrightUtil.getCurrentUsername());
                notice.setUpdateUser(BrightUtil.getCurrentUsername());
                notice.setSendTime(new Date());
                notice.setUpdateTime(new Date());
                boolean flag=this.sysNoticeService.updateById(notice);
                if(flag){
                    message="成功";
                    // 1.全局推送
                    if(notice.getMsgType().equals(BrightConstant.MSG_TYPE_ALL)){
                        JSONObject obj = new JSONObject();
                        obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_TOPIC);
                        obj.put(WebsocketConstant.MSG_ID, notice.getId());
                        obj.put(WebsocketConstant.MSG_TITLE, notice.getTitle());
                        obj.put(WebsocketConstant.MSG_TXT, notice.getMsgAbstract());
                        webSocket.sendAllMessage(obj.toJSONString());
                    }else {
                        // 2.插入用户通告阅读标记表记录
                        String userId = notice.getUserIds();
                        String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
                        JSONObject obj = new JSONObject();
                        obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_USER);
                        obj.put(WebsocketConstant.MSG_ID, notice.getId());
                        obj.put(WebsocketConstant.MSG_TITLE, notice.getTitle());
                        obj.put(WebsocketConstant.MSG_TXT, notice.getMsgAbstract());
                        webSocket.sendMoreMessage(userIds, obj.toJSONString());
                    }
                }
            }
            return new CommonResult().data(message);
        } catch (Exception e) {
            String message = "失败";
            log.error(message, e);
            throw new BrightException(message);
        }
    }

    /**
     * 撤销操作
     * @return
     */
    @GetMapping("revoke/{id}")
    @PreAuthorize("hasAuthority('notice:revoke')")
    public CommonResult doRevokeData(@PathVariable("id") Long id) throws BrightException{
        try {
            String message="";
            SysNotice notice=this.sysNoticeService.getById(id);
            if(notice==null){
                throw new BrightException("未找到对应实体");
            }else{
                notice.setSendStatus("2");
                notice.setCreateUser(BrightUtil.getCurrentUsername());
                notice.setUpdateUser(BrightUtil.getCurrentUsername());
                notice.setCancelTime(new Date());
                notice.setUpdateTime(new Date());
                boolean flag=this.sysNoticeService.updateById(notice);
                if(flag) {
                    message="成功";
                }
            }
            return new CommonResult().data(message);
        } catch (Exception e) {
            String message = "失败";
            log.error(message, e);
            throw new BrightException(message);
        }
    }

    /**
     * 补充用户数据，并返回系统消息
     * @return
     */
    @GetMapping("listNoticeByUser")
    public CommonResult listNoticeByUser(){
        Long userId=BrightUtil.getCurrentUser().getUserId();
        List<Long> noticeIds=this.sysNoticeSendService.listByUserId(userId);
        // 1.将系统消息补充到用户通告阅读标记表中
        List<SysNotice> notices=this.sysNoticeService.listByCondition(BrightConstant.MSG_TYPE_ALL,BrightConstant.HAS_SEND,BrightUtil.getCurrentUser().getCreateTime(),noticeIds);
        if(notices!=null && notices.size()>0){
            notices.forEach(notice ->{
                SysNoticeSend noticeSend= this.sysNoticeSendService.findByNoticeIdAndUserId(notice.getId(),userId);
                if(noticeSend==null){
                    SysNoticeSend ns = new SysNoticeSend();
                    ns.setNoticeId(notice.getId());
                    ns.setUserId(userId);
                    ns.setReadFlag(BrightConstant.NO_READ_FLAG);
                    ns.setCreateTime(new Date());
                    ns.setCreateUser(BrightUtil.getCurrentUser().getUsername());
                    ns.setUpdateTime(new Date());
                    ns.setUpdateUser(BrightUtil.getCurrentUser().getUsername());
                    this.sysNoticeSendService.save(ns);
                }
            });
        }
        // 2.将用户消息补充到用户通告阅读标记表中
        List<SysNotice> userNotices=this.sysNoticeService.listByCondition(BrightConstant.MSG_TYPE_USER,BrightConstant.HAS_SEND,BrightUtil.getCurrentUser().getCreateTime(),noticeIds);
        if(userNotices!=null && userNotices.size()>0){
            userNotices.forEach(notice ->{
                SysNoticeSend noticeSend= this.sysNoticeSendService.findByNoticeIdAndUserId(notice.getId(),userId);
                if(noticeSend==null && notice.getUserIds().contains(userId.toString())){
                    SysNoticeSend ns = new SysNoticeSend();
                    ns.setNoticeId(notice.getId());
                    ns.setUserId(userId);
                    ns.setReadFlag(BrightConstant.NO_READ_FLAG);
                    ns.setCreateTime(new Date());
                    ns.setCreateUser(BrightUtil.getCurrentUser().getUsername());
                    ns.setUpdateTime(new Date());
                    ns.setUpdateUser(BrightUtil.getCurrentUser().getUsername());
                    this.sysNoticeSendService.save(ns);
                }
            });
        }
        // 3.查询用户未读的系统消息
        QueryRequest queryRequest=new QueryRequest();
        queryRequest.setPageSize(5);
        //通知公告消息
        IPage<SysNotice> noticePage=this.sysNoticeService.pageUnreadMsg(queryRequest,userId,"1");
        //系统消息
        IPage<SysNotice> msgPage=this.sysNoticeService.pageUnreadMsg(queryRequest,userId,"2");
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("sysMsgList", msgPage.getRecords());
        params.put("sysMsgTotal", msgPage.getTotal());
        params.put("noticeMsgList", noticePage.getRecords());
        params.put("noticeMsgTotal", noticePage.getTotal());
        return new CommonResult().data(params);
    }

    /**
     * 同步消息
     * @param noticeId
     * @return
     * @throws BrightException
     */
    @GetMapping("syncNotice/{noticeId}")
    public CommonResult syncNotice(@PathVariable(name="noticeId") Long noticeId) throws BrightException{
        JSONObject obj = new JSONObject();
        if(noticeId!=null){
            SysNotice notice = this.sysNoticeService.getById(noticeId);
            if(notice==null) {
                throw new BrightException("未找到对应实体");
            }else {
                if(notice.getMsgType().equals(BrightConstant.MSG_TYPE_ALL)) {
                    obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_TOPIC);
                    obj.put(WebsocketConstant.MSG_ID, notice.getId());
                    obj.put(WebsocketConstant.MSG_TXT, notice.getTitle());
                    webSocket.sendAllMessage(obj.toJSONString());
                }else {
                    // 2.插入用户通告阅读标记表记录
                    String userId = notice.getUserIds();
                    if(StringUtils.isNoneBlank(userId)){
                        String[] userIds = userId.substring(0, (userId.length()-1)).split(",");
                        obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_USER);
                        obj.put(WebsocketConstant.MSG_ID, notice.getId());
                        obj.put(WebsocketConstant.MSG_TXT, notice.getTitle());
                        webSocket.sendMoreMessage(userIds, obj.toJSONString());
                    }
                }
            }
        }else{
            obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_TOPIC);
            obj.put(WebsocketConstant.MSG_TXT, "批量设置已读");
            webSocket.sendAllMessage(obj.toJSONString());
        }
        return new CommonResult().data("成功");
    }
}
