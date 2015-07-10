/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.template;

import it.eng.qbe.model.accessmodality.AbstractModelAccessModality;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;

/**
 * The Class DataMartModelAccessModality.
 *
 * @author Andrea Gioia
 */
public class QbeXMLModelAccessModality extends AbstractModelAccessModality {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(QbeXMLModelAccessModality.class);

	public QbeXMLModelAccessModality() {
		Assert.assertUnreachable("This filter is no more supported");
	}

	public QbeXMLModelAccessModality(SourceBean modalitySB) {
		Assert.assertUnreachable("This filter is no more supported");
	}

}
