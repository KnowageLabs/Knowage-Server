/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.knowage.meta.initializer.descriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class HierarchyDescriptor {

	private String name;
	private boolean hasAll;
	private boolean defaultHierarchy;
	private String allMemberName;
	private List<HierarchyLevelDescriptor> levels;

	// private TableItem ui_tableItem;
	// private Button ui_buttonEdit;
	// private Button ui_buttonRemove;

	public HierarchyDescriptor() {
		name = "";
		allMemberName = "";
		hasAll = true;
		defaultHierarchy = false;
		levels = new ArrayList<HierarchyLevelDescriptor>();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the hasAll
	 */
	public boolean isHasAll() {
		return hasAll;
	}

	/**
	 * @param hasAll
	 *            the hasAll to set
	 */
	public void setHasAll(boolean hasAll) {
		this.hasAll = hasAll;
	}

	/**
	 * @return the defaultHierarchy
	 */
	public boolean isDefaultHierarchy() {
		return defaultHierarchy;
	}

	/**
	 * @param defaultHierarchy
	 *            the defaultHierarchy to set
	 */
	public void setDefaultHierarchy(boolean defaultHierarchy) {
		this.defaultHierarchy = defaultHierarchy;
	}

	/**
	 * @return the allMemberName
	 */
	public String getAllMemberName() {
		return allMemberName;
	}

	/**
	 * @param allMemberName
	 *            the allMemberName to set
	 */
	public void setAllMemberName(String allMemberName) {
		this.allMemberName = allMemberName;
	}

	/**
	 * @return the levels
	 */
	public List<HierarchyLevelDescriptor> getLevels() {
		return levels;
	}

	/**
	 * @param levels
	 *            the levels to set
	 */
	public void setLevels(List<HierarchyLevelDescriptor> levels) {
		this.levels = levels;
	}

	// /**
	// * @return the ui_tableItem
	// */
	// public TableItem getUi_tableItem() {
	// return ui_tableItem;
	// }
	//
	// /**
	// * @param ui_tableItem the ui_tableItem to set
	// */
	// public void setUi_tableItem(TableItem ui_tableItem) {
	// this.ui_tableItem = ui_tableItem;
	// }
	//
	// /**
	// * @return the ui_buttonEdit
	// */
	// public Button getUi_buttonEdit() {
	// return ui_buttonEdit;
	// }
	//
	// /**
	// * @param ui_buttonEdit the ui_buttonEdit to set
	// */
	// public void setUi_buttonEdit(Button ui_buttonEdit) {
	// this.ui_buttonEdit = ui_buttonEdit;
	// }
	//
	// /**
	// * @return the ui_buttonRemove
	// */
	// public Button getUi_buttonRemove() {
	// return ui_buttonRemove;
	// }

	// /**
	// * @param ui_buttonRemove the ui_buttonRemove to set
	// */
	// public void setUi_buttonRemove(Button ui_buttonRemove) {
	// this.ui_buttonRemove = ui_buttonRemove;
	// }
	//
	// public void disposeUiElements() {
	// ui_buttonEdit.dispose();
	// ui_buttonRemove.dispose();
	// //ui_tableItem.dispose();
	//
	// }

}
