/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.knowage.api.dossier;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.massiveExport.dao.IProgressThreadDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class ProgressThreadManager {

    private IProgressThreadDAO progressThreadDAO;
    private static transient Logger logger = Logger.getLogger(ProgressThreadManager.class);

    public ProgressThreadManager() {
        createDao();
    }

    public void setStatusStarted(Integer progressThreadId) {
        try {
            progressThreadDAO.setStartedProgressThread(progressThreadId);
        } catch (EMFUserError e) {
            logger.error("Error setting status STARTED in thread with the progress id " + progressThreadId, e);
            throw new SpagoBIRuntimeException("Error setting status STARTED in thread with the progress id " + progressThreadId, e);
        }
    }

    public void incrementPartial(Integer progressThreadId) {
        try {
            progressThreadDAO.incrementProgressThread(progressThreadId);
            logger.debug("progress Id incremented");
        } catch (EMFUserError e) {
            logger.error("Error incrementing partial in thread with the progress id " + progressThreadId, e);
            throw new SpagoBIRuntimeException("Error incrementing partial in thread with the progress id " + progressThreadId, e);
        }

    }

    public void setStatusDownload(Integer progressThreadId) {
        try {
            progressThreadDAO.setDownloadProgressThread(progressThreadId);
            logger.debug("Thread row in database set as download state");
        } catch (EMFUserError e) {
            logger.error("Error setting status DOWNLOAD in thread with the progress id " + progressThreadId, e);
            throw new SpagoBIRuntimeException("Error setting status DOWNLOAD in thread with the progress id " + progressThreadId, e);
        }

    }

    public void setStatusError(Integer progressThreadId) {
        try {
            progressThreadDAO.setErrorProgressThread(progressThreadId);
        } catch (EMFUserError e) {
            logger.error("Error setting status ERROR in thread with the progress id " + progressThreadId, e);
            throw new SpagoBIRuntimeException("Error setting status ERROR in thread with the progress id " + progressThreadId, e);
        }
    }

    public void deleteThread(Integer progressThreadId) {
        logger.debug("IN");
        try {
            progressThreadDAO.deleteProgressThread(progressThreadId);
        } catch (EMFUserError e) {
            logger.error("Error in deleting the row with the progress id " + progressThreadId, e);
            throw new SpagoBIRuntimeException("Error in deleting the row with the progress id " + progressThreadId, e);
        }
        logger.debug("OUT");
    }

    private void createDao() {
        progressThreadDAO = DAOFactory.getProgressThreadDAO();
    }

}
