/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.qbe.datasource.configuration.dao.fileimpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.qbe.datasource.configuration.dao.IRelationshipsDAO;
import it.eng.qbe.model.structure.IModelRelationshipDescriptor;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor;
import it.eng.qbe.model.structure.ModelRelationshipDescriptor;
import it.eng.qbe.model.structure.ModelViewEntityDescriptor;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class RelationshipsDAOFileImpl implements IRelationshipsDAO{

	File modelJarFile;
	
	private static final String RELATIONSHIPS_FILE_NAME = "relationships.json";
	
	public static transient Logger logger = Logger.getLogger(ViewsDAOFileImpl.class);
	
	public RelationshipsDAOFileImpl(File file) {
		modelJarFile = file;
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.configuration.dao.IRelationshipsDAO#loadModelRelationships()
	 */
	public List<IModelRelationshipDescriptor> loadModelRelationships() {
		List<IModelRelationshipDescriptor> relationship; 
		JSONObject relationshipsConfJSON = null;
		
		relationship = new ArrayList<IModelRelationshipDescriptor>();
		
		JarFile jarFile = null;
		try {
			jarFile = new JarFile( modelJarFile );
			relationshipsConfJSON = loadRelationshipFromJarFile(jarFile);
			JSONArray relationshipsJSON = relationshipsConfJSON.optJSONArray("relationships");
			if(relationshipsJSON != null) {
				for(int i = 0; i < relationshipsJSON.length(); i++) {
					JSONObject relationshipJSON = relationshipsJSON.getJSONObject(i);
					relationship.add(new ModelRelationshipDescriptor(relationshipJSON));
				}
			}
			jarFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}				
		
		return relationship;	
	}

	protected JSONObject loadRelationshipFromJarFile(JarFile jarFile){
		JSONObject relationshipsJSON = null;
		
		try{
			ZipEntry zipEntry = jarFile.getEntry(RELATIONSHIPS_FILE_NAME);
			if (zipEntry != null){
				InputStream inputStream = jarFile.getInputStream(zipEntry);
				String relationshipsFileContent = getStringFromStream(inputStream);
				inputStream.close();
				relationshipsJSON = new JSONObject( relationshipsFileContent );
			} else {
				relationshipsJSON = new JSONObject();
			}
		} catch(Exception ioe){
			ioe.printStackTrace();
			relationshipsJSON = new JSONObject();
		}finally{
			try {
				if(jarFile!=null){
					jarFile.close();
				}
			} catch (Exception e2) {
				logger.error("Error closing the jar file",e2);
			}
		}
		
		return relationshipsJSON;
	}
	
	public String getStringFromStream(InputStream inputStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line = null;
		StringBuffer buffer = new StringBuffer();
		while((line = reader.readLine()) != null) {
			buffer.append(line + "\n");
		}
		
		return buffer.toString();
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.datasource.configuration.dao.IRelationshipsDAO#saveModelViews(java.util.List)
	 */
	public void saveModelViews(List<JSONObject> relationships) {
		throw new SpagoBIRuntimeException("saveModelViews method not supported");
	}

}
