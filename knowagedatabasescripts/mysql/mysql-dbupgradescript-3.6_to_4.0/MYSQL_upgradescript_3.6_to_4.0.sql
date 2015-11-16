--  inserts configuration for check of role in login module
INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'),'SPAGOBI.SECURITY.CHECK_ROLE_LOGIN', 'SPAGOBI.SECURITY.CHECK_ROLE_LOGIN', 'Check the correct role in login action', false, 'false',(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'SECURITY', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'),'SPAGOBI.SECURITY.ROLE_LOGIN', 'SPAGOBI.SECURITY.ROLE_LOGIN', 'The value of the role to check at login module', false, '',(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'SECURITY', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';

COMMIT;
--  NETWORK ENGINE
INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
    'PNG','PNG','EXPORT_TYPE','Exporters type','Exporters type', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
    'GRAPHML','GRAPHML','EXPORT_TYPE','Exporters type','Exporters type', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
    'NETWORK','sbidomains.nm.network','BIOBJ_TYPE','BI Object types','sbidomains.ds.network', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

SELECT @sequence:=(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ENGINES' )-1;
INSERT INTO SBI_ENGINES (ENGINE_ID, NAME,DESCR, ENCRYPT, LABEL,MAIN_URL, DRIVER_NM, ENGINE_TYPE,BIOBJ_TYPE,USE_DATASOURCE,USE_DATASET, USER_IN, TIME_IN, SBI_VERSION_IN,ORGANIZATION)
SELECT @sequence:=@sequence+1, 'Network Analysis Engine','Network Analysis Engine' , 0, 'SpagoBINetworkEngine','/SpagoBINetworkEngine/servlet/AdapterHTTP','it.eng.spagobi.engines.drivers.network.NetworkDriver',(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'ENGINE_TYPE' AND VALUE_CD = 'EXT'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'BIOBJ_TYPE' AND VALUE_CD = 'NETWORK'),false, true, 'biadmin', current_timestamp, '3.6.0', ORGANIZATION.NAME
FROM SBI_ORGANIZATIONS ORGANIZATION;

INSERT INTO SBI_EXPORTERS (ENGINE_ID,DOMAIN_ID,DEFAULT_VALUE) 
	VALUES ((SELECT ENGINE_ID FROM SBI_ENGINES WHERE LABEL = 'SpagoBINetworkEngine'),
	(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'EXPORT_TYPE' AND VALUE_CD = 'PDF'), 
	false);
commit;
INSERT INTO SBI_EXPORTERS (ENGINE_ID,DOMAIN_ID,DEFAULT_VALUE) 
	VALUES ((SELECT ENGINE_ID FROM SBI_ENGINES WHERE LABEL = 'SpagoBINetworkEngine'),
	(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'EXPORT_TYPE' AND VALUE_CD = 'PNG'), 
	true);
commit;
INSERT INTO SBI_EXPORTERS (ENGINE_ID,DOMAIN_ID,DEFAULT_VALUE) 
	VALUES ((SELECT ENGINE_ID FROM SBI_ENGINES WHERE LABEL = 'SpagoBINetworkEngine'),
	(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'EXPORT_TYPE' AND VALUE_CD = 'GRAPHML'), 
	true);
commit;

DELETE FROM SBI_DOMAINS WHERE domain_cd = 'SELECTION_TYPE';

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM, VALUE_DS, USER_IN, ORGANIZATION, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 'LIST', 'sbidomains.nm.list', 'SELECTION_TYPE', 'Selection modality of parameter values', 'sbidomains.ds.list', 'SPAGOBI', 'SPAGOBI', current_timestamp);
UPDATE hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
COMMIT;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM, VALUE_DS, USER_IN, ORGANIZATION, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 'LOOKUP', 'sbidomains.nm.lookup', 'SELECTION_TYPE', 'Selection modality of parameter values', 'sbidomains.ds.lookup', 'SPAGOBI', 'SPAGOBI', current_timestamp);
UPDATE hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
COMMIT;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM, VALUE_DS, USER_IN, ORGANIZATION, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 'SLIDER', 'sbidomains.nm.slider', 'SELECTION_TYPE', 'Selection modality of parameter values', 'sbidomains.ds.slider', 'SPAGOBI', 'SPAGOBI', current_timestamp);
UPDATE hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
COMMIT;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD, VALUE_NM, DOMAIN_CD, DOMAIN_NM, VALUE_DS, USER_IN, ORGANIZATION, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 'TREE', 'sbidomains.nm.tree', 'SELECTION_TYPE', 'Selection modality of parameter values', 'sbidomains.ds.tree', 'SPAGOBI', 'SPAGOBI', current_timestamp);
UPDATE hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
COMMIT;

