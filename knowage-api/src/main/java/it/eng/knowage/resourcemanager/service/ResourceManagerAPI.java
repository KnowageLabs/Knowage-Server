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
import it.eng.knowage.knowageapi.error.KNRM002Exception;
import it.eng.knowage.knowageapi.error.KNRM003Exception;
import it.eng.knowage.knowageapi.error.KNRM004Exception;
import it.eng.knowage.knowageapi.error.KNRM005Exception;
import it.eng.knowage.knowageapi.error.KNRM006Exception;
import it.eng.knowage.knowageapi.error.KNRM007Exception;
import it.eng.knowage.knowageapi.error.KNRM008Exception;
import it.eng.knowage.knowageapi.error.KNRM009Exception;
import it.eng.knowage.knowageapi.error.KNRM010Exception;
import it.eng.knowage.knowageapi.error.KNRM011Exception;
import it.eng.knowage.resourcemanager.resource.dto.FileDTO;
import it.eng.knowage.resourcemanager.resource.dto.MetadataDTO;
import it.eng.knowage.resourcemanager.resource.dto.RootFolderDTO;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

public interface ResourceManagerAPI {

	public RootFolderDTO getFolders(SpagoBIUserProfile profile, String path) throws KNRM001Exception, KNRM002Exception;

	boolean createFolder(String path, SpagoBIUserProfile profile) throws KNRM001Exception, KNRM004Exception;

	boolean delete(String path, SpagoBIUserProfile profile) throws KNRM001Exception, KNRM006Exception, KNRM007Exception;

	java.nio.file.Path getDownloadFolderPath(String path, SpagoBIUserProfile profile) throws KNRM001Exception, KNRM008Exception, KNRM005Exception;

	public List<FileDTO> getListOfFiles(String path, SpagoBIUserProfile profile) throws KNRM001Exception, KNRM003Exception;

	java.nio.file.Path getDownloadFilePath(List<String> path, SpagoBIUserProfile profile, boolean multi) throws KNRM001Exception, KNRM008Exception;

	boolean canSee(Path path, SpagoBIUserProfile profile);

	void importFile(InputStream archiveInputStream, String path, SpagoBIUserProfile profile)
			throws IOException, KNRM001Exception, KNRM005Exception, KNRM009Exception;

	void importFileAndExtract(InputStream archiveInputStream, String path, SpagoBIUserProfile profile) throws IOException, KNRM001Exception;

	MetadataDTO getMetadata(String path, SpagoBIUserProfile profile) throws KNRM001Exception, KNRM011Exception;

	MetadataDTO saveMetadata(MetadataDTO fileDTO, String path, SpagoBIUserProfile profile) throws KNRM001Exception, KNRM010Exception;
}
