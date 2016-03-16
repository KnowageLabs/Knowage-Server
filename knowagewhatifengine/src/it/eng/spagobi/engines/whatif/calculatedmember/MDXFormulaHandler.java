package it.eng.spagobi.engines.whatif.calculatedmember;

import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class MDXFormulaHandler {

	private String xmlPath;

	public static List<MDXFormula> getFormulas() throws JAXBException {

		String b = WhatIfEngineConfig.getInstance().getEngineResourcePath();
		File file = new File(b + "Olap/formulas.xml");
		JAXBContext jc = JAXBContext.newInstance(MDXFormulas.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		MDXFormulas formulas = (MDXFormulas) unmarshaller.unmarshal(file);

		return formulas.getFormulas();
	};

}
