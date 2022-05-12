/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.commons.multitenant;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

public abstract class OrganizationImageManager {
	static private Logger logger = Logger.getLogger(OrganizationImageManager.class);
	static private final String ORGANIZATION_IMAGE_FILE_NAME = "organization_image.base64";

	public static final String getOrganizationB64Image(String organization) {
		String organizationImagePath = SpagoBIUtilities.getRootResourcePath() + File.separatorChar + organization + File.separatorChar
				+ ORGANIZATION_IMAGE_FILE_NAME;
		if (new File(organizationImagePath).isFile()) {
			try (FileInputStream inputStream = new FileInputStream(organizationImagePath)) {
				String imgB64 = IOUtils.toString(inputStream);
				return imgB64;
			} catch (Exception e) {
				logger.error("Cannot load organization image", e);
			}
		}
		return null;
	}

	public static final void setOrganizationB64Image(String organization, String imgB64) {
		String organizationImagePath = SpagoBIUtilities.getRootResourcePath() + File.separatorChar + organization + File.separatorChar
				+ ORGANIZATION_IMAGE_FILE_NAME;
		try {
			Files.write(Paths.get(organizationImagePath), imgB64.getBytes());
		} catch (Exception e) {
			logger.error("Cannot save organization image", e);
		}
	}
}
