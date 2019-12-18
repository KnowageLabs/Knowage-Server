/**
 *
 */
package org.hibernate.dialect;

import it.eng.qbe.datasource.configuration.dao.fileimpl.InLineFunctionsDAOFileImpl.InLineFunction;

/**
 * @author Dragan Pirkovic
 *
 */
public class FunctionRegistratorFactory {

	public static IFunctionRegistrator getFunctionRegistrator(InLineFunction function) {
		return new FunctionTemplateRegistrator(function);
	}

}
