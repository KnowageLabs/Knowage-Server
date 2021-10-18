/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.spagobi.behaviouralmodel.lov.bo.example;

import java.util.List;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.lov.bo.IJavaClassLov;

/**
 * @author albnale
 *
 */
public class JavaClassLovExample implements IJavaClassLov {

	@Override
	public String getValues(IEngUserProfile profile) {
		return "<ROWS> <ROW column1=\"1\" column2=\"a\" />\r\n<ROW column1=\"2\" column2=\"b\" />\r\n</ROWS>";
	}

	@Override
	public List getNamesOfProfileAttributeRequired() {
		return null;
	}

}
