CREATE TABLE SBI_OBJ_PARVIEW (
  OBJ_PAR_ID INTEGER NOT NULL,
   OBJ_PAR_FATHER_ID  INTEGER NOT NULL,
   OPERATION  VARCHAR(20) NOT NULL,
   COMPARE_VALUE  VARCHAR(200) NOT NULL,
   VIEW_LABEL  VARCHAR(50),
   PROG INTEGER,
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
  PRIMARY KEY ( OBJ_PAR_ID ,  OBJ_PAR_FATHER_ID ,  OPERATION, COMPARE_VALUE )
);


ALTER TABLE SBI_OBJ_PARVIEW ADD CONSTRAINT FK_SBI_OBJ_PARVIEW_1 FOREIGN KEY ( OBJ_PAR_ID ) REFERENCES SBI_OBJ_PAR ( OBJ_PAR_ID ) ON DELETE RESTRICT;
ALTER TABLE SBI_OBJ_PARVIEW ADD CONSTRAINT FK_SBI_OBJ_PARVIEW_2 FOREIGN KEY ( OBJ_PAR_FATHER_ID ) REFERENCES SBI_OBJ_PAR ( OBJ_PAR_ID ) ON DELETE RESTRICT;


ALTER TABLE sbi_data_set_history ADD COLUMN CUSTOM_DATA TEXT;

update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;
INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'Custom','SbiCustomDataSet','DATA_SET_TYPE','Data Set Type','SbiCustomDataSet', 'biadmin', CURRENT_TIMESTAMP);
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;
INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),	'DATE_DEFAULT','sbidomains.nm.date.default','PAR_TYPE','Parameter type','sbidomains.ds.date.default', 'biadmin', CURRENT_TIMESTAMP);
commit;
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;
INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),	'Day scale','Day scale','METRIC_SCALE_TYPE','Metric Scale Type','', 'biadmin', CURRENT_TIMESTAMP);
commit;			
ALTER TABLE sbi_data_set_history ALTER COLUMN DS_METADATA TYPE TEXT;

UPDATE SBI_ENGINES SET USE_DATASET = TRUE WHERE DRIVER_NM = 'it.eng.spagobi.engines.drivers.worksheet.WorksheetDriver';