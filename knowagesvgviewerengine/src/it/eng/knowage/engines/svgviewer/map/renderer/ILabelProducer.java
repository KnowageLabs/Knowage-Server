package it.eng.knowage.engines.svgviewer.map.renderer;

import it.eng.spago.base.SourceBean;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface LabelProducer.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface ILabelProducer {

	/**
	 * Inits the.
	 *
	 * @param conf
	 *            the conf
	 */
	public void init(SourceBean conf);

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel();

	public Map getSettings();
}
