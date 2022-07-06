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
package it.eng.spagobi.utilities.whitelist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

public class WhiteList implements IWhiteList {

	private static final Logger LOGGER = Logger.getLogger(WhiteList.class);

	private static final WhiteList INSTANCE = new WhiteList();

	public static WhiteList getInstance() {
		return INSTANCE;
	}

	private final String WHITELIST_FILE = "services-whitelist.xml";

	private WhiteList() {

	}

	private List<String> getProperties(String property) {
		LOGGER.debug("IN");
		List<String> services = new ArrayList<String>();
		FileInputStream stream = null;
		try {
			String servicesWhitelist = SpagoBIUtilities.getRootResourcePath() + "/" + WHITELIST_FILE;
			File file = new File(servicesWhitelist);
			stream = new FileInputStream(file);

			if (!file.exists() || file.isDirectory()) {
				return services;
			} else {
				SourceBean bean = SourceBean.fromXMLStream(new InputSource(stream));
				List servicesSourceBeans = bean.getAttributeAsList("service");
				Iterator iterator = servicesSourceBeans.iterator();

				while (iterator.hasNext()) {
					SourceBean servicebean = (SourceBean) iterator.next();
					if (servicebean.containsAttribute(property)) {
						String baseUrl = servicebean.getAttribute(property).toString();
						services.add(baseUrl);
					}
				}
				stream.close();
			}
		} catch (Exception e) {
			LOGGER.error("Can not read white-list services from configuration file ", e);
			return services;
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				LOGGER.error("Can not close the stream resources ", e);
			}
		}

		LOGGER.debug("OUT");
		return services;
	}

	@Override
	public List<String> getRelativePaths() {
		return getProperties("relativepath");
	}

	@Override
	public List<String> getExternalServices() {
		return getProperties("baseurl");
	}

}
