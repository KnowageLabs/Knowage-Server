/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.federateddataset.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;

public interface ISbiFederationDefinitionDAO extends ISpagoBIDao {

	public void saveSbiFederationDefinition(FederationDefinition dataset);
	
	public void saveSbiFederationDefinitionNoDuplicated(FederationDefinition federationDefinition);

	public FederationDefinition loadFederationDefinition(Integer id) throws EMFUserError;
	 
	public List<FederationDefinition> loadAllFederatedDataSets() throws EMFUserError;

	public List<FederationDefinition> loadFederationsUsingDataset(Integer dsId, Session currSession) throws EMFUserError;
	
	public Set<IDataSet> loadAllFederatedDataSets(Integer federationID) throws EMFUserError;

}
