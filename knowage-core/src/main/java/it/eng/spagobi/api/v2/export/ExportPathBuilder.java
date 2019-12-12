/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2.export;

import java.nio.file.Paths;
import java.util.UUID;

import it.eng.spagobi.commons.bo.UserProfile;

/**
 * Singleton that contains logic to build and get export path.
 *
 * @author Marco Libanori
 *
 */
public class ExportPathBuilder {
	public static final String METADATA_FILENAME = "metadata";
	public static final String DATA_FILENAME = "data";
	/**
	 * Filename of the placeholder to put in a directory to indicate the file is already downloaded.
	 */
	public static final String DOWNLOADED_PLACEHOLDER_FILENAME = "downloaded";

	private static final ExportPathBuilder INSTANCE = new ExportPathBuilder();

	private ExportPathBuilder() {
		super();
	}

	public static ExportPathBuilder getInstance() {
		return INSTANCE;
	}

	public java.nio.file.Path getExportResourcePath(final String resourcePathAsStr) {
		return Paths.get(resourcePathAsStr, "export");
	}

	public java.nio.file.Path getPerUserExportResourcePath(final String resourcePathAsStr, final UserProfile userProfile) {
		return getExportResourcePath(resourcePathAsStr).resolve(userProfile.getUserId().toString());
	}

	public java.nio.file.Path getPerJobExportPath(final String resourcePathAsStr, final UserProfile userProfile, final UUID id) {
		return getPerUserExportResourcePath(resourcePathAsStr, userProfile).resolve(id.toString());
	}

	public java.nio.file.Path getPerJobIdDataFile(final String resourcePathAsStr, final UserProfile userProfile, final UUID id) {
		return getPerJobExportPath(resourcePathAsStr, userProfile, id).resolve(DATA_FILENAME);
	}

	public java.nio.file.Path getPerJobIdMetadataFile(final String resourcePathAsStr, final UserProfile userProfile, final UUID id) {
		return getPerJobExportPath(resourcePathAsStr, userProfile, id).resolve(METADATA_FILENAME);
	}

	public java.nio.file.Path getPerJobIdDownloadedPlaceholderFile(final String resourcePathAsStr, final UserProfile userProfile, final UUID id) {
		return getPerJobExportPath(resourcePathAsStr, userProfile, id).resolve(DOWNLOADED_PLACEHOLDER_FILENAME);
	}

}