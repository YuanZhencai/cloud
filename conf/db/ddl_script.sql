--ddl_script.sql

create table t_affiliated_warehouse (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  business_code             varchar(40),
  company_code              varchar(40),
  warehouse_code            varchar(40),
  warehouse_name            varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_affiliated_warehouse primary key (id))
;

create table t_area (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  storage_type_id           varchar(40),
  area_code                 varchar(40),
  name_key                  varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_area primary key (id))
;

create table base_model (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  version                   bigint not null,
  constraint pk_base_model primary key (id))
;

create table t_base_unit (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  unit_type                 varchar(40),
  base_unit                 boolean,
  unit_code                 varchar(40),
  name_key                  varchar(40),
  rate                      decimal(18,4),
  version                   bigint not null,
  constraint pk_t_base_unit primary key (id))
;

create table t_batch (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  parent_id                 varchar(40),
  material_id               varchar(40),
  batch_no                  varchar(40),
  version                   bigint not null,
  constraint pk_t_batch primary key (id))
;

create table t_bin (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  area_id                   varchar(40),
  bin_code                  varchar(40),
  name_key                  varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_bin primary key (id))
;

create table t_bin_capacity (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  bin_id                    varchar(40),
  capacity_type             varchar(40),
  capacity                  decimal(18,4),
  version                   bigint not null,
  constraint pk_t_bin_capacity primary key (id))
;

create table t_business (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_code             varchar(40),
  name_key                  varchar(40),
  version                   bigint not null,
  constraint pk_t_business primary key (id))
;

create table t_business_user (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  user_id                   varchar(40),
  business_id               varchar(40),
  employee_id               varchar(40),
  effective_since           timestamp,
  locked                    boolean,
  version                   bigint not null,
  constraint pk_t_business_user primary key (id))
;

create table t_business_user_role (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_user_id          varchar(40),
  role_id                   varchar(40),
  version                   bigint not null,
  constraint pk_t_business_user_role primary key (id))
;

create table t_code (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  code_cat_id               varchar(40),
  code_key                  varchar(40),
  cat_key                   varchar(40),
  name_key                  varchar(40),
  sys_code                  boolean,
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_code primary key (id))
;

create table t_code_cat (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_id               varchar(40),
  company_id                varchar(40),
  warehouse_id              varchar(40),
  cat_level                 varchar(40),
  cat_key                   varchar(40),
  name_key                  varchar(40),
  sys_code_cat              boolean,
  version                   bigint not null,
  constraint pk_t_code_cat primary key (id))
;

create table t_company (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_id               varchar(40),
  company_code              varchar(40),
  name_key                  varchar(40),
  version                   bigint not null,
  constraint pk_t_company primary key (id))
;

create table t_config (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_id               varchar(40),
  company_id                varchar(40),
  warehouse_id              varchar(40),
  config_level              varchar(40),
  config_key                varchar(40),
  config_value              varchar(40),
  version                   bigint not null,
  constraint pk_t_config primary key (id))
;

create table t_employee (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  company_id                varchar(40),
  employee_type_id          varchar(40),
  employee_code             varchar(40),
  employee_name             varchar(40),
  version                   bigint not null,
  constraint pk_t_employee primary key (id))
;

create table t_employee_type (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  company_id                varchar(40),
  type_code                 varchar(40),
  name_key                  varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_employee_type primary key (id))
;

create table t_employee_warehouse (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  employee_id               varchar(40),
  warehouse_id              varchar(40),
  version                   bigint not null,
  constraint pk_t_employee_warehouse primary key (id))
;

create table t_execution (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  plan_item_id              varchar(40),
  plan_item_detail_id       varchar(40),
  execution_type            varchar(40),
  execution_subtype         varchar(40),
  reverse                   boolean,
  reversed                  boolean,
  ref_execution_id          varchar(40),
  executed_by               varchar(40),
  executed_at               timestamp,
  executed_qty              decimal(18,4),
  material_id               varchar(40),
  from_uom_id               varchar(40),
  from_area_id              varchar(40),
  from_bin_id               varchar(40),
  from_pallet_type_id       varchar(40),
  from_pallet_id            varchar(40),
  to_uom_id                 varchar(40),
  to_area_id                varchar(40),
  to_bin_id                 varchar(40),
  to_pallet_type_id         varchar(40),
  to_pallet_id              varchar(40),
  seq_no                    bigint,
  version                   bigint not null,
  constraint pk_t_execution primary key (id))
;

create table t_ext_schema (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_id               varchar(40),
  company_id                varchar(40),
  warehouse_id              varchar(40),
  schema_level              varchar(40),
  schema_key                varchar(40),
  schema_value              varchar(40),
  version                   bigint not null,
  constraint pk_t_ext_schema primary key (id))
;

create table t_external_material (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_id               varchar(40),
  company_id                varchar(40),
  warehouse_id              varchar(40),
  owner_code                varchar(40),
  owner_name                varchar(40),
  material_code             varchar(40),
  material_name             varchar(40),
  weight_unit_code          varchar(40),
  gross_weight              decimal(18,4),
  net_weight                decimal(18,4),
  volumn_unit_code          varchar(255),
  volumn                    decimal(18,4),
  version                   bigint not null,
  constraint pk_t_external_material primary key (id))
;

create table t_external_material_uom (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  external_material_id      varchar(40),
  material_code             varchar(40),
  uom_code                  varchar(40),
  uom_name                  varchar(40),
  base_uom                  boolean,
  qty_of_base_num           decimal(18,4),
  qty_of_base_denom         decimal(18,4),
  version                   bigint not null,
  constraint pk_t_external_material_uom primary key (id))
;

