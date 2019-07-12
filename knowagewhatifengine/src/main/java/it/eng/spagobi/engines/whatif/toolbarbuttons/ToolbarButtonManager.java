/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engines.whatif.toolbarbuttons;

import java.util.ArrayList;
import java.util.List;

public class ToolbarButtonManager {

	private static List<SbiToolbarButton> listOfToolbarButtons;

	private static void init() {
		listOfToolbarButtons = new ArrayList<SbiToolbarButton>();
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_DRILL_THROUGH", true, false, false, ToolbarButtonCategory.DRILL_ON_DATA.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_MDX", true, false, false, ToolbarButtonCategory.OLAP_FUNCTIONS.toString()));
		// listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_EDIT_MDX", false, false, false, ToolbarButtonCategory.OLAP_FUNCTIONS.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_FATHER_MEMBERS", true, false, true, ToolbarButtonCategory.TABLE_FUNCTIONS.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_CC", true, false, false, ToolbarButtonCategory.TABLE_FUNCTIONS.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_HIDE_SPANS", true, false, true, ToolbarButtonCategory.TABLE_FUNCTIONS.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_SORTING_SETTINGS", true, false, false, ToolbarButtonCategory.TABLE_FUNCTIONS.toString()));
		// listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_SORTING", false, false, false, ToolbarButtonCategory.TABLE_FUNCTIONS.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_SHOW_PROPERTIES", true, false, true, ToolbarButtonCategory.TABLE_FUNCTIONS.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_HIDE_EMPTY", true, false, true, ToolbarButtonCategory.TABLE_FUNCTIONS.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_SAVE_NEW", false, false, false, ToolbarButtonCategory.WHAT_IF.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_UNDO", false, false, false, ToolbarButtonCategory.WHAT_IF.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_VERSION_MANAGER", false, false, false, ToolbarButtonCategory.WHAT_IF.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_EXPORT_OUTPUT", false, false, false, ToolbarButtonCategory.WHAT_IF.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_SAVE_SUBOBJECT", false, false, false, ToolbarButtonCategory.TABLE_FUNCTIONS.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_EDITABLE_EXCEL_EXPORT", false, false, false, ToolbarButtonCategory.WHAT_IF.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_ALGORITHMS", false, false, false, ToolbarButtonCategory.WHAT_IF.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_FLUSH_CACHE", false, false, false, ToolbarButtonCategory.OLAP_FUNCTIONS.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_MDX", true, false, false, ToolbarButtonCategory.OLAP_DESIGNER.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_SCENARIO_WIZARD", false, false, false, ToolbarButtonCategory.OLAP_DESIGNER.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_PAGINATION_WIZARD", false, false, false, ToolbarButtonCategory.OLAP_DESIGNER.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_CROSSNAV_WIZARD", false, false, false, ToolbarButtonCategory.OLAP_DESIGNER.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_WIZARD", false, false, false, ToolbarButtonCategory.OLAP_DESIGNER.toString()));
		listOfToolbarButtons.add(new SbiToolbarButton("BUTTON_CC", true, false, false, ToolbarButtonCategory.OLAP_DESIGNER.toString()));

	}

	public static List<SbiToolbarButton> loadAllToolbarButtons() {
		init();
		return listOfToolbarButtons;
	}
}
