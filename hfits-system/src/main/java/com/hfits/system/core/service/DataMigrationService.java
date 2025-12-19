package com.hfits.system.core.service;

import com.hfits.common.core.domain.BaseEntity;
import com.hfits.common.utils.StringUtils;
import com.hfits.system.core.domain.dto.*;
import com.hfits.system.core.mapper.SqlserverMapper;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 数据迁移服务实现
 *
 * @author hfits
 */
@Service
public class DataMigrationService {
    public static final Logger logger = LoggerFactory.getLogger(DataMigrationService.class);

    @Autowired
    private SqlserverMapper sqlserverMapper;

    @Autowired
    private ApplicationContext applicationContext;

    private static final String ENTITY_PACKAGE_1 = "com.hfits.common.core.domain.entity";
    private static final String ENTITY_PACKAGE_2 = "com.hfits.system.core.domain";
    private static final String MAPPER_PACKAGE = "com.hfits.system.core.mapper";


    public List<EntityClassInfo> getEntityClasses() {
        List<EntityClassInfo> entityClasses = new ArrayList<>();

        // 扫描第一个包
        entityClasses.addAll(scanEntityClasses(ENTITY_PACKAGE_1));

        // 扫描第二个包
        entityClasses.addAll(scanEntityClasses(ENTITY_PACKAGE_2));

        return entityClasses;
    }

    private List<EntityClassInfo> scanEntityClasses(String packageName) {
        List<EntityClassInfo> entityClasses = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String packagePath = packageName.replace('.', '/');
            String pattern = "classpath*:" + packagePath + "/**/*.class";
            Resource[] resources = resolver.getResources(pattern);
            CachingMetadataReaderFactory readerFactory = new CachingMetadataReaderFactory();

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();

                    // 跳过内部类
                    if (className.contains("$")) {
                        continue;
                    }

                    try {
                        Class<?> clazz = Class.forName(className);
                        // 检查是否是实体类（继承BaseEntity或不是接口/枚举）
                        if (BaseEntity.class.isAssignableFrom(clazz) && !clazz.isInterface() && !clazz.isEnum()) {
                            String simpleName = clazz.getSimpleName();
                            entityClasses.add(new EntityClassInfo(simpleName, className));
                        }
                    } catch (ClassNotFoundException e) {
                        // 忽略无法加载的类
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("扫描实体类失败: " + e.getMessage(), e);
        }

        return entityClasses;
    }


