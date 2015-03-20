/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.wapp.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.wapp.bo.Menu;

import java.util.List;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public interface IMenuDAO extends ISpagoBIDao{
	
	/**
	 * Loads all detail information for a menu identified by its <code>menuID</code>. All these information,
	 * archived by a query to the DB, are stored into an <code>menu</code> object, which is
	 * returned.
	 * 
	 * @param menuID The id for the menu to load
	 * 
	 * @return An <code>menu</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Menu loadMenuByID(Integer menuID) throws EMFUserError;
	
	/**
	 * Loads all detail information for a menu identified by its <code>menuID</code>. All these information,
	 * archived by a query to the DB, are stored into an <code>menu</code> object, which is
	 * returned.
	 * 
	 * @param menuID The id for the menu to load
	 * 
	 * @return An <code>menu</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Menu loadMenuByID(Integer menuID, Integer roleID) throws EMFUserError;
	
	/**
	 * Loads all detail information for a menu whose name is equal to <code>name</code>.
	 * 
	 * @param name The name for the menu to load
	 * 
	 * @return An <code>menu</code> object containing all loaded information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public Menu loadMenuByName(String name) throws EMFUserError;
		
	/**
	 * Loads all detail information for all menus. For each of them, detail
	 * information is stored into an <code>menu</code> object. After that, all menus
	 * are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all menu objects
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */	
	public List loadAllMenues() throws EMFUserError;	
	
	/**
	 * Implements the query to modify a menu. All information needed is stored
	 * into the input <code>menu</code> object.
	 * 
	 * @param aMenu The object containing all modify information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyMenu(Menu aMenu) throws EMFUserError;
	
	/**
	 * Implements the query to insert a menu. All information needed is stored
	 * into the input <code>menu</code> object.
	 * 
	 * @param aMenu The object containing all insert information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertMenu(Menu aMenu) throws EMFUserError;
	
	/**
	 * Implements the query to erase a menu. All information needed is stored
	 * into the input <code>menu</code> object.
	 * 
	 * @param aMenu The object containing all delete information
	 * 
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void eraseMenu(Menu aMenu) throws EMFUserError;

	
	/**
	 * Tells if a menu is associated to any roles.
	 * It is useful because a menu cannot be deleted
	 * if it is used by one or more BI Roles.
	 * 
	 * @param menuId The menu identifier
	 * 
	 * @return True if the menu is used by one or more
	 * objects, else false
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public boolean hasRolesAssociated (Integer menuId) throws EMFUserError;
	
	/**
	 * Tells if a menu has children element.
	 * It is useful for define the type of menu
	 * 
	 * @param menuId The menu identifier
	 * 
	 * @return True if the menu is used by one or more
	 * objects, else false
	 * 
	 * @throws EMFUserError If any exception occurred
	 */
	public List getChildrenMenu (Integer menuId, Integer roleID) throws EMFUserError;
	
	
	/**
	 * Substitution between the current node and his father
	 * @param menuID
	 * @throws EMFUserError
	 */
	
	public void createMasterMenu(Integer menuID) throws EMFUserError;

	/**
	 * Move up the current node 
	 * @param menuID
	 * @throws EMFUserError
	 */
	
	public void moveUpMenu(Integer menuID) throws EMFUserError;
	
	/**
	 * Move down the current node 
	 * @param menuID
	 * @throws EMFUserError
	 */
	
	public void moveDownMenu(Integer menuID) throws EMFUserError;
}
