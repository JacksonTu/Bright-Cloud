package com.tml.server.msg.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tml.server.msg.entity.SysNotice;
import com.tml.server.msg.entity.SysNoticeSend;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户通告阅读标记表 Mapper
 *
 * @author JacksonTu
 * @date 2020-10-12 15:03:04
 */
public interface SysNoticeSendMapper extends BaseMapper<SysNoticeSend> {

    List<Long> listByUserId(@Param("userId") Long userId);

    /**
     * 获取我的消息
     * @param page
     * @param sysNotice
     * @return
     */
    IPage<SysNotice> pageMyNoticeSend(Page<SysNotice> page, @Param("sysNotice") SysNotice sysNotice);

}
