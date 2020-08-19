package com.tml.server.test.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author JacksonTu
 * @version 1.0
 * @description
 * @since 2020-08-10 20:30
 */
@Data
@TableName("t_data_permission_test")
public class DataPermissionTest {
    /**
     * id
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 字段1
     */
    @TableField("FIELD1")
    private String field1;

    /**
     * 字段2
     */
    @TableField("FIELD2")
    private String field2;

    /**
     * 字段3
     */
    @TableField("FIELD3")
    private String field3;

    /**
     * 字段4
     */
    @TableField("FIELD4")
    private String field4;

    /**
     * 部门id
     */
    @TableField("DEPT_ID")
    private Integer deptId;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    private Date createTime;
}