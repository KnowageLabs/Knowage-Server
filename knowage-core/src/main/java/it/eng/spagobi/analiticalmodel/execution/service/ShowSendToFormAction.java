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
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;

import org.apache.log4j.Logger;

/**
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class ShowSendToFormAction extends AbstractHttpAction {

	private static final long serialVersionUID = 1L;
	
	private static transient Logger logger = Logger.getLogger(ShowSendToFormAction.class);

    /* (non-Javadoc)
     * @see it.eng.spagobi.commons.services.BaseProfileAction#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
     */
    public void service(SourceBean request, SourceBean responseSb) throws Exception {
    	logger.debug("IN");
    	logger.debug("OUT");
    }

}
