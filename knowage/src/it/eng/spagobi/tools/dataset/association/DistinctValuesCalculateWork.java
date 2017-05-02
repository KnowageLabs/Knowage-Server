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

package it.eng.spagobi.tools.dataset.association;

import gnu.trove.set.hash.TLongHashSet;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ConfigurationConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.NumberUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;
import it.eng.spagobi.utilities.trove.TLongHashSetSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterOutputStream;

import org.apache.log4j.Logger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeOutput;
import com.hazelcast.core.IMap;
import commonj.work.Work;

public class DistinctValuesCalculateWork implements Work {

	private final IDataSet dataSet;
	private final UserProfile userProfile;

	private static transient Logger logger = Logger.getLogger(DistinctValuesCalculateWork.class);

	public DistinctValuesCalculateWork(IDataSet dataSet, UserProfile userProfile) {
		this.dataSet = dataSet;
		this.userProfile = userProfile;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void run() {
		logger.debug("IN");
		try {
			if (userProfile != null) {
				logger.debug("Found profile for user [" + userProfile.getUserId() + "] of tenant [" + userProfile.getOrganization() + "]");
				TenantManager.setTenant(new Tenant(userProfile.getOrganization()));
			}
			String signature = dataSet.getSignature();
			logger.debug("This thread will calculate domain values for dataSet with signature [" + signature + "]");
			String hashSignature = Helper.sha256(signature);
			logger.debug("Corresponding signature hash value is [" + hashSignature + "]");
			IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME,
					SpagoBIConstants.DISTRIBUTED_MAP_FOR_ASSOCIATION);
			try {
				if (mapLocks.tryLock(hashSignature, 1, TimeUnit.SECONDS, SQLDBCache.getLeaseTime(), TimeUnit.SECONDS)) {
					UnsafeOutput output = null;
					try {
						String numberOfSamples = GeneralUtilities
								.getSpagoBIConfigurationProperty(ConfigurationConstants.SPAGOBI_DATASET_ASSOCIATIONS_AUTODETECT_SAMPLES);
						int fetchSize = Integer.parseInt(numberOfSamples);
						if (fetchSize < 0) {
							dataSet.loadData();
						} else {
							dataSet.loadData(0, fetchSize, fetchSize);
						}

						IDataStore dataStore = dataSet.getDataStore();
						logger.debug("Setting datastore metadata equals to dataset metadata");
						dataStore.setMetaData(dataSet.getMetadata());
						List<Integer> attributeFieldIndexes = getAttributeFieldIndexes(dataStore.getMetaData());
						Map<String, TLongHashSet> results = dataStore.getFieldsDistinctValuesAsLongHash(attributeFieldIndexes);
						logger.debug("Distinct values for dataSet" + dataSet.getLabel() + " are: " + results);

						String path = SpagoBIUtilities.getDatasetResourcePath() + File.separatorChar + DataSetConstants.DOMAIN_VALUES_FOLDER;
						File folder = new File(path);
						if (!folder.exists()) {
							logger.debug("Folder [" + path + "] does not exists. Creating it...");
							folder.mkdirs();
						}
						logger.debug("Writing domain values into binary file located at [" + path + "]");
						Kryo kryo = new Kryo();
						kryo.register(TLongHashSet.class, new TLongHashSetSerializer());
						output = new UnsafeOutput(new DeflaterOutputStream(new FileOutputStream(path + File.separatorChar + hashSignature
								+ DataSetConstants.DOMAIN_VALUES_EXTENSION)));
						kryo.writeObject(output, results);
						logger.debug("Writing domain values: DONE");
					} finally {
						if (output != null) {
							output.close();
						}
						mapLocks.unlock(hashSignature);
					}
				} else {
					logger.debug("Impossible to acquire the lock for dataset [" + hashSignature
							+ "]. It is likely that another thread is calculating or clearing distinct values for the same dataset.");
				}
			} catch (InterruptedException e) {
				logger.error("The current thread has failed to release the lock for dataset [" + hashSignature + "] in time.", e);
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An unexpected error occured while calculating distinct values for dataSet [" + dataSet.getLabel() + "]", e);
		} finally {
			TenantManager.unset();
			logger.debug("OUT");
		}

	}

	@Override
	public boolean isDaemon() {
		return false;
	}

	@Override
	public void release() {
	}

	private List<Integer> getAttributeFieldIndexes(IMetaData metaData) {
		List<Integer> toReturn = new ArrayList<>();
		if (metaData != null) {
			for (int fieldIndex = 0; fieldIndex < metaData.getFieldCount(); fieldIndex++) {
				IFieldMetaData fieldMeta = metaData.getFieldMeta(fieldIndex);
				if (fieldMeta.getFieldType().equals(FieldType.ATTRIBUTE) && !NumberUtilities.isFloatingPoint(fieldMeta.getType())) {
					toReturn.add(fieldIndex);
				}
			}
		}
		return toReturn;
	}
}
