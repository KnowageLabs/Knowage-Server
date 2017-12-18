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

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface ISelectField extends IQueryField {
	public final static String SIMPLE_FIELD = "datamartField";
	public final static String CALCULATED_FIELD = "calculated.field";
	public final static String IN_LINE_CALCULATED_FIELD = "inline.calculated.field";

	void setAlias(String alias);

	void setName(String name);

	String getType();

	void setType(String type);

	boolean isSimpleField();

	boolean isInLineCalculatedField();

	boolean isCalculatedField();

	boolean isGroupByField();

	void setGroupByField(boolean groupByField);

	boolean isOrderByField();

	boolean isAscendingOrder();

	String getOrderType();

	void setOrderType(String orderType);

	boolean isVisible();

	void setVisible(boolean visible);

	boolean isIncluded();

	void setIncluded(boolean include);

	String getNature();

	void setNature(String nature);

	ISelectField copy();
}
