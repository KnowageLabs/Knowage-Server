package it.eng.spagobi.commons.installation.configuration;

import it.eng.knowage.tools.servermanager.utils.LicenseSingleton;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.json.JSONException;
import org.json.JSONObject;

import com.license4j.License;

public class ConfigManagementUtil {

	public static void createGeneralConfig(String pathToSave, ServletContext context) {
		JSONObject config = new JSONObject();
		PrintWriter writer = null;

		try {
			config.put("date", new Date().toString());
			config.put("jvm_version", getJavaVersion());
			config.put("application_server", getAppServerInfo(context));
			config.put("environment_variables", getEnvironmentVariables());
			config.put("jndi_variables", getJndiVariables());
			config.put("license", getLicense());
			// config.put("chacksum_jar", getLibrariesChecksum());
			File generalConfig = new File(pathToSave + File.separator + "config.txt");
			writer = new PrintWriter(new FileWriter(generalConfig));
			writer.write(config.toString());

		} catch (JSONException e) {

		} catch (IOException e) {

		} finally {

			writer.close();

		}

	}

	public static List tablesNeeded() {
		String[] tablesRequired = { "SBI_CONFIG", "SBI_DOMAINS", "SBI_ENGINES", "SBI_PRODUCT_TYPE", "SBI_AUTHORIZATIONS", "SBI_USER_FUNC",
				"SBI_ROLE_TYPE_USER_FUNC", "SBI_PRODUCT_TYPE_ENGINE", "SBI_ORGANIZATION_PRODUCT_TYPE", "SBI_ORGANIZATIONS" };
		ArrayList<String> tables = new ArrayList<>();
		for (String tableName : tablesRequired) {
			tables.add(tableName);
		}

		return tables;

	}

	public static void convertResultSetToCSV(ResultSet rs, String name, String pathToSave) throws FileNotFoundException, SQLException {

		PrintWriter csvWriter = new PrintWriter(new File(pathToSave + File.separator + name + ".csv"));
		ResultSetMetaData meta = rs.getMetaData();
		int numberOfColumns = meta.getColumnCount();
		String dataHeaders = "\"" + meta.getColumnName(1) + "\"";
		for (int i = 2; i < numberOfColumns + 1; i++) {
			dataHeaders += ",\"" + meta.getColumnName(i) + "\"";
		}
		csvWriter.println(dataHeaders);
		while (rs.next()) {
			String row = "\"" + rs.getString(1) + "\"";
			for (int i = 2; i < numberOfColumns + 1; i++) {
				row += ",\"" + rs.getString(i) + "\"";
			}
			csvWriter.println(row);
		}
		csvWriter.close();
	}

	public static String getJavaVersion() {
		return System.getProperty("java.version");
	}

	public static Map getEnvironmentVariables() {
		return System.getenv();
	}

	public static void getLibCongigurationFiles(String pathToSave) throws IOException {
		URL providerURL = AccessController.doPrivileged(new PrivilegedAction<URL>() {
			@Override
			public URL run() {
				CodeSource cs = ConfigManagementUtil.class.getProtectionDomain().getCodeSource();
				return cs.getLocation();
			}
		});
		File hibCfgSrc = new File(providerURL.getPath() + File.separator + "hibernate.cfg.xml");
		File hibCfgDst = new File(pathToSave + File.separator + "hibernate.cfg.xml");
		copyFileUsingStream(hibCfgSrc, hibCfgDst);
		File quartzSrc = new File(providerURL.getPath() + File.separator + "quartz.properties");
		File quartzDst = new File(pathToSave + File.separator + "quartz.properties");
		copyFileUsingStream(quartzSrc, quartzDst);
		File hazSrc = new File(providerURL.getPath() + File.separator + "hazelcast.xml");
		File hazDst = new File(pathToSave + File.separator + "hazelcast.xml");
		copyFileUsingStream(hazSrc, hazDst);
		File ehcacheSrc = new File(providerURL.getPath() + File.separator + "ehcache.xml");
		File ehcacheDst = new File(pathToSave + File.separator + "ehcache.xml");
		copyFileUsingStream(ehcacheSrc, ehcacheDst);

		File knowageWebSrc = new File(providerURL.getPath() + File.separator + ".." + File.separator + "web.xml");
		File knowageWebDst = new File(pathToSave + File.separator + "knowage_web.xml");
		copyFileUsingStream(knowageWebSrc, knowageWebDst);
		File manifestSrc = new File(providerURL.getPath() + File.separator + ".." + File.separator + ".." + File.separator + "META-INF" + File.separator
				+ "MANIFEST.MF");
		File manifestDst = new File(pathToSave + File.separator + "MANIFEST.MF");
		copyFileUsingStream(manifestSrc, manifestDst);

	}

	public static String getAppServerInfo(ServletContext context) {
		return context.getServerInfo();
	}

	public static Map getJndiVariables() {
		Map<String, String> variables = new HashMap<String, String>();
		variables.put("resource_path", SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME")));
		variables.put("sso_class", SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.INTEGRATION_CLASS_JNDI")));
		variables.put("host_url", SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue("SPAGOBI.SPAGOBI_HOST_JNDI")));
		variables.put("service_url", SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue("SPAGOBI.SPAGOBI_SERVICE_JNDI")));

		return variables;
	}

	public static Map getLicense() {
		Map<String, String> licenseRet = new HashMap<>();
		Map<String, License> licenses = LicenseSingleton.getInstance().getLicenses();
		// return licenses;
		// Set<String> productTypeNames = licenses.keySet();
		// for (String productTypeName : productTypeNames) {
		// License license = licenses.get(productTypeName);
		// licenseRet.put(productTypeName,
		// license.getLicenseText().getLicenseValidProductID());
		//
		// }
		for (Map.Entry<String, License> entry : licenses.entrySet()) {
			licenseRet.put(entry.getKey(), entry.getValue().getLicenseString());
		}
		// Iterator it = licenses.keySet().iterator();
		// while (it.hasNext()) {
		//
		// Map.Entry le = (Entry) it.next();
		// License l = (License) le.getValue();
		// licenseRet.put(l.getLicenseText().getLicenseProductName().toString(),
		// l.getLicenseText().getLicenseValidProductID().toString());
		// }
		return licenseRet;
	}

	// public static Map getLibrariesChecksum() {
	// Map<String, String> checksum = new HashMap<>();
	// CodeSource cs =
	// ConfigManagementUtil.class.getProtectionDomain().getCodeSource();
	//
	// checksum.put("knowage.core", cs.getCertificates().toString());
	// CodeSource cs1 =
	// AbstractHibernateDAO.class.getProtectionDomain().getCodeSource();
	// checksum.put("knowage.dao", cs1.getCertificates().toString());
	// return checksum;
	// }

	private static void copyFileUsingStream(File source, File dest) throws IOException {

		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	public static boolean removeDirectory(File directory) {

		// System.out.println("removeDirectory " + directory);

		if (directory == null)
			return false;
		if (!directory.exists())
			return true;
		if (!directory.isDirectory())
			return false;

		String[] list = directory.list();

		// Some JVMs return null for File.list() when the
		// directory is empty.
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File entry = new File(directory, list[i]);

				// System.out.println("\tremoving entry " + entry);

				if (entry.isDirectory()) {
					if (!removeDirectory(entry))
						return false;
				} else {
					if (!entry.delete())
						return false;
				}
			}
		}

		return directory.delete();
	}

}
