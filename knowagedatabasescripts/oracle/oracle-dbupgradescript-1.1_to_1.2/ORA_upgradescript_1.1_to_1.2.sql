ALTER TABLE SBI_CATALOG_FUNCTION ADD DESCRIPTION CLOB NOT NULL;

ALTER TABLE SBI_PARUSE ADD COLUMN OPTIONS VARCHAR(4000) NULL DEFAULT NULL

ALTER TABLE  SBI_GEO_MAPS ADD HIERARCHY_NAME VARCHAR2(100);
ALTER TABLE  SBI_GEO_MAPS ADD LEVEL INTEGER;
ALTER TABLE  SBI_GEO_MAPS ADD MEMBER_NAME VARCHAR2(100);

-- alter table SBI_USER_FUNC disable constraint 'FK_PRODUCT_TYPE';
-- delete from SBI_USER_FUNC  where name = 'CreateWorksheetFromDatasetUserFunctionality';
-- alter table SBI_USER_FUNC enable constraint 'FK_PRODUCT_TYPE';

-- alter table SBI_ENGINES disable constraint 'FK_SBI_ENGINES_1';
-- alter table SBI_ENGINES disable constraint 'FK_SBI_ENGINES_2';
-- delete from SBI_ENGINES  where name = 'Worksheet Engine';
-- alter table SBI_ENGINES enable constraint 'FK_SBI_ENGINES_1';
-- alter table SBI_ENGINES enable constraint 'FK_SBI_ENGINES_2';
-- delete from SBI_DOMAINS where value_cd = 'WORKSHEET';

ALTER TABLE  SBI_DATA_SET ADD IS_PERSISTED_HDFS SMALLINT DEFAULT 0;