UPDATE SBI_OBJ_PAR SET MULT_FL=0;
UPDATE SBI_OBJ_PAR SET MULT_FL=1 WHERE par_id IN (SELECT a.par_id FROM   SBI_PARAMETERS a, SBI_PARUSE m
WHERE a.par_id = m.par_id and selection_type = 'CHECK_LIST');
UPDATE SBI_PARUSE SET selection_type='LOOKUP' WHERE selection_type = 'CHECK_LIST'OR selection_type = 'LIST';
COMMIT;

UPDATE SBI_OBJ_PAR SET REQ_FL=0;
UPDATE SBI_OBJ_PAR SET REQ_FL=1 WHERE par_id IN (SELECT a.par_id FROM   SBI_PARAMETERS a, SBI_PARUSE m, SBI_PARUSE_CK r, SBI_CHECKS c
WHERE a.par_id = m.par_id and m.use_id = r.use_id and r.check_id = c.check_id and c.value_type_cd = 'MANDATORY');
COMMIT;

update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';

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
       NAME                 VARCHAR(100) NOT NULL,
       DESCR                VARCHAR(500) NULL,
       USER_IN              VARCHAR(100) NOT NULL,
       USER_UP              VARCHAR(100),
       USER_DE              VARCHAR(100),
       TIME_IN              TIMESTAMP NOT NULL,
       TIME_UP              TIMESTAMP NULL DEFAULT NULL,
       TIME_DE              TIMESTAMP NULL DEFAULT NULL,
       SBI_VERSION_IN       VARCHAR(10),
       SBI_VERSION_UP       VARCHAR(10),
       SBI_VERSION_DE       VARCHAR(10),
       META_VERSION         VARCHAR(100),
       ORGANIZATION         VARCHAR(20),
       CONSTRAINT XAK1SBI_META_MODELS UNIQUE (NAME, ORGANIZATION),
       PRIMARY KEY (ID)
) ENGINE=InnoDB;

CREATE TABLE SBI_META_MODELS_VERSIONS (
        ID                   INTEGER NOT NULL,
        MODEL_ID             INTEGER NOT NULL,
        CONTENT              MEDIUMBLOB NOT NULL,
        NAME                 VARCHAR(100),  
        PROG                 INTEGER,
        CREATION_DATE        TIMESTAMP NULL DEFAULT NULL,
        CREATION_USER        VARCHAR(50) NOT NULL, 
        ACTIVE               BOOLEAN,  
        USER_IN              VARCHAR(100) NOT NULL,
        USER_UP              VARCHAR(100),
        USER_DE              VARCHAR(100),
        TIME_IN              TIMESTAMP NOT NULL,
        TIME_UP              TIMESTAMP NULL DEFAULT NULL,
        TIME_DE              TIMESTAMP NULL DEFAULT NULL,
        SBI_VERSION_IN       VARCHAR(10),
        SBI_VERSION_UP       VARCHAR(10),
        SBI_VERSION_DE       VARCHAR(10),
        META_VERSION         VARCHAR(100),
        ORGANIZATION         VARCHAR(20),
        PRIMARY KEY (ID)
) ENGINE=InnoDB;

ALTER TABLE SBI_META_MODELS_VERSIONS ADD CONSTRAINT FK_SBI_META_MODELS_VERSIONS_1 FOREIGN KEY ( MODEL_ID ) REFERENCES SBI_META_MODELS( ID ) ON DELETE CASCADE;

