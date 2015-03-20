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

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.kpi.alarm.bo.AlarmContact;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;

import java.util.List;

import org.hibernate.Session;


/**
 * 
 * @see it.eng.spagobi.kpi.alarm.bo.AlarmContact
 * @author Enrico Cesaretti
 */
public interface ISbiAlarmContactDAO extends ISpagoBIDao{

    public Integer insert(SbiAlarmContact item);
    
  //  public void insert(Session session, SbiAlarmContact item);

    public void update(SbiAlarmContact item);
    
 //   public void update(Session session, SbiAlarmContact item);
	
    public void delete(SbiAlarmContact item);
    
    public void delete(Session session, SbiAlarmContact item);

    public void delete(Integer id);
    
    public void delete(Session session, Integer id);
	
    public SbiAlarmContact findById(Integer id);

    public List<SbiAlarmContact> findAll();
    
    public List<SbiAlarmContact> findByCsp(String csp);
    
    public AlarmContact loadById(Integer id);
    
    public Integer countContacts()throws EMFUserError;

    public List<SbiAlarmContact> loadPagedContactsList(Integer offset, Integer fetchSize)throws EMFUserError;
	
}

