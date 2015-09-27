/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.federateddataset.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.dataset.federation.DatasetFederation;

import java.util.List;

public interface ISbiFederatedDatasetDAO extends ISpagoBIDao {

	public void saveSbiFederatedDataSet(DatasetFederation dataset);

	public DatasetFederation loadFederationDefinition(Integer id) throws EMFUserError;
 
	public List<DatasetFederation> loadAllFederatedDataSets() throws EMFUserError;

}
