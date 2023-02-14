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
package it.eng.spagobi.commons.dao;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectRating;
import it.eng.spagobi.analiticalmodel.document.dao.IObjNoteDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IObjTemplateDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IOutputParameterDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO;
import it.eng.spagobi.analiticalmodel.document.dao.IViewpointDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIMetaModelParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelParviewDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelViewpointDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.cache.dao.ICacheDAO;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.es.EventToDatabaseEmittingCommand;
import it.eng.spagobi.community.dao.ISbiCommunityDAO;
import it.eng.spagobi.dossier.dao.ISbiDossierActivityDAO;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.engines.config.dao.ISbiExportersDAO;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.functions.dao.IBIObjFunctionDAO;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.functions.dao.IFunctionInputColumnDAO;
import it.eng.spagobi.functions.dao.IFunctionInputVariableDAO;
import it.eng.spagobi.functions.dao.IFunctionOutputColumnDAO;
import it.eng.spagobi.georeport.dao.IFeaturesProviderFileDAO;
import it.eng.spagobi.georeport.dao.IFeaturesProviderWFSDAO;
import it.eng.spagobi.hotlink.rememberme.dao.IRememberMeDAO;
import it.eng.spagobi.i18n.dao.I18NMessagesDAO;
import it.eng.spagobi.images.dao.IImagesDAO;
import it.eng.spagobi.kpi.dao.IKpiDAO;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoFeaturesDAO;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoMapFeaturesDAO;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoMapsDAO;
import it.eng.spagobi.metadata.dao.ISbiDsBcDAO;
import it.eng.spagobi.metadata.dao.ISbiJobSourceDAO;
import it.eng.spagobi.metadata.dao.ISbiJobTableDAO;
import it.eng.spagobi.metadata.dao.ISbiMetaBCAttributeDAO;
import it.eng.spagobi.metadata.dao.ISbiMetaBCDAO;
import it.eng.spagobi.metadata.dao.ISbiMetaDocTabRelDAO;
import it.eng.spagobi.metadata.dao.ISbiMetaDsTabRel;
import it.eng.spagobi.metadata.dao.ISbiMetaJobDAO;
import it.eng.spagobi.metadata.dao.ISbiMetaSourceDAO;
import it.eng.spagobi.metadata.dao.ISbiMetaTableColumnDAO;
import it.eng.spagobi.metadata.dao.ISbiMetaTableDAO;
import it.eng.spagobi.metadata.dao.ISbiObjDsDAO;
import it.eng.spagobi.metadata.dao.ISbiTableBcDAO;
import it.eng.spagobi.profiling.dao.ISbiAccessibilityPreferencesDAO;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
import it.eng.spagobi.tools.alert.dao.IAlertDAO;
import it.eng.spagobi.tools.calendar.dao.ICalendarDAO;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.crossnavigation.dao.ICrossNavigationDAO;
import it.eng.spagobi.tools.dataset.dao.IBIObjDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.ISbiDataSetDAO;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO;
import it.eng.spagobi.tools.glossary.dao.IGlossaryDAO;
import it.eng.spagobi.tools.massiveExport.dao.IProgressThreadDAO;
import it.eng.spagobi.tools.news.dao.ISbiNewsDAO;
import it.eng.spagobi.tools.news.dao.ISbiNewsReadDAO;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.wsEvents.dao.SbiWsEventsDao;
import it.eng.spagobi.tools.tag.dao.ISbiTagDAO;
import it.eng.spagobi.tools.timespan.dao.ITimespanDAO;
import it.eng.spagobi.tools.udp.dao.IUdpDAO;
import it.eng.spagobi.tools.udp.dao.IUdpValueDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.wapp.dao.IMenuDAO;
import it.eng.spagobi.wapp.dao.IMenuRolesDAO;
import it.eng.spagobi.whatif.dao.IWhatifWorkflowDAO;
import it.eng.spagobi.workspace.dao.IFunctionsOrganizerDAO;
import it.eng.spagobi.workspace.dao.IObjFuncOrganizerDAO;

/**
 * Contains all the data access object for all the BO objects defined into it.eng.spagobi.bo package.
 */
public class DAOFactory {

	private static final Logger LOGGER = Logger.getLogger(DAOFactory.class);

	private static final String CONFIG_EMIT_AUTHORIZATION_EVENTS = "KNOWAGE.EMIT_AUTHORIZATION_EVENTS";

