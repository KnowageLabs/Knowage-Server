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

package it.eng.spagobi.utilities.exceptions;

import java.util.ArrayList;
import java.util.List;

public class SpagoBIException extends Exception {

	/*
	 * I18N code for user oriented description of the exception. It is usually prompted to the user. Instead the message passed to the constructor is developer
	 * oriented and it should be just logged.
	 */
	private String i18nCode;

	/*
	 * A list of possible solutions to the problem that have caused the exception
	 */
	private List<String> hints;

	/**
	 * Builds a <code>SpagoBIException</code>.
	 *
	 * @param message Text of the exception
	 */
	public SpagoBIException(String message) {
		super(message);
	}

	public SpagoBIException(String message, String i18nCode) {
		super(message);
		this.setI18NCode(i18nCode);
	}

	public SpagoBIException(String message, Throwable ex) {
		super(message, ex);
	}

	public SpagoBIException(String message, String i18nCode, Throwable ex) {
		super(message, ex);
		this.setI18NCode(i18nCode);
	}

	public String getI18NCode() {
		return i18nCode;
	}

	public void setI18NCode(String i18nCode) {
		this.i18nCode = i18nCode;
	}

	public List<String> getHints() {
		if (hints == null) {
			hints = new ArrayList<String>();
		}
		return hints;
	}

	public void addHint(String hint) {
		getHints().add(hint);
	}

}
