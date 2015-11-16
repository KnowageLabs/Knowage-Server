--inserts configuration for check of role in login module
INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'),'SPAGOBI.SECURITY.CHECK_ROLE_LOGIN', 'SPAGOBI.SECURITY.CHECK_ROLE_LOGIN', 'Check the correct role in login action', 0, 'false',(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'SECURITY', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'),'SPAGOBI.SECURITY.ROLE_LOGIN', 'SPAGOBI.SECURITY.ROLE_LOGIN', 'The value of the role to check at login module', 0, '',(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'SECURITY', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';

COMMIT;

DELETE FROM sbi_domains WHERE domain_cd = 'SELECTION_TYPE';
INSERT INTO sbi_domains (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM, VALUE_DS, USER_IN, ORGANIZATION, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 'LIST', 'sbidomains.nm.list', 'SELECTION_TYPE', 'Selection modality of parameter values', 'sbidomains.ds.list', 'SPAGOBI', 'SPAGOBI', current_timestamp);
UPDATE HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
COMMIT;

INSERT INTO sbi_domains (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM, VALUE_DS, USER_IN, ORGANIZATION, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 'LOOKUP', 'sbidomains.nm.lookup', 'SELECTION_TYPE', 'Selection modality of parameter values', 'sbidomains.ds.lookup', 'SPAGOBI', 'SPAGOBI', current_timestamp);
UPDATE HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
COMMIT;

INSERT INTO sbi_domains (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM, VALUE_DS, USER_IN, ORGANIZATION, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 'SLIDER', 'sbidomains.nm.slider', 'SELECTION_TYPE', 'Selection modality of parameter values', 'sbidomains.ds.slider', 'SPAGOBI', 'SPAGOBI', current_timestamp);
UPDATE HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
COMMIT;

INSERT INTO sbi_domains (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM, VALUE_DS, USER_IN, ORGANIZATION, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 'TREE', 'sbidomains.nm.tree', 'SELECTION_TYPE', 'Selection modality of parameter values', 'sbidomains.ds.tree', 'SPAGOBI', 'SPAGOBI', current_timestamp);
UPDATE HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
COMMIT;

INSERT INTO sbi_domains (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM, VALUE_DS, USER_IN, ORGANIZATION, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 'COMBOBOX', 'sbidomains.nm.combobox', 'SELECTION_TYPE', 'Selection modality of parameter values', 'sbidomains.ds.combobox', 'SPAGOBI', 'SPAGOBI', current_timestamp);
UPDATE HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
COMMIT;
 
UPDATE sbi_obj_par SET MULT_FL=0;
UPDATE sbi_obj_par SET MULT_FL=1 WHERE par_id IN (SELECT a.par_id FROM   sbi_parameters a, sbi_paruse m
WHERE a.par_id = m.par_id and selection_type = 'CHECK_LIST');
UPDATE sbi_paruse SET selection_type='LOOKUP' WHERE selection_type = 'CHECK_LIST'OR selection_type = 'LIST';
COMMIT;

UPDATE sbi_obj_par SET REQ_FL=0;
UPDATE sbi_obj_par SET REQ_FL=1 WHERE par_id IN (SELECT a.par_id FROM   sbi_parameters a, sbi_paruse m, sbi_paruse_ck r, sbi_checks c
WHERE a.par_id = m.par_id and m.use_id = r.use_id and r.check_id = c.check_id and c.value_type_cd = 'MANDATORY');

INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'ArtifactCatalogueManagement','ArtifactCatalogueManagement', 'server', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';
commit;

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'ArtifactCatalogueManagement'));
commit;

INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'MetaModelsCatalogueManagement','MetaModelsCatalogueManagement', 'server', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';
commit;

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'MetaModelsCatalogueManagement'));
commit;

