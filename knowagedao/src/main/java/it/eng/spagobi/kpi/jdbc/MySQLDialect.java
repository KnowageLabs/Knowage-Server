/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.kpi.jdbc;

public class MySQLDialect implements ISQLDialect {

	@Override
	public String getCastingToFloatFormula(Number number) {

		/*
		 * In standard SQL, the syntax DECIMAL(M) is equivalent to DECIMAL(M,0). So, If we don't specify precision, decimals are lost.
		 */

		Integer scale = null;
		Integer precision = null;
		if (number instanceof Double || number instanceof Float) {
			int pos = number.toString().indexOf('.');
			precision = number.toString().length() - (pos + 1);
			int integerPartLength = number.toString().length() - (precision + 1);
			scale = integerPartLength + precision;
		}

		if (scale != null && precision != null) {
			return "CAST(" + number + " as decimal(" + scale + "," + precision + "))";
		} else {
			return "CAST(" + number + " as decimal)";
		}

	}

}
