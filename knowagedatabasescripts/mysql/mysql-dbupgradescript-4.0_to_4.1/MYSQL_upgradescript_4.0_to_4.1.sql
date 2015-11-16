CREATE TABLE  SBI_COMMUNITY(
  COMMUNITY_ID int(11) NOT NULL AUTO_INCREMENT,
  NAME varchar(200) NOT NULL,
  DESCRIPTION varchar(350) DEFAULT NULL,
  OWNER char(100) NOT NULL,
  FUNCT_CODE varchar(40) DEFAULT NULL,
  CREATION_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  LAST_CHANGE_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  USER_IN varchar(100) NOT NULL,
  USER_UP varchar(100) DEFAULT NULL,
  USER_DE varchar(100) DEFAULT NULL,
  TIME_IN timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  TIME_UP timestamp NULL DEFAULT NULL,
  TIME_DE timestamp NULL DEFAULT NULL,
  SBI_VERSION_IN varchar(10) DEFAULT NULL,
  SBI_VERSION_UP varchar(10) DEFAULT NULL,
  SBI_VERSION_DE varchar(10) DEFAULT NULL,
  META_VERSION varchar(100) DEFAULT NULL,
  ORGANIZATION varchar(20) DEFAULT NULL,
  PRIMARY KEY (COMMUNITY_ID)
);

