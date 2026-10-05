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
package it.eng.qbe.datasource.configuration.dao.fileimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.datasource.configuration.dao.IGroupsDAO;
import it.eng.qbe.model.structure.IModelGroupDescriptor;
import it.eng.qbe.model.structure.ModelGroupDescriptor;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class GroupsDAOFileImpl implements IGroupsDAO {

	private static final String GROUPS_FILE_NAME = "groups.json";

	File modelJarFile;
	private final String modelName;

	public static transient Logger logger = Logger.getLogger(GroupsDAOFileImpl.class);

	public GroupsDAOFileImpl(File file) {
		this(file, null);
	}

	public GroupsDAOFileImpl(File file, String modelName) {
		modelJarFile = file;
		this.modelName = modelName;
	}

	@Override
	public List<IModelGroupDescriptor> loadModelGroups() {
		List<IModelGroupDescriptor> groups = new ArrayList<>();
		try (JarFile jarFile = new JarFile(modelJarFile)) {
			if (jarFile.getEntry(GROUPS_FILE_NAME) != null) {
				JSONObject groupsConfJSON = loadGroupsFromJarFile(jarFile);
				JSONArray groupsJSON = groupsConfJSON.getJSONArray("groups");
				for (int i = 0; i < groupsJSON.length(); i++) {
					groups.add(new ModelGroupDescriptor(groupsJSON.getJSONObject(i), modelName));
				}
			}
		} catch (IOException | JSONException | SpagoBIRuntimeException e) {
			logger.error("loadModelGroups", e);
			throw new SpagoBIRuntimeException("Cannot load groups from model [" + modelJarFile + "]", e);
		}

		return groups;
	}

	protected JSONObject loadGroupsFromJarFile(JarFile jarFile) {
		try {
			if (jarFile.getEntry(GROUPS_FILE_NAME) != null) {
				try (InputStream inputStream = jarFile.getInputStream(jarFile.getEntry(GROUPS_FILE_NAME))) {
					return new JSONObject(getStringFromStream(inputStream));
				}
			} else {
				return new JSONObject();
			}
		} catch (IOException | JSONException ioe) {
			throw new SpagoBIRuntimeException("Cannot read groups.json from model [" + modelJarFile + "]", ioe);
		}
	}

	private String getStringFromStream(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
		String line;
		StringBuilder buffer = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			buffer.append(line).append('\n');
		}

		return buffer.toString();
	}
}
