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

import java.io.InputStream;

import javax.xml.XMLConstants;
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

import it.eng.knowage.meta.initializer.InitializationException;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.ModelObject;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.ModelPropertyCategory;
import it.eng.knowage.meta.model.ModelPropertyType;
import it.eng.knowage.meta.model.business.BusinessIdentifier;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessRelationship;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.BusinessView;
import it.eng.knowage.meta.model.business.CalculatedBusinessColumn;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;

/**
 * @author cortella
 *
 */
public class BusinessModelPropertiesFromFileInitializer implements IPropertiesInitializer {

	private Document document;

	public static final String MODEL_INITIALIZER_NAME = "structural.initializer.name";
	public static final String MODEL_INITIALIZER_VERSION = "structural.initializer.version";
	public static final String MODEL_USE_CATALOG_IN_MAPPING = "structural.mapping.useCatalog";
	public static final String MODEL_USE_SCHEMA_IN_MAPPING = "structural.mapping.useSchema";
	public static final String COLUMN_PHYSICAL_TABLE = "physical.physicaltable";
	public static final String ROLE_DESTINATION = "structural.destinationRole";
	public static final String RELATIONSHIP_CARDINALITY = "structural.cardinality";
	public static final String CALCULATED_COLUMN_EXPRESSION = "structural.expression";
	public static final String CALCULATED_COLUMN_DATATYPE = "structural.datatype";
	public static final String CALCULATED_COLUMN_TYPE = "structural.columntype";
	public static final String COLUMN_DATATYPE = "structural.datatype";
	public static final String FORCE_SUBENTITY_VISIBILITY = "structural.forceVisibilityAsSubentity";
	public static final String COLUMN_ROLES_NOT_ENABLED = "behavioural.notEnabledRoles";
	public static final String COLUMN_CUSTOM_FUNCTION = "structural.customFunction";

	public static final String TABLE_TYPE = "structural.tabletype";

	public static final ModelFactory FACTORY = ModelFactory.eINSTANCE;

	private static Logger logger = LoggerFactory.getLogger(BusinessModelPropertiesFromFileInitializer.class);

	public BusinessModelPropertiesFromFileInitializer() {

		logger.trace("IN");
		try {
			/**
			 * TODO REVIEW FOR PORTING
			 */
			// File propertiesFile = RL.getFile("properties/customProperties.xml") here before
			InputStream is = getClass().getClassLoader().getResourceAsStream("it/eng/knowage/meta/initializer/properties/custom/customProperties.xml");
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newDefaultInstance();
			domFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			domFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			document = builder.parse(is);
		} catch (Exception e) {
			throw new InitializationException("Impossible to load properties from configuration file", e);
		} finally {
			logger.trace("OUT");
		}
	}

	@Override
	public void addProperties(ModelObject o) {

		if (o instanceof BusinessModel) {
			initModelProperties((BusinessModel) o);
		} else if (o instanceof BusinessTable) {
			initTableProperties((BusinessTable) o);
		} else if (o instanceof BusinessView) {
			initViewProperties((BusinessView) o);
		} else if (o instanceof SimpleBusinessColumn) {
			initColumnProperties((SimpleBusinessColumn) o);
		} else if (o instanceof CalculatedBusinessColumn) {
			initCalculatedColumnProperties((CalculatedBusinessColumn) o);
		} else if (o instanceof BusinessIdentifier) {
			initIdentifierProperties((BusinessIdentifier) o);
		} else if (o instanceof BusinessRelationship) {
			initRelationshipProperties((BusinessRelationship) o);
		}
	}

