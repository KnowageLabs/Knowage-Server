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
package it.eng.spagobi.engines.whatif.calculatedmember;

import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class MDXFormulaHandler {

	private static String xmlPath = WhatIfEngineConfig.getInstance().getEngineResourcePath() + "Olap/formulas.xml";
	private static File xmlFile;

	public MDXFormulaHandler() {

	}

	private static boolean loadFile() {

		xmlFile = new File(xmlPath);

		return xmlFile.exists();

	}

	public static MDXFormulas getFormulas() throws JAXBException {
		MDXFormulas formulas = new MDXFormulas();

		if (loadFile()) {
			JAXBContext jc = JAXBContext.newInstance(MDXFormulas.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			formulas = (MDXFormulas) unmarshaller.unmarshal(xmlFile);
		}

		return formulas;
	};

}
