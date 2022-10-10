/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.parameter;

import static java.util.stream.Collectors.joining;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;

public class SolrParameterManager extends AbstractParameterManager {

	private static final Logger logger = LogManager.getLogger(SolrParameterManager.class);

	private static final SolrParameterManager INSTANCE = new SolrParameterManager();

	public static SolrParameterManager getInstance() {
		return INSTANCE;
	}

	private SolrParameterManager() {
		super();
	}

	/**
	 * Protected for testing purposes
	 *
	 * @param value
	 * @param type
	 * @return
	 */
	@Override
	protected String getSingleValue(String value, String type) {
		String toReturn = "";
		if (type.equalsIgnoreCase(DataSetUtilities.STRING_TYPE)) {

			toReturn = value.replace("'", "\\'");

		} else if (type.equalsIgnoreCase(DataSetUtilities.NUMBER_TYPE)) {

			if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
				toReturn = value.substring(1, value.length() - 1);
			} else {
				toReturn = value;
			}
			if (toReturn == null || toReturn.length() == 0) {
				toReturn = "";
			}
		} else if (type.equalsIgnoreCase(DataSetUtilities.GENERIC_TYPE)) {
			toReturn = value;
		} else if (type.equalsIgnoreCase(DataSetUtilities.RAW_TYPE)) {
			if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
				toReturn = value.substring(1, value.length() - 1);
			} else {
				toReturn = value;
			}
		}

		return toReturn;
	}

	@Override
	protected String getMultiValue(List<Object> value, String type) {
		return value.stream()
				.map(String::valueOf)
				.map(e -> getSingleValue(e, type))
				.collect(joining(" OR ", "(", ")"));
	}

}
