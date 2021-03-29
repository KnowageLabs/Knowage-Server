/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.commons.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines all SpagoBI's constants.
 */
public class SpagoBIConstants {

	public static final String TENANT_ID = "TENANT_ID";
	public static final String SBI_ENTITY = "SBI_ENTITY";
	public static final String PAGE = "PAGE";
	public static final String ACTION_NAME = "ACTION_NAME";
	public static final String URL = "URL";
	public static final String OBJECT = "OBJECT";
	public static final String OBJECT_ID = "OBJECT_ID";
	public static final String OBJECT_LABEL = "OBJECT_LABEL";
	public static final String EXECUTION_ROLE = "SBI_EXECUTION_ROLE";
	public static final String SBI_LANGUAGE = "SBI_LANGUAGE";
	public static final String SBI_COUNTRY = "SBI_COUNTRY";
	public static final String EXECUTE_DOCUMENT_ACTION = "EXECUTE_DOCUMENT_ACTION";
	public static final String SBI_ENVIRONMENT = "SBI_ENVIRONMENT";

	public static final String EXECUTION_MODALITY = "EXECUTION_MODALITY";
	public static final String TEST_MODALITY = "TEST_MODALITY";
	public static final String DEVELOPMENT_MODALITY = "DEVELOPMENT_MODALITY";
	public static final String ADMIN_MODALITY = "ADMIN_MODALITY";
	public static final String OBJECT_TREE_MODALITY = "OBJECT_TREE_MODALITY";

	public static final String IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS = "IGNORE_SUBOBJECTS_VIEWPOINTS_SNAPSHOTS";

	public static final String RUN_ANYWAY = "RUN_ANYWAY";

	public static final String FILE_METADATA_TYPE_CODE = "FILE";

	/*
	 * public static final String ACTOR = "ACTOR"; public static final String TESTER_ACTOR = "TESTER_ACTOR"; public static final String USER_ACTOR =
	 * "USER_ACTOR"; public static final String ADMIN_ACTOR = "ADMIN_ACTOR"; public static final String DEV_ACTOR = "DEV_ACTOR";
	 */
	public static final String USE_PUBLIC_USER = "SPAGOBI.SECURITY.USE_PUBLIC_USER";
	public static final String PUBLIC_USER_ID = "public_user";

	public static final String ADMIN_ROLE_TYPE = "ADMIN";
	public static final String ROLE_TYPE_ADMIN = "ACTOR";
	public static final String ROLE_TYPE_DEV = "DEV_ROLE";
	public static final String ROLE_TYPE_TEST = "TEST_ROLE";
	public static final String ROLE_TYPE_MODEL_ADMIN = "MODEL_ADMIN";
	public static final String ROLE_TYPE_USER = "USER";

	public static final String OPERATION = "OPERATION";
	public static final String FUNCTIONALITIES_OPERATION = "FUNCTIONALITIES_OPERATION";
	public static final String FUNCTIONALITIES_LIST = "FUNCTIONALITIES_LIST";
	public static final String FUNCTIONALITY_ID = "FUNCTIONALITY_ID";

	public static final String OBJECTS_VIEW = "OBJECTS_VIEW";
	public static final String VIEW_OBJECTS_AS_LIST = "VIEW_OBJECTS_AS_LIST";
	public static final String VIEW_OBJECTS_AS_TREE = "VIEW_OBJECTS_AS_TREE";

	public static final String LIST_PAGE = "LIST_PAGE";

	public static final String DISTRIBUTED_MAP_FOR_CACHE = "CACHE";
	public static final String DISTRIBUTED_MAP_FOR_ASSOCIATION = "ASSOCIATION";
	public static final String DISTRIBUTED_MAP_INSTANCE_NAME = "KNOWAGE";
	public static final String DISTRIBUTED_MAP_FOR_LICENSE = "LICENSE";
	public static final String DISTRIBUTED_MAP_FOR_SIGNUP = "SIGNUP";

	public static final String LICENSE_PATH_SUFFIX = "lic";

	public static final String MODALITY = "MODALITY";
	public static final String MESSAGEDET = "MESSAGEDET";

	public static final String DEFAULT_BUNDLE = "messages";
	public static final String DEFAULT_USER_BUNDLE = "user_messages";

