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

package it.eng.qbe.statement;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot.MappedValuesPunctualDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot.MappedValuesRangeDescriptor;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.InLineCalculatedSelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.serializer.SerializationManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.objects.Couple;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */

public class AbstractSelectStatementClause extends AbstractStatementClause{
	
	protected Couple<String, String>[] statementFields;
	protected int index;
	protected Map entityAliases;
	public static final String SELECT = "SELECT";
	public static final String DISTINCT = "DISTINCT";
	public static transient Logger logger = Logger.getLogger(AbstractStatementClause.class);
	
	public String buildClause(Query query, Map<String, Map<String, String>> entityAliasesMaps) {
		return this.buildClause(query, entityAliasesMaps, false);
	}
	

	public String buildClause(Query query, Map<String, Map<String, String>> entityAliasesMaps, boolean useAliases) {
		StringBuffer buffer;
		List<ISelectField> selectFields;
		List<InLineCalculatedSelectField> selectInLineCalculatedFields = new ArrayList<InLineCalculatedSelectField>();
	
		logger.debug("IN");
		
		buffer = new StringBuffer();
		
		try {
			
			Assert.assertNotNull(query, "Input parameter [query] cannot be null");
			Assert.assertNotNull(query, "Input parameter [entityAliasesMaps] cannot be null");
			
			logger.debug("Building select clause for query [" + query.getId() + "]");
			
			entityAliases = (Map)entityAliasesMaps.get(query.getId());	
			Assert.assertNotNull(entityAliases, "The entity map for the query [" + query.getId() + "] canot be null");
			
			selectFields = query.getSelectFields(true);
				
			buffer.append(SELECT);		
			if (query.isDistinctClauseEnabled()) {
				buffer.append(" " + DISTINCT);
			}
			
			int calculatedFieldNumber = query.getCalculatedSelectFields(true).size();
			logger.debug("In select clause of query [" + query.getId() + "] there are [" + calculatedFieldNumber + "] calculated fields out of [" + selectFields.size() + "]");
			
			int statementFiledsNo = selectFields.size() - calculatedFieldNumber; // = simpleFields + inlineCalculatedFields
			if(statementFiledsNo == 0) {
				throw new RuntimeException("Impossible to execute a query that contains in the select statemet only (expert) calculated fields");
			}
			statementFields = (Couple<String, String>[]) Array.newInstance(new Couple<String, String>("", "").getClass(), selectFields.size() - calculatedFieldNumber); 
			index = 0;
			
			for(ISelectField selectAbstractField : selectFields){										
				if(selectAbstractField.isSimpleField()){
					addSimpleSelectField((SimpleSelectField)selectAbstractField, entityAliasesMaps); 
				} else if(selectAbstractField.isInLineCalculatedField()){
					// calculated field will be added in the second step when all the simple fields will be already in place
					selectInLineCalculatedFields.add((InLineCalculatedSelectField)selectAbstractField);
					// we keep the space to add this field later in the second process step
					index++;
				}
			}
				
			for(InLineCalculatedSelectField selectInLineField :  selectInLineCalculatedFields){
					
					String expression = selectInLineField.getExpression();
					String slots = selectInLineField.getSlots();
					
					expression = parseInLinecalculatedField(selectInLineField, slots, query, entityAliasesMaps);
					//expr = addSlots(expr, selectInLineField);
					expression = selectInLineField.getFunction().apply(expression);
					
					for(int y = 0; y < statementFields.length; y++){
						if(statementFields[y] == null){
							statementFields[y]= new Couple(" " + expression, selectInLineField.getAlias());
							index = y;
							break;
						}
					}

					logger.debug("select clause element succesfully added to select clause");
			}
			
			String separator = "";
			for(int y = 0; y < statementFields.length; y++){
				buffer.append(separator + statementFields[y].getFirst());
				String alias = statementFields[y].getSecond();
				if (useAliases && alias != null) {
					buffer.append(" as " + encapsulate(alias));
				}
				separator = ",";
			}		
		} finally {
			logger.debug("OUT");
		}
		
		return buffer.toString().trim();
	}
	
	/**
	 * This may be overwritten by sub-classes (see it.eng.qbe.statement.sql.SQLStatementSelectClause)
	 * @param alias The alias to be encapsulated, if necessary
	 * @return
	 */
	protected String encapsulate(String alias) {
		// by default (JPQL/HQL statement) there is no need to encapsulate aliases
		return alias;
	}

