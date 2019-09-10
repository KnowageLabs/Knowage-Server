/**
 *
 */
package it.eng.knowage.document.export.cockpit;

import java.util.Locale;
import java.util.Map;

import it.eng.spagobi.api.v2.export.cockpit.ExportType;
import it.eng.spagobi.commons.bo.UserProfile;

/**
 * @author Dragan Pirkovic
 *
 */
public class CockpitDataExporterBuilder {

	private Integer documentId;
	private Map<String, String> parameters;
	private ExportType type;
	private Locale locale;
	private UserProfile userProfile;
	private String resourcePath;
	private String documentLabel;

	/**
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the documentId
	 */
	public Integer getDocumentId() {
		return documentId;
	}

	/**
	 * @return the type
	 */
	public ExportType getType() {
		return type;
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
	 * @param documentId
	 * @return
	 */
	public CockpitDataExporterBuilder setDocumentId(Integer documentId) {
		this.documentId = documentId;
		return this;
	}

	/**
	 * @param parameters
	 * @return
	 */
	public CockpitDataExporterBuilder setDocumentParameters(Map<String, String> parameters) {
		this.parameters = parameters;
		return this;
	}

	/**
	 * @param exportType
	 * @return
	 */
	public CockpitDataExporterBuilder setType(ExportType exportType) {
		this.type = exportType;
		return this;
	}

	/**
	 * @param locale
	 * @return
	 */
	public CockpitDataExporterBuilder setLocale(Locale locale) {
		this.locale = locale;
		return this;
	}

	/**
	 * @param resourcePath
	 * @return
	 */
	public CockpitDataExporterBuilder setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
		return this;
	}

	/**
	 * @param userProfile
	 * @return
	 */
	public CockpitDataExporterBuilder setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
		return this;
	}

	/**
	 * @return
	 */
	public ICockpitDataExporter build() {
		ICockpitDataExporter exporter = null;
		switch (type) {
		case CSV:
			exporter = new CSVCockpitDataExporter(documentId, documentLabel, parameters, locale, userProfile, resourcePath);
			break;

		default:
			break;
		}
		return exporter;
	}

	/**
	 * @param documentLabel
	 * @return
	 */
	public CockpitDataExporterBuilder setDocumentLabel(String documentLabel) {
		this.documentLabel = documentLabel;
		return this;
	}

}