	public static final String DETAIL_SELECT = "DETAIL_SELECT";
	public static final String METADATA_SELECT = "METADATA_SELECT";
	public static final String DETAIL_SUBSC = "DETAIL_SUBSC";
	public static final String DETAIL_UNSUBSC = "DETAIL_UNSUBSC";
	public static final String DETAIL_INSERTEMAIL = "DETAIL_INSERTEMAIL";
	public static final String DETAIL_NEW = "DETAIL_NEW";
	public static final String DETAIL_MOD = "DETAIL_MOD";
	public static final String DETAIL_INS = "DETAIL_INS";
	public static final String DETAIL_DEL = "DETAIL_DEL";
	public static final String DETAIL_INS_WIZARD_QUERY = "DETAIL_INS_WIZARD_QUERY";
	public static final String DETAIL_MOD_WIZARD_QUERY = "DETAIL_MOD_WIZARD_QUERY";
	public static final String DETAIL_VIEW_WIZARD_QUERY = "DETAIL_VIEW_WIZARD_QUERY";
	public static final String DETAIL_INS_WIZARD_SCRIPT = "DETAIL_INS_WIZARD_SCRIPT";
	public static final String DETAIL_MOD_WIZARD_SCRIPT = "DETAIL_MOD_WIZARD_SCRIPT";
	public static final String DETAIL_VIEW_WIZARD_SCRIPT = "DETAIL_VIEW_WIZARD_SCRIPT";
	public static final String DETAIL_VIEW_WIZARD_SCRIPT_AFTER_TEST = "DETAIL_VIEW_WIZARD_SCRIPT_AFTER_TEST";
	public static final String DETAIL_ADD_WIZARD_LOV = "DETAIL_ADD_WIZARD_LOV";
	public static final String DETAIL_DEL_WIZARD_LOV = "DETAIL_DEL_WIZARD_LOV";
	public static final String DETAIL_INS_WIZARD_FIX_LOV = "DETAIL_INS_WIZARD_FIX_LOV";
	public static final String DETAIL_MOD_WIZARD_FIX_LOV = "DETAIL_MOD_WIZARD_FIX_LOV";
	public static final String DETAIL_VIEW_WIZARD_FIX_LOV = "DETAIL_VIEW_WIZARD_FIX_LOV";

	public static final String LIST_INPUT_TYPE = "LIST_INPUT_TYPE";
	public static final String MODALITY_VALUE_OBJECT = "MODALITY_VALUE_OBJ";

	public static final String WIZARD = "WIZARD";
	public static final String WIZARD_QUERY = "WIZARD_QUERY";
	public static final String WIZARD_FIX_LOV = "WIZARD_FIX_LOV";
	public static final String WIZARD_SCRIPT = "WIZARD_SCRIPT";

	public static final String ID_MODALITY_VALUE = "ID_MODALITY_VALUE";

	public static final String NAME_MODULE = "SpagoBI";

	public static final String TYPE_FILTER = "typeFilter";
	public static final String VALUE_FILTER = "valueFilter";
	public static final String COLUMN_FILTER = "columnFilter";
	public static final String COLUMNS_FILTER = "columnsFilter";

	public static final String TYPE_VALUE_FILTER = "typeValueFilter";
	public static final String SCOPE = "scope";
	public static final String START_FILTER = "start";
	public static final String END_FILTER = "end";
	public static final String EQUAL_FILTER = "equal";
	public static final String CONTAIN_FILTER = "contains";
	public static final String NOT_CONTAIN_FILTER = "notcontains";
	public static final String LESS_FILTER = "less";
	public static final String LESS_OR_EQUAL_FILTER = "lessequal";
	public static final String GREATER_FILTER = "greater";
	public static final String GREATER_OR_EQUAL_FILTER = "greaterequal";
	// public static final String LESS_FILTER_TEXT = "less";
	// public static final String LESS_OR_EQUAL_FILTER_TEXT = "lessequal";
	// public static final String GREATER_FILTER_TEXT = "greater";
	// public static final String GREATER_OR_EQUAL_FILTER_TEXT = "greaterequal";
	public static final String NUMBER_TYPE_FILTER = "NUM";
	public static final String STRING_TYPE_FILTER = "STRING";
	public static final String DATE_TYPE_FILTER = "DATE";
	public static final String DOCUMENT_WIDGET_USE = "DocumentWidgetUse";

	// DATE RANGE

	public static final String LESS_BEGIN_FILTER = "lessbegin";
	public static final String LESS_END_FILTER = "lessend";
	public static final String LESS_OR_EQUAL_BEGIN_FILTER = "lesseqbg";
	public static final String LESS_OR_EQUAL_END_FILTER = "lesseqend";
	public static final String GREATER_BEGIN_FILTER = "gtr_bg";
	public static final String GREATER_END_FILTER = "gtr_end";
	public static final String GREATER_OR_EQUAL_BEGIN_FILTER = "gtr_eq_bg";
	public static final String GREATER_OR_EQUAL_END_FILTER = "gtr_eq_end";
	public static final String IN_RANGE_FILTER = "inrange";
	public static final String NOT_IN_RANGE_FILTER = "notinrange";

	public static final String ERASE_VERSION = "ERASE_VERSION";

	public static final String VERSION = "VERSION";
	public static final String PATH = "PATH";
	public static final String PARAMETERS = "PARAMETERS";
	public static final String PARAMETER_TYPE = "_PARAM_TYPE";

	public static final String PROFILE_ATTRS = "PROFILE_ATTRS";
	public static final String USER_ID = "user_id";

	public static final String INPUT_TYPE = "INPUT_TYPE";
	public static final String INPUT_TYPE_QUERY_CODE = "QUERY";
	public static final String INPUT_TYPE_MAN_IN_CODE = "MAN_IN";
	public static final String INPUT_TYPE_FIX_LOV_CODE = "FIX_LOV";
	public static final String INPUT_TYPE_JAVA_CLASS_CODE = "JAVA_CLASS";
	public static final String INPUT_TYPE_SCRIPT_CODE = "SCRIPT";

