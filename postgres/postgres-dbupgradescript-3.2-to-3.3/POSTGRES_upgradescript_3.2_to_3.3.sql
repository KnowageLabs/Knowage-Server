CREATE TABLE SBI_I18N_MESSAGES (
  LANGUAGE_CD INTEGER NOT NULL,
  LABEL VARCHAR(200) NOT NULL,
  MESSAGE TEXT,
  PRIMARY KEY (LANGUAGE_CD, LABEL)
);

ALTER TABLE SBI_I18N_MESSAGES ADD CONSTRAINT FK_SBI_I18N_MESSAGES FOREIGN KEY (LANGUAGE_CD) REFERENCES SBI_DOMAINS(VALUE_ID);


INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'ITA','Italian','LANG','language ISO Code','Italian', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'ENG','English','LANG','language ISO Code','English', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'FRA','French','LANG','language ISO Code','French', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'SPA','Spanish','LANG','language ISO Code','Spanish', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

CREATE TABLE SBI_KPI_COMMENTS (
	   KPI_COMMENT_ID 			INTEGER NOT NULL ,
	   KPI_INST_ID 	        INTEGER,
       BIN_ID 	            INTEGER,
       EXEC_REQ 	        VARCHAR(500),
       OWNER 	            VARCHAR(50),
       ISPUBLIC 	        BOOLEAN,  
       CREATION_DATE 	    TIMESTAMP NOT NULL,  
       LAST_CHANGE_DATE     TIMESTAMP NOT NULL, 
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
       CONSTRAINT XAK1SBI_KPI_COMMENT UNIQUE  (KPI_COMMENT_ID),
       PRIMARY KEY (KPI_COMMENT_ID)
) WITHOUT OIDS;

ALTER TABLE SBI_KPI_COMMENTS ADD CONSTRAINT FK_SBI_KPI_COMMENT_1 FOREIGN KEY  ( BIN_ID ) REFERENCES SBI_BINARY_CONTENTS(BIN_ID);
ALTER TABLE SBI_KPI_COMMENTS ADD CONSTRAINT FK_SBI_KPI_COMMENT_2 FOREIGN KEY  ( KPI_INST_ID ) REFERENCES SBI_KPI_INSTANCE (ID_KPI_INSTANCE);
