/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.spagobi.tools.dataset.bo;

import java.util.List;

import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 */
public class JDBCImpalaDataSet extends JDBCDataSet {

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {
		super.loadData(offset, fetchSize, maxResults);
		transformAliases();
	}

	private void transformAliases() {
		if (selectQuery != null) {
			IMetaData metaData = dataStore.getMetaData();
			List<AbstractSelectionField> projections = selectQuery.getProjections();
			for (int i = 0; i < metaData.getFieldCount(); i++) {
				metaData.getFieldMeta(i).setAlias(((Projection) projections.get(i)).getAlias());
			}
		}
	}
}
