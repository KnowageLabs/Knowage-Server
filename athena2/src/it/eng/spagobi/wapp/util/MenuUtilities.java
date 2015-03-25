/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.wapp.util;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.wapp.bo.Menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.log4j.Logger;

public class MenuUtilities {

	private static Logger logger = Logger.getLogger(MenuUtilities.class);

	public static final String MODULE_PAGE = "LoginPage";
	public static final String DEFAULT_LAYOUT_MODE = "ALL_TOP";
	public static final String LAYOUT_ALL_TOP = "ALL_TOP";
	public static final String LAYOUT_ALL_LEFT = "ALL_LEFT";
	public static final String LAYOUT_TOP_LEFT = "TOP_LEFT";
	public static final String LAYOUT_ADMIN_MENU = "ADMIN_MENU";
	public static final String DEFAULT_EXTRA = "NO";
	public static final String MENU_MODE = "MENU_MODE";
	public static final String MENU_EXTRA = "MENU_EXTRA";
	public static final String LIST_MENU = "LIST_MENU";

	public static String getMenuPath(Menu menu, Locale locale) {
		String path ="";
		MessageBuilder msgBuild=new MessageBuilder();
		try {
			if (menu.getParentId() == null) {
				if (menu.getName().startsWith("#")){				
					String titleCode = menu.getName().substring(1);									
					path = msgBuild.getMessage(titleCode, locale);								
				} else {
					path = menu.getName();
				}
				return path;
			} else {
				Menu parent = DAOFactory.getMenuDAO().loadMenuByID(
						menu.getParentId());
				// can happen that parent is not found
				if (parent == null) {
					if (menu.getName().startsWith("#")){				
						String titleCode = menu.getName().substring(1);									
						path = msgBuild.getMessage(titleCode, locale);								
					} else {
						path = menu.getName();
					}
					return path;
				} else {
					return getMenuPath(parent, locale) + " > " + menu.getName();
				}
			}
		} catch (Exception e) {
			logger.error("Exception in getting menu path", e);
			return "";
		}
	}

	public static List filterListForUser(List menuList,
			IEngUserProfile userProfile) {
		List filteredMenuList = new ArrayList();
		if (menuList != null && !menuList.isEmpty()) {
			for (int i = 0; i < menuList.size(); i++) {
				Menu menuElem = (Menu) menuList.get(i);
				boolean canView = false;
				if (menuElem.getCode() == null)
					canView = MenuAccessVerifier.canView(menuElem, userProfile);
				else
					canView = true; // technical menu voice is ever visible if
									// it's present
				if (canView) {
					filteredMenuList.add(menuElem);
				}
			}
		}
		return filteredMenuList;
	}

