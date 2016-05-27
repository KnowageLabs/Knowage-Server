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
package it.eng.spagobi;

import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.configuration.FileCreatorConfiguration;
import it.eng.spagobi.commons.SimpleSingletonConfigCache;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.MockContext;
import it.eng.spagobi.utilities.MockFactory;
import it.eng.spagobi.utilities.MockHttpSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.naming.Context;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

public class UtilitiesForTest {

	public static void setUpMasterConfiguration() {
		System.setProperty("AF_CONFIG_FILE", "resources-test/master.xml");
		ConfigSingleton.setConfigurationCreation(new FileCreatorConfiguration("./"));

	}

	/**
	 * <pre>
	 *  <Environment name="service_url" type="java.lang.String" value="http://localhost:8080/knowage"/>
	 *     <Environment name="host_url" type="java.lang.String" value="http://localhost:8080"/>
	 *     <Environment name="sso_class" type="java.lang.String" value="it.eng.spagobi.services.common.FakeSsoService"/>
	 * </pre>
	 *
	 * @throws Exception
	 */
	public static void setUpTestJNDI() throws Exception {

		// Create initial context
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockFactory.class.getName());

		Context ic = new MockContext();
		MockFactory.context = ic;
		ic.bind("java:/comp/env/service_url", "http://localhost:8080/knowage");
		ic.bind("java:/comp/env/host_url", "http://localhost:8080");
		ic.bind("java:/comp/env/sso_class", "it.eng.spagobi.services.common.FakeSsoService");

		ic.bind("java://comp/env/service_url", "http://localhost:8080/knowage");
		ic.bind("java://comp/env/host_url", "http://localhost:8080");
		ic.bind("java://comp/env/sso_class", "it.eng.spagobi.services.common.FakeSsoService");
		ic.bind("java:/comp/env/jdbc/hdfs_url", "hdfs://192.168.56.1:8020");

		SimpleSingletonConfigCache cache = new SimpleSingletonConfigCache();
		cache.setProperty("SPAGOBI.ORGANIZATIONAL-UNIT.hdfsResource", "java:/comp/env/jdbc/hdfs_url");
		cache.setProperty("SPAGOBI_SSO.INTEGRATION_CLASS_JNDI", "java:/comp/env/sso_class");
		SingletonConfig.getInstance().setCache(cache);
	}

	public static void writeSessionOfWebApp() throws IOException, InterruptedException {
		Runtime runtime = Runtime.getRuntime();
		// call login page of knowage to write JSESSION COOKIE
		Process exec = runtime
				.exec("curl http://localhost:8080/knowage/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE -H 'Host: localhost:8080' -c ./resources-test/cookies.txt");
		exec.waitFor();
		String jsessionId = getJSessionId();
		exec = runtime
				.exec("curl http://localhost:8080/knowage/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE -H 'Host: localhost:8080' -H 'Cookie: JSESSIONID=%J' --data 'isInternalSecurity=true&userID=biadmin&password=biadmin'"
						.replace("%J", jsessionId));
		exec.waitFor();
	}

	@SuppressWarnings("resource")
	private static String getJSessionId() throws IOException {
		List<String> lines = FileUtils.readLines(new File("./resources-test/cookies.txt"));
		for (String line : lines) {
			Scanner sc = new Scanner(line);
			while (sc.hasNext()) {
				String token = sc.next();
				if (token.equals("JSESSIONID")) {
					// like JSESSIONID 90F5F5BCE69385898E042C50BCFEB9D0
					return sc.next();
				}
			}
		}
		Assert.fail();
		return null;
	}

	public static HttpSession getSession() throws FileNotFoundException, IOException {
		String filename = getAthenaTrunk() + "/knowage/resources-test/session.jbin";
		FileInputStream inputFileStream = new FileInputStream(filename);
		ObjectInputStream objectInputStream = new ObjectInputStream(inputFileStream);
		HttpSession session = new MockHttpSession();
		while (true) {
			try {
				String name = (String) objectInputStream.readObject();
				Object value = objectInputStream.readObject();
				session.setAttribute(name, value);
			} catch (Exception e) {
				if (e instanceof ClassNotFoundException) {
					// ignore classes not in knowage utils, not necessary for tests
					continue;
				}
				break;
			}
		}
		objectInputStream.close();
		return session;
	}

	private static String getAthenaTrunk() throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileReader("./resources-test/test.properties"));
		String res = props.getProperty("athena.trunk");
		return res;
	}

}