CREATE TABLE SBI_META_MODELS (
       ID                   INTEGER NOT NULL,
       NAME                 VARCHAR2(100) NOT NULL,
       DESCR                VARCHAR2(500) NULL,
       USER_IN              VARCHAR2(100) NOT NULL,
       USER_UP              VARCHAR2(100),
       USER_DE              VARCHAR2(100),
       TIME_IN              TIMESTAMP NOT NULL,
       TIME_UP              TIMESTAMP DEFAULT NULL,    
       TIME_DE              TIMESTAMP DEFAULT NULL,
       SBI_VERSION_IN       VARCHAR2(10),
       SBI_VERSION_UP       VARCHAR2(10),
       SBI_VERSION_DE       VARCHAR2(10),
       META_VERSION         VARCHAR2(100),
       ORGANIZATION         VARCHAR2(20),
       CONSTRAINT XAK1SBI_META_MODELS UNIQUE (NAME, ORGANIZATION),
       PRIMARY KEY (ID)
)
;

CREATE TABLE SBI_META_MODELS_VERSIONS (
        ID                   INTEGER NOT NULL,
        MODEL_ID             INTEGER NOT NULL,
        CONTENT              BLOB NOT NULL,
        NAME                 VARCHAR2(100),  
        PROG                 INTEGER,
        CREATION_DATE        TIMESTAMP DEFAULT NULL,
        CREATION_USER        VARCHAR2(50) NOT NULL ,
        ACTIVE               SMALLINT,  
        USER_IN              VARCHAR2(100) NOT NULL,
        USER_UP              VARCHAR2(100),
        USER_DE              VARCHAR2(100),
        TIME_IN              TIMESTAMP NOT NULL,
        TIME_UP              TIMESTAMP DEFAULT NULL,    
        TIME_DE              TIMESTAMP DEFAULT NULL,
        SBI_VERSION_IN       VARCHAR2(10),
        SBI_VERSION_UP       VARCHAR2(10),
        SBI_VERSION_DE       VARCHAR2(10),
        META_VERSION         VARCHAR2(100),
        ORGANIZATION         VARCHAR2(20),
        PRIMARY KEY (ID)
)
;

ALTER TABLE SBI_META_MODELS_VERSIONS ADD CONSTRAINT FK_SBI_META_MODELS_VERSIONS_1 FOREIGN KEY ( MODEL_ID ) REFERENCES SBI_META_MODELS( ID ) ON DELETE CASCADE
;

CREATE TABLE SBI_ARTIFACTS (
       ID                   INTEGER NOT NULL,
       NAME                 VARCHAR2(100) NOT NULL,
       DESCR                VARCHAR2(500) NULL,
       TYPE                 VARCHAR2(50) NULL,
       USER_IN              VARCHAR2(100) NOT NULL,
       USER_UP              VARCHAR2(100),
       USER_DE              VARCHAR2(100),
       TIME_IN              TIMESTAMP NOT NULL,
       TIME_UP              TIMESTAMP DEFAULT NULL,    
       TIME_DE              TIMESTAMP DEFAULT NULL,
       SBI_VERSION_IN       VARCHAR2(10),
       SBI_VERSION_UP       VARCHAR2(10),
       SBI_VERSION_DE       VARCHAR2(10),
       META_VERSION         VARCHAR2(100),
       ORGANIZATION         VARCHAR2(20),
       CONSTRAINT XAK1SBI_ARTIFACTS UNIQUE (NAME, TYPE, ORGANIZATION),
       PRIMARY KEY (ID)
)
;

CREATE TABLE SBI_ARTIFACTS_VERSIONS (
        ID                   INTEGER NOT NULL,
        ARTIFACT_ID          INTEGER NOT NULL,
        CONTENT              BLOB NOT NULL,
        NAME                 VARCHAR2(100),  
        PROG                 INTEGER,
        CREATION_DATE        TIMESTAMP DEFAULT NULL,
        CREATION_USER        VARCHAR2(50) NOT NULL ,
        ACTIVE               SMALLINT, 
        USER_IN              VARCHAR2(100) NOT NULL,
        USER_UP              VARCHAR2(100),
        USER_DE              VARCHAR2(100),
        TIME_IN              TIMESTAMP NOT NULL,
        TIME_UP              TIMESTAMP DEFAULT NULL,    
        TIME_DE              TIMESTAMP DEFAULT NULL,
        SBI_VERSION_IN       VARCHAR2(10),
        SBI_VERSION_UP       VARCHAR2(10),
        SBI_VERSION_DE       VARCHAR2(10),
        META_VERSION         VARCHAR2(100),
        ORGANIZATION         VARCHAR2(20),
        PRIMARY KEY (ID)
)
;

