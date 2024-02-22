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
package it.eng.knowage.meta.initializer.properties;

import it.eng.knowage.meta.initializer.InitializationException;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.ModelPropertyCategory;
import it.eng.knowage.meta.model.ModelPropertyType;
import it.eng.knowage.meta.model.olap.Cube;
import it.eng.knowage.meta.model.olap.Dimension;
import it.eng.knowage.meta.model.olap.Hierarchy;
import it.eng.knowage.meta.model.olap.Level;
import it.eng.knowage.meta.model.olap.Measure;
import it.eng.knowage.meta.model.olap.OlapModel;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.emf.common.util.EList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author cortella
 *
 */
public class OlapModelPropertiesFromFileInitializer implements IPropertiesInitializer {

	private Document document;

	public static final String LEVEL_UNIQUE_MEMBERS = "structural.uniquemembers";
	public static final String LEVEL_TYPE = "structural.leveltype";
	public static final String HIERARCHY_HAS_ALL = "structural.hasall";
	public static final String HIERARCHY_IS_DEFAULT = "structural.defaultHierarchy";
	public static final String HIERARCHY_ALL_MEMBER_NAME = "structural.allmembername";

	public static ModelFactory FACTORY = ModelFactory.eINSTANCE;
	// public static IResourceLocator RL = SpagoBIMetaInitializerPlugin.getInstance().getResourceLocator();

	private static Logger logger = LoggerFactory.getLogger(OlapModelPropertiesFromFileInitializer.class);

	public OlapModelPropertiesFromFileInitializer() {

		logger.trace("IN");
		try {
			/**
			 * TODO REVIEW FOR PORTING
			 */
			// File propertiesFile = RL.getFile("properties/customOlapProperties.xml");
			InputStream is = getClass().getClassLoader().getResourceAsStream("it/eng/knowage/meta/initializer/properties/custom/customOlapProperties.xml");
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			document = builder.parse(is);
		} catch (Throwable t) {
			throw new InitializationException("Impossible to load properties from configuration file", t);
		} finally {
			logger.trace("OUT");
		}
	}

	@Override
	public void addProperties(ModelObject o) {

		if (o instanceof OlapModel) {
			initModelProperties((OlapModel) o);
		} else if (o instanceof Cube) {
			initCubeProperties((Cube) o);
		} else if (o instanceof Dimension) {
			initDimensionProperties((Dimension) o);
		} else if (o instanceof Hierarchy) {
			initHierarchyProperties((Hierarchy) o);
		} else if (o instanceof Level) {
			initLevelProperties((Level) o);
		} else if (o instanceof Measure) {
			initMeasureProperties((Measure) o);
		}

	}

