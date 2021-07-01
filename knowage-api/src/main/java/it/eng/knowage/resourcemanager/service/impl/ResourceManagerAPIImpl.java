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
package it.eng.knowage.resourcemanager.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.error.KnowageRuntimeException;
import it.eng.knowage.knowageapi.utils.ContextPropertiesConfig;
import it.eng.knowage.resourcemanager.resource.utils.FolderDTO;
import it.eng.knowage.resourcemanager.resource.utils.RootFolderDTO;
import it.eng.knowage.resourcemanager.service.ResourceManagerAPI;

@Component
public class ResourceManagerAPIImpl implements ResourceManagerAPI {
	private static final Logger LOGGER = Logger.getLogger(ResourceManagerAPIImpl.class);

	@Override
	public RootFolderDTO getFolders(String path) {

		String resourcePathBase = ContextPropertiesConfig.getResourcePath();
		FolderDTO parentFolder = new FolderDTO(resourcePathBase);
		FolderDTO mylist = null;
		RootFolderDTO newRootFolder = null;
		LOGGER.debug("Starting resource path json tree testing");
		try {
			if (path == null)
				path = resourcePathBase;
			Path f = Paths.get(path);
			mylist = createTree(parentFolder);
			parseFolders(mylist);
			clearFolders(mylist, f.getParent().toString());
			newRootFolder = new RootFolderDTO(mylist);

		} catch (IOException e) {
			throw new KnowageRuntimeException(e.getMessage());
		}
		return newRootFolder;
	}

	private static void parseFolders(FolderDTO e) {
		setFolderLevel(e, 0, 0);
	}

	private static void setFolderLevel(FolderDTO e, int lvl, int count) {
		e.setKey(lvl + "-" + count);
		count = 0;
		if (e.getChildren() != null && e.getChildren().size() > 0) {
			lvl++;
			for (FolderDTO emp : e.getChildren()) {
				count++;
				setFolderLevel(emp, lvl, count);
			}
		}
	}

	private static void clearFolders(FolderDTO e, String path) {
		changeFolderPath(e, path);
	}

	private static void changeFolderPath(FolderDTO e, String path) {
		e.setLabel(e.getLabel().replace(path, ""));
		if (e.getChildren() != null && e.getChildren().size() > 0) {
			for (FolderDTO emp : e.getChildren()) {
				changeFolderPath(emp, path);
			}
		}
	}

	public static FolderDTO createTree(FolderDTO parentFolder) throws IOException {
		File node = new File(parentFolder.getLabel());
		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				String path = node + "\\" + filename;
				if (new File(path).isDirectory()) {
					FolderDTO folder = new FolderDTO(path);
					parentFolder.addChildren(folder);
					createTree(folder);
				} else {
//					parentFolder.addFile(new CustomFile(path));
				}
			}
		}
		return parentFolder;
	}

}
