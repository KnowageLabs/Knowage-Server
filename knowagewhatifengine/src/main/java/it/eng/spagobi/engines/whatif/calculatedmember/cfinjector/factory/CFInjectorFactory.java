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

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.olap4j.mdx.CallNode;

import it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.CFInjector;
import it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.impl.BracesInjector;
import it.eng.spagobi.engines.whatif.calculatedmember.cfinjector.impl.FunctionInjector;

/**
 * @author Dragan Pirkovic
 *
 */
public enum CFInjectorFactory {
	Braces(BracesInjector.class), Function(FunctionInjector.class);
	Class<? extends CFInjector> clazz;

	private CFInjectorFactory(Class<? extends CFInjector> clazz) {
		this.clazz = clazz;
	}

	public static transient Logger logger = Logger.getLogger(FunctionInjector.class);

	public static CFInjector getCFInjector(String name, CallNode callNode) {

		if (fromString(name) != null)
			try {
				return fromString(name).clazz.getConstructor(new Class[] { CallNode.class }).newInstance(new Object[] { callNode });
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				logger.error("Error while creating instance of CFInjector", e);
			}
		return null;

	}

	private static CFInjectorFactory fromString(String text) {
		for (CFInjectorFactory b : CFInjectorFactory.values()) {
			if (b.name().equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
