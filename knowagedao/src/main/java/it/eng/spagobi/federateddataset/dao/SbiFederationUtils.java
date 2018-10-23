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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import it.eng.spagobi.federateddataset.metadata.SbiFederationDefinition;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.utilities.assertion.Assert;

public class SbiFederationUtils {

	static private Logger logger = Logger.getLogger(SbiFederationUtils.class);

	public static FederationDefinition toDatasetFederationNoDataset(SbiFederationDefinition hibFd) {
		return toDatasetFederationWithDataset(hibFd, null);
	}

	public static FederationDefinition toDatasetFederationWithDataset(SbiFederationDefinition hibFd, Set<IDataSet> sourceDatasets) {
		FederationDefinition fd = toDatasetFederation(hibFd);
		if (sourceDatasets == null) {
			logger.debug("No dataset is added in the definition");
			sourceDatasets = new HashSet<IDataSet>();
		} else {
			logger.debug("Adding also the dataset to the federation definition");
		}
		fd.setSourceDatasets(sourceDatasets);
		return fd;
	}

	public static FederationDefinition toDatasetFederation(SbiFederationDefinition hibFd) {
		logger.debug("IN");
		FederationDefinition fd = new FederationDefinition();

		toFederationDefinition(fd,hibFd);

		logger.debug("OUT");
		return fd;
	}

	public static void toFederationDefinition(FederationDefinition federationDefinition, SbiFederationDefinition sbiFederationDefinition) {
		logger.debug("IN");

		Assert.assertNotNull(federationDefinition, "The federation is null");
		Assert.assertNotNull(sbiFederationDefinition, "The federation is null");

		federationDefinition.setFederation_id(sbiFederationDefinition.getFederation_id());
		federationDefinition.setLabel(sbiFederationDefinition.getLabel());
		federationDefinition.setName(sbiFederationDefinition.getName());
		federationDefinition.setDescription(sbiFederationDefinition.getDescription());
		federationDefinition.setRelationships(sbiFederationDefinition.getRelationships());
		federationDefinition.setDegenerated(sbiFederationDefinition.isDegenerated());
		federationDefinition.setOwner(sbiFederationDefinition.getOwner());

		logger.debug("OUT");

	}

	public static SbiFederationDefinition toSbiFederatedDataset(FederationDefinition hibFd) {
		logger.debug("IN");
		SbiFederationDefinition fd = new SbiFederationDefinition();

		toSbiFederationDefinition(fd, hibFd);

		logger.debug("OUT");
		return fd;
	}

	public static void toSbiFederationDefinition(SbiFederationDefinition sbiFederationDefinition, FederationDefinition federationDefinition) {
		logger.debug("IN");

		Assert.assertNotNull(sbiFederationDefinition, "The federation is null");
		Assert.assertNotNull(federationDefinition, "The federation is null");

		sbiFederationDefinition.setFederation_id(federationDefinition.getFederation_id());
		sbiFederationDefinition.setLabel(federationDefinition.getLabel());
		sbiFederationDefinition.setName(federationDefinition.getName());
		sbiFederationDefinition.setDescription(federationDefinition.getDescription());
		sbiFederationDefinition.setRelationships(federationDefinition.getRelationships());
		sbiFederationDefinition.setSourceDatasets(toSbiDataSet(federationDefinition.getSourceDatasets()));
		sbiFederationDefinition.setDegenerated(federationDefinition.isDegenerated());
		sbiFederationDefinition.setOwner(federationDefinition.getOwner());

		logger.debug("OUT");

	}

	public static Set<SbiDataSet> toSbiDataSet(Set<IDataSet> dataSets) {

		Set<SbiDataSet> ds = new java.util.HashSet<SbiDataSet>();
		for (IDataSet dataset : dataSets) {
			int version = 1;
			if (dataset instanceof VersionedDataSet) {
				version = ((VersionedDataSet) dataset).getVersionNum();
			}
			SbiDataSetId id = new SbiDataSetId(dataset.getId(), version, dataset.getOrganization());
			SbiDataSet iDataSet = new SbiDataSet(id);
			ds.add(iDataSet);
		}
		return ds;

	}

}
