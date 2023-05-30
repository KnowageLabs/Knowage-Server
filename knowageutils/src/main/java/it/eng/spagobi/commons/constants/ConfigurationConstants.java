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

public final class ConfigurationConstants {

	private ConfigurationConstants() {
	}

	public static final String SPAGOBI_DATASET_ASSOCIATIONS_AUTODETECT_SAMPLES = "SPAGOBI.DATASET.ASSOCIATIONS.AUTODETECT.SAMPLES";
	public static final String SPAGOBI_DATASET_ASSOCIATIVE_LOGIC_STRATEGY = "SPAGOBI.DATASET.ASSOCIATIVE_LOGIC.STRATEGY";
	public static final String DOCUMENT_EXPORTING_PDF_FRONT_PAGE = "DOCUMENT.EXPORTING.PDF.FRONT_PAGE";
	public static final String DOCUMENT_EXPORTING_PDF_BACK_PAGE = "DOCUMENT.EXPORTING.PDF.BACK_PAGE";

	public static final String INTERNAL_SECURITY_LOGIN_IMPORT_USER_IF_NOT_EXISTING = "internal.security.login.importUserIfNotExisting";
	public static final String INTERNAL_SECURITY_USERS_DEFAULT_ROLE = "internal.security.users.defaultRole";

	public static final String DOCUMENT_EXPORTER_KPI = "document.exporter.knowagekpiengine";
	public static final String DOCUMENT_EXPORTER_JASPER = "document.exporter.knowagejasperreporte";
	public static final String DOCUMENT_EXPORTER_BIRT = "document.exporter.knowagebirtreporteng";
	public static final String DOCUMENT_EXPORTER_QBE = "document.exporter.knowageqbeengine";
	public static final String DOCUMENT_EXPORTER_WHAT_IF = "document.exporter.knowagewhatifengine";
	public static final String DOCUMENT_EXPORTER_COCKPIT = "document.exporter.knowagecockpitengine";
	public static final String DOCUMENT_EXPORTER_OLAP = "document.exporter.knowageolapengine";
	/**
	 * To be used with engine label as suffix.
	 */
	public static final String DOCUMENT_EXPORTER_PREFIX = "document.exporter.";
	public static final String DOCUMENT_EXPORTER_SEPARATOR = ",";
}
