/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.qbe.query;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public abstract class AbstractSelectField implements ISelectField {
	private String alias;
	private String type;
	private boolean visible;
	private boolean included;

	private boolean groupByField;
	private String orderType;

	/**
	 * Introducing the dynamic behavior of the ordering column (attribute) for particular category, if provided (current implementation covers this feature only
	 * for the first category set for the chart - it should be extended also for other (subsequent) categories.
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	private String orderColumn;

	protected String nature;

	public static final String ORDER_ASC = "ASC";
	public static final String ORDER_DESC = "DESC";

	public AbstractSelectField(String alias, String type, boolean included, boolean visible) {
		setAlias(alias);
		setType(type);
		setIncluded(included);
		setVisible(visible);
		groupByField = false;
		orderType = null;
		orderColumn = null;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public boolean isSimpleField() {
		return this.SIMPLE_FIELD.equalsIgnoreCase(type);
	}

	@Override
	public boolean isInLineCalculatedField() {
		return this.IN_LINE_CALCULATED_FIELD.equalsIgnoreCase(type);
	}

	@Override
	public boolean isCalculatedField() {
		return this.CALCULATED_FIELD.equalsIgnoreCase(type);
	}

	@Override
	public boolean isGroupByField() {
		return groupByField;
	}

	@Override
	public void setGroupByField(boolean groupByField) {
		this.groupByField = groupByField;
	}

	@Override
	public boolean isOrderByField() {
		return ORDER_ASC.equalsIgnoreCase(getOrderType()) || ORDER_DESC.equalsIgnoreCase(getOrderType());
	}

	@Override
	public boolean isAscendingOrder() {
		return ORDER_ASC.equalsIgnoreCase(getOrderType());
	}

	@Override
	public String getOrderType() {
		return orderType;
	}

	@Override
	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getOrderColumn() {
		return this.orderColumn;
	}

	public void setOrderColumn(String orderColumn) {
		this.orderColumn = orderColumn;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isIncluded() {
		return included;
	}

	@Override
	public void setIncluded(boolean included) {
		this.included = included;
	}

	@Override
	public String getNature() {
		return this.nature;
	}

	@Override
	public void setNature(String nature) {
		this.nature = nature;
	}

}
