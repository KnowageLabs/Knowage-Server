/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.datasource;

import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;

/**
 * @author Bernabei Angelo
 *
 */
public interface DataSourceService {

    /**
     * 
     * @param token  String
     * @param user String
     * @param documentId String
     * @return SpagoBiDataSource
     */
    SpagoBiDataSource getDataSource(String token,String user,String documentId);
    /**
     * 
     * @param token  String
     * @param user String
     * @param label String
     * @return SpagoBiDataSource
     */
    SpagoBiDataSource getDataSourceByLabel(String token,String user,String label);    
    /**
     * 
     * @param token  String
     * @param user String
     * @param id int
     * @return SpagoBiDataSource
     */
    SpagoBiDataSource getDataSourceById(String token,String user,Integer id);    
    /**
     * 
     * @param token String
     * @param user String
     * @return SpagoBiDataSource[]
     */
    SpagoBiDataSource[] getAllDataSource(String token,String user);
}
