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
package it.eng.knowage.boot.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import it.eng.knowage.boot.filter.IWhiteList;
import it.eng.knowage.commons.security.KnowageSystemConfiguration;

public class WhiteList implements IWhiteList {

	private static final Logger LOGGER = Logger.getLogger(WhiteList.class);

	private static final WhiteList INSTANCE = new WhiteList();

	public static WhiteList getInstance() {
		return INSTANCE;
	}

	private final String WHITELIST_FILE = "services-whitelist.xml";

	private WhiteList() {

	}

	private List<Service> getProperties() {
		LOGGER.debug("IN");
		List<Service> services = new ArrayList<>();
		String servicesWhitelist = ContextPropertiesConfig.getResourcePath() + "/" + WHITELIST_FILE;
		File file = new File(servicesWhitelist);

		try (FileInputStream stream = new FileInputStream(file)) {
			XStream xstream = new XStream();
			xstream.alias("WHITELIST", WhiteListBean.class);
			xstream.autodetectAnnotations(true);
			xstream.registerConverter(new ServiceConverter());
			xstream.addImplicitCollection(WhiteListBean.class, "service", Service.class);
			xstream.allowTypes(new Class[] { it.eng.knowage.boot.utils.WhiteListBean.class });
			String fileString = getFileContent(stream, UTF_8.name());
			if (!file.exists() || file.isDirectory()) {
				return services;
			} else {
				WhiteListBean bean = (WhiteListBean) xstream.fromXML(fileString);
				for (Service ser : bean.service) {
					services.add(ser);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Can not read white-list services from configuration file ", e);
			return services;
		}

		LOGGER.debug("OUT");
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

	@Override
	public List<String> getRelativePaths() {
		List<String> ret = getProperties().stream().filter(e -> e.relativepath != null).map(e -> e.relativepath).collect(Collectors.toList());

		ret.add(KnowageSystemConfiguration.getKnowageContext());
		ret.add(KnowageSystemConfiguration.getKnowageAPIContext());
		ret.add(KnowageSystemConfiguration.getKnowageBirtReportEngineContext());
		ret.add(KnowageSystemConfiguration.getKnowageCockpitEngineContext());
		ret.add(KnowageSystemConfiguration.getKnowageDataPreparationContext());
		ret.add(KnowageSystemConfiguration.getKnowageDossierEngineContext());
		ret.add(KnowageSystemConfiguration.getKnowageJasperReportEngineContext());
		ret.add(KnowageSystemConfiguration.getKnowageKpiEngineContext());
		ret.add(KnowageSystemConfiguration.getKnowageMetaContext());
		ret.add(KnowageSystemConfiguration.getKnowageQbeEngineContext());
		ret.add(KnowageSystemConfiguration.getKnowageTalendEngineContext());
		ret.add(KnowageSystemConfiguration.getKnowageVueContext());
		ret.add(KnowageSystemConfiguration.getKnowageWhatifEngineContext());

		return ret;
	}

	@Override
	public List<String> getExternalServices() {
		List<String> extServiceList = getProperties().stream().filter(e -> e.baseurl != null).map(e -> e.baseurl).collect(Collectors.toList());
		// baseURL dei servizi knowage
		String serviceUrl ="/";
		try {
			Context ctx = new InitialContext();
			serviceUrl = (String) ctx.lookup("java:comp/env/service_url");
		} catch (NamingException e1) {
			LOGGER.error("Cannot read service_url from jndiContext", e1);
		}
		extServiceList.add(serviceUrl);
		return extServiceList;
	}

}
