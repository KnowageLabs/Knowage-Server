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