create table t_external_order (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_id               varchar(40),
  company_id                varchar(40),
  warehouse_id              varchar(40),
  order_type                varchar(40),
  source_type               varchar(40),
  external_order_no         varchar(40),
  contract_no               varchar(40),
  owner_code                varchar(40),
  owner_name                varchar(40),
  order_timestamp           timestamp,
  order_status              varchar(40),
  version                   bigint not null,
  constraint pk_t_external_order primary key (id))
;

create table t_external_order_item (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  external_order_id         varchar(40),
  external_material_id      varchar(40),
  external_material_uom_id  varchar(40),
  qty                       decimal(18,4),
  min_percent               decimal(18,4),
  max_percent               decimal(18,4),
  item_status               varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_external_order_item primary key (id))
;

create table t_inout_policy (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  item_policy_id            varchar(40),
  sort_no                   integer,
  in_out_type               varchar(40),
  version                   bigint not null,
  constraint pk_t_inout_policy primary key (id))
;

create table t_item_policy (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  order_item_id             varchar(40),
  sort_no                   integer,
  policy_type               varchar(40),
  policy_description        text,
  version                   bigint not null,
  constraint pk_t_item_policy primary key (id))
;

create table t_ldap (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  ip_address                varchar(40),
  admin_user                varchar(40),
  admin_password            varchar(40),
  version                   bigint not null,
  constraint pk_t_ldap primary key (id))
;

create table t_material (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  owner_id                  varchar(40),
  material_code             varchar(40),
  material_name             varchar(40),
  weight_unit_code          varchar(40),
  gross_weight              decimal(18,4),
  net_weight                decimal(18,4),
  volumn_unit_code          varchar(255),
  volumn                    decimal(18,4),
  source_type               varchar(40),
  version                   bigint not null,
  constraint pk_t_material primary key (id))
;

create table t_material_uom (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  material_id               varchar(40),
  uom_code                  varchar(40),
  base_uom                  boolean,
  qty_of_base_num           decimal(18,4),
  qty_of_base_denom         decimal(18,4),
  version                   bigint not null,
  constraint pk_t_material_uom primary key (id))
;

create table t_menu (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  menu_set_id               varchar(40),
  name_key                  varchar(40),
  uri                       varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_menu primary key (id))
;

create table t_menu_set (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  parent_id                 varchar(40),
  name_key                  varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_menu_set primary key (id))
;

create table t_name (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_id               varchar(40),
  company_id                varchar(40),
  warehouse_id              varchar(40),
  name_level                varchar(40),
  name_key                  varchar(40),
  language                  varchar(40),
  name_value                text,
  version                   bigint not null,
  constraint pk_t_name primary key (id))
;

create table t_order (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  order_type                varchar(40),
  source_type               varchar(40),
  external_order_no         varchar(40),
  internal_order_no         varchar(40),
  contract_no               varchar(40),
  order_timestamp           timestamp,
  order_status              varchar(40),
  version                   bigint not null,
  constraint pk_t_order primary key (id))
;

create table t_order_item (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  order_id                  varchar(40),
  external_order_id         varchar(40),
  external_order_item_id    varchar(40),
  material_id               varchar(40),
  material_uom_id           varchar(40),
  qty                       decimal(18,4),
  min_percent               decimal(18,4),
  max_percent               decimal(18,4),
  item_status               varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_order_item primary key (id))
;

create table t_owner (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  owner_code                varchar(40),
  owner_name                varchar(40),
  version                   bigint not null,
  constraint pk_t_owner primary key (id))
;

create table t_pallet (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  area_id                   varchar(40),
  bin_id                    varchar(40),
  pallet_type_id            varchar(40),
  serial_no                 varchar(40),
  in_use_since              timestamp,
  pallet_status             varchar(40),
  version                   bigint not null,
  constraint pk_t_pallet primary key (id))
;

create table t_pallet_capacity (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  pallet_type_id            varchar(40),
  pallet_id                 varchar(40),
  capacity_type             varchar(40),
  capacity                  decimal(18,4),
  version                   bigint not null,
  constraint pk_t_pallet_capacity primary key (id))
;

create table t_pallet_type (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  type_code                 varchar(40),
  name_key                  varchar(40),
  version                   bigint not null,
  constraint pk_t_pallet_type primary key (id))
;

create table t_permission (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  index                     integer,
  code                      varchar(40),
  name                      varchar(40),
  version                   bigint not null,
  constraint pk_t_permission primary key (id))
;

create table t_permission_template (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  code                      varchar(40),
  name                      varchar(40),
  summary                   text,
  version                   bigint not null,
  constraint pk_t_permission_template primary key (id))
;

create table t_plan (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  plan_type                 varchar(40),
  plan_subtype              varchar(40),
  order_id                  varchar(40),
  planned_timestamp         timestamp,
  seq_no                    bigint,
  assigned_to               varchar(40),
  plan_status               varchar(40),
  version                   bigint not null,
  constraint pk_t_plan primary key (id))
;

create table t_plan_item (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  plan_id                   varchar(40),
  plan_type                 varchar(40),
  plan_subtype              varchar(40),
  order_id                  varchar(40),
  order_item_id             varchar(40),
  material_id               varchar(40),
  material_uom_id           varchar(40),
  batch_id                  varchar(40),
  planned_execution_at      timestamp,
  palnned_qty               decimal(18,4),
  min_percent               decimal(18,4),
  max_percent               decimal(18,4),
  from_uom_id               varchar(40),
  from_area_id              varchar(40),
  from_bin_id               varchar(40),
  to_uom_id                 varchar(40),
  to_area_id                varchar(40),
  to_bin_id                 varchar(40),
  seq_no                    bigint,
  sort_no                   integer,
  assigned_to               varchar(40),
  item_status               varchar(40),
  version                   bigint not null,
  constraint pk_t_plan_item primary key (id))
