/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
