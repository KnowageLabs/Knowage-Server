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

package it.eng.spagobi.engines.qbe.query.formula;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * Class for getting property values from formula.properties
 *
 * @author dpirkovic
 *
 */
public class FormulaConfig {

	private static final String PROPERTY_FILE = "formula.properties";
	public static transient Logger logger = Logger.getLogger(FormulaConfig.class);

	/**
	 *
	 * @param property
	 * @return property value
	 */
	public String getProperty(String property) {

		logger.debug("IN");
		String propValue = null;
		Properties props;
		InputStream inputStream = null;
		URL url;

		props = new Properties();
		url = this.getClass().getClassLoader().getResource(PROPERTY_FILE);
		logger.debug("Url path for proterty file" + PROPERTY_FILE + "is :" + url);
		try {
			if (url != null) {
				inputStream = url.openStream();
				logger.debug("Input stream opened for url:" + url);
				props.load(inputStream);
				logger.debug("Properties loaded");
				propValue = props.getProperty(property);
				logger.debug("Value for property " + property + " is: " + propValue);
				inputStream.close();
				logger.debug("Input stream closed");
			}

			return propValue;

		} catch (IOException e) {
			logger.error("Error while loading property file: " + PROPERTY_FILE, e);
			throw new SpagoBIEngineRuntimeException("Error while loading property file: " + PROPERTY_FILE, e);

		} finally {
			logger.debug("OUT");
		}
	}

}
