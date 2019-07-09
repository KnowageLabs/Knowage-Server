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
package it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.factory;

import org.apache.log4j.Logger;
import org.olap4j.mdx.CallNode;

import it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.CFInjector;
import it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.impl.UnionCFInjector;

/**
 * @author Dragan Pirkovic
 *
 */
public enum CFInjectionFuntionFactory {

	Union(UnionCFInjector.class);
	Class<? extends CFInjector> clazz;

	private CFInjectionFuntionFactory(Class<? extends CFInjector> clazz) {
		this.clazz = clazz;
	}

	public static transient Logger logger = Logger.getLogger(CFInjectionFuntionFactory.class);

	public static CFInjector getCFFunctionInjector(String operatorName, CallNode rootNode) {
		try {
			if (fromString(operatorName) != null)
				return fromString(operatorName).clazz.getConstructor(new Class[] { CallNode.class }).newInstance(new Object[] { rootNode });
		} catch (Exception e) {
			logger.error("Error while creating instance of CFInjector", e);
		}

		return null;
	}

	private static CFInjectionFuntionFactory fromString(String text) {
		for (CFInjectionFuntionFactory b : CFInjectionFuntionFactory.values()) {
			if (b.name().equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