CREATE TABLE SBI_ARTIFACTS (
       ID                   INTEGER NOT NULL,
       NAME                 VARCHAR(100) NOT NULL,
       DESCR                VARCHAR(500) NULL,
       TYPE                 VARCHAR(50) NULL,
       USER_IN              VARCHAR(100) NOT NULL,
       USER_UP              VARCHAR(100),
       USER_DE              VARCHAR(100),
       TIME_IN              TIMESTAMP NOT NULL,
       TIME_UP              TIMESTAMP NULL DEFAULT NULL,
       TIME_DE              TIMESTAMP NULL DEFAULT NULL,
       SBI_VERSION_IN       VARCHAR(10),
       SBI_VERSION_UP       VARCHAR(10),
       SBI_VERSION_DE       VARCHAR(10),
       META_VERSION         VARCHAR(100),
       ORGANIZATION         VARCHAR(20),
       CONSTRAINT XAK1SBI_ARTIFACTS UNIQUE (NAME, TYPE, ORGANIZATION),
       PRIMARY KEY (ID)
) ENGINE=InnoDB;

CREATE TABLE SBI_ARTIFACTS_VERSIONS (
        ID                   INTEGER NOT NULL,
        ARTIFACT_ID          INTEGER NOT NULL,
        CONTENT              MEDIUMBLOB NOT NULL,
        NAME                 VARCHAR(100),  
        PROG                 INTEGER,
        CREATION_DATE        TIMESTAMP NULL DEFAULT NULL,
        CREATION_USER        VARCHAR(50) NOT NULL, 
        ACTIVE               BOOLEAN,  
        USER_IN              VARCHAR(100) NOT NULL,
        USER_UP              VARCHAR(100),
        USER_DE              VARCHAR(100),
        TIME_IN              TIMESTAMP NOT NULL,
        TIME_UP              TIMESTAMP NULL DEFAULT NULL,
        TIME_DE              TIMESTAMP NULL DEFAULT NULL,
        SBI_VERSION_IN       VARCHAR(10),
        SBI_VERSION_UP       VARCHAR(10),
        SBI_VERSION_DE       VARCHAR(10),
        META_VERSION         VARCHAR(100),
        ORGANIZATION         VARCHAR(20),
        PRIMARY KEY (ID)
) ENGINE=InnoDB;

ALTER TABLE SBI_ARTIFACTS_VERSIONS ADD CONSTRAINT FK_SBI_ARTIFACTS_VERSIONS_1 FOREIGN KEY ( ARTIFACT_ID ) REFERENCES SBI_ARTIFACTS( ID ) ON DELETE CASCADE;

ALTER TABLE SBI_PARUSE ADD COLUMN DEFAULT_LOV_ID INTEGER NULL;
ALTER TABLE SBI_PARUSE ADD COLUMN DEFAULT_FORMULA VARCHAR(4000) NULL;
ALTER TABLE SBI_PARUSE ADD CONSTRAINT FK_SBI_PARUSE_3 FOREIGN KEY ( DEFAULT_LOV_ID ) REFERENCES SBI_LOV ( LOV_ID ) ON DELETE RESTRICT;

INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'CreateWorksheetFromDatasetUserFunctionality','CreateWorksheetFromDatasetUserFunctionality', 'server', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';
commit;

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'USER' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'CreateWorksheetFromDatasetUserFunctionality'));
commit;

ALTER TABLE SBI_META_MODELS ADD COLUMN CATEGORY_ID INTEGER NULL;

CREATE TABLE  SBI_EXT_ROLES_CATEGORY (
  EXT_ROLE_ID INTEGER NOT NULL,
  CATEGORY_ID INTEGER NOT NULL,
  PRIMARY KEY (EXT_ROLE_ID,CATEGORY_ID),
  KEY FK_SB_EXT_ROLES_META_MODEL_CATEGORY_2 (CATEGORY_ID),
  CONSTRAINT FK_SB_EXT_ROLES_META_MODEL_CATEGORY_1 FOREIGN KEY (EXT_ROLE_ID) REFERENCES SBI_EXT_ROLES (EXT_ROLE_ID),
  CONSTRAINT FK_SB_EXT_ROLES_META_MODEL_CATEGORY_2 FOREIGN KEY (CATEGORY_ID) REFERENCES SBI_DOMAINS (VALUE_ID)
) ENGINE=InnoDB ;

ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN IS_PERSISTED BOOLEAN DEFAULT FALSE;
ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN DATA_SOURCE_PERSIST_ID INTEGER NULL;
ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN IS_FLAT_DATASET BOOLEAN DEFAULT FALSE;
ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN FLAT_TABLE_NAME VARCHAR(50) NULL;
ALTER TABLE SBI_DATA_SET_HISTORY ADD COLUMN DATA_SOURCE_FLAT_ID INTEGER NULL;

