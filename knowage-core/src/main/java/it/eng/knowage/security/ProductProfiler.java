package it.eng.knowage.security;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.config.bo.Engine;

public class ProductProfiler {

	static private Logger logger = Logger.getLogger(ProductProfiler.class);

	private static boolean isCommunity;
	private static Class licenseManager, productProfilerEE;

	static {
		try {
			licenseManager = Class.forName("it.eng.knowage.tools.servermanager.utils.LicenseManager");
			isCommunity = false;
		} catch (ClassNotFoundException e) {
			isCommunity = true;
		}

		if (!isCommunity) {
			try {
				productProfilerEE = Class.forName("it.eng.knowage.enterprise.security.ProductProfiler");
			} catch (Exception e) {
				logger.error("Cannot instantiate product profiler: ", e);
			}
		}
	}

	public static List filterDocumentTypesByProduct(List types) {
		if (isCommunity) {
			return types;
		} else {
			List filteredTypes = new ArrayList();
			try {
				Method filterDocumentTypesByProductMethod = productProfilerEE.getMethod("filterDocumentTypesByProduct", List.class);
				filteredTypes = (List) filterDocumentTypesByProductMethod.invoke(productProfilerEE, types);
			} catch (Exception e) {
				logger.error("Error while filtering document types by product: ", e);
			}
			return filteredTypes;
		}
	}

	public static List<Engine> filterEnginesByProduct(List<Engine> engines) {
		if (isCommunity) {
			return engines;
		} else {
			List<Engine> filteredEngines = new ArrayList<Engine>();
			try {
				Method filterEnginesByProductMethod = productProfilerEE.getMethod("filterEnginesByProduct", List.class);
				filteredEngines = (List<Engine>) filterEnginesByProductMethod.invoke(productProfilerEE, engines);
			} catch (Exception e) {
				logger.error("Error while filtering engines by product: ", e);
			}
			return filteredEngines;
		}
	}

	public static boolean canExecuteDocument(BIObject biObj) {
		if (isCommunity) {
			return true;
		} else {
			boolean toReturn = false;
			try {
				Method canExecuteDocumentMethod = productProfilerEE.getMethod("canExecuteDocument", BIObject.class);
				toReturn = (boolean) canExecuteDocumentMethod.invoke(productProfilerEE, biObj);
			} catch (Exception e) {
				logger.error("Error while filtering engines by product: ", e);
			}
			return toReturn;
		}
	}

	public static boolean canCreateDataset(String type, UserProfile profile) {
		if (isCommunity) {
			return true;
		} else {
			boolean toReturn = false;
			try {
				Method canCreateDatasetMethod = productProfilerEE.getMethod("canCreateDataset", String.class, UserProfile.class);
				toReturn = (boolean) canCreateDatasetMethod.invoke(productProfilerEE, type, profile);
			} catch (Exception e) {
				logger.error("Error while filtering datasets by product: ", e);
			}
			return toReturn;
		}
	}

	public static Set<String> filterAuthorizationsByProduct(List<String> authorizations) {
		if (isCommunity) {
			return new HashSet<String>(authorizations);
		} else {
			Set<String> filteredAuthorizations = new HashSet<String>();
			try {
				Method filterAuthorizationsByProductMethod = productProfilerEE.getMethod("filterAuthorizationsByProduct", List.class);
				filteredAuthorizations = (Set<String>) filterAuthorizationsByProductMethod.invoke(productProfilerEE, authorizations);
			} catch (Exception e) {
				logger.error("Error while filtering authorizations by product: ", e);
			}
			return filteredAuthorizations;
		}
	}

	public static boolean canCreateWidget(String type) {
		if (isCommunity) {
			return true;
		} else {
			boolean toReturn = false;
			try {
				Method canCreateWidgetMethod = productProfilerEE.getMethod("canCreateWidget", String.class);
				toReturn = (boolean) canCreateWidgetMethod.invoke(productProfilerEE, type);
			} catch (Exception e) {
				logger.error("Error while filtering widgets by product: ", e);
			}
			return toReturn;
		}
	}

	public static boolean canUseFunctions() {
		if (isCommunity) {
			return true;
		} else {
			boolean toReturn = false;
			try {
				Method canUseFunctionsMethod = productProfilerEE.getMethod("canUseFunctions");
				toReturn = (boolean) canUseFunctionsMethod.invoke(productProfilerEE);
			} catch (Exception e) {
				logger.error("Error while profiling catalog functions usage by product: ", e);
			}
			return toReturn;
		}
	}

}
