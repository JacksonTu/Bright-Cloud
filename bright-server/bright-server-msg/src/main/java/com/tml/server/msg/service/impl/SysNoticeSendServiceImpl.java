package com.tml.server.msg.service.impl;

import com.tml.server.msg.entity.SysNotice;
import com.tml.server.msg.entity.SysNoticeSend;
import com.tml.server.msg.mapper.SysNoticeSendMapper;
import com.tml.server.msg.service.ISysNoticeSendService;
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
 * 用户通告阅读标记表 Service实现
 *
 * @author JacksonTu
 * @date 2020-10-12 15:03:04
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class SysNoticeSendServiceImpl extends ServiceImpl<SysNoticeSendMapper, SysNoticeSend> implements ISysNoticeSendService {

    @Override
    public IPage<SysNoticeSend> pageSysNoticeSend(QueryRequest request, SysNoticeSend sysNoticeSend) {
        LambdaQueryWrapper<SysNoticeSend> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<SysNoticeSend> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SysNoticeSend> listSysNoticeSend(SysNoticeSend sysNoticeSend) {
        LambdaQueryWrapper<SysNoticeSend> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSysNoticeSend(SysNoticeSend sysNoticeSend) {
        this.save(sysNoticeSend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSysNoticeSend(SysNoticeSend sysNoticeSend) {
        this.saveOrUpdate(sysNoticeSend);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSysNoticeSend(String[] ids) {
        List<String> list = Arrays.asList(ids);
        this.removeByIds(list);
    }

    @Override
    public List<Long> listByUserId(Long userId) {
        return this.baseMapper.listByUserId(userId);
    }

    @Override
    public SysNoticeSend findByNoticeIdAndUserId(Long noticeId, Long userId) {
        LambdaQueryWrapper<SysNoticeSend> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        queryWrapper.eq(noticeId!=null,SysNoticeSend::getNoticeId,noticeId);
        queryWrapper.eq(userId!=null,SysNoticeSend::getUserId,userId);
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    public IPage<SysNotice> pageMyNoticeSend(QueryRequest request, SysNotice sysNotice) {
        Page<SysNotice> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.baseMapper.pageMyNoticeSend(page,sysNotice);
    }
}