-- ----------------------------
-- 流程定义表
-- ----------------------------
drop table if exists flow_def;
create table flow_def (
                            id               SERIAL8          not null  primary key   ,
                            flow_code        varchar(64)      default ''                 ,
                            flow_name        varchar(128)     default ''                 ,
                            form_schema      varchar(2000)    default ''                 ,
                            version          integer          default 1                  ,
                            publish          boolean          default false              ,
                            is_del           boolean          default false              ,
                            create_by        varchar(64)      default ''                 ,
                            create_time      DATETIME YEAR TO SECOND                     ,
                            update_by        varchar(64)      default ''                 ,
                            update_time      DATETIME YEAR TO SECOND                     ,
                            remark           varchar(500)     default null
) ;
comment on column flow_def.id is '主键';
comment on column flow_def.flow_code is '流程编号';
comment on column flow_def.flow_name is '流程名称';
comment on column flow_def.form_schema is '表单Schema(JSON)';
comment on column flow_def.version is '版本号';
comment on column flow_def.publish is '是否已发布';
comment on column flow_def.is_del is '是否删除';
comment on column flow_def.create_by is '创建者';
comment on column flow_def.create_time is '创建时间';
comment on column flow_def.update_by is '更新者';
comment on column flow_def.update_time is '更新时间';
comment on column flow_def.remark is '备注';


-- ----------------------------
-- 流程步骤定义表
-- ----------------------------
drop table if exists flow_step_def;
create table flow_step_def (
                            id               SERIAL8          not null  primary key   ,
                            flow_id          bigint           not null                  ,
                            step_code        varchar(64)      default ''                 ,
                            step_name        varchar(128)     default ''                 ,
                            next_on_pass     varchar(64)      default ''                 ,
                            next_on_reject   varchar(64)      default ''                 ,
                            handler_type     varchar(50)      default ''                 ,
                            handler_value    varchar(128)     default ''                 ,
                            event_on_pass    varchar(255)     default ''                 ,
                            event_on_reject  varchar(255)     default ''                 ,
                            order_num        integer          default 0                  ,
                            create_by        varchar(64)      default ''                 ,
                            create_time      DATETIME YEAR TO SECOND                     ,
                            update_by        varchar(64)      default ''                 ,
                            update_time      DATETIME YEAR TO SECOND                     ,
                            remark           varchar(500)     default null,
                            UNIQUE (flow_id, step_code)
) ;
comment on column flow_step_def.id is '主键';
comment on column flow_step_def.flow_id is '流程ID';
comment on column flow_step_def.step_code is '步骤编码';
comment on column flow_step_def.step_name is '步骤名称';
comment on column flow_step_def.next_on_pass is '通过后跳转编码';
comment on column flow_step_def.next_on_reject is '拒绝后跳转编码';
comment on column flow_step_def.handler_type is '审批人类型';
comment on column flow_step_def.handler_value is '审批人取值';
comment on column flow_step_def.event_on_pass is '通过事件';
comment on column flow_step_def.event_on_reject is '拒绝事件';
comment on column flow_step_def.order_num is '排序';
comment on column flow_step_def.create_by is '创建者';
comment on column flow_step_def.create_time is '创建时间';
comment on column flow_step_def.update_by is '更新者';
comment on column flow_step_def.update_time is '更新时间';
comment on column flow_step_def.remark is '备注';


-- ----------------------------
-- 流程实例表
-- ----------------------------
drop table if exists flow_instance;
create table flow_instance (
                            id                 SERIAL8          not null  primary key   ,
                            ref_id             bigint           ,
                            ref_module             varchar(64)           ,
                            flow_id            bigint           not null                  ,
                            flow_code          varchar(64)      default ''                 ,
                            flow_version       integer          default null               ,
                            applicant_id       bigint           default null              ,
                            status             varchar(20)      default 'RUNNING'         ,
                            current_step_code  varchar(64)      default ''                 ,
                            current_step_handler varchar(64)      default ''                 ,
                            form_data          varchar(2000)    default ''                 ,
                            create_by          varchar(64)      default ''                 ,
                            create_time        DATETIME YEAR TO SECOND                     ,
                            update_by          varchar(64)      default ''                 ,
                            update_time        DATETIME YEAR TO SECOND                     ,
                            remark             varchar(500)     default null
) ;
comment on column flow_instance.id is '主键';
comment on column flow_instance.flow_id is '流程ID';
comment on column flow_instance.ref_id is '业务表的ID，用于筛选';
comment on column flow_instance.ref_module is '业务模块名，用于筛选';
comment on column flow_instance.flow_code is '流程编号（冗余）';
comment on column flow_instance.flow_version is '流程版本（冗余）';
comment on column flow_instance.applicant_id is '申请人用户ID';
comment on column flow_instance.status is '状态 RUNNING/PASS/REJECT/CANCEL';
comment on column flow_instance.current_step_code is '当前步骤编码';
comment on column flow_instance.current_step_handler is '当前步骤审批人';
comment on column flow_instance.form_data is '业务表单数据(JSON)';
comment on column flow_instance.create_by is '创建者';
comment on column flow_instance.create_time is '创建时间';
comment on column flow_instance.update_by is '更新者';
comment on column flow_instance.update_time is '更新时间';
comment on column flow_instance.remark is '备注';


-- ----------------------------
-- 审批动作记录表
-- ----------------------------
drop table if exists flow_action;
create table flow_action (
                            id               SERIAL8          not null  primary key   ,
                            instance_id      bigint           not null                  ,
                            step_code        varchar(64)      default ''                 ,
                            action           varchar(20)      default ''                 ,
                            comment          varchar(500)     default null              ,
                            operator_id      bigint           default null              ,
                            action_time      DATETIME YEAR TO SECOND
) ;
comment on column flow_action.id is '主键';
comment on column flow_action.instance_id is '实例ID';
comment on column flow_action.step_code is '动作发生的步骤编码';
comment on column flow_action.action is 'PASS/REJECT/CANCEL';
comment on column flow_action.comment is '审批意见';
comment on column flow_action.operator_id is '审批人';
comment on column flow_action.action_time is '动作时间';