	private static String getDAOClass(String daoName) {
		return DAOConfig.getMappings().get(daoName);
	}

	/**
	 * Given, for a defined BO, its DAO name, creates the correct DAO instance
	 *
	 *
	 * @param daoName The BO DAO name
	 * @return An object representing the DAO instance
	 */

	private static Object createDAOInstance(String daoName) {
		Object daoObject = null;
		try {
			daoObject = Class.forName(getDAOClass(daoName)).newInstance();
		} catch (Throwable e) {
			throw new SpagoBIRuntimeException("Cannot instantiate " + daoName, e);
		}
		return daoObject;
	}

	/**
	 * Creates a DAO instance for a BI object.
	 *
	 * @return a DAO instance for the BIObject
	 *
	 */
	public static IBIObjectDAO getBIObjectDAO() {
		return (IBIObjectDAO) createDAOInstance("BIObjectDAO");
	}

	/**
	 * Creates a DAO instance for a Subreport.
	 *
	 * @return a DAO instance for the Subreport
	 *
	 */
	public static ISubreportDAO getSubreportDAO() {
		return (ISubreportDAO) createDAOInstance("SubreportDAO");
	}

	/**
	 * Creates a DAO instance for a BI object parameter.
	 *
	 * @return a DAO instance for the BIObject parameter
	 *
	 */
	public static IBIObjectParameterDAO getBIObjectParameterDAO() throws HibernateException {
		return (IBIObjectParameterDAO) createDAOInstance("BIObjectParameterDAO");
	}

	public static IBIMetaModelParameterDAO getBIMetaModelParameterDAO() throws HibernateException {
		return (IBIMetaModelParameterDAO) createDAOInstance("BIMetaModelParameterDAO");
	}

	/**
	 * Creates a DAO instance for a value constraint.
	 *
	 * @return a DAO instance for the value constraint
	 *
	 */
	public static ICheckDAO getChecksDAO() {
		return (ICheckDAO) createDAOInstance("ChecksDAO");
	}

	/**
	 * Creates a DAO instance for a domain.
	 *
	 * @return a DAO instance for the domain
	 *
	 */
	public static IDomainDAO getDomainDAO() {
		return (IDomainDAO) createDAOInstance("DomainDAO");
	}

	/**
	 * Creates a DAO instance for a categories.
	 *
	 * @return a DAO instance for categories
	 *
	 */
	public static ICategoryDAO getCategoryDAO() {
		return (ICategoryDAO) createDAOInstance("CategoryDAO");
	}

	/**
	 * Creates a DAO instance for an engine.
	 *
	 * @return a DAO instance for the engine
	 *
	 */
	public static IEngineDAO getEngineDAO() {
		return (IEngineDAO) createDAOInstance("EngineDAO");
	}

	/**
	 * Creates a DAO instance for a low functionality.
	 *
	 * @return a DAO instance for the low functionality
	 *
	 */
	public static ILowFunctionalityDAO getLowFunctionalityDAO() {
		return (ILowFunctionalityDAO) createDAOInstance("LowFunctionalityDAO");
	}

	/**
	 * Creates a DAO instance for a predefined LOV.
	 *
	 * @return a DAO instance for the predefined LOV
	 *
	 */
	public static IModalitiesValueDAO getModalitiesValueDAO() {
		return (IModalitiesValueDAO) createDAOInstance("ModalitiesValueDAO");
	}

	/**
	 * Creates a DAO instance for a parameter.
	 *
	 * @return a DAO instance for the parameter
	 *
	 */
	public static IParameterDAO getParameterDAO() {
		return (IParameterDAO) createDAOInstance("ParameterDAO");
	}

	/**
	 * Creates a DAO instance for a parameter use mode.
	 *
	 * @return a DAO instance for the parameter use mode
	 *
	 */
	public static IParameterUseDAO getParameterUseDAO() {
		return (IParameterUseDAO) createDAOInstance("ParameterUseDAO");
	}

	/**
	 * Creates a DAO instance for a role.
	 *
	 * @return a DAO instance for the role
	 *
	 */
	public static IRoleDAO getRoleDAO() {
		IRoleDAO ret = (IRoleDAO) createDAOInstance("RoleDAO");

		if (isAuthorizationEventsEmissionEnable()) {
			EventToDatabaseEmittingCommand eventToDatabaseEmittingCommand = new EventToDatabaseEmittingCommand();
			ret.setEventEmittingCommand(eventToDatabaseEmittingCommand);
		}

		return ret;
	}