ALTER TABLE SBI_ARTIFACTS_VERSIONS ADD CONSTRAINT FK_SBI_ARTIFACTS_VERSIONS_1 FOREIGN KEY ( ARTIFACT_ID ) REFERENCES SBI_ARTIFACTS( ID ) ON DELETE CASCADE
;

ALTER TABLE SBI_PARUSE ADD DEFAULT_LOV_ID INTEGER NULL
;
CREATE INDEX XIF3SBI_PARUSE ON SBI_PARUSE (DEFAULT_LOV_ID)
;
ALTER TABLE SBI_PARUSE ADD DEFAULT_FORMULA VARCHAR2(4000) NULL
;
ALTER TABLE SBI_PARUSE ADD CONSTRAINT FK_SBI_PARUSE_3 FOREIGN KEY ( DEFAULT_LOV_ID ) REFERENCES SBI_LOV ( LOV_ID )
;

INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'CreateWorksheetFromDatasetUserFunctionality','CreateWorksheetFromDatasetUserFunctionality', 'server', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';
commit;

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'USER' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'CreateWorksheetFromDatasetUserFunctionality'));
commit;

-- scripts from ex 3.7 to 4.0
ALTER TABLE SBI_META_MODELS ADD CATEGORY_ID INTEGER NULL;

CREATE TABLE  SBI_EXT_ROLES_CATEGORY (
  EXT_ROLE_ID INTEGER NOT NULL,
  CATEGORY_ID INTEGER NOT NULL,
  PRIMARY KEY (EXT_ROLE_ID,CATEGORY_ID),
  CONSTRAINT FK_SB_EXT_ROLES_META_MODEL_C_1  FOREIGN KEY (EXT_ROLE_ID) REFERENCES SBI_EXT_ROLES (EXT_ROLE_ID),
  CONSTRAINT FK_SB_EXT_ROLES_META_MODEL_C_2  FOREIGN KEY (CATEGORY_ID) REFERENCES SBI_DOMAINS (VALUE_ID)
) ;

ALTER TABLE SBI_DATA_SET_HISTORY ADD IS_PERSISTED SMALLINT DEFAULT 0;
ALTER TABLE SBI_DATA_SET_HISTORY ADD DATA_SOURCE_PERSIST_ID INTEGER NULL;
ALTER TABLE SBI_DATA_SET_HISTORY ADD IS_FLAT_DATASET SMALLINT DEFAULT 0;
ALTER TABLE SBI_DATA_SET_HISTORY ADD FLAT_TABLE_NAME VARCHAR2(50) NULL;
ALTER TABLE SBI_DATA_SET_HISTORY ADD DATA_SOURCE_FLAT_ID INTEGER NULL;

ALTER TABLE SBI_DATA_SET_HISTORY ADD CONSTRAINT FK_SBI_DATA_SET_DS3 FOREIGN KEY ( DATA_SOURCE_PERSIST_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID ) ON DELETE CASCADE;
ALTER TABLE SBI_DATA_SET_HISTORY ADD CONSTRAINT FK_SBI_DATA_SET_DS4 FOREIGN KEY ( DATA_SOURCE_FLAT_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID ) ON DELETE CASCADE;

