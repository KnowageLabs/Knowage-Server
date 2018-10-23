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
package it.eng.knowage.engines.svgviewer.map.renderer.configurator;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;
import it.eng.knowage.engines.svgviewer.map.renderer.InteractiveMapRenderer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;

import org.apache.log4j.Logger;

/**
 * The Class InteractiveMapRendererConfigurator.
 *
 */
public class InteractiveMapRendererConfigurator {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(InteractiveMapRendererConfigurator.class);

	/**
	 * Configure.
	 *
	 * @param interactiveMapRenderer
	 *            the interactive map renderer
	 * @param conf
	 *            the conf
	 *
	 * @throws SvgViewerEngineException
	 *             the geo engine exception
	 */
	public static void configure(InteractiveMapRenderer interactiveMapRenderer, Object conf) throws SvgViewerEngineException {
		SourceBean confSB = null;

		if (conf instanceof String) {
			try {
				confSB = SourceBean.fromXMLString((String) conf);
			} catch (SourceBeanException e) {
				logger.error("Impossible to parse configuration block for MapRenderer", e);
				throw new SvgViewerEngineException("Impossible to parse configuration block for MapRenderer", e);
			}
		} else {
			confSB = (SourceBean) conf;
		}
	}

}
