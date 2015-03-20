/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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