	private void initModelProperties(BusinessModel o) {

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
			logger.error("initModelProperties",e);
		}

	}

	private void initTableProperties(BusinessTable o) {
		try {
			// 1- Search model categories definitions
			NodeList nodes = readXMLNodes(document, "/properties/table/categories/category");
			initeModelPropertyCategories(nodes, o.getModel().getParentModel());

			// 2- Search model types definitions
			nodes = readXMLNodes(document, "/properties/table/types/type");
			initModelPropertyTypes(nodes, o.getModel().getParentModel(), o);

			// 3- Search model admissible types values definitions
			nodes = readXMLNodes(document, "/properties/table/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, o.getModel().getParentModel());
		} catch (Exception e) {
			logger.error("initTableProperties",e);
		}
	}

	private void initViewProperties(BusinessView o) {
		try {
			// 1- Search model categories definitions
			NodeList nodes = readXMLNodes(document, "/properties/table/categories/category");
			initeModelPropertyCategories(nodes, o.getModel().getParentModel());

			// 2- Search model types definitions
			nodes = readXMLNodes(document, "/properties/table/types/type");
			initModelPropertyTypes(nodes, o.getModel().getParentModel(), o);

			// 3- Search model admissible types values definitions
			nodes = readXMLNodes(document, "/properties/table/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, o.getModel().getParentModel());
		} catch (Exception e) {
			logger.error("initViewProperties",e);
		}
	}

	private void initColumnProperties(SimpleBusinessColumn o) {
		NodeList nodes;
		Model rootModel = null;

		int nodesLength;
		try {
			if (o.getTable() != null && o.getTable().getModel() != null) {
				rootModel = o.getTable().getModel().getParentModel();
			}
			// 1- Search column categories definitions
			nodes = readXMLNodes(document, "/properties/column/categories/category");
			initeModelPropertyCategories(nodes, rootModel);

			// 2- Search column types definitions
			nodes = readXMLNodes(document, "/properties/column/types/type");
			nodesLength = nodes.getLength();
			initModelPropertyTypes(nodes, rootModel, o);

			// 3- Search column admissible types values definitions
			nodes = readXMLNodes(document, "/properties/column/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, rootModel);

			// *********************************************************************
			// THIS WILL ADD PHYSICAL TABLE REFERENCE PROPERTY FOR BUSINESS COLUMN
			// This is not a custom property readed from xml file
			// *********************************************************************

			ModelPropertyCategory physicalReferenceCategory = o.getTable().getModel().getParentModel().getPropertyCategory("Physical References");
			if (physicalReferenceCategory == null) {
				physicalReferenceCategory = FACTORY.createModelPropertyCategory();
				physicalReferenceCategory.setName("Physical References");
				physicalReferenceCategory.setDescription("The reference to the original physical object");
				o.getTable().getModel().getParentModel().getPropertyCategories().add(physicalReferenceCategory);
			}

			// Column Physical Table TYPE
			ModelPropertyType propertyType = null;

			if (rootModel != null)
				propertyType = rootModel.getPropertyType(COLUMN_PHYSICAL_TABLE);
			if (propertyType == null) {
				propertyType = FACTORY.createModelPropertyType();
				propertyType.setId(COLUMN_PHYSICAL_TABLE);
				propertyType.setName("Physical table");
				propertyType.setDescription("The original physical table of this column");
				propertyType.setCategory(physicalReferenceCategory);
				propertyType.setDefaultValue(o.getPhysicalColumn().getTable().getName());

				if (rootModel != null)
					rootModel.getPropertyTypes().add(propertyType);
			}

			ModelProperty property = FACTORY.createModelProperty();
			property.setPropertyType(propertyType);
			o.getProperties().put(property.getPropertyType().getId(), property);

			// **************************************************************************

		} catch (Exception e) {
			logger.error("initColumnProperties",e);
		}

	}

	private void initCalculatedColumnProperties(CalculatedBusinessColumn o) {
		NodeList nodes;
		Model rootModel = null;

		int nodesLength;
		try {
			if (o.getTable() != null && o.getTable().getModel() != null) {
				rootModel = o.getTable().getModel().getParentModel();
			}
			// 1- Search column categories definitions
			nodes = readXMLNodes(document, "/properties/calculatedcolumn/categories/category");
			initeModelPropertyCategories(nodes, rootModel);

			// 2- Search column types definitions
			nodes = readXMLNodes(document, "/properties/calculatedcolumn/types/type");
			nodesLength = nodes.getLength();
			initModelPropertyTypes(nodes, rootModel, o);

			// 3- Search column admissible types values definitions
			nodes = readXMLNodes(document, "/properties/calculatedcolumn/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, rootModel);

		} catch (Exception e) {
			logger.error("initCalculatedColumnProperties",e);
		}

	}

	private void initIdentifierProperties(BusinessIdentifier o) {

	}

	private void initRelationshipProperties(BusinessRelationship o) {
		try {
			// 1- Search relationship categories definitions
			NodeList nodes = readXMLNodes(document, "/properties/relationship/categories/category");
			initeModelPropertyCategories(nodes, o.getModel().getParentModel());

			// 2- Search relationship types definitions
			nodes = readXMLNodes(document, "/properties/relationship/types/type");
			initModelPropertyTypes(nodes, o.getModel().getParentModel(), o);

			// 3- Search relationship admissible types values definitions
			nodes = readXMLNodes(document, "/properties/relationship/typesValues/admissibleValuesOf");
			initModelAdmissibleValues(nodes, o.getModel().getParentModel());
		} catch (Exception e) {
			logger.error("initRelationshipProperties",e);
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
					/**
					 * TODO REVIEW FOR PORTING
					 */
					if ("value".equalsIgnoreCase(nodeName)) {
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
		} else if (o instanceof BusinessModel) {
			categories = ((BusinessModel) o).getParentModel().getPropertyCategories();
		} else if (o instanceof BusinessTable) {
			categories = ((BusinessTable) o).getModel().getParentModel().getPropertyCategories();
		} else if (o instanceof BusinessView) {
			categories = ((BusinessView) o).getModel().getParentModel().getPropertyCategories();
		} else if (o instanceof SimpleBusinessColumn) {
			categories = ((SimpleBusinessColumn) o).getTable().getModel().getParentModel().getPropertyCategories();
		} else if (o instanceof CalculatedBusinessColumn) {
			categories = ((CalculatedBusinessColumn) o).getTable().getModel().getParentModel().getPropertyCategories();
		} else if (o instanceof BusinessIdentifier) {
			categories = ((BusinessIdentifier) o).getModel().getParentModel().getPropertyCategories();
		} else if (o instanceof BusinessRelationship) {
			categories = ((BusinessRelationship) o).getModel().getParentModel().getPropertyCategories();
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
		} else if (o instanceof BusinessModel) {
			types = ((BusinessModel) o).getParentModel().getPropertyTypes();
		} else if (o instanceof BusinessTable) {
			types = ((BusinessTable) o).getModel().getParentModel().getPropertyTypes();
		} else if (o instanceof BusinessView) {
			types = ((BusinessView) o).getModel().getParentModel().getPropertyTypes();
		} else if (o instanceof SimpleBusinessColumn) {
			types = ((SimpleBusinessColumn) o).getTable().getModel().getParentModel().getPropertyTypes();
		} else if (o instanceof CalculatedBusinessColumn) {
			types = ((CalculatedBusinessColumn) o).getTable().getModel().getParentModel().getPropertyTypes();
		} else if (o instanceof BusinessIdentifier) {
			types = ((BusinessIdentifier) o).getModel().getParentModel().getPropertyTypes();
		} else if (o instanceof BusinessRelationship) {
			types = ((BusinessRelationship) o).getModel().getParentModel().getPropertyTypes();
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
