/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