	/**
	 * Gets the obj paruse dao.
	 *
	 * @return the obj paruse dao
	 *
	 *
	 */
	public static IObjParuseDAO getObjParuseDAO() throws HibernateException {
		return (IObjParuseDAO) createDAOInstance("ObjParuseDAO");
	}

	/**
	 * Gets the obj parview dao.
	 *
	 * @return the obj parview dao
	 *
	 *
	 */
	public static IObjParviewDAO getObjParviewDAO() throws HibernateException {
		return (IObjParviewDAO) createDAOInstance("ObjParviewDAO");
	}

	/**
	 * Creates a DAO instance for a viewpoint.
	 *
	 * @return a DAO instance for the viewpoint
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static IViewpointDAO getViewpointDAO() {
		return (IViewpointDAO) createDAOInstance("ViewpointDAO");
	}

	/**
	 * Creates a DAO instance for a meta model viewpoint.
	 *
	 * @return a DAO instance for the meta model viewpoint
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static IMetaModelViewpointDAO getMetaModelViewpointDAO() {
		return (IMetaModelViewpointDAO) createDAOInstance("MetaModelViewpointDAO");
	}

	/**
	 * Gets the data source dao.
	 *
	 * @return the data source dao
	 *
	 *
	 */
	public static IDataSourceDAO getDataSourceDAO() {
		return (IDataSourceDAO) createDAOInstance("DataSourceDAO");
	}

	/**
	 * Gets the data set dao.
	 *
	 * @return the data set dao
	 *
	 *
	 */
	public static IDataSetDAO getDataSetDAO() {
		return (IDataSetDAO) createDAOInstance("DataSetDAO");
	}

	/**
	 * Gets the sbi data set dao.
	 *
	 * @return the sbi data set dao
	 *
	 *
	 */
	public static ISbiDataSetDAO getSbiDataSetDAO() {
		return (ISbiDataSetDAO) createDAOInstance("SbiDataSetDAO");
	}

	/**
	 * Get the SbiNews dao
	 *
	 * @return the sbiNews dao
	 *
	 */
	public static ISbiNewsDAO getSbiNewsDAO() {
		return (ISbiNewsDAO) createDAOInstance("SbiNewsDAO");
	}

	/**
	 * Get the SbiNewsRead dao
	 *
	 * @return the SbiNews dao
	 */

	public static ISbiNewsReadDAO getSbiNewsReadDAO() {
		return (ISbiNewsReadDAO) createDAOInstance("SbiNewsReadDAO");
	}

	/**
	 * Gets the bin content dao.
	 *
	 * @return the bin content dao
	 *
	 *
	 */
	public static IBinContentDAO getBinContentDAO() {
		return (IBinContentDAO) createDAOInstance("BinContentDAO");
	}

	/**
	 * Gets the obj template dao.
	 *
	 * @return the obj template dao
	 *
	 *
	 */
	public static IObjTemplateDAO getObjTemplateDAO() {
		return (IObjTemplateDAO) createDAOInstance("ObjTemplateDAO");
	}

	/**
	 * Gets the obj note dao.
	 *
	 * @return the obj note dao
	 *
	 *
	 */
	public static IObjNoteDAO getObjNoteDAO() {
		return (IObjNoteDAO) createDAOInstance("ObjNoteDAO");
	}

	/**
	 * Gets the sub object dao.
	 *
	 * @return the sub object dao
	 *
	 *
	 */
	public static ISubObjectDAO getSubObjectDAO() {
		return (ISubObjectDAO) createDAOInstance("SubObjectDAO");
	}

	/**
	 * Gets the snapshot dao.
	 *
	 * @return the snapshot dao
	 *
	 *
	 */
	public static ISnapshotDAO getSnapshotDAO() {
		return (ISnapshotDAO) createDAOInstance("SnapshotDAO");
	}

