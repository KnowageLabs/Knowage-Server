/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.statement;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.ISelectField;
import it.eng.qbe.query.InLineCalculatedSelectField;
import it.eng.qbe.query.Query;
import it.eng.qbe.query.SimpleSelectField;
import it.eng.qbe.statement.jpa.JPQLStatementConstants;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.objects.Couple;

import java.util.List;
import java.util.Map;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */


public abstract class AbstractStatementOrderByClause extends AbstractStatementClause{

	public static String ORDER_BY = "ORDER BY";
	
	public String buildClause(Query query, Map<String, Map<String, String>> entityAliasesMaps) {
		StringBuffer buffer;
		
		
		buffer = new StringBuffer();
		
		List<ISelectField> orderByFields = query.getOrderByFields();
		if(orderByFields.size() == 0) return buffer.toString();
		
		buffer.append(JPQLStatementConstants.STMT_KEYWORD_ORDER_BY);
		
		Map entityAliases = (Map)entityAliasesMaps.get(query.getId());
		
		String fieldSeparator = "";
		
		for( ISelectField orderByField : orderByFields ) {
			Assert.assertTrue(orderByField.isOrderByField(), "Field [" + orderByField.getAlias() +"] is not an orderBy filed");
			
			buffer.append(fieldSeparator);
			
			if(orderByField.isSimpleField()) {				
				SimpleSelectField simpleField = (SimpleSelectField)orderByField;
				
				IModelField modelField = parentStatement.getDataSource().getModelStructure().getField(simpleField.getUniqueName());

				String fieldName = parentStatement.getFieldAliasWithRoles(modelField, entityAliases, entityAliasesMaps, simpleField);
				
				buffer.append(" " + simpleField.getFunction().apply(fieldName));
			
			} else if(orderByField.isInLineCalculatedField()) {
				InLineCalculatedSelectField inlineCalculatedField = (InLineCalculatedSelectField)orderByField;
				String fieldName = parseInLinecalculatedField(inlineCalculatedField, inlineCalculatedField.getSlots(), query, entityAliasesMaps);
				
				buffer.append(" " + inlineCalculatedField.getFunction().apply(fieldName));
			} else {
				// TODO throw an exception here
			}
			
			buffer.append(" " + (orderByField.isAscendingOrder()? JPQLStatementConstants.STMT_KEYWORD_ASCENDING: JPQLStatementConstants.STMT_KEYWORD_DESCENDING) );
			
			fieldSeparator = ", ";			
		}
		
		return buffer.toString().trim();
	}
	
}
