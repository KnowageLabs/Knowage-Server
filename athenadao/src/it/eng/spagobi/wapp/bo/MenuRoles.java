/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.wapp.bo;


import java.io.Serializable;
/**
 * Defines a value constraint object.
 * 
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */


public class MenuRoles  implements Serializable   {

	private Integer menuId;
	private Integer extRoleId;
	
	/**
	 * Gets the menu id.
	 * 
	 * @return the menu id
	 */
	public Integer getMenuId() {
		return menuId;
	}
	
	/**
	 * Sets the menu id.
	 * 
	 * @param menuId the new menu id
	 */
	public void setMenuId(Integer menuId) {
		this.menuId = menuId;
	}
	
	/**
	 * Gets the ext role id.
	 * 
	 * @return the ext role id
	 */
	public Integer getExtRoleId() {
		return extRoleId;
	}
	
	/**
	 * Sets the ext role id.
	 * 
	 * @param extRoleId the new ext role id
	 */
	public void setExtRoleId(Integer extRoleId) {
		this.extRoleId = extRoleId;
	}
	
		


}
