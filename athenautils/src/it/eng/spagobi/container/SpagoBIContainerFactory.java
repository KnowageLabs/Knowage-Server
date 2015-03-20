/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SpagoBIContainerFactory {
	public static IContainer getContainer(Object o) {
		IContainer container;
		
		container = null;
		
		if(o instanceof ServletRequest) {
			container =  new SpagoBIServletRequestContainer( (ServletRequest)o );
		} else if(o instanceof HttpSession){
			container =  new SpagoBIHttpSessionContainer( (HttpSession)o );
		} else {
			throw new IllegalArgumentException("Impossible to build a container around an instance of [" + o.getClass().getName() + "]");
		}
		
		return container;
	}
}