CREATE TABLE SBI_DATA_SET_TEMP (
	   DS_ID 		   		  INTEGER NOT NULL ,
	   VERSION_NUM	   		  INTEGER NOT NULL,
	   ACTIVE		   		  SMALLINT NOT NULL,
	   DESCR 		   		  VARCHAR2(160), 
	   LABEL	 	   		  VARCHAR2(50) NOT NULL,
	   NAME	   	   			  VARCHAR2(50) NOT NULL,   	   
	   OBJECT_TYPE   		  VARCHAR2(50),
	   DS_METADATA    		  CLOB,
	   PARAMS         		  VARCHAR2(4000),
	   CATEGORY_ID    		  INTEGER,
	   TRANSFORMER_ID 		  INTEGER,
       PIVOT_COLUMN   		  VARCHAR2(50),
	   PIVOT_ROW      		  VARCHAR2(50),
	   PIVOT_VALUE   		  VARCHAR2(50),
	   NUM_ROWS	   		 	  SMALLINT DEFAULT 0,	
	   IS_PERSISTED  		  SMALLINT DEFAULT 0,
	   DATA_SOURCE_PERSIST_ID INTEGER NULL,
	   IS_FLAT_DATASET 		  SMALLINT DEFAULT 0,
	   FLAT_TABLE_NAME 		  VARCHAR2(50) NULL,
	   DATA_SOURCE_FLAT_ID 	  INTEGER NULL,	   
	   CONFIGURATION          CLOB NULL,    	   	   
	   USER_IN                VARCHAR2(100) NOT NULL,
	   USER_UP                VARCHAR2(100),
	   USER_DE                VARCHAR2(100),
	   TIME_IN                TIMESTAMP NOT NULL,
	   TIME_UP                TIMESTAMP NULL,
	   TIME_DE                TIMESTAMP NULL,
	   SBI_VERSION_IN         VARCHAR2(10),
	   SBI_VERSION_UP         VARCHAR2(10),
	   SBI_VERSION_DE         VARCHAR2(10),
	   META_VERSION           VARCHAR2(100),
	   ORGANIZATION           VARCHAR2(20), 
     CONSTRAINT XAK2SBI_DATA_SET UNIQUE (LABEL,VERSION_NUM, ORGANIZATION),
     PRIMARY KEY (DS_ID, VERSION_NUM)
) ;


INSERT INTO SBI_DATA_SET_TEMP 
(DS_ID, VERSION_NUM, ACTIVE,  LABEL, DESCR, NAME, OBJECT_TYPE, DS_METADATA, PARAMS, CATEGORY_ID, TRANSFORMER_ID, PIVOT_COLUMN, PIVOT_ROW, PIVOT_VALUE, NUM_ROWS, IS_PERSISTED, 
DATA_SOURCE_PERSIST_ID, IS_FLAT_DATASET, FLAT_TABLE_NAME, DATA_SOURCE_FLAT_ID, USER_IN, USER_UP, USER_DE, TIME_IN, TIME_UP, TIME_DE, SBI_VERSION_IN, SBI_VERSION_UP, SBI_VERSION_DE,
META_VERSION, ORGANIZATION, CONFIGURATION)  
SELECT DS.DS_ID, ds_h.VERSION_NUM, ds_h.ACTIVE, ds.LABEL, ds.DESCR, ds.name,
ds_h.OBJECT_TYPE, ds_h.DS_METADATA,
ds_h.PARAMS, ds_h.CATEGORY_ID, ds_h.TRANSFORMER_ID, ds_h.PIVOT_COLUMN, ds_h.PIVOT_ROW,
ds_h.PIVOT_VALUE, ds_h.NUM_ROWS,
ds_h.IS_PERSISTED, ds_h.DATA_SOURCE_PERSIST_ID, ds_h.IS_FLAT_DATASET, ds_h.FLAT_TABLE_NAME, ds_h.DATA_SOURCE_FLAT_ID,
ds_h.USER_IN, 
null as USER_UP,null as USER_DE, ds_h.TIME_IN, null as TIME_UP, null as TIME_DE,
ds_h.SBI_VERSION_IN, null as SBI_VERSION_UP,  null as SBI_VERSION_DE, ds_h.META_VERSION,
ds_h.ORGANIZATION,
CASE WHEN ds_h.OBJECT_TYPE = 'SbiQueryDataSet' THEN 
TO_CLOB('{"Query":"' || REPLACE(ds_h.QUERY,'\"','\\\"') || '","queryScript":"' || REPLACE(NVL(DS_H.QUERY_SCRIPT,''),'\"','\\\"') || '","queryScriptLanguage":"' || REPLACE(NVL(QUERY_SCRIPT_LANGUAGE,''),'\"','\\\"') || '","dataSource":"' || NVL((SELECT LABEL FROM SBI_DATA_SOURCE WHERE DS_ID = DATA_SOURCE_ID),'') || '"}' )
WHEN ds_h.OBJECT_TYPE = 'SbiFileDataSet' THEN 
TO_CLOB('{"fileName":"' || NVL(DS_H.FILE_NAME,'') || '"}')
WHEN ds_h.OBJECT_TYPE = 'SbiFileDataSet' THEN 
TO_CLOB('{"SbiJClassDataSet":"' || NVL(DS_H.JCLASS_NAME,'') || '"}')
WHEN ds_h.OBJECT_TYPE = 'SbiFileDataSet' THEN 
TO_CLOB('{"wsAddress":"' || NVL(DS_H.ADRESS,'') || '","wsOperation":"' || NVL(DS_H.OPERATION,'') || '"}')
WHEN ds_h.OBJECT_TYPE = 'SbiScriptDataSet' THEN 
TO_CLOB('{"Script":"' || REPLACE(NVL(DS_H.SCRIPT,''),'\"','\\\"') || '","scriptLanguage":"' || NVL(DS_H.LANGUAGE_SCRIPT,'') || '"}')
WHEN ds_h.OBJECT_TYPE = 'SbiCustomDataSet' THEN 
TO_CLOB('{"customData":"' || REPLACE(NVL(DS_H.CUSTOM_DATA,'"{}"'),'\"','\\\"') || '","jClassName":"' || NVL(DS_H.JCLASS_NAME,'') || '"}')
WHEN ds_h.OBJECT_TYPE = 'SbiQbeDataSet' THEN 
TO_CLOB('{"qbeDatamarts":"' || NVL(DS_H.DATAMARTS,'') || '","qbeDataSource":"' || NVL((SELECT LABEL FROM SBI_DATA_SOURCE WHERE DS_ID = DATA_SOURCE_ID),'') || '","qbeJSONQuery":"' || REPLACE(NVL(DS_H.JSON_QUERY,''),'\"','\\\"') || '"' || '"}')
end AS CONFIGURATION
FROM SBI_DATA_SET DS INNER JOIN SBI_DATA_SET_HISTORY DS_H ON (DS.DS_ID = DS_H.DS_ID)
--WHERE DS_H.ACTIVE = 1
order by ds_id, version_num ;

