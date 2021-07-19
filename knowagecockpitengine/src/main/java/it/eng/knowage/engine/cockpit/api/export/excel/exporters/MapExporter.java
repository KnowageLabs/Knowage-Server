package it.eng.knowage.engine.cockpit.api.export.excel.exporters;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class MapExporter extends GenericExporter implements IWidgetExporter {

	public static transient Logger logger = Logger.getLogger(MapExporter.class);

	public MapExporter(ExcelExporter excelExporter, String widgetType, String templateString, long widgetId, Workbook wb, JSONObject options) {
		super(excelExporter, widgetType, templateString, widgetId, wb, options);
	}

	@Override
	public int export() {
		int exportedSheets = 0;
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			String widgetName = getWidgetName(widget);

			JSONArray dataStoreArray = excelExporter.getMultiDataStoreForWidget(template, widget);
			for (int i = 0; i < dataStoreArray.length(); i++) {
				try {
					JSONObject dataStore = dataStoreArray.getJSONObject(i);
					if (dataStore != null) {
						String cockpitSheetName = getCockpitSheetName(template, widgetId) + String.valueOf(i);
						excelExporter.createAndFillExcelSheet(dataStore, wb, widgetName, cockpitSheetName);
						exportedSheets++;
					}
				} catch (Exception e) {
					logger.error("Couldn't export layer [" + (i + 1) + "] of map widget [" + widgetId + "]");
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to export map widget: " + widgetId, e);
		}
		return exportedSheets;
	}
}
