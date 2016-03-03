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

