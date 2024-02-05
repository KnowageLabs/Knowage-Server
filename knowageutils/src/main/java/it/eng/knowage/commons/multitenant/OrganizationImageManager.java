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

import it.eng.knowage.commons.security.PathTraversalChecker;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

public class OrganizationImageManager {

	private static Logger logger = Logger.getLogger(OrganizationImageManager.class);

	private static final String ORGANIZATION_IMAGE_FILE_NAME = "organization_image.base64";
	private static final String ORGANIZATION_IMAGE_WIDE_FILE_NAME = "organization_image_wide.base64";

	private OrganizationImageManager() {
		throw new IllegalStateException("This class cannot be instantiated");
	}

	public static final String getOrganizationB64Image(String organization) {
		File organizationImageFile = PathTraversalChecker.get(SpagoBIUtilities.getRootResourcePath(), organization, ORGANIZATION_IMAGE_FILE_NAME);
		if (organizationImageFile.isFile()) {
			try (FileInputStream inputStream = new FileInputStream(organizationImageFile)) {
				return IOUtils.toString(inputStream);
			} catch (Exception e) {
				logger.error("Cannot load organization image", e);
			}
		}
		return null;
	}

	public static final void setOrganizationB64Image(String organization, String imgB64) {
		File organizationImageFile = PathTraversalChecker.get(SpagoBIUtilities.getRootResourcePath(), organization, ORGANIZATION_IMAGE_FILE_NAME);
		try {
			Files.write(Paths.get(organizationImageFile.getAbsolutePath()), imgB64.getBytes());
		} catch (Exception e) {
			logger.error("Cannot save organization image", e);
		}
	}

	public static final String getOrganizationB64ImageWide(String organization) {
		File organizationImageFile = PathTraversalChecker.get(SpagoBIUtilities.getRootResourcePath(), organization, ORGANIZATION_IMAGE_WIDE_FILE_NAME);
		if (organizationImageFile.isFile()) {
			try (FileInputStream inputStream = new FileInputStream(organizationImageFile)) {
				return IOUtils.toString(inputStream);
			} catch (Exception e) {
				logger.error("Cannot load organization image wide", e);
			}
		}
		return null;
	}

	public static final void setOrganizationB64ImageWide(String organization, String imgB64) {
		File organizationImageFile = PathTraversalChecker.get(SpagoBIUtilities.getRootResourcePath(), organization, ORGANIZATION_IMAGE_WIDE_FILE_NAME);
		try {
			Files.write(Paths.get(organizationImageFile.getAbsolutePath()), imgB64.getBytes());
		} catch (Exception e) {
			logger.error("Cannot save organization image wide", e);
		}
	}

}