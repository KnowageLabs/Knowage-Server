/**
 *
 */
package org.hibernate.dialect;

import org.apache.log4j.Logger;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import it.eng.qbe.datasource.configuration.dao.fileimpl.InLineFunctionsDAOFileImpl.InLineFunction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

/**
 * @author Dragan Pirkovic
 *
 */
public class FunctionTemplateRegistrator implements IFunctionRegistrator {

	static protected Logger logger = Logger.getLogger(FunctionTemplateRegistrator.class);

	private final InLineFunction function;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.hibernate.dialect.function.registration.IFunctionRegistrator#register()
	 */

	/**
	 * @param function
	 */
	public FunctionTemplateRegistrator(InLineFunction function) {
		this.function = function;
	}

	@Override
	public void register(Dialect dialect) {
		logger.debug("IN");
		try {
			if (function.getCode() != null || !function.getCode().equals("")) {
				dialect.registerFunction(function.getName(),
						new SQLFunctionTemplate((Type) StandardBasicTypes.class.getField(function.getType()).get(null), function.getCode()));
			}

		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.error("Error while registering function", e);
			throw new SpagoBIEngineRuntimeException("Error while registering function", e);
		} finally {
			logger.debug("OUT");
		}

	}

}
