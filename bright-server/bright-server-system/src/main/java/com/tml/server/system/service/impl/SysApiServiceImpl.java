package com.tml.server.system.service.impl;

import com.tml.api.system.entity.*;
import com.tml.common.core.entity.TreeNode;
import com.tml.common.core.entity.constant.PageConstant;
import com.tml.common.core.utils.TreeUtil;
import com.tml.server.system.mapper.SysApiMapper;
import com.tml.server.system.service.IGatewayDynamicRouteService;
import com.tml.server.system.service.ISysApiService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tml.common.core.entity.QueryRequest;

import javax.annotation.Resource;
import java.util.*;

/**
 * 系统API接口 Service实现
 *
 * @author JacksonTu
 * @date 2020-08-20 19:22:24
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class SysApiServiceImpl extends ServiceImpl<SysApiMapper, SysApi> implements ISysApiService {

    @Resource
    IGatewayDynamicRouteService gatewayDynamicRouteService;

    @Override
    public IPage<SysApi> pageSysApi(QueryRequest request, SysApi sysApi) {
        LambdaQueryWrapper<SysApi> queryWrapper = new LambdaQueryWrapper<>(sysApi);
        // TODO 设置查询条件
        queryWrapper.eq(StringUtils.isNoneBlank(sysApi.getApiName()),SysApi::getApiName,sysApi.getApiName())
                .eq(StringUtils.isNoneBlank(sysApi.getPath()),SysApi::getPath,sysApi.getPath())
                .eq(StringUtils.isNoneBlank(sysApi.getServiceId()),SysApi::getServiceId,sysApi.getServiceId())
                .orderByDesc(SysApi::getUpdateTime);
        Page<SysApi> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SysApi> listSysApi(SysApi sysApi) {
        LambdaQueryWrapper<SysApi> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        queryWrapper.eq(StringUtils.isNoneBlank(sysApi.getApiName()),SysApi::getApiName,sysApi.getApiName())
                .eq(StringUtils.isNoneBlank(sysApi.getPath()),SysApi::getPath,sysApi.getPath())
                .eq(StringUtils.isNoneBlank(sysApi.getServiceId()),SysApi::getServiceId,sysApi.getServiceId())
                .orderByDesc(SysApi::getUpdateTime);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSysApi(SysApi sysApi) {
        this.save(sysApi);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSysApi(SysApi sysApi) {
        this.saveOrUpdate(sysApi);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSysApi(String[] ids) {
        List<String> list = Arrays.asList(ids);
        this.removeByIds(list);
    }

    @Override
    public boolean check(String apiCode) {
        LambdaQueryWrapper<SysApi> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(StringUtils.isNoneBlank(apiCode),SysApi::getApiCode, apiCode);
        int count=this.baseMapper.selectCount(queryWrapper);
        if(count>0){
            return false;
        }
        return true;
    }

    @Override
    public SysApi getSysApiByApiCode(String apiCode) {
        LambdaQueryWrapper<SysApi> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(StringUtils.isNoneBlank(apiCode),SysApi::getApiCode, apiCode);
        return this.baseMapper.selectOne(queryWrapper);
    }


    @Override
    public Map<String,Object> treeServiceId(){
        Map<String, Object> result = new HashMap<>(2);
        try {
            List<GatewayDynamicRoute> routeList= gatewayDynamicRouteService.list();
            List<GatewayDynamicRouteTree> trees = new ArrayList<>();
            buildTrees(trees, routeList);
            List<? extends TreeNode<?>> routeTree = TreeUtil.build(trees);

            result.put(PageConstant.ROWS, routeTree);
            result.put(PageConstant.TOTAL, routeTree.size());
        } catch (Exception e) {
            log.error("获取服务列表失败", e);
            result.put(PageConstant.ROWS, null);
            result.put(PageConstant.TOTAL, 0);
        }
        return result;
    }

    @Override
    public Map<String, Object> treeApi(SysApi sysApi) {
        Map<String, Object> result = new HashMap<>(2);
        try {
            LambdaQueryWrapper<SysApi> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(SysApi::getStatus,1);
            queryWrapper.like(StringUtils.isNoneBlank(sysApi.getPrefix()), SysApi::getPath, sysApi.getPrefix());
            List<SysApi> apiList= this.baseMapper.selectList(queryWrapper);
            List<ApiTree> trees = new ArrayList<>();
            buildTreesByApi(trees, apiList);
            List<? extends TreeNode<?>> apiTree = TreeUtil.build(trees);

            result.put(PageConstant.ROWS, apiTree);
            result.put(PageConstant.TOTAL, trees.size());
        } catch (Exception e) {
            log.error("获取服务列表失败", e);
            result.put(PageConstant.ROWS, null);
            result.put(PageConstant.TOTAL, 0);
        }
        return result;
    }

    private void buildTrees(List<GatewayDynamicRouteTree> trees, List<GatewayDynamicRoute> routes) {
        routes.forEach(route -> {
            GatewayDynamicRouteTree tree = new GatewayDynamicRouteTree();
            tree.setId(route.getRouteId());
            tree.setParentId("0");
            tree.setLabel(route.getRouteName());
            trees.add(tree);
        });
    }

    private void buildTreesByApi(List<ApiTree> trees, List<SysApi> apis) {
        apis.forEach(api -> {
            ApiTree tree = new ApiTree();
            tree.setId(api.getApiId().toString());
            tree.setParentId(api.getServiceId());
            tree.setLabel(api.getPath()+"-"+api.getApiName());
            tree.setPath(api.getPath());
            tree.setServiceId(api.getServiceId());
            tree.setRequestMethod(api.getRequestMethod());
            tree.setContentType(api.getContentType());
            trees.add(tree);
        });

        List<GatewayDynamicRoute> routeList= gatewayDynamicRouteService.list();
        routeList.forEach(dynamicRoute -> {
            ApiTree tree = new ApiTree();
            tree.setId(dynamicRoute.getRouteId());
            tree.setParentId("0");
            tree.setLabel(dynamicRoute.getRouteName());
            trees.add(tree);
        });
    }

}