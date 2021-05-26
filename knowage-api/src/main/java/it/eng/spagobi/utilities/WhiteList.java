package it.eng.spagobi.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import it.eng.knowage.knowageapi.utils.ContextPropertiesConfig;

public class WhiteList {

	private static final WhiteList INSTANCE = new WhiteList();

	private static Logger logger = Logger.getLogger(WhiteList.class);
	private final String WHITELIST_FILE = "services-whitelist.xml";

	private WhiteList() {

	}

	private List<String> getProperties(String property) {
		logger.debug("IN");
		List<String> services = new ArrayList<String>();
		FileInputStream stream = null;
		try {
			String servicesWhitelist = ContextPropertiesConfig.getResourcePath() + "/" + WHITELIST_FILE;
			File file = new File(servicesWhitelist);
			stream = new FileInputStream(file);
			XStream xstream = new XStream();
			xstream.alias("WHITELIST", WhiteListBean.class);
			xstream.autodetectAnnotations(true);
			xstream.registerConverter(new ServiceConverter());
			xstream.addImplicitCollection(WhiteListBean.class, "service", Service.class);
			String fileString = getFileContent(stream, "UTF-8");
			if (!file.exists() || file.isDirectory()) {
				return services;
			} else {
				WhiteListBean bean = (WhiteListBean) xstream.fromXML(fileString);
				for (Service ser : bean.service) {
					services.add(ser.baseurl);
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

	public static String getFileContent(FileInputStream fis, String encoding) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			return sb.toString();
		}
	}

	public static WhiteList getInstance() {
		return INSTANCE;
	}

	public List<String> getRelativePaths() {
		return getProperties("relativepath");
	}

	public List<String> getExternalServices() {
		return getProperties("baseurl");
	}

}
