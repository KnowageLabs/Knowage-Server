/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterValuesRetriever;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class retrieves values executing a Formula object
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class FormulaParameterValuesRetriever extends
		ParameterValuesRetriever {
	
	static private Logger logger = Logger.getLogger(FormulaParameterValuesRetriever.class);	

	private Formula formula;
	
	@Override
	public List<String> retrieveValues(BIObjectParameter parameter) throws Exception {
		logger.debug("IN");
		List<String> toReturn = new ArrayList<String>();
		String result = formula.execute();
		logger.debug("Result obtained from formula is [" + result + "]");
		if (result != null) {
			toReturn.add(result);
		}
		logger.debug("IN");
		return toReturn;
	}

	public Formula getFormula() {
		return formula;
	}

	public void setFormula(Formula formula) {
		this.formula = formula;
	}

}
