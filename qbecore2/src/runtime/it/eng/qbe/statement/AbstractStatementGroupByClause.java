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

public abstract class AbstractStatementGroupByClause extends AbstractStatementClause {
	
	public static final String GROUP_BY = "GROUP BY";
	
	public String buildClause(Query query, Map<String, Map<String, String>> entityAliasesMaps) {
		StringBuffer buffer;	
		String fieldName;
		
		buffer = new StringBuffer();
		
		List<ISelectField> groupByFields = query.getGroupByFields();
		if(groupByFields.size() == 0) return buffer.toString();
		
		buffer.append(JPQLStatementConstants.STMT_KEYWORD_GROUP_BY);
		
		Map entityAliases = (Map)entityAliasesMaps.get(query.getId());
		
		String fieldSeparator = "";
		
		for( ISelectField groupByField : groupByFields ) {
			Assert.assertTrue(groupByField.isGroupByField(), "Field [" + groupByField.getAlias() +"] is not an groupBy filed");
			
			buffer.append(fieldSeparator);
			
			fieldName = null;			
			if(groupByField.isInLineCalculatedField()){
				InLineCalculatedSelectField inlineCalculatedField = (InLineCalculatedSelectField)groupByField;
				fieldName = parseInLinecalculatedField(inlineCalculatedField, inlineCalculatedField.getSlots(), query, entityAliasesMaps);
			} else if(groupByField.isSimpleField()){			
				SimpleSelectField simpleField = (SimpleSelectField)groupByField;
				IModelField datamartField = parentStatement.getDataSource().getModelStructure().getField(simpleField.getUniqueName());
				fieldName = parentStatement.getFieldAliasWithRoles(datamartField, entityAliases, entityAliasesMaps, simpleField);
			} else {
				// TODO throw an exception here
			}
			
			buffer.append(" " + fieldName);
			
			fieldSeparator = ", ";
			
		}
		
		return buffer.toString().trim();
	}
}