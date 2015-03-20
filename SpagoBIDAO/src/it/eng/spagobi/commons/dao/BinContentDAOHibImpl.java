/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 22-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.commons.metadata.SbiBinContents;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class BinContentDAOHibImpl extends AbstractHibernateDAO implements IBinContentDAO {

    private static transient Logger logger = Logger.getLogger(BinContentDAOHibImpl.class);

    /*
     * (non-Javadoc)
     * 
     * @see it.eng.spagobi.commons.dao.IBinContentDAO#getBinContent(java.lang.Integer)
     */
    public byte[] getBinContent(Integer binId) throws EMFInternalError {
	logger.debug("IN");
	if (binId != null)
	    logger.debug("binId=" + binId.toString());
	byte[] content = new byte[0];
	Session aSession = null;
	Transaction tx = null;
	try {
	    aSession = getSession();
	    tx = aSession.beginTransaction();
	    SbiBinContents hibBinCont = (SbiBinContents) aSession.load(SbiBinContents.class, binId);
	    content = hibBinCont.getContent();
	    tx.commit();
	} catch (HibernateException he) {
	    logger.error("HibernateException",he);
	    if (tx != null)
		tx.rollback();
	    throw new EMFInternalError(EMFErrorSeverity.ERROR, "100");
	} finally {
	    if (aSession != null) {
		if (aSession.isOpen())
		    aSession.close();
	    }
	    logger.debug("OUT");
	}
	return content;
    }

}