ALTER TABLE SBI_DATA_SET_HISTORY ADD CONSTRAINT FK_SBI_DATA_SET_DS3 FOREIGN KEY ( DATA_SOURCE_PERSIST_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID ) ON DELETE CASCADE;
ALTER TABLE SBI_DATA_SET_HISTORY ADD CONSTRAINT FK_SBI_DATA_SET_DS4 FOREIGN KEY ( DATA_SOURCE_FLAT_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID ) ON DELETE CASCADE;

CREATE TABLE SBI_DATA_SET_TEMP (
	   DS_ID 		   		  INTEGER NOT NULL ,
	   VERSION_NUM	   		  INTEGER NOT NULL,
	   ACTIVE		   		  BOOLEAN NOT NULL,
	   DESCR 		   		  VARCHAR(160), 
	   LABEL	 	   		  VARCHAR(50) NOT NULL,
	   NAME	   	   			  VARCHAR(50) NOT NULL,  
 	   
	   OBJECT_TYPE   		  VARCHAR(50),
	   DS_METADATA    		  TEXT,
	   PARAMS         		  VARCHAR(4000),
	   CATEGORY_ID    		  INTEGER,
	   TRANSFORMER_ID 		  INTEGER,
       PIVOT_COLUMN   		  VARCHAR(50),
	   PIVOT_ROW      		  VARCHAR(50),
	   PIVOT_VALUE   		  VARCHAR(50),
	   NUM_ROWS	   		 	  BOOLEAN DEFAULT FALSE,	
	   IS_PERSISTED  		  BOOLEAN DEFAULT FALSE,
	   DATA_SOURCE_PERSIST_ID INTEGER NULL,
	   IS_FLAT_DATASET 		  BOOLEAN DEFAULT FALSE,
	   FLAT_TABLE_NAME 		  VARCHAR(50) NULL,
	   DATA_SOURCE_FLAT_ID 	  INTEGER NULL,
	   
	   CONFIGURATION          TEXT NULL,    
	   	   
	   USER_IN                VARCHAR(100) NOT NULL,
	   USER_UP                VARCHAR(100),
	   USER_DE                VARCHAR(100),
	   TIME_IN                TIMESTAMP NOT NULL,
	   TIME_UP                TIMESTAMP NULL DEFAULT NULL,
	   TIME_DE                TIMESTAMP NULL DEFAULT NULL,
	   SBI_VERSION_IN         VARCHAR(10),
	   SBI_VERSION_UP         VARCHAR(10),
	   SBI_VERSION_DE         VARCHAR(10),
	   META_VERSION           VARCHAR(100),
	   ORGANIZATION           VARCHAR(20), 
	   
     UNIQUE XAK1SBI_DATA_SET (LABEL, VERSION_NUM, ORGANIZATION),
     PRIMARY KEY (DS_ID, VERSION_NUM)
) ENGINE=InnoDB;

