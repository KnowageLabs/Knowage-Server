/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.dao;

import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;

import java.util.List;

public interface ISbiDataSetDAO extends ISpagoBIDao {

	public SbiDataSet loadSbiDataSetByLabel(String label);

	public List<SbiDataSet> loadSbiDataSets();

	public List<SbiDataSet> loadDataSets(String owner, Boolean includeOwned, Boolean includePublic, String scope, String type, String category,
			String implementation);
}
