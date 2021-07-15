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
package it.eng.knowage.resourcemanager.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import it.eng.knowage.knowageapi.error.KNRM001Exception;
import it.eng.knowage.resourcemanager.resource.dto.FileDTO;
import it.eng.knowage.resourcemanager.resource.dto.MetadataDTO;
import it.eng.knowage.resourcemanager.resource.dto.RootFolderDTO;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

public interface ResourceManagerAPI {

	public RootFolderDTO getFolders(SpagoBIUserProfile profile, String path) throws KNRM001Exception;

	boolean createFolder(String path, SpagoBIUserProfile profile) throws KNRM001Exception;

	boolean delete(String path, SpagoBIUserProfile profile) throws KNRM001Exception;

	java.nio.file.Path getDownloadFolderPath(String path, SpagoBIUserProfile profile) throws KNRM001Exception;

	public List<FileDTO> getListOfFiles(String path, SpagoBIUserProfile profile) throws KNRM001Exception;

	java.nio.file.Path getDownloadFilePath(List<String> path, SpagoBIUserProfile profile, boolean multi) throws KNRM001Exception;

	boolean canSee(Path path, SpagoBIUserProfile profile);

	void importFile(InputStream archiveInputStream, String path, SpagoBIUserProfile profile) throws IOException, KNRM001Exception;

	void importFileAndExtract(InputStream archiveInputStream, String path, SpagoBIUserProfile profile) throws IOException, KNRM001Exception;

	MetadataDTO getMetadata(String path, SpagoBIUserProfile profile) throws KNRM001Exception;

	MetadataDTO saveMetadata(MetadataDTO fileDTO, String path, SpagoBIUserProfile profile) throws KNRM001Exception;
}
