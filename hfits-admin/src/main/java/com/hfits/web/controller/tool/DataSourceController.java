package com.hfits.web.controller.tool;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hfits.common.annotation.DataSource;
import com.hfits.common.core.controller.BaseController;
import com.hfits.common.core.domain.Resp;
import com.hfits.common.enums.DataSourceType;
import com.hfits.system.core.domain.TableColumn;
import com.hfits.system.core.mapper.SqlserverMapper;

@RestController
@RequestMapping("/dataSource")
@DataSource(value = DataSourceType.SQLSERVER)
public class DataSourceController extends BaseController {

    @Autowired
    private SqlserverMapper sqlserverMapper;

    /**
     * 获取表列表
     */
    @GetMapping("/tables")
    public Resp<List<String>> getTables() {
        List<String> tables = sqlserverMapper.tables();
        return successR(tables);
    }

    /**
     * 获取表结构
     */
    @GetMapping("/table/{tableName}")
    public Resp<List<TableColumn>> getTable(@PathVariable String tableName) {
        List<TableColumn> columns = sqlserverMapper.table(tableName);
        return successR(columns);
    }
}
