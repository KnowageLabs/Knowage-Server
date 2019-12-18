/**
 *
 */
package org.hibernate.dialect;

import org.hibernate.dialect.Dialect;

/**
 * @author Dragan Pirkovic
 *
 */
public interface IFunctionRegistrator {

	public void register(Dialect dialect);

}
