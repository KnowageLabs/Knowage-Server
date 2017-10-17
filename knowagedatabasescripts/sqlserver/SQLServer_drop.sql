DECLARE @tbldroplist TABLE(
TblName VARCHAR(255)
)

DECLARE @tblname VARCHAR(255)
DECLARE @cstrname VARCHAR(255)
DECLARE @droptablesql NVARCHAR(MAX)
DECLARE @dropfksql NVARCHAR(MAX)

INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORGANIZATION_PRODUCT_TYPE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PRODUCT_TYPE_ENGINE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PRODUCT_TYPE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_WORD');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_ATTRIBUTES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_WORD_ATTR');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_REFERENCES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_GLOSSARY');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_CONTENTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_WLIST');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_IMAGES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_DOCWLIST');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_WS_EVENT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_TIMESPAN');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ROLES_LAYERS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_VALUE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_RULE_PLACEHOLDER');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_KPI_RULE_OUTPUT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_RULE_OUTPUT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_THRESHOLD_VALUE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_THRESHOLD');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_PLACEHOLDER');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_TARGET');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_TARGET_VALUE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_KPI');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_RULE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_ALIAS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CROSS_NAVIGATION');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CROSS_NAVIGATION_PAR');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_BNESS_CLS_WLIST');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_DATASETWLIST');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GL_TABLE_WLIST');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OUTPUT_PARAMETER');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_JOB_TABLE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_JOB_SOURCE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_OBJ_DS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_DS_BC');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_BC_ATTRIBUTE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_TABLE_BC');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_BC');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_TABLE_COLUMN');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_TABLE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_SOURCE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_JOB');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_DATASET_REL');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_DOCUMENT_REL');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ARTIFACTS_VERSIONS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ARTIFACTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_MODELS_VERSIONS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_MODELS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXT_ROLES_CATEGORY');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DIST_LIST_USER');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DIST_LIST_OBJECTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DIST_LIST');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_REMEMBER_ME');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_AUDIT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EVENTS_ROLES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EVENTS_LOG');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EVENTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_MENU_ROLE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_MENU');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ACTIVITY_MONITORING');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_METACONTENTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_METADATA');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_SNAPSHOTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_SUBOBJECTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_STATE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECTS_RATING');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ROLE_TYPE_USER_FUNC');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_USER_FUNC');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_USER_ATTRIBUTES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ATTRIBUTE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_PARUSE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARUSE_CK');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARUSE_DET');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARUSE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FUNC_ROLE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_FUNC');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FUNCTIONS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CHECKS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_PARVIEW');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_PAR');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARAMETERS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_MAP_FEATURES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_FEATURES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_MAPS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_VIEWPOINTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_SUBREPORTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECT_NOTES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECT_TEMPLATES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_BINARY_CONTENTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_LOV');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXPORTERS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXT_USER_ROLES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXT_ROLES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_USER');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ENGINES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CONFIG');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_UDP_VALUE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_UDP');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DATA_SET');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DATA_SOURCE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_LAYERS ');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DOMAINS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PROGRESS_THREAD');
INSERT INTO @tbldroplist (TblName) VALUES ('hibernate_sequences');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_I18N_MESSAGES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORGANIZATIONS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_AUTHORIZATIONS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_AUTHORIZATIONS_ROLES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORGANIZATION_DATASOURCE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CACHE_ITEM');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_COMMUNITY');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_COMMUNITY_USERS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_DATA_SET');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_TRIGGER_PAUSED');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DATA_SET_FEDERATION');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FEDERATION_DEFINITION');

INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_EXECUTION_FILTER');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_EXECUTION_KPI');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_EXECUTION');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_SCORECARD_KPI');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_SCORECARD');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_VALUE_EXEC_LOG');

INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALERT_ACTION');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALERT_LISTENER');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALERT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALERT_LOG');

INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FUNCTION_OUTPUT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FUNCTION_INPUT_DATASET');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FUNCTION_INPUT_VARIABLE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CATALOG_FUNCTION');

INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_FUNC_ORGANIZER');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FUNCTIONS_ORGANIZER');

INSERT INTO @tbldroplist (TblName) VALUES ('SBI_WHATIF_WORKFLOW');

INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DOSSIER_ACTIVITY');


DECLARE tblcursor CURSOR FOR
SELECT TblName FROM @tbldroplist

OPEN tblcursor 
FETCH NEXT FROM tblcursor INTO @tblname 

WHILE @@FETCH_STATUS = 0
BEGIN

	DECLARE cstrcursor CURSOR FOR
		SELECT OBJECT_NAME(object_id) FROM sys.foreign_keys
		WHERE OBJECT_NAME(parent_object_id) = @tblname

	OPEN cstrcursor 

	FETCH NEXT FROM cstrcursor  INTO @cstrname
	
	WHILE @@FETCH_STATUS = 0
	BEGIN
		PRINT 'Dropping constraint ' + @cstrname + ' on table ' + @tblname + '...'
		SET @dropfksql = 'ALTER TABLE ' + @tblname + ' DROP CONSTRAINT ' + @cstrname
		exec sp_executesql @dropfksql 
		SET @dropfksql = ''
		FETCH NEXT FROM cstrcursor  INTO @cstrname
	END
	
	CLOSE cstrcursor 
	DEALLOCATE cstrcursor 
	
--	PRINT 'Dropping table ' + @tblname + '...'
--	SET @droptablesql = 'DROP TABLE ' +  @tblname 
--	exec sp_executesql @droptablesql
--	SET @droptablesql = ''
	
	FETCH NEXT FROM tblcursor INTO @tblname 
END
CLOSE tblcursor 
DEALLOCATE tblcursor

DECLARE tblcursor CURSOR FOR
SELECT TblName FROM @tbldroplist

OPEN tblcursor 
FETCH NEXT FROM tblcursor INTO @tblname 

WHILE @@FETCH_STATUS = 0
BEGIN

	PRINT 'Dropping table ' + @tblname + '...'
	SET @droptablesql = 'DROP TABLE ' +  @tblname 
	exec sp_executesql @droptablesql
	SET @droptablesql = ''
	
	FETCH NEXT FROM tblcursor INTO @tblname 
END
CLOSE tblcursor 
DEALLOCATE tblcursor 

GO
