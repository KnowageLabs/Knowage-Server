/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.wapp.bo;

import it.eng.spagobi.commons.bo.Role;

import java.io.Serializable;
import java.util.List;


/**
 * Defines a <code>Menu</code> object. 
 * 
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */


public class Menu  implements Serializable  {
	
    private Integer menuId;
    private Integer objId;
    private String objParameters;
    private String subObjName;
    private String snapshotName;
    private Integer snapshotHistory;
    private String functionality;
    private String initialPath;
    private String name;
    private String descr;
    private Integer parentId;
    private Integer level;
    private Integer depth;    
    private Integer prog;    
    private boolean hasChildren;
    private List	lstChildren;
    private Role[] roles = null;
    private boolean viewIcons=false;
    private boolean hideToolbar=false;
    private boolean hideSliders=false;
    private String staticPage;
    private String extApplicationUrl;
    private String code;
    private String url;
    private String iconPath;
    private String iconCls;
    private String groupingMenu;
    private boolean isAdminsMenu=false;
    


	public String getGroupingMenu() {
		return groupingMenu;
	}

	public void setGroupingMenu(String groupingMenu) {
		this.groupingMenu = groupingMenu;
	}

	public String getIconCls() {
		return iconCls;
	}

	public void setIconCls(String iconCls) {
		this.iconCls = iconCls;
	}

	

    	/**
	 * Gets the lst children.
	 * 
	 * @return the lst children
	 */
	public List getLstChildren() {
		return lstChildren;
	}
	
	/**
	 * Sets the lst children.
	 * 
	 * @param lstChildren the new lst children
	 */
	public void setLstChildren(List lstChildren) {
		this.lstChildren = lstChildren;
	}
	
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
	 * Gets the obj id.
	 * 
	 * @return the obj id
	 */
	public Integer getObjId() {
		return objId;
	}
	
	/**
	 * Sets the obj id.
	 * 
	 * @param objId the new obj id
	 */
	public void setObjId(Integer objId) {
		this.objId = objId;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the isAdminsMenu
	 */
	public boolean isAdminsMenu() {
		return isAdminsMenu;
	}

	/**
	 * @param isAdminsMenu the isAdminsMenu to set
	 */
	public void setAdminsMenu(boolean isAdminsMenu) {
		this.isAdminsMenu = isAdminsMenu;
	}

	/**
	 * Gets the descr.
	 * 
	 * @return the descr
	 */
	public String getDescr() {
		return descr;
	}
	
	/**
	 * Sets the descr.
	 * 
	 * @param descr the new descr
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}
	
	/**
	 * Gets the parent id.
	 * 
	 * @return the parent id
	 */
	public Integer getParentId() {
		return parentId;
	}
	
	/**
	 * Sets the parent id.
	 * 
	 * @param parentId the new parent id
	 */
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	
	/**
	 * Gets the level.
	 * 
	 * @return the level
	 */
	public Integer getLevel() {
		return level;
	}
	
	/**
	 * Sets the level.
	 * 
	 * @param level the new level
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}
	
	/**
	 * Gets the depth.
	 * 
	 * @return the depth
	 */
	
	public Integer getDepth() {
		return depth;
	}

	/**
	 * Sets the depth.
	 * 
	 * @param depth the new depth
	 */
	
	public void setDepth(Integer depth) {
		this.depth = depth;
	}

	/**
	 * Gets the checks for children.
	 * 
	 * @return the checks for children
	 */
	public boolean getHasChildren() {
		return hasChildren;
	}
	
	/**
	 * Sets the checks for children.
	 * 
	 * @param hasChildren the new checks for children
	 */
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	public Role[] getRoles() {
		return roles;
	}

	public void setRoles(Role[] roles) {
		this.roles = roles;
	}

	public boolean isViewIcons() {
		return viewIcons;
	}

	public void setViewIcons(boolean viewIcons) {
		this.viewIcons = viewIcons;
	}

	public boolean getHideToolbar() {
		return hideToolbar;
	}

	public void setHideToolbar(boolean hideToolbar) {
		this.hideToolbar = hideToolbar;
	}

	public String getStaticPage() {
		return staticPage;
	}

	public void setStaticPage(String staticPage) {
		this.staticPage = staticPage;
	}

	public String getObjParameters() {
		return objParameters;
	}

	public void setObjParameters(String objParameters) {
		this.objParameters = objParameters;
	}

	public String getSubObjName() {
		return subObjName;
	}

	public void setSubObjName(String subObjName) {
		this.subObjName = subObjName;
	}

	public String getSnapshotName() {
		return snapshotName;
	}

	public void setSnapshotName(String snapshotName) {
		this.snapshotName = snapshotName;
	}

	public Integer getSnapshotHistory() {
		return snapshotHistory;
	}

	public void setSnapshotHistory(Integer snapshotHistory) {
		this.snapshotHistory = snapshotHistory;
	}

	public boolean getHideSliders() {
		return hideSliders;
	}

	public void setHideSliders(boolean hideSliders) {
		this.hideSliders = hideSliders;
	}

	public String getFunctionality() {
		return functionality;
	}

	public void setFunctionality(String functionality) {
		this.functionality = functionality;
	}

	public Integer getProg() {
		return prog;
	}

	public void setProg(Integer prog) {
		this.prog = prog;
	}

	public String getInitialPath() {
		return initialPath;
	}

	public void setInitialPath(String initialPath) {
		this.initialPath = initialPath;
	}


	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the iconPath
	 */
	public String getIconPath() {
		return iconPath;
	}

	/**
	 * @param iconPath the iconPath to set
	 */
	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}	
	

    public String getExternalApplicationUrl() {
		return extApplicationUrl;
	}

	public void setExternalApplicationUrl(String extApplicationUrl) {
		this.extApplicationUrl = extApplicationUrl;
	}
	
}
