INSERT INTO SBI_ENGINES
(ENGINE_ID,ENCRYPT,NAME,DESCR,MAIN_URL,SECN_URL,OBJ_UPL_DIR,OBJ_USE_DIR,DRIVER_NM,LABEL,ENGINE_TYPE,CLASS_NM,BIOBJ_TYPE,USE_DATASET,USE_DATASOURCE,USER_IN,USER_UP,USER_DE,TIME_IN,
TIME_UP,TIME_DE,SBI_VERSION_IN,SBI_VERSION_UP,SBI_VERSION_DE,META_VERSION,ORGANIZATION)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ENGINES'), 0, 'What-If Engine', 'What-If Engine', '/SpagoBIWhatIfEngine/restful-services/start', NULL, NULL, NULL, 'it.eng.spagobi.engines.drivers.whatif.WhatIfDriver', 'SpagoBIWhatIfEngine', (SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'ENGINE_TYPE' AND VALUE_CD = 'EXT'), NULL, (SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'BIOBJ_TYPE' AND VALUE_CD = 'OLAP'), 0, 1, 'system', NULL, NULL, SYSDATE, NULL, NULL, '5.0', NULL, NULL, NULL, 'SPAGOBI');

update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ENGINES';

INSERT INTO SBI_ORGANIZATION_ENGINE (USER_IN, TIME_IN, SBI_VERSION_IN, ENGINE_ID, ORGANIZATION_ID, ORGANIZATION)
SELECT 
'system' AS USER_IN,
SYSDATE AS TIME_IN, 
'5.0' AS SBI_VERSION_IN,
(SELECT engine_id from SBI_ENGINES where label = 'SpagoBIWhatIfEngine') AS ENGINE_ID, 
o.id AS ORGANIZATION_ID, o.name AS ORGANIZATION from SBI_ORGANIZATIONS o;


ALTER TABLE SBI_ARTIFACTS ADD  MODEL_LOCKED SMALLINT NULL;
ALTER TABLE SBI_ARTIFACTS ADD  MODEL_LOCKER VARCHAR2(100) NULL;
commit;
UPDATE SBI_ARTIFACTS SET MODEL_LOCKED = 0 WHERE MODEL_LOCKED IS NULL;

ALTER TABLE SBI_OBJECTS ADD  PARAMETERS_REGION VARCHAR2(20);

UPDATE SBI_ENGINES SET LABEL = 'SpagoBIDataMiningEngine', NAME = 'Data-Mining Engine', DESCR = 'Data-Mining Engine', MAIN_URL = '/SpagoBIDataMiningEngine/restful-services/start', DRIVER_NM = 'it.eng.spagobi.engines.drivers.datamining.DataMiningDriver', USE_DATASET=0, USE_DATASOURCE=0 WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.weka.WekaDriver';
COMMIT;

ALTER TABLE SBI_OBJ_PAR ADD  COL_SPAN INTEGER NULL;
ALTER TABLE SBI_OBJ_PAR ADD  THICK_PERC INTEGER NULL;

INSERT INTO SBI_AUTHORIZATIONS
(ID, NAME,  USER_IN, TIME_IN) 
values ((SELECT NEXT_VAL FROM hibernate_sequences WHERE SEQUENCE_NAME='SBI_AUTHORIZATIONS'), 
'CREATE_SOCIAL_ANALYSIS',  'server', current_timestamp) ;
commit;
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_AUTHORIZATIONS';
commit;

UPDATE SBI_ENGINES SET MAIN_URL = '/SpagoBICockpitEngine/api/1.0/pages/execute' WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.cockpit.CockpitDriver';
COMMIT;

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'), 
'SPAGOBI.HOME.SHOW_LOGOUT_ON_SILENT_LOGIN', 'SPAGOBI.HOME.SHOW_LOGOUT_ON_SILENT_LOGIN', 'Show the logout button in case of silent login', 1, 'true',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'NUM' AND DOMAIN_CD = 'PAR_TYPE'), 'GENERIC_CONFIGURATION', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS,USER_IN,TIME_IN,SBI_VERSION_IN) VALUES (
(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 'org.hibernate.dialect.MongoDialect', 'sbidomains.nm.mongo',
'DIALECT_HIB', 'Predefined hibernate dialect', 'sbidomains.ds.mongo', 'biadmin',  current_timestamp,  '5.0');
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;