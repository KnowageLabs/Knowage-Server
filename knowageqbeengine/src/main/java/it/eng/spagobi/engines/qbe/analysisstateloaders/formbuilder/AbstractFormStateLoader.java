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
package it.eng.spagobi.engines.qbe.analysisstateloaders.formbuilder;

import org.json.JSONObject;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */
public abstract class AbstractFormStateLoader implements IFormStateLoader {

	IFormStateLoader nextLoader;

	protected AbstractFormStateLoader() {
	}

	protected AbstractFormStateLoader(IFormStateLoader loader) {
		setNextLoader(loader);
	}

	@Override
	public JSONObject load(String rowData) {
		JSONObject result;

		try {
			// load data
			result = new JSONObject(rowData);
			result = this.load(result);
		} catch (Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from rowData [" + rowData + "]", t);
		}

		return result;
	}

	@Override
	public JSONObject load(JSONObject jsonObject) {
		JSONObject result;

		try {
			result = this.convert(jsonObject);
			// make next converts
			if (nextLoader != null) {
				result = nextLoader.load(result);
			}
		} catch (Throwable t) {
			throw new SpagoBIEngineRuntimeException("Impossible to load from JSON object [" + jsonObject + "]", t);
		}

		return result;
	}

	public IFormStateLoader getNextLoader() {
		return nextLoader;
	}

	void setNextLoader(IFormStateLoader nextLoader) {
		this.nextLoader = nextLoader;
	}
}