commit;

--DROP OLDER FK TO SBI_DATA_SET
ALTER TABLE SBI_LOV DROP CONSTRAINT SBI_LOV_2;
ALTER TABLE SBI_OBJECTS DROP CONSTRAINT SBI_OBJECTS_7;
ALTER TABLE SBI_KPI DROP CONSTRAINT SBI_KPI_1;

ALTER TABLE SBI_DATA_SET RENAME TO SBI_DATA_SET_OLD;
ALTER TABLE SBI_DATA_SET_HISTORY RENAME TO SBI_DATA_SET_HISTORY_OLD;
ALTER TABLE SBI_DATA_SET_TEMP RENAME TO SBI_DATA_SET;

-- to do at the end, when all it's ended correctly!
--DROP TABLE SBI_DATA_SET_HISTORY_OLD CASCADE CONSTRAINTS;
--DROP TABLE SBI_DATA_SET_OLD CASCADE CONSTRAINTS;    
-- to do only after drop stmt
--ALTER TABLE SBI_DATA_SET ADD CONSTRAINT FK_SBI_DATA_SET_T  FOREIGN KEY ( TRANSFORMER_ID ) REFERENCES SBI_DOMAINS ( VALUE_ID ) ON DELETE CASCADE;
--ALTER TABLE SBI_DATA_SET ADD CONSTRAINT FK_SBI_DATA_SET_CAT  FOREIGN KEY (CATEGORY_ID) REFERENCES SBI_DOMAINS (VALUE_ID) ON DELETE CASCADE ON UPDATE RESTRICT;
--ALTER TABLE SBI_DATA_SET ADD CONSTRAINT FK_SBI_DATA_SET_DS3 FOREIGN KEY ( DATA_SOURCE_PERSIST_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID ) ON DELETE CASCADE;
--ALTER TABLE SBI_DATA_SET ADD CONSTRAINT FK_SBI_DATA_SET_DS4 FOREIGN KEY ( DATA_SOURCE_FLAT_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID ) ON DELETE CASCADE;

