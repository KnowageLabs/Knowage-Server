/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.alarm.dao;

/**
 * Title: SpagoBI
 * Description: SpagoBI
 * Copyright: Copyright (c) 2008
 * Company: Xaltia S.p.A.
 * 
 * @author Enrico Cesaretti
 * @version 1.0
 */




import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmEvent;

import java.util.List;

import org.hibernate.Session;

/**
 * 
 * @see it.eng.spagobi.kpi.alarm.metadata.SbiAlarmEvent
 * @author Enrico Cesaretti
 */
public interface ISbiAlarmEventDAO extends ISpagoBIDao{

    public void insert(SbiAlarmEvent item);
    
    
   // public void insert(Session session, SbiAlarmEvent item);

    public void update(SbiAlarmEvent item);
    
  //  public void update(Session session, SbiAlarmEvent item);
    
    public void delete(SbiAlarmEvent item);
    
    public void delete(Session session, SbiAlarmEvent item);

    public void delete(Integer id);
    
    
    public void delete(Session session, Integer id);
	
    public SbiAlarmEvent findById(Integer id);

    public List<SbiAlarmEvent> findAll();
    
    public List<SbiAlarmEvent> findActive();
}