	private void initModelProperties(OlapModel o) {

		try {

			// 1- Search model categories definitions
			NodeList nodes = readXMLNodes(document, "/properties/model/categories/category");
			initeModelPropertyCategories(nodes, o.getParentModel());

			// 2- Search model types definitions
			nodes = readXMLNodes(document, "/properties/model/types/type");
			initModelPropertyTypes(nodes, o.getParentModel(), o);

			// 3- Search model admissible types values definitions
			nodes = readXMLNodes(document, "/properties/model/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, o.getParentModel());

		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("initModelProperties",e);
		}

	}

	private void initCubeProperties(Cube o) {
		try {
			// 1- Search model categories definitions
			NodeList nodes = readXMLNodes(document, "/properties/cube/categories/category");
			initeModelPropertyCategories(nodes, o.getModel().getParentModel());

			// 2- Search model types definitions
			nodes = readXMLNodes(document, "/properties/cube/types/type");
			initModelPropertyTypes(nodes, o.getModel().getParentModel(), o);

			// 3- Search model admissible types values definitions
			nodes = readXMLNodes(document, "/properties/cube/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, o.getModel().getParentModel());
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("initCubeProperties",e);
		}
	}

	private void initDimensionProperties(Dimension o) {
		try {
			// 1- Search model categories definitions
			NodeList nodes = readXMLNodes(document, "/properties/dimension/categories/category");
			initeModelPropertyCategories(nodes, o.getModel().getParentModel());

			// 2- Search model types definitions
			nodes = readXMLNodes(document, "/properties/dimension/types/type");
			initModelPropertyTypes(nodes, o.getModel().getParentModel(), o);

			// 3- Search model admissible types values definitions
			nodes = readXMLNodes(document, "/properties/dimension/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, o.getModel().getParentModel());
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("initDimensionProperties",e);
		}
	}

	private void initHierarchyProperties(Hierarchy o) {
		try {
			// 1- Search model categories definitions
			NodeList nodes = readXMLNodes(document, "/properties/hierarchy/categories/category");
			initeModelPropertyCategories(nodes, o.getDimension().getModel().getParentModel());

			// 2- Search model types definitions
			nodes = readXMLNodes(document, "/properties/hierarchy/types/type");
			initModelPropertyTypes(nodes, o.getDimension().getModel().getParentModel(), o);

			// 3- Search model admissible types values definitions
			nodes = readXMLNodes(document, "/properties/hierarchy/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, o.getDimension().getModel().getParentModel());
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("initHierarchyProperties",e);
		}
	}

	private void initLevelProperties(Level o) {
		try {
			// 1- Search model categories definitions
			NodeList nodes = readXMLNodes(document, "/properties/level/categories/category");
			initeModelPropertyCategories(nodes, o.getHierarchy().getDimension().getModel().getParentModel());

			// 2- Search model types definitions
			nodes = readXMLNodes(document, "/properties/level/types/type");
			initModelPropertyTypes(nodes, o.getHierarchy().getDimension().getModel().getParentModel(), o);

			// 3- Search model admissible types values definitions
			nodes = readXMLNodes(document, "/properties/level/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, o.getHierarchy().getDimension().getModel().getParentModel());
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("initLevelProperties",e);
		}
	}

	private void initMeasureProperties(Measure o) {
		try {
			// 1- Search model categories definitions
			NodeList nodes = readXMLNodes(document, "/properties/measure/categories/category");
			initeModelPropertyCategories(nodes, o.getCube().getModel().getParentModel());

			// 2- Search model types definitions
			nodes = readXMLNodes(document, "/properties/measure/types/type");
			initModelPropertyTypes(nodes, o.getCube().getModel().getParentModel(), o);

			// 3- Search model admissible types values definitions
			nodes = readXMLNodes(document, "/properties/measure/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, o.getCube().getModel().getParentModel());
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error("initMeasureProperties",e);
		}
	}

	private NodeList readXMLNodes(Document doc, String xpathExpression) throws Exception {
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile(xpathExpression);

		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		return nodes;
	}

	private void initeModelPropertyCategories(NodeList nodes, Model model) {
		int nodesLength = nodes.getLength();
		for (int i = 0; i < nodesLength; i++) {
			NamedNodeMap nodeAttributes = nodes.item(i).getAttributes();
			if (nodeAttributes != null) {
				String categoryName = nodeAttributes.getNamedItem("name").getNodeValue();
				String categoryDescription = nodeAttributes.getNamedItem("description").getNodeValue();

				// if doesn't exist, create a model category
				ModelPropertyCategory modelCategory = model.getPropertyCategory(categoryName);
				if (modelCategory == null) {
					modelCategory = FACTORY.createModelPropertyCategory();
					modelCategory.setName(categoryName);
					modelCategory.setDescription(categoryDescription);
					model.getPropertyCategories().add(modelCategory);
				}
			}
		}
	}

	private void initModelPropertyTypes(NodeList nodes, Model model, ModelObject o) {
		int nodesLength = nodes.getLength();
		for (int j = 0; j < nodesLength; j++) {
			NamedNodeMap nodeAttributes = nodes.item(j).getAttributes();
			if (nodeAttributes != null) {
				String typeId = nodeAttributes.getNamedItem("id").getNodeValue();
				String typeName = nodeAttributes.getNamedItem("name").getNodeValue();
				String typeDescription = nodeAttributes.getNamedItem("description").getNodeValue();
				String typeCategory = nodeAttributes.getNamedItem("category").getNodeValue();
				String typeDefaultValue = nodeAttributes.getNamedItem("defaultValue").getNodeValue();

				// Create the new property type
				ModelPropertyType propertyType = null;

				if (model != null) {
					propertyType = model.getPropertyType(typeId);
				}
				if (propertyType == null) {
					propertyType = FACTORY.createModelPropertyType();
					propertyType.setId(typeId);
					propertyType.setName(typeName);
					propertyType.setDescription(typeDescription);
					propertyType.setCategory(getModelPropertyCategory(model, typeCategory));
					propertyType.setDefaultValue(typeDefaultValue);

					if (model != null) {
						model.getPropertyTypes().add(propertyType);
					}
				}

				// add a model property type for model object
				ModelProperty property = FACTORY.createModelProperty();
				property.setPropertyType(propertyType);
				o.getProperties().put(property.getPropertyType().getId(), property);
			}
		}
	}

	private void initModelAdmissibleValues(NodeList nodes, Model model) throws Exception {
		int nodesLength = nodes.getLength();
		for (int j = 0; j < nodesLength; j++) {
			NamedNodeMap nodeAttributes = nodes.item(j).getAttributes();
			if (nodeAttributes != null) {
				String typeId = nodeAttributes.getNamedItem("typeId").getNodeValue();
				ModelPropertyType propertyType = getModelPropertyType(model, typeId);

				NodeList values = nodes.item(j).getChildNodes();

				for (int z = 0; z < values.getLength(); z++) {
					Node n = values.item(z);
					String nodeName = n.getNodeName();
					if ("value".equalsIgnoreCase(nodeName)) {
						/**
						 * TODO REVIEW FOR PORTING
						 */
						String value = values.item(z).getTextContent();
						propertyType.getAdmissibleValues().add(value);
					}
				}
			}
		}
	}

	// Utility methods
	// -----------------------------------------------------------------------

	private ModelPropertyCategory getModelPropertyCategory(Object o, String categoryName) {
		EList<ModelPropertyCategory> categories = null;
		if (o instanceof Model) {
			categories = ((Model) o).getPropertyCategories();
		} else if (o instanceof OlapModel) {
			categories = ((OlapModel) o).getParentModel().getPropertyCategories();
		} else if (o instanceof Cube) {
			categories = ((Cube) o).getModel().getParentModel().getPropertyCategories();
		} else if (o instanceof Dimension) {
			categories = ((Dimension) o).getModel().getParentModel().getPropertyCategories();
		} else if (o instanceof Hierarchy) {
			categories = ((Hierarchy) o).getDimension().getModel().getParentModel().getPropertyCategories();
		} else if (o instanceof Level) {
			categories = ((Level) o).getHierarchy().getDimension().getModel().getParentModel().getPropertyCategories();
		} else if (o instanceof Measure) {
			categories = ((Measure) o).getCube().getModel().getParentModel().getPropertyCategories();
		}

		if (categories != null) {
			for (ModelPropertyCategory category : categories) {
				if (category.getName().equalsIgnoreCase(categoryName)) {
					return category;
				}
			}
		}
		return null;
	}

	private ModelPropertyType getModelPropertyType(Object o, String typeId) {
		EList<ModelPropertyType> types = null;

		if (o instanceof Model) {
			types = ((Model) o).getPropertyTypes();
		} else if (o instanceof OlapModel) {
			types = ((OlapModel) o).getParentModel().getPropertyTypes();
		} else if (o instanceof Cube) {
			types = ((Cube) o).getModel().getParentModel().getPropertyTypes();
		} else if (o instanceof Dimension) {
			types = ((Dimension) o).getModel().getParentModel().getPropertyTypes();
		} else if (o instanceof Hierarchy) {
			types = ((Hierarchy) o).getDimension().getModel().getParentModel().getPropertyTypes();
		} else if (o instanceof Level) {
			types = ((Level) o).getHierarchy().getDimension().getModel().getParentModel().getPropertyTypes();
		} else if (o instanceof Measure) {
			types = ((Measure) o).getCube().getModel().getParentModel().getPropertyTypes();
		}

		if (types != null) {
			for (ModelPropertyType type : types) {
				if (type.getId().equalsIgnoreCase(typeId)) {
					return type;
				}
			}
		}
		return null;
	}

}
