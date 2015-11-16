CREATE MEMORY TABLE SBI_PROGRESS_THREAD(PROGRESS_THREAD_ID INTEGER NOT NULL,USER_ID VARCHAR(100) NOT NULL,PARTIAL INTEGER, TOTAL INTEGER, FUNCTION_CD VARCHAR(200), STATUS  VARCHAR(4000), RANDOM_KEY VARCHAR(4000), TYPE VARCHAR(200), CONSTRAINT XAK1SBI_PROGRESS_THREAD PRIMARY KEY(PROGRESS_THREAD_ID));

insert into hibernate_sequences(next_val,sequence_name) values (1, 'SBI_PROGRESS_THREAD');

ALTER TABLE SBI_EXT_ROLES ADD COLUMN DO_MASSIVE_EXPORT BOOLEAN DEFAULT TRUE;

ALTER TABLE SBI_UDP_VALUE ALTER COLUMN VALUE VARCHAR NULL;

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, USER_IN, TIME_IN) VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'),'JNDI_THREAD_MANAGER', 'JNDI_THREAD_MANAGER', 'Jndi to build work manager', true, 'java:/comp/env/wm/SpagoWorkManager',(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'biadmin', current_timestamp);

update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';

commit;
delete from SBI_DOMAINS where VALUE_CD like '%MOBILE%';
COMMIT;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) 
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'MOBILE_REPORT','sbidomains.nm.mobile.report','BIOBJ_TYPE','BI Object types','sbidomains.ds.mobile.report', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_DOMAINS';	
INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) 
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'MOBILE_CHART','sbidomains.nm.mobile.chart','BIOBJ_TYPE','BI Object types','sbidomains.ds.mobile.chart', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_DOMAINS';		
INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) 
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'MOBILE_COCKPIT','sbidomains.nm.mobile.cockpit','BIOBJ_TYPE','BI Object types','sbidomains.ds.mobile.cockpit', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_DOMAINS';	
commit;


INSERT INTO SBI_ENGINES (ENGINE_ID,NAME, ENCRYPT, LABEL,MAIN_URL, DRIVER_NM, ENGINE_TYPE,BIOBJ_TYPE,USE_DATASOURCE,USE_DATASET, USER_IN, TIME_IN) VALUES
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ENGINES'),'Mobile Table Engine', 0, 'SpagoBITableMobileEn','/SpagoBIMobileEngine/servlet/AdapterHTTP?ACTION_NAME=MOBILE_ENGINE_START_ACTION','it.eng.spagobi.engines.drivers.mobile.report.MobileReportDriver',(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'ENGINE_TYPE' AND VALUE_CD = 'EXT'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'BIOBJ_TYPE' AND VALUE_CD = 'MOBILE_REPORT'),false, true, 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ENGINES';
commit;
INSERT INTO SBI_ENGINES (ENGINE_ID, NAME, ENCRYPT, LABEL,MAIN_URL, DRIVER_NM, ENGINE_TYPE,BIOBJ_TYPE,USE_DATASOURCE,USE_DATASET, USER_IN, TIME_IN) VALUES
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ENGINES'),'Mobile Chart Engine', 0, 'SpagoBIChartMobileEn','/SpagoBIMobileEngine/servlet/AdapterHTTP?ACTION_NAME=MOBILE_ENGINE_START_ACTION','it.eng.spagobi.engines.drivers.mobile.chart.MobileChartDriver',(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'ENGINE_TYPE' AND VALUE_CD = 'EXT'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'BIOBJ_TYPE' AND VALUE_CD = 'MOBILE_CHART'),false, true, 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ENGINES';
commit;
INSERT INTO SBI_ENGINES (ENGINE_ID, NAME, ENCRYPT, LABEL,MAIN_URL, DRIVER_NM, ENGINE_TYPE,BIOBJ_TYPE,USE_DATASOURCE,USE_DATASET, USER_IN, TIME_IN) VALUES
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ENGINES'),'Mobile Cockpit Engine', 0, 'SpagoBICockpitMobile','/SpagoBIMobileEngine/servlet/AdapterHTTP?ACTION_NAME=MOBILE_ENGINE_START_ACTION','it.eng.spagobi.engines.drivers.mobile.cockpit.MobileCockpitDriver',(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'ENGINE_TYPE' AND VALUE_CD = 'EXT'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'BIOBJ_TYPE' AND VALUE_CD = 'MOBILE_COCKPIT'),false, true, 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ENGINES';
commit;


INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) 
VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'CHART','Chart','BIOBJ_TYPE','BI Object types','sbidomains.ds.chart', 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_DOMAINS';	

INSERT INTO SBI_ENGINES (ENGINE_ID,NAME, ENCRYPT, LABEL,MAIN_URL, DRIVER_NM, ENGINE_TYPE,BIOBJ_TYPE,USE_DATASOURCE,USE_DATASET, USER_IN, TIME_IN) VALUES
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ENGINES'),'JS Chart Engine', 0, 'SpagoBIJSChartEngine','/SpagoBIChartEngine/servlet/AdapterHTTP','it.eng.spagobi.engines.drivers.chart.ChartDriver',(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'ENGINE_TYPE' AND VALUE_CD = 'EXT'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'BIOBJ_TYPE' AND VALUE_CD = 'CHART'),false, true, 'biadmin', current_timestamp);
update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_ENGINES';
commit;

INSERT INTO SBI_EXPORTERS (ENGINE_ID,DOMAIN_ID,DEFAULT_VALUE)values((SELECT ENGINE_ID FROM SBI_ENGINES WHERE LABEL='SpagoBIJSChartEngine'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'EXPORT_TYPE' AND VALUE_CD = 'PDF'), false);
INSERT INTO SBI_EXPORTERS (ENGINE_ID,DOMAIN_ID,DEFAULT_VALUE)values((SELECT ENGINE_ID FROM SBI_ENGINES WHERE LABEL='SpagoBIJSChartEngine'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'EXPORT_TYPE' AND VALUE_CD = 'JPG'), true);
commit;

UPDATE SBI_ENGINES SET MAIN_URL = '/SpagoBIConsoleEngine/servlet/AdapterHTTP', DRIVER_NM = 'it.eng.spagobi.engines.drivers.console.ConsoleDriver' WHERE LABEL = 'ConsoleEngine';
COMMIT;