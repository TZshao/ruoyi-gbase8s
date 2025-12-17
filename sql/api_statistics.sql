-- ----------------------------
-- API接口统计记录表
-- ----------------------------
drop table if exists api_log;
create table api_log (
    id               SERIAL8          not null  primary key,
    api_key          varchar(255)     not null,              -- 接口标识（格式：HTTP方法 + URI，如 "GET /flow/instance/list"）
    http_method      varchar(10)      default '',            -- HTTP方法（GET、POST等）
    request_uri      varchar(500)     default '',            -- 请求URI
    response_time    bigint           default 0,              -- 响应时长（毫秒）
    status           char(1)          default '0',            -- 状态（0正常 1异常）
    error_msg        varchar(2000)    default null,           -- 错误消息
    create_time      DATETIME YEAR TO SECOND                  -- 调用时间
);

comment on column api_log.id is '主键';
comment on column api_log.api_key is '接口标识（格式：HTTP方法 + URI）';
comment on column api_log.http_method is 'HTTP方法（GET、POST等）';
comment on column api_log.request_uri is '请求URI';
comment on column api_log.response_time is '响应时长（毫秒）';
comment on column api_log.status is '状态（0正常 1异常）';
comment on column api_log.error_msg is '错误消息';
comment on column api_log.create_time is '调用时间';

-- 创建索引优化查询性能
create index idx_api_key_time on api_log(api_key, create_time desc);
create index idx_api_key on api_log(api_key);

