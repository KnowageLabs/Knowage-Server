/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 1-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.dao;

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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.check.dao.ICheckDAO;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.cache.dao.ICacheDAO;
import it.eng.spagobi.community.dao.ISbiCommunityDAO;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.engines.dossier.dao.IDossierDAO;
import it.eng.spagobi.engines.dossier.dao.IDossierPartsTempDAO;
import it.eng.spagobi.engines.dossier.dao.IDossierPresentationsDAO;
import it.eng.spagobi.federateddataset.dao.ISbiFederationDefinitionDAO;
import it.eng.spagobi.georeport.dao.IFeaturesProviderFileDAO;
import it.eng.spagobi.georeport.dao.IFeaturesProviderWFSDAO;
import it.eng.spagobi.hotlink.rememberme.dao.IRememberMeDAO;
import it.eng.spagobi.i18n.dao.I18NMessagesDAO;
import it.eng.spagobi.images.dao.IImagesDAO;
import it.eng.spagobi.kpi.alarm.dao.ISbiAlarmContactDAO;
import it.eng.spagobi.kpi.alarm.dao.ISbiAlarmDAO;
import it.eng.spagobi.kpi.alarm.dao.ISbiAlarmEventDAO;
import it.eng.spagobi.kpi.config.dao.IKpiDAO;
import it.eng.spagobi.kpi.config.dao.IKpiErrorDAO;
import it.eng.spagobi.kpi.config.dao.IKpiInstPeriodDAO;
import it.eng.spagobi.kpi.config.dao.IKpiInstanceDAO;
import it.eng.spagobi.kpi.config.dao.IMeasureUnitDAO;
import it.eng.spagobi.kpi.config.dao.IPeriodicityDAO;
import it.eng.spagobi.kpi.goal.dao.IGoalDAO;
import it.eng.spagobi.kpi.model.dao.IModelDAO;
import it.eng.spagobi.kpi.model.dao.IModelInstanceDAO;
import it.eng.spagobi.kpi.model.dao.IModelResourceDAO;
import it.eng.spagobi.kpi.model.dao.IResourceDAO;
import it.eng.spagobi.kpi.ou.dao.IOrganizationalUnitDAO;
import it.eng.spagobi.kpi.threshold.dao.IThresholdDAO;
import it.eng.spagobi.kpi.threshold.dao.IThresholdValueDAO;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoFeaturesDAO;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoLayersDAO;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoMapFeaturesDAO;
import it.eng.spagobi.mapcatalogue.dao.ISbiGeoMapsDAO;
import it.eng.spagobi.profiling.dao.ISbiAttributeDAO;
import it.eng.spagobi.profiling.dao.ISbiUserDAO;
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
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.wsEvents.dao.SbiWsEventsDao;
import it.eng.spagobi.tools.timespan.dao.ITimespanDAO;
import it.eng.spagobi.tools.udp.dao.IUdpDAO;
import it.eng.spagobi.tools.udp.dao.IUdpValueDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.wapp.dao.IMenuDAO;
import it.eng.spagobi.wapp.dao.IMenuRolesDAO;

import org.apache.log4j.Logger;

/**
 * Contains all the data access object for all the BO objects defined into it.eng.spagobi.bo package.
 */
public class DAOFactory {

	static private Logger logger = Logger.getLogger(DAOFactory.class);

	private static String getDAOClass(String daoName) {
		return DAOConfig.getMappings().get(daoName);
	}

	/**
	 * Given, for a defined BO, its DAO name, creates the correct DAO instance
	 * 
	 * 
	 * @param daoName
	 *            The BO DAO name
	 * @return An object representing the DAO instance
	 */

