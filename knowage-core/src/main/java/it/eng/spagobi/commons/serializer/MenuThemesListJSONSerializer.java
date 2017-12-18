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
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.GeneralUtilities;

import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Chiarelli Chiara
 */
public class MenuThemesListJSONSerializer implements Serializer {

	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String TEXT = "text";
	public static final String PATH = "path";
	public static final String CLS = "cls";
	public static final String ICON = "icon";
	public static final String GROUP = "group";
	public static final String HREF = "href";
	public static final String TYPE = "type";
	public static final String ITEMS = "items";
	public static final String css = "x-btn-menubutton x-btn-text-icon bmenu";
	public String contextName = "";
	public String defaultThemePath = "/themes/sbi_default";

	static protected Logger logger = Logger.getLogger(MenuThemesListJSONSerializer.class);

	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject result = null;

		contextName = GeneralUtilities.getSpagoBiContext();
		if (!(o instanceof List)) {
			throw new SerializationException("MenuListJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}

		try {
			List<String> filteredMenuList = (List) o;
			if (filteredMenuList != null && !filteredMenuList.isEmpty()) {
				result = new JSONObject();
				JSONArray tempFirstLevelMenuList = new JSONArray();
				for (int i = 0; i < filteredMenuList.size(); i++) {
					String theme = filteredMenuList.get(i);
					String name = SingletonConfig.getInstance().getConfigValue("SPAGOBI.THEMES.THEME." + theme + ".name");
					String viewName = SingletonConfig.getInstance().getConfigValue("SPAGOBI.THEMES.THEME." + theme + ".view_name");
					if (viewName == null || viewName.equalsIgnoreCase("")) {
						viewName = name;
					}
					JSONObject temp = new JSONObject();
					temp.put(ID, new Double(Math.random()).toString());
					temp.put(TEXT, viewName);
					temp.put(HREF, "javascript:execUrl('" + contextName + "/servlet/AdapterHTTP?ACTION_NAME=CHANGE_THEME&THEME_NAME=" + name + "')");
					tempFirstLevelMenuList.put(temp);
				}
				result.put(ITEMS, tempFirstLevelMenuList);
			}
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

		}
		// logger.debug(result);
		return result;
	}

}