	/**
	 * Creates a DAO instance for a BI object.
	 *
	 * @return a DAO instance for the BIObject
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static ISbiGeoMapsDAO getSbiGeoMapsDAO() {
		return (ISbiGeoMapsDAO) createDAOInstance("GeoMapDAO");
	}

	/**
	 * Creates a DAO instance for a BI object.
	 *
	 * @return a DAO instance for the BIObject
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static ISbiGeoFeaturesDAO getSbiGeoFeaturesDAO() {
		return (ISbiGeoFeaturesDAO) createDAOInstance("GeoFeatureDAO");
	}

	/**
	 * Creates a DAO instance for a BI object.
	 *
	 * @return a DAO instance for the BIObject
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static ISbiGeoMapFeaturesDAO getSbiGeoMapFeaturesDAO() {
		return (ISbiGeoMapFeaturesDAO) createDAOInstance("GeoMapFeatureDAO");
	}

	/**
	 * Creates a DAO instance for a BI object.
	 *
	 * @return a DAO instance for the BIObject
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static IUserFunctionalityDAO getUserFunctionalityDAO() {
		return (IUserFunctionalityDAO) createDAOInstance("UserFunctionalityDAO");
	}

	/**
	 * Gets the distribution list dao.
	 *
	 * @return the distribution list dao
	 *
	 *
	 */
	public static IDistributionListDAO getDistributionListDAO() {
		return (IDistributionListDAO) createDAOInstance("DistributionListDAO");
	}

	/**
	 * Gets the remember me dao.
	 *
	 * @return the remember me dao
	 *
	 *
	 */
	public static IRememberMeDAO getRememberMeDAO() {
		return (IRememberMeDAO) createDAOInstance("RememberMeDAO");
	}

	/**
	 * Gets the menu dao.
	 *
	 * @return the menu dao
	 *
	 *
	 */
	public static IMenuDAO getMenuDAO() {
		return (IMenuDAO) createDAOInstance("MenuDAO");
	}

	/**
	 * Gets the menu roles dao.
	 *
	 * @return the menu roles dao
	 *
	 *
	 */
	public static IMenuRolesDAO getMenuRolesDAO() {
		return (IMenuRolesDAO) createDAOInstance("MenuRolesDAO");
	}

	/**
	 * Gets the bI object rating dao.
	 *
	 * @return the bI object rating dao
	 *
	 *
	 */
	public static IBIObjectRating getBIObjectRatingDAO() {
		return (IBIObjectRating) createDAOInstance("BIObjectRatingDAO");
	}

	/**
	 * Gets the KPI dao.
	 *
	 * @return the KPI dao
	 *
	 *
	 */
	public static IKpiDAO getKpiDAO() {
		return (IKpiDAO) createDAOInstance("KpiDAO");
	}

	/**
	 * Creates a DAO instance for a object Metadata.
	 *
	 * @return a DAO instance for the predefined object metadata
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static IObjMetadataDAO getObjMetadataDAO() {
		return (IObjMetadataDAO) createDAOInstance("ObjMetadataDAO");
	}

	/**
	 * Creates a DAO instance for a object Metadata.
	 *
	 * @return a DAO instance for the predefined object metadata
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static IObjMetacontentDAO getObjMetacontentDAO() {
		return (IObjMetacontentDAO) createDAOInstance("ObjMetacontentDAO");
	}

	/**
	 * Creates a DAO instance for a object SbiUser.
	 *
	 * @return a DAO instance for the predefined SbiUser
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static ISbiUserDAO getSbiUserDAO() {
		ISbiUserDAO ret = (ISbiUserDAO) createDAOInstance("SbiUserDAO");

		if (isAuthorizationEventsEmissionEnable()) {
			EventToDatabaseEmittingCommand eventToDatabaseEmittingCommand = new EventToDatabaseEmittingCommand();
			ret.setEventEmittingCommand(eventToDatabaseEmittingCommand);
		}

		return ret;
	}

	/**
	 * Creates a DAO instance for a object SbiAttribute.
	 *
	 * @return a DAO instance for the predefined SbiAttribute
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static ISbiAttributeDAO getSbiAttributeDAO() {
		return (ISbiAttributeDAO) createDAOInstance("SbiAttributeDAO");
	}

	/**
	 * Creates a DAO instance for a object SbiConfig.
	 *
	 * @return a DAO instance for the predefined SbiConfig
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static IConfigDAO getSbiConfigDAO() {
		return (IConfigDAO) createDAOInstance("SbiConfigDAO");
	}

	/**
	 * Creates a DAO instance for a object Udp.
	 *
	 * @return a DAO instance for the predefined Udp
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static IUdpDAO getUdpDAO() {
		return (IUdpDAO) createDAOInstance("UdpDAO");
	}

	/**
	 * Creates a DAO instance for a object UdpValue.
	 *
	 * @return a DAO instance for the predefined UdpValue
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static IUdpValueDAO getUdpDAOValue() {
		return (IUdpValueDAO) createDAOInstance("UdpDAOValue");
	}

	/**
	 * Creates a DAO instance for a object KpiError.
	 *
	 * @return a DAO instance for the predefined KpiError
	 *
	 * @throws EMFUserError If an Exception occurred
	 */