	private String addSlots(String expr, InLineCalculatedSelectField selectInLineField) {
		String newExpr;
		
		newExpr = null;
		
		try {
			String s = selectInLineField.getSlots();
			if(s ==  null || s.trim().length() == 0) return expr;
			JSONArray slotsJSON = new JSONArray(s);
			List<Slot> slots = new ArrayList<Slot>();
			for(int i = 0; i < slotsJSON.length(); i++) {
				Slot slot = (Slot)SerializationManager.deserialize(slotsJSON.get(i), "application/json", Slot.class);
				slots.add(slot);
			}
			
			
			
			if(slots.isEmpty()) return expr;
			
			Slot defaultSlot = null;
			
			newExpr = "CASE";
			for(Slot slot : slots) {
				List<Slot.IMappedValuesDescriptor> descriptors =  slot.getMappedValuesDescriptors();
				if(descriptors == null || descriptors.isEmpty()) {
					defaultSlot = slot;
					continue;
				}
				for(Slot.IMappedValuesDescriptor descriptor : descriptors) {
					if(descriptor instanceof MappedValuesPunctualDescriptor) {
					
						MappedValuesPunctualDescriptor punctualDescriptor = (MappedValuesPunctualDescriptor)descriptor;
						newExpr += " WHEN (" + expr + ") IN (";
						String valueSeparator = "";
						Set<String> values = punctualDescriptor.getValues();
						for(String value : values) {
							newExpr += valueSeparator + "'" + value + "'";
							valueSeparator = ", ";
						}
						newExpr += ") THEN '" + slot.getName() + "'";
						
					} else if(descriptor instanceof MappedValuesRangeDescriptor) {
						MappedValuesRangeDescriptor punctualDescriptor = (MappedValuesRangeDescriptor)descriptor;
						newExpr += " WHEN";
						String minCondition = null;
						String maxCondition = null;
						if(punctualDescriptor.getMinValue() != null) {
							minCondition = " (" + expr + ")";
							minCondition += (punctualDescriptor.isIncludeMinValue())? " >= " : ">";
							minCondition += punctualDescriptor.getMinValue();
						}
						if(punctualDescriptor.getMaxValue() != null) {
							maxCondition = " (" + expr + ")";
							maxCondition += (punctualDescriptor.isIncludeMaxValue())? " <= " : "<";
							maxCondition += punctualDescriptor.getMaxValue();
						}
						String completeCondition = "";
						if(minCondition != null) {
							completeCondition += "(" + minCondition + ")";
						}
						if(maxCondition != null) {
							completeCondition += (minCondition != null)? " AND " : "";
							completeCondition += "(" + maxCondition + ")";
						}
						newExpr += " " + completeCondition;
						newExpr += " THEN '" + slot.getName() + "'";
					} else {
						// ignore slot
					}
				
				}
			}
			if(defaultSlot != null) {
				newExpr += " ELSE '" + defaultSlot.getName() + "'";
			} else {
				newExpr += " ELSE (" + expr + ")";
			}
			newExpr += " END ";
		} catch (Throwable t) {
			logger.error("Impossible to add slots", t);
			return expr;
		}
		
		return newExpr;
	}

	private void addSimpleSelectField(SimpleSelectField selectField, Map entityAliasesMaps) {
		
		IModelField datamartField;
		String queryName;
		IModelEntity rootEntity;
		String rootEntityAlias;
		String selectClauseElement; 
	
		
		logger.debug("select field unique name [" + selectField.getUniqueName() + "]");
		
		datamartField = parentStatement.getDataSource().getModelStructure().getField(selectField.getUniqueName());
		

		
		selectClauseElement =parentStatement.getFieldAliasWithRoles(datamartField, entityAliases, entityAliasesMaps, selectField);
		logger.debug("select clause element before aggregation [" + selectClauseElement + "]");
		
		selectClauseElement = selectField.getFunction().apply(selectClauseElement);
		logger.debug("select clause element after aggregation [" + selectClauseElement + "]");
		
		
		statementFields[index] = new Couple(" " + selectClauseElement, selectField.getAlias());
		index++;
		
		logger.debug("select clause element succesfully added to select clause");
	}


}
