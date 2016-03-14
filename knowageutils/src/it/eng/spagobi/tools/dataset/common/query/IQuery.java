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

package it.eng.spagobi.tools.dataset.common.query;

/**
 * @authors Alessandro Portosa (alessandro.portosa@eng.it)
 */

public interface IQuery {

	public boolean isDistinctClauseEnabled();

	public void setDistinctClauseEnabled(boolean distinctClauseEnabled);

	/**
	 * Extend the method by the 'orderColumn' parameter, that is now dynamic (not fixed) and it is just temporarily enabled only for the first category in the
	 * chart. Ordering column is the attribute (column) that user can pick from the set of all available attributes that are provided by the used dataset.
	 *
	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	public void addSelectFiled(String fieldUniqueName, String function, String fieldAlias, boolean include, boolean visible, boolean groupByField,
			String orderType, String pattern, String orderColumn);

	public void addWhereField(String name, String description, boolean promptable, String[] leftOperatorValues, String leftOperatorDescription,
			String leftOperatorType, String[] leftOperatorDefaulttValues, String[] leftOperatorLastValues, String leftOperatorAlias, String operator,
			String[] rightOperatorValues, String rightOperatorDescription, String rightOperatorType, String[] rightOperatorDefaulttValues,
			String[] rightOperatorLastValues, String rightOperatorAlias, String booleanConnector);

	public String toSql(String schema, String table);
}