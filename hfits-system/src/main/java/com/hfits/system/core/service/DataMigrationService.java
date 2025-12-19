package com.hfits.system.core.service;

import com.hfits.system.core.domain.dto.*;

import java.util.List;

/**
 * 数据迁移服务接口
 *
 * @author hfits
 */
public interface DataMigrationService {

    /**
     * 获取所有实体类列表
     *
     * @return 实体类列表
     */
    List<EntityClassInfo> getEntityClasses();

    /**
     * 获取指定实体类的字段列表
     *
     * @param className 实体类全限定名
     * @return 字段列表
     */
    List<EntityFieldInfo> getEntityFields(String className);

    /**
     * 获取所有Mapper接口的insert方法列表
     *
     * @return Mapper方法列表
     */
    List<MapperMethodInfo> getMapperMethods();

    /**
     * 验证数据转换
     *
     * @param request 迁移请求
     * @return 验证结果
     */
    ValidationResult validate(MigrationRequest request);

    /**
     * 执行数据迁移
     *
     * @param request 迁移请求
     * @return 迁移结果
     */
    MigrationResult migrate(MigrationRequest request);
}
