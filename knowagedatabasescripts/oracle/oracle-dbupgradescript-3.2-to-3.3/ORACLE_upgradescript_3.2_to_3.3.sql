CREATE TABLE SBI_I18N_MESSAGES (
  LANGUAGE_CD INTEGER NOT NULL,
  LABEL VARCHAR(200) NOT NULL,
  MESSAGE CLOB,
  PRIMARY KEY (LANGUAGE_CD, LABEL)
);

ALTER TABLE SBI_I18N_MESSAGES ADD CONSTRAINT FK_SBI_I18N_MESSAGES FOREIGN KEY (LANGUAGE_CD) REFERENCES SBI_DOMAINS(VALUE_ID);


INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'ITA','Italian','LANG','language ISO Code','Italian', 'biadmin', SYSDATE);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'ENG','English','LANG','language ISO Code','English', 'biadmin', SYSDATE);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'FRA','French','LANG','language ISO Code','French', 'biadmin', SYSDATE);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'SPA','Spanish','LANG','language ISO Code','Spanish', 'biadmin', SYSDATE);
update hibernate_sequences set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

CREATE TABLE SBI_KPI_COMMENTS (
	KPI_COMMENT_ID INTEGER NOT NULL ,
	KPI_INST_ID INTEGER NOT NULL ,
	BIN_ID INTEGER,
	EXEC_REQ VARCHAR2 (500),
	OWNER VARCHAR2 (50),
	ISPUBLIC SMALLINT,
	CREATION_DATE TIMESTAMP(6) NOT NULL ,
	LAST_CHANGE_DATE TIMESTAMP(6) NOT NULL ,
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
PRIMARY KEY (KPI_COMMENT_ID) 
);
ALTER TABLE SBI_KPI_COMMENTS ADD CONSTRAINT FK_SBI_KPI_COMMENT_1 FOREIGN KEY (BIN_ID) REFERENCES SBI_BINARY_CONTENTS (BIN_ID);
ALTER TABLE SBI_KPI_COMMENTS ADD CONSTRAINT FK_SBI_KPI_COMMENT_2 FOREIGN KEY (KPI_INST_ID) REFERENCES SBI_KPI_INSTANCE (ID_KPI_INSTANCE);

alter table SBI_ORG_UNIT_NODES drop constraint ( SELECT c.constraint_name
  FROM user_constraints c, user_cons_columns cc
  WHERE     c.constraint_name = cc.constraint_name
       AND c.table_name = 'SBI_ORG_UNIT_NODES'
       AND c.constraint_type = 'R'
       AND cc.column_name = 'HIERARCHY_ID' );
ALTER TABLE SBI_ORG_UNIT_NODES ADD CONSTRAINT FK_SBI_ORG_UNIT_NODES_2 FOREIGN KEY ( HIERARCHY_ID ) REFERENCES SBI_ORG_UNIT_HIERARCHIES ( ID ) ON DELETE CASCADE;
alter table SBI_ORG_UNIT_NODES drop constraint ( SELECT c.constraint_name
  FROM user_constraints c, user_cons_columns cc
  WHERE     c.constraint_name = cc.constraint_name
       AND c.table_name = 'SBI_ORG_UNIT_NODES'
       AND c.constraint_type = 'R'
       AND cc.column_name = 'PARENT_NODE_ID' );
ALTER TABLE SBI_ORG_UNIT_NODES ADD CONSTRAINT FK_SBI_ORG_UNIT_NODES_3 FOREIGN KEY ( PARENT_NODE_ID ) REFERENCES SBI_ORG_UNIT_NODES ( NODE_ID ) ON DELETE CASCADE;
