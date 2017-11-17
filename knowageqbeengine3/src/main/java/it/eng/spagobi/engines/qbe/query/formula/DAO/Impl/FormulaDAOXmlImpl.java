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
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.utilities.SpagoBIUnmarshallerWrapper;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.query.formula.FormulaConfig;
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
	private static final String ACTIVE_FORMULA_FILE = "qbe.formulas.active";
	private final FormulaConfig formulaConf;
	private final String engineResourcePath;

	public FormulaDAOXmlImpl() {
		formulaConf = new FormulaConfig();
		engineResourcePath = QbeEngineConfig.getInstance().getEngineResourcePath();
	}

	@Override
	public List<Formula> getAll() {

		logger.debug("IN");
		SpagoBIUnmarshallerWrapper<Formulas> unmarshaller;
		Formulas formulas;
		File xmlFile;

		unmarshaller = new SpagoBIUnmarshallerWrapper<>();
		formulas = new Formulas();
		xmlFile = new File(getFilePath());
		logger.debug("File wtih file exists: " + xmlFile.exists());
		try {

			if (xmlFile.exists()) {
				Formulas temp = unmarshaller.unmarshall(xmlFile, Formulas.class);
				logger.debug("File " + xmlFile.getName() + " unmarshalled");
				formulas.setFormulas(temp.getFormulas());
			}

			return formulas.getFormulas();
		} catch (JAXBException e) {
			logger.error("Error while loading formulas ", e);
			throw new SpagoBIEngineRuntimeException("Error while loading formulas ", e);
		} finally {
			logger.debug("OUT");
		}

	}

	private String getFilePath() {
		logger.debug("IN");
		String finalFilePath;
		String relativeFilePath = formulaConf.getProperty(ACTIVE_FORMULA_FILE);
		if (relativeFilePath == null) {
			logger.error("Property " + ACTIVE_FORMULA_FILE + "is missing.");
			throw new SpagoBIEngineRuntimeException("Property " + ACTIVE_FORMULA_FILE + "is missing.");
		}

		finalFilePath = engineResourcePath + File.separator + relativeFilePath;
		logger.debug("Final file path is: " + finalFilePath);
		logger.debug("OUT");
		return finalFilePath;

	}

}
