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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.knowage.resourcemanager.KnowageResourceManagerConfigurationTest;
import it.eng.knowage.resourcemanager.resource.dto.FolderDTO;

/**
 * @author Matteo Massarotto
 */

@ContextConfiguration(classes = KnowageResourceManagerConfigurationTest.class)
@SpringBootTest
@Component
@ActiveProfiles("test")
public class ResourceManagerResourceTest {

	static List<String> allList = new ArrayList<String>();
	private static final Logger LOGGER = Logger.getLogger(ResourceManagerResourceTest.class);

	@Autowired
	ObjectMapper mapper;

	@Test
	void getTree(@Value("${test.resourcepath}") String resourcePath) {
		Path p = Paths.get(resourcePath);
		FolderDTO parentFolder = new FolderDTO(p);
		FolderDTO mylist = null;
		LOGGER.debug("Starting resource path json tree testing");

		try {
			Path f = Paths.get(resourcePath);
			mylist = createTree(parentFolder);
			parseFolders(mylist);
			clearFolders(mylist, f.getParent().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			String tree = mapper.writeValueAsString(mylist);
			JSONObject ja = new JSONObject(tree);
			JSONArray jsArr = ja.getJSONArray("children");
			JSONObject jo = new JSONObject();
			jo.put("root", jsArr);

			// RootFolderDTO newRootFolder = new RootFolderDTO(mylist);

			LOGGER.debug(jo.toString(4));
			// LOGGER.debug(ja.toString(4));
			LOGGER.debug("END");
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void parseFolders(FolderDTO e) {
		setFolderLevel(e, 0, 0);
	}

	private static void setFolderLevel(FolderDTO e, int lvl, int count) {
//		e.setKey(lvl + "-" + count);
//		count = 0;
//		if (e.getChildren() != null && e.getChildren().size() > 0) {
//			lvl++;
//			for (FolderDTO emp : e.getChildren()) {
//				count++;
//				setFolderLevel(emp, lvl, count);
//			}
//		}
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
				Path path = Paths.get(node.toString()).resolve(filename);
				if (Files.isDirectory(path)) {
					FolderDTO folder = new FolderDTO(path);
					folder.setKey(DatatypeConverter.printHexBinary(path.toString().getBytes()));
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
