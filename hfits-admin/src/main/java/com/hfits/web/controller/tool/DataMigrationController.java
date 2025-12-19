package com.hfits.web.controller.tool;

import com.hfits.common.core.controller.BaseController;
import com.hfits.common.core.domain.Resp;
import com.hfits.system.core.domain.dto.*;
import com.hfits.system.core.service.DataMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据迁移控制器
 *
 * @author hfits
 */
@RestController
@RequestMapping("/dataMigration")
public class DataMigrationController extends BaseController {

    @Autowired
    private DataMigrationService dataMigrationService;

    /**
     * 获取所有实体类列表
     */
    @GetMapping("/entityClasses")
    public Resp<List<EntityClassInfo>> getEntityClasses() {
        List<EntityClassInfo> entityClasses = dataMigrationService.getEntityClasses();
        return successR(entityClasses);
    }

    /**
     * 获取指定实体类的字段列表
     */
    @GetMapping("/entityFields/{className}")
    public Resp<List<EntityFieldInfo>> getEntityFields(@PathVariable String className) {
        List<EntityFieldInfo> fields = dataMigrationService.getEntityFields(className);
        return successR(fields);
    }

    /**
     * 获取所有Mapper接口的insert方法列表
     */
    @GetMapping("/mapperMethods")
    public Resp<List<MapperMethodInfo>> getMapperMethods() {
        List<MapperMethodInfo> methods = dataMigrationService.getMapperMethods();
        return successR(methods);
    }

    /**
     * 验证数据转换
     */
    @PostMapping("/validate")
    public Resp<ValidationResult> validate(@RequestBody MigrationRequest request) {
        try {
            ValidationResult result = dataMigrationService.validate(request);
            return successR(result);
        } catch (Exception e) {
            logger.error("验证失败", e);
            return errorR("验证失败: " + e.getMessage());
        }
    }

    /**
     * 执行数据迁移
     */
    @PostMapping("/migrate")
    public Resp<MigrationResult> migrate(@RequestBody MigrationRequest request) {
        try {
            MigrationResult result = dataMigrationService.migrate(request);
            return successR(result);
        } catch (Exception e) {
            logger.error("迁移失败", e);
            return errorR("迁移失败: " + e.getMessage());
        }
    }
}
