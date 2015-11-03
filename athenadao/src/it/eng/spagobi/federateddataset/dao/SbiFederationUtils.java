/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.federateddataset.dao;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.federateddataset.metadata.SbiFederationDefinition;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.federation.FederationDefinition;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;


public class SbiFederationUtils {

	static private Logger logger = Logger.getLogger(SbiFederationUtils.class);

	public static FederationDefinition toDatasetFederationNoDataset(SbiFederationDefinition hibFd) {
		return toDatasetFederationWithDataset(hibFd, null);
	}

	
	public static FederationDefinition toDatasetFederationWithDataset(SbiFederationDefinition hibFd, Set<IDataSet> sourceDatasets) {
		FederationDefinition fd = toDatasetFederation(hibFd);
		if(sourceDatasets==null){
			logger.debug("No dataset is added in the definition");
			sourceDatasets= new HashSet<IDataSet>();
		}else{
			logger.debug("Adding also the dataset to the federation definition");
		}
		fd.setSourceDatasets(sourceDatasets);
		return fd;
	}
	
	

	public static FederationDefinition toDatasetFederation(SbiFederationDefinition hibFd) {
		logger.debug("IN");
		FederationDefinition fd = new FederationDefinition();

		if(hibFd!=null){

			logger.debug("Th federation is not null. Label is " + hibFd.getLabel());
			fd.setLabel(hibFd.getLabel());
			fd.setName(hibFd.getName());
			fd.setDescription(hibFd.getDescription());
			fd.setRelationships(hibFd.getRelationships());
			fd.setFederation_id(hibFd.getFederation_id());

		}else{
			logger.debug("The federation is null");
		}

		logger.debug("OUT");
		return fd;
	}
	
	public static SbiFederationDefinition toSbiFederatedDataset( FederationDefinition hibFd) {
		logger.debug("IN");
		SbiFederationDefinition fd = new SbiFederationDefinition();

		if(hibFd!=null){

			logger.debug("Th federation is not null. Label is " + hibFd.getLabel());
			fd.setFederation_id(hibFd.getFederation_id());
			
			fd.setLabel(hibFd.getLabel());
			fd.setName(hibFd.getName());
			fd.setDescription(hibFd.getDescription());
			fd.setRelationships(hibFd.getRelationships());
			fd.setSourceDatasets(toSbiDataSet(hibFd.getSourceDatasets()));

		}else{
			logger.debug("The federation is null");
		}

		logger.debug("OUT");
		return fd;
	}
	
	public static Set<SbiDataSet> toSbiDataSet(Set<IDataSet> dataSets){
		
		JDBCDataSet ss = null;

		
		Set<SbiDataSet> ds = new java.util.HashSet<SbiDataSet>();
		for (IDataSet dataset : dataSets) {
			int version = 1;
			if (dataset instanceof VersionedDataSet) {
				version = ((VersionedDataSet) dataset).getVersionNum();
			}
			SbiDataSetId id = new SbiDataSetId(dataset.getId(),  version, dataset.getOrganization());
			SbiDataSet iDataSet = new SbiDataSet(id);
			ds.add(iDataSet);
		}
		return ds;
		
		
		
	}

}
