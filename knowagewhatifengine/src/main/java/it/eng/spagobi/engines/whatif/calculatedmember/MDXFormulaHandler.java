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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;

import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;

public class MDXFormulaHandler {

	private static final Logger LOGGER = Logger.getLogger(MDXFormulaHandler.class);
	private static final String SERVER_RESOURCE_FILE_PATH = WhatIfEngineConfig.getInstance().getEngineResourcePath()
			+ "Olap/formulas.xml";
	private static final String JAVA_RESOURCE_FILE_PATH = File.separatorChar + "calculated_fields_formulas"
			+ File.separatorChar + "formulas.xml";
	private static final String TIME_DIMENSION = "TimeDimension";

	private static File xmlFile;
	private static MDXFormulas formulas;
	private static Map<String, String> placeHolders = new HashMap<>();
	private static SpagoBIPivotModel model;
	private static ModelConfig modelConfig;
	private static ClassLoader classLoader = MDXFormulaHandler.class.getClassLoader();



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
			LOGGER.error("Can not load MDX formulas", e);
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

	}

	private static MDXFormulas getFormulasFromXML2() throws JAXBException {
		Box<MDXFormulas> box = new Box<>();
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
					String selectedHierarchyName = modelConfig.getDimensionHierarchyMap()
							.get(dimension.getUniqueName());

					if (selectedHierarchyName == null) {
						h = dimension.getDefaultHierarchy();
						return h.getUniqueName();

					} else {
						h = CubeUtilities.getHierarchy(model.getCube(), selectedHierarchyName);
						return h.getUniqueName();
					}
				}
			} catch (OlapException e) {
				LOGGER.error("Error getting selected time hierarchy name", e);
			}
		}
		return null;

	}

	private static void injectTimeDimensionInDefaultAgumentValue() {

		for (int i = 0; i < formulas.getFormulas().size(); i++) {

			for (int j = 0; j < formulas.getFormulas().get(i).getArguments().size(); j++) {

				String defaultAgumentValue = formulas.getFormulas().get(i).getArguments().get(j).getDefault_value();
				Iterator<Entry<String, String>> it = placeHolders.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> pair = it.next();

					defaultAgumentValue = defaultAgumentValue.replaceAll(pair.getKey(), pair.getValue());
					formulas.getFormulas().get(i).getArguments().get(j).setDefault_value(defaultAgumentValue);

				}

			}
		}
	}

	private static void injectTimeDimensionInBody() {

		for (int i = 0; i < formulas.getFormulas().size(); i++) {

			String body = formulas.getFormulas().get(i).getBody();
			Iterator<Entry<String, String>> it = placeHolders.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> pair = it.next();
				if (body != null) {
					body = body.replaceAll(pair.getKey(), pair.getValue());
					formulas.getFormulas().get(i).setBody(body);
				}
			}
		}
	}

}

class Box<T> {

	private static final Logger LOGGER = Logger.getLogger(Box.class);

	@SuppressWarnings("unchecked")
	public T unmarshalFile(File file, Class<T> clazz) throws JAXBException {

		JAXBContext jc = JAXBContext.newInstance(clazz);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
		xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

		try (FileInputStream inputStream = new FileInputStream(file);
				InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(reader));

			return (T) jc.createUnmarshaller().unmarshal(xsr);
		} catch (IOException | XMLStreamException e) {
			LOGGER.error("Error loading XML document: " + e.getMessage(), e);
			throw new RuntimeException("Error loading XML document: " + e.getMessage(), e);
		}
	}
}
