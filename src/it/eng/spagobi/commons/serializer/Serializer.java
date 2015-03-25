/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import java.util.Locale;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface Serializer {
	Object serialize(Object o, Locale locale) throws SerializationException;
	//void setProperty(String name, String value);
}
