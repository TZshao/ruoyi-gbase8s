package com.ruoyi.common.enums;

public enum FieldGroup {
    //系统字段组
    SYS_DEPT_CODE(Fields.SYS_DEPT_CODE),



    COMMON_CASE(Fields.SBDW, Fields.QTDW, Fields.JD_EXIST),

    CASE1(Fields.LHQY, Fields.JD_EXIST);

    private final String[] fields;

    FieldGroup(String... fields) {
        this.fields = fields;
    }

    public String[] getFields() {
        return fields;
    }
}

class Fields {
    //系统字段认证
    public static final String SYS_DEPT_CODE = "dept_code";

    //项目表字段
    public static final String SBDW = "sbdw";
    public static final String QTDW = "qtdw";
    public static final String LHQY = "lhqy";

    //特殊字段 ： 生成sql "exists (select 1 from jdb, zrdw  where zrdw.jdid = jdb.id and jdb.xmid = {} and ({}))";
    public static final String JD_EXIST = "exist";
}
