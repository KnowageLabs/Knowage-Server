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
package it.eng.knowage.document.export.cockpit;

import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.document.cockpit.CockpitDocument;
import it.eng.knowage.document.cockpit.template.widget.ICockpitWidget;
import it.eng.knowage.document.cockpit.template.widget.WidgetReaderFactory;
import it.eng.knowage.document.export.cockpit.converter.ConverterFactory;
import it.eng.knowage.document.export.cockpit.converter.IConverterException;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.CsvExportFileManager;
import it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Dragan Pirkovic
 *
 */
public class CSVCockpitDataExporter implements ICockpitDataExporter {

	private final CockpitDocument cockpitDocument;
	private final CsvExportFileManager fileManager;
	private final UserProfile userProfile;
	private static Logger logger = Logger.getLogger(CSVCockpitDataExporter.class);

	/**
	 * @param documentId
	 * @param documentLabel
	 * @param parameters
	 * @param locale
	 * @param userProfile
	 * @param resourcePath
	 * @param zipFileName
	 */
	public CSVCockpitDataExporter(Integer documentId, String documentLabel, Map<String, String> parameters, Locale locale, UserProfile userProfile,
			String resourcePath, String zipFileName) {
		this.userProfile = userProfile;
		this.cockpitDocument = new CockpitDocument(parameters, documentId, documentLabel);
		this.fileManager = new CsvExportFileManager(documentLabel, resourcePath, zipFileName);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.ICockpitDataExporter#export()
	 */
	@Override
	public void export() {
		logger.debug("IN");
		ICockpitWidget widget = null;
		for (int i = 0; i < getWidgets().length(); i++) {
			try {
				logger.debug("getting widget");
				widget = getWidget(i);

				if (widget != null) {
					logger.debug("creating csv file");
					fileManager.createCSVFileAndZipIt(widget.getName(), getDataStore(getDataStoreConf(widget)));
					logger.debug("csv file created");
				}

			} catch (JSONException | IConverterException e) {
				String msg = String.format("Error while exporting csv file for widget with name: %s for cockpit with label: %d", widget.getName(),
						cockpitDocument.getLabel());
				logger.error(msg);
				throw new SpagoBIRuntimeException(msg, e);
			}

		}
		logger.debug("OUT");
	}

	/**
	 * @param i
	 * @return
	 * @throws JSONException
	 */
	private ICockpitWidget getWidget(int i) throws JSONException {
		JSONObject jsonWidget = getWidgets().getJSONObject(i);
		ICockpitWidget widget = WidgetReaderFactory.getWidget(jsonWidget);
		return widget;
	}

	/**
	 * @return the cockpitDocument
	 */
	public CockpitDocument getCockpitDocument() {
		return cockpitDocument;
	}

	/**
	 * @return the userProfile
	 */
	public UserProfile getUserProfile() {
		return userProfile;
	}

	/**
	 * @param conf
	 * @return
	 * @throws JSONException
	 */
	private IDataStore getDataStore(IDataStoreConfiguration conf) throws JSONException {
		return new DatasetManagementAPI(userProfile).getDataStore(conf.getDataset(), true, conf.getParameters(), conf.getProjections(), conf.getFilter(),
				conf.getGroups(), conf.getSortings(), conf.getSummaryRowProjections(), conf.getOffset(), conf.getFetchSize(), conf.getMaxRowCount(), null);
	}

	/**
	 * @param cockpitDocument
	 * @param widget
	 * @return
	 * @throws IConverterException
	 */
	private IDataStoreConfiguration getDataStoreConf(ICockpitWidget widget) throws IConverterException {
		return ConverterFactory.getDataStoreConfigurationConverter(cockpitDocument).convert(widget);
	}

	/**
	 * @param cockpitDocument
	 * @return
	 */
	private JSONArray getWidgets() {
		return cockpitDocument.getWidgets();
	}

}
