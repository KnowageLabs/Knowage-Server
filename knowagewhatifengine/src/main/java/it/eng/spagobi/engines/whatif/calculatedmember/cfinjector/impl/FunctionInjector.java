/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.impl;

import org.olap4j.mdx.CallNode;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.metadata.Member;

import it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.AbstractInjector;
import it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.CFInjector;
import it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.factory.CFInjectionFuntionFactory;

/**
 * @author Dragan Pirkovic
 *
 */
public class FunctionInjector extends AbstractInjector {

	/**
	 * @param callNode
	 */
	public FunctionInjector(CallNode callNode) {
		super(callNode);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.engines.whatif.calculatedmember.CFInjector#injectField(org.olap4j.metadata.Member, org.olap4j.mdx.IdentifierNode)
	 */
	@Override
	public void injectField(Member parentMember, IdentifierNode calculatedNode) {

		CFInjector cfInjector = CFInjectionFuntionFactory.getCFFunctionInjector(rootNode.getOperatorName(), rootNode);
		if (cfInjector != null) {
			cfInjector.injectField(parentMember, calculatedNode);
		}

	}

}
