alter table SBI_OBJ_METADATA CHANGE DESCRIPTION  DESCR  VARCHAR(100) ;

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN, SBI_VERSION_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'), 
'SPAGOBI.SOCIAL_ANALYSIS_URL', 'SPAGOBI SOCIAL ANALYSIS URL', 'SPAGOBI SOCIAL ANALYSIS URL', true, '/SpagoBISocialAnalysis/restful-services/start',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'SOCIAL_CONFIGURATION', 'server', current_timestamp, '5.1');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN, SBI_VERSION_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'), 
'SPAGOBI.SOCIAL_ANALYSIS_IS_ACTIVE', 'SPAGOBI SOCIAL ANALYSIS STATUS', 'SPAGOBI SOCIAL ANALYSIS STATUS', true, 'true',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'SOCIAL_CONFIGURATION', 'server', current_timestamp, '5.1');
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;

INSERT INTO SBI_AUTHORIZATIONS
(ID, NAME, USER_IN, TIME_IN)
values ((SELECT NEXT_VAL FROM hibernate_sequences WHERE SEQUENCE_NAME='SBI_AUTHORIZATIONS'),
'HIERARCHIES_MANAGEMENT',
'server', current_timestamp) ;
commit;
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_AUTHORIZATIONS';
commit; 


INSERT INTO SBI_AUTHORIZATIONS
(ID, NAME, USER_IN, TIME_IN) 
values ((SELECT NEXT_VAL FROM hibernate_sequences WHERE SEQUENCE_NAME='SBI_AUTHORIZATIONS'), 
'VIEW_SOCIAL_ANALYSIS', 
'server', current_timestamp) ;
commit;
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_AUTHORIZATIONS';
commit;

INSERT INTO SBI_AUTHORIZATIONS
(ID, NAME, USER_IN, TIME_IN)
values ((SELECT NEXT_VAL FROM hibernate_sequences WHERE SEQUENCE_NAME='SBI_AUTHORIZATIONS'),
'ENABLE_DATASET_PERSISTENCE',
'server', current_timestamp) ;
commit;
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_AUTHORIZATIONS';
commit; 


INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'), 
'SPAGOBI.DATASET.PREVIEW_ROWS', 'Number of rows to show for dataset preview', 'Number of rows to show for dataset preview', true, '1000',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'NUM' AND DOMAIN_CD = 'PAR_TYPE'), 'GENERIC_CONFIGURATION', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, CATEGORY, USER_IN, TIME_IN) VALUES
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'),
'SPAGOBI.DATASET.PERSIST.TABLE_PREFIX', 'Table name prefix for dataset persistence', 'Table name prefix for dataset persistence', true, 'D_',
(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'GENERIC_CONFIGURATION', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';
commit;