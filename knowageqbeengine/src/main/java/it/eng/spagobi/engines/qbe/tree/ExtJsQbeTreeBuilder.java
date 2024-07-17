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
package it.eng.spagobi.engines.qbe.tree;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.properties.IModelProperties;
import it.eng.qbe.model.properties.SimpleModelProperties;
import it.eng.qbe.model.structure.FilteredModelStructure;
import it.eng.qbe.model.structure.HierarchicalDimensionField;
import it.eng.qbe.model.structure.Hierarchy;
import it.eng.qbe.model.structure.HierarchyLevel;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.ModelCalculatedField;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot;
import it.eng.qbe.model.structure.filter.QbeTreeFilter;
import it.eng.qbe.query.serializer.json.QueryJSONSerializer;
import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.serializer.json.QbeSerializationConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.messages.EngineMessageBundle;

/**
 * The Class ExtJsQbeTreeBuilder.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class ExtJsQbeTreeBuilder {

	private QbeTreeFilter qbeTreeFilter;

	private IDataSource dataSource;

	private Locale locale;

	private IModelProperties datamartLabels;

	public static final String NODE_TYPE_ENTITY = "entity";
	public static final String NODE_TYPE_SIMPLE_FIELD = "field";
	public static final String NODE_TYPE_CALCULATED_FIELD = "calculatedField";
	public static final String NODE_TYPE_HIERARCHY_FIELD = "hierarchyField";
	public static final String NODE_TYPE_HIERARCHY_LEVEL_FIELD = "hierarchyLevelField";
	public static final String NODE_TYPE_INLINE_CALCULATED_FIELD = "inLineCalculatedField";
	public static final String NODE_TYPE_RELATION_FIELD = "relation";
	private static Logger logger = Logger.getLogger(ExtJsQbeTreeBuilder.class);
	/**
	 * Instantiates a new ext js qbe tree builder.
	 *
	 * @param qbeTreeFilter the qbe tree filter
	 */
	public ExtJsQbeTreeBuilder(QbeTreeFilter qbeTreeFilter) {
		setQbeTreeFilter(qbeTreeFilter);
	}

	public JSONArray getQbeTree(IDataSource dataSource, Locale locale, String datamartName, UserProfile userProfile) {
		setLocale(locale);
		setDatamartModel(dataSource);
		setDatamartLabels(dataSource.getModelI18NProperties(getLocale()));
		if (getDatamartLabels() == null) {
			setDatamartLabels(new SimpleModelProperties());
		}
		return buildQbeTree(datamartName, userProfile);
	}

	private String getEntityLabel(IModelEntity entity) {
		String label;
		label = getDatamartLabels().getProperty(entity, "label");
		if (label == null) {
			label = entity.getPropertyAsString("label");
		}
		return StringUtils.isEmpty(label) ? entity.getName() : label;
	}

	private String getEntityTooltip(IModelEntity entity) {
		String tooltip = getDatamartLabels().getProperty(entity, "tooltip");
		return tooltip != null ? tooltip : "";
	}

	private String getFieldLabel(IModelField field) {
		String label;
		label = getDatamartLabels().getProperty(field, "label");
		if (StringUtils.isEmpty(label)) {
			IModelEntity parentEntity = field.getParent();
			// IModelEntity parentEntity = field.getLogicalParent();
			IModelEntity rootEntity = field.getStructure().getRootEntity(parentEntity);
			IModelField rootField = rootEntity.getFieldByName(field.getName());
			label = getDatamartLabels().getProperty(rootField, "label");
		}
		return StringUtils.isEmpty(label) ? field.getName() : label;
	}

	private String getFieldTooltip(IModelField field) {
		String tooltip = getDatamartLabels().getProperty(field, "tooltip");
		return tooltip != null ? tooltip : "";
	}

	public PrintWriter writer;

	/**
	 * Builds the qbe tree.
	 *
	 * @param datamartName the datamart name
	 *
	 * @return the jSON array
	 */
	private JSONArray buildQbeTree(String datamartName, UserProfile userProfile) {
		JSONArray nodes = new JSONArray();
		File file = new File(new File(ConfigSingleton.getRootPath()), "labels.properties");
		try {
			writer = new PrintWriter(new FileWriter(file));
		} catch (IOException e) {
			logger.error("PrintWriter",e);
			writer = new PrintWriter(new CharArrayWriter());
		}
		addEntityNodes(nodes, datamartName, userProfile);
		writer.flush();
		writer.close();
		return nodes;
	}

	/**
	 * Adds the entity nodes.
	 *
	 * @param nodes        the nodes
	 * @param datamartName the datamart name
	 */
	public void addEntityNodes(JSONArray nodes, String datamartName, UserProfile userProfile) {
		FilteredModelStructure filteredModelStructure = new FilteredModelStructure(
				(dataSource).getModelStructure(userProfile), getDataSource(), getQbeTreeFilter());
		List<IModelEntity> entities = filteredModelStructure.getRootEntities(datamartName);

		Iterator<IModelEntity> it = entities.iterator();
		while (it.hasNext()) {
			IModelEntity entity = it.next();
			addEntityNode(nodes, entity, 1);
		}
	}

	/**
	 * Adds the entity node.
	 *
	 * @param nodes          the nodes
	 * @param entity         the entity
	 * @param recursionLevel the recursion level
	 */
	public void addEntityNode(JSONArray nodes, IModelEntity entity, int recursionLevel) {

		addEntityRootNode(nodes, entity, recursionLevel);
	}

	/**
	 * Adds the entity root node.
	 *
	 * @param nodes          the nodes
	 * @param entity         the entity
	 * @param recursionLevel the recursion level
	 */
	public void addEntityRootNode(JSONArray nodes, IModelEntity entity, int recursionLevel) {

		// DatamartProperties datamartProperties =
		// dataSource.getDataMartProperties();
		String iconCls = entity.getPropertyAsString("type");
		String label = getEntityLabel(entity);
		String londDescription = QueryJSONSerializer.getEntityLongDescription(entity, getDatamartLabels());
		String tooltip = getEntityTooltip(entity);
		String linkedToWords = entity.getPropertyAsString("linkedToWords");

		writer.println("\n\n####################################################");
		writer.println(entity.getUniqueName().replaceAll(":", "/") + "=");
		writer.println(entity.getUniqueName().replaceAll(":", "/") + ".tooltip=");

		JSONArray childrenNodes = getFieldNodes(entity, recursionLevel);

		JSONObject entityNode = new JSONObject();

		JSONArray relationNodes = getRelationNodes(entity, recursionLevel);

		try {
			entityNode.put("id", entity.getUniqueName());
			entityNode.put("text", label);
			entityNode.put("iconCls", iconCls);
			entityNode.put("qtip", tooltip);

			JSONObject nodeAttributes = new JSONObject();
			nodeAttributes.put("iconCls", iconCls);
			nodeAttributes.put("type", NODE_TYPE_ENTITY);
			nodeAttributes.put("londDescription", londDescription);
			nodeAttributes.put("linkedToWords", new Boolean(linkedToWords)); // TO-DO
																			 // check
																			 // if
																			 // isabletoGlossary
																			 // and
																			 // if
																			 // this
																			 // nod
																			 // have
																			 // a
																			 // word
																			 // associated
			entityNode.put("attributes", nodeAttributes);
			entityNode.put("children", childrenNodes);
			entityNode.put("relation", relationNodes);

		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("error generating the relation node");
		}

		nodes.put(entityNode);
	}

	/**
	 * Gets the field nodes.
	 *
	 * @param entity         the entity
	 * @param recursionLevel the recursion level
	 *
	 * @return the field nodes
	 */
	public JSONArray getFieldNodes(IModelEntity entity, int recursionLevel) {

		JSONArray children = new JSONArray();

		// add hierarchy fields
		HierarchicalDimensionField dimField = entity.getHierarchicalDimensionByEntity(entity.getType());
		if (dimField != null) {
			List<Hierarchy> hierarchies = dimField.getHierarchies();
			Iterator<Hierarchy> hierarchiesIterator = hierarchies.iterator();
			while (hierarchiesIterator.hasNext()) {
				Hierarchy hierarchy = hierarchiesIterator.next();
				JSONObject jsObject = getHierarchyNode(entity, hierarchy);

				if (jsObject != null) {
					children.put(jsObject);
				}
			}
		}

		// add key fields
		List<IModelField> keyFields = entity.getKeyFields();

		Iterator<IModelField> keyFieldIterator = keyFields.iterator();
		while (keyFieldIterator.hasNext()) {
			IModelField field = keyFieldIterator.next();
			JSONObject jsObject = getFieldNode(entity, field);
			if (jsObject != null) {
				children.put(jsObject);
			}

		}

		// add normal fields
		List<IModelField> normalFields = entity.getNormalFields();

		Iterator<IModelField> normalFieldIterator = normalFields.iterator();
		while (normalFieldIterator.hasNext()) {
			IModelField field = normalFieldIterator.next();
			Boolean isARelation = (Boolean) field.getProperties().get("relation");
			JSONObject jsObject = getFieldNode(entity, field);
			if (jsObject != null && (isARelation == null || !isARelation)) {
				children.put(jsObject);
			}
		}

		// add calculated fields
		List<ModelCalculatedField> calculatedFields = entity.getCalculatedFields();

		Iterator<ModelCalculatedField> calculatedFieldIterator = calculatedFields.iterator();
		while (calculatedFieldIterator.hasNext()) {
			ModelCalculatedField field = calculatedFieldIterator.next();

			JSONObject jsObject = getCalculatedFieldNode(entity, field);
			if (jsObject != null) {
				children.put(jsObject);
			}
		}

		// add subentities
		getSubEntitiesNodes(entity, children, recursionLevel);

		return children;
	}

	public JSONArray getRelationNodes(IModelEntity entity, int recursionLevel) {
		JSONArray relation = new JSONArray();

		// add relations
		Set<Relationship> relations = entity.getStructure().getRootEntityDirectConnections(entity);
		if (relations != null) {
			Iterator<Relationship> iter = relations.iterator();
			while (iter.hasNext()) {
				Relationship relationship = iter.next();
				// if the source entity refer to another entity more than one
				// time, we print the relation name in the label of the relation
				// field
				boolean needRelationInLabel = (entity.getStructure().getDirectConnections(entity,
						relationship.getTargetEntity())).size() > 1;
				JSONObject jsObject = getRelationFieldNode(relationship, entity, needRelationInLabel);
				if (jsObject != null) {
					relation.put(jsObject);
				}
			}
		}

		return relation;
	}

	/**
	 * Gets the field node.
	 *
	 * @param parentEntity the parent entity
	 * @param field        the field
	 *
	 * @return the field node
	 */
	public JSONObject getFieldNode(IModelEntity parentEntity, IModelField field) {

		// DatamartProperties datamartProperties =
		// dataSource.getDataMartProperties();
		String iconCls = field.getPropertyAsString("type");
		String aggtype = field.getPropertyAsString("aggtype");
		String format = field.getPropertyAsString("format");
		boolean personal = Boolean.parseBoolean(field.getPropertyAsString("personal"));
		boolean decrypt = Boolean.parseBoolean(field.getPropertyAsString("decrypt"));
		boolean subjectId = Boolean.parseBoolean(field.getPropertyAsString("subjectId"));
		String fieldLabel = getFieldLabel(field);
		String longDescription = QueryJSONSerializer.getFieldLongDescription(field, getDatamartLabels(), null);
		String fieldTooltip = getFieldTooltip(field);
		String entityLabel = getEntityLabel(parentEntity);

		writer.println(field.getUniqueName().replaceAll(":", "/") + "=");
		writer.println(field.getUniqueName().replaceAll(":", "/") + ".tooltip=");

		JSONObject fieldNode = new JSONObject();
		try {
			fieldNode.put("id", field.getUniqueName());
			fieldNode.put("text", fieldLabel);
			fieldNode.put("iconCls", iconCls);
			fieldNode.put("dataType", field.getType());
			fieldNode.put("aggtype", aggtype);
			fieldNode.put("format", format);
			fieldNode.put("leaf", true);
			fieldNode.put("qtip", fieldTooltip);
			if (iconCls.equals(QuerySerializationConstants.CALENDAR)) {
				fieldNode.put("hidden", true);
			}

			JSONObject nodeAttributes = new JSONObject();
			nodeAttributes.put("iconCls", iconCls);
			nodeAttributes.put("type", NODE_TYPE_SIMPLE_FIELD);
			nodeAttributes.put("entity", entityLabel);
			nodeAttributes.put("field", fieldLabel);
			nodeAttributes.put("longDescription", longDescription);
			nodeAttributes.put("personal", personal);
			nodeAttributes.put("decrypt", decrypt);
			nodeAttributes.put("subjectId", subjectId);
			fieldNode.put("attributes", nodeAttributes);
		} catch (JSONException e) {
			logger.error(e);
		}

		return fieldNode;
	}

	public JSONObject getHierarchyNode(IModelEntity parentEntity, Hierarchy hierarchy) {
		JSONObject fieldNode = new JSONObject();
		String entityLabel = getEntityLabel(parentEntity);

		try {
			fieldNode.put("id", hierarchy.getName());
			fieldNode.put("text", hierarchy.getName());
			fieldNode.put("leaf", false);

			JSONObject nodeAttributes = new JSONObject();
			nodeAttributes.put("type", NODE_TYPE_HIERARCHY_FIELD);
			nodeAttributes.put("entity", entityLabel);
			nodeAttributes.put("isdefault", false);
			if (hierarchy.getIsDefault()) {
				fieldNode.put("cls", "default_hierarchy");
				nodeAttributes.put("isdefault", true);
			}
			fieldNode.put("attributes", nodeAttributes);

			JSONArray jsonlevels = new JSONArray();

			List<HierarchyLevel> levels = hierarchy.getLevels();
			Iterator<HierarchyLevel> levelsIterator = levels.iterator();
			while (levelsIterator.hasNext()) {
				HierarchyLevel level = levelsIterator.next();
				JSONObject jsObject = new JSONObject();
				jsObject.put("text", level.getName());
				jsObject.put("type", level.getType());
				jsObject.put("alias", parentEntity.getType() + ":" + level.getColumn());
				jsObject.put("leaf", true);

				JSONObject levelAttributes = new JSONObject();
				levelAttributes.put("type", NODE_TYPE_HIERARCHY_LEVEL_FIELD);
				levelAttributes.put("entity", entityLabel);
				// if(hierarchy.getIsDefault()){
				// fieldNode.put("cls", "default_hierarchy");
				// }
				jsObject.put("attributes", levelAttributes);

				if (jsObject != null) {
					jsonlevels.put(jsObject);
				}
			}

			fieldNode.put("children", jsonlevels);

		} catch (JSONException e) {
			logger.error(e);
		}
		return fieldNode;
	}

	public JSONObject getCalculatedFieldNode(IModelEntity parentEntity, ModelCalculatedField field) {

		String fieldLabel = getFieldLabel(field);
		String fieldTooltip = getFieldTooltip(field);
		String entityLabel = getEntityLabel(parentEntity);

		writer.println(field.getUniqueName().replaceAll(":", "/") + "=");
		writer.println(field.getUniqueName().replaceAll(":", "/") + ".tooltip=");

		JSONObject fieldNode = new JSONObject();
		try {
			fieldNode.put("id", field.getUniqueName());
			fieldNode.put("text", fieldLabel);
			fieldNode.put("leaf", true);
			fieldNode.put("iconCls", "calculation");
			fieldNode.put("qtip", fieldTooltip);

			JSONObject nodeAttributes = new JSONObject();
			nodeAttributes.put("iconCls", "calculation");
			if (field.isInLine()) {
				nodeAttributes.put("type", NODE_TYPE_INLINE_CALCULATED_FIELD);
			} else {
				nodeAttributes.put("type", NODE_TYPE_CALCULATED_FIELD);
			}

			nodeAttributes.put("entity", entityLabel);
			nodeAttributes.put("field", fieldLabel);

			JSONObject formState = new JSONObject();
			formState.put("alias", field.getName());
			formState.put("type", field.getType());
			formState.put("nature", field.getNature());
			formState.put("expression", field.getExpression());

			List<Slot> slots = field.getSlots();
			JSONArray slotsJSON = new JSONArray();

			if (field.getDefaultSlotValue() != null) {
				JSONObject defaultSlot = new JSONObject();
				defaultSlot.put(QbeSerializationConstants.SLOT_NAME, field.getDefaultSlotValue());
				JSONArray valuesets = new JSONArray();
				JSONObject valueset = new JSONObject();
				valueset.put(QbeSerializationConstants.SLOT_VALUESET_TYPE,
						QbeSerializationConstants.SLOT_VALUESET_TYPE_DEFAULT);
				valueset.put(QbeSerializationConstants.SLOT_VALUESET_VALUES, "");
				valuesets.put(valueset);
				defaultSlot.put(QbeSerializationConstants.SLOT_VALUESET, valuesets);
				slotsJSON.put(defaultSlot);
			}

			for (Slot slot : slots) {
				try {
					JSONObject slotJSON = (JSONObject) SerializationManager.serialize(slot, "application/json");
					slotsJSON.put(slotJSON);
				} catch (Throwable e) {
					throw new SpagoBIEngineRuntimeException("Impossible to serialize slots definition", e);
				}
			}

			formState.put("slots", slotsJSON);

			nodeAttributes.put("formState", formState);
			fieldNode.put("attributes", nodeAttributes);
		} catch (JSONException e) {
			logger.error(e);
		}

		return fieldNode;
	}

	public JSONObject getRelationFieldNode(Relationship relation, IModelEntity parentEntity,
			boolean needRelationInLabel) {
		String iconCls = "relation";
		String sourceText = relation.getSourceFieldsString();
		String targetText = relation.getTargetFieldsString();
		String relationId = relation.getId();
		String targetEntityLabel = getEntityLabel(relation.getTargetFields().get(0).getParent());
		String sourceEntityLabel = getEntityLabel(relation.getSourceFields().get(0).getParent());
		String targetEntityUniqueName = relation.getTargetFields().get(0).getParent().getUniqueName();
		String relationString = targetEntityLabel;
		String relationEntityString = "-->" + targetEntityLabel;
		String relationName = relation.getName();
		String relationJoinType = relation.getJoinType().toString();

		if (needRelationInLabel) {
			relationEntityString = relationEntityString + "(" + relationName + ")";
		}

		String relationTooltip = (EngineMessageBundle.getMessage("sbi.qbe.tree.relation.name", this.getLocale())) + ": "
				+ relationName + "<br>"
				+ (EngineMessageBundle.getMessage("sbi.qbe.tree.source.fields", this.getLocale())) + ": [" + sourceText
				+ "]<br>" + (EngineMessageBundle.getMessage("sbi.qbe.tree.target.entity", this.getLocale())) + ": "
				+ targetEntityLabel + "<br>"
				+ (EngineMessageBundle.getMessage("sbi.qbe.tree.target.fields", this.getLocale())) + ": " + targetText
				+ "<br>";

		JSONObject fieldNode = new JSONObject();
		try {
			fieldNode.put("id", relationId);
			fieldNode.put("text", relationEntityString);
			fieldNode.put("iconCls", iconCls);
			fieldNode.put("leaf", true);
			fieldNode.put("qtip", relationTooltip);
			fieldNode.put("relationName", relationName);
			fieldNode.put("sourceFields", sourceText);
			fieldNode.put("targetEntity", targetEntityUniqueName);
			fieldNode.put("targetEntityLabel", targetEntityLabel);
			fieldNode.put("targetFields", targetText);
			fieldNode.put("joinType", relationJoinType);
			fieldNode.put("isConsidered", true);

			JSONObject nodeAttributes = new JSONObject();
			nodeAttributes.put("iconCls", iconCls);
			nodeAttributes.put("type", NODE_TYPE_RELATION_FIELD);
			nodeAttributes.put("sourceEntity", sourceEntityLabel);
			nodeAttributes.put("entity", targetEntityLabel);
			nodeAttributes.put("field", relationString);
			nodeAttributes.put("longDescription", relationString);
			fieldNode.put("attributes", nodeAttributes);
		} catch (JSONException e) {
			logger.error(e);
		}

		return fieldNode;
	}

	/**
	 * Add Calculate Fields on the entity Control recursion level because calculate field are applied only at entity level not in dimension level.
	 *
	 * @param tree               the tree
	 * @param parentEntityNodeId the parent entity node id
	 * @param nodeCounter        the node counter
	 * @param entity             the entity
	 *
	 * @return the int
	 */
	public int addCalculatedFieldNodes(IQbeTree tree, IModelEntity entity, int parentEntityNodeId, int nodeCounter) {

		/*
		 * List manualCalcultatedFieldForEntity = getDatamartModel().getDataSource().getFormula(). getManualCalculatedFieldsForEntity( entity.getType() );
		 *
		 * CalculatedField calculatedField = null; String fieldAction = null;
		 *
		 * Iterator manualCalculatedFieldsIterator = manualCalcultatedFieldForEntity.iterator(); while (manualCalculatedFieldsIterator.hasNext()){ calculatedField =
		 * (CalculatedField)manualCalculatedFieldsIterator.next();
		 *
		 *
		 *
		 * if (prefix != null){ calculatedField.setFldCompleteNameInQuery(prefix + "." + calculatedField.getId()); }else{
		 * calculatedField.setFldCompleteNameInQuery(calculatedField.getId()); }
		 *
		 *
		 * fieldAction = getUrlGenerator().getActionUrlForCalculateField(calculatedField.getId (), entity.getName(), calculatedField.getFldCompleteNameInQuery());
		 *
		 * nodeCounter++; tree.addNode("" + nodeCounter, "" + parentEntityNodeId, calculatedField.getFldLabel(), fieldAction, calculatedField.getFldLabel(), "_self",
		 * getUrlGenerator().getResourceUrl("../img/cfield.gif"), getUrlGenerator().getResourceUrl("../img/cfield.gif"), "", "", "", "", ""); }
		 *
		 * return nodeCounter;
		 */
		return -1;
	}

	/**
	 * Gets the sub entities nodes.
	 *
	 * @param entity         the entity
	 * @param nodes          the nodes
	 * @param recursionLevel the recursion level
	 *
	 * @return the sub entities nodes
	 */
	public JSONArray getSubEntitiesNodes(IModelEntity entity, JSONArray nodes, int recursionLevel) {

		List<IModelEntity> subEntities = entity.getSubEntities();

		Iterator<IModelEntity> subEntitiesIterator = subEntities.iterator();
		while (subEntitiesIterator.hasNext()) {
			IModelEntity subentity = subEntitiesIterator.next();
			if (subentity.getType().equalsIgnoreCase(entity.getType()) || recursionLevel > 10) {
				// stop recursion
			} else {
				addEntityNode(nodes, subentity, recursionLevel + 1);
			}
		}

		return nodes;
	}

	/**
	 * Gets the datamart model.
	 *
	 * @return the datamart model
	 */
	protected IDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the datamart model.
	 *
	 * @param dataSource the new datamart model
	 */
	protected void setDatamartModel(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Gets the qbe tree filter.
	 *
	 * @return the qbe tree filter
	 */
	private QbeTreeFilter getQbeTreeFilter() {
		return qbeTreeFilter;
	}

	/**
	 * Sets the qbe tree filter.
	 *
	 * @param qbeTreeFilter the new qbe tree filter
	 */
	private void setQbeTreeFilter(QbeTreeFilter qbeTreeFilter) {
		this.qbeTreeFilter = qbeTreeFilter;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	private IModelProperties getDatamartLabels() {
		return datamartLabels;
	}

	private void setDatamartLabels(IModelProperties datamartLabels) {
		this.datamartLabels = datamartLabels;
	}
}
