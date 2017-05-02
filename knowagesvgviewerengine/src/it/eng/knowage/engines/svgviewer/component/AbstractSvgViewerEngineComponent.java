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
package it.eng.knowage.engines.svgviewer.component;

import it.eng.knowage.engines.svgviewer.SvgViewerEngineException;

import java.util.Map;

/**
 * The Class AbstractGeoEngineComponent.
 *
 */
public class AbstractSvgViewerEngineComponent implements ISvgViewerEngineComponent {

	/** The conf. */
	Object conf;

	/** The env. */
	Map env;

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.IGeoEngineComponent#init(java.lang.Object)
	 */
	@Override
	public void init(Object conf) throws SvgViewerEngineException {
		this.conf = conf;
	}

	/**
	 * Gets the conf.
	 *
	 * @return the conf
	 */
	protected Object getConf() {
		return conf;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.geo.IGeoEngineComponent#setEnv(java.util.Map)
	 */
	@Override
	public void setEnv(Map env) {
		this.env = env;
	}

	/**
	 * Gets the env.
	 *
	 * @return the env
	 */
	public Map getEnv() {
		return env;
	}

}
