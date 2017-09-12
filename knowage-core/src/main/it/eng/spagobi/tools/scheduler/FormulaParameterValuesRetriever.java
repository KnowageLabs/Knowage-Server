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