;

create table t_plan_item_detail (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  plan_item_id              varchar(40),
  stock_id                  varchar(40),
  palnned_qty               decimal(18,4),
  from_uom_id               varchar(40),
  from_area_id              varchar(40),
  from_bin_id               varchar(40),
  to_uom_id                 varchar(40),
  to_area_id                varchar(40),
  to_bin_id                 varchar(40),
  assigned_to               varchar(40),
  detail_status             varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_plan_item_detail primary key (id))
;

create table t_role (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  name_key                  varchar(40),
  version                   bigint not null,
  constraint pk_t_role primary key (id))
;

create table t_role_menu (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  role_id                   varchar(40),
  menu_id                   varchar(40),
  version                   bigint not null,
  constraint pk_t_role_menu primary key (id))
;

create table t_role_permission (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  role_id                   varchar(40),
  summary                   text,
  version                   bigint not null,
  constraint pk_t_role_permission primary key (id))
;

create table t_stock (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  material_id               varchar(40),
  material_uom_id           varchar(40),
  batch_id                  varchar(40),
  qty                       decimal(18,4),
  pallet_type_id            varchar(40),
  pallet_id                 varchar(40),
  tracing_id                varchar(40),
  area_id                   varchar(40),
  bin_id                    varchar(40),
  received_at               timestamp,
  arrived_at                timestamp,
  issued_at                 timestamp,
  stock_status              varchar(40),
  version                   bigint not null,
  constraint pk_t_stock primary key (id))
;

create table t_stock_count (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  count_at                  timestamp,
  count_type                varchar(40),
  count_status              varchar(40),
  supervisor_id             varchar(40),
  seq_no                    bigint,
  version                   bigint not null,
  constraint pk_t_stock_count primary key (id))
;

create table t_stock_count_bin (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  stock_count_id            varchar(40),
  area_id                   varchar(40),
  bin_id                    varchar(40),
  count_status              varchar(40),
  version                   bigint not null,
  constraint pk_t_stock_count_bin primary key (id))
;

create table t_stock_count_item (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  stock_count_bin_id        varchar(40),
  material_id               varchar(40),
  material_uom_id           varchar(40),
  batch_id                  varchar(40),
  expected_qty              decimal(18,4),
  actual_qty                decimal(18,4),
  counted_at                timestamp,
  operator_id               varchar(40),
  version                   bigint not null,
  constraint pk_t_stock_count_item primary key (id))
;

create table t_stock_transaction (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  stock_id                  varchar(40),
  execution_id              varchar(40),
  transaction_code          varchar(40),
  old_uom_id                varchar(40),
  old_qty                   decimal(18,4),
  old_area_id               varchar(40),
  old_bin_id                varchar(40),
  old_arrived_at            timestamp,
  old_status                varchar(40),
  old_pallet_type_id        varchar(40),
  old_pallet_id             varchar(40),
  old_tracing_id            varchar(40),
  new_uom_id                varchar(40),
  new_qty                   decimal(18,4),
  new_area_id               varchar(40),
  new_bin_id                varchar(40),
  new_arrived_at            timestamp,
  new_status                varchar(40),
  new_pallet_type_id        varchar(40),
  new_pallet_id             varchar(40),
  new_tracing_id            varchar(40),
  transaction_at            timestamp,
  seq_no                    bigint,
  version                   bigint not null,
  constraint pk_t_stock_transaction primary key (id))
;

create table t_storage_type (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  name_key                  varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_storage_type primary key (id))
;

create table t_sys_log (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_id               varchar(40),
  company_id                varchar(40),
  warehouse_id              varchar(40),
  employee_id               varchar(40),
  user_id                   varchar(40),
  log_level                 varchar(40),
  log_timestamp             timestamp,
  log_content               text,
  version                   bigint not null,
  constraint pk_t_sys_log primary key (id))
;

create table t_timing_policy (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  item_policy_id            varchar(40),
  sort_no                   integer,
  material_id               varchar(40),
  storage_type_id           varchar(40),
  min_hours                 integer,
  max_hours                 integer,
  version                   bigint not null,
  constraint pk_t_timing_policy primary key (id))
;

create table t_uom_capacity_point (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  material_uom_id           varchar(40),
  capacity_type             varchar(40),
  capacity_point            decimal(18,4),
  version                   bigint not null,
  constraint pk_t_uom_capacity_point primary key (id))
;

create table t_user (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  name                      varchar(40),
  auth_method               varchar(40),
  password_hash             varchar(40),
  ldap_id                   varchar(40),
  ldap_username             varchar(40),
  employee_id               varchar(40),
  effective_since           timestamp,
  locked                    boolean,
  version                   bigint not null,
  constraint pk_t_user primary key (id))
;

create table t_user_perference (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  user_id                   varchar(40),
  pref_key                  varchar(40),
  pref_value                text,
  version                   bigint not null,
  constraint pk_t_user_perference primary key (id))
;

create table t_user_warehouse (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  user_id                   varchar(40),
  warehouse_id              varchar(40),
  version                   bigint not null,
  constraint pk_t_user_warehouse primary key (id))
;

create table t_warehouse (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  company_id                varchar(40),
  warehouse_code            varchar(40),
  name_key                  varchar(40),
  sort_no                   integer,
  version                   bigint not null,
  constraint pk_t_warehouse primary key (id))
;

create table t_warehouse_policy (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  business_id               varchar(40),
  company_id                varchar(40),
  warehouse_id              varchar(40),
  storage_type_id           varchar(40),
  area_id                   varchar(40),
  bin_id                    varchar(40),
  sort_no                   integer,
  policy_type               varchar(40),
  policy_description        text,
  version                   bigint not null,
  constraint pk_t_warehouse_policy primary key (id))
;

