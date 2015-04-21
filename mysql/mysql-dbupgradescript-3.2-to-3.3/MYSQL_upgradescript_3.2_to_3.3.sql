CREATE TABLE SBI_I18N_MESSAGES (
  LANGUAGE_CD INTEGER NOT NULL,
  LABEL VARCHAR(200) NOT NULL,
  MESSAGE TEXT,
  PRIMARY KEY (LANGUAGE_CD, LABEL)
);

ALTER TABLE SBI_I18N_MESSAGES ADD CONSTRAINT FK_SBI_I18N_MESSAGES FOREIGN KEY (LANGUAGE_CD) REFERENCES SBI_DOMAINS(VALUE_ID) ON DELETE RESTRICT;


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

CREATE TABLE  SBI_KPI_COMMENTS(
  KPI_COMMENT_ID int(11) NOT NULL AUTO_INCREMENT,
  KPI_INST_ID int(11) DEFAULT NULL,
  BIN_ID int(11) DEFAULT NULL,
  EXEC_REQ varchar(500) DEFAULT NULL,
  OWNER varchar(50) DEFAULT NULL,
  ISPUBLIC tinyint(1) DEFAULT NULL,
  CREATION_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  LAST_CHANGE_DATE timestamp,
  USER_IN varchar(100) NOT NULL,
  USER_UP varchar(100) DEFAULT NULL,
  USER_DE varchar(100) DEFAULT NULL,
  TIME_IN timestamp,
  TIME_UP timestamp NULL DEFAULT NULL,
  TIME_DE timestamp NULL DEFAULT NULL,
  SBI_VERSION_IN varchar(10) DEFAULT NULL,
  SBI_VERSION_UP varchar(10) DEFAULT NULL,
  SBI_VERSION_DE varchar(10) DEFAULT NULL,
  META_VERSION varchar(100) DEFAULT NULL,
  ORGANIZATION varchar(20) DEFAULT NULL,
  PRIMARY KEY (KPI_COMMENT_ID),
  UNIQUE KEY XAK1SBI_KPI_COMMENT (KPI_COMMENT_ID),
  KEY FK_SBI_KPI_COMMENT_1 (BIN_ID),
  KEY FK_SBI_KPI_COMMENT_2 (KPI_INST_ID),
  CONSTRAINT FK_SBI_KPI_COMMENT_1 FOREIGN KEY (BIN_ID) REFERENCES SBI_BINARY_CONTENTS (BIN_ID),
  CONSTRAINT FK_SBI_KPI_COMMENT_2 FOREIGN KEY (KPI_INST_ID) REFERENCES SBI_KPI_INSTANCE (ID_KPI_INSTANCE)
);