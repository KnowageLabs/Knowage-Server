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

package it.eng.spagobi.tools.dataset.metasql.query.item;

import java.util.HashMap;
import java.util.Map;

import it.eng.spagobi.utilities.assertion.Assert;

public enum SimpleFilterOperator {
	DIFFERENT_FROM("!=", 1, false), EQUALS_TO("=", 1, false), GREATER_THAN(">", 1, false), GREATER_THAN_OR_EQUAL(">=", 1, false), IN("IN", -1,
			false), LESS_THAN("<", 1, false), LESS_THAN_OR_EQUAL("<=", 1, false), LIKE("LIKE", 1, false), NOT_IN("NOT IN", -1, false), NOT_LIKE("NOT LIKE", 1,
					false), IS_NULL("IS NULL", 0, false), IS_NOT_NULL("IS NOT NULL", 0, false), EQUALS_TO_MIN("MIN", 0,
							true), EQUALS_TO_MAX("MAX", 0, true), BETWEEN("RANGE", 2, false), NOT_BETWEEN("NOT RANGE", 2, false);

	private static Map<String, SimpleFilterOperator> symbolToOperator = new HashMap<>();
	static {
		for (SimpleFilterOperator operator : SimpleFilterOperator.values()) {
			symbolToOperator.put(operator.symbol, operator);
		}
	}

	public static SimpleFilterOperator ofSymbol(String symbol) {
		return symbolToOperator.get(symbol);
	}

	private String symbol;
	private int arity;
	private boolean placeHolder;

	private SimpleFilterOperator(String symbol, int arity, boolean markup) {
		Assert.assertTrue(-1 <= arity && arity <= 2, arity + " operands are not supported");

		this.symbol = symbol;
		this.arity = arity;
		this.placeHolder = markup;
	}

	public int getArity() {
		return arity;
	}

	public boolean isPlaceholder() {
		return placeHolder;
	}

	public boolean isNullary() {
		return arity == 0;
	}

	public boolean isUnary() {
		return arity == 1;
	}

	public boolean isBinary() {
		return arity == 2;
	}

	public boolean isNary() {
		return arity == -1;
	}

	@Override
	public String toString() {
		return this.symbol;
	}

}