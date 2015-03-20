/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.udp.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.tools.udp.bo.Udp;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;

import java.util.List;

import org.hibernate.Session;


/**
 * 
 * @see it.eng.spagobi.udp.bo.Udp
 * @author Antonella Giachino
 */
public interface IUdpDAO extends ISpagoBIDao{

    public Integer insert(SbiUdp prop);

    public void update(SbiUdp prop);
	
    public void delete(SbiUdp prop);

    public void delete(Integer id);

	
    public SbiUdp findById(Integer id);

    public List<SbiUdp> findAll();

    public List<Udp> loadAllByFamily(String familyCode) throws EMFUserError;

    public Udp loadByLabel(String label) throws EMFUserError;

    public Udp loadByLabelAndFamily(String label, String family) throws EMFUserError;

    public Udp loadById(Integer id);
    
    public List<SbiUdp> loadPagedUdpList(Integer offset, Integer fetchSize)throws EMFUserError;
	
	public Integer countUdp()throws EMFUserError;

}

