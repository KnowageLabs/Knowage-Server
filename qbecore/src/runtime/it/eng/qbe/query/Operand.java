/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public abstract class Operand {
	
	public String[] values; // values may be more than 1 (the right operand with IN, NOT IN, BEETWEN and NOT BEETWEN operators)
	public String description;
	public String type;
	public String[] defaulttValues;
	public String[] lastValues;
	public String alias;
	
	public Operand(String[] values,
			String description,
			String type,
			String[] defaulttValues,
			String[] lastValues,
			String alias) {
		this.values = values;
		this.description = description;
		this.type = type;
		this.defaulttValues = defaulttValues;
		this.lastValues = lastValues;
		this.alias = alias;
	}
	
}
