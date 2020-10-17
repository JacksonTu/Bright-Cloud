package com.tml.server.system.service.impl;

import com.tml.api.system.entity.SysApi;
import com.tml.server.system.mapper.SysApiMapper;
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
import java.util.Arrays;
import java.util.List;

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
}