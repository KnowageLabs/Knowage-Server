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

