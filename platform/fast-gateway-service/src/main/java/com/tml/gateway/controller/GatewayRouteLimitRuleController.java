package com.tml.gateway.controller;

import com.tml.common.api.CommonResult;
import com.tml.common.web.vo.PageVo;
import com.tml.gateway.dto.GatewayRouteLimitRuleDto;
import com.tml.gateway.entity.GatewayRouteLimitRule;
import com.tml.gateway.service.IGatewayRouteLimitRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @Description 限流规则接口
 * @Author TuMingLong
 * @Date 2020/7/9 16:38
 */
@Api(value = "限流规则接口", tags = "限流规则接口")
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("gateway/routeLimitRule")
public class GatewayRouteLimitRuleController {

    private final IGatewayRouteLimitRuleService gatewayRateLimitRuleService;

    @ApiOperation(value = "限流规则分页列表", notes = "限流规则分页列表")
    @GetMapping("/list")
    //@PreAuthorize("hasAuthority('gateway:routeLimitRule:list')")
    public CommonResult<PageVo<GatewayRouteLimitRule>> pageList(GatewayRouteLimitRuleDto routeLimitRuleDto) {
        PageVo<GatewayRouteLimitRule> pageVo = gatewayRateLimitRuleService.pageList(routeLimitRuleDto);
        return CommonResult.success(pageVo);
    }

    @ApiOperation(value = "新增限流规则", notes = "新增限流规则")
    @PostMapping("/add")
    //@PreAuthorize("hasAuthority('gateway:routeLimitRule:add')")
    public CommonResult addGatewayRateLimitRule(@Valid GatewayRouteLimitRule routeLimitRule) {
        routeLimitRule.setCreateTime(LocalDateTime.now());
        gatewayRateLimitRuleService.saveGatewayRouteLimitRule(routeLimitRule);
        return CommonResult.success("新增限流规则成功");
    }

    @ApiOperation(value = "更新限流规则", notes = "更新限流规则")
    @PostMapping("/update")
    //@PreAuthorize("hasAuthority('gateway:routeLimitRule:update')")
    public CommonResult updateGatewayRateLimitRule(@Valid GatewayRouteLimitRule routeLimitRule) {
        routeLimitRule.setUpdateTime(LocalDateTime.now());
        gatewayRateLimitRuleService.updateGatewayRouteLimitRule(routeLimitRule);
        return CommonResult.success("更新限流规则成功");
    }


    @ApiOperation(value = "删除限流规则", notes = "删除限流规则")
    @ApiImplicitParam(paramType = "query", name = "ids", value = "限流规则ID集合", required = true, dataType = "String")
    @PostMapping("/delete")
    //@PreAuthorize("hasAuthority('gateway:routeLimitRule:delete')")
    public CommonResult deleteGatewayRateLimitRule(String ids) {
        String[] idArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(ids, ",");
        gatewayRateLimitRuleService.deleteGatewayRouteLimitRule(Arrays.asList(idArray));
        return CommonResult.success("删除限流规则成功");
    }
}