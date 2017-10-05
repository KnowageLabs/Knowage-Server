ALTER TABLE SBI_KPI_KPI ALTER DEFINITION TYPE VARCHAR(4000);
ALTER TABLE SBI_KPI_VALUE ALTER LOGICAL_KEY TYPE VARCHAR(4000);

ALTER TABLE SBI_META_MODEL add  Foreign Key (DATA_SOURCE_ID) references SBI_DATA_SOURCE (DS_ID);

delete from SBI_PRODUCT_TYPE_ENGINE where ENGINE_ID in(select ENGINE_ID from SBI_ENGINES where NAME='Mobile Chart Engine');
delete from SBI_PRODUCT_TYPE_ENGINE where ENGINE_ID in(select ENGINE_ID from SBI_ENGINES where NAME='Mobile Cockpit Engine');
delete from SBI_PRODUCT_TYPE_ENGINE where ENGINE_ID in(select ENGINE_ID from SBI_ENGINES where NAME='Mobile Report Engine');
update SBI_ENGINES set BIOBJ_TYPE=null where NAME='Mobile Chart Engine';
update SBI_ENGINES set BIOBJ_TYPE=null where NAME='Mobile Cockpit Engine';
update SBI_ENGINES set BIOBJ_TYPE=null where NAME='Mobile Report Engine';
delete from SBI_DOMAINS where VALUE_CD='MOBILE_CHART';
delete from SBI_DOMAINS where VALUE_CD='MOBILE_COCKPIT';
delete from SBI_DOMAINS where VALUE_CD='MOBILE_REPORT';
commit;