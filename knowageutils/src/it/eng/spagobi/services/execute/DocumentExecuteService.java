/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.execute;

import java.util.HashMap;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public interface DocumentExecuteService {
    /**
     *  Return the IMAGE of a chart
     *  The primary goal is to integrate a chart in a JasperReport
     * @param token
     * @param user
     * @param document
     * @param parameters
     * @return
     */
    byte[] executeChart(String token,String user,String document,HashMap parameters);
    
    /**
     *  Returns the XML DATA of the Kpi value with id kpiValueID
     * @param token
     * @param user
     * @param kpiValueID
     * @return
     */
    String getKpiValueXML(String token, String user,Integer kpiValueID);

}
