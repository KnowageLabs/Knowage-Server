/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.bean;

import it.eng.spagobi.engines.network.serializer.SerializationException;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public interface INetwork {
	public String getNetworkAsString() throws SerializationException;
	public String getNetworkType();
	public String getNetworkInfo();
	public String getNetworkCrossNavigation() throws SerializationException;
	public String getNetworkOptions();
}