	public static final String THEME = "THEME";
	public static final String DEFAULT_ROLE = "DEFAULT_ROLE";

	public static final String BIOBJ_TYPE = "BIOBJ_TYPE";
	public static final String BIOBJECT_TYPE_CODE = "BIOBJECT_TYPE_CODE";
	public static final String REPORT_TYPE_CODE = "REPORT";
	public static final String DATAMART_TYPE_CODE = "DATAMART";

	public static final String OLAP_TYPE_CODE = "OLAP";
	public static final String DATA_MINING_TYPE_CODE = "DATA_MINING";
	public static final String DASH_TYPE_CODE = "DASH";
	public static final String CONSOLE_TYPE_CODE = "CONSOLE";

	public static final String MAP_TYPE_CODE = "MAP";
	public static final String LOW_FUNCTIONALITY_TYPE_CODE = "LOW_FUNCT";
	public static final String USER_FUNCTIONALITY_TYPE_CODE = "USER_FUNCT";
	public static final String DOC_STATE = "STATE";
	public static final String DOC_STATE_DEV = "DEV";
	public static final String DOC_STATE_REL = "REL";
	public static final String DOC_STATE_TEST = "TEST";
	public static final String DOC_STATE_SUSP = "SUSP";

	public static final String SUBOBJECT_LIST = "SUBOBJECT_NAMES_LIST";
	public static final String SNAPSHOT_LIST = "SNAPSHOT_LIST";
	public static final String EXEC_SNAPSHOT_MESSAGE = "EXEC_SNAPSHOT_MESSAGE";
	public static final String ERASE_SNAPSHOT_MESSAGE = "ERASE_SNAPSHOT_MESSAGE";
	public static final String SNAPSHOT = "SNAPSHOT";
	public static final String SNAPSHOT_NAME = "SNAPSHOT_NAME";
	public static final String SNAPSHOT_HISTORY_NUMBER = "SNAPSHOT_HISTORY_NUMBER";
	public static final String SNAPSHOT_ID = "SNAPSHOT_ID";

	public static final String VIEWPOINT_SAVE = "VIEWPOINT_SAVE";
	public static final String VIEWPOINT_LIST = "VIEWPOINT_LIST";
	public static final String VIEWPOINT_EXEC = "VIEWPOINT_EXEC";
	public static final String VIEWPOINT_ERASE = "VIEWPOINT_ERASE";
	public static final String VIEWPOINT_VIEW = "VIEWPOINT_VIEW";

	public static final String TITLE_VISIBLE = "TITLE_VISIBLE";
	public static final String TOOLBAR_VISIBLE = "TOOLBAR_VISIBLE";
	public static final String SLIDERS_VISIBLE = "SLIDERS_VISIBLE";

	public static final String ROLE = "ROLE";

	public static final String SINGLE_OBJECT_EXECUTION_MODALITY = "SINGLE_OBJECT_EXECUTION_MODALITY";
	public static final String NORMAL_EXECUTION_MODALITY = "NORMAL_EXECUTION_MODALITY";
	public static final String MASSIVE_EXPORT_MODALITY = "MASSIVE_EXPORT_MODALITY";

	public static final String FILTER_TREE_MODALITY = "FILTER_TREE_MODALITY";
	public static final String ENTIRE_TREE_MODALITY = "ENTIRE_TREE_MODALITY";

	public static final String HEIGHT_OUTPUT_AREA = "HEIGHT_OUTPUT_AREA";

	public static final String PUBLISHER_NAME = "PUBLISHER_NAME";
	public static final String PUBLISHER_LOOPBACK_AFTER_DEL_SUBOBJECT = "loopbackAfterSubObjectDeletion";
	public static final String PUBLISHER_LOOPBACK_VIEWPOINT_EXEC = "loopbackViewPointExecution";

	public final static String CMS_BIOBJECTS_PATH = "SPAGOBI.CMS_PATHS.BIOBJECTSPATH";

	public static final String IMPORTEXPORT_OPERATION = "IMPORTEXPORT_OPERATION";
	public static final String OPERATION_PAMPHLETS_VIEW_TREE = "OPERATION_PAMPHLETS_VIEW_TREE";

	public static final String EXEC_PHASE_CREATE_PAGE = "EXEC_PHASE_CREATE_PAGE";
	public static final String EXEC_PHASE_SELECTED_ROLE = "EXEC_PHASE_SELECTED_ROLE";
	public static final String EXEC_PHASE_RUN_SUBOJECT = "EXEC_SUBOBJECT";
	public static final String EXEC_PHASE_DELETE_SUBOJECT = "DELETE_SUBOBJECT";
	public static final String EXEC_PHASE_RETURN_FROM_LOOKUP = "EXEC_PHASE_RETURN_FROM_LOOKUP";
	public static final String EXEC_CHANGE_STATE = "EXEC_CHANGE_STATE";
	public static final String EXEC_PHASE_RUN = "EXEC_PHASE_RUN";
	public static final String EXEC_PHASE_REFRESH = "EXEC_PHASE_REFRESH";
	public static final String SELECT_ALL = "SELECT_ALL";
	public static final String DESELECT_ALL = "DESELECT_ALL";

