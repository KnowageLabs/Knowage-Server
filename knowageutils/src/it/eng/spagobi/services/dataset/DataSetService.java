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
package it.eng.spagobi.services.dataset;

import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public interface DataSetService {

    /**
     * 
     * @param token  String
     * @param user String
     * @param datasetId String
     * @return SpagoBiDataSet
     */
    SpagoBiDataSet getDataSet(String token,String user,String datasetId);
    /**
     * 
     * @param token  String
     * @param user String
     * @param label String
     * @return SpagoBiDataSet
     */
    SpagoBiDataSet getDataSetByLabel(String token,String user,String label);    
    /**
     * 
     * @param token String
     * @param user String
     * @return SpagoBiDataSet[]
     */
    SpagoBiDataSet[] getAllDataSet(String token,String user);
    /**
     * 
     * @param token String
     * @param user String
     * @param dataset SpagoBiDataSet
     * @return SpagoBiDataSet
     */
    SpagoBiDataSet saveDataSet(String token,String user, SpagoBiDataSet dataset);
}
