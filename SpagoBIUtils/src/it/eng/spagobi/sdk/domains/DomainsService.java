/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.domains;

import it.eng.spagobi.sdk.domains.bo.SDKDomain;
import it.eng.spagobi.sdk.exceptions.SDKException;

public interface DomainsService {
	
	boolean insertDomain(SDKDomain sdkDomain) throws SDKException;

	boolean updateDomain(SDKDomain sdkDomain) throws SDKException;
	
	SDKDomain getDomainById(Integer valueId) throws SDKException;
	
	SDKDomain getDomainByDomainAndValueCd(String domainCd, String valueCd) throws SDKException;

	SDKDomain[] getAllDomains() throws SDKException;

	SDKDomain[] getDomainsListByDomainCd(String domainCd) throws SDKException;

	
}