create table t_warehouse_sequence (
  id                        varchar(40) not null,
  remarks                   text,
  ext                       text,
  created_by                varchar(40),
  created_at                timestamp,
  updated_by                varchar(40),
  updated_at                timestamp,
  deleted                   boolean,
  schema_code               varchar(40),
  warehouse_id              varchar(40),
  sequence_key              varchar(40),
  start_no                  bigint,
  current_no                bigint,
  version                   bigint not null,
  constraint pk_t_warehouse_sequence primary key (id))
;

alter table t_affiliated_warehouse add constraint fk_t_affiliated_warehouse_ware_1 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_affiliated_warehouse_ware_1 on t_affiliated_warehouse (warehouse_id);
alter table t_area add constraint fk_t_area_warehouse_2 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_area_warehouse_2 on t_area (warehouse_id);
alter table t_area add constraint fk_t_area_storageType_3 foreign key (storage_type_id) references t_storage_type (id);
create index ix_t_area_storageType_3 on t_area (storage_type_id);
alter table t_batch add constraint fk_t_batch_parent_4 foreign key (parent_id) references t_batch (id);
create index ix_t_batch_parent_4 on t_batch (parent_id);
alter table t_batch add constraint fk_t_batch_material_5 foreign key (material_id) references t_material (id);
create index ix_t_batch_material_5 on t_batch (material_id);
alter table t_bin add constraint fk_t_bin_area_6 foreign key (area_id) references t_area (id);
create index ix_t_bin_area_6 on t_bin (area_id);
alter table t_bin_capacity add constraint fk_t_bin_capacity_bin_7 foreign key (bin_id) references t_bin (id);
create index ix_t_bin_capacity_bin_7 on t_bin_capacity (bin_id);
alter table t_business_user add constraint fk_t_business_user_user_8 foreign key (user_id) references t_user (id);
create index ix_t_business_user_user_8 on t_business_user (user_id);
alter table t_business_user add constraint fk_t_business_user_business_9 foreign key (business_id) references t_business (id);
create index ix_t_business_user_business_9 on t_business_user (business_id);
alter table t_business_user add constraint fk_t_business_user_employee_10 foreign key (employee_id) references t_employee (id);
create index ix_t_business_user_employee_10 on t_business_user (employee_id);
alter table t_business_user_role add constraint fk_t_business_user_role_busin_11 foreign key (business_user_id) references t_business_user (id);
create index ix_t_business_user_role_busin_11 on t_business_user_role (business_user_id);
alter table t_business_user_role add constraint fk_t_business_user_role_role_12 foreign key (role_id) references t_role (id);
create index ix_t_business_user_role_role_12 on t_business_user_role (role_id);
alter table t_code add constraint fk_t_code_codeCat_13 foreign key (code_cat_id) references t_code_cat (id);
create index ix_t_code_codeCat_13 on t_code (code_cat_id);
alter table t_code_cat add constraint fk_t_code_cat_business_14 foreign key (business_id) references t_business (id);
create index ix_t_code_cat_business_14 on t_code_cat (business_id);
alter table t_code_cat add constraint fk_t_code_cat_company_15 foreign key (company_id) references t_company (id);
create index ix_t_code_cat_company_15 on t_code_cat (company_id);
alter table t_code_cat add constraint fk_t_code_cat_warehouse_16 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_code_cat_warehouse_16 on t_code_cat (warehouse_id);
alter table t_company add constraint fk_t_company_business_17 foreign key (business_id) references t_business (id);
create index ix_t_company_business_17 on t_company (business_id);
alter table t_config add constraint fk_t_config_business_18 foreign key (business_id) references t_business (id);
create index ix_t_config_business_18 on t_config (business_id);
alter table t_config add constraint fk_t_config_company_19 foreign key (company_id) references t_company (id);
create index ix_t_config_company_19 on t_config (company_id);
alter table t_config add constraint fk_t_config_warehouse_20 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_config_warehouse_20 on t_config (warehouse_id);
alter table t_employee add constraint fk_t_employee_company_21 foreign key (company_id) references t_company (id);
create index ix_t_employee_company_21 on t_employee (company_id);
alter table t_employee add constraint fk_t_employee_employeeType_22 foreign key (employee_type_id) references t_employee_type (id);
create index ix_t_employee_employeeType_22 on t_employee (employee_type_id);
alter table t_employee_type add constraint fk_t_employee_type_company_23 foreign key (company_id) references t_company (id);
create index ix_t_employee_type_company_23 on t_employee_type (company_id);
alter table t_employee_warehouse add constraint fk_t_employee_warehouse_emplo_24 foreign key (employee_id) references t_employee (id);
create index ix_t_employee_warehouse_emplo_24 on t_employee_warehouse (employee_id);
alter table t_employee_warehouse add constraint fk_t_employee_warehouse_wareh_25 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_employee_warehouse_wareh_25 on t_employee_warehouse (warehouse_id);
alter table t_execution add constraint fk_t_execution_planItem_26 foreign key (plan_item_id) references t_plan_item (id);
create index ix_t_execution_planItem_26 on t_execution (plan_item_id);
alter table t_execution add constraint fk_t_execution_planItemDetail_27 foreign key (plan_item_detail_id) references t_plan_item_detail (id);
create index ix_t_execution_planItemDetail_27 on t_execution (plan_item_detail_id);
alter table t_execution add constraint fk_t_execution_refExecution_28 foreign key (ref_execution_id) references t_execution (id);
create index ix_t_execution_refExecution_28 on t_execution (ref_execution_id);
alter table t_execution add constraint fk_t_execution_executedBy_29 foreign key (executed_by) references t_user (id);
create index ix_t_execution_executedBy_29 on t_execution (executed_by);
alter table t_execution add constraint fk_t_execution_material_30 foreign key (material_id) references t_material (id);
create index ix_t_execution_material_30 on t_execution (material_id);
alter table t_execution add constraint fk_t_execution_fromMaterialUo_31 foreign key (from_uom_id) references t_material_uom (id);
create index ix_t_execution_fromMaterialUo_31 on t_execution (from_uom_id);
alter table t_execution add constraint fk_t_execution_fromArea_32 foreign key (from_area_id) references t_area (id);
create index ix_t_execution_fromArea_32 on t_execution (from_area_id);
alter table t_execution add constraint fk_t_execution_fromBin_33 foreign key (from_bin_id) references t_bin (id);
create index ix_t_execution_fromBin_33 on t_execution (from_bin_id);
alter table t_execution add constraint fk_t_execution_fromPalletType_34 foreign key (from_pallet_type_id) references t_pallet_type (id);
create index ix_t_execution_fromPalletType_34 on t_execution (from_pallet_type_id);
alter table t_execution add constraint fk_t_execution_fromPallet_35 foreign key (from_pallet_id) references t_pallet (id);
create index ix_t_execution_fromPallet_35 on t_execution (from_pallet_id);
alter table t_execution add constraint fk_t_execution_toMaterialUom_36 foreign key (to_uom_id) references t_material_uom (id);
create index ix_t_execution_toMaterialUom_36 on t_execution (to_uom_id);
alter table t_execution add constraint fk_t_execution_toArea_37 foreign key (to_area_id) references t_area (id);
create index ix_t_execution_toArea_37 on t_execution (to_area_id);
alter table t_execution add constraint fk_t_execution_toBin_38 foreign key (to_bin_id) references t_bin (id);
create index ix_t_execution_toBin_38 on t_execution (to_bin_id);
alter table t_execution add constraint fk_t_execution_toPalletType_39 foreign key (to_pallet_type_id) references t_pallet_type (id);
create index ix_t_execution_toPalletType_39 on t_execution (to_pallet_type_id);
alter table t_execution add constraint fk_t_execution_toPallet_40 foreign key (to_pallet_id) references t_pallet (id);
create index ix_t_execution_toPallet_40 on t_execution (to_pallet_id);
alter table t_ext_schema add constraint fk_t_ext_schema_business_41 foreign key (business_id) references t_business (id);
create index ix_t_ext_schema_business_41 on t_ext_schema (business_id);
alter table t_ext_schema add constraint fk_t_ext_schema_company_42 foreign key (company_id) references t_company (id);
create index ix_t_ext_schema_company_42 on t_ext_schema (company_id);
alter table t_ext_schema add constraint fk_t_ext_schema_warehouse_43 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_ext_schema_warehouse_43 on t_ext_schema (warehouse_id);
alter table t_external_material add constraint fk_t_external_material_busine_44 foreign key (business_id) references t_business (id);
create index ix_t_external_material_busine_44 on t_external_material (business_id);
alter table t_external_material add constraint fk_t_external_material_compan_45 foreign key (company_id) references t_company (id);
create index ix_t_external_material_compan_45 on t_external_material (company_id);
alter table t_external_material add constraint fk_t_external_material_wareho_46 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_external_material_wareho_46 on t_external_material (warehouse_id);
alter table t_external_material_uom add constraint fk_t_external_material_uom_ex_47 foreign key (external_material_id) references t_external_material (id);
create index ix_t_external_material_uom_ex_47 on t_external_material_uom (external_material_id);
alter table t_external_order add constraint fk_t_external_order_business_48 foreign key (business_id) references t_business (id);
create index ix_t_external_order_business_48 on t_external_order (business_id);
alter table t_external_order add constraint fk_t_external_order_company_49 foreign key (company_id) references t_company (id);
create index ix_t_external_order_company_49 on t_external_order (company_id);
alter table t_external_order add constraint fk_t_external_order_warehouse_50 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_external_order_warehouse_50 on t_external_order (warehouse_id);
alter table t_external_order_item add constraint fk_t_external_order_item_exte_51 foreign key (external_order_id) references t_external_order (id);
create index ix_t_external_order_item_exte_51 on t_external_order_item (external_order_id);
alter table t_external_order_item add constraint fk_t_external_order_item_exte_52 foreign key (external_material_id) references t_external_material (id);
create index ix_t_external_order_item_exte_52 on t_external_order_item (external_material_id);
alter table t_external_order_item add constraint fk_t_external_order_item_exte_53 foreign key (external_material_uom_id) references t_external_material_uom (id);
create index ix_t_external_order_item_exte_53 on t_external_order_item (external_material_uom_id);
alter table t_inout_policy add constraint fk_t_inout_policy_itemPolicy_54 foreign key (item_policy_id) references t_item_policy (id);
create index ix_t_inout_policy_itemPolicy_54 on t_inout_policy (item_policy_id);
alter table t_item_policy add constraint fk_t_item_policy_orderItem_55 foreign key (order_item_id) references t_order_item (id);
create index ix_t_item_policy_orderItem_55 on t_item_policy (order_item_id);
alter table t_material add constraint fk_t_material_owner_56 foreign key (owner_id) references t_owner (id);
create index ix_t_material_owner_56 on t_material (owner_id);
alter table t_material_uom add constraint fk_t_material_uom_warehouse_57 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_material_uom_warehouse_57 on t_material_uom (warehouse_id);
alter table t_material_uom add constraint fk_t_material_uom_material_58 foreign key (material_id) references t_material (id);
create index ix_t_material_uom_material_58 on t_material_uom (material_id);
alter table t_menu add constraint fk_t_menu_menuSet_59 foreign key (menu_set_id) references t_menu_set (id);
create index ix_t_menu_menuSet_59 on t_menu (menu_set_id);
alter table t_menu_set add constraint fk_t_menu_set_parent_60 foreign key (parent_id) references t_menu_set (id);
create index ix_t_menu_set_parent_60 on t_menu_set (parent_id);
alter table t_name add constraint fk_t_name_business_61 foreign key (business_id) references t_business (id);
create index ix_t_name_business_61 on t_name (business_id);
alter table t_name add constraint fk_t_name_company_62 foreign key (company_id) references t_company (id);
create index ix_t_name_company_62 on t_name (company_id);
alter table t_name add constraint fk_t_name_warehouse_63 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_name_warehouse_63 on t_name (warehouse_id);
alter table t_order add constraint fk_t_order_warehouse_64 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_order_warehouse_64 on t_order (warehouse_id);
alter table t_order_item add constraint fk_t_order_item_order_65 foreign key (order_id) references t_order (id);
create index ix_t_order_item_order_65 on t_order_item (order_id);
alter table t_order_item add constraint fk_t_order_item_externalOrder_66 foreign key (external_order_id) references t_external_order (id);
create index ix_t_order_item_externalOrder_66 on t_order_item (external_order_id);
alter table t_order_item add constraint fk_t_order_item_externalOrder_67 foreign key (external_order_item_id) references t_external_order_item (id);
create index ix_t_order_item_externalOrder_67 on t_order_item (external_order_item_id);
alter table t_order_item add constraint fk_t_order_item_material_68 foreign key (material_id) references t_material (id);
create index ix_t_order_item_material_68 on t_order_item (material_id);
alter table t_order_item add constraint fk_t_order_item_materialUom_69 foreign key (material_uom_id) references t_material_uom (id);
create index ix_t_order_item_materialUom_69 on t_order_item (material_uom_id);
alter table t_owner add constraint fk_t_owner_warehouse_70 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_owner_warehouse_70 on t_owner (warehouse_id);
alter table t_pallet add constraint fk_t_pallet_warehouse_71 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_pallet_warehouse_71 on t_pallet (warehouse_id);
alter table t_pallet add constraint fk_t_pallet_area_72 foreign key (area_id) references t_area (id);
create index ix_t_pallet_area_72 on t_pallet (area_id);
alter table t_pallet add constraint fk_t_pallet_bin_73 foreign key (bin_id) references t_bin (id);
create index ix_t_pallet_bin_73 on t_pallet (bin_id);
alter table t_pallet add constraint fk_t_pallet_palletType_74 foreign key (pallet_type_id) references t_pallet_type (id);
create index ix_t_pallet_palletType_74 on t_pallet (pallet_type_id);
alter table t_pallet_capacity add constraint fk_t_pallet_capacity_palletTy_75 foreign key (pallet_type_id) references t_pallet_type (id);
create index ix_t_pallet_capacity_palletTy_75 on t_pallet_capacity (pallet_type_id);
alter table t_pallet_capacity add constraint fk_t_pallet_capacity_pallet_76 foreign key (pallet_id) references t_pallet (id);
create index ix_t_pallet_capacity_pallet_76 on t_pallet_capacity (pallet_id);
alter table t_pallet_type add constraint fk_t_pallet_type_warehouse_77 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_pallet_type_warehouse_77 on t_pallet_type (warehouse_id);
alter table t_plan add constraint fk_t_plan_warehouse_78 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_plan_warehouse_78 on t_plan (warehouse_id);
alter table t_plan add constraint fk_t_plan_order_79 foreign key (order_id) references t_order (id);
create index ix_t_plan_order_79 on t_plan (order_id);
alter table t_plan add constraint fk_t_plan_assignedTo_80 foreign key (assigned_to) references t_employee (id);
create index ix_t_plan_assignedTo_80 on t_plan (assigned_to);
alter table t_plan_item add constraint fk_t_plan_item_plan_81 foreign key (plan_id) references t_plan (id);
create index ix_t_plan_item_plan_81 on t_plan_item (plan_id);
alter table t_plan_item add constraint fk_t_plan_item_order_82 foreign key (order_id) references t_order (id);
create index ix_t_plan_item_order_82 on t_plan_item (order_id);
alter table t_plan_item add constraint fk_t_plan_item_orderItem_83 foreign key (order_item_id) references t_order_item (id);
create index ix_t_plan_item_orderItem_83 on t_plan_item (order_item_id);
alter table t_plan_item add constraint fk_t_plan_item_material_84 foreign key (material_id) references t_material (id);
create index ix_t_plan_item_material_84 on t_plan_item (material_id);
alter table t_plan_item add constraint fk_t_plan_item_materialUom_85 foreign key (material_uom_id) references t_material_uom (id);
create index ix_t_plan_item_materialUom_85 on t_plan_item (material_uom_id);
alter table t_plan_item add constraint fk_t_plan_item_batch_86 foreign key (batch_id) references t_batch (id);
create index ix_t_plan_item_batch_86 on t_plan_item (batch_id);
alter table t_plan_item add constraint fk_t_plan_item_fromMaterialUo_87 foreign key (from_uom_id) references t_material_uom (id);
create index ix_t_plan_item_fromMaterialUo_87 on t_plan_item (from_uom_id);
alter table t_plan_item add constraint fk_t_plan_item_fromArea_88 foreign key (from_area_id) references t_area (id);
create index ix_t_plan_item_fromArea_88 on t_plan_item (from_area_id);
alter table t_plan_item add constraint fk_t_plan_item_fromBin_89 foreign key (from_bin_id) references t_bin (id);
create index ix_t_plan_item_fromBin_89 on t_plan_item (from_bin_id);
alter table t_plan_item add constraint fk_t_plan_item_toMaterialUom_90 foreign key (to_uom_id) references t_material_uom (id);
create index ix_t_plan_item_toMaterialUom_90 on t_plan_item (to_uom_id);
alter table t_plan_item add constraint fk_t_plan_item_toArea_91 foreign key (to_area_id) references t_area (id);
create index ix_t_plan_item_toArea_91 on t_plan_item (to_area_id);
alter table t_plan_item add constraint fk_t_plan_item_toBin_92 foreign key (to_bin_id) references t_bin (id);
create index ix_t_plan_item_toBin_92 on t_plan_item (to_bin_id);
alter table t_plan_item add constraint fk_t_plan_item_assignedTo_93 foreign key (assigned_to) references t_employee (id);
create index ix_t_plan_item_assignedTo_93 on t_plan_item (assigned_to);
alter table t_plan_item_detail add constraint fk_t_plan_item_detail_planIte_94 foreign key (plan_item_id) references t_plan_item (id);
create index ix_t_plan_item_detail_planIte_94 on t_plan_item_detail (plan_item_id);
alter table t_plan_item_detail add constraint fk_t_plan_item_detail_stock_95 foreign key (stock_id) references t_stock (id);
create index ix_t_plan_item_detail_stock_95 on t_plan_item_detail (stock_id);
alter table t_plan_item_detail add constraint fk_t_plan_item_detail_fromMat_96 foreign key (from_uom_id) references t_material_uom (id);
create index ix_t_plan_item_detail_fromMat_96 on t_plan_item_detail (from_uom_id);
alter table t_plan_item_detail add constraint fk_t_plan_item_detail_fromAre_97 foreign key (from_area_id) references t_area (id);
create index ix_t_plan_item_detail_fromAre_97 on t_plan_item_detail (from_area_id);
alter table t_plan_item_detail add constraint fk_t_plan_item_detail_fromBin_98 foreign key (from_bin_id) references t_bin (id);
create index ix_t_plan_item_detail_fromBin_98 on t_plan_item_detail (from_bin_id);
alter table t_plan_item_detail add constraint fk_t_plan_item_detail_toMater_99 foreign key (to_uom_id) references t_material_uom (id);
create index ix_t_plan_item_detail_toMater_99 on t_plan_item_detail (to_uom_id);
alter table t_plan_item_detail add constraint fk_t_plan_item_detail_toArea_100 foreign key (to_area_id) references t_area (id);
create index ix_t_plan_item_detail_toArea_100 on t_plan_item_detail (to_area_id);
alter table t_plan_item_detail add constraint fk_t_plan_item_detail_toBin_101 foreign key (to_bin_id) references t_bin (id);
create index ix_t_plan_item_detail_toBin_101 on t_plan_item_detail (to_bin_id);
alter table t_plan_item_detail add constraint fk_t_plan_item_detail_assign_102 foreign key (assigned_to) references t_employee (id);
create index ix_t_plan_item_detail_assign_102 on t_plan_item_detail (assigned_to);
alter table t_role_menu add constraint fk_t_role_menu_role_103 foreign key (role_id) references t_role (id);
create index ix_t_role_menu_role_103 on t_role_menu (role_id);
alter table t_role_menu add constraint fk_t_role_menu_menu_104 foreign key (menu_id) references t_menu (id);
create index ix_t_role_menu_menu_104 on t_role_menu (menu_id);
alter table t_role_permission add constraint fk_t_role_permission_role_105 foreign key (role_id) references t_role (id);
create index ix_t_role_permission_role_105 on t_role_permission (role_id);
alter table t_stock add constraint fk_t_stock_warehouse_106 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_stock_warehouse_106 on t_stock (warehouse_id);
alter table t_stock add constraint fk_t_stock_material_107 foreign key (material_id) references t_material (id);
create index ix_t_stock_material_107 on t_stock (material_id);
alter table t_stock add constraint fk_t_stock_materialUom_108 foreign key (material_uom_id) references t_material_uom (id);
create index ix_t_stock_materialUom_108 on t_stock (material_uom_id);
alter table t_stock add constraint fk_t_stock_batch_109 foreign key (batch_id) references t_batch (id);
create index ix_t_stock_batch_109 on t_stock (batch_id);
alter table t_stock add constraint fk_t_stock_palletType_110 foreign key (pallet_type_id) references t_pallet_type (id);
create index ix_t_stock_palletType_110 on t_stock (pallet_type_id);
alter table t_stock add constraint fk_t_stock_pallet_111 foreign key (pallet_id) references t_pallet (id);
create index ix_t_stock_pallet_111 on t_stock (pallet_id);
alter table t_stock add constraint fk_t_stock_area_112 foreign key (area_id) references t_area (id);
create index ix_t_stock_area_112 on t_stock (area_id);
alter table t_stock add constraint fk_t_stock_bin_113 foreign key (bin_id) references t_bin (id);
create index ix_t_stock_bin_113 on t_stock (bin_id);
alter table t_stock_count add constraint fk_t_stock_count_warehouse_114 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_stock_count_warehouse_114 on t_stock_count (warehouse_id);
alter table t_stock_count add constraint fk_t_stock_count_supervisor_115 foreign key (supervisor_id) references t_employee (id);
create index ix_t_stock_count_supervisor_115 on t_stock_count (supervisor_id);
alter table t_stock_count_bin add constraint fk_t_stock_count_bin_stockCo_116 foreign key (stock_count_id) references t_stock_count (id);
create index ix_t_stock_count_bin_stockCo_116 on t_stock_count_bin (stock_count_id);
alter table t_stock_count_bin add constraint fk_t_stock_count_bin_area_117 foreign key (area_id) references t_area (id);
create index ix_t_stock_count_bin_area_117 on t_stock_count_bin (area_id);
alter table t_stock_count_bin add constraint fk_t_stock_count_bin_bin_118 foreign key (bin_id) references t_bin (id);
create index ix_t_stock_count_bin_bin_118 on t_stock_count_bin (bin_id);
alter table t_stock_count_item add constraint fk_t_stock_count_item_stockC_119 foreign key (stock_count_bin_id) references t_stock_count_bin (id);
create index ix_t_stock_count_item_stockC_119 on t_stock_count_item (stock_count_bin_id);
alter table t_stock_count_item add constraint fk_t_stock_count_item_materi_120 foreign key (material_id) references t_material (id);
create index ix_t_stock_count_item_materi_120 on t_stock_count_item (material_id);
alter table t_stock_count_item add constraint fk_t_stock_count_item_materi_121 foreign key (material_uom_id) references t_material_uom (id);
create index ix_t_stock_count_item_materi_121 on t_stock_count_item (material_uom_id);
alter table t_stock_count_item add constraint fk_t_stock_count_item_batch_122 foreign key (batch_id) references t_batch (id);
create index ix_t_stock_count_item_batch_122 on t_stock_count_item (batch_id);
alter table t_stock_count_item add constraint fk_t_stock_count_item_operat_123 foreign key (operator_id) references t_employee (id);
create index ix_t_stock_count_item_operat_123 on t_stock_count_item (operator_id);
alter table t_stock_transaction add constraint fk_t_stock_transaction_wareh_124 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_stock_transaction_wareh_124 on t_stock_transaction (warehouse_id);
alter table t_stock_transaction add constraint fk_t_stock_transaction_stock_125 foreign key (stock_id) references t_stock (id);
create index ix_t_stock_transaction_stock_125 on t_stock_transaction (stock_id);
alter table t_stock_transaction add constraint fk_t_stock_transaction_execu_126 foreign key (execution_id) references t_execution (id);
create index ix_t_stock_transaction_execu_126 on t_stock_transaction (execution_id);
alter table t_storage_type add constraint fk_t_storage_type_warehouse_127 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_storage_type_warehouse_127 on t_storage_type (warehouse_id);
alter table t_timing_policy add constraint fk_t_timing_policy_itemPolic_128 foreign key (item_policy_id) references t_item_policy (id);
create index ix_t_timing_policy_itemPolic_128 on t_timing_policy (item_policy_id);
alter table t_timing_policy add constraint fk_t_timing_policy_material_129 foreign key (material_id) references t_material (id);
create index ix_t_timing_policy_material_129 on t_timing_policy (material_id);
alter table t_timing_policy add constraint fk_t_timing_policy_storageTy_130 foreign key (storage_type_id) references t_storage_type (id);
create index ix_t_timing_policy_storageTy_130 on t_timing_policy (storage_type_id);
alter table t_uom_capacity_point add constraint fk_t_uom_capacity_point_mate_131 foreign key (material_uom_id) references t_material_uom (id);
create index ix_t_uom_capacity_point_mate_131 on t_uom_capacity_point (material_uom_id);
alter table t_user add constraint fk_t_user_ldap_132 foreign key (ldap_id) references t_ldap (id);
create index ix_t_user_ldap_132 on t_user (ldap_id);
alter table t_user add constraint fk_t_user_employee_133 foreign key (employee_id) references t_employee (id);
create index ix_t_user_employee_133 on t_user (employee_id);
alter table t_user_perference add constraint fk_t_user_perference_user_134 foreign key (user_id) references t_user (id);
create index ix_t_user_perference_user_134 on t_user_perference (user_id);
alter table t_user_warehouse add constraint fk_t_user_warehouse_user_135 foreign key (user_id) references t_user (id);
create index ix_t_user_warehouse_user_135 on t_user_warehouse (user_id);
alter table t_user_warehouse add constraint fk_t_user_warehouse_warehous_136 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_user_warehouse_warehous_136 on t_user_warehouse (warehouse_id);
alter table t_warehouse add constraint fk_t_warehouse_company_137 foreign key (company_id) references t_company (id);
create index ix_t_warehouse_company_137 on t_warehouse (company_id);
alter table t_warehouse_policy add constraint fk_t_warehouse_policy_busine_138 foreign key (business_id) references t_business (id);
create index ix_t_warehouse_policy_busine_138 on t_warehouse_policy (business_id);
alter table t_warehouse_policy add constraint fk_t_warehouse_policy_compan_139 foreign key (company_id) references t_company (id);
create index ix_t_warehouse_policy_compan_139 on t_warehouse_policy (company_id);
alter table t_warehouse_policy add constraint fk_t_warehouse_policy_wareho_140 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_warehouse_policy_wareho_140 on t_warehouse_policy (warehouse_id);
alter table t_warehouse_policy add constraint fk_t_warehouse_policy_storag_141 foreign key (storage_type_id) references t_storage_type (id);
create index ix_t_warehouse_policy_storag_141 on t_warehouse_policy (storage_type_id);
alter table t_warehouse_policy add constraint fk_t_warehouse_policy_area_142 foreign key (area_id) references t_area (id);
create index ix_t_warehouse_policy_area_142 on t_warehouse_policy (area_id);
alter table t_warehouse_policy add constraint fk_t_warehouse_policy_bin_143 foreign key (bin_id) references t_bin (id);
create index ix_t_warehouse_policy_bin_143 on t_warehouse_policy (bin_id);
alter table t_warehouse_sequence add constraint fk_t_warehouse_sequence_ware_144 foreign key (warehouse_id) references t_warehouse (id);
create index ix_t_warehouse_sequence_ware_144 on t_warehouse_sequence (warehouse_id);


ALTER TABLE t_role_menu ADD COLUMN summary text;
ALTER TABLE t_menu ADD COLUMN index bigint;
ALTER TABLE t_material_uom ADD COLUMN uom_name varchar(40);

