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
package it.eng.spagobi.api.v2.documentdetails.subresources;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/")
public class SubreportsDocumentResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(SubreportsDocumentResource.class);

	@SuppressWarnings("unchecked")
	@GET
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public List<Subreport> getSubreports(@PathParam("id") Integer id) {
		logger.debug("IN");
		ISubreportDAO subRaportDAO = null;
		List<Subreport> subreports = null;
		try {
			subRaportDAO = DAOFactory.getSubreportDAO();
			subreports = subRaportDAO.loadSubreportsByMasterRptId(id);
			Assert.assertNotNull(subreports, "Subreports can not be null");
		} catch (EMFUserError e) {
			logger.debug("Getting subreport has failed", e);
			throw new SpagoBIRestServiceException("Getting subreport has failed", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return subreports;
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public Subreport addSubReport(@PathParam("id") Integer id, BIObject document) {
		logger.debug("IN");
		Assert.assertNotNull(document, "Subreport can not be null");
		ISubreportDAO subReportDAO = null;
		Subreport subreport = null;
		try {
			subReportDAO = DAOFactory.getSubreportDAO();
			subreport = new Subreport();
			subreport.setMaster_rpt_id(id);
			subreport.setSub_rpt_id(document.getId());
			subReportDAO.insertSubreport(subreport);
		} catch (EMFUserError e) {
			logger.debug("Adding subreport has failed", e);
			throw new SpagoBIRestServiceException("Adding subreport has failed", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return subreport;
	}

	@DELETE
	@Path("{subreportId}")
	@Produces("application/json")
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public Subreport deleteSubReport(@PathParam("id") Integer id, @PathParam("subreportId") Integer subreportId) {
		logger.debug("IN");
		Assert.assertNotNull(subreportId, "SubreportID can not be null");
		ISubreportDAO subReportDAO = null;
		Subreport subreport = null;
		try {
			subReportDAO = DAOFactory.getSubreportDAO();
			subReportDAO.eraseSubreportBySubRptId(subreportId);
		} catch (EMFUserError e) {
			logger.debug("Documents objects can not be provided", e);
			throw new SpagoBIRestServiceException("Erasing subreport has failed", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return subreport;
	}

}