INSERT SBI_DATA_SET_TEMP (DS_ID, VERSION_NUM, ACTIVE,  LABEL, DESCR, NAME, OBJECT_TYPE, DS_METADATA, PARAMS, CATEGORY_ID, TRANSFORMER_ID, PIVOT_COLUMN, PIVOT_ROW, PIVOT_VALUE, NUM_ROWS, IS_PERSISTED, 
DATA_SOURCE_PERSIST_ID, IS_FLAT_DATASET, FLAT_TABLE_NAME, DATA_SOURCE_FLAT_ID, USER_IN, USER_UP, USER_DE, TIME_IN, TIME_UP, TIME_DE, SBI_VERSION_IN, SBI_VERSION_UP, SBI_VERSION_DE,
META_VERSION, ORGANIZATION, CONFIGURATION) 
SELECT DS.DS_ID, DS_H.VERSION_NUM, DS_H.ACTIVE, DS.LABEL, DS.DESCR, DS.name,
DS_H.OBJECT_TYPE, DS_H.DS_METADATA,
DS_H.PARAMS, DS_H.CATEGORY_ID, DS_H.TRANSFORMER_ID, DS_H.PIVOT_COLUMN, DS_H.PIVOT_ROW,
DS_H.PIVOT_VALUE, DS_H.NUM_ROWS, DS_H.IS_PERSISTED, DS_H.DATA_SOURCE_PERSIST_ID, 
DS_H.IS_FLAT_DATASET, DS_H.FLAT_TABLE_NAME, DS_H.DATA_SOURCE_FLAT_ID, DS_H.USER_IN, 
null as USER_UP,null as USER_DE, DS_H.TIME_IN, null as TIME_UP, null as TIME_DE,
DS_H.SBI_VERSION_IN, null as SBI_VERSION_UP,  null as SBI_VERSION_DE, DS_H.META_VERSION,
DS_H.ORGANIZATION,
case when DS_H.OBJECT_TYPE = 'SbiQueryDataSet' then 
concat('{"Query":"',REPLACE(DS_H.QUERY,'\"','\\\"'),'","queryScript":"',REPLACE(COALESCE(DS_H.QUERY_SCRIPT,''),'\"','\\\"'),'","queryScriptLanguage":"',COALESCE(QUERY_SCRIPT_LANGUAGE,''),'","dataSource":"',COALESCE(CAST((SELECT LABEL FROM SBI_DATA_SOURCE WHERE DS_ID = DATA_SOURCE_ID) AS CHAR),''),'"}')  
WHEN DS_H.OBJECT_TYPE = 'SbiFileDataSet' then 
CONCAT('{"fileName":"',COALESCE(DS_H.FILE_NAME,''),'"}')
WHEN DS_H.OBJECT_TYPE = 'SbiFileDataSet' then 
CONCAT('{"SbiJClassDataSet":"',COALESCE(DS_H.JCLASS_NAME,''),'"}')
WHEN DS_H.OBJECT_TYPE = 'SbiWSDataSet' then 
CONCAT('{"wsAddress":"',COALESCE(DS_H.ADRESS,''),'","wsOperation":"',COALESCE(DS_H.OPERATION,''),'"}')
WHEN DS_H.OBJECT_TYPE = 'SbiScriptDataSet' then 
CONCAT('{"Script":"',REPLACE(COALESCE(DS_H.SCRIPT,''),'\"','\\\"'),'","scriptLanguage":"',COALESCE(DS_H.LANGUAGE_SCRIPT,''),'"}')
WHEN DS_H.OBJECT_TYPE = 'SbiCustomDataSet' then 
CONCAT('{"customData":"',REPLACE(COALESCE(DS_H.CUSTOM_DATA,'"{}"'),'\"','\\\"'),'","jClassName":"',COALESCE(DS_H.JCLASS_NAME,''),'"}')
WHEN DS_H.OBJECT_TYPE = 'SbiQbeDataSet' then 
CONCAT('{"qbeDatamarts":"',COALESCE(DS_H.DATAMARTS,''),'","qbeDataSource":"',COALESCE(CAST((SELECT LABEL FROM SBI_DATA_SOURCE WHERE DS_ID = DATA_SOURCE_ID) AS CHAR),''),'","qbeJSONQuery":"',REPLACE(COALESCE(DS_H.JSON_QUERY,''),'\"','\\\"'),'"}')
end AS CONFIGURATION
FROM 
SBI_DATA_SET DS INNER JOIN SBI_DATA_SET_HISTORY DS_H ON (DS.DS_ID = DS_H.DS_ID)
WHERE DS_H.ACTIVE = TRUE
order by ds_id, version_num;

commit;
         
RENAME TABLE SBI_DATA_SET TO SBI_DATA_SET_OLD;
RENAME TABLE SBI_DATA_SET_HISTORY TO SBI_DATA_SET_HISTORY_OLD;
RENAME TABLE SBI_DATA_SET_TEMP TO SBI_DATA_SET;

--  to do at the end, when all it's ended correctly!
--  Dropping older FK to SBI_DATA_SET
--  ALTER TABLE SBI_LOV DROP FOREIGN KEY FK_SBI_LOV_2;
--  ALTER TABLE SBI_OBJECTS DROP FOREIGN KEY FK_SBI_OBJECTS_7;

