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
package it.eng.spagobi.engines.whatif.crossnavigation;

import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.initializer.impl.TargetsClickableInitializerImpl;

public class TargetClickable implements Serializable {

	/*
	 * Target clickable in mdx configuration
	 */
	private static final long serialVersionUID = 7603427843810725619L;
	private String title;
	private String uniqueName;
	private TreeMap<String, String> parametersList;

	public TargetClickable() {
	}

	public TargetClickable(SourceBean sb) {

		title = (String) sb.getAttribute(TargetsClickableInitializerImpl.TAG_CNC_TITLE);
		uniqueName = (String) sb.getAttribute(TargetsClickableInitializerImpl.TAG_CNC_UNIQUENAME);
		List parameters = sb.getAttributeAsList(TargetsClickableInitializerImpl.TAG_CNC_PARAMETERS);
		boolean hasParameters = parameters != null && !parameters.isEmpty();
		if (hasParameters) {
			parametersList = new TreeMap<String, String>();
			for (int i = 0; i < parameters.size(); i++) {
				SourceBean temp = (SourceBean) parameters.get(i);
				String clickParameterName = (String) temp.getAttribute(TargetsClickableInitializerImpl.TAG_CNC_NAME);
				String clickParameterValue = (String) temp.getAttribute(TargetsClickableInitializerImpl.TAG_CNC_VALUE);
				parametersList.put(clickParameterName, clickParameterValue);
			}
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	public TreeMap<String, String> getParametersList() {
		return parametersList;
	}

	public void setParametersList(TreeMap<String, String> parametersList) {
		this.parametersList = parametersList;
	}

}