	/**
	 * Gets the elements of menu relative by the user logged. It reaches the role from the request and 
	 * asks to the DB all detail
	 * menu information, by calling the method <code>loadMenuByRoleId</code>.
	 *   
	 * @param request The request Source Bean
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */   
	public static void getMenuItems(SourceBean request, SourceBean response, IEngUserProfile profile) throws EMFUserError {
		try {	
			List lstFinalMenu = new ArrayList();
			boolean technicalMenuLoaded = false;

			Collection lstRolesForUser = ((UserProfile)profile).getRolesForUse();
			logger.debug("** Roles for user: " + lstRolesForUser.size());

			Object[] arrRoles = lstRolesForUser.toArray();
			Integer levelItem = 1;			
			for (int i=0; i< arrRoles.length; i++) {
				logger.debug("*** arrRoles[i]): " + arrRoles[i]);
				Role role = (Role)DAOFactory.getRoleDAO().loadByName((String)arrRoles[i]);
				if (role != null) {	
					
					List menuItemsForARole  = DAOFactory.getMenuRolesDAO().loadMenuByRoleId(role.getId());
					if (menuItemsForARole != null) {
						mergeMenuItems(lstFinalMenu, menuItemsForARole);
					} else {
						logger.debug("Not found menu items for user role " + (String) arrRoles[i] );
					}
					
					if (!technicalMenuLoaded && UserUtilities.isTechnicalUser(profile)){ 
						//list technical user menu
						technicalMenuLoaded = true;						
						List firstLevelItems = ConfigSingleton.getInstance().getAttributeAsList("TECHNICAL_USER_MENU.ITEM");
						Iterator it = firstLevelItems.iterator();
						while (it.hasNext()) {
							SourceBean itemSB = (SourceBean) it.next();
							if (isAbleToSeeItem(itemSB, profile)) {

								lstFinalMenu.add(getAdminItemRec(itemSB, levelItem, profile, null));
								levelItem++;
							}
						}						
					}			      		        										
				}
				else
					logger.debug("Role " + (String)arrRoles[i] + " not found on db");
			}
			response.setAttribute(LIST_MENU, lstFinalMenu);

			logger.debug("List Menu Size " + lstFinalMenu.size());
			//String menuMode = (configSingleton.getAttribute("SPAGOBI.MENU.mode")==null)?DEFAULT_LAYOUT_MODE:(String)configSingleton.getAttribute("SPAGOBI.MENU.mode");
			//response.setAttribute(MENU_MODE, menuMode);
			response.setAttribute(MENU_MODE, DEFAULT_LAYOUT_MODE);

		} catch (Exception ex) {
			logger.error("Cannot fill response container" + ex.getLocalizedMessage());	
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 500, new Vector(), params);
		}
	}
	
	/**
	 * Gets the elements of menu relative by the user logged. It reaches the role from the request and 
	 * asks to the DB all detail
	 * menu information, by calling the method <code>loadMenuByRoleId</code>.
	 *   
	 * @param request The request Source Bean
	 * @param response The response Source Bean
	 * @throws EMFUserError If an exception occurs
	 */   
	public static List getMenuItems(IEngUserProfile profile) throws EMFUserError {
		try {	
			List lstFinalMenu = new ArrayList();
			boolean technicalMenuLoaded = false;

			Collection lstRolesForUser = ((UserProfile)profile).getRolesForUse();
			logger.debug("** Roles for user: " + lstRolesForUser.size());

			Object[] arrRoles = lstRolesForUser.toArray();
			Integer levelItem = 1;			
			for (int i=0; i< arrRoles.length; i++) {
				logger.debug("*** arrRoles[i]): " + arrRoles[i]);
				Role role = (Role)DAOFactory.getRoleDAO().loadByName((String)arrRoles[i]);
				if (role != null) {	
					
					List menuItemsForARole  = DAOFactory.getMenuRolesDAO().loadMenuByRoleId(role.getId());
					if (menuItemsForARole != null) {
						mergeMenuItems(lstFinalMenu, menuItemsForARole);
					} else {
						logger.debug("Not found menu items for user role " + (String) arrRoles[i] );
					}
					
					if (!technicalMenuLoaded && UserUtilities.isTechnicalUser(profile)){ 
						//list technical user menu
						technicalMenuLoaded = true;						
						List firstLevelItems = ConfigSingleton.getInstance().getAttributeAsList("TECHNICAL_USER_MENU.ITEM");
						Iterator it = firstLevelItems.iterator();
						while (it.hasNext()) {
							SourceBean itemSB = (SourceBean) it.next();
							if (isAbleToSeeItem(itemSB, profile)) {

								lstFinalMenu.add(getAdminItemRec(itemSB, levelItem, profile, null));
								levelItem++;
							}
						}						
					}			      		        										
				}
				else
					logger.debug("Role " + (String)arrRoles[i] + " not found on db");
			}		

			logger.debug("List Menu Size " + lstFinalMenu.size());
			return lstFinalMenu;
		} catch (Exception ex) {
			logger.error("Cannot fill response container" + ex.getLocalizedMessage());	
			HashMap params = new HashMap();
			params.put(AdmintoolsConstants.PAGE, MODULE_PAGE);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 500, new Vector(), params);
		}
	}

	private static void mergeMenuItems(List finalMenuList, List menuItemsForARole) {
		for (int j = 0; j < menuItemsForARole.size(); j++) {
			Menu aMenuItemForARole = (Menu) menuItemsForARole.get(j);
			// if the final menu list does not contain a specific role menu item, it is inserted into the final list
			int index = indexOf(finalMenuList, aMenuItemForARole);
			if (index == -1) {						
				finalMenuList.add(aMenuItemForARole);	
			} else {
				// we have to recursively cycle on children if any
				if (aMenuItemForARole.getHasChildren()) {
					List aMenuItemForARoleChildrenList = aMenuItemForARole.getLstChildren();
					Menu aFinalMenuItem = (Menu) finalMenuList.get(index);
					List finalMenuChildrenList = aFinalMenuItem.getLstChildren();
					mergeMenuItems(finalMenuChildrenList, aMenuItemForARoleChildrenList);
				}
			}
		}
	}

	/**
	 * This method checks if the single item is visible from the technical user
	 * @param itemSB the single item
	 * @param profile the profile
	 * @return boolean value
	 * @throws EMFInternalError
	 */
	private static boolean isAbleToSeeItem(SourceBean itemSB, IEngUserProfile profile) throws EMFInternalError {
		String functionality = (String) itemSB.getAttribute("functionality");
		if (functionality == null) {
			return isAbleToSeeContainedItems(itemSB, profile);
		} else {
			return profile.isAbleToExecuteAction(functionality);
		}
	}

	/**
	 * This method checks if the single item has other sub-items visible from the technical user
	 * @param itemSB the master item
	 * @param profile the profile
	 * @return boolean value
	 * @throws EMFInternalError
	 */
	private static  boolean isAbleToSeeContainedItems(SourceBean itemSB, IEngUserProfile profile) throws EMFInternalError {
		List subItems = itemSB.getAttributeAsList("ITEM");
		if (subItems == null || subItems.isEmpty()) return false;
		Iterator it = subItems.iterator();
		while (it.hasNext()) {
			SourceBean subItem = (SourceBean) it.next();
			String functionality = (String) subItem.getAttribute("functionality");
			String group= (String)subItem.getAttribute("groupingMenu");
			if (profile.isAbleToExecuteAction(functionality) || (group != null && group.equals("true"))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method return a Menu type element recursivly with the technical user item (the item is created in memory, it isn't on db)
	 * @param itemSB the technical item to add
	 * @param father
	 * @return
	 */
	private static Menu getAdminItemRec(SourceBean itemSB, Integer progStart, IEngUserProfile profile, Integer parent){
		Menu node = new Menu();
		try{
			Integer menuId = new Integer(progStart*1000);
			String functionality = (String) itemSB.getAttribute("functionality");
			String code = (String) itemSB.getAttribute("code");
			String titleCode = (String) itemSB.getAttribute("title");
			String iconUrl = (String) itemSB.getAttribute("iconUrl");
			String url = (String) itemSB.getAttribute("url");
			String iconCls = (String) itemSB.getAttribute("iconCls");
			String groupingMenu = (String) itemSB.getAttribute("groupingMenu");

			node.setParentId(parent);
			node.setMenuId(menuId);	
	
			node.setCode(code);		

			node.setProg(progStart);
			node.setName(titleCode);
			node.setLevel(new Integer(1));
			node.setDescr(titleCode);
			node.setUrl(url);
			node.setViewIcons(true);
			node.setIconPath(iconUrl);
			node.setAdminsMenu(true);
			node.setIconCls(iconCls);
			if(groupingMenu != null)
				node.setGroupingMenu(groupingMenu);

			if (functionality == null) {
				//father node
				List subItems = itemSB.getAttributeAsList("ITEM");	
				Iterator it = subItems.iterator();
				if (subItems == null || subItems.isEmpty())
					node.setHasChildren(false);
				else{
					node.setHasChildren(true);			
					List lstChildren = new ArrayList();
					while (it.hasNext()) {
						//defines children
						SourceBean subItem = (SourceBean) it.next();
						if (isAbleToSeeItem(subItem, profile)) {
							Menu subNode = getAdminItemRec(subItem, progStart, profile, menuId);
							lstChildren.add(subNode);
						}
					}
					node.setLstChildren(lstChildren);
					progStart++;

				}

			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			logger.debug("OUT");
		}
		return node;
	}


	/**
	 * Check if the menu element in input is already presents into the list
	 * @param lst the list to check
	 * @param menu the element to check
	 * @return the index of the input menu item or -1 if it is not found in the list
	 */
	public static int indexOf(List lst, Menu menu){
		if (lst == null)
			return -1;
		for (int i = 0; i < lst.size(); i++) {
			Menu tmpMenu = (Menu) lst.get(i);
			if (tmpMenu.getMenuId().intValue() == menu.getMenuId().intValue()) {
				return i;
			}
		}
		return -1;
	}

}
