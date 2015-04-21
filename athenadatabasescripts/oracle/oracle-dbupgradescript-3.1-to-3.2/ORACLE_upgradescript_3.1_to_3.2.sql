CREATE TABLE SBI_OBJ_PARVIEW (
       OBJ_PAR_ID 			INTEGER NOT NULL,
   	   OBJ_PAR_FATHER_ID    INTEGER NOT NULL,
       OPERATION  			VARCHAR2(20) NOT NULL,
   	   COMPARE_VALUE  		VARCHAR2(200) NOT NULL,
   	   VIEW_LABEL   		VARCHAR2(50),
	     PROG INTEGER,
       USER_IN           	VARCHAR2(100) NOT NULL,
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
  PRIMARY KEY ( OBJ_PAR_ID ,  OBJ_PAR_FATHER_ID ,  OPERATION, COMPARE_VALUE )
);


CREATE INDEX XAK1SBI_PARVIEW ON SBI_OBJ_PARVIEW ( OBJ_PAR_ID ASC, OBJ_PAR_FATHER_ID ASC);


ALTER TABLE SBI_OBJ_PARVIEW ADD CONSTRAINT FK_SBI_OBJ_PARVIEW_1 FOREIGN KEY ( OBJ_PAR_ID ) REFERENCES SBI_OBJ_PAR ( OBJ_PAR_ID ) ;
ALTER TABLE SBI_OBJ_PARVIEW ADD CONSTRAINT FK_SBI_OBJ_PARVIEW_2 FOREIGN KEY ( OBJ_PAR_FATHER_ID ) REFERENCES SBI_OBJ_PAR ( OBJ_PAR_ID ) ;


ALTER TABLE sbi_data_set_history ADD CUSTOM_DATA CLOB;

update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
Commit;
INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'Custom','SbiCustomDataSet','DATA_SET_TYPE','Data Set Type','SbiCustomDataSet', 'biadmin', CURRENT_TIMESTAMP);
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
Commit;
Insert Into Sbi_Domains (Value_Id, Value_Cd,Value_Nm,Domain_Cd,Domain_Nm,Value_Ds, User_In, Time_In)
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),	'DATE_DEFAULT','sbidomains.nm.date.default','PAR_TYPE','Parameter type','sbidomains.ds.date.default', 'biadmin', SYSDATE);
commit;
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
Commit;
Insert Into Sbi_Domains (Value_Id, Value_Cd,Value_Nm,Domain_Cd,Domain_Nm,Value_Ds, User_In, Time_In)
	Values ((Select Next_Val From Hibernate_Sequences Where Sequence_Name = 'SBI_DOMAINS'),	'Day scale','Day scale','METRIC_SCALE_TYPE','Metric Scale Type','', 'biadmin', SYSDATE);
commit;
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
Commit;
	
--ALTER TABLE SBI_DATA_SET_HISTORY MODIFY ( DS_METADATA CLOB );
ALTER TABLE SBI_DATA_SET_HISTORY ADD DS_METADATA_TMP CLOB;
UPDATE SBI_DATA_SET_HISTORY DS1 SET DS_METADATA_TMP =  (SELECT DS_METADATA FROM  SBI_DATA_SET_HISTORY DS2  WHERE DS2.DS_H_ID = DS1.DS_H_ID);
ALTER TABLE SBI_DATA_SET_HISTORY DROP COLUMN DS_METADATA;
ALTER TABLE SBI_DATA_SET_HISTORY RENAME COLUMN DS_METADATA_TMP TO DS_METADATA;




UPDATE SBI_ENGINES SET USE_DATASET = 1 WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.worksheet.WorksheetDriver';

COMMIT;