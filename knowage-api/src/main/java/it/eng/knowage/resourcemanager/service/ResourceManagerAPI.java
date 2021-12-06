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

import it.eng.knowage.knowageapi.error.ImpossibleToCreateFileException;
import it.eng.knowage.knowageapi.error.ImpossibleToCreateFolderException;
import it.eng.knowage.knowageapi.error.ImpossibleToDeleteFileException;
import it.eng.knowage.knowageapi.error.ImpossibleToDeleteFolderException;
import it.eng.knowage.knowageapi.error.ImpossibleToDownloadFileException;
import it.eng.knowage.knowageapi.error.ImpossibleToReadFilesListException;
import it.eng.knowage.knowageapi.error.ImpossibleToReadFolderListException;
import it.eng.knowage.knowageapi.error.ImpossibleToReadMetadataException;
import it.eng.knowage.knowageapi.error.ImpossibleToSaveMetadataException;
import it.eng.knowage.knowageapi.error.ImpossibleToUploadFileException;
import it.eng.knowage.resourcemanager.resource.dto.FileDTO;
import it.eng.knowage.resourcemanager.resource.dto.MetadataDTO;
import it.eng.knowage.resourcemanager.resource.dto.RootFolderDTO;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

public interface ResourceManagerAPI {

	public RootFolderDTO getFolders(SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException;

	boolean createFolder(String path, SpagoBIUserProfile profile) throws ImpossibleToCreateFolderException;

	boolean delete(String path, SpagoBIUserProfile profile) throws IOException, ImpossibleToDeleteFolderException, ImpossibleToDeleteFileException;

	public List<FileDTO> getListOfFiles(String key, SpagoBIUserProfile profile) throws ImpossibleToReadFilesListException, ImpossibleToReadFolderListException;

	java.nio.file.Path getDownloadFilePath(List<String> path, SpagoBIUserProfile profile, boolean multi) throws ImpossibleToDownloadFileException;

	boolean canSee(Path path, SpagoBIUserProfile profile) throws IOException;

	void importFile(InputStream archiveInputStream, String path, SpagoBIUserProfile profile)
			throws IOException, ImpossibleToCreateFileException, ImpossibleToUploadFileException;

	void importFileAndExtract(InputStream archiveInputStream, String path, SpagoBIUserProfile profile) throws IOException;

	MetadataDTO getMetadata(String path, SpagoBIUserProfile profile) throws ImpossibleToReadMetadataException;

	MetadataDTO saveMetadata(MetadataDTO fileDTO, String path, SpagoBIUserProfile profile) throws ImpossibleToSaveMetadataException;

	/**
	 * @param key
	 * @param profile
	 * @return
	 * @throws ImpossibleToReadFolderListException
	 */
	public String getFolderByKey(String key, SpagoBIUserProfile profile) throws ImpossibleToReadFolderListException;

	/**
	 * @param key
	 * @param path
	 * @param profile
	 * @return
	 * @throws ImpossibleToCreateFileException
	 */
	Path getDownloadFolderPath(String key, String path, SpagoBIUserProfile profile) throws ImpossibleToCreateFileException;

	/**
	 * @param completePath
	 * @param folderName
	 * @param profile
	 * @return
	 * @throws ImpossibleToCreateFolderException
	 */
	public boolean updateFolder(Path completePath, String folderName, SpagoBIUserProfile profile) throws IOException, ImpossibleToCreateFolderException;
}
