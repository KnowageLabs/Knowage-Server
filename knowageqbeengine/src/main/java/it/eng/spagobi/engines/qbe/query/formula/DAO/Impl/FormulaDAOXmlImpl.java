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
package it.eng.spagobi.engines.qbe.query.formula.DAO.Impl;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.SpagoBIUnmarshallerWrapper;
import it.eng.spagobi.engines.qbe.query.formula.DAO.IFormulaDAO;
import it.eng.spagobi.engines.qbe.query.formula.mapping.Formula;
import it.eng.spagobi.engines.qbe.query.formula.mapping.Formulas;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 *
 * @author dpirkovic
 *
 */
public class FormulaDAOXmlImpl implements IFormulaDAO {

	public static transient Logger logger = Logger.getLogger(FormulaDAOXmlImpl.class);
	private final IEngUserProfile userProfile;

	public FormulaDAOXmlImpl(IEngUserProfile userProfile) {
		this.userProfile = userProfile;
	}

	@Override
	public List<Formula> getAll() {

		logger.debug("IN");
		SpagoBIUnmarshallerWrapper<Formulas> unmarshaller;
		Formulas formulas;
		File xmlFile;
		FormulaFileRetriver formulaFileRetriver = new FormulaFileRetriver();

		unmarshaller = new SpagoBIUnmarshallerWrapper<>();
		formulas = new Formulas();
		xmlFile = formulaFileRetriver.getFormulaFile();

		try {

			if (xmlFile != null && xmlFile.exists()) {
				Formulas temp = unmarshaller.unmarshall(xmlFile, Formulas.class);
				logger.debug("File " + xmlFile.getName() + " unmarshalled");

				applyFilter(temp);
				formulas.setFormulas(temp.getFormulas());
			}

			return formulas.getFormulas();
		} catch (JAXBException e) {
			logger.error("Error while loading formulas ", e);
			throw new SpagoBIEngineRuntimeException("Error while loading formulas ", e);
		} catch (EMFInternalError e) {
			logger.error("Error while loading formulas ", e);
			throw new SpagoBIEngineRuntimeException("Error while loading formulas ", e);
		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * @param temp
	 * @throws EMFInternalError
	 */
	private void applyFilter(Formulas temp) throws EMFInternalError {
		Iterator<Formula> it = temp.getFormulas().iterator();

		while (it.hasNext()) {
			if (it.next().getType().equals("time") && !this.userProfile.getFunctionalities().contains("Time_functions")) {
				it.remove();
			}

		}
	}

}