--  ATTENTION: for the SBI_KPI table the FK haven't an explicity name, so is necessary get it and use it in drop command: 
--  select constraint_name from INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS WHERE constraint_schema = '<SCHEMA_DB_NAME>' and
--  referenced_table_name = 'SBI_DATA_SET_OLD'  and table_name = 'SBI_KPI';
--  ALTER TABLE sbi_kpi DROP FOREIGN KEY <FK_NAME_GETTED>;

-- DROP TABLE SBI_DATA_SET_HISTORY_OLD CASCADE;  
-- DROP TABLE SBI_DATA_SET_OLD CASCADE;
--  to do only after drop stmt
-- ALTER TABLE SBI_DATA_SET ADD CONSTRAINT FK_SBI_DATA_SET_T  FOREIGN KEY ( TRANSFORMER_ID ) REFERENCES SBI_DOMAINS ( VALUE_ID ) ON DELETE CASCADE;
-- ALTER TABLE SBI_DATA_SET ADD CONSTRAINT FK_SBI_DATA_SET_CAT  FOREIGN KEY (CATEGORY_ID) REFERENCES SBI_DOMAINS (VALUE_ID) ON DELETE CASCADE ON UPDATE RESTRICT;
-- ALTER TABLE SBI_DATA_SET ADD CONSTRAINT FK_SBI_DATA_SET_DS3 FOREIGN KEY ( DATA_SOURCE_PERSIST_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID ) ON DELETE CASCADE;
-- ALTER TABLE SBI_DATA_SET ADD CONSTRAINT FK_SBI_DATA_SET_DS4 FOREIGN KEY ( DATA_SOURCE_FLAT_ID ) REFERENCES SBI_DATA_SOURCE( DS_ID ) ON DELETE CASCADE;

--  insert records for selfservice dataset management 
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

UPDATE SBI_ENGINES SET USE_DATASET = TRUE WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.worksheet.WorksheetDriver';
commit;

ALTER TABLE SBI_DATA_SET ADD COLUMN OWNER VARCHAR(50);
ALTER TABLE SBI_DATA_SET ADD COLUMN IS_PUBLIC BOOLEAN DEFAULT FALSE;

UPDATE SBI_DATA_SET SET IS_PUBLIC = TRUE, OWNER = USER_IN;
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
'SPAGOBI.DATASET_FILE_MAX_SIZE', 'DATASET FILE MAX SIZE', 'Max size for a file used as a dataset', true, '10485760',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'GENERIC_CONFIGURATION', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;

UPDATE SBI_CONFIG SET VALUE_CHECK = 'dd/MM/yyyy HH:mm:ss' WHERE LABEL = 'SPAGOBI.TIMESTAMP-FORMAT.format';
commit;

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'fieldType','fieldType','DS_META_PROPERTY','Data Set Metadata Property','Data Set Metadata Property','biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT into SBI_DOMAINS (VALUE_ID,VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'type','type','DS_META_PROPERTY','Data Set Metadata Property','Data Set Metadata Property','biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

ALTER TABLE SBI_EXT_ROLES ADD COLUMN SEE_DOCUMENT_BROWSER BOOLEAN DEFAULT TRUE;
ALTER TABLE SBI_EXT_ROLES ADD COLUMN SEE_FAVOURITES 	BOOLEAN DEFAULT TRUE;
ALTER TABLE SBI_EXT_ROLES ADD COLUMN SEE_SUBSCRIPTIONS 	BOOLEAN DEFAULT TRUE;
ALTER TABLE SBI_EXT_ROLES ADD COLUMN SEE_MY_DATA 		BOOLEAN DEFAULT TRUE;
ALTER TABLE SBI_EXT_ROLES ADD COLUMN SEE_TODO_LIST 		BOOLEAN DEFAULT TRUE;
ALTER TABLE SBI_EXT_ROLES ADD COLUMN CREATE_DOCUMENTS 	BOOLEAN DEFAULT TRUE;

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

update SBI_ENGINES set label = 'SpagoBIGisEngine' where label = 'GeoReportEngine';
commit;
