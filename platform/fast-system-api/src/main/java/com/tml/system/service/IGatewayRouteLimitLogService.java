package com.tml.system.service;

import com.tml.common.web.service.IBaseService;
import com.tml.common.web.vo.PageVo;
import com.tml.system.dto.GatewayRouteLimitLogDto;
import com.tml.system.entity.GatewayRouteLimitLog;

/**
 * @Description 限流日志服务类接口
 * @Author TuMingLong
 * @Date 2020/5/10 16:48
 */
public interface IGatewayRouteLimitLogService extends IBaseService<GatewayRouteLimitLog> {

    PageVo<GatewayRouteLimitLog> pageList(GatewayRouteLimitLogDto gatewayRouteLimitLogDto);

}