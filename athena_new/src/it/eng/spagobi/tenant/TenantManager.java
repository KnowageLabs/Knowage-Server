/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tenant;


/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class TenantManager {

	private static final ThreadLocal<Tenant> _tenant = new ThreadLocal<Tenant>();

	public static void setTenant(Tenant tenant) {
		_tenant.set(tenant);
	}
	
	public static Tenant getTenant() {
		return _tenant.get();
	}
	
	public static void unset() {
		_tenant.remove();
	}
	
}