	public static final String EXEC_CROSS_NAVIGATION = "EXEC_CROSS_NAVIGATION";
	public static final String RECOVER_EXECUTION_FROM_CROSS_NAVIGATION = "RECOVER_EXECUTION_FROM_CROSS_NAVIGATION";

	public static final String DOCUMENT_TEMPLATE_BUILD = "DocumentTemplateBuildPage";
	public static final String NEW_DOCUMENT_TEMPLATE = "NEW_DOCUMENT_TEMPLATE";
	public static final String EDIT_DOCUMENT_TEMPLATE = "EDIT_DOCUMENT_TEMPLATE";

	public static final String ACTIVITYKEY = "ACTIVITYKEY";

	public static final String PREFERENCE_MAXIMIZE_ABLE = "MAXIMIZE_ABLE";

	public static final String PREFERENCE_NOTES_EDITOR_ABLE = "NOTES_EDITOR_ABLE";
	public static final String PREFERENCE_NOTES_EDITOR_WIDTH = "NOTES_EDITOR_WIDTH";
	public static final String PREFERENCE_NOTES_EDITOR_HEIGHT = "NOTES_EDITOR_HEIGHT";
	public static final String PREFERENCE_NOTES_EDITOR_OPEN = "NOTES_EDITOR_OPEN";

	public static final String LIST_BIOBJ_PARAMETERS = "LIST_BIOBJ_PARAMETERS";

	public static final String LOV_MODIFIED = "LOV_MODIFIED";
	public static final String DATASET_MODIFIED = "DATASET_MODIFIED";

	public static final String DATASETS_LIST = "DATASETS_LIST";

	public static final String PROFILE_ATTRIBUTES_TO_FILL = "PROFILE_ATTRIBUTES_TO_FILL";

	public static final String MESSAGE_TEST_AFTER_ATTRIBUTES_FILLING = "MESSAGE_TEST_AFTER_ATTRIBUTES_FILLING";

	public static final String USER_PROFILE_FOR_TEST = "USER_PROFILE_FOR_TEST";

	public static final String PARAMETERS_TO_FILL = "PARAMETERS_TO_FILL";
	public static final String PARAMETERS_FILLED = "PARAMETERS_FILLED";

	public static final String AUTHENTICATION_FAILED_MESSAGE = "AUTHENTICATION_FAILED_MESSAGE";

	public static final String MESSAGE_BUILDER = "MESSAGE_BUILDER";

	public static final String URL_BUILDER = "URL_BUILDER";

	public static final String VALUE_COLUMN_NAME = "VALUE_COLUMN_NAME";

	public static final String PARAMETER_FIELD_NAME = "PARAMETER_FIELD_NAME";

	public static final String MESSAGE_INFO = "MESSAGE_INFO";

	public static final String MESSAGE_GETOBJECTS_SCHED = "MESSAGE_GETOBJECTS_SCHED";
	public static final String MESSAGE_SCHEDULE_OBJECT = "MESSAGE_SCHEDULE_OBJECT";
	public static final String MESSAGE_GET_OBJECT_SCHEDULATIONS = "MESSAGE_GET_OBJECT_SCHEDULATIONS";
	public static final String MESSAGE_NEW_OBJECT_SCHEDULATION = "MESSAGE_NEW_OBJECT_SCHEDULATION";
	public static final String MESSAGE_GET_OBJECT_SCHEDULATION_DETAILS = "MESSAGE_GET_OBJECT_SCHEDULATION_DETAILS";
	public static final String MESSAGE_DELETE_OBJECT_SCHEDULE = "MESSAGE_DELETE_OBJECT_SCHEDULE";
	public static final String OBJ_SCHEDULE_DETAIL = "OBJ_SCHEDULE_DETAIL";
	public static final String OBJ_JOB_EXISTS = "OBJ_JOB_EXISTS";

	public static final String MESSAGE_GET_ALL_JOBS = "MESSAGE_GET_ALL_JOBS";
	public static final String MESSAGE_NEW_JOB = "MESSAGE_NEW_JOB";
	public static final String MESSAGE_DOCUMENTS_SELECTED = "MESSAGE_DOCUMENTS_SELECTED";
	public static final String MESSAGE_FILL_PARAMETERS = "MESSAGE_FILL_PARAMETERS";
	public static final String MESSAGE_SAVE_JOB = "MESSAGE_SAVE_JOB";
	public static final String MESSAGE_DELETE_JOB = "MESSAGE_DELETE_JOB";
	public static final String MESSAGE_GET_JOB_DETAIL = "MESSAGE_GET_JOB_DETAIL";
	public static final String RETURN_TO_ACTIVITY_DETAIL = "RETURN_TO_ACTIVITY_DETAIL";
	public static final String IGNORE_WARNING = "IGNORE_WARNING";
	public static final String MESSAGE_GET_JOB_SCHEDULES = "MESSAGE_GET_JOB_SCHEDULES";
	public static final String MESSAGE_NEW_SCHEDULE = "MESSAGE_NEW_SCHEDULE";
	public static final String MESSAGE_SAVE_SCHEDULE = "MESSAGE_SAVE_SCHEDULE";
	public static final String MESSAGE_DELETE_SCHEDULE = "MESSAGE_DELETE_SCHEDULE";
	public static final String MESSAGE_GET_SCHEDULE_DETAIL = "MESSAGE_GET_SCHEDULE_DETAIL";
	public static final String MESSAGE_RUN_SCHEDULE = "MESSAGE_RUN_SCHEDULE";
	public static final String MESSAGE_ORDER_LIST = "MESSAGE_ORDER_LIST";
	public static final String JOB_INFO = "JOB_INFO";
	public static final String TRIGGER_INFO = "TRIGGER_INFO";
	public static final String JOB_NAME = "jobName";
	public static final String JOB_GROUP_NAME = "jobGroupName";

