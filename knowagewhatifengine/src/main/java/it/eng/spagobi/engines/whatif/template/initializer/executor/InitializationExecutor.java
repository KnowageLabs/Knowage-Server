/**
 *
 */
package it.eng.spagobi.engines.whatif.template.initializer.executor;

import java.util.ArrayList;
import java.util.List;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.initializer.Initializer;
import it.eng.spagobi.engines.whatif.template.initializer.impl.AliasesInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.CalculatedFieldsInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.CrossNavigationInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.MdxQueryInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.MondrianMdxQueryInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.PaginationInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.ParametersInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.ProfilingUserAttributesInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.ScenarioInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.StandAloneInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.TargetsClickableInitializerImpl;
import it.eng.spagobi.engines.whatif.template.initializer.impl.ToolbarInitializerImpl;

/**
 * @author Dragan Pirkovic
 *
 */
public class InitializationExecutor implements Initializer {

	private final List<Initializer> initializers;

	/**
	 *
	 */
	public InitializationExecutor() {
		initializers = new ArrayList<>();
		initializers.add(new MdxQueryInitializerImpl());
		initializers.add(new TargetsClickableInitializerImpl());
		initializers.add(new MondrianMdxQueryInitializerImpl());
		initializers.add(new ScenarioInitializerImpl());
		initializers.add(new AliasesInitializerImpl());
		initializers.add(new ToolbarInitializerImpl());
		initializers.add(new CalculatedFieldsInitializerImpl());
		initializers.add(new CrossNavigationInitializerImpl());
		initializers.add(new StandAloneInitializerImpl());
		initializers.add(new PaginationInitializerImpl());
		initializers.add(new ParametersInitializerImpl());
		initializers.add(new ProfilingUserAttributesInitializerImpl());
	}

	@Override
	public void init(SourceBean template, WhatIfTemplate toReturn) {
		for (Initializer initializer : initializers) {
			initializer.init(template, toReturn);
		}
	}

}
