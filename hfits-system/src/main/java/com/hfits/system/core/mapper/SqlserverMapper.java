package com.hfits.system.core.mapper;

import com.hfits.common.annotation.DataSource;
import com.hfits.common.enums.DataSourceType;
import com.hfits.system.core.domain.TableColumn;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@DataSource(DataSourceType.SQLSERVER)
public interface SqlserverMapper {

    List<String> tables();

    List<TableColumn> table(String tableName);

    /**
     * 查询源表全部数据
     *
     * @param tableName 表名
     * @return 数据列表
     */
    List<Map<String, Object>> selectTableData(@Param("tableName") String tableName);

    /**
     * 查询源表部分数据用于验证
     *
     * @param tableName 表名
     * @param limit 限制条数
     * @return 数据列表
     */
    List<Map<String, Object>> selectTableDataLimit(@Param("tableName") String tableName, @Param("limit") Integer limit);

}
