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
package it.eng.knowage.meta.model.util;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;

/** Contains utility methods to create safe {@code JXPathContext} objects. */
public class JXPathContextBuilder {

	private JXPathContext context;

	private JXPathContextBuilder(Object contextBean) {
		context = newSafeContext(contextBean);
	}

	public static JXPathContextBuilder newInstance(Object contextBean) {
		return new JXPathContextBuilder(contextBean);
	}

	public JXPathContextBuilder withFactory(AbstractFactory factory) {
		context.setFactory(factory);
		return this;
	}

	/**
	 * Creates a {@code JXPathContext} that disables calling Java methods from XPath expressions.
	 *
	 * @param contextBean the root node object
	 * @return the context
	 */
	public JXPathContext newSafeContext(Object contextBean) {
		JXPathContext safeContext = JXPathContext.newContext(contextBean);
		// Set empty function library to prevent calling functions
		safeContext.setFunctions(new FunctionLibrary());
		return safeContext;
	}

	public JXPathContext build() {
		return context;
	}

}