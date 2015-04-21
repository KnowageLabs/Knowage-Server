ALTER TABLE SBI_META_MODELS ADD COLUMN CATEGORY_ID INTEGER NULL;

CREATE TABLE  SBI_EXT_ROLES_CATEGORY (
  EXT_ROLE_ID INTEGER NOT NULL,
  CATEGORY_ID INTEGER NOT NULL,
  PRIMARY KEY (EXT_ROLE_ID,CATEGORY_ID),
  KEY FK_SB_EXT_ROLES_META_MODEL_CATEGORY_2 (CATEGORY_ID),
  CONSTRAINT FK_SB_EXT_ROLES_META_MODEL_CATEGORY_1 FOREIGN KEY (EXT_ROLE_ID) REFERENCES SBI_EXT_ROLES (EXT_ROLE_ID),
  CONSTRAINT FK_SB_EXT_ROLES_META_MODEL_CATEGORY_2 FOREIGN KEY (CATEGORY_ID) REFERENCES SBI_DOMAINS (VALUE_ID)
) ;\p\g

ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN IS_PERSISTED  TINYINT DEFAULT 0;\p\g
ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN DATA_SOURCE_PERSIST_ID INTEGER NULL;\p\g
ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN IS_FLAT_DATASET TINYINT DEFAULT 0;\p\g
ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN FLAT_TABLE_NAME VARCHAR(50) NULL;\p\g
ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN DATA_SOURCE_FLAT_ID INTEGER NULL;\p\g

ALTER TABLE SBI_DATA_SET_HISTORY ADD CONSTRAINT FK_SBI_DATA_SET_DS3 FOREIGN KEY ( DATA_SOURCE_PERSIST_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID ) ON DELETE CASCADE;\p\g
ALTER TABLE SBI_DATA_SET_HISTORY ADD CONSTRAINT FK_SBI_DATA_SET_DS4 FOREIGN KEY ( DATA_SOURCE_FLAT_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID ) ON DELETE CASCADE;\p\g

-- insert records for selfservice dataset management 
INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'SelfServiceDatasetManagement','SelfServiceDatasetManagement', 'server', current_timestamp);\p\g 
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';\p\g 
commit;\p\g 
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'USER' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceDatasetManagement'));\p\g 
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceDatasetManagement'));\p\g 
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'DEV_ROLE' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceDatasetManagement'));\p\g 
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'TEST_ROLE' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceDatasetManagement'));\p\g 
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'MODEL_ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceDatasetManagement'));\p\g 
commit;\p\g 

UPDATE SBI_ENGINES SET USE_DATASET = 1 WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.worksheet.WorksheetDriver';\p\g
COMMIT;\p\g

INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'SelfServiceMetaModelManagement','SelfServiceMetaModelManagement', 'server', current_timestamp);\p\g 
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';\p\g
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'USER' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceMetaModelManagement'));\p\g
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceMetaModelManagement'));\p\g
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'DEV_ROLE' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceMetaModelManagement'));\p\g
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'TEST_ROLE' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceMetaModelManagement'));\p\g
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'MODEL_ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceMetaModelManagement'));\p\g
commit;\p\g
ALTER TABLE SBI_META_MODELS  ADD DATA_SOURCE_ID INTEGER;\p\g
ALTER TABLE SBI_META_MODELS ADD CONSTRAINT FK_SBIDATA_SOURCE FOREIGN KEY ( DATA_SOURCE_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID );\p\g

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'), 
'SPAGOBI.DATASET_FILE_MAX_SIZE', 'DATASET FILE MAX SIZE', 'Max size for a file used as a dataset', true, '10485760',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'GENERIC_CONFIGURATION', 'biadmin', current_timestamp);\p\g
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';\p\g
commit;\p\g

UPDATE SBI_CONFIG SET VALUE_CHECK = 'dd/MM/yyyy HH:mm:ss' WHERE LABEL = 'SPAGOBI.TIMESTAMP-FORMAT.format';
commit;

-- 24/06/2013 Marco: Added default mandatory Dataset Metadata Properties
INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'fieldType','fieldType','DS_META_PROPERTY','Data Set Metadata Property','Data Set Metadata Property','biadmin', current_timestamp);\p\g
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';\p\g
commit;\p\g

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'type','type','DS_META_PROPERTY','Data Set Metadata Property','Data Set Metadata Property','biadmin', current_timestamp);\p\g
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';\p\g
commit;\p\g

-- 25/06/2013 Marco: Added default mandatory Dataset Metadata Properties' Values
INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'MEASURE','MEASURE','DS_META_VALUE','Data Set Metadata Value','Data Set Metadata Value','biadmin', current_timestamp);\p\g
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';\p\g
commit;\p\g

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'ATTRIBUTE','ATTRIBUTE','DS_META_VALUE','Data Set Metadata Value','Data Set Metadata Value','biadmin', current_timestamp);\p\g
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';\p\g
commit;\p\g

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'String','String','DS_META_VALUE','Data Set Metadata Value','Data Set Metadata Value','biadmin', current_timestamp);\p\g
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';\p\g
commit;\p\g

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'Integer','Integer','DS_META_VALUE','Data Set Metadata Value','Data Set Metadata Value','biadmin', current_timestamp);\p\g
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';\p\g
commit;\p\g

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'Double','Double','DS_META_VALUE','Data Set Metadata Value','Data Set Metadata Value','biadmin', current_timestamp);\p\g
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';\p\g
commit;\p\g