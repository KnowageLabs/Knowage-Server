DELETE FROM SBI_EVENTS_ROLES ;
DROP TABLE SBI_EVENTS_ROLES ;

ALTER TABLE SBI_CACHE_ITEM
ADD COLUMN PARAMETERS TEXT NULL DEFAULT NULL;

DELETE
FROM
	SBI_PRODUCT_TYPE_ENGINE
where
	ENGINE_ID in (
	select
		ENGINE_ID
	from
		SBI_ENGINES
	WHERE
		label in('knowagegisengine', 'knowageprocessengine', 'knowagesvgviewerengine','knowagewhatifengine')

);
DELETE FROM SBI_ENGINES WHERE label in('knowagegisengine', 'knowageprocessengine', 'knowagesvgviewerengine','knowagewhatifengine');

DELETE FROM SBI_GEO_MAP_FEATURES ;
DROP TABLE SBI_GEO_MAP_FEATURES ;

DELETE FROM SBI_GEO_MAPS ;
DROP TABLE SBI_GEO_MAPS ;

DELETE FROM SBI_GEO_FEATURES ;
DROP TABLE SBI_GEO_FEATURES ;

DELETE FROM SBI_COMMUNITY_USERS ;
DROP TABLE SBI_COMMUNITY_USERS ;

DELETE FROM SBI_COMMUNITY ;
DROP TABLE SBI_COMMUNITY ;

delete from SBI_DOMAINS where VALUE_CD = 'Ckan' and VALUE_NM = 'SbiCkanDataSet';

delete from SBI_ROLE_TYPE_USER_FUNC where USER_FUNCT_ID in (select USER_FUNCT_ID from SBI_USER_FUNC where name = 'CkanIntegrationFunctionality');
delete from SBI_USER_FUNC where name = 'CkanIntegrationFunctionality';

ALTER TABLE SBI_DATA_SET DROP COLUMN PIVOT_COLUMN;
ALTER TABLE SBI_DATA_SET DROP COLUMN PIVOT_ROW;
ALTER TABLE SBI_DATA_SET DROP COLUMN PIVOT_VALUE;
ALTER TABLE SBI_DATA_SET DROP COLUMN NUM_ROWS;

delete from SBI_DOMAINS sd where DOMAIN_CD = 'TRANSFORMER_TYPE' and VALUE_CD = 'PIVOT_TRANSFOMER';

DELETE FROM SBI_WHATIF_WORKFLOW ;
DROP TABLE SBI_WHATIF_WORKFLOW ;

ALTER TABLE SBI_CACHE_ITEM
MODIFY COLUMN NAME VARCHAR(50) NOT NULL;

ALTER TABLE SBI_DASHBOARD_THEME
ADD COLUMN IS_DEFAULT TINYINT(1) NOT NULL DEFAULT 0;

DELETE FROM SBI_AUTHORIZATIONS_ROLES sar WHERE sar.AUTHORIZATION_ID IN (SELECT id FROM SBI_AUTHORIZATIONS 
WHERE name IN ('MANAGE_KPI_VALUE','CREATE_SELF_SERVICE_KPI','ENABLE_FEDERATED_DATASET','CREATE_SELF_SERVICE_GEOREPORT'))
DELETE FROM SBI_AUTHORIZATIONS 
WHERE name IN ('MANAGE_KPI_VALUE','CREATE_SELF_SERVICE_KPI','ENABLE_FEDERATED_DATASET','CREATE_SELF_SERVICE_GEOREPORT')
COMMIT;

ALTER TABLE SBI_USER ADD COLUMN OTP_SECRET VARCHAR(100) NULL;
ALTER TABLE SBI_ORGANIZATIONS ADD COLUMN IS_MFA TINYINT(1) DEFAULT 0;