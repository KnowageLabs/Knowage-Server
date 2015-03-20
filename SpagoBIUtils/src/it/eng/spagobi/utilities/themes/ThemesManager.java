/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.themes;
/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class ThemesManager {

	private static transient Logger logger = Logger.getLogger(ThemesManager.class);


	/**
	 * Gets the elements of menu relative by the user logged. It reaches the role from the request and 
	 * asks to the DB all detail
	 * menu information, by calling the method <code>loadMenuByRoleId</code>.
	 *   
	 * @param request The request Source Bean
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */   

	public static String getDefaultTheme(){
		return SingletonConfig.getInstance().getConfigValue("SPAGOBI.THEMES.THEME.default");
	
	}

	public static String getCurrentThemeName(String  currTheme){
		logger.debug("IN:currTheme="+currTheme);
		return SingletonConfig.getInstance().getConfigValue("SPAGOBI.THEMES.THEME."+currTheme+".view_name");

	}
	

	public static String getCurrentTheme(RequestContainer reqCont){
		SessionContainer sessCont = reqCont.getSessionContainer();
		SessionContainer permSess = sessCont.getPermanentContainer();		
		String currTheme=(String)permSess.getAttribute(SpagoBIConstants.THEME);

		if(currTheme!=null){
			return currTheme;
		}
		else{
			return getDefaultTheme();
		}
	}

	public static boolean drawSelectTheme(List themes){
		boolean drawSelect=false;
		if(themes==null || themes.size()<=1) { // if no theme is defined only default will be used
			drawSelect=false;
		}
		else drawSelect=true;
		return drawSelect;

	}
	
	public static List<String> getThemes(){
		logger.debug("IN");
		String themes=SingletonConfig.getInstance().getConfigValue("SPAGOBI.THEMES.THEMES");
		List<String> toReturn=new ArrayList<String>();
		StringTokenizer tokenizer=new StringTokenizer(themes,",");
		while (tokenizer.hasMoreElements()) {
			String theme = (String) tokenizer.nextElement();
			toReturn.add(theme);
			logger.debug("Add Theme:"+theme);
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Check if a resource exists in the current team;
	 * @param currTheme  the current theme
	 * @param resource addres of the resource ro be verified
	 */   

	public static boolean resourceExists(String currTheme, String resource){
		logger.debug("IN");
		ConfigSingleton config = ConfigSingleton.getInstance();
		String rootPath=config.getRootPath();
		logger.debug("rootPath="+rootPath);
		String urlByTheme=resource;
		resource.trim();
		if(resource.startsWith("/"))
			resource=resource.substring(1);

		if(currTheme!=null)
		{
			urlByTheme="/themes/"+currTheme+"/"+resource;
		}

		String urlComplete=rootPath+urlByTheme;
		// check if object exists
		File check=new File(urlComplete);
		// if file
		
		if(!check.exists())
		{
			logger.debug("OUT.true");
			return false;
		}
		else {
			logger.debug("OUT.false");
			return true;
		}

	}

	/**
	 * Check if a resource exists;  if the name of the resource contains the spagoBICOntext removes it
	 *   
	 * @param resource addres of the resource ro be verified
	 * @param spagoBIContext   the name of the context
	 */   

	public static boolean resourceExistsInTheme(String resource, String spagoBIContext){
		logger.debug("IN");
		ConfigSingleton config = ConfigSingleton.getInstance();
		String rootPath=config.getRootPath();
		String urlByTheme=resource;
		resource.trim();

		if(spagoBIContext!=null && resource.startsWith(spagoBIContext)){
			int sizeToRemove=spagoBIContext.length();
			resource=resource.substring(sizeToRemove);			
		}

		String urlComplete=rootPath+resource;
		// check if object exists
		File check=new File(urlComplete);
		// if file
		logger.debug("IN");
		if(!check.exists())
		{
			return false;
		}
		else return true;
	}



	public static String getTheExtTheme(String currTheme){
		
		logger.debug("IN:currTheme="+currTheme);
		String ext_theme= SingletonConfig.getInstance().getConfigValue("SPAGOBI.THEMES.THEME."+currTheme+".ext_theme");
		if (ext_theme==null){
			ext_theme=getTheExtTheme("sbi_default");
		}
		logger.debug("OUT:"+ext_theme);
		return ext_theme;

		}




	}






