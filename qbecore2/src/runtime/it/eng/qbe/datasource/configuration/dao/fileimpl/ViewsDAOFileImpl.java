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

import it.eng.qbe.datasource.configuration.dao.IViewsDAO;
import it.eng.qbe.model.structure.IModelViewEntityDescriptor;
import it.eng.qbe.model.structure.ModelViewEntityDescriptor;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

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

/**
 * Implementation of IViewsDAO that read model views from a json
 * file named views.json stored within the jar file passed in as argument to
 * the class constructor.
 * 
 * NOTE: this class does not support interface methods saveViews. Calling it will
 * cause an exception
 * 
 * @author Andrea Gioia
 */
public class ViewsDAOFileImpl implements IViewsDAO {
	
	File modelJarFile;
	
	private static final String VIEWS_FILE_NAME = "views.json";
	
	public static transient Logger logger = Logger.getLogger(ViewsDAOFileImpl.class);
	
	public ViewsDAOFileImpl(File file) {
		modelJarFile = file;
	}
	

	public List<IModelViewEntityDescriptor> loadModelViews() {
		List<IModelViewEntityDescriptor> views; 
		JSONObject viewsConfJSON = null;
		
		views = new ArrayList<IModelViewEntityDescriptor>();
		
		JarFile jarFile = null;
		try {
			jarFile = new JarFile( modelJarFile );
			viewsConfJSON = loadViewsFormJarFile(jarFile);
			JSONArray viewsJSON = viewsConfJSON.optJSONArray("views");
			if(viewsJSON != null) {
				for(int i = 0; i < viewsJSON.length(); i++) {
					JSONObject viewJSON = viewsJSON.getJSONObject(i);
					views.add(new ModelViewEntityDescriptor(viewJSON));
				}
			}
			jarFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}				
		
		return views;	
	}

	protected JSONObject loadViewsFormJarFile(JarFile jarFile){
		JSONObject viewsJSON = null;
		
		try{
			ZipEntry zipEntry = jarFile.getEntry(VIEWS_FILE_NAME);
			if (zipEntry != null){
				InputStream inputStream = jarFile.getInputStream(zipEntry);
				String viewsFileContent = getStringFromStream(inputStream);
				inputStream.close();
				viewsJSON = new JSONObject( viewsFileContent );
			} else {
				viewsJSON = new JSONObject();
			}
		} catch(Exception ioe){
			ioe.printStackTrace();
			viewsJSON = new JSONObject();
		}finally{
			try {
				if(jarFile!=null){
					jarFile.close();
				}
			} catch (Exception e2) {
				logger.error("Error closing the jar file",e2);
			}
		}
		
		return viewsJSON;
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
	 * @see it.eng.qbe.dao.DatamartPropertiesDAO#saveModelViews()
	 */
	public void saveModelViews(List<JSONObject> views) {
		throw new SpagoBIRuntimeException("saveModelViews method not supported");
	}
}
