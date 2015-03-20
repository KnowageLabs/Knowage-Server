/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.jpa;

import it.eng.qbe.datasource.IDataSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author giachino
 *
 */
public interface IJpaDataSource  extends IDataSource {
	it.eng.spagobi.tools.datasource.bo.IDataSource getToolsDataSource();
	EntityManager getEntityManager();
	EntityManagerFactory getEntityManagerFactory();
	EntityManagerFactory getEntityManagerFactory(String dmName);

		
}
