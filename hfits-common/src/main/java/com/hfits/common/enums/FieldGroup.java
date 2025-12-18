package com.hfits.common.enums;

import java.util.Map;

public enum FieldGroup {
    PASS(),
    REJECT(),

    //系统字段组
    SYS_DEPT_CODE(
            Fields.SYS_DEPT_CODE
    ),

    //项目 部门权限字段组
    PROJ_DEPT(
            Fields.PROJ_SBDW,
            Fields.PROJ_QTDW,
            Fields.SP_EXIST_FZDW
    ),

    //项目 个人权限字段组
    PROJ_SELF(
            Fields.PROJ_CJR,
            Fields.SP_EXIST_CJR
    ),
    ;




    private final String[] fields;

    FieldGroup(String... fields) {
        this.fields = fields;
    }

    public String[] getFields() {
        return fields;
    }

    /**
     * 获取特殊字段对应的 SQL 模板
     *
     * @param fieldCode 字段代码
     * @return SQL 模板，如果不存在则返回 null
     */
    public static String getSqlTemplate(String fieldCode) {
        return Fields.sqlMap.get(fieldCode);
    }

    /**
     * 检查字段代码是否为特殊字段（存在于 sqlMap 中）
     *
     * @param fieldCode 字段代码
     * @return 是否为特殊字段
     */
    public static boolean isSpecialField(String fieldCode) {
        return Fields.sqlMap.containsKey(fieldCode);
    }
}

// todo 具体字段建表后修改
class Fields {
    // 系统字段认证
    public static final String SYS_DEPT_CODE = "dept_code";

    // 项目表字段
    public static final String PROJ_SBDW = "sbdw";
    public static final String PROJ_QTDW = "qtdw";
    public static final String PROJ_CJR = "cjr";

    // 特殊字段
    public static final String SP_EXIST_FZDW = "zrdwb.fzdw"; // 存在节点负责单位符合
    public static final String SP_EXIST_CJR = "zrdwb.cjr"; // 存在节点创建人符合
    public static final String SP_EXIST_ZRDW = "zrdwb.zrdw"; // 存在责任单位符合


    // 如果特殊字段需要不同的sql模板（如不同子表），还可以这样实现，
    public static final Map<String, String> sqlMap = Map.of(
            SP_EXIST_FZDW,
            """
                    exists (select 1 from jdb, zrdwb
                        where zrdwb.jdid = jdb.id and jdb.xmid = {}.id and {} )
                    """);

}