	public static final String WEBMODE = "WEBMODE";

	public static final String SBI_BACK_END_HOST = "BACK_END_HOST";
	public static final String SBI_EXECUTION_ID = "SBI_EXECUTION_ID";

	public static final String COUNTRY = "country";
	public static final String LANGUAGE = "language";

	// public static final String CURRENT_COUNTRY = "AF_COUNTRY";

	public static final String CURRENT_LANGUAGE = "CURRENT_LANGUAGE";
	public static final String AF_LANGUAGE = "AF_LANGUAGE";
	public static final String AF_COUNTRY = "AF_COUNTRY";
	public static final String AF_LANGUAGE_TAG = "AF_LANGUAGE_TAG";

	public static final String TEMPLATE_ID = "TEMPLATE_ID";

	public static final String SUBOBJECT_ID = "SUBOBJECT_ID";

	public static final String SUBOBJECT_NAME = "SUBOBJECT_NAME";

	public static final String SUBOBJECT = "SUBOBJECT";

	// constants for user profiles
	// ---superadmin functionalities
	public static final String TENANT_MANAGEMENT = "TenantManagement";
	public static final String READ_ENGINES_MANAGEMENT = "ReadEnginesManagement";
	public static final String CACHE_MANAGEMENT = "CacheManagement";
	public static final String LICENSE_MANAGEMENT = "LicenseManagement";
	// ---end
	public static final String DATASET_MANAGEMENT = "DatasetManagement";
	public static final String FUNCTIONALITIES_MANAGEMENT = "FunctionalitiesManagement";
	public static final String LOVS_MANAGEMENT = "LovsManagement";
	public static final String CONTSTRAINT_MANAGEMENT = "ConstraintManagement";
	public static final String PARAMETER_MANAGEMENT = "ParameterManagement";
	public static final String LOVS_VIEW = "LovsView";
	public static final String CONTSTRAINT_VIEW = "ConstraintView";
	public static final String PARAMETER_VIEW = "ParameterView";
	public static final String DOCUMENT_BROWSER_USER = "DocumentUserBrowser";
	public static final String DOCUMENT_MANAGEMENT_DEV = "DocumentDevManagement";
	public static final String DOCUMENT_MANAGEMENT_TEST = "DocumentTestManagement";
	public static final String DOCUMENT_MANAGEMENT_ADMIN = "DocumentAdminManagement";
	public static final String DOCUMENT_MANAGEMENT_USER = "DocumentUserManagement";
	public static final String IMPORT_EXPORT_MANAGEMENT = "ImportExportManagement";
	public static final String SCHEDULER_MANAGEMENT = "SchedulerManagement";
	public static final String EVENTS_MANAGEMENT = "EventsManagement";
	public static final String WORKSPACE_MANAGEMENT = "WorkspaceManagement";
	public static final String WORKLIST_MANAGEMENT = "WorklistManagement";
	public static final String MAPCATALOGUE_MANAGEMENT = "MapCatalogueManagement";
	public static final String MAP_FEATURES_MANAGEMENT = "MapFeaturesManagement";
	public static final String DOCUMENT_MANAGEMENT = "DocumentManagement";
	public static final String SYNCRONIZE_ROLES_MANAGEMENT = "SyncronizeRolesManagement";
	public static final String PROFILE_ATTRIBUTES_MANAGEMENT = "ProfileAttributeManagement";
	public static final String DATASOURCE_MANAGEMENT = "DataSourceManagement";
	public static final String DATASOURCE_READ = "DataSourceRead";
	public static final String DATASOURCE_BIG_DATA = "DataSourceBigData";
	public static final String DISTRIBUTIONLIST_MANAGEMENT = "DistributionListManagement";
	public static final String DISTRIBUTIONLIST_USER = "DistributionListUser";
	public static final String DOCUMENT_DELETE_MANAGEMENT = "DocumentDeleteManagement";
	public static final String DOCUMENT_DETAIL_MANAGEMENT = "DocumentDetailManagement";
	public static final String DOCUMENT_STATE_MANAGEMENT = "DocumentStateManagement";
	public static final String DOCUMENT_METADATA_MANAGEMENT = "DocumentMetadataManagement";
	public static final String MENU_MANAGEMENT = "MenuManagement";
	public static final String HOTLINK_MANAGEMENT = "HotLinkManagement";
	public static final String KPI_MANAGEMENT = "KpiManagement";
	public static final String KPI_SCHEDULATION = "KpiSchedulation";
	public static final String PROFILE_MANAGEMENT = "ProfileManagement";
	public static final String FINAL_USERS_MANAGEMENT = "FinalUsersManagement";
	public static final String SELF_SERVICE_DATASET_MANAGEMENT = "SelfServiceDatasetManagement";
	public static final String READ_ROLES = "ReadRoles";