-- insert records for selfservice dataset management 
INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'SelfServiceDatasetManagement','SelfServiceDatasetManagement', 'server', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';
commit;
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'USER' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceDatasetManagement'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceDatasetManagement'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'DEV_ROLE' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceDatasetManagement'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'TEST_ROLE' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceDatasetManagement'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'MODEL_ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceDatasetManagement'));
commit;

UPDATE SBI_ENGINES SET USE_DATASET = 1 WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.worksheet.WorksheetDriver';
COMMIT;

ALTER TABLE SBI_DATA_SET ADD OWNER VARCHAR2(50);
ALTER TABLE SBI_DATA_SET ADD IS_PUBLIC SMALLINT DEFAULT 0;

UPDATE SBI_DATA_SET SET IS_PUBLIC = 1, OWNER = USER_IN;
COMMIT;

INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'SelfServiceMetaModelManagement','SelfServiceMetaModelManagement', 'server', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';
commit;
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'USER' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceMetaModelManagement'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceMetaModelManagement'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'DEV_ROLE' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceMetaModelManagement'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'TEST_ROLE' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceMetaModelManagement'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'MODEL_ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'SelfServiceMetaModelManagement'));
commit;

ALTER TABLE SBI_META_MODELS  ADD DATA_SOURCE_ID INTEGER;
ALTER TABLE SBI_META_MODELS ADD CONSTRAINT FK_SBIDATA_SOURCE FOREIGN KEY ( DATA_SOURCE_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID );

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'), 
'SPAGOBI.DATASET_FILE_MAX_SIZE', 'DATASET FILE MAX SIZE', 'Max size for a file used as a dataset', 1, '10485760',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'GENERIC_CONFIGURATION', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;

UPDATE SBI_CONFIG SET VALUE_CHECK = 'dd/MM/yyyy HH:mm:ss' WHERE LABEL = 'SPAGOBI.TIMESTAMP-FORMAT.format';
commit;

-- 24/06/2013 Marco: Added default mandatory Dataset Metadata Properties
INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'fieldType','fieldType','DS_META_PROPERTY','Data Set Metadata Property','Data Set Metadata Property','biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'type','type','DS_META_PROPERTY','Data Set Metadata Property','Data Set Metadata Property','biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

ALTER TABLE SBI_EXT_ROLES ADD SEE_DOCUMENT_BROWSER 	SMALLINT DEFAULT 1;
ALTER TABLE SBI_EXT_ROLES ADD SEE_FAVOURITES 		SMALLINT DEFAULT 1;
ALTER TABLE SBI_EXT_ROLES ADD SEE_SUBSCRIPTIONS 	SMALLINT DEFAULT 1;
ALTER TABLE SBI_EXT_ROLES ADD SEE_MY_DATA 			SMALLINT DEFAULT 1;
ALTER TABLE SBI_EXT_ROLES ADD SEE_TODO_LIST 		SMALLINT DEFAULT 1;
ALTER TABLE SBI_EXT_ROLES ADD CREATE_DOCUMENTS 		SMALLINT DEFAULT 1;

-- 25/06/2013 Marco: Added default mandatory Dataset Metadata Properties' Values
INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'MEASURE','MEASURE','DS_META_VALUE','Data Set Metadata Value','Data Set Metadata Value','biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'ATTRIBUTE','ATTRIBUTE','DS_META_VALUE','Data Set Metadata Value','Data Set Metadata Value','biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'String','String','DS_META_VALUE','Data Set Metadata Value','Data Set Metadata Value','biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'Integer','Integer','DS_META_VALUE','Data Set Metadata Value','Data Set Metadata Value','biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'Double','Double','DS_META_VALUE','Data Set Metadata Value','Data Set Metadata Value','biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

update sbi_engines set label = 'SpagoBIGisEngine' where label = 'GeoReportEngine';
commit;