/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.bo;

import junit.framework.Assert;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class CronExpression {
	String expression;
	
	public CronExpression() {
		setExpression("single{}");
	}
	public CronExpression(String expression) {
		setExpression(expression);
	}
	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		Assert.assertNotNull("Input parameter [expression] cannot be null");
		this.expression = expression;
	}
	
	public boolean isSimpleExpression() {
		return "single{}".equalsIgnoreCase(expression);
	}
	
	@Override
	public String toString() {
		return "CronExpression [expression=" + expression + "]";
	}
	
	
}
