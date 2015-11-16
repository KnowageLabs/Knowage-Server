/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.scripting;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;

public class ScriptUtilities {
	private static transient Logger logger = Logger.getLogger(ScriptUtilities.class);

	static String[] embeddedJavascriptAlias={"ejs","EmbeddedJavaScript","embeddedjavascript"};
	static String[] javascriptAlias={"rhino-nonjdk","js","rhino","JavaScript","javascript","ECMAScript","ecmascript"};
	static String[] groovyAlias={"groovy"};


	static public String bindAliasEngine(String alias){
		String toReturn=alias;
		boolean found=false;

		for(int i=0;found==false && i<javascriptAlias.length;i++){
			if(alias.equalsIgnoreCase(javascriptAlias[i])){
				toReturn="JavaScript";
				found=true;
			}
		}
		for(int i=0;found==false && i<embeddedJavascriptAlias.length;i++){
			if(alias.equalsIgnoreCase(embeddedJavascriptAlias[i])){
				toReturn="Embedded JavaScript";
				found=true;
			}
		}
		for(int i=0;found==false && i<groovyAlias.length;i++){
			if(alias.equalsIgnoreCase(groovyAlias[i])){
				toReturn="Groovy";
				found=true;
			}
		}
		return toReturn;
	}

	/**
	 * getEngineFactoriesNames
	 *
	 * @return a map <name of the engine, alias>
	 */
	static public Map<String, String> getEngineFactoriesNames(){
		logger.debug("IN");
		ScriptEngineManager mgr = new ScriptEngineManager();
		List<ScriptEngineFactory> factories = mgr.getEngineFactories();

		Map<String,String> names=new HashMap<String,String>();
		for (ScriptEngineFactory factory: factories) {
			String engName1 = factory.getEngineName(); 
			String engVersion=factory.getEngineVersion();
			String engName=engName1+engVersion;
			List<String> engNames = factory.getNames();
			String alias=engName;
			if(engNames.size()>=1){
				alias=engNames.get(0);
			}
			names.put(engName, alias);
		}
		logger.debug("found "+names.keySet().size()+" engines");		
		logger.debug("OUT");
		return names;
	}

	
	/**
	 * Gets the all profile attributes.
	 * 
	 * @param profile the profile
	 * 
	 * @return the all profile attributes
	 * 
	 * @throws EMFInternalError the EMF internal error
	 */
	public static HashMap getAllProfileAttributes(IEngUserProfile profile) throws EMFInternalError {
		logger.debug("IN");
		if (profile == null)
			throw new EMFInternalError(EMFErrorSeverity.ERROR,
			"getAllProfileAttributes method invoked with null input profile object");
		HashMap profileattrs = new HashMap();
		Collection profileattrsNames = profile.getUserAttributeNames();
		if (profileattrsNames == null || profileattrsNames.size() == 0)
			return profileattrs;
		Iterator it = profileattrsNames.iterator();
		while (it.hasNext()) {
			Object profileattrName = it.next();
			Object profileattrValue = profile.getUserAttribute(profileattrName.toString());
			profileattrs.put(profileattrName, profileattrValue);
		}
		logger.debug("OUT");
		return profileattrs;
	}

	
	
}
