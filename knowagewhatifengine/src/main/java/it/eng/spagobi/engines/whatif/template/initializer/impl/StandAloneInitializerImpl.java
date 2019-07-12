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
package it.eng.spagobi.engines.whatif.template.initializer.impl;

import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.AbstractInitializer;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;

/**
 * @author Dragan Pirkovic
 *
 */
public class StandAloneInitializerImpl extends AbstractInitializer {

	public static transient Logger logger = Logger.getLogger(StandAloneInitializerImpl.class);

	public static final String TAG_CONNECTION = "CONNECTION";
	public static final String TAG_STAND_ALONE = "STANDALONE";
	public static final String TAG_USR = "USR";
	public static final String TAG_PWD = "PWD";
	public static final String TAG_JNDI_NAME = "JNDI_NAME";
	public static final String TAG_CATALOG = "CATALOG";
	public static final String STAD_ALONE_DS_LABEL = "STAD_ALONE_DS_LABEL";
	public static final String TAG_CONNECTIONSTRING = "CONNECTIONSTRING";
	public static final String TAG_DIALECT = "DIALECT";
	public static final String TAG_DRIVER = "DRIVER";

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		logger.debug("IN. loading the configuration for a stand alone execution");
		SourceBean standAloneSB = (SourceBean) template.getAttribute(TAG_STAND_ALONE);
		if (standAloneSB != null) {
			logger.debug("This is a stand alone execution");
			logger.debug(TAG_STAND_ALONE + ": " + standAloneSB);
			IDataSource ds = DataSourceFactory.getDataSource();
			ds.setLabel(STAD_ALONE_DS_LABEL);
			SourceBean connectionProperties = (SourceBean) standAloneSB.getAttribute(TAG_CONNECTION);
			String jndiName = getBeanValue(TAG_JNDI_NAME, connectionProperties);
			if (StringUtilities.isNotEmpty(jndiName)) {
				ds.setJndi(getBeanValue(TAG_JNDI_NAME, connectionProperties));
			} else {
				ds.setPwd(getBeanValue(TAG_PWD, connectionProperties));
				ds.setHibDialectClass(getBeanValue(TAG_DIALECT, connectionProperties));
				ds.setUser(getBeanValue(TAG_USR, connectionProperties));
				ds.setUrlConnection(getBeanValue(TAG_CONNECTIONSTRING, connectionProperties));
				ds.setDriver(getBeanValue(TAG_DRIVER, connectionProperties));
			}
			String catalog = getBeanValue(TAG_CATALOG, connectionProperties);
			toReturn.setStandAloneConnection(ds);
			toReturn.setMondrianSchema(catalog);
		} else {
			logger.debug("This is not a stand alone execution");
		}
	}

	private String getBeanValue(String tag, SourceBean bean) {
		String field = null;
		SourceBean fieldBean = null;
		fieldBean = (SourceBean) bean.getAttribute(tag);
		if (fieldBean != null) {
			field = fieldBean.getCharacters();
			if (field == null) {
				field = "";
			}
		}
		return field;
	}

}
