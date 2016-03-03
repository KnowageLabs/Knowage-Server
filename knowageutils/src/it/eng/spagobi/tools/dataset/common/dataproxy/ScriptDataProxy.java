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
package it.eng.spagobi.tools.dataset.common.dataproxy;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.common.datareader.IDataReader;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.scripting.SpagoBIScriptManager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ScriptDataProxy extends AbstractDataProxy {

	private String language;

	private static transient Logger logger = Logger.getLogger(ScriptDataProxy.class);

	public ScriptDataProxy() {
		super();
	}

	public ScriptDataProxy(String script, String language) {
		setStatement(script);
		setLanguage(language);
	}

	public IDataStore load(IDataReader dataReader) {
			
		String data = null;
		IDataStore dataStore = null;
		SpagoBIScriptManager scriptManager = new SpagoBIScriptManager();
		
		logger.debug("IN");
		
		try {
			if(dataReader == null) throw new IllegalArgumentException("Input parameter [" + dataReader + "] cannot be null");
			
			scriptManager = new SpagoBIScriptManager();
			
			List<File> imports = null;
			if( "groovy".equals(language) ){
				imports = new ArrayList<File>();
				URL url = Thread.currentThread().getContextClassLoader().getResource("predefinedGroovyScript.groovy");
				File scriptFile = new File(url.toURI());
				imports.add(scriptFile);
			} else if( "ECMAScript".equals(language ) ){
				imports = new ArrayList<File>();
				URL url = Thread.currentThread().getContextClassLoader().getResource("predefinedJavascriptScript.js");
				File scriptFile = new File(url.toURI());
				imports.add(scriptFile);
			}
			
			Map<String, Object> bindings = new HashMap<String, Object>();
			bindings.put("attributes", getProfile());
			bindings.put("parameters", getParameters());
			Object o = scriptManager.runScript(statement, language, bindings, imports);
			data = (o == null)? "": o.toString();
			dataStore = dataReader.read(data);
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while loading datastore", t);
		} finally {
			logger.debug("OUT");
		}
		

		return dataStore;
	}


	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}



}
