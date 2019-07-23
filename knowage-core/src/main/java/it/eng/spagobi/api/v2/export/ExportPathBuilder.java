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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.function.Supplier;

import it.eng.spagobi.commons.bo.UserProfile;

/**
 * Singleton that contains logic to build and get export path.
 *
 * @author Marco Libanori
 *
 */
public class ExportPathBuilder {
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

	public java.nio.file.Path getPerJobIdFile(final String resourcePathAsStr, final UserProfile userProfile, final UUID id) throws IOException {
		final java.nio.file.Path perIdPath = getPerJobExportPath(resourcePathAsStr, userProfile, id);
		java.nio.file.Path perIdFilePath = Files.list(perIdPath).findFirst().orElseThrow(new Supplier<IllegalStateException>() {

			@Override
			public IllegalStateException get() {
				String msg = String.format("Path %s must contains only one file", perIdPath);
				return new IllegalStateException(msg);
			}
		});

		return perIdFilePath;
	}
}