	/**
	 * Creates a DAO instance for a BI i18n messages.
	 *
	 * @return a DAO instance for the I18nmessage
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static I18NMessagesDAO getI18NMessageDAO() {
		return (I18NMessagesDAO) createDAOInstance("I18NMessagesDAO");
	}

	/**
	 * Creates a DAO instance for a progress Thread.
	 *
	 * @return a DAO instance for the progress Thread
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static IProgressThreadDAO getProgressThreadDAO() {
		return (IProgressThreadDAO) createDAOInstance("ProgressThreadDAO");
	}

	/**
	 * Creates a DAO instance for a scheduler.
	 *
	 * @return a DAO instance for the scheduler
	 *
	 * @throws EMFUserError If an Exception occurred
	 */
	public static ISchedulerDAO getSchedulerDAO() {
		return (ISchedulerDAO) createDAOInstance("SchedulerDAO");
	}

	public static SbiWsEventsDao getWsEventsDao() {
		return (SbiWsEventsDao) createDAOInstance("WsEventDAO");
	}

	public static ITenantsDAO getTenantsDAO() {
		return (ITenantsDAO) createDAOInstance("TenantsDAO");
	}

	public static IMetaModelsDAO getMetaModelsDAO() {
		return (IMetaModelsDAO) createDAOInstance("MetaModelsDAO");
	}

	public static IArtifactsDAO getArtifactsDAO() {
		return (IArtifactsDAO) createDAOInstance("ArtifactsDAO");
	}

	public static ISbiCommunityDAO getCommunityDAO() {
		return (ISbiCommunityDAO) createDAOInstance("CommunityDAO");
	}

	public static ISbiGeoLayersDAO getSbiGeoLayerDao() {
		return (ISbiGeoLayersDAO) createDAOInstance("GeoLayersDAO");
	}

	public static ICacheDAO getCacheDao() {
		return (ICacheDAO) createDAOInstance("CacheDAO");
	}

	public static IProductTypeDAO getProductTypeDAO() {
		return (IProductTypeDAO) createDAOInstance("ProductTypeDAO");
	}

	/**
	 * Gets the Glossary dao.
	 *
	 * @return the Glossary dao
	 *
	 *
	 */
	public static IGlossaryDAO getGlossaryDAO() {
		return (IGlossaryDAO) createDAOInstance("GlossaryDAO");
	}

	public static ICalendarDAO getCalendarDAO() {
		return (ICalendarDAO) createDAOInstance("CalendarDAO");
	}

	/**
	 * Gets the Images dao.
	 *
	 * @return the Images dao
	 * @throws EMFUserError
	 */
	public static IImagesDAO getImagesDAO() {
		return (IImagesDAO) createDAOInstance("ImagesDAO");
	}

	public static ISbiFederationDefinitionDAO getFedetatedDatasetDAO() {
		return (ISbiFederationDefinitionDAO) createDAOInstance("FederationDefinitionDAO");
	}

	public static IBIObjDataSetDAO getBIObjDataSetDAO() {
		return (IBIObjDataSetDAO) createDAOInstance("BIObjDataSetDAO");
	}

	public static IBIObjFunctionDAO getBIObjFunctionDAO() {
		return (IBIObjFunctionDAO) createDAOInstance("BIObjFunctionDAO");
	}

	public static IFunctionInputVariableDAO getFunctionInputVariableDAO() {
		return (IFunctionInputVariableDAO) createDAOInstance("FunctionInputVariableDAO");
	}

	public static IFunctionOutputColumnDAO getFunctionOutputColumnDAO() {
		return (IFunctionOutputColumnDAO) createDAOInstance("FunctionOutputColumnDAO");
	}

	public static IFunctionInputColumnDAO getFunctionInputColumnDAO() {
		return (IFunctionInputColumnDAO) createDAOInstance("FunctionInputColumnDAO");
	}

