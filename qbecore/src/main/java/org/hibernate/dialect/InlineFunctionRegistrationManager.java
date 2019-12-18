/**
 *
 */
package org.hibernate.dialect;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.configuration.dao.IInLineFunctionsDAO;
import it.eng.qbe.datasource.configuration.dao.fileimpl.InLineFunctionsDAOFileImpl;
import it.eng.qbe.datasource.configuration.dao.fileimpl.InLineFunctionsDAOFileImpl.InLineFunction;

/**
 * @author Dragan Pirkovic
 *
 */
public class InlineFunctionRegistrationManager {

	static protected Logger logger = Logger.getLogger(InlineFunctionRegistrationManager.class);

	public static void registerInlineFunctions(Dialect dialect) {
		logger.debug("IN");
		for (Entry<String, InLineFunction> function : getFunctionMap(dialect).entrySet()) {

			FunctionRegistratorFactory.getFunctionRegistrator(function.getValue()).register(dialect);
		}
		logger.debug("OUT");
	}

	/**
	 * @param dialect
	 * @return
	 */
	private static Map<String, InLineFunction> getFunctionMap(Dialect dialect) {
		IInLineFunctionsDAO dao = new InLineFunctionsDAOFileImpl();

		return dao.loadInLineFunctions(dialect.getClass().getName());
	}

}
