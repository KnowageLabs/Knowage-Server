/**
 *
 */
package it.eng.knowage.document.export.cockpit;

/**
 * @author Dragan Pirkovic
 *
 */
public interface IConverter<R, P> {

	/**
	 * @param input
	 * @return
	 */
	R convert(P input);

}
