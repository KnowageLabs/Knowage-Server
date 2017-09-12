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
