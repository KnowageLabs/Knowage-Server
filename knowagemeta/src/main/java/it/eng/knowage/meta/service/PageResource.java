/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.meta.service;

import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.serializer.EmfXmiSerializer;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IProductTypeDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @authors
 *
 */
@ManageAuthorization
@Path("/1.0/pages")
public class PageResource {

	static private Map<String, String> urls;

	static private Logger logger = Logger.getLogger(PageResource.class);

	{
		urls = new HashMap<String, String>();
		urls.put("edit", "/WEB-INF/jsp/metaWeb.jsp");
		urls.put("test", "/WEB-INF/jsp/test.jsp");
	}

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	@GET
	@Path("/{pagename}")
	@Produces("text/html")
	public void getPage(@PathParam("pagename") String pageName) {
		String dispatchUrl = urls.get(pageName);

		try {
			EngineStartServletIOManager ioManager = new EngineStartServletIOManager(request, response);
			UserProfile userProfile = ioManager.getUserProfile();
			if (userProfile == null) {
				String userId = request.getHeader("user");
				userProfile = (UserProfile) UserUtilities.getUserProfile(userId);
				ioManager.setUserProfile(userProfile);
			}

			// To deploy into JBOSSEAP64 is needed a StandardWrapper, instead of RestEasy Wrapper
//			HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
//			HttpServletResponse response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);
			ioManager.getHttpSession().setAttribute("ioManager", ioManager);
			ioManager.getHttpSession().setAttribute("userProfile", userProfile);

			// load product types
			IProductTypeDAO ptdao = DAOFactory.getProductTypeDAO();
			ptdao.setUserProfile(userProfile);
			ioManager.getHttpSession().setAttribute("productTypes", ptdao.loadCurrentTenantProductTypes());

			// load profile attributes
			ISbiAttributeDAO objDao = DAOFactory.getSbiAttributeDAO();
			objDao.setUserProfile(userProfile);
			List<SbiAttribute> attrList = objDao.loadSbiAttributes();
			List<String> attl = new ArrayList<>();
			for (SbiAttribute att : attrList) {
				attl.add(att.getAttributeName());
			}
			ioManager.getHttpSession().setAttribute("profileAttributes", attl);

			// load roles
			IRoleDAO rdao = DAOFactory.getRoleDAO();
			List<Role> rlist = rdao.loadAllRoles();
			List<String> rolL = new ArrayList<>();
			for (Role ro : rlist) {
				rolL.add(ro.getName());
			}
			ioManager.getHttpSession().setAttribute("avaiableRoles", rolL);

			// ----------------------load the sbiModel if present-----------------------------------------
			Integer bmId = Integer.parseInt(request.getParameter("bmId"));

			IMetaModelsDAO businessModelsDAO = DAOFactory.getMetaModelsDAO();
			businessModelsDAO.setUserProfile(userProfile);

			Content lastFileModelContent = businessModelsDAO.loadActiveMetaModelContentById(bmId);
			InputStream is = null;
			if (lastFileModelContent != null) {
				byte[] sbiModel = lastFileModelContent.getFileModel();
				if (sbiModel != null) {
					is = new ByteArrayInputStream(sbiModel);
				} else {
					// try to get the sbiModel inside the jar
					byte[] jar = lastFileModelContent.getContent();
					if (jar != null) {
						// open the jar and find the sbimodel file
						byte[] jarSbiModel = MetaService.extractSbiModelFromJar(lastFileModelContent);
						jarSbiModel = EmfXmiSerializer.checkCompatibility(jarSbiModel);

						if (jarSbiModel != null) {
							is = new ByteArrayInputStream(jarSbiModel);
						} else {
							// SbiModel not found inside jar
						}
					} else {
						// SbiModel not found
					}

				}
			}

			if (is != null) {
				EmfXmiSerializer serializer = new EmfXmiSerializer();
				Model model = serializer.deserialize(is);
				request.getSession().setAttribute(MetaService.EMF_MODEL, model);
				JSONObject translatedModel = MetaService.createJson(model);
				ioManager.getHttpSession().setAttribute("translatedModel", translatedModel.toString());
			} else {
				request.getSession().removeAttribute(MetaService.EMF_MODEL);
				ioManager.getHttpSession().removeAttribute("translatedModel");
			}

			// -------------------------------------------------------------------------------------------

			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");

			request.getRequestDispatcher(dispatchUrl).forward(request, response);

		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
		} finally {
			logger.debug("OUT");
		}
	}

}