	public static final String EDIT_PYTHON_SCRIPTS = "EditPythonScripts";
	public static final String CREATE_CUSTOM_CHART = "CreateCustomChart";
	public static final String SAVE_SUBOBJECT_FUNCTIONALITY = "SaveSubobjectFunctionality";
	public static final String SEE_SUBOBJECTS_FUNCTIONALITY = "SeeSubobjectsFunctionality";
	public static final String SEE_VIEWPOINTS_FUNCTIONALITY = "SeeViewpointsFunctionality";
	public static final String SEE_SNAPSHOTS_FUNCTIONALITY = "SeeSnapshotsFunctionality";
	public static final String RUN_SNAPSHOTS_FUNCTIONALITY = "RunSnapshotsFunctionality";
	public static final String SEE_NOTES_FUNCTIONALITY = "SeeNotesFunctionality";
	public static final String SEND_MAIL_FUNCTIONALITY = "SendMailFunctionality";
	public static final String SAVE_INTO_FOLDER_FUNCTIONALITY = "SaveIntoFolderFunctionality";
	public static final String SAVE_REMEMBER_ME_FUNCTIONALITY = "SaveRememberMeFunctionality";
	public static final String SEE_METADATA_FUNCTIONALITY = "SeeMetadataFunctionality";
	public static final String SAVE_METADATA_FUNCTIONALITY = "SaveMetadataFunctionality";
	public static final String BUILD_QBE_QUERIES_FUNCTIONALITY = "BuildQbeQueriesFunctionality";
	public static final String DO_MASSIVE_EXPORT_FUNCTIONALITY = "DoMassiveExportFunctionality";

	public static final String CREATE_SELF_SERVICE_COCKPIT = "CreateSelfSelviceCockpit";
	public static final String CREATE_SELF_SERVICE_GEOREPORT = "CreateSelfSelviceGeoreport";
	public static final String CREATE_SELF_SERVICE_KPI = "CreateSelfSelviceKpi";

	public static final String SEE_DOCUMENT_BROWSER = "SeeDocBrowser";
	public static final String SEE_MY_DATA = "SeeMyData";
	public static final String SEE_MY_WORKSPACE = "SeeMyWorkspace";
	public static final String SEE_FAVOURITES = "SeeFavourites";
	public static final String SEE_SUBSCRIPTIONS = "SeeSubscriptions";
	public static final String SEE_TODO_LIST = "SeeToDoList";
	public static final String CREATE_DOCUMENT = "CreateDocument";
	public static final String KPI_COMMENT_EDIT_ALL = "KpiCommentEditAll";
	public static final String KPI_COMMENT_EDIT_MY = "KpiCommentEditMy";
	public static final String KPI_COMMENT_DELETE = "KpiCommentDelete";
	public static final String CREATE_SOCIAL_ANALYSIS = "CreateSocialAnalysis";
	public static final String VIEW_SOCIAL_ANALYSIS = "ViewSocialAnalysis";
	public static final String HIERARCHIES_MANAGEMENT = "HierarchiesManagement";
	public static final String CREATE_COCKPIT_FUNCTIONALITY = "CreateCockpitFunctionality";
	public static final String CKAN_FUNCTIONALITY = "CkanIntegrationFunctionality";
	public static final String IMAGES_MANAGEMENT = "ImagesManagement";
	public static final String GLOSSARY = "Glossary";
	public static final String TIMESPAN = "Timespan";
	public static final String CREATE_DATASETS_AS_FINAL_USER = "CreateDatasetsAsFinalUser";
	public static final String USER_SAVE_DOCUMENT_FUNCTIONALITY = "UserSaveDocumentFunctionality";
	public static final String MANAGE_INTERNATIONALIZATION = "ManageInternationalization";
	public static final String SEE_NEWS = "NewsVisualization";

	public static final String DOCUMENT_MOVE_DOWN_STATE = "DocumentMoveDownState";
	public static final String DOCUMENT_MOVE_UP_STATE = "DocumentMoveUpState";
	// for management of DOCUMENT COMPOSITION
	public static final String DOCUMENT_COMPOSITION = "DOCUMENT_COMPOSITION";
	public static final String DOCUMENT_COMPOSITE_TYPE = "DOCUMENT_COMPOSITE";
	// to modify refresh time
	public static final String MODIFY_REFRESH = "ModifyRefresh";

	public static final String MENUES_LIST = "MENUES_LIST";

	public static final String BACK_URL = "BACK_URL";
	public static final String SDK_EXECUTION_SERVICE = "SDK_EXECUTION_SERVICE";

