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
package it.eng.spagobi.engines.qbe.services.initializers;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;


/**
 * The Class BuildQbeDatasetStartAction.
 *
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class BuildQbeDatasetStartAction extends QbeEngineStartAction {

	private static final long serialVersionUID = 8517069222119935320L;

	public static final String DATASOURCE_LABEL = "DATASOURCE_LABEL";

	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(BuildQbeDatasetStartAction.class);

    public static final String ENGINE_NAME = "SpagoBIQbeEngine";

    @Override
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) {
    	logger.debug("IN");
    	super.service(serviceRequest, serviceResponse);
    	checkUser();
	}

	/**
     * Checks if the user is able to build dataset
     */
    private void checkUser() {
    	logger.debug("IN");
    	try {
    		UserProfile profile = this.getUserProfile();
    		if (!profile.isAbleToExecuteAction("DatasetManagement")) {
    			throw new SecurityException("User [" +
    					"unique identifier: " + profile.getUserUniqueIdentifier() +
    					"user id : " + profile.getUserId() +
    					"name: " + profile.getUserName() +
    					"] cannot build dataset!!");
    		}
    	} catch (Throwable t) {
    		throw new SpagoBIRuntimeException("Cannot verify if user is able to build dataset", t);
    	} finally {
    		logger.debug("OUT");
    	}
	}

	@Override
    public IDataSource getDataSource() {
    	String schema = null;
        String attrname = null;

        String datasourceLabel = this.getAttributeAsString(DATASOURCE_LABEL);

        IDataSource dataSource = getDataSourceServiceProxy().getDataSourceByLabel( datasourceLabel );
        if (dataSource.checkIsMultiSchema()){
            logger.debug("Datasource [" + dataSource.getLabel() + "] is defined on multi schema");
            try {
                logger.debug("Retriving target schema for datasource [" + dataSource.getLabel() + "]");
                attrname = dataSource.getSchemaAttribute();
                logger.debug("Datasource's schema attribute name is equals to [" + attrname + "]");
                Assert.assertNotNull(attrname, "Datasource's schema attribute name cannot be null in order to retrive the target schema");
                schema = (String)getUserProfile().getUserAttribute(attrname);
                Assert.assertNotNull(schema, "Impossible to retrive the value of attribute [" + attrname + "] form user profile");
                dataSource.setJndi( dataSource.getJndi() + schema);
                logger.debug("Target schema for datasource  [" + dataSource.getLabel() + "] is [" + dataSource.getJndi()+ "]");
            } catch (Throwable t) {
                throw new SpagoBIEngineRuntimeException("Impossible to retrive target schema for datasource [" + dataSource.getLabel() + "]", t);
            }
            logger.debug("Target schema for datasource  [" + dataSource.getLabel() + "] retrieved succesfully");
        }

		return dataSource;
    }

}
