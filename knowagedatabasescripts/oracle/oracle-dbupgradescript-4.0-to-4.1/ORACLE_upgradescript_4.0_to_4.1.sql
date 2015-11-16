CREATE TABLE  SBI_COMMUNITY(
  COMMUNITY_ID integer NOT NULL,
  NAME varchar2(200) NOT NULL,
  DESCRIPTION varchar2(350) DEFAULT NULL,
  OWNER varchar2(100) NOT NULL,
  FUNCT_CODE varchar2(40) DEFAULT NULL,
  CREATION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  LAST_CHANGE_DATE TIMESTAMP DEFAULT NULL,
  USER_IN varchar2(100) NOT NULL,
  USER_UP varchar2(100) DEFAULT NULL,
  USER_DE varchar2(100) DEFAULT NULL,
  TIME_IN TIMESTAMP DEFAULT NULL,
  TIME_UP timestamp DEFAULT NULL,
  TIME_DE timestamp DEFAULT NULL,
  SBI_VERSION_IN varchar2(10) DEFAULT NULL,
  SBI_VERSION_UP varchar2(10) DEFAULT NULL,
  SBI_VERSION_DE varchar2(10) DEFAULT NULL,
  META_VERSION varchar2(100) DEFAULT NULL,
  ORGANIZATION varchar2(20) DEFAULT NULL,
  PRIMARY KEY (COMMUNITY_ID)
);

CREATE TABLE SBI_COMMUNITY_USERS (
  COMMUNITY_ID integer NOT NULL,
  USER_ID varchar2(100) NOT NULL,
  CREATION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  LAST_CHANGE_DATE TIMESTAMP DEFAULT NULL,
  USER_IN varchar2(100) NOT NULL,
  USER_UP varchar2(100) DEFAULT NULL,
  USER_DE varchar2(100) DEFAULT NULL,
  TIME_IN TIMESTAMP DEFAULT NULL,
  TIME_UP timestamp  DEFAULT NULL,
  TIME_DE timestamp  DEFAULT NULL,
  SBI_VERSION_IN varchar2(10) DEFAULT NULL,
  SBI_VERSION_UP varchar2(10) DEFAULT NULL,
  SBI_VERSION_DE varchar2(10) DEFAULT NULL,
  META_VERSION varchar2(100) DEFAULT NULL,
  ORGANIZATION varchar2(20) DEFAULT NULL,
  PRIMARY KEY (COMMUNITY_ID,USER_ID),
  CONSTRAINT FK_COMMUNITY FOREIGN KEY (COMMUNITY_ID) REFERENCES SBI_COMMUNITY (COMMUNITY_ID) 
) ;

ALTER TABLE SBI_OBJECTS ADD PREVIEW_FILE VARCHAR2(100) NULL;

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
  	LAYER_ID 			 INTEGER NOT NULL,
  	LABEL 				 VARCHAR2(100) NOT NULL,
  	NAME 				 VARCHAR2(100),
  	DESCR 				 VARCHAR2(100),
  	LAYER_DEFINITION 	 BLOB NOT NULL,
  	TYPE 				 VARCHAR2(40),
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
  	CONSTRAINT LABEL_UNIQUE UNIQUE (LABEL,ORGANIZATION),
  	PRIMARY KEY (LAYER_ID)
) ;


INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) 
values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'FILE','FILE','LAYER_TYPE','Layer Type','Layer Type','server',sysdate);
UPDATE hibernate_sequences SET next_val = (SELECT MAX(VALUE_ID) + 1 FROM SBI_DOMAINS) WHERE sequence_name = 'SBI_DOMAINS';  

INSERT into SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) 
values ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),'WFS','WFS','LAYER_TYPE','Layer Type','Layer Type','server',sysdate);
UPDATE hibernate_sequences SET next_val = (SELECT MAX(VALUE_ID) + 1 FROM SBI_DOMAINS) WHERE sequence_name = 'SBI_DOMAINS';  

INSERT INTO SBI_USER_FUNC (USER_FUNCT_ID, NAME, DESCRIPTION, USER_IN, TIME_IN)
    VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_USER_FUNC'), 
    'GeoLayersManagement','GeoLayersManagement', 'server', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_USER_FUNC';

