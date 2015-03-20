/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.utilities.engines;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.services.proxy.DataSourceServiceProxy;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.Map;

import org.apache.log4j.Logger;


/**
 * @author ghedin
 * Add some functionality to the AbstractEngineInstance
 */
public abstract class ExtendedAbstractEngineInstance extends AbstractEngineInstance {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(ExtendedAbstractEngineInstance.class);


	public ExtendedAbstractEngineInstance() {
		super( );
	}
	
	public ExtendedAbstractEngineInstance(Map env) {
		super(env);
	}
	
	public IDataSource getDataSourceForWriting() {
		String schema = null;
		String attrname = null;

		String datasourceLabel = (String)getEnv().get(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL);
		logger.debug("DEFAULT_DATASOURCE_FOR_WRITING_LABEL :" + datasourceLabel);
		if (datasourceLabel != null) {
			DataSourceServiceProxy datasourceProxy = (DataSourceServiceProxy) getEnv().get( EngineConstants.ENV_DATASOURCE_PROXY );
			IDataSource dataSource = datasourceProxy.getDataSourceByLabel(datasourceLabel);
			if (dataSource.checkIsMultiSchema()) {
				logger.debug("Datasource [" + dataSource.getLabel()
						+ "] is defined on multi schema");
				try {
					logger.debug("Retriving target schema for datasource ["
							+ dataSource.getLabel() + "]");
					attrname = dataSource.getSchemaAttribute();
					logger.debug("Datasource's schema attribute name is equals to ["
							+ attrname + "]");
					Assert.assertNotNull(
							attrname,
							"Datasource's schema attribute name cannot be null in order to retrive the target schema");
					IEngUserProfile profile = (IEngUserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
					schema = (String) profile.getUserAttribute(attrname);
					Assert.assertNotNull(schema,
							"Impossible to retrive the value of attribute ["
									+ attrname + "] form user profile");
					dataSource.setJndi(dataSource.getJndi() + schema);
					logger.debug("Target schema for datasource  ["
							+ dataSource.getLabel() + "] is ["
							+ dataSource.getJndi() + "]");
				} catch (Throwable t) {
					throw new SpagoBIEngineRuntimeException(
							"Impossible to retrive target schema for datasource ["
									+ dataSource.getLabel() + "]", t);
				}
				logger.debug("Target schema for datasource  ["
						+ dataSource.getLabel() + "] retrieved succesfully");
			}
			return dataSource;
		}

		return null;
	}


}
