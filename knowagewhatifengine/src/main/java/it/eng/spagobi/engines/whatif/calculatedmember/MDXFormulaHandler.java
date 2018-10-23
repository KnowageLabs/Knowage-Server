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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;

import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;

public class MDXFormulaHandler {

	private static final String SERVER_RESOURCE_FILE_PATH = WhatIfEngineConfig.getInstance().getEngineResourcePath() + "Olap/formulas.xml";;
	private static final String JAVA_RESOURCE_FILE_PATH = File.separatorChar + "calculated_fields_formulas" + File.separatorChar + "formulas.xml";
	private static File xmlFile;
	private static MDXFormulas formulas;
	private static Map<String, String> placeHolders = new HashMap<String, String>();
	private static String TIME_DIMENSION = "TimeDimension";
	private static SpagoBIPivotModel model;
	private static ModelConfig modelConfig;
	private static ClassLoader classLoader = MDXFormulaHandler.class.getClassLoader();
	private static Logger logger = Logger.getLogger(MDXFormulaHandler.class);

	public static void main(String[] args) throws JAXBException, InstantiationException, IllegalAccessException {
		loadFile();
		getFormulasFromXML2();
	}

	public static SpagoBIPivotModel getModel() {
		return model;
	}

	public static void setModel(SpagoBIPivotModel model) {
		MDXFormulaHandler.model = model;
	}

	public static ModelConfig getModelConfig() {
		return modelConfig;
	}

	public static void setModelConfig(ModelConfig modelConfig) {
		MDXFormulaHandler.modelConfig = modelConfig;
	}

	private static boolean loadFile() {

		xmlFile = new File(SERVER_RESOURCE_FILE_PATH);
		try {
			if (!xmlFile.exists()) {
				URL resource = classLoader.getResource(JAVA_RESOURCE_FILE_PATH);
				if (resource != null) {
					xmlFile = new File(resource.getPath());
				}

			}
		} catch (Exception e) {
			logger.error("Can not load MDX formulas", e);
		}

		return xmlFile.exists();

	}

	private static MDXFormulas getFormulasFromXML() throws JAXBException {
		formulas = new MDXFormulas();

		if (loadFile()) {
			JAXBContext jc = JAXBContext.newInstance(MDXFormulas.class);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			formulas = (MDXFormulas) unmarshaller.unmarshal(xmlFile);
		}
		return formulas;

	};

	private static MDXFormulas getFormulasFromXML2() throws JAXBException, InstantiationException, IllegalAccessException {
		Box<MDXFormulas> box = new Box<MDXFormulas>();
		formulas = box.unmarshalFile(xmlFile, MDXFormulas.class);
		return formulas;
	}

	public static MDXFormulas getFormulas() throws JAXBException {

		String selectedTimeHierarchyName = getSelectedTimeHierarchyName();
		getFormulasFromXML();

		if (selectedTimeHierarchyName != null) {
			placeHolders.put(TIME_DIMENSION, getSelectedTimeHierarchyName());
			injectTimeDimensionInDefaultAgumentValue();
			injectTimeDimensionInBody();
		}

		return formulas;

	}

	private static String getSelectedTimeHierarchyName() {

		Hierarchy h = null;
		List<Dimension> dimensions = CubeUtilities.getDimensions(model.getCube().getHierarchies());
		for (Dimension dimension : dimensions) {
			try {
				if (dimension.getDimensionType().name().equalsIgnoreCase("Time")) {
					String selectedHierarchyName = modelConfig.getDimensionHierarchyMap().get(dimension.getUniqueName());

					if (selectedHierarchyName == null) {
						h = dimension.getDefaultHierarchy();
						return h.getUniqueName();

					} else {
						try {
							h = CubeUtilities.getHierarchy(model.getCube(), selectedHierarchyName);
						} catch (OlapException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return h.getUniqueName();
					}
				}
			} catch (OlapException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;

	}

	private static void injectTimeDimensionInDefaultAgumentValue() {

		for (int i = 0; i < formulas.getFormulas().size(); i++) {

			for (int j = 0; j < formulas.getFormulas().get(i).getArguments().size(); j++) {

				String defaultAgumentValue = formulas.getFormulas().get(i).getArguments().get(j).getDefault_value();
				Iterator it = placeHolders.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();

					defaultAgumentValue = defaultAgumentValue.replaceAll(pair.getKey().toString(), pair.getValue().toString());
					formulas.getFormulas().get(i).getArguments().get(j).setDefault_value(defaultAgumentValue);

				}

			}
		}
	}

	private static void injectTimeDimensionInBody() {

		for (int i = 0; i < formulas.getFormulas().size(); i++) {

			String body = formulas.getFormulas().get(i).getBody();
			Iterator it = placeHolders.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				if (body != null) {
					body = body.replaceAll(pair.getKey().toString(), pair.getValue().toString());
					formulas.getFormulas().get(i).setBody(body);
				}
			}
		}
	}

}

class Box<T> {

	@SuppressWarnings("unchecked")
	public T unmarshalFile(File file, Class<T> clazz) throws JAXBException {

		JAXBContext jc = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		return (T) unmarshaller.unmarshal(file);

	}
}
