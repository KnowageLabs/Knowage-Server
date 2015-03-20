/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query;

import it.eng.qbe.query.serializer.json.QuerySerializationConstants;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SimpleSelectField extends AbstractSelectField {
	
	private String uniqueName;
	private IAggregationFunction function;
	
	private String pattern;
	


	public SimpleSelectField(String uniqueName, String function, String alias, boolean include, boolean visible,
		boolean groupByField, String orderType, String pattern ) {
		
		super(alias, ISelectField.SIMPLE_FIELD, include, visible);
				
		setUniqueName(uniqueName);
		setFunction( AggregationFunctions.get(function) );		
		setGroupByField(groupByField);
		setOrderType(orderType);
		setPattern(pattern);
	}
	
	public SimpleSelectField(SimpleSelectField field) {
			
			this(field.getUniqueName(), 
				field.getFunction().getName(), 
				field.getAlias(), 
				field.isIncluded(), 
				field.isVisible(),
				field.isGroupByField(), 
				field.getOrderType(), 
				field.getPattern());					
	}


	
	public IAggregationFunction getFunction() {
		return function;
	}

	public void setFunction(IAggregationFunction function) {
		this.function = function;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}
	
	public ISelectField copy() {
		return new SimpleSelectField( this );
	}
	
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getName() {
		return getUniqueName();
	}

	public void setName(String name) {
		setUniqueName(name);
	}
	
	public String updateNature(String iconCls){
		//if an aggregation function is defined or if the field is declared as "measure" into property file,
		//  then it is a measure, elsewhere it is an attribute
		if ((getFunction() != null 
			&& !getFunction().equals(AggregationFunctions.NONE_FUNCTION))
			|| iconCls.equals("measure") || iconCls.equals("mandatory_measure")) {
			
			if(iconCls.equals("mandatory_measure")){
				nature = QuerySerializationConstants.FIELD_NATURE_MANDATORY_MEASURE;
			}
			else{
				nature = QuerySerializationConstants.FIELD_NATURE_MEASURE;
			}
		} else {

			if(iconCls.equals("segment_attribute")){
				nature = QuerySerializationConstants.FIELD_NATURE_SEGMENT_ATTRIBUTE;
			}
			else{
				nature = QuerySerializationConstants.FIELD_NATURE_ATTRIBUTE;
			}
		}
		return nature;
	}


}
