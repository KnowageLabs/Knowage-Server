/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler;

import groovy.lang.GroovyShell;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class Formula {
	
	static private Logger logger = Logger.getLogger(Formula.class);	
	
	private String name;
	private String description;
	private String groovyCode;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getGroovyCode() {
		return groovyCode;
	}
	public void setGrovyCode(String groovyCode) {
		this.groovyCode = groovyCode;
	}

	public Formula(SourceBean configuration) throws Exception {
		logger.debug("IN");
		this.name = (String) configuration.getAttribute("name");
		if (name == null || name.trim().equals("")) throw new Exception("Formula name not found!");
		this.description = (String) configuration.getAttribute("description");
		if (description == null || description.trim().equals("")) throw new Exception("Formula description not found!");
		this.groovyCode = configuration.getCharacters();
		if (groovyCode == null || groovyCode.trim().equals("")) throw new Exception("Formula groovy code not found!");
		logger.debug("OUT");
	}

	/**
	 * Executes the formula (i.e. executes the Groovy script)
	 * @return the result as a String
	 * @throws Exception if the groovy code is null or if the execution of the code generates an Excpetion
	 */
	public String execute() throws Exception {
		logger.debug("IN");
		if (groovyCode == null) {
			throw new Exception("Groovy code not found");
		}
		logger.debug("Executing groovy code: \n" + groovyCode);
		GroovyShell shell = new GroovyShell();
		Object value = shell.evaluate(groovyCode);
		String toReturn = null;
		if (value != null) {
			toReturn = value.toString();
		}
		logger.debug("OUT: returning " + toReturn);
		return toReturn;
	}
	
	/**
	 * Returns a List with all the available formulas: they are configured in WEB-INF/conf/tools/scheduler/formulas.xml.
	 * In case no formulas are found, an empty ArrayList is returned.
	 * 
	 * @return the List of configured formulas.
	 */
	public static List<Formula> getAvailableFormulas() {
		logger.debug("IN");
		List<Formula> toReturn = new ArrayList<Formula>();
		List formulasList = ConfigSingleton.getInstance().getAttributeAsList("FORMULAS.FORMULA");
		if (formulasList == null || formulasList.isEmpty()) {
			logger.debug("No formulas configured.");
			return toReturn;
		}
		Iterator it = formulasList.iterator();
		while (it.hasNext()) {
			SourceBean sb = (SourceBean) it.next();
			if (sb != null) {
				Formula f = null;
				try {
					f = new Formula(sb);
				} catch (Exception e) {
					logger.error("The SourceBean is not a valid configuration for a Formula : " + sb, e);
				}
				if (f != null) {
					toReturn.add(f);
				}
			}
		}
		logger.debug("OUT");
		return toReturn;
	}
	
	/**
	 * Returns the formula corresponding to the specified name.
	 * If this formula does not exists, null is returned.
	 * @param fName the name of the formula
	 * @return the formula corresponding to the specified name.
	 */
	public static Formula getFormula(String fName) {
		logger.debug("IN");
		Formula toReturn = null;
		List formulasList = getAvailableFormulas();
		if (formulasList == null || formulasList.isEmpty()) {
			logger.debug("No formulas configured.");
			return toReturn;
		}
		Iterator<Formula> it = formulasList.iterator();
		while (it.hasNext()) {
			Formula f = it.next();
			if (f.name.equalsIgnoreCase(fName)) {
				toReturn = f;
				break;
			}
		}
		if (toReturn == null) {
			logger.warn("Formula with name [" + fName + "] was not found!");
		}
		logger.debug("OUT");
		return toReturn;
	}
	
}
