/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version.
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file.
 */
package it.eng.spagobi.tools.dataset.cache;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.bo.AbstractDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroupJSONSerializer;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JoinedDataSet extends AbstractDataSet {

	UserProfile userProfile;
	IDataSetDAO dataSetDao;
	List<IDataSet> joinedDataSets;
	IDataStore joinedDataStore;
	AssociationGroup associationGroup;

	private static transient Logger logger = Logger.getLogger(JoinedDataSet.class);

	public JoinedDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);
	}

	public JoinedDataSet(String label, String name, String description, String configuration) {
		setName(name);
		setLabel(label);
		setDescription(description);
		setConfiguration(configuration);
	}

	public JoinedDataSet(String label, String name, String description, JSONObject configuration) {
		setName(name);
		setLabel(label);
		setDescription(description);
		setConfiguration(configuration);
	}

	public JoinedDataSet(String label, String name, String description, AssociationGroup associationGroup) {
		setName(name);
		setLabel(label);
		setDescription(description);
		setAssociations(associationGroup);
	}

	@Override
	public void setConfiguration(String configuration) {
		logger.trace("IN");

		try {
			JSONObject jsonConf = ObjectUtils.toJSONObject(this.configuration);
			setConfiguration(jsonConf);
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while setting configuration", t);
		} finally {
			logger.trace("OUT");
		}
	}

	public void setConfiguration(JSONObject configuration) {
		logger.trace("IN");

		try {
			AssociationGroupJSONSerializer deserializer = new AssociationGroupJSONSerializer();
			AssociationGroup associationGroup = deserializer.deserialize(configuration);
			setAssociations(associationGroup);
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while setting configuration", t);
		} finally {
			logger.trace("OUT");
		}
	}

	/**
	 * @return the configuration
	 */
	@Override
	public String getConfiguration() {
		JSONObject configuration = null;
		try {
			AssociationGroupJSONSerializer serializer = new AssociationGroupJSONSerializer();
			AssociationGroup associationGroup = getAssociations();
			configuration = serializer.serialize(associationGroup);
			return configuration.toString();
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while getting configuration", t);
		}
	}

	public List<IDataSet> getDataSets() {
		return this.joinedDataSets;
	}

	public IDataSet getDataSet(String datasetLabel) {
		IDataSet targetDataSet = null;
		if (datasetLabel == null)
			return targetDataSet;
		for (IDataSet dataSet : joinedDataSets) {
			if (dataSet.getLabel().equals(datasetLabel)) {
				targetDataSet = dataSet;
				break;
			}
		}
		return targetDataSet;
	}

	private void setDataSetsByLabel(Collection<String> dataSetLabels) {
		List<IDataSet> datsets = new ArrayList<IDataSet>();
		for (String dataSetLabel : dataSetLabels) {
			IDataSet dataSet = this.getDataSetDAO().loadDataSetByLabel(dataSetLabel);
			// checkQbeDataset(dataSet);
			datsets.add(dataSet);
		}
		setDataSets(datsets);
	}

	private void setDataSets(List<IDataSet> joinedDataSets) {
		this.joinedDataSets = joinedDataSets;
	}

	private IDataSetDAO getDataSetDAO() {
		if (dataSetDao == null) {
			try {
				dataSetDao = DAOFactory.getDataSetDAO();
				if (getUserProfile() != null) {
					dataSetDao.setUserProfile(userProfile);
				}
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An unexpected error occured while instatiating the DAO", t);
			}
		}
		return dataSetDao;
	}

	// private void checkQbeDataset(IDataSet dataSet) {
	//
	// IDataSet ds = null;
	// if (dataSet instanceof VersionedDataSet) {
	// VersionedDataSet versionedDataSet = (VersionedDataSet) dataSet;
	// ds = versionedDataSet.getWrappedDataset();
	// } else {
	// ds = dataSet;
	// }
	//
	// if (ds instanceof QbeDataSet) {
	// SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
	// Map parameters = ds.getParamsMap();
	// if (parameters == null) {
	// parameters = new HashMap();
	// ds.setParamsMap(parameters);
	// }
	// ds.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
	// }
	// }

	public AssociationGroup getAssociations() {
		return associationGroup;
	}

	private void setAssociations(AssociationGroup associationGroup) {
		this.associationGroup = associationGroup;
		setDataSetsByLabel(associationGroup.getDataSetLabels());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#getUserProfileAttributes()
	 */
	public Map getUserProfileAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#setUserProfileAttributes(java .util.Map)
	 */
	public void setUserProfileAttributes(Map<String, Object> attributes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadData(int offset, int fetchSize, int maxResults) {
		ICache cache = SpagoBICacheManager.getCache();

		// check if all joinedDataset have been succesfully stored in cache
		List<IDataSet> dataSetNotStoredInCache = cache.getNotContained(joinedDataSets);
		if (dataSetNotStoredInCache.size() > 0) {
			String dataSetLabels = "";
			for (IDataSet dataSet : dataSetNotStoredInCache) {
				dataSetLabels += dataSet.getLabel() + ";";
			}
			logger.warn("In order to load data for joined store [" + this.getName() + "] " + "the following datasets [" + dataSetLabels
					+ "] need to be loaded before");

			cache.load(joinedDataSets, true);
		}

		// qunado tutti i work sono finiti creo la tabella di join
		joinedDataStore = cache.refresh(this, associationGroup);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#getDataStore()
	 */
	public IDataStore getDataStore() {
		return joinedDataStore;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#setAbortOnOverflow(boolean)
	 */
	public void setAbortOnOverflow(boolean abortOnOverflow) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#addBinding(java.lang.String, java.lang.Object)
	 */
	public void addBinding(String bindingName, Object bindingValue) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#test()
	 */
	public IDataStore test() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#test(int, int, int)
	 */
	public IDataStore test(int offset, int fetchSize, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#getSignature()
	 */
	public String getSignature() {
		String signature = "";
		for (IDataSet dataSet : this.joinedDataSets) {
			signature += dataSet.getSignature();
		}
		return signature;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#decode(it.eng.spagobi.tools. dataset.common.datastore.IDataStore)
	 */
	public IDataStore decode(IDataStore datastore) {
		throw new UnsupportedOperationException("Dataset implementation class [" + this.getClass().getName() + "] does not support method [decode]");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#isCalculateResultNumberOnLoadEnabled ()
	 */
	public boolean isCalculateResultNumberOnLoadEnabled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#setCalculateResultNumberOnLoad (boolean)
	 */
	public void setCalculateResultNumberOnLoad(boolean enabled) {
		logger.warn("In [" + this.getClass().getName() + "] calculate result number on loadis always false");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#setDataSource(it.eng.spagobi .tools.datasource.bo.IDataSource)
	 */
	public void setDataSource(IDataSource dataSource) {
		logger.warn("In [" + this.getClass().getName() + "] datasource is not used");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.eng.spagobi.tools.dataset.bo.IDataSet#getDataSource()
	 */
	public IDataSource getDataSource() {
		return null;
	}

	public UserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public void setParamsMaps(Map<String, Map<String, String>> paramsMaps) {
		Set<String> datasetLabels = paramsMaps.keySet();
		for (String datasetLabel : datasetLabels) {
			Map paramsMap = paramsMaps.get(datasetLabel);
			IDataSet dataSet = getDataSet(datasetLabel);
			dataSet.setParamsMap(paramsMap);
		}
	}

	@Override
	public void setParamsMap(Map paramsMap) {
		super.setParamsMap(paramsMap);
		for (IDataSet dataSet : joinedDataSets) {
			dataSet.setParamsMap(paramsMap);
		}
	}
}
