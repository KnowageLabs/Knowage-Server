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

import it.eng.spagobi.api.v2.export.cockpit.ExportType;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.utilities.assertion.Assert;

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
	private String zipFileName;

	/**
	 * @return the zipFileName
	 */
	public String getZipFileName() {
		return zipFileName;
	}

	/**
	 * @param zipFileName
	 *            the zipFileName to set
	 */
	public CockpitDataExporterBuilder setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
		return this;
	}

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
			Assert.assertNotNull(documentId, "documentId cannot be null");
			Assert.assertNotNull(documentLabel, "documentLabel cannot be null");
			Assert.assertNotNull(userProfile, "userProfile cannot be null");
			Assert.assertNotNull(zipFileName, "zipFileName cannot be null");

			Assert.assertNotNull(userProfile, "Attribute userProfile cannot be null");
			exporter = new CSVCockpitDataExporter(documentId, documentLabel, parameters, locale, userProfile, resourcePath, zipFileName);
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
