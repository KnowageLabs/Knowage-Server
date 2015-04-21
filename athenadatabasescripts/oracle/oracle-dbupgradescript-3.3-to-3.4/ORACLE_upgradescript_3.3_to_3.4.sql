CREATE TABLE SBI_PROGRESS_THREAD (
       PROGRESS_THREAD_ID   INTEGER NOT NULL,
       USER_ID              VARCHAR2(100) NOT NULL,
       PARTIAL              INTEGER,
       TOTAL        	      INTEGER,
       FUNCTION_CD         VARCHAR2(200),
       STATUS              VARCHAR2(4000),
       RANDOM_KEY			VARCHAR2(4000),	
       TYPE              VARCHAR2(200),
              PRIMARY KEY (PROGRESS_THREAD_ID)
);

INSERT INTO hibernate_sequences(NEXT_VAL,SEQUENCE_NAME) VALUES (1, 'SBI_PROGRESS_THREAD');

ALTER TABLE SBI_EXT_ROLES ADD DO_MASSIVE_EXPORT SMALLINT DEFAULT 1;

--ALTER TABLE SBI_UDP_VALUE  MODIFY (VALUE NULL);

ALTER TABLE SBI_UDP_VALUE MODIFY (VALUE VARCHAR2(1000));

INSERT INTO SBI_CONFIG ( ID, LABEL, NAME, DESCRIPTION, IS_ACTIVE, VALUE_CHECK, VALUE_TYPE_ID, USER_IN, TIME_IN) VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_CONFIG'),'JNDI_THREAD_MANAGER', 'JNDI_THREAD_MANAGER', 'Jndi to build work manager', 1, 'java:/comp/env/wm/SpagoWorkManager',(select VALUE_ID from SBI_DOMAINS where VALUE_CD = 'STRING' AND DOMAIN_CD = 'PAR_TYPE'), 'biadmin', SYSDATE);

update hibernate_sequences set next_val = next_val+1 where sequence_name = 'SBI_CONFIG';


commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) 
  VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
  'MOBILE_REPORT','sbidomains.nm.mobile.report','BIOBJ_TYPE','BI Object types','sbidomains.ds.mobile.report', 'biadmin', CURRENT_TIMESTAMP);
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'MOBILE_CHART','sbidomains.nm.mobile.chart','BIOBJ_TYPE','BI Object types','sbidomains.ds.mobile.chart', 'biadmin', CURRENT_TIMESTAMP);
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN) 
  VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'), 
  'MOBILE_COCKPIT','sbidomains.nm.mobile.cockpit','BIOBJ_TYPE','BI Object types','sbidomains.ds.mobile.cockpit', 'biadmin', CURRENT_TIMESTAMP);	
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_ENGINES (ENGINE_ID,NAME, ENCRYPT, LABEL,MAIN_URL, DRIVER_NM, ENGINE_TYPE,BIOBJ_TYPE,USE_DATASOURCE,USE_DATASET, USER_IN, TIME_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ENGINES'),'Mobile Table Engine', 0, 'SpagoBITableMobileEn','/SpagoBIMobileEngine/servlet/AdapterHTTP?ACTION_NAME=MOBILE_ENGINE_START_ACTION','it.eng.spagobi.engines.drivers.mobile.report.MobileReportDriver',(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'ENGINE_TYPE' AND VALUE_CD = 'EXT'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'BIOBJ_TYPE' AND VALUE_CD = 'MOBILE_REPORT'),0, 1, 'biadmin', current_timestamp);
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_ENGINES';
commit;
INSERT INTO SBI_ENGINES (ENGINE_ID, NAME, ENCRYPT, LABEL,MAIN_URL, DRIVER_NM, ENGINE_TYPE,BIOBJ_TYPE,USE_DATASOURCE,USE_DATASET, USER_IN, TIME_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ENGINES'),'Mobile Chart Engine', 0, 'SpagoBIChartMobileEn','/SpagoBIMobileEngine/servlet/AdapterHTTP?ACTION_NAME=MOBILE_ENGINE_START_ACTION','it.eng.spagobi.engines.drivers.mobile.chart.MobileChartDriver',(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'ENGINE_TYPE' AND VALUE_CD = 'EXT'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'BIOBJ_TYPE' AND VALUE_CD = 'MOBILE_CHART'),0, 1, 'biadmin', current_timestamp);
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_ENGINES';
commit;
INSERT INTO SBI_ENGINES (ENGINE_ID, NAME, ENCRYPT, LABEL,MAIN_URL, DRIVER_NM, ENGINE_TYPE,BIOBJ_TYPE,USE_DATASOURCE,USE_DATASET, USER_IN, TIME_IN) VALUES 
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ENGINES'),'Mobile Cockpit Engine', 0, 'SpagoBICockpitMobile','/SpagoBIMobileEngine/servlet/AdapterHTTP?ACTION_NAME=MOBILE_ENGINE_START_ACTION','it.eng.spagobi.engines.drivers.mobile.cockpit.MobileCockpitDriver',(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'ENGINE_TYPE' AND VALUE_CD = 'EXT'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'BIOBJ_TYPE' AND VALUE_CD = 'MOBILE_COCKPIT'),0, 1, 'biadmin', current_timestamp);
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_ENGINES';
commit;



INSERT INTO SBI_DOMAINS (VALUE_ID, VALUE_CD,VALUE_NM,DOMAIN_CD,DOMAIN_NM,VALUE_DS, USER_IN, TIME_IN)
	VALUES ((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_DOMAINS'),
	'CHART','Chart','BIOBJ_TYPE','BI Object types','sbidomains.ds.chart', 'biadmin', CURRENT_TIMESTAMP);
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_DOMAINS';
commit;

INSERT INTO SBI_ENGINES (ENGINE_ID,NAME, ENCRYPT, LABEL,MAIN_URL, DRIVER_NM, ENGINE_TYPE,BIOBJ_TYPE,USE_DATASOURCE,USE_DATASET, USER_IN, TIME_IN) VALUES
((SELECT next_val FROM hibernate_sequences WHERE sequence_name = 'SBI_ENGINES'),'JS Chart Engine', 0, 'SpagoBIJSChartEngine','/SpagoBIChartEngine/servlet/AdapterHTTP','it.eng.spagobi.engines.drivers.chart.ChartDriver',(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'ENGINE_TYPE' AND VALUE_CD = 'EXT'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'BIOBJ_TYPE' AND VALUE_CD = 'CHART'),0, 1, 'biadmin', current_timestamp);
update HIBERNATE_SEQUENCES set next_val = next_val+1 where  sequence_name = 'SBI_ENGINES';
commit;

INSERT INTO SBI_EXPORTERS (ENGINE_ID,DOMAIN_ID,DEFAULT_VALUE)values((SELECT ENGINE_ID FROM SBI_ENGINES WHERE LABEL='SpagoBIJSChartEngine'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'EXPORT_TYPE' AND VALUE_CD = 'PDF'), 0);
INSERT INTO SBI_EXPORTERS (ENGINE_ID,DOMAIN_ID,DEFAULT_VALUE)values((SELECT ENGINE_ID FROM SBI_ENGINES WHERE LABEL='SpagoBIJSChartEngine'),(SELECT VALUE_ID FROM SBI_DOMAINS WHERE DOMAIN_CD = 'EXPORT_TYPE' AND VALUE_CD = 'JPG'), 1);

UPDATE SBI_ENGINES SET MAIN_URL = '/SpagoBIConsoleEngine/servlet/AdapterHTTP', DRIVER_NM = 'it.eng.spagobi.engines.drivers.console.ConsoleDriver' WHERE LABEL = 'ConsoleEngine';
COMMIT;