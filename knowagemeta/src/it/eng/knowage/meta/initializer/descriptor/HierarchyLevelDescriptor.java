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

import it.eng.knowage.meta.model.business.BusinessColumn;

//import org.eclipse.swt.custom.CCombo;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.TableItem;
//import org.eclipse.swt.widgets.Text;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class HierarchyLevelDescriptor {

	private BusinessColumn businessColumn;
	private String name;
	private String description;
	private BusinessColumn nameColumn;
	private BusinessColumn captionColumn;
	private boolean uniqueMembers;
	private BusinessColumn ordinalColumn;
	private String levelType;

	// private TableItem ui_tableItem;
	// private Text ui_textLevelName;
	// private Button ui_buttonRemove;
	// private Text ui_textDescription;
	// private CCombo ui_comboNameColumn;
	// private CCombo ui_comboCaptionColumn;
	// private CCombo ui_comboUniqueMembers;
	// private CCombo ui_comboOrdinalColumn;
	// private CCombo ui_comboLevelType;

	/**
	 * @return the businessColumn
	 */
	public BusinessColumn getBusinessColumn() {
		return businessColumn;
	}

	/**
	 * @param businessColumn
	 *            the businessColumn to set
	 */
	public void setBusinessColumn(BusinessColumn businessColumn) {
		this.businessColumn = businessColumn;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the nameColumn
	 */
	public BusinessColumn getNameColumn() {
		return nameColumn;
	}

	/**
	 * @param nameColumn
	 *            the nameColumn to set
	 */
	public void setNameColumn(BusinessColumn nameColumn) {
		this.nameColumn = nameColumn;
	}

	/**
	 * @return the captionColumn
	 */
	public BusinessColumn getCaptionColumn() {
		return captionColumn;
	}

	/**
	 * @param captionColumn
	 *            the captionColumn to set
	 */
	public void setCaptionColumn(BusinessColumn captionColumn) {
		this.captionColumn = captionColumn;
	}

	/**
	 * @return the ordinalColumn
	 */
	public BusinessColumn getOrdinalColumn() {
		return ordinalColumn;
	}

	/**
	 * @param ordinalColumn
	 *            the ordinalColumn to set
	 */
	public void setOrdinalColumn(BusinessColumn ordinalColumn) {
		this.ordinalColumn = ordinalColumn;
	}

	/**
	 * @return the uniqueMembers
	 */
	public boolean isUniqueMembers() {
		return uniqueMembers;
	}

	/**
	 * @param uniqueMembers
	 *            the uniqueMembers to set
	 */
	public void setUniqueMembers(boolean uniqueMembers) {
		this.uniqueMembers = uniqueMembers;
	}

	/**
	 * @return the levelType
	 */
	public String getLevelType() {
		return levelType;
	}

	/**
	 * @param levelType
	 *            the levelType to set
	 */
	public void setLevelType(String levelType) {
		this.levelType = levelType;
	}

	// /**
	// * @return the ui_textLevelName
	// */
	// public Text getUi_textLevelName() {
	// return ui_textLevelName;
	// }
	//
	// /**
	// * @param ui_textLevelName
	// * the ui_textLevelName to set
	// */
	// public void setUi_textLevelName(Text ui_textLevelName) {
	// this.ui_textLevelName = ui_textLevelName;
	// }
	//
	// /**
	// * @return the ui_buttonRemove
	// */
	// public Button getUi_buttonRemove() {
	// return ui_buttonRemove;
	// }
	//
	// /**
	// * @param ui_buttonRemove
	// * the ui_buttonRemove to set
	// */
	// public void setUi_buttonRemove(Button ui_buttonRemove) {
	// this.ui_buttonRemove = ui_buttonRemove;
	// }
	//
	// /**
	// * @return the ui_tableItem
	// */
	// public TableItem getUi_tableItem() {
	// return ui_tableItem;
	// }
	//
	// /**
	// * @param ui_tableItem
	// * the ui_tableItem to set
	// */
	// public void setUi_tableItem(TableItem ui_tableItem) {
	// this.ui_tableItem = ui_tableItem;
	// }
	//
	// /**
	// * @return the ui_textDescription
	// */
	// public Text getUi_textDescription() {
	// return ui_textDescription;
	// }
	//
	// /**
	// * @param ui_textDescription
	// * the ui_textDescription to set
	// */
	// public void setUi_textDescription(Text ui_textDescription) {
	// this.ui_textDescription = ui_textDescription;
	// }
	//
	// /**
	// * @return the ui_textNameColumn
	// */
	// public CCombo getUi_comboNameColumn() {
	// return ui_comboNameColumn;
	// }
	//
	// /**
	// * @param ui_textNameColumn
	// * the ui_textNameColumn to set
	// */
	// public void setUi_comboNameColumn(CCombo ui_comboNameColumn) {
	// this.ui_comboNameColumn = ui_comboNameColumn;
	// }
	//
	// /**
	// * @return the ui_textCaptionColumn
	// */
	// public CCombo getUi_comboCaptionColumn() {
	// return ui_comboCaptionColumn;
	// }
	//
	// /**
	// * @param ui_textCaptionColumn
	// * the ui_textCaptionColumn to set
	// */
	// public void setUi_comboCaptionColumn(CCombo ui_comboCaptionColumn) {
	// this.ui_comboCaptionColumn = ui_comboCaptionColumn;
	// }
	//
	// /**
	// * @return the ui_comboUniqueMembers
	// */
	// public CCombo getUi_comboUniqueMembers() {
	// return ui_comboUniqueMembers;
	// }
	//
	// /**
	// * @param ui_comboUniqueMembers
	// * the ui_comboUniqueMembers to set
	// */
	// public void setUi_comboUniqueMembers(CCombo ui_comboUniqueMembers) {
	// this.ui_comboUniqueMembers = ui_comboUniqueMembers;
	// }
	//
	// /**
	// * @return the ui_comboOrdinalColumn
	// */
	// public CCombo getUi_comboOrdinalColumn() {
	// return ui_comboOrdinalColumn;
	// }
	//
	// /**
	// * @param ui_comboOrdinalColumn
	// * the ui_comboOrdinalColumn to set
	// */
	// public void setUi_comboOrdinalColumn(CCombo ui_comboOrdinalColumn) {
	// this.ui_comboOrdinalColumn = ui_comboOrdinalColumn;
	// }
	//
	// /**
	// * @return the ui_comboLevelType
	// */
	// public CCombo getUi_comboLevelType() {
	// return ui_comboLevelType;
	// }
	//
	// /**
	// * @param ui_comboLevelType
	// * the ui_comboLevelType to set
	// */
	// public void setUi_comboLevelType(CCombo ui_comboLevelType) {
	// this.ui_comboLevelType = ui_comboLevelType;
	// }
	//
	// public void disposeUiElements() {
	// ui_textLevelName.dispose();
	// ui_buttonRemove.dispose();
	// // ui_textDescription.dispose();
	// ui_comboNameColumn.dispose();
	// ui_comboCaptionColumn.dispose();
	// ui_comboUniqueMembers.dispose();
	// ui_comboOrdinalColumn.dispose();
	// ui_tableItem.dispose();
	// if (ui_comboLevelType != null) {
	// ui_comboLevelType.dispose();
	// }
	//
	// }
	//
	// @Override
	// public String toString() {
	// String result = "";
	// if (name != null) {
	// result = result + "Name: " + name;
	// }
	// if (businessColumn.getUniqueName() != null) {
	// result = result + " Column: " + businessColumn.getUniqueName();
	// }
	// if (ordinalColumn != null) {
	// result = result + " OrdinalColumn: " + ordinalColumn.getName();
	// }
	// if (nameColumn != null) {
	// result = result + " NameColumn: " + nameColumn.getName();
	// }
	// if (captionColumn != null) {
	// result = result + " CaptionColumn: " + captionColumn.getName();
	// }
	//
	// result = result + " UniqueMembers: " + uniqueMembers;
	//
	// if (levelType != null) {
	// result = result + " Level Type: " + levelType;
	// }
	//
	// return result;
	// }

}
