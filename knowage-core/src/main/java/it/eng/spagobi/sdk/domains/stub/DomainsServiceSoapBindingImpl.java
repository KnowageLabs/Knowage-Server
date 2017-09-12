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

package it.eng.spagobi.sdk.domains.stub;

import it.eng.spagobi.sdk.domains.impl.DomainsServiceImpl;
import it.eng.spagobi.sdk.engines.impl.EnginesServiceImpl;

public class DomainsServiceSoapBindingImpl implements it.eng.spagobi.sdk.domains.stub.DomainsService{

	public boolean insertDomain(it.eng.spagobi.sdk.domains.bo.SDKDomain in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException {
       	DomainsServiceImpl supplier=new DomainsServiceImpl();
    	return supplier.insertDomain(in0);
    }

    public boolean updateDomain(it.eng.spagobi.sdk.domains.bo.SDKDomain in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException {
       	DomainsServiceImpl supplier=new DomainsServiceImpl();
    	return supplier.updateDomain(in0);

    }

    public it.eng.spagobi.sdk.domains.bo.SDKDomain getDomainById(java.lang.Integer in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException {
       	DomainsServiceImpl supplier=new DomainsServiceImpl();
    	return supplier.getDomainById(in0);
    }

    public it.eng.spagobi.sdk.domains.bo.SDKDomain getDomainByDomainAndValueCd(java.lang.String in0, java.lang.String in1) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException {
       	DomainsServiceImpl supplier=new DomainsServiceImpl();
    	return supplier.getDomainByDomainAndValueCd(in0, in1);

    }

    public it.eng.spagobi.sdk.domains.bo.SDKDomain[] getAllDomains() throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException {
       	DomainsServiceImpl supplier=new DomainsServiceImpl();
    	return supplier.getAllDomains();
    }

    public it.eng.spagobi.sdk.domains.bo.SDKDomain[] getDomainsListByDomainCd(java.lang.String in0) throws java.rmi.RemoteException, it.eng.spagobi.sdk.exceptions.SDKException {
       	DomainsServiceImpl supplier=new DomainsServiceImpl();
    	return supplier.getDomainsListByDomainCd(in0);
    }

}
