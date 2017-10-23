package it.eng.spagobi.tools.dataset.association;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.IMap;
import commonj.work.Work;

public class DistinctValuesClearWork implements Work {

	private final IDataSet dataSet;
	private final UserProfile userProfile;

	private static transient Logger logger = Logger.getLogger(DistinctValuesClearWork.class);

	public DistinctValuesClearWork(IDataSet dataSet, UserProfile userProfile) {
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
			logger.debug("This thread will clear domain values for dataSet with signature [" + signature + "]");
			String hashSignature = Helper.sha256(signature);
			logger.debug("Corresponding signature hash value is [" + hashSignature + "]");
			IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME,
					SpagoBIConstants.DISTRIBUTED_MAP_FOR_ASSOCIATION);
			try {
				if (mapLocks.tryLock(hashSignature, 1, TimeUnit.SECONDS, SQLDBCache.getLeaseTime(), TimeUnit.SECONDS)) {
					try {
						String filePath = SpagoBIUtilities.getDatasetResourcePath() + File.separatorChar + DataSetConstants.DOMAIN_VALUES_FOLDER
								+ File.separatorChar + hashSignature + DataSetConstants.DOMAIN_VALUES_EXTENSION;
						File binaryFile = new File(filePath);
						if (binaryFile.exists()) {
							binaryFile.delete();
							logger.debug("Binary file [" + filePath + "] deleted.");
						}
						logger.debug("Clearing domain values: DONE");
					} finally {
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
			throw new SpagoBIRuntimeException("An unexpected error occured while clearing distinct values for dataSet [" + dataSet.getLabel() + "]", e);
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
}