	/**
	 * Gets the Timespan dao.
	 *
	 * @return the Timespan dao
	 *
	 *
	 */
	public static ITimespanDAO getTimespanDAO() {
		return (ITimespanDAO) createDAOInstance("TimespanDAO");
	}

	/**
	 * Gets the IFeaturesProviderFileDAO dao.
	 *
	 * @return the IFeaturesProviderFileDAO dao
	 *
	 *
	 */
	public static IFeaturesProviderFileDAO getFeaturesProviderFileDAO() {
		return (IFeaturesProviderFileDAO) createDAOInstance("IFeaturesProviderFileDAO");

	}

	/**
	 * Gets the IFeaturesProviderWFSDAO dao.
	 *
	 * @return the IFeaturesProviderWFSDAO dao
	 *
	 *
	 */
	public static IFeaturesProviderWFSDAO getFeaturesProviderWFSDAO() {
		return (IFeaturesProviderWFSDAO) createDAOInstance("IFeaturesProviderWFSDAO");
	}

	/**
	 * Gets the CrossNavigationDAO dao.
	 *
	 * @return the CrossNavigationDAO dao
	 *
	 *
	 */
	public static ICrossNavigationDAO getCrossNavigationDAO() throws HibernateException {
		return (ICrossNavigationDAO) createDAOInstance("CrossNavigationDAO");
	}

	/**
	 * Gets the OutputParameterDAO dao.
	 *
	 * @return the OutputParameterDAO dao
	 *
	 *
	 */
	public static IOutputParameterDAO getOutputParameterDAO() {
		return (IOutputParameterDAO) createDAOInstance("OutputParameterDAO");
	}

	/**
	 * Gets the SbiMetaSourceDAO dao.
	 *
	 * @return the SbiMetaSourceDAO dao
	 *
	 *
	 */
	public static ISbiMetaSourceDAO getSbiMetaSourceDAO() {
		return (ISbiMetaSourceDAO) createDAOInstance("ISbiMetaSourceDAO");
	}

	/**
	 * Gets the SbiMetaTableDAO dao.
	 *
	 * @return the SbiMetaTableDAO dao
	 *
	 *
	 */
	public static ISbiMetaTableDAO getSbiMetaTableDAO() {
		return (ISbiMetaTableDAO) createDAOInstance("ISbiMetaTableDAO");
	}

	/**
	 * Gets the SbiMetaTableColumnDAO dao.
	 *
	 * @return the SbiMetaTableColumnDAO dao
	 *
	 *
	 */
	public static ISbiMetaTableColumnDAO getSbiMetaTableColumnDAO() {
		return (ISbiMetaTableColumnDAO) createDAOInstance("ISbiMetaTableColumnDAO");
	}

	/**
	 * Gets the SbiMetaBCDAO dao.
	 *
	 * @return the SbiMetaBCDAO dao
	 *
	 *
	 */
	public static ISbiMetaBCDAO getSbiMetaBCDAO() {
		return (ISbiMetaBCDAO) createDAOInstance("ISbiMetaBCDAO");
	}

	/**
	 * Gets the SbiMetaBCAttributeDAO dao.
	 *
	 * @return the SbiMetaBCAttributeDAO dao
	 *
	 *
	 */
	public static ISbiMetaBCAttributeDAO getSbiMetaBCAttributeDAO() {
		return (ISbiMetaBCAttributeDAO) createDAOInstance("ISbiMetaBCAttributeDAO");
	}

	/**
	 * Gets the SbiMetaJobDAO dao.
	 *
	 * @return the SbiMetaJobDAO dao
	 *
	 *
	 */
	public static ISbiMetaJobDAO getSbiMetaJobDAO() {
		return (ISbiMetaJobDAO) createDAOInstance("ISbiMetaJobDAO");
	}

	/**
	 * Gets the SbiDsBcDAO dao.
	 *
	 * @return the SbiDsBcDAO dao
	 *
	 *
	 */
	public static ISbiDsBcDAO getSbiDsBcDAO() {
		return (ISbiDsBcDAO) createDAOInstance("ISbiDsBcDAO");
	}

