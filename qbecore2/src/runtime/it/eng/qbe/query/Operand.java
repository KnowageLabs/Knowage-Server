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
