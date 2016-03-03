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
package it.eng.spagobi.tools.udp.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.udp.bo.UdpValue;
import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;

import java.util.List;

import org.hibernate.Session;


/**
 * 
 * @see it.eng.spagobi.udp.bo.Udp
 * @author Antonella Giachino
 */
public interface IUdpValueDAO extends ISpagoBIDao{

    public Integer insert(SbiUdpValue prop);
    
    public void insert(Session session, SbiUdpValue propValue);

    public void update(SbiUdpValue propValue);
    
    public void update(Session session, SbiUdpValue propValue);
	
    public void delete(SbiUdpValue propValue);
    
    public void delete(Session session, SbiUdpValue propValue);

    public void delete(Integer id);
    
    public void delete(Session session, Integer id);
	
    public SbiUdpValue findById(Integer id);

    public List<SbiUdpValue> findAll();
    
    public UdpValue loadById(Integer id);

	public List findByReferenceId(Integer kpiId, String family);

	public UdpValue loadByReferenceIdAndUdpId(Integer referenceId, Integer udpId, String family);
	
	public void insertOrUpdateRelatedUdpValues(Object object, Object sbiObject, Session aSession, String family) throws EMFUserError;
	
}

