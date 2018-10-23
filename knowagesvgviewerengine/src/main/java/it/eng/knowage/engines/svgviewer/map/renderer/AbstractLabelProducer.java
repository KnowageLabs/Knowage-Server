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
package it.eng.knowage.engines.svgviewer.map.renderer;

import it.eng.knowage.engines.svgviewer.map.renderer.configurator.AbstractMapRendererConfigurator;
import it.eng.spago.base.SourceBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractLabelProducer implements ILabelProducer {

	Map settings;

	@Override
	public void init(SourceBean conf) {
		if (settings == null) {
			settings = new HashMap();
		}

		List params = conf.getAttributeAsList("PARAM");
		AbstractMapRendererConfigurator.addSettings(getSettings(), params);
	}

	@Override
	public Map getSettings() {
		return settings;
	}

	public void setSettings(Map settings) {
		this.settings = settings;
	}
}