	private static Object createDAOInstance(String daoName) {
		Object daoObject = null;
		try {
			// ConfigSingleton configSingleton=ConfigSingleton.getInstance();
			// SourceBean daoConfigSourceBean =(SourceBean) configSingleton.getFilteredSourceBeanAttribute("SPAGOBI.DAO-CONF.DAO","name", daoName);
			// String daoClassName = (String)daoConfigSourceBean.getAttribute("implementation");
			// daoObject = Class.forName(daoClassName).newInstance();
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
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IBIObjectDAO getBIObjectDAO() throws EMFUserError {
		return (IBIObjectDAO) createDAOInstance("BIObjectDAO");
	}

	/**
	 * Creates a DAO instance for a Subreport.
	 * 
	 * @return a DAO instance for the Subreport
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static ISubreportDAO getSubreportDAO() throws EMFUserError {
		return (ISubreportDAO) createDAOInstance("SubreportDAO");
	}

	/**
	 * Creates a DAO instance for a BI object parameter.
	 * 
	 * @return a DAO instance for the BIObject parameter
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IBIObjectParameterDAO getBIObjectParameterDAO() throws EMFUserError {
		return (IBIObjectParameterDAO) createDAOInstance("BIObjectParameterDAO");
	}

	/**
	 * Creates a DAO instance for a value constraint.
	 * 
	 * @return a DAO instance for the value constraint
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static ICheckDAO getChecksDAO() throws EMFUserError {
		return (ICheckDAO) createDAOInstance("ChecksDAO");
	}

	/**
	 * Creates a DAO instance for a domain.
	 * 
	 * @return a DAO instance for the domain
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IDomainDAO getDomainDAO() throws EMFUserError {
		return (IDomainDAO) createDAOInstance("DomainDAO");
	}

	/**
	 * Creates a DAO instance for an engine.
	 * 
	 * @return a DAO instance for the engine
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IEngineDAO getEngineDAO() throws EMFUserError {
		return (IEngineDAO) createDAOInstance("EngineDAO");
	}

	/**
	 * Creates a DAO instance for a low functionality.
	 * 
	 * @return a DAO instance for the low functionality
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static ILowFunctionalityDAO getLowFunctionalityDAO() throws EMFUserError {
		return (ILowFunctionalityDAO) createDAOInstance("LowFunctionalityDAO");
	}

	/**
	 * Creates a DAO instance for a predefined LOV.
	 * 
	 * @return a DAO instance for the predefined LOV
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IModalitiesValueDAO getModalitiesValueDAO() throws EMFUserError {
		return (IModalitiesValueDAO) createDAOInstance("ModalitiesValueDAO");
	}

	/**
	 * Creates a DAO instance for a parameter.
	 * 
	 * @return a DAO instance for the parameter
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IParameterDAO getParameterDAO() throws EMFUserError {
		return (IParameterDAO) createDAOInstance("ParameterDAO");
	}

	/**
	 * Creates a DAO instance for a parameter use mode.
	 * 
	 * @return a DAO instance for the parameter use mode
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IParameterUseDAO getParameterUseDAO() throws EMFUserError {
		return (IParameterUseDAO) createDAOInstance("ParameterUseDAO");
	}

	/**
	 * Creates a DAO instance for a role.
	 * 
	 * @return a DAO instance for the role
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IRoleDAO getRoleDAO() throws EMFUserError {
		return (IRoleDAO) createDAOInstance("RoleDAO");
	}

	/**
	 * Gets the obj paruse dao.
	 * 
	 * @return the obj paruse dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IObjParuseDAO getObjParuseDAO() throws EMFUserError {
		return (IObjParuseDAO) createDAOInstance("ObjParuseDAO");
	}

	/**
	 * Gets the obj parview dao.
	 * 
	 * @return the obj parview dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IObjParviewDAO getObjParviewDAO() throws EMFUserError {
		return (IObjParviewDAO) createDAOInstance("ObjParviewDAO");
	}

	/**
	 * Creates a DAO instance for a viewpoint.
	 * 
	 * @return a DAO instance for the viewpoint
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IViewpointDAO getViewpointDAO() throws EMFUserError {
		return (IViewpointDAO) createDAOInstance("ViewpointDAO");
	}

	/**
	 * Gets the data source dao.
	 * 
	 * @return the data source dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IDataSourceDAO getDataSourceDAO() throws EMFUserError {
		return (IDataSourceDAO) createDAOInstance("DataSourceDAO");
	}

	/**
	 * Gets the data set dao.
	 * 
	 * @return the data set dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IDataSetDAO getDataSetDAO() throws EMFUserError {
		return (IDataSetDAO) createDAOInstance("DataSetDAO");
	}

	/**
	 * Gets the sbi data set dao.
	 * 
	 * @return the sbi data set dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static ISbiDataSetDAO getSbiDataSetDAO() throws EMFUserError {
		return (ISbiDataSetDAO) createDAOInstance("SbiDataSetDAO");
	}

	/**
	 * Gets the bin content dao.
	 * 
	 * @return the bin content dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IBinContentDAO getBinContentDAO() throws EMFUserError {
		return (IBinContentDAO) createDAOInstance("BinContentDAO");
	}

	/**
	 * Gets the obj template dao.
	 * 
	 * @return the obj template dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IObjTemplateDAO getObjTemplateDAO() throws EMFUserError {
		return (IObjTemplateDAO) createDAOInstance("ObjTemplateDAO");
	}

	/**
	 * Gets the obj note dao.
	 * 
	 * @return the obj note dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IObjNoteDAO getObjNoteDAO() throws EMFUserError {
		return (IObjNoteDAO) createDAOInstance("ObjNoteDAO");
	}

	/**
	 * Gets the sub object dao.
	 * 
	 * @return the sub object dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static ISubObjectDAO getSubObjectDAO() throws EMFUserError {
		return (ISubObjectDAO) createDAOInstance("SubObjectDAO");
	}

	/**
	 * Gets the snapshot dao.
	 * 
	 * @return the snapshot dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static ISnapshotDAO getSnapshotDAO() throws EMFUserError {
		return (ISnapshotDAO) createDAOInstance("SnapshotDAO");
	}

	/**
	 * Creates a DAO instance for a BI object.
	 * 
	 * @return a DAO instance for the BIObject
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static ISbiGeoMapsDAO getSbiGeoMapsDAO() throws EMFUserError {
		return (ISbiGeoMapsDAO) createDAOInstance("GeoMapDAO");
	}

	/**
	 * Creates a DAO instance for a BI object.
	 * 
	 * @return a DAO instance for the BIObject
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static ISbiGeoFeaturesDAO getSbiGeoFeaturesDAO() throws EMFUserError {
		return (ISbiGeoFeaturesDAO) createDAOInstance("GeoFeatureDAO");
	}

	/**
	 * Creates a DAO instance for a BI object.
	 * 
	 * @return a DAO instance for the BIObject
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static ISbiGeoMapFeaturesDAO getSbiGeoMapFeaturesDAO() throws EMFUserError {
		return (ISbiGeoMapFeaturesDAO) createDAOInstance("GeoMapFeatureDAO");
	}

	/**
	 * Creates a DAO instance for a BI object.
	 * 
	 * @return a DAO instance for the BIObject
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IUserFunctionalityDAO getUserFunctionalityDAO() throws EMFUserError {
		return (IUserFunctionalityDAO) createDAOInstance("UserFunctionalityDAO");
	}

	/**
	 * Gets the distribution list dao.
	 * 
	 * @return the distribution list dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IDistributionListDAO getDistributionListDAO() throws EMFUserError {
		return (IDistributionListDAO) createDAOInstance("DistributionListDAO");
	}

	/**
	 * Gets the dossier dao.
	 * 
	 * @return the dossier dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IDossierDAO getDossierDAO() throws EMFUserError {
		return (IDossierDAO) createDAOInstance("DossierDAO");
	}

	/**
	 * Gets the dossier presentation dao.
	 * 
	 * @return the dossier presentation dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IDossierPresentationsDAO getDossierPresentationDAO() throws EMFUserError {
		return (IDossierPresentationsDAO) createDAOInstance("DossierPresentationDAO");
	}

	/**
	 * Gets the dossier parts temp dao.
	 * 
	 * @return the dossier parts temp dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IDossierPartsTempDAO getDossierPartsTempDAO() throws EMFUserError {
		return (IDossierPartsTempDAO) createDAOInstance("DossierPartsTempDAO");
	}

	/**
	 * Gets the remember me dao.
	 * 
	 * @return the remember me dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IRememberMeDAO getRememberMeDAO() throws EMFUserError {
		return (IRememberMeDAO) createDAOInstance("RememberMeDAO");
	}

	/**
	 * Gets the menu dao.
	 * 
	 * @return the menu dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IMenuDAO getMenuDAO() throws EMFUserError {
		return (IMenuDAO) createDAOInstance("MenuDAO");
	}

	/**
	 * Gets the menu roles dao.
	 * 
	 * @return the menu roles dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IMenuRolesDAO getMenuRolesDAO() throws EMFUserError {
		return (IMenuRolesDAO) createDAOInstance("MenuRolesDAO");
	}

	/**
	 * Gets the bI object rating dao.
	 * 
	 * @return the bI object rating dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IBIObjectRating getBIObjectRatingDAO() throws EMFUserError {
		return (IBIObjectRating) createDAOInstance("BIObjectRatingDAO");
	}

	/**
	 * Gets the KPI dao.
	 * 
	 * @return the KPI dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IKpiDAO getKpiDAO() throws EMFUserError {
		return (IKpiDAO) createDAOInstance("KpiDAO");
	}

	/**
	 * Gets the KPI Instance dao.
	 * 
	 * @return the KPI Instance dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IKpiInstanceDAO getKpiInstanceDAO() throws EMFUserError {
		return (IKpiInstanceDAO) createDAOInstance("KpiInstanceDAO");
	}

	/**
	 * Gets the bI object rating dao.
	 * 
	 * @return the bI object rating dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static ISbiAlarmContactDAO getAlarmContactDAO() throws EMFUserError {
		return (ISbiAlarmContactDAO) createDAOInstance("AlarmContactDAO");
	}

	/**
	 * Gets the bI object rating dao.
	 * 
	 * @return the bI object rating dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static ISbiAlarmDAO getAlarmDAO() throws EMFUserError {
		return (ISbiAlarmDAO) createDAOInstance("AlarmDAO");
	}

	/**
	 * Gets the bI object rating dao.
	 * 
	 * @return the bI object rating dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static ISbiAlarmEventDAO getAlarmEventDAO() throws EMFUserError {
		return (ISbiAlarmEventDAO) createDAOInstance("AlarmEventDAO");
	}

	/**
	 * Gets the MODEL dao.
	 * 
	 * @return the MODEL dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IModelDAO getModelDAO() throws EMFUserError {
		return (IModelDAO) createDAOInstance("ModelDAO");
	}

	/**
	 * Gets the MODELINSTANCE dao.
	 * 
	 * @return the MODELINSTANCE dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IModelInstanceDAO getModelInstanceDAO() throws EMFUserError {
		return (IModelInstanceDAO) createDAOInstance("ModelInstanceDAO");
	}

	/**
	 * Gets the PERIODICITY dao.
	 * 
	 * @return the PERIODICITY dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IPeriodicityDAO getPeriodicityDAO() throws EMFUserError {
		return (IPeriodicityDAO) createDAOInstance("PeriodicityDAO");
	}

	/**
	 * Gets the THRESHOLD dao.
	 * 
	 * @return the THRESHOLD dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IThresholdDAO getThresholdDAO() throws EMFUserError {
		return (IThresholdDAO) createDAOInstance("ThresholdDAO");
	}

	/**
	 * Gets the MODELRESOURCE dao.
	 * 
	 * @return the MODELRESOURCE dao.
	 * 
	 * @throws EMFUserError
	 *             the EMF user error.
	 */
	public static IModelResourceDAO getModelResourcesDAO() throws EMFUserError {
		return (IModelResourceDAO) createDAOInstance("ModelResourceDAO");
	}

	/**
	 * Gets the ThresholdValue dao.
	 * 
	 * @return the ThresholdValue dao.
	 * 
	 * @throws EMFUserError
	 *             the EMF user error.
	 */
	public static IThresholdValueDAO getThresholdValueDAO() throws EMFUserError {
		return (IThresholdValueDAO) createDAOInstance("ThresholdValueDAO");
	}

	/**
	 * Creates a DAO instance for a predefined Measure Unit.
	 * 
	 * @return a DAO instance for the predefined MeasureUnit
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IMeasureUnitDAO getMeasureUnitDAO() throws EMFUserError {
		return (IMeasureUnitDAO) createDAOInstance("MeasureUnitDAO");
	}

	/**
	 * Creates a DAO instance for Organizational Unit.
	 * 
	 * @return a DAO instance for Organizational Unit
	 */
	public static IOrganizationalUnitDAO getOrganizationalUnitDAO() {
		return (IOrganizationalUnitDAO) createDAOInstance("SbiKpiOUDAO");
	}

	/**
	 * Creates a DAO instance for Organizational Unit.
	 * 
	 * @return a DAO instance for Organizational Unit
	 */
	public static IGoalDAO getGoalDAO() {
		return (IGoalDAO) createDAOInstance("GoalDAO");
	}

	/**
	 * Creates a DAO instance for a predefined Resource.
	 * 
	 * @return a DAO instance for the predefined rescource
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IResourceDAO getResourceDAO() throws EMFUserError {
		return (IResourceDAO) createDAOInstance("ResourceDAO");
	}

	/**
	 * Creates a DAO instance for a predefined Resource.
	 * 
	 * @return a DAO instance for the predefined rescource
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IKpiInstPeriodDAO getKpiInstPeriodDAO() throws EMFUserError {
		return (IKpiInstPeriodDAO) createDAOInstance("KpiInstPeriodDAO");
	}

	/**
	 * Creates a DAO instance for a object Metadata.
	 * 
	 * @return a DAO instance for the predefined object metadata
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IObjMetadataDAO getObjMetadataDAO() throws EMFUserError {
		return (IObjMetadataDAO) createDAOInstance("ObjMetadataDAO");
	}

	/**
	 * Creates a DAO instance for a object Metadata.
	 * 
	 * @return a DAO instance for the predefined object metadata
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IObjMetacontentDAO getObjMetacontentDAO() throws EMFUserError {
		return (IObjMetacontentDAO) createDAOInstance("ObjMetacontentDAO");
	}

	/**
	 * Creates a DAO instance for a object SbiUser.
	 * 
	 * @return a DAO instance for the predefined SbiUser
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static ISbiUserDAO getSbiUserDAO() throws EMFUserError {
		return (ISbiUserDAO) createDAOInstance("SbiUserDAO");
	}

	/**
	 * Creates a DAO instance for a object SbiAttribute.
	 * 
	 * @return a DAO instance for the predefined SbiAttribute
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static ISbiAttributeDAO getSbiAttributeDAO() throws EMFUserError {
		return (ISbiAttributeDAO) createDAOInstance("SbiAttributeDAO");
	}

	/**
	 * Creates a DAO instance for a object SbiConfig.
	 * 
	 * @return a DAO instance for the predefined SbiConfig
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IConfigDAO getSbiConfigDAO() throws EMFUserError {
		return (IConfigDAO) createDAOInstance("SbiConfigDAO");
	}

	/**
	 * Creates a DAO instance for a object Udp.
	 * 
	 * @return a DAO instance for the predefined Udp
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IUdpDAO getUdpDAO() throws EMFUserError {
		return (IUdpDAO) createDAOInstance("UdpDAO");
	}

	/**
	 * Creates a DAO instance for a object UdpValue.
	 * 
	 * @return a DAO instance for the predefined UdpValue
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IUdpValueDAO getUdpDAOValue() throws EMFUserError {
		return (IUdpValueDAO) createDAOInstance("UdpDAOValue");
	}

	/**
	 * Creates a DAO instance for a object KpiError.
	 * 
	 * @return a DAO instance for the predefined KpiError
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IKpiErrorDAO getKpiErrorDAO() throws EMFUserError {
		return (IKpiErrorDAO) createDAOInstance("KpiErrorDAO");
	}

	/**
	 * Creates a DAO instance for a BI i18n messages.
	 * 
	 * @return a DAO instance for the I18nmessage
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static I18NMessagesDAO getI18NMessageDAO() throws EMFUserError {
		return (I18NMessagesDAO) createDAOInstance("I18NMessagesDAO");
	}

	/**
	 * Creates a DAO instance for a progress Thread.
	 * 
	 * @return a DAO instance for the progress Thread
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static IProgressThreadDAO getProgressThreadDAO() throws EMFUserError {
		return (IProgressThreadDAO) createDAOInstance("ProgressThreadDAO");
	}

	/**
	 * Creates a DAO instance for a scheduler.
	 * 
	 * @return a DAO instance for the scheduler
	 * 
	 * @throws EMFUserError
	 *             If an Exception occurred
	 */
	public static ISchedulerDAO getSchedulerDAO() throws EMFUserError {
		return (ISchedulerDAO) createDAOInstance("SchedulerDAO");
	}

	public static SbiWsEventsDao getWsEventsDao() throws EMFUserError {
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
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IGlossaryDAO getGlossaryDAO() throws EMFUserError {
		return (IGlossaryDAO) createDAOInstance("GlossaryDAO");
	}

	/**
	 * Gets the Images dao.
	 * 
	 * @return the Images dao
	 * @throws EMFUserError
	 */
	public static IImagesDAO getImagesDAO() throws EMFUserError {
		return (IImagesDAO) createDAOInstance("ImagesDAO");
	}

	public static ISbiFederationDefinitionDAO getFedetatedDatasetDAO() throws EMFUserError {
		return (ISbiFederationDefinitionDAO) createDAOInstance("FederationDefinitionDAO");
	}

	public static IBIObjDataSetDAO getBIObjDataSetDAO() throws EMFUserError {
		return (IBIObjDataSetDAO) createDAOInstance("BIObjDataSetDAO");
	}

	/**
	 * Gets the Timespan dao.
	 * 
	 * @return the Timespan dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static ITimespanDAO getTimespanDAO() throws EMFUserError {
		return (ITimespanDAO) createDAOInstance("TimespanDAO");
	}

	/**
	 * Gets the IFeaturesProviderFileDAO dao.
	 * 
	 * @return the IFeaturesProviderFileDAO dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IFeaturesProviderFileDAO getFeaturesProviderFileDAO() throws EMFUserError {
		return (IFeaturesProviderFileDAO) createDAOInstance("IFeaturesProviderFileDAO");

	}

	/**
	 * Gets the IFeaturesProviderWFSDAO dao.
	 * 
	 * @return the IFeaturesProviderWFSDAO dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IFeaturesProviderWFSDAO getFeaturesProviderWFSDAO() throws EMFUserError {
		return (IFeaturesProviderWFSDAO) createDAOInstance("IFeaturesProviderWFSDAO");
	}

	/**
	 * Gets the CrossNavigationDAO dao.
	 * 
	 * @return the CrossNavigationDAO dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static ICrossNavigationDAO getCrossNavigationDAO() throws EMFUserError {
		return (ICrossNavigationDAO) createDAOInstance("CrossNavigationDAO");
	}

	/**
	 * Gets the OutputParameterDAO dao.
	 * 
	 * @return the OutputParameterDAO dao
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static IOutputParameterDAO getOutputParameterDAO() throws EMFUserError {
		return (IOutputParameterDAO) createDAOInstance("OutputParameterDAO");
	}
}