	/**
	 * Gets the SbiJobSourceDAO dao.
	 *
	 * @return the SbiJobSourceDAO dao
	 *
	 *
	 */
	public static ISbiJobSourceDAO getSbiJobSourceDAO() {
		return (ISbiJobSourceDAO) createDAOInstance("ISbiJobSourceDAO");
	}

	/**
	 * Gets the SbiJobTableDAO dao.
	 *
	 * @return the SbiJobTableDAO dao
	 *
	 *
	 */
	public static ISbiJobTableDAO getSbiJobTableDAO() {
		return (ISbiJobTableDAO) createDAOInstance("ISbiJobTableDAO");
	}

	/**
	 * Gets the SbiObjDsDAO dao.
	 *
	 * @return the SbiObjDsDAO dao
	 *
	 *
	 */
	public static ISbiObjDsDAO getSbiObjDsDAO() {
		return (ISbiObjDsDAO) createDAOInstance("ISbiObjDsDAO");
	}

	/**
	 * Gets the SbiTableBcDAO dao.
	 *
	 * @return the SbiTableBcDAO dao
	 *
	 *
	 */
	public static ISbiTableBcDAO getSbiTableBCDAO() {
		return (ISbiTableBcDAO) createDAOInstance("ISbiTableBcDAO");
	}

	/**
	 * Gets the SbiDsBcDAO dao.
	 *
	 * @return the SbiDsBcDAO dao
	 *
	 *
	 */
	public static ISbiDsBcDAO getDsBcDAO() {
		return (ISbiDsBcDAO) createDAOInstance("ISbiDsBcDAO");
	}

	public static ISbiMetaDsTabRel getDsTableRelDAO() {
		return (ISbiMetaDsTabRel) createDAOInstance("ISbiMetaDsTabRel");
	}

	public static ISbiMetaDocTabRelDAO getSbiMetaDocTabRelDAO() {
		return (ISbiMetaDocTabRelDAO) createDAOInstance("ISbiMetaDocTabRelDAO");
	}

	/**
	 * Gets the AlertDAO dao.
	 *
	 * @return the AlertDAO dao
	 *
	 *
	 */
	public static IAlertDAO getAlertDAO() {
		return (IAlertDAO) createDAOInstance("AlertDAO");
	}

	/**
	 * Gets the CatalogFunction dao.
	 *
	 * @return the CatalogFunction dao
	 *
	 *
	 */
	public static ICatalogFunctionDAO getCatalogFunctionDAO() {
		return (ICatalogFunctionDAO) createDAOInstance("ICatalogFunctionDAO");
	}

	public static IFunctionsOrganizerDAO getFunctionsOrganizerDAO() {
		return (IFunctionsOrganizerDAO) createDAOInstance("IFunctionsOrganizerDAO");
	}

	public static IWhatifWorkflowDAO getWhatifWorkflowDAO() {
		return (IWhatifWorkflowDAO) createDAOInstance("IWhatifWorkflowDAO");
	}

	public static IObjFuncOrganizerDAO getObjFuncOrganizerDAO() {
		return (IObjFuncOrganizerDAO) createDAOInstance("IObjFuncOrganizerDAO");
	}

	public static ISbiAccessibilityPreferencesDAO getSiAccessibilityPreferencesDAO() {
		return (ISbiAccessibilityPreferencesDAO) createDAOInstance("SbiAccessibilityPreferencesDAO");
	}

	public static ISbiDossierActivityDAO getDossierActivityDao() {
		return (ISbiDossierActivityDAO) createDAOInstance("DossierActivityDAO");
	}

	public static ISbiExportersDAO getExportersDao() {
		return (ISbiExportersDAO) createDAOInstance("ExportersDAO");
	}

	public static IMetaModelParuseDAO getMetaModelParuseDao() {
		return (IMetaModelParuseDAO) createDAOInstance("MetaModelParuseDAO");
	}

	public static IMetaModelParviewDAO getMetaModelParviewDao() {
		return (IMetaModelParviewDAO) createDAOInstance("MetaModelParviewDAO");
	}

	public static ISbiTagDAO getSbiTagDao() {
		return (ISbiTagDAO) createDAOInstance("ISbiTagDAO");
	}

	private static boolean isAuthorizationEventsEmissionEnable() {
		SingletonConfig config = SingletonConfig.getInstance();
		String configValue = config.getConfigValue(CONFIG_EMIT_AUTHORIZATION_EVENTS);
		return Boolean.parseBoolean(configValue);
	}
}
