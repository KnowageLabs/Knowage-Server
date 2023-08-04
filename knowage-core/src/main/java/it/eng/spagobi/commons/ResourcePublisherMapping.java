package it.eng.spagobi.commons;

import java.util.HashMap;
import java.util.Map;

public class ResourcePublisherMapping {

	private static final Map<String, String> resourceMap = new HashMap<String, String>() {
		{
			// Sorted by alphabetical order

			put("importExportAlerts", "/WEB-INF/jsp/tools/servermanager/importExportAlerts/importExportAlerts.jsp");
			put("importExportAnalyticalDrivers", "/WEB-INF/jsp/tools/servermanager/importExportAnalyticalDrivers/importExportAnalyticalDrivers.jsp");
			put("importExportCatalog", "/WEB-INF/jsp/tools/servermanager/importExportCatalog/importExportCatalog.jsp");
			put("importExportDocuments", "/WEB-INF/jsp/tools/servermanager/importExportDocuments/importExportDocuments.jsp");
			put("importExportGlossaryTemplate", "/WEB-INF/jsp/tools/servermanager/importExportGlossary/importExportGlossaryTemplate.jsp");
			put("importExportKpis", "/WEB-INF/jsp/tools/servermanager/importExportKpis/importExportKpis.jsp");
			put("importExportMenu", "/WEB-INF/jsp/tools/servermanager/importExportMenu/importExportMenu.jsp");
			put("importExportResources", "/WEB-INF/jsp/tools/servermanager/importExportResources.jsp");
			put("importExportUsers", "/WEB-INF/jsp/tools/servermanager/importExportUsers/importExportUsers.jsp");
			
		}
	};

	public static String get(String key) {
		return resourceMap.get(key);
	}

}
