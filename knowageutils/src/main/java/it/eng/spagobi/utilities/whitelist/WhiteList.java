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
import it.eng.spagobi.utilities.filters.XSSRequestWrapper;

public enum WhiteList {

	INSTANCE;

	private static transient Logger logger = Logger.getLogger(XSSRequestWrapper.class);
	private final String WHITELIST_FILE = "services-whitelist.xml";

	private List<String> getProperties(String property) {
		logger.debug("IN");
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
			logger.error("Can not read white-list services from configuration file ", e);
			return services;
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e) {
				logger.error("Can not close the stream resources ", e);
			}
		}

		logger.debug("OUT");
		return services;
	}

	public List<String> getRelativePaths() {
		return getProperties("relativepath");
	}

	public List<String> getExternalServices() {
		return getProperties("baseurl");
	}

}
