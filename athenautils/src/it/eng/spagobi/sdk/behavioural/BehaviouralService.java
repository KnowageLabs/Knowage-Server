/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.behavioural;

import it.eng.spagobi.sdk.behavioural.bo.SDKAttribute;
import it.eng.spagobi.sdk.behavioural.bo.SDKRole;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;

public interface BehaviouralService {

	SDKAttribute[] getAllAttributes(String roleName) throws NotAllowedOperationException;

	SDKRole[] getRoles() throws NotAllowedOperationException;

	SDKRole[] getRolesByUserId(String userId) throws NotAllowedOperationException;

}