	// constants to menage configuration parameters
	public static final String CHANGEPWDMOD_LEN_MIN = "changepwdmodule.len_min";
	public static final String CHANGEPWDMOD_SPECIAL_CHAR = "changepwdmodule.special_char";
	public static final String CHANGEPWDMOD_UPPER_CHAR = "changepwdmodule.upper_char";
	public static final String CHANGEPWDMOD_LOWER_CHAR = "changepwdmodule.lower_char";
	public static final String CHANGEPWDMOD_NUMBER = "changepwdmodule.number";
	public static final String CHANGEPWDMOD_ALPHA = "changepwdmodule.alphabetical";
	public static final String CHANGEPWDMOD_CHANGE = "changepwdmodule.change";
	public static final String CHANGEPWDMOD_EXPIRED_TIME = "changepwdmodule.expired_time";
	public static final String CHANGEPWD_EXPIRED_TIME = "changepwd.expired_time";
	public static final String CHANGEPWD_CHANGE_FIRST = "changepwd.change_first";
	public static final String CHANGEPWD_DISACTIVE_TIME = "changepwd.disactivation_time";

	// constants to manage roles permission on folder (to develop, test, execute
	// and create documents)
	public static final String PERMISSION_ON_FOLDER = "PERMISSION_ON_FOLDER";
	public static final String PERMISSION_ON_FOLDER_TO_DEVELOP = "DEVELOPMENT";
	public static final String PERMISSION_ON_FOLDER_TO_TEST = "TEST";
	public static final String PERMISSION_ON_FOLDER_TO_EXECUTE = "EXECUTION";
	public static final String PERMISSION_ON_FOLDER_TO_CREATE = "CREATION";

	public static final String JNDI_THREAD_MANAGER = "JNDI_THREAD_MANAGER";

	public static final String TEMPORARY_TABLE_NAME = "SBI_TEMPORARY_TABLE_NAME";
	public static final String TEMPORARY_TABLE_ROOT_NAME = "SBI_TEMPORARY_TABLE_ROOT_NAME";
	public static final String DROP_TEMPORARY_TABLE_ON_EXIT = "SBI_DROP_TEMPORARY_TABLE_ON_EXIT";

	public static final String SOCIAL_ANALYSIS = "knowagesocialanalysis";

	public static final String CHART_TYPE_CODE = "CHART";
	public static final String COCKPIT_ENGINE_NAME = "Cockpit Engine";
	public static final String COCKPIT_ENGINE_LABEL = "knowagecockpitengine";
	public static final String BIRT_ENGINE_LABEL = "knowagebirtreporteng";
	public static final String BIRT_ENGINE_LABEL_2 = "knowagebirtreportengine";
	public static final String JASPER_ENGINE_LABEL = "knowagejasperreporte";
	public static final String JASPER_ENGINE_LABEL_2 = "knowagejasperreportengine";
	public static final String GIS_ENGINE_LABEL = "knowagegisengine";
	public static final String JPIVOT_ENGINE_LABEL = "knowagejpivotengine";
	public static final String QBE_ENGINE_LABEL = "knowageqbeengine";
	public static final String GEO_ENGINE_LABEL = "knowagegeoengine";
	public static final String TALEND_ENGINE_LABEL = "knowagetalendengine";
	public static final String CHART_ENGINE_LABEL = "knowagechartengine";
	public static final String GEOREPORT_ENGINE_LABEL = "knowagegeoreportengine";
	public static final String KPI_ENGINE_LABEL = "knowagekpiengine";
	public static final String META_ENGINE_LABEL = "knowagemeta";

	public static final String MONDRIAN_SCHEMA = "MONDRIAN_SCHEMA";
	public static final String MONDRIAN_CUBE = "cube";
	public static final String MONDRIAN_REFERENCE = "reference";
	public static final String SBI_ARTIFACT_VERSION_ID = "SBI_ARTIFACT_VERSION_ID";
	public static final String SBI_ARTIFACT_STATUS = "SBI_ARTIFACT_STATUS";
	public static final String SBI_ARTIFACT_VALUE_LOCKED_BY_USER = "locked_by_user";
	public static final String SBI_ARTIFACT_VALUE_LOCKED_BY_OTHER = "locked_by_other";
	public static final String SBI_ARTIFACT_VALUE_UNLOCKED = "unlocked";
	public static final String SBI_ARTIFACT_LOCKER = "SBI_ARTIFACT_LOCKER";
	public static final String SBI_ARTIFACT_ID = "SBI_ARTIFACT_ID";

	public static final String SBI_META_MODEL_VALUE_LOCKED_BY_USER = "locked_by_user";
	public static final String SBI_META_MODEL_VALUE_LOCKED_BY_OTHER = "locked_by_other";
	public static final String SBI_META_MODEL_VALUE_UNLOCKED = "unlocked";

	public static final String DATAMART_RETRIEVER = "DATAMART_RETRIEVER";

	public static final String DS_SCOPE_USER = "USER";
	public static final String DS_SCOPE_TECHNICAL = "TECHNICAL";
	public static final String DS_SCOPE_ENTERPRISE = "ENTERPRISE";