    public List<EntityFieldInfo> getEntityFields(String className) {
        List<EntityFieldInfo> fields = new ArrayList<>();
        try {
            Class<?> clazz = Class.forName(className);
            Field[] declaredFields = clazz.getDeclaredFields();

            for (Field field : declaredFields) {
                // 跳过serialVersionUID和静态字段
                if (field.getName().equals("serialVersionUID") ||
                    java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                String fieldName = field.getName();
                String fieldType = field.getType().getSimpleName();

                // 检查是否有@NotNull注解
                boolean required = field.isAnnotationPresent(NotNull.class) ||
                                  field.isAnnotationPresent(jakarta.validation.constraints.NotBlank.class) ||
                                  field.isAnnotationPresent(jakarta.validation.constraints.NotEmpty.class);

                fields.add(new EntityFieldInfo(fieldName, fieldType, required));
            }

            // 如果继承BaseEntity，也包含父类字段
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && BaseEntity.class.isAssignableFrom(superClass)) {
                Field[] superFields = superClass.getDeclaredFields();
                for (Field field : superFields) {
                    if (field.getName().equals("serialVersionUID") ||
                        java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    String fieldName = field.getName();
                    String fieldType = field.getType().getSimpleName();
                    boolean required = field.isAnnotationPresent(NotNull.class);
                    fields.add(new EntityFieldInfo(fieldName, fieldType, required));
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("实体类不存在: " + className, e);
        }

        return fields;
    }


    public List<MapperMethodInfo> getMapperMethods() {
        List<MapperMethodInfo> methods = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            String packagePath = MAPPER_PACKAGE.replace('.', '/');
            String pattern = "classpath*:" + packagePath + "/**/*.class";
            Resource[] resources = resolver.getResources(pattern);
            CachingMetadataReaderFactory readerFactory = new CachingMetadataReaderFactory();

            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();

                    // 跳过内部类
                    if (className.contains("$")) {
                        continue;
                    }

                    try {
                        Class<?> clazz = Class.forName(className);
                        // 检查是否是接口
                        if (clazz.isInterface()) {
                            Method[] declaredMethods = clazz.getDeclaredMethods();
                            for (Method method : declaredMethods) {
                                String methodName = method.getName();
                                // 过滤insert开头的方法
                                if (methodName.startsWith("insert")) {
                                    Parameter[] parameters = method.getParameters();
                                    // 检查是否只有一个参数且是实体类类型
                                    if (parameters.length == 1) {
                                        Class<?> paramType = parameters[0].getType();
                                        // 检查参数类型是否是实体类（继承BaseEntity）
                                        if (BaseEntity.class.isAssignableFrom(paramType) && !paramType.isInterface()) {
                                            String paramTypeName = paramType.getName();
                                            String paramTypeSimpleName = paramType.getSimpleName();
                                            methods.add(new MapperMethodInfo(methodName, className, paramTypeName, paramTypeSimpleName));
                                        }
                                    }
                                }
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        // 忽略无法加载的类
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("扫描Mapper方法失败: " + e.getMessage(), e);
        }

        return methods;
    }


    public ValidationResult validate(MigrationRequest request) {
        ValidationResult result = new ValidationResult();
        List<ValidationError> errors = new ArrayList<>();

        try {
            // 查询部分数据
            Integer limit = request.getValidateLimit() != null ? request.getValidateLimit() : 10;
            List<Map<String, Object>> dataList = sqlserverMapper.selectTableDataLimit(request.getTableName(), limit);

            result.setTotalCount(dataList.size());
            int successCount = 0;
            int failureCount = 0;

            // 获取实体类
            Class<?> entityClass = Class.forName(request.getEntityClassName());

            // 创建字段映射Map
            Map<String, String> fieldMappingMap = new HashMap<>();
            if (request.getFieldMappings() != null) {
                for (FieldMapping mapping : request.getFieldMappings()) {
                    if (StringUtils.isNotEmpty(mapping.getSourceField()) &&
                        StringUtils.isNotEmpty(mapping.getTargetField())) {
                        fieldMappingMap.put(mapping.getSourceField(), mapping.getTargetField());
                    }
                }
            }

            List<Object> list = new ArrayList<>();
            // 遍历每条数据
            for (int i = 0; i < dataList.size(); i++) {
                Map<String, Object> sourceData = dataList.get(i);
                try {
                    // 转换为实体类对象（验证转换是否成功）
                    Object o = convertToEntity(sourceData, entityClass, fieldMappingMap);
                    list.add(o);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    String errorMsg = e.getMessage();
                    if (StringUtils.isEmpty(errorMsg)) {
                        errorMsg = e.getClass().getSimpleName();
                    }
                    errors.add(new ValidationError(i + 1, new HashMap<>(sourceData), errorMsg, e.getClass().getName()));
                }
            }
            System.out.println(list);
            result.setSuccessCount(successCount);
            result.setFailureCount(failureCount);
            result.setErrors(errors);
            result.setPassed(failureCount == 0);
            result.setMessage(String.format("验证完成：总数 %d，成功 %d，失败 %d",
                result.getTotalCount(), successCount, failureCount));

        } catch (Exception e) {
            result.setPassed(false);
            result.setMessage("验证失败: " + e.getMessage());
            throw new RuntimeException("验证失败: " + e.getMessage(), e);
        }

        return result;
    }


    public MigrationResult migrate(MigrationRequest request) {
        MigrationResult result = new MigrationResult();
        List<String> errorDetails = new ArrayList<>();

        try {
            // 查询全部数据
            List<Map<String, Object>> dataList = sqlserverMapper.selectTableData(request.getTableName());
            result.setTotalCount(dataList.size());

            // 获取实体类和Mapper
            Class<?> entityClass = Class.forName(request.getEntityClassName());
            Class<?> mapperClass = Class.forName(request.getMapperClassName());
            Object mapperBean = getMapperBean(mapperClass);
            Method insertMethod = mapperClass.getMethod(request.getMethodName(), entityClass);

            // 创建字段映射Map
            Map<String, String> fieldMappingMap = new HashMap<>();
            if (request.getFieldMappings() != null) {
                for (FieldMapping mapping : request.getFieldMappings()) {
                    if (StringUtils.isNotEmpty(mapping.getSourceField()) &&
                        StringUtils.isNotEmpty(mapping.getTargetField())) {
                        fieldMappingMap.put(mapping.getSourceField(), mapping.getTargetField());
                    }
                }
            }

            int successCount = 0;
            int failureCount = 0;

            // 分批处理（每批1000条）
            int batchSize = 1000;
            for (int i = 0; i < dataList.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, dataList.size());
                List<Map<String, Object>> batch = dataList.subList(i, endIndex);

                for (Map<String, Object> sourceData : batch) {
                    try {
                        // 转换为实体类对象
                        Object entity = convertToEntity(sourceData, entityClass, fieldMappingMap);

                        // 调用insert方法
                        insertMethod.invoke(mapperBean, entity);
                        successCount++;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        failureCount++;
                        String errorMsg = String.format("第 %d 条数据迁移失败: %s",
                            i + batch.indexOf(sourceData) + 1, e.getMessage());
                        errorDetails.add(errorMsg);
                    }
                }
            }

            result.setSuccessCount(successCount);
            result.setFailureCount(failureCount);
            result.setErrorDetails(errorDetails);
            result.setMessage(String.format("迁移完成：总数 %d，成功 %d，失败 %d",
                result.getTotalCount(), successCount, failureCount));

        } catch (Exception e) {
            result.setMessage("迁移失败: " + e.getMessage());
            throw new RuntimeException("迁移失败: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * 将源数据转换为实体类对象
     */
    private Object convertToEntity(Map<String, Object> sourceData, Class<?> entityClass, Map<String, String> fieldMappingMap) throws Exception {
        Object entity = entityClass.getDeclaredConstructor().newInstance();

        // 获取所有字段（包括父类字段）
        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = entityClass;
        while (currentClass != null && BaseEntity.class.isAssignableFrom(currentClass)) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (!field.getName().equals("serialVersionUID") &&
                    !java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    allFields.add(field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        // 遍历字段映射
        for (Map.Entry<String, String> entry : fieldMappingMap.entrySet()) {
            String sourceField = entry.getKey();
            String targetField = entry.getValue();

            Object value = sourceData.get(sourceField);
            if (value == null) {
                continue;
            }

            // 找到目标字段
            Field targetFieldObj = null;
            for (Field field : allFields) {
                if (field.getName().equals(targetField)) {
                    targetFieldObj = field;
                    break;
                }
            }

            if (targetFieldObj == null) {
                continue;
            }

            targetFieldObj.setAccessible(true);
            Class<?> fieldType = targetFieldObj.getType();

            // 类型转换
            Object convertedValue = convertValue(value, fieldType);
            targetFieldObj.set(entity, convertedValue);
        }

        return entity;
    }

    /**
     * 值类型转换
     */
    private Object convertValue(Object value, Class<?> targetType) throws Exception {
        if (value == null) {
            return null;
        }

        String strValue = value.toString().trim();
        if (StringUtils.isEmpty(strValue)) {
            return null;
        }

        // String类型
        if (targetType == String.class) {
            return strValue;
        }

        // Long类型
        if (targetType == Long.class || targetType == long.class) {
            try {
                return Long.parseLong(strValue);
            } catch (NumberFormatException e) {
                // 尝试解析为double再转long
                return (long) Double.parseDouble(strValue);
            }
        }

        // Integer类型
        if (targetType == Integer.class || targetType == int.class) {
            try {
                return Integer.parseInt(strValue);
            } catch (NumberFormatException e) {
                return (int) Double.parseDouble(strValue);
            }
        }

        // Double类型
        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(strValue);
        }

        // Float类型
        if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(strValue);
        }

        // Boolean类型
        if (targetType == Boolean.class || targetType == boolean.class) {
            String lower = strValue.toLowerCase();
            return "true".equals(lower) || "1".equals(lower) || "yes".equals(lower) || "y".equals(lower);
        }

        // Date类型
        if (targetType == Date.class || targetType == java.sql.Date.class) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                return sdf.parse(strValue);
            } catch (Exception e) {
                // 尝试其他格式
                try {
                    sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.parse(strValue);
                } catch (Exception e2) {
                    throw new Exception("日期格式转换失败: " + strValue, e2);
                }
            }
        }

        // 其他类型，尝试直接转换
        return value;
    }

    /**
     * 获取Mapper Bean（从Spring容器获取）
     */
    private Object getMapperBean(Class<?> mapperClass) {
        return applicationContext.getBean(mapperClass);
    }
}
