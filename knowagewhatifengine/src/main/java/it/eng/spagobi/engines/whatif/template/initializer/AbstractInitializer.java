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
package it.eng.spagobi.engines.whatif.template.initializer;

/**
 * @author Dragan Pirkovic
 *
 */
public abstract class AbstractInitializer implements Initializer {

	public static final String TAG_MDX_QUERY = "MDXquery";
	public static final String TAG_SCENARIO = "SCENARIO";
	public static final String PROP_VALUE = "value";
	public static final String PROP_TYPE = "type";
	public static final String PROP_NAME = "name";
	public static final String TRUE = "true";
	public static final String PROP_PARAMETER_NAME = "name";
	public static final String TAG_TN_PARAMETERS = "PARAMETERS";
	public static final String TAG_TN_PARAMETER = "PARAMETER";

}
