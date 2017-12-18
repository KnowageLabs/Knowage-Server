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

package it.eng.spagobi.federateddataset.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.federateddataset.metadata.SbiFederationDefinition;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;

public interface ISbiFederationDefinitionDAO extends ISpagoBIDao {

	public int saveSbiFederationDefinition(FederationDefinition dataset);

	public int saveSbiFederationDefinitionNoDuplicated(FederationDefinition federationDefinition);

	public FederationDefinition loadFederationDefinition(Integer id) throws EMFUserError;

	public SbiFederationDefinition loadSbiFederationDefinition(Integer id, Session currSession) throws EMFUserError;

	public List<FederationDefinition> loadAllFederatedDataSets() throws EMFUserError;

	public List<FederationDefinition> loadFederationsUsingDataset(Integer dsId) throws EMFUserError;

	public List<FederationDefinition> loadFederationsUsingDataset(Integer dsId, Session currSession) throws EMFUserError;

	public Set<IDataSet> loadAllFederatedDataSets(Integer federationID) throws EMFUserError;

	public Integer countFederationsUsingDataset(Integer dsId);

	public void deleteFederatedDatasetById(Integer id) throws EMFUserError;

	public Integer modifyFederation(FederationDefinition fds) throws EMFUserError;

	public List<FederationDefinition> loadNotDegeneratedFederatedDataSets() throws EMFUserError;
}
