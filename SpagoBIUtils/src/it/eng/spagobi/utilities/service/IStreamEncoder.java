/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface IStreamEncoder {
	public void encode(InputStream inputStream,	OutputStream outputStream) throws IOException;
}
