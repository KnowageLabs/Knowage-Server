package it.eng.knowage.engines.svgviewer.map.renderer;

import it.eng.knowage.engines.svgviewer.map.renderer.configurator.AbstractMapRendererConfigurator;
import it.eng.spago.base.SourceBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractLabelProducer implements ILabelProducer {

	Map settings;

	@Override
	public void init(SourceBean conf) {
		if (settings == null) {
			settings = new HashMap();
		}

		List params = conf.getAttributeAsList("PARAM");
		AbstractMapRendererConfigurator.addSettings(getSettings(), params);
	}

	@Override
	public Map getSettings() {
		return settings;
	}

	public void setSettings(Map settings) {
		this.settings = settings;
	}
}
