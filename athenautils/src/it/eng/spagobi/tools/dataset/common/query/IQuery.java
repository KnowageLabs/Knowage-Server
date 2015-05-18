/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.common.query;

/**
 * @authors Alessandro Portosa (alessandro.portosa@eng.it)
 */

public interface IQuery {

	public boolean isDistinctClauseEnabled();

	public void setDistinctClauseEnabled(boolean distinctClauseEnabled);

	public void addSelectFiled(String fieldUniqueName, String function, String fieldAlias, boolean include, boolean visible, boolean groupByField,
			String orderType, String pattern);

	public void addWhereField(String name, String description, boolean promptable, String[] leftOperatorValues, String leftOperatorDescription,
			String leftOperatorType, String[] leftOperatorDefaulttValues, String[] leftOperatorLastValues, String leftOperatorAlias, String operator,
			String[] rightOperatorValues, String rightOperatorDescription, String rightOperatorType, String[] rightOperatorDefaulttValues,
			String[] rightOperatorLastValues, String rightOperatorAlias, String booleanConnector);

	public String toSql(String schema, String table);
}