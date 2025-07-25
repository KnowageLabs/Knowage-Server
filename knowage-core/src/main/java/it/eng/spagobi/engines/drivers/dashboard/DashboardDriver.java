/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.spagobi.engines.drivers.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import it.eng.spagobi.engines.drivers.cockpit.CockpitDriver;

/**
 * @author albnale
 *
 *         The purpose of this class is to inherit the behavior of the cockpit UNTIL the transition from the cockpit to the dashboard is completed
 */
public class DashboardDriver extends CockpitDriver {

	private static Logger logger = Logger.getLogger(DashboardDriver.class);

	@Override
	public ArrayList<String> getFunctionsAssociated(byte[] contentTemplate) throws JSONException {
		logger.debug("IN");

		ArrayList<String> functionUuids = new ArrayList<>();
		if (contentTemplate == null) {
			logger.error("Template content non returned. Impossible get associated functions. Check the template!");
			return functionUuids;
		}

		Configuration conf = Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL).build();
		List<String> catalogFunction = JsonPath.using(conf).parse(new String(contentTemplate)).read("$.widgets[*].columns[*].catalogFunctionId");
		functionUuids.addAll(catalogFunction.stream().filter(Objects::nonNull).toList());

		logger.debug("OUT");
		return functionUuids;
	}

}
