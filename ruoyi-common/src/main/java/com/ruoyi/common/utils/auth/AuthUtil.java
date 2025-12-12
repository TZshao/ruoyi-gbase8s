package com.ruoyi.common.utils.auth;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.FieldGroup;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限 SQL 构建工具
 */
public final class AuthUtil {
    private static final Logger logger = LoggerFactory.getLogger(AuthUtil.class);

    public static final String DATA_SCOPE = "dataScope";
    //全部数据权限
    public static final String DATA_SCOPE_ALL = "1";

    //自定数据权限
    public static final String DATA_SCOPE_CUSTOM = "2";

    //部门数据权限
    public static final String DATA_SCOPE_DEPT = "3";

    //部门及以下数据权限
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    //仅本人数据权限
    public static final String DATA_SCOPE_SELF = "5";


    private AuthUtil() {
    }

    private static final String EXIST_SQL = """
            exists (select 1 from jdb, zrdw where zrdw.jdid = jdb.id and jdb.xmid = {} and ({}))
            """;


    /* ========================== 对外入口 ========================== */

    public static void apply(BaseEntity entity, SysUser user, String tableAlias, FieldGroup fieldGroup) {
        if (entity == null || user == null || user.isAdmin()) {
            return;
        }

        String sql = build(user, tableAlias, fieldGroup);
        if (StringUtils.isNotBlank(sql)) {
            logger.debug("追加权限过滤sql: AND ({})", sql);
            entity.getParams().put(
                    DATA_SCOPE,
                    StringUtils.format(" AND ({})", sql)
            );
        }
    }

    /* ========================== 主构建入口 ========================== */
    public static String build(SysUser user, String tableAlias, FieldGroup fieldGroup) {
        if (user == null || fieldGroup == null) {
            return "";
        }

        String[] fields = fieldGroup.getFields();
        if (fields == null || fields.length == 0) {
            return "";
        }

        String scope = user.getDataScope();
        String[] deptCode = new String[]{user.getDeptCode()};
        String[] authDeptCodes = user.getAuthDeptCodes();

        return switch (scope) {
            //结果类似 t.field1 = deptCode or t.field2 = deptCode or exist ...
            case DATA_SCOPE_DEPT -> //本人部门
                    buildByCodes(tableAlias, fields, deptCode, false);

            //结果类似 t.field1 like deptCode% or t.field2 like deptCode% or exist ...
            case DATA_SCOPE_DEPT_AND_CHILD -> //本人及以下部门
                    buildByCodes(tableAlias, fields, deptCode, true);


            //结果类似 t.field1 in (deptCode1,deptCode2..) or t.field2 in (deptCode1,deptCode2,deptCode3) or exist ...
            case DATA_SCOPE_CUSTOM -> //自定义
                    buildByCodes(tableAlias, fields, authDeptCodes, false);

            //本人数据  t.fields[0] = user.userName
            case DATA_SCOPE_SELF -> buildSelf(user, tableAlias, fields);
            default -> "";
        };
    }

    /* ========================== 核心：按 code 构建 ========================== */
    private static String buildByCodes(String tableAlias, String[] fields, String[] codes, boolean fuzzy) {

        if (codes == null || codes.length == 0) {
            return "";
        }

        List<String> conditions = new ArrayList<>();

        for (String field : fields) {
            String cond;
            if ("exist".equals(field)) {
                cond = buildExist(tableAlias, codes, fuzzy);
            } else {
                cond = buildField(tableAlias, field, codes, fuzzy);
            }

            if (StringUtils.isNotBlank(cond)) {
                conditions.add(cond);
            }
        }

        return joinOr(conditions);
    }

    /* ========================== 普通字段 ========================== */
    private static String buildField(String tableAlias, String field, String[] codes, boolean fuzzy) {

        String column = StringUtils.format("{}.{}", tableAlias, field);

        // 单值：= 或 like
        if (codes.length == 1) {
            String code = codes[0];
            if (StringUtils.isBlank(code)) {
                return "";
            }
            return fuzzy
                    ? StringUtils.format("{} like '{}%'", column, code)
                    : StringUtils.format("{} = '{}'", column, code);
        }

        // 多值：in
        String in = toInClause(codes);
        return StringUtils.isBlank(in)
                ? ""
                : StringUtils.format("{} in {}", column, in);
    }

    /* ========================== exist 子查询 ========================== */
    private static String buildExist(String tableAlias, String[] codes, boolean fuzzy) {

        String xmId = StringUtils.format("{}.id", tableAlias);
        List<String> parts = new ArrayList<>();

        for (String code : codes) {
            if (StringUtils.isBlank(code)) {
                continue;
            }
            parts.add(
                    fuzzy
                            ? StringUtils.format("zrdw.code like '{}%'", code)
                            : StringUtils.format("zrdw.code = '{}'", code)
            );
        }

        if (parts.isEmpty()) {
            return "";
        }

        return StringUtils.format(
                EXIST_SQL,
                xmId,
                joinOr(parts)
        );
    }

    /* ========================== 仅本人 ========================== */
    private static String buildSelf(SysUser user,
                                    String tableAlias,
                                    String[] fields) {

        String userName = user.getUserName();
        if (StringUtils.isBlank(userName)) {
            return "";
        }

        return StringUtils.format(
                "{} = '{}'",
                StringUtils.format("{}.{}", tableAlias, fields[0]),
                userName
        );
    }

    /* ========================== 工具方法 ========================== */

    private static String toInClause(String[] values) {
        List<String> list = new ArrayList<>();
        for (String v : values) {
            if (StringUtils.isNotBlank(v)) {
                list.add(StringUtils.format("'{}'", v));
            }
        }
        return list.isEmpty()
                ? ""
                : StringUtils.format("({})", String.join(",", list));
    }

    private static String joinOr(List<String> parts) {
        return parts.isEmpty()
                ? ""
                : String.join(" OR ", parts);
    }
}
