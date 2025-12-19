package com.hfits.common.utils.auth;

import com.hfits.common.core.domain.BaseEntity;
import com.hfits.common.core.domain.entity.SysUser;
import com.hfits.common.enums.FieldGroup;
import com.hfits.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 当前实现是基于 字段组 + 部门编码，判断字段组中某个字段是否 等于/匹配 编码
 * <p>
 * 不要让业务模型入侵权限模型：这里不应该出现，某个具体的表，或者具体的字段
 */
public final class AuthUtil {
    private static final Logger logger = LoggerFactory.getLogger(AuthUtil.class);

    public static final String DATA_SCOPE = "dataScope";
    //全部数据权限
    public static final String DATA_SCOPE_ALL = "1";

    //自定部门数据权限
    public static final String DATA_SCOPE_CUSTOM = "2";

    //本部门数据权限
    public static final String DATA_SCOPE_DEPT = "3";

    //本部门及以下数据权限
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    //仅本人数据权限
    public static final String DATA_SCOPE_SELF = "5";


    private AuthUtil() {
    }


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
        if (FieldGroup.REJECT.equals(fieldGroup)) {
            return " 1 = 0 ";
        } else if (FieldGroup.PASS.equals(fieldGroup)) {
            return " 1 = 1";
        }
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
            case DATA_SCOPE_DEPT -> //本人部门
                    buildByCodes(tableAlias, fields, deptCode, false);

            case DATA_SCOPE_DEPT_AND_CHILD -> //本人及以下部门
                    buildByCodes(tableAlias, fields, deptCode, true);

            case DATA_SCOPE_CUSTOM -> //自定义
                    buildByCodes(tableAlias, fields, filterPrefixCodes(authDeptCodes), true);

            //本人数据  t.fields[0] = user.userName
            case DATA_SCOPE_SELF -> buildSelf(user, tableAlias, fields);
            case DATA_SCOPE_ALL -> "";
            default -> "";
        };
    }

    /* ========================== 核心：按 (字段 + deptCode) 构建 ========================== */

    /*
     *  对每个字段，buildField构建出  t.field = deptCode(%) ，然后  joinOr，用 or 合并
     *  如果是自定义范围，则是 t.field in (code...) 然后 joinOr
     *
     *  其中比较特殊的是 exist 子语句，但最终也是拼出一个条件
     *
     *  部门与子部权限，字段组【name，nickName，existxx】
     *  结果类似：
     *  t.name like 10010% or
     *  t.nickName like 10010% or
     *  exist (select 1 ... where xx like code%)
     *
     *
     * */

    /**
     * @param tableAlias 表别名
     * @param fields     字段列表
     * @param codes      条件-部门编码
     * @param fuzzy      通过模糊查子部门  = code / like code%
     * @return
     */
    private static String buildByCodes(String tableAlias, String[] fields, String[] codes, boolean fuzzy) {

        if (codes == null || codes.length == 0) {
            return "";
        }

        List<String> conditions = new ArrayList<>();

        for (String field : fields) {
            String cond;
            // 检查是否为特殊字段（存在于 sqlMap 中）
            if (FieldGroup.isSpecialField(field)) {
                cond = buildExist(tableAlias, field, codes, fuzzy);
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

    /**
     * 构建 exist 子查询（部门范围模式）
     *
     * @param tableAlias 表别名
     * @param field      字段代码
     * @param codes      条件-部门编码数组/或者是用户名
     * @param fuzzy      是否模糊匹配
     * @return SQL 条件
     */
    private static String buildExist(String tableAlias, String field, String[] codes, boolean fuzzy) {
        // 从 FieldGroup 获取 SQL 模板
        String sqlTemplate = FieldGroup.getSqlTemplate(field);
        if (StringUtils.isBlank(sqlTemplate)) {
            return "";
        }

        List<String> parts = new ArrayList<>();

        for (String code : codes) {
            if (StringUtils.isBlank(code)) {
                continue;
            }
            parts.add(
                    fuzzy ? StringUtils.format("{} like '{}%'", field, code)
                            : StringUtils.format("{} = '{}'", field, code)
            );
        }

        if (parts.isEmpty()) {
            return "";
        }

        String conditionPart = joinOr(parts);
        return StringUtils.format(sqlTemplate, tableAlias, conditionPart);
    }

    /* ========================== 仅本人 ========================== */
    private static String buildSelf(SysUser user,
                                    String tableAlias,
                                    String[] fields) {

        String userName = user.getUserName();
        if (StringUtils.isBlank(userName)) {
            return "";
        }

        List<String> conditions = new ArrayList<>();

        for (String field : fields) {
            String cond;
            // 检查是否为特殊字段（存在于 sqlMap 中）
            if (FieldGroup.isSpecialField(field)) {
                // 特殊字段使用 exist 查询（本人数据模式）
                cond = buildExist(tableAlias, field, new String[]{userName}, false);
            } else {
                // 普通字段直接匹配 userName
                cond = StringUtils.format(
                        "{} = '{}'",
                        StringUtils.format("{}.{}", tableAlias, field),
                        userName
                );
            }

            if (StringUtils.isNotBlank(cond)) {
                conditions.add(cond);
            }
        }

        return joinOr(conditions);
    }

    /* ========================== 工具方法 ========================== */

    /**
     * 过滤部门编码，移除那些是其他编码前缀的子编码
     * 例如：[1001,10011,10012,1002] -> [1001,1002]
     * 因为1001%已经包括了10011%和10012%
     *
     * @param codes 原始部门编码数组
     * @return 过滤后的部门编码数组
     */
    private static String[] filterPrefixCodes(String[] codes) {
        if (codes == null || codes.length == 0) {
            return codes;
        }

        List<String> result = new ArrayList<>();
        for (String code : codes) {
            if (StringUtils.isBlank(code)) {
                continue;
            }
            // 检查当前编码是否是其他编码的子编码（即是否有其他编码是它的前缀）
            boolean isSubCode = false;
            for (String otherCode : codes) {
                if (StringUtils.isBlank(otherCode) || code.equals(otherCode)) {
                    continue;
                }
                // 如果code以otherCode开头，说明otherCode是code的前缀，code是子编码，应该移除
                if (code.startsWith(otherCode)) {
                    isSubCode = true;
                    break;
                }
            }
            // 只保留不是任何其他编码子编码的编码
            if (!isSubCode) {
                result.add(code);
            }
        }

        return result.toArray(new String[0]);
    }

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
