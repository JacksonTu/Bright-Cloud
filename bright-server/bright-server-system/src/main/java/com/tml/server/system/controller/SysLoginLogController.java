package com.tml.server.system.controller;

import com.tml.api.system.entity.SysLoginLog;
import com.tml.common.core.entity.CommonResult;
import com.tml.common.core.entity.QueryRequest;
import com.tml.common.core.entity.constant.StringConstant;
import com.tml.common.core.utils.BrightUtil;
import com.tml.server.system.annotation.ControllerEndpoint;
import com.tml.server.system.service.ISysLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * @description 
 * @author JacksonTu
 * @since 2020-08-10 20:30
 * @version 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("loginLog")
public class SysLoginLogController {

    private final ISysLoginLogService loginLogService;

    @GetMapping
    public CommonResult loginLogList(SysLoginLog loginLog, QueryRequest request) {
        Map<String, Object> dataTable = BrightUtil.getDataTable(this.loginLogService.findLoginLogs(loginLog, request));
        return new CommonResult().data(dataTable);
    }

    @GetMapping("currentUser")
    public CommonResult getUserLastSevenLoginLogs() {
        String currentUsername = BrightUtil.getCurrentUsername();
        List<SysLoginLog> userLastSevenLoginLogs = this.loginLogService.findUserLastSevenLoginLogs(currentUsername);
        return new CommonResult().data(userLastSevenLoginLogs);
    }

    @DeleteMapping("{ids}")
    @PreAuthorize("hasAuthority('loginlog:delete')")
    @ControllerEndpoint(operation = "删除登录日志", exceptionMessage = "删除登录日志失败")
    public void deleteLogs(@NotBlank(message = "{required}") @PathVariable String ids) {
        String[] loginLogIds = ids.split(StringConstant.COMMA);
        this.loginLogService.deleteLoginLogs(loginLogIds);
    }
}
