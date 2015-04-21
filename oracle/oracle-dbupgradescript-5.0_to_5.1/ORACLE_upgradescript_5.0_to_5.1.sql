
ALTER TABLE SBI_OBJ_METADATA RENAME COLUMN DESCRIPTION TO DESCR;

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN, SBI_VERSION_IN) 
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'), 'SPAGOBI.SOCIAL_ANALYSIS_URL', 'SPAGOBI SOCIAL ANALYSIS URL', 'SPAGOBI SOCIAL ANALYSIS URL', 1, '/SpagoBISocialAnalysis/restful-services/start',(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'SOCIAL_CONFIGURATION', 'server', SYSDATE, '5.1');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN, SBI_VERSION_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'), 
'SPAGOBI.SOCIAL_ANALYSIS_IS_ACTIVE', 'SPAGOBI SOCIAL ANALYSIS STATUS', 'SPAGOBI SOCIAL ANALYSIS STATUS', 1, 'true',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'SOCIAL_CONFIGURATION', 'server', SYSDATE, '5.1');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;

INSERT INTO SBI_AUTHORIZATIONS
(ID, NAME, USER_IN, TIME_IN)
values ((SELECT NEXT_VAL FROM hibernate_sequences WHERE SEQUENCE_NAME='SBI_AUTHORIZATIONS'),
'HIERARCHIES_MANAGEMENT',
'server', SYSDATE) ;
commit;
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_AUTHORIZATIONS';
commit; 

INSERT INTO SBI_AUTHORIZATIONS
(ID, NAME,  USER_IN, TIME_IN) 
values ((SELECT NEXT_VAL FROM hibernate_sequences WHERE SEQUENCE_NAME='SBI_AUTHORIZATIONS'), 
'VIEW_SOCIAL_ANALYSIS',  'server', SYSDATE) ;
commit;
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_AUTHORIZATIONS';
commit;

INSERT INTO SBI_AUTHORIZATIONS
(ID, NAME, USER_IN, TIME_IN)
values ((SELECT NEXT_VAL FROM hibernate_sequences WHERE SEQUENCE_NAME='SBI_AUTHORIZATIONS'),
'ENABLE_DATASET_PERSISTENCE',
'server', SYSDATE) ;
commit;
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_AUTHORIZATIONS';
commit; 


INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'), 
'SPAGOBI.DATASET.PREVIEW_ROWS', 'Number of rows to show for dataset preview', 'Number of rows to show for dataset preview', 1, '1000',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'NUM' AND DOMAIN_CD = 'PAR_TYPE'), 'GENERIC_CONFIGURATION', 'biadmin', SYSDATE);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'),
'SPAGOBI.DATASET.PERSIST.TABLE_PREFIX', 'Table name prefix for dataset persistence', 'Table name prefix for dataset persistence', 1, 'D_',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'GENERIC_CONFIGURATION', 'biadmin', SYSDATE);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;