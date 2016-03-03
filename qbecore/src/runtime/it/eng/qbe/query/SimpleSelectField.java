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

	private String temporalOperand;
	private String temporalOperandParameter;

	public SimpleSelectField(String uniqueName, String function, String alias, boolean include, boolean visible, boolean groupByField, String orderType,
			String pattern, String temporalOperand, String temporalOperandParameter) {

		super(alias, ISelectField.SIMPLE_FIELD, include, visible);

		setUniqueName(uniqueName);
		setFunction(AggregationFunctions.get(function));
		setGroupByField(groupByField);
		setOrderType(orderType);
		setPattern(pattern);
		setTemporalOperand(temporalOperand);
		setTemporalOperandParameter(temporalOperandParameter);
	}

	public SimpleSelectField(SimpleSelectField field) {

		this(field.getUniqueName(), field.getFunction().getName(), field.getAlias(), field.isIncluded(), field.isVisible(), field.isGroupByField(),
				field.getOrderType(), field.getPattern(), field.getTemporalOperand(), field.getTemporalOperandParameter());
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

	@Override
	public ISelectField copy() {
		return new SimpleSelectField(this);
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public String getName() {
		return getUniqueName();
	}

	@Override
	public void setName(String name) {
		setUniqueName(name);
	}

	public void setTemporalOperand(String temporalOperand) {
		this.temporalOperand = temporalOperand;
	}

	public void setTemporalOperandParameter(String temporalOperandParameter) {
		this.temporalOperandParameter = temporalOperandParameter;
	}

	public String getTemporalOperand() {
		return temporalOperand;
	}

	public String getTemporalOperandParameter() {
		return temporalOperandParameter;
	}

	public String updateNature(String iconCls) {
		// if an aggregation function is defined or if the field is declared as "measure" into property file,
		// then it is a measure, elsewhere it is an attribute
		if ((getFunction() != null && !getFunction().equals(AggregationFunctions.NONE_FUNCTION)) || iconCls.equals("measure")
				|| iconCls.equals("mandatory_measure")) {

			if (iconCls.equals("mandatory_measure")) {
				nature = QuerySerializationConstants.FIELD_NATURE_MANDATORY_MEASURE;
			} else {
				nature = QuerySerializationConstants.FIELD_NATURE_MEASURE;
			}
		} else {

			if (iconCls.equals("segment_attribute")) {
				nature = QuerySerializationConstants.FIELD_NATURE_SEGMENT_ATTRIBUTE;
			} else {
				nature = QuerySerializationConstants.FIELD_NATURE_ATTRIBUTE;
			}
		}
		return nature;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof SimpleSelectField) {
			SimpleSelectField ssObj = (SimpleSelectField) obj;
			if (getIdForEquals().equals(ssObj.getIdForEquals())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.getIdForEquals().hashCode() * 34;
	}

	private String getIdForEquals() {
		return this.getAlias() + '|' + this.getName();
	}

}
