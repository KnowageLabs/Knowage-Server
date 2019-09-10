/**
 *
 */
package it.eng.knowage.document.export.cockpit;

import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.document.cockpit.CockpitDocument;
import it.eng.knowage.document.cockpit.template.widget.ICockpitWidget;
import it.eng.knowage.document.cockpit.template.widget.WidgetReaderFactory;
import it.eng.knowage.document.export.cockpit.converter.DataStoreConfigurationConverter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.FileManager;
import it.eng.spagobi.tools.dataset.metasql.query.item.IDataStoreConfiguration;

/**
 * @author Dragan Pirkovic
 *
 */
public class CSVCockpitDataExporter implements ICockpitDataExporter {

	private final Integer documentId;
	private final Map<String, String> parameters;
	private final Locale locale;
	private final UserProfile userProfile;
	private final String resourcePath;
	private final String documentLabel;

	/**
	 * @param documentId
	 * @param documentLabel
	 * @param parameters
	 * @param locale
	 * @param userProfile
	 * @param resourcePath
	 */
	public CSVCockpitDataExporter(Integer documentId, String documentLabel, Map<String, String> parameters, Locale locale, UserProfile userProfile,
			String resourcePath) {
		this.documentId = documentId;
		this.parameters = parameters;
		this.locale = locale;
		this.userProfile = userProfile;
		this.resourcePath = resourcePath;
		this.documentLabel = documentLabel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.ICockpitDataExporter#export()
	 */
	@Override
	public void export() {
		CockpitDocument cockpitDocument = new CockpitDocument(parameters, documentId, documentLabel);
		DatasetManagementAPI datasetManagementAPI = new DatasetManagementAPI(userProfile);

		FileManager fileManager = new FileManager(cockpitDocument.getLabel(), resourcePath);
		IConverter<IDataStoreConfiguration, ICockpitWidget> converter = new DataStoreConfigurationConverter(cockpitDocument);
		JSONArray jsonWidgets = cockpitDocument.getWidgets();

		for (int i = 0; i < jsonWidgets.length(); i++) {
			try {
				JSONObject jsonWidget = jsonWidgets.getJSONObject(i);
				ICockpitWidget widget = WidgetReaderFactory.getWidget(jsonWidget);
				IDataStoreConfiguration conf = converter.convert(widget);
				IDataStore datastore = datasetManagementAPI.getDataStore(conf.getDataset(), true, conf.getParameters(), conf.getProjections(), conf.getFilter(),
						conf.getGroups(), conf.getSortings(), conf.getSummaryRowProjections(), conf.getOffset(), conf.getFetchSize(), conf.getMaxRowCount());

				fileManager.createFile(widget.getName(), datastore);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * @return the documentId
	 */
	public Integer getDocumentId() {
		return documentId;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @return the userProfile
	 */
	public UserProfile getUserProfile() {
		return userProfile;
	}

	/**
	 * @return the resourcePath
	 */
	public String getResourcePath() {
		return resourcePath;
	}

	/**
	 * @return the documentLabel
	 */
	public String getDocumentLabel() {
		return documentLabel;
	}

}