	public static final String ENABLE_DATASET_PERSISTENCE = "EnableDatasetPersistence";
	public static final String ENABLE_FEDERATED_DATASET = "EnableFederatedDataset";
	public static final String ENABLE_TO_RATE = "EnableToRate";
	public static final String ENABLE_TO_PRINT = "EnableToPrint";
	public static final String ENABLE_TO_COPY_AND_EMBED = "EnableToCopyAndEmbed";
	public static final String FEDERATED_DATASET_MANAGEMENT = "EnableFederatedDataset";

	public static final String MANAGE_GLOSSARY_BUSINESS = "ManageGlossaryBusiness";
	public static final String MANAGE_GLOSSARY_TECHNICAL = "ManageGlossaryTechnical";

	// public static final String CALENDAR = "Calendar";

	public static final String MANAGE_KPI_VALUE = "ManageKpiValue";

	public static final String MANAGE_CALENDAR = "ManageCalendar";

	public static final String MANAGE_CROSS_NAVIGATION = "ManageCrossNavigation";
	public static final String EXECUTE_CROSS_NAVIGATION = "ExecuteCrossNavigation";

	public static final String MANAGE_CROSS_OUT_PARAMS_PAGE = "ManageCrossOutParamsPage";
	public static final String MANAGE_DOC_LINKS_PAGE = "ManageDocLinksPage";
	public static final String CREATE_TIMESPAN = "CreateTimepan";

	public static final String DOMAIN_WRITE = "DomainWrite";
	public static final String DOMAIN_MANAGEMENT = "DomainManagement";
	public static final String EXPORTERS_CATALOGUE = "ExportersCatalogue";
	public static final String CONFIG_MANAGEMENT = "ConfigManagement";
	public static final String USER_DATA_PROPERTIES_MANAGEMENT = "UserDefinedPropertyManagement";
	public static final String TEMPLATE_MANAGEMENT = "TemplateManagement";
	public static final String IMP_EXP_DOCUMENT = "ImpExpDocument";
	public static final String IMP_EXP_RESOURCES = "ImpExpResources";
	public static final String IMP_EXP_USERS = "ImpExpUsers";
	public static final String IMP_EXP_KPIS = "ImpExpKpis";
	public static final String IMP_EXP_METADATA = "ImpExpMetadata";
	public static final String IMP_EXP_CATALOG = "ImpExpCatalog";
	public static final String IMP_EXP_GLOSSARY = "ImpExpGlossary";
	public static final String IMP_EXP_SCHEDULER = "ImpExpScheduler";

	public static final String META_MODELS_CATALOGUE_MANAGEMENT = "MetaModelsCatalogueManagement";

	public static final String MEASURES_CATALOGUE_MANAGEMENT = "MeasuresCatalogueManagement";

	public static final String MAP_WIDGET_USE = "MapWidgetUse";
	public static final String DISCOVERY_WIDGET_USE = "DiscoveryWidgetUse";

	// Cockpit
	public static final String MANAGE_STATIC_WIDGET = "StaticWidget";
	public static final String MANAGE_ANALYTICAL_WIDGET = "AnalyticalWidget";
	public static final String MANAGE_CHART_WIDGET = "ChartWidget";
	public static final String MANAGE_MULTISHEET_COCKPIT = "MultisheetCockpit";

	public static final String REGISTRY_DATA_ENTRY = "RegistryDataEntry";
	public static final String DOCUMENT_SCHEDULING = "DocumentScheduling";
	public static final String SCHEDULING_DISTRIBUTED_OUTPUT = "SchedulingDistributedOutput";
	public static final String META_MODEL_CWM_EXPORTING = "MetaModelCwmExporting";
	public static final String META_MODEL_SAVING_TO_RDBMS = "MetaModelSavingToRdbms";
	public static final String META_MODEL_LIFECYCLE_MANAGEMENT = "MetaModelLifecycleManagement";

	public static final String SHARED_DEVELOPMENT = "SharedDevelopment";
	public static final String FUNCTIONS_CATALOG_MANAGEMENT = "FunctionsCatalogManagement";
	public static final String FUNCTIONS_CATALOG_USAGE = "FunctionsCatalogUsage";

	public static final String TEMPORAL_DIMENSION = "TemporalDimension";

	public static final String IS_FOR_EXPORT = "IS_FOR_EXPORT";
	public static final String COCKPIT_SELECTIONS = "COCKPIT_SELECTIONS";

	// Date Range
	public static final String DATE_RANGE_OPTION_QUANTITY_PREFIX = "dateRangeOptionQuantity_";
	public static final String DATE_RANGE_OPTION_TYPE_PREFIX = "dateRangeOptionType_";
	public static final Set<String> DATE_RANGE_VALID_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("years", "months", "days", "weeks")));
	public static final String DATE_RANGE_TYPE_JSON = "type";
	public static final String DATE_RANGE_QUANTITY_JSON = "quantity";
	public static final String DATE_RANGE_TYPE = "DATE_RANGE";
	public static final String DATE_RANGE_OPTIONS_KEY = "options";
	public static final String PREDEFINED_GROOVY_SCRIPT_FILE_NAME = "predefinedGroovyScript.groovy";

	// Session Parameters
	public static final String SESSION_PARAMETERS_STORE_NAME = "sessionParameterService";
	public static final String SESSION_PARAMETER_STATE_OBJECT_KEY = "parameterStateKey";

}
