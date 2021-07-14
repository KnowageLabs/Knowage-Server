package it.eng.spagobi.commons;

import java.util.HashMap;
import java.util.Map;

public class ResourcePublisherMapping {

	private static final Map<String, String> resourceMap = new HashMap<String, String>() {
		{
			// Sorted by alphabetical order
			put("alertDefinition", "/WEB-INF/jsp/tools/alert/alertDefinition.jsp");
			put("analyticalDrivers", "/WEB-INF/jsp/tools/catalogue/analyticalDrivers.jsp");
			put("behaviouralModelLineage", "/WEB-INF/jsp/tools/catalogue/behaviouralModelLineage.jsp");
			put("businessModelCatalogue", "/WEB-INF/jsp/tools/catalogue/businessModelCatalogue.jsp");
			put("cacheHome", "/WEB-INF/jsp/tools/cache/cacheHome.jsp");
			put("calendarTemplate", "/WEB-INF/jsp/tools/calendar/calendarTemplate.jsp");
			put("configManagement", "/WEB-INF/jsp/tools/config/configManagement.jsp");
			put("crossDefinition", "/WEB-INF/jsp/tools/cross/definition/crossDefinition.jsp");
			put("datasetManagement", "/WEB-INF/jsp/tools/catalogue/datasetManagement.jsp");
			put("datasource", "/WEB-INF/jsp/tools/datasource/datasource.jsp");
			put("documentExecutionNg", "/WEB-INF/jsp/tools/documentexecution/documentExecutionNg.jsp");
			put("domainManagement", "/WEB-INF/jsp/tools/domain/domainManagement.jsp");
			put("eventLogsApp", "/WEB-INF/jsp/tools/event/eventLogsApp.jsp");
			put("exportersCatalogue", "/WEB-INF/jsp/tools/catalogue/exportersCatalogue.jsp");
			put("federatedDatasetBusiness", "/WEB-INF/jsp/tools/federateddataset/federatedDatasetBusiness.jsp");
			put("functionalitiesManagement", "/WEB-INF/jsp/tools/catalogue/functionalitiesManagement.jsp");
			put("functionsCatalog", "/WEB-INF/jsp/tools/functionsCatalog/functionsCatalog.jsp");
			put("geoMapFilter", "/WEB-INF/jsp/behaviouralmodel/analyticaldriver/mapFilter/geoMapFilter.jsp");
			put("glossaryBusiness", "/WEB-INF/jsp/tools/glossary/businessuser/glossaryBusiness.jsp");
			put("glossaryHelpOnline", "/WEB-INF/jsp/tools/glossary/finaluser/glossaryHelpOnline.jsp");
			put("glossaryTechnical", "/WEB-INF/jsp/tools/glossary/technicaluser/glossaryTechnical.jsp");
			put("hierarchiesEditor", "/WEB-INF/jsp/tools/hierarchieseditor/hierarchiesEditor.jsp");
			put("hierBackup", "/WEB-INF/jsp/tools/hierarchieseditor/hierBackup.jsp");
			put("hierMaster", "/WEB-INF/jsp/tools/hierarchieseditor/hierMaster.jsp");
			put("hierTechnical", "/WEB-INF/jsp/tools/hierarchieseditor/hierTechnical.jsp");
			put("importExportAlerts", "/WEB-INF/jsp/tools/servermanager/importExportAlerts/importExportAlerts.jsp");
			put("importExportAnalyticalDrivers", "/WEB-INF/jsp/tools/servermanager/importExportAnalyticalDrivers/importExportAnalyticalDrivers.jsp");
			put("importExportCatalog", "/WEB-INF/jsp/tools/servermanager/importExportCatalog/importExportCatalog.jsp");
			put("importExportDocuments", "/WEB-INF/jsp/tools/servermanager/importExportDocuments/importExportDocuments.jsp");
			put("importExportGlossaryTemplate", "/WEB-INF/jsp/tools/servermanager/importExportGlossary/importExportGlossaryTemplate.jsp");
			put("importExportKpis", "/WEB-INF/jsp/tools/servermanager/importExportKpis/importExportKpis.jsp");
			put("importExportMenu", "/WEB-INF/jsp/tools/servermanager/importExportMenu/importExportMenu.jsp");
			put("importExportResources", "/WEB-INF/jsp/tools/servermanager/importExportResources.jsp");
			put("importExportUsers", "/WEB-INF/jsp/tools/servermanager/importExportUsers/importExportUsers.jsp");
			put("internationalization", "/WEB-INF/jsp/internationalization/internationalization.jsp");
			put("kpiDefinition", "/WEB-INF/jsp/tools/kpi/kpiDefinition.jsp");
			put("layerCatalogue", "/WEB-INF/jsp/tools/layer/layerCatalogue.jsp");
			put("linkDataset", "/WEB-INF/jsp/tools/dataset/linkDataset.jsp");
			put("LinkDatasetIFrame", "/WEB-INF/jsp/tools/dataset/LinkDatasetIFrame.jsp");
			put("lovsManagement", "/WEB-INF/jsp/tools/catalogue/lovsManagement.jsp");
			put("ManageCommunity", "/WEB-INF/jsp/community/ManageCommunity.jsp");
			put("manageUdpAngular", "/WEB-INF/jsp/udp/manageUdpAngular.jsp");
			put("measureRuleDefinition", "/WEB-INF/jsp/tools/kpi/measureRuleDefinition.jsp");
			put("menuConfiguration", "/WEB-INF/jsp/tools/catalogue/menuConfiguration.jsp");
			put("measuresCatalogue", "/WEB-INF/jsp/tools/measure/measuresCatalogue.jsp");
			put("modalitiesCheck", "/WEB-INF/jsp/tools/catalogue/modalitiesCheck.jsp");
			put("mondrianSchemasCatalogue", "/WEB-INF/jsp/tools/catalogue/mondrianSchemasCatalogue.jsp");
			put("multitenantManagementAngular", "/WEB-INF/jsp/tools/multitenant/multitenantManagementAngular.jsp");
			put("newsManagement", "/WEB-INF/jsp/tools/news/newsManagement.jsp");
			put("profileAttributesManagement", "/WEB-INF/jsp/tools/catalogue/profileAttributesManagement.jsp");
			put("rolesManagement", "/WEB-INF/jsp/tools/catalogue/rolesManagement.jsp");
			put("schedulerKpi", "/WEB-INF/jsp/tools/kpi/schedulerKpi.jsp");
			put("scorecardKpi", "/WEB-INF/jsp/tools/kpi/scorecardKpi.jsp");
			put("targetDefinition", "/WEB-INF/jsp/tools/kpi/targetDefinition.jsp");
			put("templateManagement", "/WEB-INF/jsp/tools/servermanager/templateManagement.jsp");
			put("timespan", "/WEB-INF/jsp/tools/timespan/timespan.jsp");
			put("usersManagement", "/WEB-INF/jsp/tools/catalogue/usersManagement.jsp");
			put("workspaceManagement", "/WEB-INF/jsp/tools/workspace/workspaceManagement.jsp");
		}
	};

	public static String get(String key) {
		return resourceMap.get(key);
	}

}
