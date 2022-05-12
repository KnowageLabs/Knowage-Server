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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.jboss.resteasy.plugins.providers.html.View;
import org.json.JSONObject;

import it.eng.knowage.meta.generator.jpamapping.wrappers.JpaProperties;
import it.eng.knowage.meta.generator.utils.JavaKeywordsUtils;
import it.eng.knowage.meta.model.Model;
import it.eng.knowage.meta.model.ModelFactory;
import it.eng.knowage.meta.model.ModelProperty;
import it.eng.knowage.meta.model.ModelPropertyCategory;
import it.eng.knowage.meta.model.ModelPropertyType;
import it.eng.knowage.meta.model.business.BusinessModel;
import it.eng.knowage.meta.model.business.BusinessTable;
import it.eng.knowage.meta.model.business.SimpleBusinessColumn;
import it.eng.knowage.meta.model.serializer.EmfXmiSerializer;
import it.eng.qbe.utility.DbTypeThreadLocal;
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
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.IDataBase;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;

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
		urls.put("error", "/WEB-INF/jsp/error.jsp");

	}

	@Context
	protected HttpServletRequest request;
	@Context
	protected HttpServletResponse response;

	@GET
	@Path("/{pagename}")
	@Produces("text/html")
	public View getPage(@PathParam("pagename") String pageName, @QueryParam("bmName") String businessModelName) throws Exception {
		String dispatchUrl = urls.get(pageName);

		try {
			EngineStartServletIOManager ioManager = new EngineStartServletIOManager(request, response);
			UserProfile userProfile = ioManager.getUserProfile();
			if (userProfile == null) {
				String userId = request.getHeader("user");
				userProfile = (UserProfile) UserUtilities.getUserProfile(userId);
				ioManager.setUserProfile(userProfile);
			}

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
			if (userProfile != null) {
				rdao.setUserProfile(userProfile);
			}
			List<Role> rlist = rdao.loadAllRolesFiltereByTenant();
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
				boolean modelFound = false;
				if (sbiModel != null) {
					is = new ByteArrayInputStream(sbiModel);
				} else {
					// try to get the sbiModel inside the jar
					byte[] jar = lastFileModelContent.getContent();
					if (jar != null) {
						// open the jar and find the sbimodel file
						byte[] jarSbiModel = MetaService.extractSbiModelFromJar(lastFileModelContent);

						if (jarSbiModel != null) {
							jarSbiModel = EmfXmiSerializer.checkCompatibility(jarSbiModel);
							is = new ByteArrayInputStream(jarSbiModel);
							modelFound = true;
						} else {
							// SbiModel not found inside jar
							logger.error("Business model file not found inside jar");
						}
					} else {
						// SbiModel not found
						logger.error("Business model file not found");
					}
					if (!modelFound) {
						return new View(urls.get("error"));
					}

				}
			}

			if (is != null) {
				EmfXmiSerializer serializer = new EmfXmiSerializer();
				Model model = serializer.deserialize(is);
				checkBackwardCompatibility(model);

				updateModelName(model, businessModelName);

				request.getSession().setAttribute(MetaService.EMF_MODEL, model);
				model.eAdapters().add(new ECrossReferenceAdapter());

				String datasourceId = request.getParameter("datasourceId");
				if (datasourceId != null && !datasourceId.equals("")) {
					IDataSource ds = DAOFactory.getDataSourceDAO().loadDataSourceByID(Integer.valueOf(datasourceId));
					if (ds != null) {
						IDataBase db = DataBaseFactory.getDataBase(ds);
						String dbType = db.getName();
						DbTypeThreadLocal.setDbType(dbType);
					}
				}
				JSONObject translatedModel = MetaService.createJson(model);
				ioManager.getHttpSession().setAttribute("translatedModel", translatedModel.toString());
			} else {
				request.getSession().removeAttribute(MetaService.EMF_MODEL);
				ioManager.getHttpSession().removeAttribute("translatedModel");
			}

			// -------------------------------------------------------------------------------------------

			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");

			return new View(dispatchUrl);

		} catch (Exception e) {
			logger.error("Error during Metamodel initialization: ", e);
			throw e;
		} finally {
			DbTypeThreadLocal.unset();
			logger.debug("OUT");
		}
	}

	private void updateModelName(Model model, String name) {
		model.setName(name);
		BusinessModel businessModel = model.getBusinessModels().get(0);
		businessModel.setName(name);
		// Get Package Name
		String packageName = businessModel.getProperties().get(JpaProperties.MODEL_PACKAGE).getValue();
		// removing previous business model's name
		packageName = packageName.substring(0, packageName.lastIndexOf("."));
		// append logical name of the model to the package name to avoid same class load problem
		packageName = packageName + "." + JavaKeywordsUtils.transformToJavaPropertyName(name);
		businessModel.getProperties().get(JpaProperties.MODEL_PACKAGE).setValue(packageName.toLowerCase());
	}

	/**
	 * @param model
	 * @param crossReferenceAdapter
	 */
	private void addCrossReferenceAdapterToResource(Model model, ECrossReferenceAdapter crossReferenceAdapter) {
		ResourceSet resourceSet = new ResourceSetImpl();
		URI uri = URI.createURI(MetaService.KNOWAGE_MODEL_URI);
		Resource resource = resourceSet.createResource(uri);
		resource.getContents().add(model);

		resource.getResourceSet().eAdapters().add(crossReferenceAdapter);
	}

	public void checkBackwardCompatibility(Model model) {
		// Put here methods to guarantee the backward compatibility with old versions of the metamodel
		removeColumnTypes(model);
		removeTableTypes(model);
		addProfileFilterConditionProperty(model);
		addCustomFunctionProperty(model);
		addDateFormatProperty(model);
		addTimeFormatProperty(model);
		addDataTypeProperty(model);

	}

	/**
	 * @param model
	 */
	private void addDataTypeProperty(Model model) {
		ModelFactory FACTORY = ModelFactory.eINSTANCE;
		BusinessModel businessModel = model.getBusinessModels().get(0);
		ModelPropertyType propertyType = null;
		ModelProperty property;
		ModelPropertyCategory structuralCategory = model.getPropertyCategory("Structural");
		List<BusinessTable> businessTables = businessModel.getBusinessTables();

		// check if the property already exists
		propertyType = model.getPropertyType("structural.datatype");
		if (propertyType != null) {

			propertyType.getAdmissibleValues().add("DECIMAL");
			propertyType.getAdmissibleValues().add("BIGINT");
			propertyType.getAdmissibleValues().add("FLOAT");
			propertyType.getAdmissibleValues().add("SMALLINT");
		}

	}

	/**
	 * @param model
	 */
	private void removeColumnTypes(Model model) {
		ModelPropertyType propertyType = null;
		// check if the property already exists
		propertyType = model.getPropertyType("structural.columntype");
		if (propertyType != null) {
			// model has already the property, we can skip the check
			propertyType.getAdmissibleValues().remove("hour_id");
			propertyType.getAdmissibleValues().remove("the_date");
			propertyType.getAdmissibleValues().remove("calendar");
			propertyType.getAdmissibleValues().remove("temporal_id");

		}

	}

	/**
	 * @param model
	 */
	private void removeTableTypes(Model model) {
		ModelPropertyType propertyType = null;
		// check if the property already exists
		propertyType = model.getPropertyType("structural.tabletype");
		if (propertyType != null) {
			// model has already the property, we can skip the check
			propertyType.getAdmissibleValues().remove("temporal dimension");
			propertyType.getAdmissibleValues().remove("time dimension");

		}

	}

	/**
	 * @param model
	 */
	private void addDateFormatProperty(Model model) {
		ModelFactory FACTORY = ModelFactory.eINSTANCE;
		BusinessModel businessModel = model.getBusinessModels().get(0);
		ModelPropertyType propertyType = null;
		ModelProperty property;
		ModelPropertyCategory structuralCategory = model.getPropertyCategory("Structural");
		List<BusinessTable> businessTables = businessModel.getBusinessTables();

		// check if the property already exists
		propertyType = model.getPropertyType("structural.dateformat");
		if (propertyType != null) {
			for (int i = 0; i < propertyType.getAdmissibleValues().size(); i++) {
				if (propertyType.getAdmissibleValues().get(i).equals("DD/MM/YYYY HH:MM:SS")) {
					propertyType.getAdmissibleValues().set(i, "DD/MM/YYYY HH:mm:SS");
				}

				if (propertyType.getAdmissibleValues().get(i).equals("DD/MM/YYYY HH:MM")) {
					propertyType.getAdmissibleValues().set(i, "DD/MM/YYYY HH:mm");
				}
			}
			return;
		} else {

			// inject property type
			propertyType = FACTORY.createModelPropertyType();
			propertyType.setId("structural.dateformat");
			propertyType.setName("Format Date");
			propertyType.setDescription("The date format to use if the value is date");
			propertyType.setCategory(structuralCategory);
			propertyType.getAdmissibleValues().add("LLLL");
			propertyType.getAdmissibleValues().add("llll");
			propertyType.getAdmissibleValues().add("LLL");
			propertyType.getAdmissibleValues().add("lll");
			propertyType.getAdmissibleValues().add("DD/MM/YYYY HH:mm:SS");
			propertyType.getAdmissibleValues().add("DD/MM/YYYY HH:mm");
			propertyType.getAdmissibleValues().add("LL");
			propertyType.getAdmissibleValues().add("ll");
			propertyType.getAdmissibleValues().add("L");
			propertyType.getAdmissibleValues().add("l");
			propertyType.setDefaultValue("LLLL");

			model.getPropertyTypes().add(propertyType);

			// apply the property to every simple business column
			for (BusinessTable businessTable : businessTables) {
				List<SimpleBusinessColumn> simpleBusinessColumns = businessTable.getSimpleBusinessColumns();
				for (SimpleBusinessColumn simpleBusinessColumn : simpleBusinessColumns) {

					property = FACTORY.createModelProperty();
					property.setPropertyType(propertyType);
					// add property on simple business column
					simpleBusinessColumn.getProperties().put(property.getPropertyType().getId(), property);
				}
			}
		}

	}

	private void addTimeFormatProperty(Model model) {
		ModelFactory FACTORY = ModelFactory.eINSTANCE;
		BusinessModel businessModel = model.getBusinessModels().get(0);
		ModelPropertyType propertyType = null;
		ModelProperty property;
		ModelPropertyCategory structuralCategory = model.getPropertyCategory("Structural");
		List<BusinessTable> businessTables = businessModel.getBusinessTables();

		// check if the property already exists
		propertyType = model.getPropertyType("structural.timeformat");
		if (propertyType != null) {
			// model has already the property, we can skip the check
			return;
		} else {

			// inject property type
			propertyType = FACTORY.createModelPropertyType();
			propertyType.setId("structural.timeformat");
			propertyType.setName("Format Time");
			propertyType.setDescription("The date format to use if the value is time");
			propertyType.setCategory(structuralCategory);
			propertyType.getAdmissibleValues().add("LT");
			propertyType.getAdmissibleValues().add("LTS");

			propertyType.setDefaultValue("LT");

			model.getPropertyTypes().add(propertyType);

			// apply the property to every simple business column
			for (BusinessTable businessTable : businessTables) {
				List<SimpleBusinessColumn> simpleBusinessColumns = businessTable.getSimpleBusinessColumns();
				for (SimpleBusinessColumn simpleBusinessColumn : simpleBusinessColumns) {

					property = FACTORY.createModelProperty();
					property.setPropertyType(propertyType);
					// add property on simple business column
					simpleBusinessColumn.getProperties().put(property.getPropertyType().getId(), property);
				}
			}
		}

	}

	/**
	 * Add the property type 'structural.filtercondition' to simple business columns if it doesn't already exists (previous to Knowage 6.2)
	 *
	 * @param model
	 */
	public void addProfileFilterConditionProperty(Model model) {
		ModelFactory FACTORY = ModelFactory.eINSTANCE;
		BusinessModel businessModel = model.getBusinessModels().get(0);
		ModelPropertyType propertyType = null;
		ModelProperty property;
		ModelPropertyCategory structuralCategory = model.getPropertyCategory("Structural");
		List<BusinessTable> businessTables = businessModel.getBusinessTables();

		// check if the property already exists
		propertyType = model.getPropertyType("structural.filtercondition");
		if (propertyType != null) {
			// model has already the property, we can skip the check
			return;
		} else {

			// inject property type
			propertyType = FACTORY.createModelPropertyType();
			propertyType.setId("structural.filtercondition");
			propertyType.setName("Profile Attribute Filter Type");
			propertyType.setDescription("The type of filter to use with profile attributes");
			propertyType.setCategory(structuralCategory);
			propertyType.getAdmissibleValues().add("EQUALS TO");
			propertyType.getAdmissibleValues().add("IN");
			propertyType.getAdmissibleValues().add("LIKE");
			propertyType.setDefaultValue("EQUALS TO");

			model.getPropertyTypes().add(propertyType);

			// apply the property to every simple business column
			for (BusinessTable businessTable : businessTables) {
				List<SimpleBusinessColumn> simpleBusinessColumns = businessTable.getSimpleBusinessColumns();
				for (SimpleBusinessColumn simpleBusinessColumn : simpleBusinessColumns) {

					property = FACTORY.createModelProperty();
					property.setPropertyType(propertyType);
					// add property on simple business column
					simpleBusinessColumn.getProperties().put(property.getPropertyType().getId(), property);
				}
			}
		}
	}

	/**
	 * Add the property type 'structural.customFunction' to simple business columns if it doesn't already exists (previous to Knowage 6.2)
	 *
	 * @param model
	 */
	public void addCustomFunctionProperty(Model model) {
		ModelFactory FACTORY = ModelFactory.eINSTANCE;
		BusinessModel businessModel = model.getBusinessModels().get(0);
		ModelPropertyType propertyType = null;
		ModelProperty property;
		ModelPropertyCategory structuralCategory = model.getPropertyCategory("Structural");
		List<BusinessTable> businessTables = businessModel.getBusinessTables();

		// check if the property already exists
		propertyType = model.getPropertyType("structural.customFunction");
		if (propertyType != null) {
			// model has already the property, we can skip the check
			return;
		} else {

			// inject property type
			propertyType = FACTORY.createModelPropertyType();
			propertyType.setId("structural.customFunction");
			propertyType.setName("Custom function");
			propertyType.setDescription("Custom DB function to apply to column");
			propertyType.setCategory(structuralCategory);
			propertyType.setDefaultValue("");

			model.getPropertyTypes().add(propertyType);

			// apply the property to every simple business column
			for (BusinessTable businessTable : businessTables) {
				List<SimpleBusinessColumn> simpleBusinessColumns = businessTable.getSimpleBusinessColumns();
				for (SimpleBusinessColumn simpleBusinessColumn : simpleBusinessColumns) {

					property = FACTORY.createModelProperty();
					property.setPropertyType(propertyType);
					// add property on simple business column
					simpleBusinessColumn.getProperties().put(property.getPropertyType().getId(), property);
				}
			}
		}
	}

}
