-- May work.. to be tested

DECLARE @tbldroplist TABLE(
TblName VARCHAR(255)
)

DECLARE @tblname VARCHAR(255)
DECLARE @cstrname VARCHAR(255)
DECLARE @droptablesql NVARCHAR(MAX)
DECLARE @dropfksql NVARCHAR(MAX)

INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT_GRANT')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ARTIFACTS_VERSIONS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ARTIFACTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_MODELS_VERSIONS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_MODELS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXT_ROLES_CATEGORY');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_COMMENTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT_GRANT_NODES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT_GRANT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT_NODES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT_HIERARCHIES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DATA_SET');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECTS_RATING');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_REMEMBER_ME');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DOSSIER_PRES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DOSSIER_BIN_TEMP');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DOSSIER_TEMP');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_AUDIT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EVENTS_ROLES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EVENTS_LOG');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EVENTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_SUBREPORTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_PARUSE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_PARVIEW');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARUSE_CK');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARUSE_DET');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARUSE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FUNC_ROLE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_FUNC');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FUNCTIONS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CHECKS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_PAR');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARAMETERS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECT_NOTES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECT_TEMPLATES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_BINARY_CONTENTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_SNAPSHOTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_SUBOBJECTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_USER_FUNC');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ROLE_TYPE_USER_FUNC');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_STATE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXT_ROLES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_LOV ');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ENGINES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DOMAINS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_MAP_FEATURES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_FEATURES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_MAPS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_LAYERS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_VIEWPOINTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DATA_SOURCE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_MENU_ROLE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_MENU');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DIST_LIST_OBJECTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DIST_LIST_USER');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DIST_LIST');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_MODEL_RESOURCES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_RESOURCES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_MODEL_INST');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_INST_PERIOD');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_INSTANCE_HISTORY');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_INSTANCE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_PERIODICITY');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_MODEL');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_THRESHOLD_VALUE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_THRESHOLD');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_MEASURE_UNIT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALARM_DISTRIBUTION');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALARM_CONTACT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALARM_EVENT');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALARM');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_VALUE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_ROLE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXPORTERS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_METACONTENTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_METADATA');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CONFIG');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_UDP_VALUE');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_UDP');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GOAL');  
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GOAL_HIERARCHY');  
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GOAL_KPI');  
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ACTIVITY_MONITORING'); 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ATTRIBUTE');  
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXT_USER_ROLES'); 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_DOCUMENTS'); 
INSERT INTO @tbldroplist (TblName) VALUES ('hibernate_sequences'); 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_ERROR'); 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_REL'); 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_USER'); 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_USER_ATTRIBUTES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PROGRESS_THREAD');  
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_I18N_MESSAGES');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORGANIZATIONS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ARTIFACTS_VERSIONS') ;
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ARTIFACTS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_MODELS_VERSIONS');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_META_MODELS  ');
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXT_ROLES_CATEGORY')  ;
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_COMMENTS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT_GRANT_NODES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT_GRANT')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT_NODES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT_HIERARCHIES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORG_UNIT')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DATA_SET')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECTS_RATING')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_REMEMBER_ME')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DOSSIER_PRES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DOSSIER_BIN_TEMP')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DOSSIER_TEMP')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_AUDIT')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EVENTS_ROLES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EVENTS_LOG')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EVENTS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_SUBREPORTS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_PARUSE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_PARVIEW')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARUSE_CK')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARUSE_DET')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARUSE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FUNC_ROLE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_FUNC')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_FUNCTIONS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CHECKS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_PAR')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PARAMETERS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECT_NOTES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECT_TEMPLATES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_BINARY_CONTENTS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_SNAPSHOTS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_SUBOBJECTS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_USER_FUNC')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ROLE_TYPE_USER_FUNC')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_STATE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJECTS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXT_ROLES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_LOV ')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ENGINES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DOMAINS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_MAP_FEATURES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_FEATURES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GEO_MAPS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_VIEWPOINTS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DATA_SOURCE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_MENU_ROLE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_MENU')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DIST_LIST_OBJECTS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DIST_LIST_USER')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_DIST_LIST')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_MODEL_RESOURCES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_RESOURCES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_MODEL_INST')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_INST_PERIOD')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_INSTANCE_HISTORY')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_INSTANCE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_PERIODICITY')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_MODEL')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_THRESHOLD_VALUE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_THRESHOLD')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_MEASURE_UNIT')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALARM_DISTRIBUTION')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALARM_CONTACT')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALARM_EVENT')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ALARM')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_VALUE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_ROLE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXPORTERS ')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_METACONTENTS ')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_OBJ_METADATA ')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CONFIG ')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_UDP_VALUE ')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_UDP ')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GOAL')  
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GOAL_HIERARCHY')  
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_GOAL_KPI')  
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ACTIVITY_MONITORING') 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ATTRIBUTE')  
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_EXT_USER_ROLES') 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_DOCUMENTS') 
INSERT INTO @tbldroplist (TblName) VALUES ('hibernate_sequences') 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_ERROR') 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_KPI_REL') 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_USER') 
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_USER_ATTRIBUTES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_PROGRESS_THREAD')  
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_I18N_MESSAGES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORGANIZATIONS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORGANIZATION_ENGINE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_ORGANIZATION_DATASOURCE')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_AUTHORIZATIONS')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_AUTHORIZATIONS_ROLES')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CACHE_JOINED_ITEM')
INSERT INTO @tbldroplist (TblName) VALUES ('SBI_CACHE_ITEM')

DECLARE tblcursor CURSOR FOR
 SELECT TblName FROM @tbldroplist

OPEN tblcursor 
FETCH NEXT FROM tblcursor INTO @tblname 

WHILE @@FETCH_STATUS = 0
BEGIN

	DECLARE cstrcursor CURSOR FOR
		select OBJECT_NAME(object_id) from sys.foreign_keys
			where OBJECT_NAME(parent_object_id) = @tblname

	OPEN cstrcursor 

	FETCH NEXT FROM cstrcursor  INTO @cstrname
		WHILE @@FETCH_STATUS = 0
		BEGIN
			PRINT 'Constraint' + @cstrname
			SET @dropfksql = 'ALTER TABLE ' + @tblname + 'DROP CONSTRAINT ' + @cstrname
			exec sp_executesql @dropfksql 
			SET @dropfksql = ''
			FETCH NEXT FROM cstrcursor  INTO @cstrname
		END
	CLOSE cstrcursor 
	DEALLOCATE cstrcursor 
	SET @droptablesql = 'DROP TABLE ' +  @tblname 
	exec sp_executesql @droptablesql 
	SET @droptablesql = ''
	FETCH NEXT FROM tblcursor INTO @tblname 
END
CLOSE tblcursor 
DEALLOCATE tblcursor 
GO
