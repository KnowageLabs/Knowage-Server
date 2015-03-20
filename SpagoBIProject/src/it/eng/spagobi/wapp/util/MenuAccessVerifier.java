/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.wapp.util;


import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.wapp.bo.Menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MenuAccessVerifier {



	public static boolean canView(Menu menu, IEngUserProfile profile) {

		Role[] menuRoles=menu.getRoles();
		Collection profileRoles=null;

		try {
			profileRoles = ((UserProfile)profile).getRolesForUse();
		} catch (EMFInternalError e) {
			return false;
		}


		boolean found=false;
		for (Iterator iterator = profileRoles.iterator(); iterator.hasNext() && !found;) {
			String profileRole = (String) iterator.next();
			for(int i=0;i<menuRoles.length && !found;i++){
				Role menuRole=menuRoles[i];
				String menuRoleName=menuRole.getName();

				if(menuRoleName.equals(profileRole)){
					found=true;
				}
			}

		}
		return found;


	}



}