INSERT INTO SBI_ROLE_TYPE_USER_FUNC (ROLE_TYPE_ID, USER_FUNCT_ID)
    VALUES ((SELECT VALUE_ID FROM SBI_DOMAINS WHERE VALUE_CD = 'ADMIN' AND DOMAIN_CD = 'ROLE_TYPE'), 
    (SELECT USER_FUNCT_ID FROM SBI_USER_FUNC WHERE NAME = 'GeoLayersManagement'));
commit;


ALTER TABLE SBI_COMMUNITY ADD CONSTRAINT SBI_COMMUNITY_UK1 UNIQUE (NAME , ORGANIZATION ) ENABLE;

ALTER TABLE SBI_OBJECTS ADD (IS_PUBLIC INTEGER DEFAULT 0 );
UPDATE SBI_OBJECTS SET IS_PUBLIC = 1;

ALTER TABLE SBI_DATA_SET ADD PERSIST_TABLE_NAME VARCHAR2(50) NULL;

ALTER TABLE SBI_DATA_SET DROP COLUMN IS_FLAT_DATASET;
ALTER TABLE SBI_DATA_SET DROP COLUMN FLAT_TABLE_NAME;
ALTER TABLE SBI_DATA_SET DROP COLUMN DATA_SOURCE_FLAT_ID;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'Flat','SbiFlatDataSet','DATA_SET_TYPE','Data Set Type','SbiFlatDataSet', 'biadmin', current_timestamp);
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

ALTER TABLE SBI_GEO_LAYERS ADD (IS_BASE_LAYER INTEGER DEFAULT 0 );

ALTER TABLE SBI_ENGINES DROP COLUMN DEFAULT_DS_ID;

commit;
ALTER TABLE SBI_DATA_SOURCE ADD (READ_ONLY INTEGER DEFAULT 0 );
ALTER TABLE SBI_DATA_SOURCE ADD (WRITE_DEFAULT INTEGER DEFAULT 0 );
commit;

ALTER TABLE SBI_DATA_SET DROP COLUMN DATA_SOURCE_PERSIST_ID;
commit;

UPDATE SBI_CONFIG SET VALUE_CHECK = '' WHERE VALUE_CHECK = 'spagobi@eng.it';
commit;

ALTER TABLE SBI_SNAPSHOTS ADD CONTENT_TYPE VARCHAR2(300) NULL;

ALTER TABLE SBI_DATA_SET 	ADD CONSTRAINT FK_DATA_SET_CATEGORY FOREIGN KEY (CATEGORY_ID) 	   REFERENCES SBI_DOMAINS (VALUE_ID);
ALTER TABLE SBI_META_MODELS ADD CONSTRAINT FK_META_MODELS_CATEGORY FOREIGN KEY (CATEGORY_ID) REFERENCES SBI_DOMAINS (VALUE_ID);

ALTER TABLE SBI_RESOURCES MODIFY (RESOURCE_NAME VARCHAR2(200) );

DELETE FROM SBI_ROLE_TYPE_USER_FUNC WHERE ROLE_TYPE_ID IN (SELECT value_id FROM SBI_DOMAINS where domain_cd = 'ROLE_TYPE')
AND USER_FUNCT_ID =(SELECT user_funct_id FROM SBI_USER_FUNC where NAME = 'FinalUsersManagement');

ALTER TABLE SBI_OBJECTS MODIFY (LABEL VARCHAR2(200) );

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

ALTER TABLE SBI_DATA_SET ADD (SCOPE_ID INTEGER  );
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

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) 
 values ('gender','gender',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server',sysdate,'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('location','location',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate,'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('community','community',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate,'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('short_bio','short_bio',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate,'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

INSERT INTO SBI_ATTRIBUTE (attribute_name,description,attribute_id,user_in,time_in,sbi_version_in,organization) values ('language','language',(SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ATTRIBUTE'),'server_init',sysdate,'4.0','SPAGOBI');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ATTRIBUTE';
commit;

DELETE FROM SBI_PARUSE_CK WHERE CHECK_ID = (SELECT CHECK_ID FROM SBI_CHECKS WHERE value_Type_Cd = 'MANDATORY');
DELETE FROM SBI_CHECKS WHERE CHECK_ID = (SELECT CHECK_ID FROM SBI_CHECKS WHERE value_Type_Cd = 'MANDATORY');
COMMIT;

DELETE FROM SBI_USER_FUNC WHERE NAME = 'FinalUsersManagement';
COMMIT;