CREATE TABLE SBI_COMMUNITY_USERS (
  COMMUNITY_ID int(11) NOT NULL,
  USER_ID char(100) NOT NULL,
  CREATION_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  LAST_CHANGE_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  USER_IN varchar(100) NOT NULL,
  USER_UP varchar(100) DEFAULT NULL,
  USER_DE varchar(100) DEFAULT NULL,
  TIME_IN timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  TIME_UP timestamp NULL DEFAULT NULL,
  TIME_DE timestamp NULL DEFAULT NULL,
  SBI_VERSION_IN varchar(10) DEFAULT NULL,
  SBI_VERSION_UP varchar(10) DEFAULT NULL,
  SBI_VERSION_DE varchar(10) DEFAULT NULL,
  META_VERSION varchar(100) DEFAULT NULL,
  ORGANIZATION varchar(20) DEFAULT NULL,
  PRIMARY KEY (`COMMUNITY_ID`,`USER_ID`),
  CONSTRAINT `FK_COMMUNITY` FOREIGN KEY (`COMMUNITY_ID`) REFERENCES `SBI_COMMUNITY` (`COMMUNITY_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ;

ALTER TABLE SBI_OBJECTS ADD COLUMN PREVIEW_FILE VARCHAR(100);

INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'MeasuresCatalogueManagement','MeasuresCatalogueManagement', 'server', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'MeasuresCatalogueManagement'));
commit;

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'CreateWorksheetFromDatasetUserFunctionality'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'DEV_ROLE' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'CreateWorksheetFromDatasetUserFunctionality'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'TEST_ROLE' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'CreateWorksheetFromDatasetUserFunctionality'));
INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'MODEL_ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'CreateWorksheetFromDatasetUserFunctionality'));
commit;


CREATE TABLE SBI_GEO_LAYERS (
  LAYER_ID int(11) NOT NULL,
  LABEL varchar(100) NOT NULL,
  NAME varchar(100),
  DESCR varchar(100),
  LAYER_DEFINITION BLOB NOT NULL,
  TYPE varchar(40),
  USER_IN varchar(100) NOT NULL,
  USER_UP varchar(100) DEFAULT NULL,
  USER_DE varchar(100) DEFAULT NULL,
  TIME_IN timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  TIME_UP timestamp NULL DEFAULT NULL,
  TIME_DE timestamp NULL DEFAULT NULL,
  SBI_VERSION_IN varchar(10) DEFAULT NULL,
  SBI_VERSION_UP varchar(10) DEFAULT NULL,
  SBI_VERSION_DE varchar(10) DEFAULT NULL,
  META_VERSION varchar(100) DEFAULT NULL,
  ORGANIZATION varchar(20) DEFAULT NULL,
  UNIQUE LABEL_UNIQUE (LABEL,ORGANIZATION),
  PRIMARY KEY (`LAYER_ID`)
) ;


INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN) 
values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'FILE','FILE','LAYER_TYPE','Layer Type','Layer Type','');
UPDATE hibernate_sequences SET next_val = (SELECT MAX(VALUE_ID) + 1 FROM SBI_DOMAINS) WHERE sequence_name = 'SBI_DOMAINS';  

INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN) 
values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'WFS','WFS','LAYER_TYPE','Layer Type','Layer Type','');
UPDATE hibernate_sequences SET next_val = (SELECT MAX(VALUE_ID) + 1 FROM SBI_DOMAINS) WHERE sequence_name = 'SBI_DOMAINS';  

INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'GeoLayersManagement','GeoLayersManagement', 'server', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'GeoLayersManagement'));
commit;


ALTER TABLE SBI_COMMUNITY ADD UNIQUE INDEX NAME_UNIQUE (ORGANIZATION, NAME ASC) ; 

ALTER TABLE SBI_OBJECTS ADD COLUMN IS_PUBLIC BOOLEAN DEFAULT FALSE;
UPDATE SBI_OBJECTS SET IS_PUBLIC = TRUE;

ALTER TABLE SBI_DATA_SET ADD COLUMN PERSIST_TABLE_NAME VARCHAR(50);

ALTER TABLE SBI_DATA_SET DROP COLUMN IS_FLAT_DATASET;
ALTER TABLE SBI_DATA_SET DROP COLUMN FLAT_TABLE_NAME;
ALTER TABLE SBI_DATA_SET DROP COLUMN DATA_SOURCE_FLAT_ID;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'Flat','SbiFlatDataSet','DATA_SET_TYPE','Data Set Type','SbiFlatDataSet', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

ALTER TABLE SBI_GEO_LAYERS ADD COLUMN IS_BASE_LAYER BOOLEAN DEFAULT FALSE;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('sesso','sesso',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('data_nascita','data nascita',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('indirizzo','indirizzo',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('azienda','azienda',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('biografia','biografia',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('lingua','lingua',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

ALTER TABLE SBI_DATA_SET MODIFY COLUMN ORGANIZATION VARCHAR(20) NOT NULL,
 DROP PRIMARY KEY,
 ADD PRIMARY KEY  (DS_ID, VERSION_NUM, ORGANIZATION);
 
 ALTER TABLE SBI_ENGINES DROP FOREIGN KEY FK_SBI_ENGINE_3;
ALTER TABLE SBI_ENGINES DROP COLUMN DEFAULT_DS_ID;
commit;

ALTER TABLE SBI_DATA_SOURCE ADD COLUMN READ_ONLY BOOLEAN DEFAULT FALSE;
ALTER TABLE SBI_DATA_SOURCE ADD COLUMN WRITE_DEFAULT BOOLEAN DEFAULT FALSE;
commit;

ALTER TABLE SBI_DATA_SET DROP COLUMN DATA_SOURCE_PERSIST_ID;
commit;

UPDATE SBI_CONFIG SET VALUE_CHECK = '' WHERE VALUE_CHECK = 'spagobi@eng.it';
commit;
 
ALTER TABLE SBI_SNAPSHOTS ADD COLUMN CONTENT_TYPE VARCHAR(300) NULL DEFAULT NULL  AFTER ORGANIZATION ;

ALTER TABLE SBI_DATA_SET ADD CONSTRAINT FK_DATA_SET_CATEGORY FOREIGN KEY (CATEGORY_ID) REFERENCES SBI_DOMAINS (VALUE_ID);
ALTER TABLE SBI_META_MODELS ADD CONSTRAINT FK_META_MODELS_CATEGORY FOREIGN KEY (CATEGORY_ID) REFERENCES SBI_DOMAINS (VALUE_ID);

ALTER TABLE SBI_RESOURCES MODIFY COLUMN RESOURCE_NAME VARCHAR(200);

DELETE FROM SBI_ROLE_TYPE_USER_FUNC WHERE ROLE_TYPE_ID IN (SELECT value_id FROM SBI_DOMAINS where domain_cd = 'ROLE_TYPE')
AND USER_FUNCT_ID =(SELECT user_funct_id FROM SBI_USER_FUNC where NAME = 'FinalUsersManagement');

ALTER TABLE SBI_OBJECTS MODIFY COLUMN LABEL VARCHAR(100);

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
    'USER','User','DS_SCOPE','Dataset scope','Dataset scope', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
    'TECHNICAL','Technical','DS_SCOPE','Dataset scope','Dataset scope', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
    'ENTERPRISE','Enterprise','DS_SCOPE','Dataset scope','Dataset scope', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

ALTER TABLE SBI_DATA_SET ADD COLUMN SCOPE_ID INT(11) NULL DEFAULT NULL;
ALTER TABLE SBI_DATA_SET ADD CONSTRAINT FK_SBI_DOMAINS_2 FOREIGN KEY ( SCOPE_ID ) REFERENCES SBI_DOMAINS( VALUE_ID ) ON DELETE CASCADE;

UPDATE SBI_DATA_SET
       SET SCOPE_ID =
                 CASE
                   WHEN OWNER IN (SELECT 
						U.USER_ID
						FROM 
						SBI_USER U,
						SBI_EXT_USER_ROLES R,
						SBI_EXT_ROLES RO
						WHERE OWNER = U.USER_ID
						AND R.ID = U.ID
						AND RO.EXT_ROLE_ID = R.EXT_ROLE_ID
						AND RO.ROLE_TYPE_CD IN ('ADMIN', 'DEV_ROLE')
						AND IS_PUBLIC = 0) THEN (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD='TECHNICAL' AND DOMAIN_CD='DS_SCOPE')
                   WHEN OWNER IN (SELECT 
						U.USER_ID
						FROM 
						SBI_USER U,
						SBI_EXT_USER_ROLES R,
						SBI_EXT_ROLES RO
						WHERE OWNER = U.USER_ID
						AND R.ID = U.ID
						AND RO.EXT_ROLE_ID = R.EXT_ROLE_ID
						AND RO.ROLE_TYPE_CD IN ('ADMIN', 'DEV_ROLE')
						AND IS_PUBLIC = 1) THEN (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD='ENTERPRISE' AND DOMAIN_CD='DS_SCOPE')
                   WHEN OWNER IN (SELECT 
						U.USER_ID
						FROM 
						SBI_USER U,
						SBI_EXT_USER_ROLES R,
						SBI_EXT_ROLES RO
						WHERE OWNER = U.USER_ID
						AND R.ID = U.ID
						AND RO.EXT_ROLE_ID = R.EXT_ROLE_ID
						AND RO.ROLE_TYPE_CD ='USER') THEN (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD='USER' AND DOMAIN_CD='DS_SCOPE')
                   ELSE (SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD='TECHNICAL' AND DOMAIN_CD='DS_SCOPE')
                 END;
                 
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('gender','gender',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('location','location',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('community','community',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('short_bio','short_bio',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('language','language',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate(),'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

DELETE FROM SBI_PARUSE_CK WHERE CHECK_ID = (SELECT CHECK_ID FROM SBI_CHECKS WHERE value_Type_Cd = 'MANDATORY');
DELETE FROM SBI_CHECKS WHERE value_Type_Cd = 'MANDATORY';
COMMIT;

DELETE FROM SBI_USER_FUNC WHERE NAME = 'FinalUsersManagement';
COMMIT;
