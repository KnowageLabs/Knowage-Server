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

import java.util.List;

import it.eng.qbe.model.structure.ModelCalculatedField.Slot;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class InLineCalculatedSelectField extends AbstractSelectField {
	
	private String expression;
	private String slots;
	private String type;
	private String nature;
//	private Object initialValue;
	private boolean groupByField;
	private IAggregationFunction function;
	private String orderType;
//	private int resetType;
//	private int incrementType;
	
	public InLineCalculatedSelectField(String alias, String expression, String slots, String type, String nature, boolean included, boolean visible, boolean groupByField, String orderType, String function ) {
		super(alias, ISelectField.CALCULATED_FIELD, included, visible);
		this.expression = expression;
		this.slots = slots;
		this.type = type;
		this.nature = nature;
		this.groupByField = groupByField;
		setOrderType(orderType);
		setFunction( AggregationFunctions.get(function) );
	}
	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
	
	public String getSlots() {
		return slots;
	}

	public void setSlots(String slots) {
		this.slots = slots;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getNature() {
		return nature;
	}

	public void setNature(String nature) {
		this.nature = nature;
	}

	public ISelectField copy() {
		return null;
	}
	
	public boolean isInLineCalculatedField() {
		return true;
	}
	
	public boolean isGroupByField() {
		return groupByField;
	}


	public void setGroupByField(boolean groupByField) {
		this.groupByField = groupByField;
	}
	
	public boolean isOrderByField() {
		return "ASC".equalsIgnoreCase( getOrderType() )
			|| "DESC".equalsIgnoreCase( getOrderType() );
	}

	public boolean isAscendingOrder() {
		return "ASC".equalsIgnoreCase( getOrderType() );
	}
	


	public String getOrderType() {
		return orderType;
	}


	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	public IAggregationFunction getFunction() {
		return function;
	}

	public void setFunction(IAggregationFunction function) {
		this.function = function;
	}


	public String getName() {
		return getAlias();
	}

	public void setName(String alias) {
		setAlias(alias);
	}
	
}
