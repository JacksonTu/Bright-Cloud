package com.tml.server.msg.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tml.common.core.entity.constant.BrightConstant;
import com.tml.common.core.entity.constant.WebsocketConstant;
import com.tml.server.msg.entity.SysNotice;
import com.tml.server.msg.entity.SysNoticeSend;
import com.tml.server.msg.service.ISysNoticeSendService;
import com.tml.common.core.entity.CommonResult;
import com.tml.common.core.entity.QueryRequest;
import com.tml.common.core.exception.BrightException;
import com.tml.common.core.utils.BrightUtil;
import com.tml.common.core.entity.constant.StringConstant;
import com.tml.server.msg.websocket.CommonWebSocket;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.Map;

/**
 * 用户通告阅读标记表 Controller
 *
 * @author JacksonTu
 * @date 2020-10-12 15:03:04
 */
@Slf4j
@Validated
@RestController
@RequestMapping("notice/send")
@RequiredArgsConstructor
public class SysNoticeSendController {

    private final ISysNoticeSendService sysNoticeSendService;

    private final CommonWebSocket webSocket;

    @GetMapping
    @PreAuthorize("hasAuthority('notice:send:list')")
    public CommonResult listSysNoticeSend(SysNoticeSend sysNoticeSend) {
        return new CommonResult().data(sysNoticeSendService.listSysNoticeSend(sysNoticeSend));
    }

    @GetMapping("list")
    @PreAuthorize("hasAuthority('notice:send:list')")
    public CommonResult pageSysNoticeSend(QueryRequest request, SysNoticeSend sysNoticeSend) {
        Map<String, Object> dataTable = BrightUtil.getDataTable(this.sysNoticeSendService.pageSysNoticeSend(request, sysNoticeSend));
        return new CommonResult().data(dataTable);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('notice:send:add')")
    public void saveSysNoticeSend(@Valid SysNoticeSend sysNoticeSend) throws BrightException {
        try {
            this.sysNoticeSendService.saveSysNoticeSend(sysNoticeSend);
        } catch (Exception e) {
            String message = "新增SysNoticeSend失败";
            log.error(message, e);
            throw new BrightException(message);
        }
    }

    @DeleteMapping("{ids}")
    @PreAuthorize("hasAuthority('notice:send:delete')")
    public void deleteSysNoticeSend(@NotBlank(message = "{required}") @PathVariable String ids) throws BrightException {
        try {
            String[] idArray = ids.split(StringConstant.COMMA);
            this.sysNoticeSendService.deleteSysNoticeSend(idArray);
        } catch (Exception e) {
            String message = "删除SysNoticeSend失败";
            log.error(message, e);
            throw new BrightException(message);
        }
    }

    @PutMapping
    @PreAuthorize("hasAuthority('notice:send:update')")
    public void updateSysNoticeSend(SysNoticeSend sysNoticeSend) throws BrightException {
        try {
            this.sysNoticeSendService.updateSysNoticeSend(sysNoticeSend);
        } catch (Exception e) {
            String message = "修改SysNoticeSend失败";
            log.error(message, e);
            throw new BrightException(message);
        }
    }

    /**
     * 更新用户系统消息阅读状态
     * @param noticeId
     * @return
     * @throws BrightException
     */
    @GetMapping("editByNoticeIdAndUserId/{noticeId}")
    public CommonResult editByNoticeIdAndUserId(@PathVariable("noticeId") Long noticeId) throws BrightException{
        try {
            Long userId = BrightUtil.getCurrentUser().getUserId();
            LambdaUpdateWrapper<SysNoticeSend> updateWrapper = new UpdateWrapper().lambda();
            updateWrapper.set(SysNoticeSend::getReadFlag, BrightConstant.HAS_READ_FLAG);
            updateWrapper.set(SysNoticeSend::getReadTime, new Date());
            updateWrapper.last("where notice_id ="+noticeId+" and user_id ="+userId);
            SysNoticeSend noticeSend = new SysNoticeSend();
            noticeSend.setCreateUser(BrightUtil.getCurrentUsername());
            noticeSend.setCreateTime(new Date());
            noticeSend.setUpdateUser(BrightUtil.getCurrentUsername());
            noticeSend.setUpdateTime(new Date());
            this.sysNoticeSendService.update(noticeSend, updateWrapper);

            JSONObject obj = new JSONObject();
            obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_REVOKE);
            webSocket.sendAllMessage(obj.toJSONString());

            return new CommonResult().data(true);
        } catch (Exception e) {
            String message = "更新阅读状态失败";
            log.error(message, e);
            throw new BrightException(message);
        }
    }

    /**
     * 获取我的消息
     * @param request
     * @param sysNotice
     * @return
     */
    @GetMapping("getMyNoticeSend")
    public CommonResult getMyNoticeSend(QueryRequest request, SysNotice sysNotice) {
        Long userId = BrightUtil.getCurrentUser().getUserId();
        sysNotice.setUserId(userId);
        Map<String, Object> dataTable = BrightUtil.getDataTable(this.sysNoticeSendService.pageMyNoticeSend(request, sysNotice));
        return new CommonResult().data(dataTable);
    }

    /**
     * 一键已读
     * @return
     * @throws BrightException
     */
    @GetMapping("readAll")
    public CommonResult readAll() throws BrightException{
        try {
            Long userId = BrightUtil.getCurrentUser().getUserId();
            LambdaUpdateWrapper<SysNoticeSend> updateWrapper = new UpdateWrapper().lambda();
            updateWrapper.set(SysNoticeSend::getReadFlag, BrightConstant.HAS_READ_FLAG);
            updateWrapper.set(SysNoticeSend::getReadTime, new Date());
            updateWrapper.last("where user_id ="+userId);

            SysNoticeSend noticeSend = new SysNoticeSend();
            this.sysNoticeSendService.update(noticeSend, updateWrapper);

            JSONObject obj = new JSONObject();
            obj.put(WebsocketConstant.MSG_CMD, WebsocketConstant.CMD_REVOKE);
            webSocket.sendAllMessage(obj.toJSONString());

            return new CommonResult().data(true);
        } catch (Exception e) {
            String message = "全部已读失败";
            log.error(message, e);
            throw new BrightException(message);
        }
    }
}
