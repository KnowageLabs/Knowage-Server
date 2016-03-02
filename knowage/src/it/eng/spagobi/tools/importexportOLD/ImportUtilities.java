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
package it.eng.spagobi.tools.importexportOLD;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjPar;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjTemplates;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiSnapshots;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiSubObjects;
import it.eng.spagobi.analiticalmodel.document.metadata.SbiViewpoints;
import it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFuncRole;
import it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFuncRoleId;
import it.eng.spagobi.analiticalmodel.functionalitytree.metadata.SbiFunctions;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParuseId;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiObjParviewId;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuseCk;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuseCkId;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuseDet;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParuseDetId;
import it.eng.spagobi.behaviouralmodel.check.metadata.SbiChecks;
import it.eng.spagobi.behaviouralmodel.lov.bo.DatasetDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovDetailFactory;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.behaviouralmodel.lov.metadata.SbiLov;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiAuthorizations;
import it.eng.spagobi.commons.metadata.SbiBinContents;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.federateddataset.metadata.SbiDataSetFederation;
import it.eng.spagobi.federateddataset.metadata.SbiFederationDefinition;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarm;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;
import it.eng.spagobi.kpi.config.metadata.SbiKpi;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstPeriod;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstance;
import it.eng.spagobi.kpi.config.metadata.SbiKpiPeriodicity;
import it.eng.spagobi.kpi.config.metadata.SbiKpiRel;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModel;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelInst;
import it.eng.spagobi.kpi.model.metadata.SbiKpiModelResources;
import it.eng.spagobi.kpi.model.metadata.SbiResources;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitGrant;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitGrantNodes;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitGrantNodesId;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitHierarchies;
import it.eng.spagobi.kpi.ou.metadata.SbiOrgUnitNodes;
import it.eng.spagobi.kpi.threshold.metadata.SbiThreshold;
import it.eng.spagobi.kpi.threshold.metadata.SbiThresholdValue;
import it.eng.spagobi.tools.catalogue.metadata.SbiArtifact;
import it.eng.spagobi.tools.catalogue.metadata.SbiArtifactContent;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModel;
import it.eng.spagobi.tools.catalogue.metadata.SbiMetaModelContent;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.dataset.metadata.SbiObjDataSet;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.tools.objmetadata.metadata.SbiObjMetacontents;
import it.eng.spagobi.tools.objmetadata.metadata.SbiObjMetadata;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;
import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.json.JSONObject;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

public class ImportUtilities {

	static private Logger logger = Logger.getLogger(ImportUtilities.class);

	public static final int MAX_DEFAULT_IMPORT_FILE_SIZE = 5242880;

	// if cannot retrieve once more the object cause has been alreeady retrieved cache it in Map
	Map<Integer, SbiDataSet> datasetMap = new HashMap<Integer, SbiDataSet>();
	IEngUserProfile profile = null;
	MetadataLogger metaLog;

	/**
	 * Decompress the export compress file.
	 *
	 * @param pathImpTmpFolder
	 *            The path of the import directory
	 * @param pathArchiveFile
	 *            The path of the exported archive
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static void decompressArchive(String pathImpTmpFolder, String pathArchiveFile) throws EMFUserError {
		logger.debug("IN");
		File tmpFolder = new File(pathImpTmpFolder);
		tmpFolder.mkdirs();
		int BUFFER = 2048;
		FileInputStream fis = null;
		ZipInputStream zis = null;
		try {
			fis = new FileInputStream(pathArchiveFile);
			zis = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				BufferedOutputStream dest = null;
				FileOutputStream fos = null;
				try {
					int count;
					byte data[] = new byte[BUFFER];
					String entryName = entry.getName();
					int indexofdp = entryName.indexOf(":\\");
					if (indexofdp != -1) {
						int indexlastslash = entryName.lastIndexOf("\\");
						entryName = entryName.substring(0, indexofdp - 2) + entryName.substring(indexlastslash);
					}
					File entryFile = new File(pathImpTmpFolder + "/" + entryName);
					File entryFileFolder = entryFile.getParentFile();
					entryFileFolder.mkdirs();
					fos = new FileOutputStream(pathImpTmpFolder + "/" + entryName);
					dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = zis.read(data, 0, BUFFER)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
				} finally {
					if (dest != null)
						dest.close();
					if (fos != null)
						fos.close();
				}
			}
		} catch (EOFException eofe) {
			logger.warn("Error during the decompression of the exported file ", eofe);
		} catch (Exception e) {
			logger.warn("Error during the decompression of the exported file ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "100", ImportManager.messageBundle);
		} finally {
			if (zis != null || fis != null) {
				try {
					if (zis != null)
						zis.close();
					if (fis != null)
						fis.close();
				} catch (IOException e) {
					logger.error("Error closing stream", e);
				}
			}
			logger.debug("OUT");
		}
	}

	/**
	 * Creates an Hibernate session factory for the exported database.
	 *
	 * @param pathDBFolder
	 *            The path of the folder which contains the exported database
	 * @return The Hibernate session factory
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public static SessionFactory getHibSessionExportDB(String pathDBFolder) throws EMFUserError {
		logger.debug("IN");
		Configuration conf = new Configuration();
		String resource = "it/eng/spagobi/tools/importexport/metadata/hibernate.cfg.hsql.export.xml";
		conf = conf.configure(resource);
		String hsqlJdbcString = "jdbc:hsqldb:file:" + pathDBFolder + "/metadata;shutdown=true";
		conf.setProperty("hibernate.connection.url", hsqlJdbcString);
		SessionFactory sessionFactory = conf.buildSessionFactory();
		logger.debug("IN");
		return sessionFactory;
	}

	/**
	 * Creates a new hibernate role object.
	 *
	 * @param role
	 *            old hibernate role object
	 * @return the new hibernate role object
	 */
	public SbiExtRoles makeNew(SbiExtRoles role) {
		logger.debug("IN");
		SbiExtRoles newRole = new SbiExtRoles();
		newRole.setCode(role.getCode());
		newRole.setDescr(role.getDescr());
		newRole.setName(role.getName());
		// newRole.setRoleType(role.getRoleType());
		// newRole.setRoleTypeCode(role.getRoleTypeCode());
		newRole.setSbiParuseDets(new HashSet());
		newRole.setSbiFuncRoles(new HashSet());
		// newRole.setIsAbleToSaveIntoPersonalFolder(role.getIsAbleToSaveIntoPersonalFolder());
		// newRole.setIsAbleToSaveRememberMe(role.getIsAbleToSaveRememberMe());
		// newRole.setIsAbleToSeeMetadata(role.getIsAbleToSeeMetadata());
		// newRole.setIsAbleToSeeNotes(role.getIsAbleToSeeNotes());
		// newRole.setIsAbleToSeeSnapshots(role.getIsAbleToSeeSnapshots());
		// newRole.setIsAbleToSeeSubobjects(role.getIsAbleToSeeSubobjects());
		// newRole.setIsAbleToSeeViewpoints(role.getIsAbleToSeeViewpoints());
		// newRole.setIsAbleToSendMail(role.getIsAbleToSendMail());
		// newRole.setIsAbleToBuildQbeQuery(role.getIsAbleToBuildQbeQuery());
		// newRole.setIsAbleToDoMassiveExport(role.getIsAbleToDoMassiveExport());
		// newRole.setIsAbleToEditWorksheet(role.getIsAbleToEditWorksheet());
		// newRole.setIsAbleToManageUsers(role.getIsAbleToManageUsers());
		logger.debug("OUT");
		return newRole;
	}

	/**
	 * Creates a new hibernate role object.
	 *
	 * @param role
	 *            old hibernate role object
	 * @param id
	 *            the id
	 * @return the new hibernate role object
	 */
	public SbiExtRoles makeNew(SbiExtRoles role, Integer id) {
		logger.debug("IN");
		SbiExtRoles newRole = makeNew(role);
		newRole.setExtRoleId(id);
		logger.debug("OUT");
		return newRole;
	}

	/**
	 * Creates a new hibernate engine object.
	 *
	 * @param engine
	 *            old hibernate engine object
	 * @return the new hibernate engine object
	 */
	public SbiEngines makeNew(SbiEngines engine) {
		logger.debug("IN");
		SbiEngines newEng = new SbiEngines();
		newEng.setDescr(engine.getDescr());
		newEng.setDriverNm(engine.getDriverNm());
		newEng.setEncrypt(engine.getEncrypt());
		newEng.setLabel(engine.getLabel());
		newEng.setMainUrl(engine.getMainUrl());
		newEng.setName(engine.getName());
		newEng.setObjUplDir(engine.getObjUplDir());
		newEng.setObjUseDir(engine.getObjUseDir());
		newEng.setSecnUrl(engine.getSecnUrl());
		newEng.setClassNm(engine.getClassNm());
		newEng.setUseDataSet(engine.getUseDataSet());
		newEng.setUseDataSource(engine.getUseDataSource());
		logger.debug("OUT");
		return newEng;
	}

	/**
	 * Make new data source.
	 *
	 * @param ds
	 *            the ds
	 * @return the sbi data source
	 */
	public SbiDataSource makeNew(SbiDataSource ds) {
		logger.debug("IN");
		SbiDataSource newDS = new SbiDataSource();
		newDS.setDescr(ds.getDescr());
		newDS.setLabel(ds.getLabel());
		newDS.setJndi(ds.getJndi());
		newDS.setDriver(ds.getDriver());
		newDS.setPwd(ds.getPwd());
		newDS.setUrl_connection(ds.getUrl_connection());
		newDS.setUser(ds.getUser());
		newDS.setMultiSchema((ds.getMultiSchema() != null) ? ds.getMultiSchema() : Boolean.valueOf(false));
		newDS.setReadOnly((ds.getReadOnly() != null) ? ds.getReadOnly() : Boolean.valueOf(false));
		newDS.setWriteDefault((ds.getWriteDefault() != null) ? ds.getWriteDefault() : Boolean.valueOf(false));

		newDS.setSchemaAttribute(ds.getSchemaAttribute());
		logger.debug("OUT");
		return newDS;
	}

	public SbiDataSource modifyExisting(SbiDataSource dataSource, Session sessionCurrDB, Integer existingDatasourceId) {
		logger.debug("IN");
		SbiDataSource existingDatasource = null;
		try {
			existingDatasource = (SbiDataSource) sessionCurrDB.load(SbiDataSource.class, existingDatasourceId);
			existingDatasource.setDescr(dataSource.getDescr());
			existingDatasource.setDialect(dataSource.getDialect());
			existingDatasource.setDialectDescr(dataSource.getDialectDescr());
			existingDatasource.setDriver(dataSource.getDriver());
			existingDatasource.setJndi(dataSource.getJndi());
			existingDatasource.setLabel(dataSource.getLabel());
			existingDatasource.setPwd(dataSource.getPwd());
			existingDatasource.setUrl_connection(dataSource.getUrl_connection());
			existingDatasource.setUser(dataSource.getUser());
			existingDatasource.setMultiSchema((dataSource.getMultiSchema() != null) ? dataSource.getMultiSchema() : Boolean.valueOf(false));
			existingDatasource.setReadOnly((dataSource.getReadOnly() != null) ? dataSource.getReadOnly() : Boolean.valueOf(false));
			existingDatasource.setWriteDefault((dataSource.getWriteDefault() != null) ? dataSource.getWriteDefault() : Boolean.valueOf(false));
			existingDatasource.setSchemaAttribute(dataSource.getSchemaAttribute());

		} finally {
			logger.debug("OUT");
		}
		return existingDatasource;
	}

	/**
	 * Creates a new hibernate engine object.
	 *
	 * @param engine
	 *            old hibernate engine object
	 * @param id
	 *            the id
	 * @return the new hibernate engine object
	 */
	public SbiEngines makeNew(SbiEngines engine, Integer id) {
		logger.debug("IN");
		SbiEngines newEng = makeNew(engine);
		newEng.setEngineId(id);
		logger.debug("OUT");
		return newEng;
	}

	/**
	 * Creates a new hibernate functionality object.
	 *
	 * @param funct
	 *            the funct
	 * @return the new hibernate functionality object
	 */
	public SbiFunctions makeNew(SbiFunctions funct) {
		logger.debug("IN");
		SbiFunctions newFunct = new SbiFunctions();
		newFunct.setCode(funct.getCode());
		newFunct.setDescr(funct.getDescr());
		newFunct.setFunctType(funct.getFunctType());
		newFunct.setFunctTypeCd(funct.getFunctTypeCd());
		newFunct.setName(funct.getName());
		newFunct.setParentFunct(funct.getParentFunct());
		newFunct.setPath(funct.getPath());
		logger.debug("OUT");
		return newFunct;
	}

	/**
	 * Creates a new hibernate functionality object.
	 *
	 * @param funct
	 *            the funct
	 * @param id
	 *            the id
	 * @return the new hibernate functionality object
	 */
	public SbiFunctions makeNew(SbiFunctions funct, Integer id) {
		logger.debug("IN");
		SbiFunctions newFunct = makeNew(funct);
		newFunct.setFunctId(id);
		logger.debug("OUT");
		return newFunct;
	}

	public SbiFunctions modifyExisting(SbiFunctions exportedFunction, Integer existingFunctionId, Session sessionCurrDB) throws EMFUserError {
		logger.debug("IN");

		SbiFunctions existingFunction = (SbiFunctions) sessionCurrDB.load(SbiFunctions.class, existingFunctionId);

		existingFunction.setCode(exportedFunction.getCode());
		existingFunction.setName(exportedFunction.getName());
		existingFunction.setDescr(exportedFunction.getDescr());
		existingFunction.setPath(exportedFunction.getPath());
		logger.debug("OUT");
		return existingFunction;
	}

	/**
	 * Creates a new hibernate lov object.
	 *
	 * @param lov
	 *            old hibernate lov object
	 * @return the new hibernate lov object
	 */
	public SbiLov makeNew(SbiLov lov, Session sessionCurrDB, HashMap<String, String> dsExportUser) {
		logger.debug("IN");
		SbiLov newlov = new SbiLov();
		newlov.setDefaultVal(lov.getDefaultVal());
		newlov.setDescr(lov.getDescr());
		newlov.setInputType(lov.getInputType());
		newlov.setInputTypeCd(lov.getInputTypeCd());
		newlov.setLabel(lov.getLabel());
		newlov.setName(lov.getName());
		newlov.setProfileAttr(lov.getProfileAttr());

		String lovProvider = lov.getLovProvider();
		try {
			ILovDetail lovDetail = LovDetailFactory.getLovFromXML(lovProvider);
			if (lovDetail instanceof QueryDetail) {
				// if user has associated another datasource then set the
				// associated one, else put the same
				QueryDetail queryDetail = new QueryDetail(lov.getLovProvider());
				String dataSource = queryDetail.getDataSource();
				if (dsExportUser != null && dsExportUser.get(dataSource) != null) {
					String newDs = dsExportUser.get(dataSource);
					queryDetail.setDataSource(newDs);
				} else {
					queryDetail.setDataSource(dataSource);
				}
				newlov.setLovProvider(queryDetail.toXML());
			} else if (lovDetail instanceof DatasetDetail) {
				// update dataset id
				DatasetDetail datasetDetail = (DatasetDetail) lovDetail;
				String datasetLabel = datasetDetail.getDatasetLabel();

				Integer datasetId = null;
				try {
					Query query = sessionCurrDB.createQuery("select d.id from SbiDataSet d where d.label = :label and d.active= true");
					query.setString("label", datasetLabel);
					datasetId = (Integer) query.uniqueResult();
				} catch (Exception e) {
					logger.error("More than one dataset active with label " + datasetLabel + ": filter from user organization");
				}
				if (datasetId == null) {
					// if there are more dataset that are retrieved choose the organization one (should take from LOV but it is not handled
					Query query = sessionCurrDB
							.createQuery("select d.id from SbiDataSet d where d.label = :label and d.active= true and d.organization=:organization");
					query.setString("label", datasetLabel);
					String organization = ((UserProfile) profile).getOrganization();
					logger.debug("filter for organization " + organization);
					query.setString("organization", organization);
					datasetId = (Integer) query.uniqueResult();
				}

				datasetDetail.setDatasetId(datasetId.toString());
				newlov.setLovProvider(datasetDetail.toXML());
			} else {
				newlov.setLovProvider(lovProvider);
			}
		} catch (Exception e) {
			logger.error("Error in evaluating lov provider for exporter lov [" + lov.getLabel() + "]. It will not be modified", e);
			newlov.setLovProvider(lovProvider);
		}

		logger.debug("OUT");
		return newlov;
	}

	/**
	 * Creates a new hibernate lov object.
	 *
	 * @param lov
	 *            old hibernate lov object
	 * @param id
	 *            the id
	 * @return the new hibernate lov object
	 */
	public SbiLov makeNew(SbiLov lov, Session sessionCurrDB, Integer id, Map user) {
		logger.debug("IN");
		SbiLov newlov = makeNew(lov, sessionCurrDB, null);
		newlov.setLovId(id);
		logger.debug("OUT");
		return newlov;
	}

	/**
	 * Creates a new hibernate check object.
	 *
	 * @param check
	 *            old hibernate check object
	 * @return the new hibernate check object
	 */
	public SbiChecks makeNew(SbiChecks check) {
		logger.debug("IN");
		SbiChecks newck = new SbiChecks();
		newck.setCheckType(check.getCheckType());
		newck.setDescr(check.getDescr());
		newck.setLabel(check.getLabel());
		newck.setName(check.getName());
		newck.setValue1(check.getValue1());
		newck.setValue2(check.getValue2());
		newck.setValueTypeCd(check.getValueTypeCd());
		logger.debug("OUT");
		return newck;
	}

	/**
	 * Creates a new hibernate check object.
	 *
	 * @param check
	 *            old hibernate check object
	 * @param id
	 *            the id
	 * @return the new hibernate check object
	 */
	public SbiChecks makeNew(SbiChecks check, Integer id) {
		logger.debug("IN");
		SbiChecks newCk = makeNew(check);
		newCk.setCheckId(id);
		logger.debug("OUT");
		return newCk;
	}

	/**
	 * Creates a new hibernate parameter object.
	 *
	 * @param param
	 *            the param
	 * @return the new hibernate parameter object
	 */
	public SbiParameters makeNew(SbiParameters param) {
		logger.debug("IN");
		SbiParameters newPar = new SbiParameters();
		newPar.setDescr(param.getDescr());
		newPar.setLabel(param.getLabel());
		newPar.setLength(param.getLength());
		newPar.setMask(param.getMask());
		newPar.setName(param.getName());
		newPar.setParameterType(param.getParameterType());
		newPar.setParameterTypeCode(param.getParameterTypeCode());
		newPar.setSbiObjPars(new HashSet());
		newPar.setSbiParuses(new HashSet());
		newPar.setFunctionalFlag(param.getFunctionalFlag());
		newPar.setTemporalFlag(param.getTemporalFlag());
		logger.debug("OUT");
		return newPar;
	}

	/**
	 * Creates a new hibernate parameter object.
	 *
	 * @param param
	 *            the param
	 * @param id
	 *            the id
	 * @return the new hibernate parameter object
	 */
	public SbiParameters makeNew(SbiParameters param, Integer id) {
		logger.debug("IN");
		SbiParameters newPar = makeNew(param);
		newPar.setParId(id);
		logger.debug("OUT");
		return newPar;
	}

	/**
	 * Creates a new hibernate viewpoint
	 *
	 * @param paruse
	 *            the paruse
	 * @return the new hibernate parameter use object
	 * @throws EMFUserError
	 */
	public SbiViewpoints makeNew(SbiViewpoints exportedViewpoint) {
		logger.debug("IN");
		SbiViewpoints newViewpoints = new SbiViewpoints();
		newViewpoints.setVpCreationDate(exportedViewpoint.getVpCreationDate());
		newViewpoints.setVpDesc(exportedViewpoint.getVpDesc());
		newViewpoints.setVpName(exportedViewpoint.getVpName());
		newViewpoints.setVpOwner(exportedViewpoint.getVpOwner());
		newViewpoints.setVpScope(exportedViewpoint.getVpScope());
		newViewpoints.setVpValueParams(exportedViewpoint.getVpValueParams());

		logger.debug("OUT");
		return newViewpoints;
	}

	// public void entitiesAssociations(SbiViewpoints exportedViewpoint, SbiViewpoints newViewpoints,Session sessionCurrDB,
	// MetadataAssociations metaAss) throws EMFUserError {
	// logger.debug("IN");
	// // overwrite existging entities
	//
	// if(exportedViewpoint.getSbiObject() != null){
	// Integer newObjId = (Integer)metaAss.getBIObjAssociation().get(exportedViewpoint.getSbiObject().getBiobjId());
	// if(newObjId != null){
	// SbiObjects newObject = (SbiObjects) sessionCurrDB.load(SbiObjects.class, newObjId);
	// newViewpoints.setSbiObject(newObject);
	// }
	// else{
	// logger.error("could not find corresponding object");
	// List params = new ArrayList();
	// params.add("SbiObj");
	// params.add("sbi Object");
	// params.add("...");
	// throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
	// }
	// }
	//
	// logger.debug("OUT");
	//
	// }

	/**
	 * Creates a new hibernate parameter use object.
	 *
	 * @param paruse
	 *            the paruse
	 * @return the new hibernate parameter use object
	 * @throws EMFUserError
	 */
	public SbiObjParview makeNew(SbiObjParview exportedObjParview, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		SbiObjParview newObjParview = new SbiObjParview();
		try {
			newObjParview.setViewLabel(exportedObjParview.getViewLabel());
			newObjParview.setProg(exportedObjParview.getProg());

			entitiesAssociations(exportedObjParview, newObjParview, sessionCurrDB, metaAss);
		} catch (EMFUserError e) {
			logger.error("Error in making new SbiObjParview starting with SbiObjParview realted to parameter "
					+ exportedObjParview.getId().getSbiObjPar().getLabel());
			throw e;
		}
		logger.debug("OUT");
		return newObjParview;
	}

	public SbiObjParview modifyExisting(SbiObjParview exportedSbiObjParview, SbiObjParview exisingSbiObjParview, Session sessionCurrDB,
			MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		try {
			exisingSbiObjParview.setViewLabel(exportedSbiObjParview.getViewLabel());
			exisingSbiObjParview.setProg(exportedSbiObjParview.getProg());
			entitiesAssociations(exportedSbiObjParview, exisingSbiObjParview, sessionCurrDB, metaAss);
		} catch (EMFUserError e) {
			logger.error("Error in updating SbiObjParview starting with SbiObjParview related to parameter "
					+ exportedSbiObjParview.getId().getSbiObjPar().getLabel());
			throw e;
		}
		logger.debug("OUT");
		return exisingSbiObjParview;
	}

	public void entitiesAssociations(SbiObjParview exportedSbiObjParview, SbiObjParview newSbiObjParview, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		if (newSbiObjParview.getId() == null)
			newSbiObjParview.setId(new SbiObjParviewId());

		if (exportedSbiObjParview.getId().getSbiObjPar() != null) {
			Integer newObjParId = (Integer) metaAss.getObjparIDAssociation().get(exportedSbiObjParview.getId().getSbiObjPar().getObjParId());
			if (newObjParId != null) {
				SbiObjPar newSbiObjPar = (SbiObjPar) sessionCurrDB.load(SbiObjPar.class, newObjParId);
				newSbiObjParview.getId().setSbiObjPar(newSbiObjPar);
			} else {
				logger.error("could not find corresponding obj par");
				List params = new ArrayList();
				params.add("Sbi_ObjPar");
				params.add("sbi_obj_parview");
				params.add("...");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		if (exportedSbiObjParview.getId().getSbiObjParFather() != null) {
			Integer newObjParFathId = (Integer) metaAss.getObjparIDAssociation().get(exportedSbiObjParview.getId().getSbiObjParFather().getObjParId());
			if (newObjParFathId != null) {
				SbiObjPar newSbiObjParFather = (SbiObjPar) sessionCurrDB.load(SbiObjPar.class, newObjParFathId);
				newSbiObjParview.getId().setSbiObjParFather(newSbiObjParFather);
			} else {
				logger.error("could not find corresponding obj par father");
				List params = new ArrayList();
				params.add("Sbi_ObjPar");
				params.add("sbi_obj_parview");
				params.add("...");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		newSbiObjParview.getId().setCompareValue(exportedSbiObjParview.getId().getCompareValue());
		newSbiObjParview.getId().setOperation(exportedSbiObjParview.getId().getOperation());

		logger.debug("OUT");

	}

	/**
	 * Creates a new hibernate parameter use object.
	 *
	 * @param paruse
	 *            the paruse
	 * @return the new hibernate parameter use object
	 * @throws EMFUserError
	 */
	public SbiObjParuse makeNew(SbiObjParuse exportedObjParuse, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		SbiObjParuse newObjParuse = new SbiObjParuse();
		try {
			newObjParuse.setLogicOperator(exportedObjParuse.getLogicOperator());
			newObjParuse.setPostCondition(exportedObjParuse.getPostCondition());
			newObjParuse.setPreCondition(exportedObjParuse.getPreCondition());
			newObjParuse.setProg(exportedObjParuse.getProg());
			newObjParuse.setFilterColumn(exportedObjParuse.getFilterColumn());
			entitiesAssociations(exportedObjParuse, newObjParuse, sessionCurrDB, metaAss);
		} catch (EMFUserError e) {
			logger.error("Error in making new SbiObjParuse starting with SbiObjParuse realted to parameter "
					+ exportedObjParuse.getId().getSbiObjPar().getLabel());
			throw e;
		}
		logger.debug("OUT");
		return newObjParuse;
	}

	public SbiObjParuse modifyExisting(SbiObjParuse exportedSbiObjParuse, SbiObjParuse exisingSbiObjParuse, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		try {
			exisingSbiObjParuse.setLogicOperator(exportedSbiObjParuse.getLogicOperator());
			exisingSbiObjParuse.setPostCondition(exportedSbiObjParuse.getPostCondition());
			exisingSbiObjParuse.setPreCondition(exportedSbiObjParuse.getPreCondition());
			exisingSbiObjParuse.setProg(exportedSbiObjParuse.getProg());
			exisingSbiObjParuse.setFilterColumn(exportedSbiObjParuse.getFilterColumn());

			entitiesAssociations(exportedSbiObjParuse, exisingSbiObjParuse, sessionCurrDB, metaAss);
		} catch (EMFUserError e) {
			logger.error("Error in updating SbiObjParuse starting with SbiObjParuse related to parameter "
					+ exportedSbiObjParuse.getId().getSbiObjPar().getLabel());
			throw e;
		}
		logger.debug("OUT");
		return exisingSbiObjParuse;
	}

	public void entitiesAssociations(SbiObjParuse exportedSbiObjParuse, SbiObjParuse newSbiObjParuse, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		if (newSbiObjParuse.getId() == null)
			newSbiObjParuse.setId(new SbiObjParuseId());

		if (exportedSbiObjParuse.getId().getSbiParuse() != null) {
			Integer newParuseId = (Integer) metaAss.getParuseIDAssociation().get(exportedSbiObjParuse.getId().getSbiParuse().getUseId());
			if (newParuseId != null) {
				SbiParuse newSbiParuse = (SbiParuse) sessionCurrDB.load(SbiParuse.class, newParuseId);
				newSbiObjParuse.getId().setSbiParuse(newSbiParuse);
			} else {
				logger.error("could not find corresponding modality use");
				List params = new ArrayList();
				params.add("sbi_paruse");
				params.add("sbi_obj_paruse");
				params.add("...");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}

		}

		if (exportedSbiObjParuse.getId().getSbiObjPar() != null) {
			Integer newObjParId = (Integer) metaAss.getObjparIDAssociation().get(exportedSbiObjParuse.getId().getSbiObjPar().getObjParId());
			if (newObjParId != null) {
				SbiObjPar newSbiObjPar = (SbiObjPar) sessionCurrDB.load(SbiObjPar.class, newObjParId);
				newSbiObjParuse.getId().setSbiObjPar(newSbiObjPar);
			} else {
				logger.error("could not find corresponding obj par");
				List params = new ArrayList();
				params.add("Sbi_Obj_Par");
				params.add("sbi_obj_paruse");
				params.add("...");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		if (exportedSbiObjParuse.getId().getSbiObjParFather() != null) {
			Integer newObjParFathId = (Integer) metaAss.getObjparIDAssociation().get(exportedSbiObjParuse.getId().getSbiObjParFather().getObjParId());
			if (newObjParFathId != null) {
				SbiObjPar newSbiObjParFather = (SbiObjPar) sessionCurrDB.load(SbiObjPar.class, newObjParFathId);
				newSbiObjParuse.getId().setSbiObjParFather(newSbiObjParFather);
			} else {
				logger.error("could not find corresponding obj par father");
				List params = new ArrayList();
				params.add("father Sbi_Obj_Par");
				params.add("sbi_obj_paruse");
				params.add("...");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		newSbiObjParuse.getId().setFilterOperation(exportedSbiObjParuse.getId().getFilterOperation());

		logger.debug("OUT");

	}

	/**
	 * Creates a new hibernate parameter use object.
	 *
	 * @param paruse
	 *            the paruse
	 * @return the new hibernate parameter use object
	 * @throws EMFUserError
	 */
	public SbiParuse makeNew(SbiParuse exportedParuse, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		SbiParuse newParuse = new SbiParuse();
		try {
			newParuse.setDescr(exportedParuse.getDescr());
			newParuse.setLabel(exportedParuse.getLabel());
			newParuse.setName(exportedParuse.getName());
			newParuse.setSbiLov(exportedParuse.getSbiLov());
			newParuse.setSbiLovForDefault(exportedParuse.getSbiLovForDefault());
			newParuse.setDefaultFormula(exportedParuse.getDefaultFormula());
			newParuse.setSbiParameters(exportedParuse.getSbiParameters());
			newParuse.setSbiParuseCks(new HashSet());
			newParuse.setSbiParuseDets(new HashSet());
			newParuse.setManualInput(exportedParuse.getManualInput());
			newParuse.setMaximizerEnabled(exportedParuse.getMaximizerEnabled());
			newParuse.setSelectionType(exportedParuse.getSelectionType());
			newParuse.setMultivalue(exportedParuse.getMultivalue());

			entitiesAssociations(exportedParuse, newParuse, sessionCurrDB, metaAss);
		} catch (EMFUserError e) {
			logger.error("Error in making new SbiParuse with id " + exportedParuse.getLabel() + " of parameter " + exportedParuse.getSbiParameters().getLabel());
			throw e;
		}
		logger.debug("OUT");
		return newParuse;
	}

	public SbiParuse modifyExisting(SbiParuse exportedParuse, SbiParuse exisingParuse, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		try {
			exisingParuse.setDescr(exportedParuse.getDescr());
			exisingParuse.setLabel(exportedParuse.getLabel());
			exisingParuse.setName(exportedParuse.getName());
			exisingParuse.setSbiLov(exportedParuse.getSbiLov());
			exisingParuse.setSbiLovForDefault(exportedParuse.getSbiLovForDefault());
			exisingParuse.setDefaultFormula(exportedParuse.getDefaultFormula());
			exisingParuse.setSbiParameters(exportedParuse.getSbiParameters());
			exisingParuse.setSbiParuseCks(new HashSet());
			exisingParuse.setSbiParuseDets(new HashSet());
			exisingParuse.setManualInput(exportedParuse.getManualInput());
			exisingParuse.setMaximizerEnabled(exportedParuse.getMaximizerEnabled());
			exisingParuse.setSelectionType(exportedParuse.getSelectionType());
			exisingParuse.setMultivalue(exportedParuse.getMultivalue());

			entitiesAssociations(exportedParuse, exisingParuse, sessionCurrDB, metaAss);

		} catch (EMFUserError e) {
			logger.error("Error in updating new SbiParuse with id " + exportedParuse.getLabel() + " of parameter "
					+ exportedParuse.getSbiParameters().getLabel());
			throw e;
		}
		logger.debug("OUT");
		return exisingParuse;
	}

	public void entitiesAssociations(SbiParuse exportedSbiParuse, SbiParuse newSbiParuse, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		if (exportedSbiParuse.getSbiParameters() != null) {
			Integer newParId = (Integer) metaAss.getParameterIDAssociation().get(exportedSbiParuse.getSbiParameters().getParId());
			if (newParId != null) {
				SbiParameters newSbiPar = (SbiParameters) sessionCurrDB.load(SbiParameters.class, newParId);
				newSbiParuse.setSbiParameters(newSbiPar);
			} else {
				logger.error("could not find corresponding parameter");
				List params = new ArrayList();
				params.add("Sbi_Parameter");
				params.add("sbi_Paruse");
				params.add(newSbiParuse.getLabel());
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		if (exportedSbiParuse.getSbiLov() != null) {
			Integer newLovId = (Integer) metaAss.getLovIDAssociation().get(exportedSbiParuse.getSbiLov().getLovId());
			if (newLovId != null) {
				SbiLov newSbiLov = (SbiLov) sessionCurrDB.load(SbiLov.class, newLovId);
				newSbiParuse.setSbiLov(newSbiLov);
			} else {
				logger.error("could not find corresponding Lov");
				List params = new ArrayList();
				params.add("Sbi_Lov");
				params.add("sbi_Paruse");
				params.add(newSbiParuse.getLabel());
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		if (exportedSbiParuse.getSbiLovForDefault() != null) {
			Integer newLovId = (Integer) metaAss.getLovIDAssociation().get(exportedSbiParuse.getSbiLovForDefault().getLovId());
			if (newLovId != null) {
				SbiLov newSbiLov = (SbiLov) sessionCurrDB.load(SbiLov.class, newLovId);
				newSbiParuse.setSbiLovForDefault(newSbiLov);
			} else {
				logger.error("could not find corresponding Lov for default");
				List params = new ArrayList();
				params.add("Sbi_Lov");
				params.add("sbi_Paruse");
				params.add(newSbiParuse.getLabel());
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}
		logger.debug("out");
	}

	/**
	 * Creates a new hibernate parameter use object.
	 *
	 * @param paruse
	 *            the paruse
	 * @param id
	 *            the id
	 * @return the new hibernate parameter use object
	 */
	// public SbiParuse makeNewSbiParuse(SbiParuse paruse, Integer id){
	// logger.debug("IN");
	// SbiParuse newParuse = makeNewSbiParuse(paruse);
	// newParuse.setUseId(id);
	// logger.debug("OUT");
	// return newParuse;
	// }

	/**
	 * Creates a new hibernate biobject.
	 *
	 * @param obj
	 *            old hibernate biobject
	 * @return the new hibernate biobject
	 */
	public SbiObjects makeNew(SbiObjects obj) {
		logger.debug("IN");
		SbiObjects newObj = new SbiObjects();
		newObj.setDescr(obj.getDescr());
		newObj.setEncrypt(obj.getEncrypt());
		newObj.setExecMode(obj.getExecMode());
		newObj.setExecModeCode(obj.getExecModeCode());
		newObj.setLabel(obj.getLabel());
		newObj.setName(obj.getName());
		newObj.setObjectType(obj.getObjectType());
		// newObj.setObjectTypeCode(obj.getObjectTypeCode());
		newObj.setPath(obj.getPath());
		newObj.setRelName(obj.getRelName());
		// newObj.setSbiEngines(obj.getSbiEngines());
		newObj.setSbiObjPars(new HashSet());
		newObj.setSbiObjFuncs(new HashSet());
		newObj.setSbiObjStates(new HashSet());
		newObj.setSchedFl(obj.getSchedFl());
		newObj.setState(obj.getState());
		newObj.setStateCode(obj.getStateCode());
		newObj.setStateConsideration(obj.getStateConsideration());
		newObj.setStateConsiderationCode(obj.getStateConsiderationCode());
		newObj.setVisible(obj.getVisible());
		newObj.setProfiledVisibility(obj.getProfiledVisibility());
		newObj.setUuid(obj.getUuid());
		newObj.setCreationDate(obj.getCreationDate());
		newObj.setCreationUser(obj.getCreationUser());
		newObj.setRefreshSeconds(obj.getRefreshSeconds());
		newObj.setParametersRegion(obj.getParametersRegion());

		// newObj.setDataSource(obj.getDataSource());
		logger.debug("OUT");
		return newObj;
	}

	/**
	 * Make new sbi snapshots.
	 *
	 * @param obj
	 *            the obj
	 * @return the sbi snapshots
	 */
	public SbiSnapshots makeNew(SbiSnapshots obj) {
		logger.debug("IN");
		SbiSnapshots newObj = new SbiSnapshots();
		newObj.setCreationDate(obj.getCreationDate());
		newObj.setDescription(obj.getDescription());
		newObj.setName(obj.getName());
		newObj.setContentType(obj.getContentType());
		logger.debug("OUT");
		return newObj;
	}

	/**
	 * Make new sbi sub objects.
	 *
	 * @param obj
	 *            the obj
	 * @return the sbi sub objects
	 */
	public SbiSubObjects makeNew(SbiSubObjects obj) {
		logger.debug("IN");
		SbiSubObjects newObj = new SbiSubObjects();
		newObj.setCreationDate(obj.getCreationDate());
		newObj.setDescription(obj.getDescription());
		newObj.setIsPublic(obj.getIsPublic());
		newObj.setLastChangeDate(obj.getLastChangeDate());
		newObj.setName(obj.getName());
		newObj.setOwner(obj.getOwner());
		logger.debug("OUT");
		return newObj;
	}

	/**
	 * Make new sbi obj templates.
	 *
	 * @param obj
	 *            the obj
	 * @return the sbi obj templates
	 */
	public SbiObjTemplates makeNew(SbiObjTemplates obj) {
		logger.debug("IN");
		SbiObjTemplates newObj = new SbiObjTemplates();
		newObj.setActive(obj.getActive());
		newObj.setCreationDate(obj.getCreationDate());
		newObj.setCreationUser(obj.getCreationUser());
		newObj.setName(obj.getName());
		newObj.setProg(obj.getProg());
		newObj.setDimension(obj.getDimension());
		logger.debug("OUT");
		return newObj;
	}

	/**
	 * Make new sbi bin contents.
	 *
	 * @param obj
	 *            the obj
	 * @return the sbi bin contents
	 */
	public SbiBinContents makeNew(SbiBinContents obj) {
		logger.debug("IN");
		SbiBinContents newObj = new SbiBinContents();
		newObj.setContent(obj.getContent());
		logger.debug("OUT");
		return newObj;
	}

	/**
	 * Creates a new hibernate biobject.
	 *
	 * @param obj
	 *            old hibernate biobject
	 * @param id
	 *            the id
	 * @return the new hibernate biobject
	 */
	public SbiObjects makeNew(SbiObjects obj, Integer id) {
		logger.debug("IN");
		SbiObjects newObj = makeNew(obj);
		newObj.setBiobjId(id);
		logger.debug("OUT");
		return newObj;
	}

	/**
	 * Load an existing biobject and make modifications as per the exported biobject in input (existing associations with functionalities are maintained, while
	 * existing associations with parameters are deleted).
	 *
	 * @param exportedObj
	 *            the exported obj
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing biobject modified as per the exported biobject in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiObjects modifyExisting(SbiObjects exportedObj, Session sessionCurrDB, Integer existingId) throws EMFUserError {
		logger.debug("IN");
		SbiObjects existingObj = null;
		try {
			// update document
			existingObj = (SbiObjects) sessionCurrDB.load(SbiObjects.class, existingId);
			existingObj.setName(exportedObj.getName());
			existingObj.setDescr(exportedObj.getDescr());
			existingObj.setLabel(exportedObj.getLabel());
			existingObj.setExecModeCode(exportedObj.getExecModeCode());
			existingObj.setObjectTypeCode(exportedObj.getObjectTypeCode());
			existingObj.setPath(exportedObj.getPath());
			existingObj.setRelName(exportedObj.getRelName());
			existingObj.setStateConsiderationCode(exportedObj.getStateConsiderationCode());
			existingObj.setUuid(exportedObj.getUuid());
			existingObj.setEncrypt(exportedObj.getEncrypt());
			existingObj.setSbiEngines(exportedObj.getSbiEngines());
			existingObj.setSchedFl(exportedObj.getSchedFl());
			existingObj.setSbiObjStates(new HashSet());
			existingObj.setVisible(exportedObj.getVisible());
			existingObj.setProfiledVisibility(exportedObj.getProfiledVisibility());
			existingObj.setRefreshSeconds(exportedObj.getRefreshSeconds());

			// deletes existing associations between object and parameters
			// NO MORE

			Set objPars = existingObj.getSbiObjPars();

			/*
			 *
			 * Iterator objParsIt = objPars.iterator(); while (objParsIt.hasNext()) { SbiObjPar objPar = (SbiObjPar) objParsIt.next(); // for each
			 * biobjectparameter deletes all its dependencies, if any Query query =
			 * sessionCurrDB.createQuery(" from SbiObjParuse where id.sbiObjPar.objParId = " + objPar.getObjParId()); logger.debug("delete dependencies"); List
			 * dependencies = query.list(); if (dependencies != null && !dependencies.isEmpty()) { Iterator it = dependencies.iterator(); while (it.hasNext()) {
			 * SbiObjParuse aSbiObjParuse = (SbiObjParuse) it.next(); sessionCurrDB.delete(aSbiObjParuse); } }
			 *
			 * // for each biobjectparameter deletes all its visual dependencies, if any Query visQuery =
			 * sessionCurrDB.createQuery(" from SbiObjParview where id.sbiObjPar.objParId = " + objPar.getObjParId());
			 * logger.debug("delete visual dependencies");
			 *
			 * List visdependencies = visQuery.list(); if (visdependencies != null && !visdependencies.isEmpty()) { Iterator it = visdependencies.iterator();
			 * while (it.hasNext()) { SbiObjParview aSbiObjParview = (SbiObjParview) it.next();
			 * logger.debug("Delete parView "+aSbiObjParview.getId().getSbiObjPar().getLabel()); sessionCurrDB.delete(aSbiObjParview); } } } // delete par only
			 * after having deleted alll parviews otherwise constraint fails Iterator objParsItAgain = objPars.iterator(); while (objParsItAgain.hasNext()){
			 * SbiObjPar objPar = (SbiObjPar) objParsItAgain.next(); logger.debug("delete objPar with label "+objPar.getLabel()); sessionCurrDB.delete(objPar);
			 * }
			 */

		} finally {
			logger.debug("OUT");
		}
		return existingObj;
	}

	/**
	 * Load an existing parameter and make modifications as per the exported parameter in input (existing associations with biobjects are maintained, while
	 * parameter uses are deleted).
	 *
	 * @param exportedParameter
	 *            the exported parameter
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing parameter modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiParameters modifyExisting(SbiParameters exportedParameter, Session sessionCurrDB, Integer existingId) throws EMFUserError {
		logger.debug("IN");
		SbiParameters existingPar = null;
		try {
			// update parameter
			existingPar = (SbiParameters) sessionCurrDB.load(SbiParameters.class, existingId);
			existingPar.setDescr(exportedParameter.getDescr());
			existingPar.setLabel(exportedParameter.getLabel());
			existingPar.setLength(exportedParameter.getLength());
			existingPar.setMask(exportedParameter.getMask());
			existingPar.setName(exportedParameter.getName());
			existingPar.setSbiParuses(new HashSet());
			existingPar.setFunctionalFlag(exportedParameter.getFunctionalFlag());
			existingPar.setTemporalFlag(exportedParameter.getTemporalFlag());
			// deletes existing associations between object and parameters
			/*
			 * NO MORE Set paruses = existingPar.getSbiParuses(); Iterator parusesIt = paruses.iterator(); while (parusesIt.hasNext()) { SbiParuse paruse =
			 * (SbiParuse) parusesIt.next(); sessionCurrDB.delete(paruse); }
			 */
		} finally {
			logger.debug("OUT");
		}
		return existingPar;
	}

	/**
	 * Creates a new hibernate biobject dataset object.
	 *
	 * @param objDs
	 *            the objDs
	 * @return the sbi obj dataset
	 * @throws EMFUserError
	 */
	public SbiObjDataSet makeNew(SbiObjDataSet expObjDataSet, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		SbiObjDataSet newObjDataSet = new SbiObjDataSet();
		try {

			newObjDataSet.setIsDetail(expObjDataSet.isIsDetail());
			entitiesAssociations(expObjDataSet, newObjDataSet, sessionCurrDB, metaAss);
		} catch (EMFUserError e) {
			logger.error("Error while modifying creating new objDataset", e);
			throw e;
		}
		logger.debug("OUT");
		return newObjDataSet;
	}

	public void entitiesAssociations(SbiObjDataSet exportedSbiObjDs, SbiObjDataSet newSbiObjDs, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		if (exportedSbiObjDs.getSbiObject() != null) {
			Integer objId = exportedSbiObjDs.getSbiObject().getBiobjId();

			Integer newObjId = (Integer) metaAss.getBIobjIDAssociation().get(exportedSbiObjDs.getSbiObject().getBiobjId());
			if (newObjId != null) {
				SbiObjects sbiObject = (SbiObjects) sessionCurrDB.load(SbiObjects.class, newObjId);
				newSbiObjDs.setSbiObject(sbiObject);
			} else {
				logger.error("could not find corresponding BiObject");
				List params = new ArrayList();
				params.add("Sbi_Object");
				params.add("sbi_obj_ds");
				params.add(exportedSbiObjDs.getDsId());
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		if (exportedSbiObjDs.getDsId() != null) {
			Integer oldDsId = exportedSbiObjDs.getDsId();

			Integer newDsId = (Integer) metaAss.getDataSetIDAssociation().get(exportedSbiObjDs.getDsId());

			if (newDsId != null) {
				// SbiDataSet sbiParameters = (SbiDataSet) sessionCurrDB.load(SbiDataSet.class, newDsId);
				newSbiObjDs.setDsId(newDsId);
			} else {
				logger.error("could not find corresponding dataset");
				List params = new ArrayList();
				params.add("Sbi_dataset");
				params.add("sbi_obj_dataset");
				params.add(exportedSbiObjDs.getBiObjDsId());
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		logger.debug("OUT");
	}

	/**
	 * Creates a new hibernate biobject parameter object.
	 *
	 * @param objpar
	 *            the objpar
	 * @return the sbi obj par
	 * @throws EMFUserError
	 */
	public SbiObjPar makeNew(SbiObjPar expObjpar, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		SbiObjPar newObjPar = new SbiObjPar();
		try {

			newObjPar.setLabel(expObjpar.getLabel());
			newObjPar.setModFl(expObjpar.getModFl());
			newObjPar.setMultFl(expObjpar.getMultFl());
			newObjPar.setParurlNm(expObjpar.getParurlNm());
			newObjPar.setPriority(expObjpar.getPriority());
			newObjPar.setProg(expObjpar.getProg());
			newObjPar.setReqFl(expObjpar.getReqFl());
			newObjPar.setSbiObject(expObjpar.getSbiObject());
			newObjPar.setSbiParameter(expObjpar.getSbiParameter());
			newObjPar.setViewFl(expObjpar.getViewFl());
			newObjPar.setColSpan(expObjpar.getColSpan());
			newObjPar.setThickPerc(expObjpar.getThickPerc());

			entitiesAssociations(expObjpar, newObjPar, sessionCurrDB, metaAss);
		} catch (EMFUserError e) {
			logger.error("Error while modifying creating new par object", e);
			throw e;
		}
		logger.debug("OUT");
		return newObjPar;
	}

	/**
	 * Creates a new hibernate biobject parameter object.
	 *
	 * @param objpar
	 *            the objpar
	 * @param id
	 *            the id
	 * @return the sbi obj par
	 * @throws EMFUserError
	 */
	public SbiObjPar makeNew(SbiObjPar objpar, Integer id, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		SbiObjPar newObjPar = makeNew(objpar, sessionCurrDB, metaAss);
		newObjPar.setObjParId(id);
		logger.debug("OUT");
		return newObjPar;
	}

	public SbiObjPar modifyExisting(SbiObjPar exportedSbiObjpar, SbiObjPar existingSbiObjpar, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		try {
			existingSbiObjpar.setLabel(exportedSbiObjpar.getLabel());
			existingSbiObjpar.setModFl(exportedSbiObjpar.getModFl());
			existingSbiObjpar.setMultFl(exportedSbiObjpar.getMultFl());
			existingSbiObjpar.setPriority(exportedSbiObjpar.getPriority());
			existingSbiObjpar.setProg(exportedSbiObjpar.getProg());
			existingSbiObjpar.setReqFl(exportedSbiObjpar.getReqFl());
			existingSbiObjpar.setViewFl(exportedSbiObjpar.getViewFl());
			existingSbiObjpar.setParurlNm(exportedSbiObjpar.getParurlNm());
			entitiesAssociations(exportedSbiObjpar, existingSbiObjpar, sessionCurrDB, metaAss);
		} catch (EMFUserError e) {
			logger.error("Error while modifying existing par object", e);
			throw e;
		}
		logger.debug("OUT");
		return existingSbiObjpar;
	}

	public void entitiesAssociations(SbiObjPar exportedSbiObjPar, SbiObjPar newSbiObjPar, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		if (exportedSbiObjPar.getSbiObject() != null) {
			Integer objId = exportedSbiObjPar.getSbiObject().getBiobjId();

			Integer newObjId = (Integer) metaAss.getBIobjIDAssociation().get(exportedSbiObjPar.getSbiObject().getBiobjId());
			if (newObjId != null) {
				SbiObjects sbiObject = (SbiObjects) sessionCurrDB.load(SbiObjects.class, newObjId);
				newSbiObjPar.setSbiObject(sbiObject);
			} else {
				logger.error("could not find corresponding BiObject");
				List params = new ArrayList();
				params.add("Sbi_Object");
				params.add("sbi_obj_par");
				params.add(exportedSbiObjPar.getLabel());
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		if (exportedSbiObjPar.getSbiParameter() != null) {
			Integer parId = exportedSbiObjPar.getSbiParameter().getParId();

			Integer newParId = (Integer) metaAss.getParameterIDAssociation().get(exportedSbiObjPar.getSbiParameter().getParId());
			if (newParId != null) {
				SbiParameters sbiParameters = (SbiParameters) sessionCurrDB.load(SbiParameters.class, newParId);
				newSbiObjPar.setSbiParameter(sbiParameters);
			} else {
				logger.error("could not find corresponding BiParameters");
				List params = new ArrayList();
				params.add("Sbi_Parameter");
				params.add("sbi_obj_par");
				params.add(exportedSbiObjPar.getLabel());
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		logger.debug("OUT");
	}

	/**
	 * Set into the biobject to the engine/object type/object state/datasource the entities associated with the exported biobject.
	 *
	 * @param obj
	 *            the obj
	 * @param exportedObj
	 *            the exported obj
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param importer
	 *            the importer
	 * @param metaAss
	 *            the meta ass
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void associateWithExistingEntities(SbiObjects obj, SbiObjects exportedObj, Session sessionCurrDB, ImporterMetadata importer,
			MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		try {
			// reading exist engine
			SbiEngines engine = getAssociatedSbiEngine(exportedObj, sessionCurrDB, metaAss);
			obj.setSbiEngines(engine);
			// reading exist object type
			SbiDomains existDom = getAssociatedBIObjectType(exportedObj, sessionCurrDB, importer);
			if (existDom != null) {
				obj.setObjectType(existDom);
				obj.setObjectTypeCode(existDom.getValueCd());
			}
			// reading exist state
			SbiDomains existDomSt = getAssociatedBIObjectState(exportedObj, sessionCurrDB, importer);
			if (existDomSt != null) {
				obj.setState(existDomSt);
				obj.setStateCode(existDomSt.getValueCd());
			}
			// reading exist datasource
			SbiDataSource localDS = getAssociatedSbiDataSource(exportedObj, sessionCurrDB, metaAss);
			if (localDS != null)
				obj.setDataSource(localDS);
			// reading exist datasset; now added later
			// SbiDataSet localDataSet = getAssociatedSbiDataSet(exportedObj, sessionCurrDB, metaAss);
			// if (localDataSet != null) {
			// obj.setDataSet(localDataSet.getId().getDsId());
			// }
		} finally {
			logger.debug("OUT");
		}
	}

	private SbiDataSource getAssociatedSbiDataSource(SbiObjects exportedObj, Session sessionCurrDB, MetadataAssociations metaAss) {
		logger.debug("IN");
		SbiDataSource expDs = exportedObj.getDataSource();
		if (expDs != null) {
			Integer existingDsId = (Integer) metaAss.getDataSourceIDAssociation().get(new Integer(expDs.getDsId()));
			// if (label == null) {
			// // exist a DataSource Association, read a new DataSource
			// // from the DB
			// label = expDs.getLabel();
			// }
			// Criterion labelCriterrion = Expression.eq("label", label);
			// Criteria criteria = sessionCurrDB.createCriteria(SbiDataSource.class);
			// criteria.add(labelCriterrion);
			// SbiDataSource localDS = (SbiDataSource) criteria.uniqueResult();
			SbiDataSource localDS = (SbiDataSource) sessionCurrDB.load(SbiDataSource.class, existingDsId);
			logger.debug("OUT");
			return localDS;
		} else {
			logger.debug("OUT");
			return null;
		}

	}

	// private SbiDataSet getAssociatedSbiDataSet(SbiObjects exportedObj, Session sessionCurrDB, MetadataAssociations metaAss) {
	// logger.debug("IN");
	// SbiDataSet localDS = null;
	// Integer expDataset = exportedObj.getDataSet();
	//
	// if (expDataset != null) {
	// Integer existingDatasetId = (Integer) metaAss.getDataSetIDAssociation().get(expDataset);
	// Query hqlQuery = sessionCurrDB.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
	// hqlQuery.setBoolean(0, true);
	// hqlQuery.setInteger(1, existingDatasetId);
	// // hqlQuery.setInteger(1, expDataset);
	// localDS = (SbiDataSet) hqlQuery.uniqueResult();
	// if (datasetMap.get(localDS.getId().getDsId()) == null) {
	// datasetMap.put(localDS.getId().getDsId(), localDS);
	// }
	//
	// } else {
	// logger.debug("no dataset associated to document");
	// }
	//
	// if (localDS != null) {
	// logger.debug("OUT");
	// return localDS;
	// } else {
	// logger.debug("OUT");
	// return null;
	// }

	/*
	 * orig: SbiDataSetConfig expDataset = exportedObj.getDataSet(); if (expDataset != null) { Integer existingDatasetId = (Integer)
	 * metaAss.getDataSetIDAssociation().get(new Integer(expDataset.getDsId())); SbiDataSetConfig localDS = (SbiDataSetConfig)
	 * sessionCurrDB.load(SbiDataSetConfig.class, existingDatasetId); logger.debug("OUT"); return localDS; } else { logger.debug("OUT"); return null; }
	 */
	// }

	private SbiDomains getAssociatedBIObjectState(SbiObjects exportedObj, Session sessionCurrDB, ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		String stateCd = exportedObj.getStateCode();
		Map unique = new HashMap();
		unique.put("valuecd", stateCd);
		unique.put("domaincd", "STATE");
		SbiDomains existDomSt = (SbiDomains) importer.checkExistence(unique, sessionCurrDB, new SbiDomains());
		logger.debug("OUT");
		return existDomSt;
	}

	private SbiDomains getAssociatedBIObjectType(SbiObjects exportedObj, Session sessionCurrDB, ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		String typeCd = exportedObj.getObjectTypeCode();
		Map unique = new HashMap();
		unique.put("valuecd", typeCd);
		unique.put("domaincd", "BIOBJ_TYPE");
		SbiDomains existDom = (SbiDomains) importer.checkExistence(unique, sessionCurrDB, new SbiDomains());
		logger.debug("OUT");
		return existDom;
	}

	private SbiDomains getAssociatedDomainForKpi(String domainCode, String typeDomain, Session sessionCurrDB, ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		Map unique = new HashMap();
		unique.put("valuecd", domainCode);
		// could be STATE, KPI_CHART, MODALITY ecc
		unique.put("domaincd", typeDomain);
		SbiDomains existDomSt = (SbiDomains) importer.checkExistence(unique, sessionCurrDB, new SbiDomains());
		logger.debug("OUT");
		return existDomSt;
	}

	private SbiEngines getAssociatedSbiEngine(SbiObjects exportedObj, Session sessionCurrDB, MetadataAssociations metaAss) {
		logger.debug("IN");
		SbiEngines existingEngine = null;
		SbiEngines engine = exportedObj.getSbiEngines();
		Integer expEngId = engine.getEngineId();
		Map assEngs = metaAss.getEngineIDAssociation();
		Integer existingId = (Integer) assEngs.get(expEngId);
		if (existingId != null) {
			existingEngine = (SbiEngines) sessionCurrDB.load(SbiEngines.class, existingId);
		}
		logger.debug("OUT");
		return existingEngine;
	}

	private SbiDataSource getAssociatedSbiDataSource(SbiDataSet exportedDataset, Session sessionExpDB, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) {
		logger.debug("IN");
		SbiDataSource toReturn = null;
		// JSONObject jsonConf = ObjectUtils.toJSONObject(exportedDataset.getConfiguration());
		String config = JSONUtils.escapeJsonString(exportedDataset.getConfiguration());
		JSONObject jsonConf = ObjectUtils.toJSONObject(config);
		try {

			String dataSourceLabel = jsonConf.getString(DataSetConstants.DATA_SOURCE);

			if (dataSourceLabel != null) {

				// CHeck if this datasource has been mapped to another one, maybe with different label because of user selection
				Query hqlQueryExp = sessionExpDB.createQuery("from SbiDataSource h where h.label = '" + dataSourceLabel + "'");
				SbiDataSource dataSourceExp = (SbiDataSource) hqlQueryExp.uniqueResult();

				if (dataSourceExp != null) {
					Integer expId = dataSourceExp.getDsId();
					Integer newId = metaAss.getDataSourceIDAssociation().get(expId) != null ? (Integer) metaAss.getDataSourceIDAssociation().get(expId) : 0;

					Query hqlQuery = null;
					if (newId != null) {
						logger.debug("Dataset with label " + exportedDataset.getLabel() + " is associated with datasource with label " + dataSourceLabel
								+ " that in import db has been mapped with id " + newId);
						hqlQuery = sessionCurrDB.createQuery("from SbiDataSource h where h.dsId = " + newId);
					} else {
						logger.debug("Datasource with label " + dataSourceLabel
								+ " was not mapped into another one with differnet label; can check same label one");
						hqlQuery = sessionCurrDB.createQuery("from SbiDataSource h where h.label = '" + dataSourceLabel + "'");
					}

					logger.debug("To dataset " + exportedDataset.getLabel() + " is associated datasource with label " + dataSourceLabel + ": recover it");

					SbiDataSource foundDataSource = (SbiDataSource) hqlQuery.uniqueResult();
					if (foundDataSource != null) {
						logger.debug("Datasource with label " + dataSourceLabel + " found");
						toReturn = foundDataSource;
					} else {
						logger.error("DataSource with label " + dataSourceLabel + " was not found in current DB; this should not happen: ignore association");

					}
				}
			}

			// // THis case should never happened, this insert is not needed
			// logger.debug("Datasource with label "+dataSourceLabel+" was not found; create it");
			// //get the exported one
			// Query hqlQueryExp = sessionExpDB.createQuery("from SbiDataSource h where h.label = '"+dataSourceLabel+"'");
			// SbiDataSource dataSourceExp = (SbiDataSource)hqlQueryExp.uniqueResult();
			// if(dataSourceExp!= null){
			// SbiDataSource newDS = makeNew(dataSourceExp);
			// associateWithExistingEntities(newDS, dataSourceExp, sessionCurrDB, importer, metaAss);
			// Integer newId = (Integer) sessionCurrDB.save(newDS);
			// sessionCurrDB.flush();
			// metaLog.log("Inserted new datasource " + newDS.getLabel());
			// logger.debug("Inserted new datasource " + newDS.getLabel());
			// metaAss.insertCoupleDataSources(dataSourceExp.getDsId(), newId);
			// }
			// else{
			// logger.error("DataSource with label "+dataSourceLabel+" was not found in export DB; ignore association");
			// }
		} catch (Exception e) {
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Set into the parameter to the parameter type domain associated with the exported parameter.
	 *
	 * @param parameter
	 *            the parameter
	 * @param exportedParameter
	 *            the exported parameter
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param importer
	 *            the importer
	 * @param metaAss
	 *            the meta ass
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void associateWithExistingEntities(SbiParameters parameter, SbiParameters exportedParameter, Session sessionCurrDB, ImporterMetadata importer,
			MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		try {
			// reading existing parameter type
			SbiDomains existDom = getAssociatedParameterType(exportedParameter, sessionCurrDB, metaAss, importer);
			if (existDom != null) {
				parameter.setParameterType(existDom);
				parameter.setParameterTypeCode(existDom.getValueCd());
			}
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Make new data set.
	 *
	 * @param dataProxy
	 *            the ds
	 * @return the sbi data set public SbiDataSet makeNew(SbiDataSet dataset, IEngUserProfile profile){ logger.debug("IN"); SbiDataSet newDsConfig = new
	 *         SbiDataSet(); newDsConfig.setLabel(dataset.getLabel()); newDsConfig.setName(dataset.getName());
	 *         newDsConfig.setDescription(dataset.getDescription()); SbiCommonInfo i = new SbiCommonInfo(); String userid = "biadmin"; if(profile!=null){ userid
	 *         =(String) profile.getUserUniqueIdentifier(); } i.setTimeIn(new Date()); i.setUserIn(userid); i.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
	 *         newDsConfig.setCommonInfo(i); logger.debug("OUT"); return newDsConfig; }
	 */

	/**
	 * Make new Artifact.
	 *
	 * @param dataProxy
	 *            the ds
	 * @return the sbi Artifact
	 */
	public SbiArtifact makeNew(SbiArtifact exportedArtifact, Session sessionCurrDB, IEngUserProfile profile) {
		logger.debug("IN");
		SbiArtifact newArtifact = new SbiArtifact();

		newArtifact.setName(exportedArtifact.getName());
		newArtifact.setDescription(exportedArtifact.getDescription());
		newArtifact.setType(exportedArtifact.getType());

		SbiCommonInfo i = new SbiCommonInfo();
		String userid = "biadmin";
		if (profile != null) {
			userid = (String) profile.getUserUniqueIdentifier();
		}
		i.setTimeIn(new Date());
		i.setUserIn(userid);
		i.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);

		newArtifact.setCommonInfo(i);

		// sessionCurrDB.save(newDataset);

		logger.debug("OUT");
		return newArtifact;
	}

	public SbiFederationDefinition makeNew(SbiFederationDefinition exportedFederationDefinition, Session sessionCurrDB, IEngUserProfile profile) {
		logger.debug("IN");
		SbiFederationDefinition newFederationDefinition = new SbiFederationDefinition();

		newFederationDefinition.setLabel(exportedFederationDefinition.getLabel());
		newFederationDefinition.setName(exportedFederationDefinition.getName());
		newFederationDefinition.setDescription(exportedFederationDefinition.getDescription());
		newFederationDefinition.setRelationships(exportedFederationDefinition.getRelationships());

		SbiCommonInfo i = new SbiCommonInfo();
		String userid = "biadmin";
		if (profile != null) {
			userid = (String) profile.getUserUniqueIdentifier();
		}
		i.setTimeIn(new Date());
		i.setUserIn(userid);
		i.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);

		newFederationDefinition.setCommonInfo(i);

		// sessionCurrDB.save(newDataset);

		logger.debug("OUT");
		return newFederationDefinition;
	}

	public SbiFederationDefinition modifyExisting(SbiFederationDefinition exportedFederationDefinition, Session sessionCurrDB, Integer existingId,
			Session sessionExpDB, IEngUserProfile profile) throws Exception {
		logger.debug("IN");
		SbiFederationDefinition existFederationDefinition = null;
		try {
			Query hibQueryFederationDefinition = sessionCurrDB.createQuery("from SbiFederationDefinition h where h.federation_id= ?");
			hibQueryFederationDefinition.setInteger(0, existingId);
			existFederationDefinition = (SbiFederationDefinition) hibQueryFederationDefinition.uniqueResult();

			existFederationDefinition.setLabel(exportedFederationDefinition.getLabel());
			existFederationDefinition.setName(exportedFederationDefinition.getName());
			existFederationDefinition.setDescription(exportedFederationDefinition.getDescription());
			existFederationDefinition.setRelationships(exportedFederationDefinition.getRelationships());

			SbiCommonInfo i = new SbiCommonInfo();
			String userid = "biadmin";
			if (profile != null) {
				userid = (String) profile.getUserUniqueIdentifier();
			}
			i.setTimeIn(new Date());
			i.setUserIn(userid);
			i.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
			existFederationDefinition.setCommonInfo(i);

			// sessionCurrDB.update(existFederationDefinition);

		} catch (Exception e) {
			logger.error("Error in modifying exported FederationDefinition " + exportedFederationDefinition.getName(), e);
			throw e;
		} finally {
			logger.debug("OUT");
		}
		return existFederationDefinition;
	}

	public SbiFederationDefinition associateWithExistingEntities(SbiFederationDefinition newFederationDefinition,
			SbiFederationDefinition exportedFederationDefinition, Session sessionExpDB, Session sessionCurrDB, ImporterMetadata importer,
			MetadataAssociations metaAss, Integer existingFederationId) throws EMFUserError {

		logger.debug("IN");

		if (existingFederationId != null) {
			// delete previous association of the modify federation
			Query query = sessionCurrDB.createQuery(" from SbiDataSetFederation a where a.id.federationId = " + existingFederationId);

			List contents = query.list();
			if (contents != null) {
				Iterator it = contents.iterator();
				while (it.hasNext()) {
					SbiDataSetFederation sbiDataSetFederation = (SbiDataSetFederation) it.next();
					sessionCurrDB.delete(sbiDataSetFederation);
				}
			}
		}

		sessionCurrDB.clear();

		Set<SbiDataSet> previousSbiDataSets = exportedFederationDefinition.getSourceDatasets();

		Set<SbiDataSet> newDataSets = new HashSet<SbiDataSet>();

		for (Iterator iterator = previousSbiDataSets.iterator(); iterator.hasNext();) {
			SbiDataSet sbiDataSet = (SbiDataSet) iterator.next();
			Integer newDsId = (Integer) metaAss.getDataSetIDAssociation().get(sbiDataSet.getId().getDsId());
			SbiDataSet sbiDS = DAOFactory.getDataSetDAO().loadSbiDataSetById(newDsId, sessionCurrDB);
			newDataSets.add(sbiDS);
		}
		newFederationDefinition.setSourceDatasets(newDataSets);
		logger.debug("OUT");
		return newFederationDefinition;
	}

	/**
	 * Load an existing artifact and make modifications as per the exported artifact in input
	 *
	 * @param exportedartifact
	 *            the exported artifact
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing metamodel modified as per the exported dataset in input
	 * @throws EMFUserError
	 */
	public SbiArtifact modifyExisting(SbiArtifact exportedArtifact, Session sessionCurrDB, Integer existingId, Session sessionExpDB, IEngUserProfile profile)
			throws Exception {
		logger.debug("IN");
		SbiArtifact existArtifact = null;
		try {
			Query hibQueryArtifact = sessionCurrDB.createQuery("from SbiArtifact h where h.id= ?");
			hibQueryArtifact.setInteger(0, existingId);
			existArtifact = (SbiArtifact) hibQueryArtifact.uniqueResult();

			existArtifact.setName(exportedArtifact.getName());
			existArtifact.setDescription(exportedArtifact.getDescription());
			existArtifact.setType(exportedArtifact.getType());

			SbiCommonInfo i = new SbiCommonInfo();
			String userid = "biadmin";
			if (profile != null) {
				userid = (String) profile.getUserUniqueIdentifier();
			}
			i.setTimeIn(new Date());
			i.setUserIn(userid);
			i.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
			existArtifact.setCommonInfo(i);

			sessionCurrDB.update(existArtifact);

			// delete previous meta content and add new one
			Query query = sessionCurrDB.createQuery(" from SbiArtifactContent a where a.artifact.id = " + existingId);
			List contents = query.list();
			if (contents != null) {
				Iterator it = contents.iterator();
				while (it.hasNext()) {
					SbiArtifactContent sbiArtifactContent = (SbiArtifactContent) it.next();
					sessionCurrDB.delete(sbiArtifactContent);
				}
			}

		} catch (Exception e) {
			logger.error("Error in modifying exported meta model " + exportedArtifact.getName(), e);
			throw e;
		} finally {
			logger.debug("OUT");
		}
		return existArtifact;
	}

	public SbiArtifact associateWithExistingEntities(SbiArtifact newArtifact, SbiArtifact exportedArtifact, SbiArtifactContent exportedArtifactContent,
			Session sessionCurrDB, ImporterMetadata importer, MetadataAssociations metaAss) throws EMFUserError {

		logger.debug("IN");
		// No operations at the moment
		logger.debug("OUT");
		return newArtifact;
	}

	public SbiArtifact insertArtifactContent(SbiArtifact newArtifact, SbiArtifact exportedArtifact, SbiArtifactContent exportedArtifactContent,
			Session sessionCurrDB, ImporterMetadata importer, MetadataAssociations metaAss) throws EMFUserError {

		logger.debug("IN");

		SbiArtifactContent newContent = new SbiArtifactContent();
		newContent.setActive(true);
		newContent.setContent(exportedArtifactContent.getContent());
		newContent.setCreationDate(exportedArtifactContent.getCreationDate());
		newContent.setCreationUser(exportedArtifactContent.getCreationUser());
		newContent.setDimension(exportedArtifactContent.getDimension());
		newContent.setFileName(exportedArtifactContent.getFileName());
		newContent.setArtifact(newArtifact);

		newContent.setCommonInfo(newArtifact.getCommonInfo());
		sessionCurrDB.save(newContent);
		logger.debug("New Artifact Content saved");

		logger.debug("OUT");
		return newArtifact;
	}

	/**
	 * Make new Meta MOdel
	 *
	 * @param dataProxy
	 *            the ds
	 * @return the Meta Model
	 */
	public SbiMetaModel makeNew(SbiMetaModel exportedMetaModel, Session sessionCurrDB, IEngUserProfile profile) {
		logger.debug("IN");
		SbiMetaModel newMetaModel = new SbiMetaModel();

		newMetaModel.setName(exportedMetaModel.getName());
		newMetaModel.setDescription(exportedMetaModel.getDescription());

		// newMetaModel.setCategory(exportedMetaModel.getCategory());

		SbiCommonInfo i = new SbiCommonInfo();
		String userid = "biadmin";
		if (profile != null) {
			userid = (String) profile.getUserUniqueIdentifier();
		}
		i.setTimeIn(new Date());
		i.setUserIn(userid);
		i.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
		newMetaModel.setCommonInfo(i);

		// sessionCurrDB.save(newDataset);

		logger.debug("OUT");
		return newMetaModel;
	}

	/**
	 * Load an existing metamodel and make modifications as per the exported metamodel in input
	 *
	 * @param exportedmetamodel
	 *            the exported metamodel
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing metamodel modified as per the exported dataset in input
	 * @throws EMFUserError
	 */
	public SbiMetaModel modifyExisting(SbiMetaModel exportedMeta, Session sessionCurrDB, Integer existingId, Session sessionExpDB, IEngUserProfile profile)
			throws Exception {
		logger.debug("IN");
		SbiMetaModel existMeta = null;
		try {
			Query hibQueryMeta = sessionCurrDB.createQuery("from SbiMetaModel h where h.id= ?");
			hibQueryMeta.setInteger(0, existingId);
			existMeta = (SbiMetaModel) hibQueryMeta.uniqueResult();

			existMeta.setName(exportedMeta.getName());
			existMeta.setDescription(exportedMeta.getDescription());

			// existMeta.setCategory(exportedMeta.getCategory());

			SbiCommonInfo i = new SbiCommonInfo();
			String userid = "biadmin";
			if (profile != null) {
				userid = (String) profile.getUserUniqueIdentifier();
			}
			i.setTimeIn(new Date());
			i.setUserIn(userid);
			i.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
			existMeta.setCommonInfo(i);

			sessionCurrDB.update(existMeta);

		} catch (Exception e) {
			logger.error("Error in modifying exported meta model " + exportedMeta.getName(), e);
			throw e;
		} finally {
			logger.debug("OUT");
		}
		return existMeta;
	}

	public SbiMetaModel associateWithExistingEntities(SbiMetaModel newMetaModel, SbiMetaModel exportedMetaModel, SbiMetaModelContent exportedMetaModelContent,
			Session sessionExpDB, Session sessionCurrDB, ImporterMetadata importer, MetadataAssociations metaAss) throws EMFUserError {

		logger.debug("IN");

		// Data Source

		SbiDataSource expDs = exportedMetaModel.getDataSource();
		if (expDs != null) {
			SbiDataSource newDs = null;
			Integer id = expDs.getDsId();
			Integer newDsID = (Integer) metaAss.getDataSourceIDAssociation().get(id);

			newDs = (SbiDataSource) sessionCurrDB.load(SbiDataSource.class, newDsID);
			if (newDs != null) {
				newMetaModel.setDataSource(newDs);
				logger.debug("Set datasource " + newDs.getDescr());
			}

		}

		Map domainIdAss = metaAss.getDomainIDAssociation();
		if (exportedMetaModel.getCategory() != null) {
			Integer oldId = exportedMetaModel.getCategory();
			Integer newId = (Integer) domainIdAss.get(oldId);
			if (newId == null) {
				logger.warn("could not find CATEGORY TYPE domain included by meta model" + exportedMetaModel.getDescription() + ": insert it as new");
				SbiDomains catNew = insertCategoryTypeDomain(oldId, sessionExpDB, sessionCurrDB, metaAss);
				if (catNew != null) {
					logger.debug("inserted new BM category " + catNew.getDomainCd());
					newMetaModel.setCategory(catNew.getValueId());
				} else {
					newMetaModel.setCategory(null);
				}

			} else {
				// I must get the new SbiDomains object
				logger.debug("category previously identificed by " + oldId + " is now identified by " + newId);
				SbiDomains newCategory = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newId);
				newMetaModel.setCategory(newCategory.getValueId());
			}
		} else {
			newMetaModel.setCategory(null);
		}

		logger.debug("OUT");
		return newMetaModel;
	}

	public SbiMetaModel insertMetaModelContent(SbiMetaModel newMetaModel, SbiMetaModel exportedMetaModel, SbiMetaModelContent exportedMetaModelContent,
			Session sessionCurrDB, ImporterMetadata importer, MetadataAssociations metaAss) throws EMFUserError {

		logger.debug("IN");

		// set to not active the current active template
		String hql = " update SbiMetaModelContent mmc set mmc.active = false where mmc.active = true and mmc.model.id = ? ";
		Query query = sessionCurrDB.createQuery(hql);
		query.setInteger(0, newMetaModel.getId());
		logger.debug("Updates the current content of model " + newMetaModel + " with active = false.");
		query.executeUpdate();

		SbiMetaModelContent newContent = new SbiMetaModelContent();
		newContent.setActive(true);
		newContent.setContent(exportedMetaModelContent.getContent());
		newContent.setCreationDate(exportedMetaModelContent.getCreationDate());
		newContent.setCreationUser(exportedMetaModelContent.getCreationUser());
		newContent.setDimension(exportedMetaModelContent.getDimension());
		newContent.setFileName(exportedMetaModelContent.getFileName());
		newContent.setModel(newMetaModel);

		newContent.setCommonInfo(newMetaModel.getCommonInfo());
		sessionCurrDB.save(newContent);
		logger.debug("New MetaModel Content saved");

		logger.debug("OUT");
		return newMetaModel;
	}

	/**
	 * Make new data set.
	 *
	 * @param dataProxy
	 *            the ds
	 * @return the sbi data set
	 */
	public SbiDataSet makeNew(SbiDataSet exportedDataset, Session sessionCurrDB, IEngUserProfile profile) {
		logger.debug("IN");
		SbiDataSetId dsID = getDataSetKey(sessionCurrDB, exportedDataset, true, (UserProfile) profile);
		SbiDataSet newDataset = new SbiDataSet(dsID);

		newDataset.setActive(true);
		newDataset.setLabel(exportedDataset.getLabel());
		newDataset.setName(exportedDataset.getName());
		newDataset.setDescription(exportedDataset.getDescription());
		newDataset.setConfiguration(exportedDataset.getConfiguration());
		// newDataset.setCategory(exportedDataset.getCategory());
		// newDataset.getsetOrganization(((UserProfile) profile).getOrganization());
		newDataset.setDsMetadata(exportedDataset.getDsMetadata());
		newDataset.setMetaVersion(exportedDataset.getMetaVersion());
		newDataset.setNumRows(exportedDataset.isNumRows());
		newDataset.setParameters(exportedDataset.getParameters());
		newDataset.setPersisted(exportedDataset.isPersisted());
		newDataset.setPersistTableName(exportedDataset.getPersistTableName());
		newDataset.setPivotColumnName(exportedDataset.getPivotColumnName());
		newDataset.setPivotColumnValue(exportedDataset.getPivotColumnName());
		newDataset.setPivotRowName(exportedDataset.getPivotRowName());
		newDataset.setPublicDS(exportedDataset.isPublicDS());
		newDataset.setOwner(exportedDataset.getOwner());

		SbiCommonInfo i = new SbiCommonInfo();
		String userid = "biadmin";
		if (profile != null) {
			userid = (String) profile.getUserUniqueIdentifier();
		}
		i.setTimeIn(new Date());
		i.setUserIn(userid);
		i.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
		newDataset.setCommonInfo(i);

		// sessionCurrDB.save(newDataset);

		logger.debug("OUT");
		return newDataset;
	}

	public SbiDataSet associateWithExistingEntities(SbiDataSet newDataset, SbiDataSet exportedDataset, Session sessionExpDB, Session sessionCurrDB,
			ImporterMetadata importer, MetadataAssociations metaAss) throws EMFUserError {

		logger.debug("IN");
		Map unique = new HashMap<String, String>();
		unique.put("valuecd", exportedDataset.getType());
		unique.put("domaincd", DataSetConstants.DATA_SET_TYPE);
		logger.debug("get Ds Domain Type from label type " + exportedDataset.getType());
		SbiDomains existDom = (SbiDomains) importer.checkExistence(unique, sessionCurrDB, new SbiDomains());
		newDataset.setType(existDom.getValueDs());

		if (exportedDataset.getType().equalsIgnoreCase(DataSetConstants.QUERY) || exportedDataset.getType().equalsIgnoreCase(DataSetConstants.DS_QUERY)) {
			String config = JSONUtils.escapeJsonString(newDataset.getConfiguration());
			JSONObject jsonConf = ObjectUtils.toJSONObject(config);
			// SbiDataSet queryDataSet = (SbiDataSet) dsHistory;
			// SbiDataSource ds = getAssociatedSbiDataSource(queryDataSet, sessionCurrDB, metaAss);
			SbiDataSource ds = getAssociatedSbiDataSource(newDataset, sessionExpDB, sessionCurrDB, metaAss, importer);

			if (ds != null) {
				try {
					jsonConf.remove(DataSetConstants.DATA_SOURCE);
					jsonConf.put(DataSetConstants.DATA_SOURCE, ds.getLabel());
					newDataset.setConfiguration(jsonConf.toString());
				} catch (Exception e) {
					logger.error("Error while defining dataset / dtasource configuration.  Error: " + e.getMessage());
				}
			}
		}

		SbiDomains transformer = getAssociatedTransfomerType(exportedDataset, sessionCurrDB, metaAss, importer);
		if (transformer != null) {
			newDataset.setTransformer(transformer);
		}

		// associate scope
		Map domainIdAss = metaAss.getDomainIDAssociation();
		if (exportedDataset.getScope() != null) {
			Integer oldId = exportedDataset.getScope().getValueId();
			Integer newId = (Integer) domainIdAss.get(oldId);
			if (newId != null) {
				logger.debug("scope previously identificed by " + oldId + " is now identified by " + newId);
				SbiDomains newScope = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newId);
				newDataset.setScope(newScope);
			} else {
				logger.error("Could not find scope category previously identified with id " + oldId + ": leave it blanck");
				newDataset.setScope(null);
			}

		}

		// associate category; if category is not present means it is new and must be inserted
		if (exportedDataset.getCategory() != null) {
			Integer oldId = exportedDataset.getCategory().getValueId();
			Integer newId = (Integer) domainIdAss.get(oldId);
			if (newId == null) {
				logger.warn("could not find CATEGORY TYPEdomain " + exportedDataset.getCategory().getDomainNm() + ": insert it as new");
				SbiDomains catNew = insertCategoryTypeDomain(oldId, sessionExpDB, sessionCurrDB, metaAss);
				if (catNew != null) {
					logger.debug("inserted new category type" + catNew.getDomainCd());
					newDataset.setCategory(catNew);
				} else {
					newDataset.setCategory(null);
				}

			} else {
				// I must get the new SbiDomains object
				logger.debug("category previously identificed by " + oldId + " is now identified by " + newId);
				SbiDomains newCategory = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newId);
				newDataset.setCategory(newCategory);
			}
		} else {
			newDataset.setCategory(null);
		}

		logger.debug("OUT");
		return newDataset;
	}

	SbiDomains insertCategoryTypeDomain(Integer expDomainId, Session sessionExpDB, Session sessionCurrDB, MetadataAssociations metaAss) {
		logger.debug("IN");
		// insert new category
		SbiDomains toReturn = null;
		try {

			String hql = "from SbiDomains d where d.valueId = " + expDomainId;
			Query hqlQuery = sessionExpDB.createQuery(hql);
			SbiDomains hibDomains = (SbiDomains) hqlQuery.uniqueResult();
			if (hibDomains != null) {
				SbiDomains newSbiDomains = new SbiDomains();
				newSbiDomains.setDomainCd(hibDomains.getDomainCd());
				newSbiDomains.setDomainNm(hibDomains.getDomainNm());
				newSbiDomains.setValueCd(hibDomains.getValueCd());
				newSbiDomains.setValueNm(hibDomains.getValueNm());
				newSbiDomains.setValueDs(hibDomains.getValueDs());
				newSbiDomains.setCommonInfo(hibDomains.getCommonInfo());

				Serializable s = sessionCurrDB.save(newSbiDomains);
				sessionCurrDB.flush();

				if (s != null) {
					Integer id = (Integer) s;
					Object obj = sessionCurrDB.load(SbiDomains.class, id);
					if (obj != null) {
						toReturn = (SbiDomains) obj;
						metaAss.getDomainIDAssociation().put(expDomainId, id);

					}

				}

				// String back = "from SbiDomains d where d.domainCd = 'CATEGORY_TYPE' AND d.valueCd='"+expDomainId+"'";
				// Query backQuery = sessionCurrDB.createQuery(back);
				// toReturn = (SbiDomains) backQuery.uniqueResult();
			} else {
				logger.error("Could not find category which in exported DB should have had id " + expDomainId + " go on without carrying category");
			}
		} catch (Exception e) {
			logger.error("Error inserting category which in exported Db has id " + expDomainId + " go on without carrying category");

		}
		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Load an existing dataset and make modifications as per the exported dataset in input For what concern the history keep track of the previous one and
	 * insert the new one
	 *
	 * @param exportedDataset
	 *            the exported dataset
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing dataset modified as per the exported dataset in input
	 * @throws EMFUserError
	 */
	public SbiDataSet modifyExisting(SbiDataSet exportedDataset, Session sessionCurrDB, Integer existingId, Session sessionExpDB, IEngUserProfile profile)
			throws Exception {
		logger.debug("IN");
		SbiDataSet newDataset = null;
		try {
			// Query hibQueryExisting = sessionCurrDB.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId= ?" );
			// hibQueryExisting.setBoolean(0, true);
			// hibQueryExisting.setInteger(1, existingId);
			// existingDataset = (SbiDataSet) sessionCurrDB.load(SbiDataSet.class, existingId);
			// existingDataset = (SbiDataSet)hibQueryExisting.uniqueResult();
			Query hibQueryPreActive = sessionCurrDB.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId= ?");
			hibQueryPreActive.setBoolean(0, true);
			hibQueryPreActive.setInteger(1, existingId);
			SbiDataSet preActive = (SbiDataSet) hibQueryPreActive.uniqueResult();

			if (datasetMap.get(preActive.getId().getDsId()) == null) {
				datasetMap.put(preActive.getId().getDsId(), preActive);
			}

			SbiDataSetId dsID = getDataSetKey(sessionCurrDB, preActive, false, (UserProfile) profile);
			newDataset = new SbiDataSet(dsID);
			newDataset.setActive(true);
			newDataset.setLabel(exportedDataset.getLabel());
			newDataset.setName(exportedDataset.getName());
			newDataset.setDescription(exportedDataset.getDescription());
			newDataset.setConfiguration(exportedDataset.getConfiguration());
			// newDataset.setCategory(exportedDataset.getCategory());
			// newDataset.setOrganization(((UserProfile) profile).getOrganization());
			newDataset.setDsMetadata(exportedDataset.getDsMetadata());
			newDataset.setMetaVersion(exportedDataset.getMetaVersion());
			newDataset.setNumRows(exportedDataset.isNumRows());
			newDataset.setParameters(exportedDataset.getParameters());
			newDataset.setPersisted(exportedDataset.isPersisted());
			newDataset.setPersistTableName(exportedDataset.getPersistTableName());
			newDataset.setPivotColumnName(exportedDataset.getPivotColumnName());
			newDataset.setPivotColumnValue(exportedDataset.getPivotColumnName());
			newDataset.setPivotRowName(exportedDataset.getPivotRowName());
			newDataset.setPublicDS(exportedDataset.isPublicDS());
			newDataset.setOwner(exportedDataset.getOwner());

			SbiCommonInfo i = new SbiCommonInfo();
			String userid = "biadmin";
			if (profile != null) {
				userid = (String) profile.getUserUniqueIdentifier();
			}
			i.setTimeIn(new Date());
			i.setUserIn(userid);
			i.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
			newDataset.setCommonInfo(i);

			preActive.setActive(false);
			// sessionCurrDB.update(preActive);

			// sessionCurrDB.save(newDataset);

		} catch (Exception e) {
			logger.error("Error in modifying exported dataset " + exportedDataset.getLabel(), e);
			throw e;
		} finally {
			logger.debug("OUT");
		}
		return newDataset;
	}

	// insert

	/**
	 * associate the new History associated with the exported dataset.
	 *
	 * @param dataset
	 *            the dataset
	 * @param exportedDataset
	 *            the exported dataset
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param importer
	 *            the importer
	 * @param metaAss
	 *            the meta ass
	 * @throws EMFUserError
	 *             the EMF user error
	 */

	// public void associateNewSbiDataSet(SbiDataSet newDataset,
	// SbiDataSet exportedDataset, Session sessionCurrDB, Session sessionExpDB,
	// ImporterMetadata importer, MetadataAssociations metaAss, IEngUserProfile profile) {
	// logger.debug("IN");
	// try {
	// // save the new exported
	// // insert new Active dataset: in the export DB there is only one for each datasetConfig
	// Query hibQuery = sessionExpDB.createQuery("from SbiDataSet h where h.active = ? and h.label = ?" );
	// hibQuery.setBoolean(0, true);
	// hibQuery.setString(1, exportedDataset.getLabel());
	// SbiDataSet ds2 =(SbiDataSet)hibQuery.uniqueResult();
	// // create a copy for current dataset (cannot modify the one retieved frome export DB
	// //SbiDataSet dsnew = DAOFactory.getDataSetDAO().copyDataSet(ds2 );
	// SbiDataSet dsnew = ds2;
	//
	// // associate data source
	//
	//
	// if(ds2.getType().equalsIgnoreCase(DataSetConstants.QUERY)) {
	// String config = JSONUtils.escapeJsonString(ds2.getConfiguration());
	// JSONObject jsonConf = ObjectUtils.toJSONObject(config);
	// //SbiDataSet queryDataSet = (SbiDataSet) dsHistory;
	// //SbiDataSource ds = getAssociatedSbiDataSource(queryDataSet, sessionCurrDB, metaAss);
	// SbiDataSource ds = getAssociatedSbiDataSource(ds2, sessionCurrDB, metaAss);
	//
	// if (ds != null) {
	// try{
	// jsonConf.remove(DataSetConstants.DATA_SOURCE);
	// jsonConf.put(DataSetConstants.DATA_SOURCE, ds.getLabel());
	// dsnew.setConfiguration(jsonConf.toString());
	// }catch (Exception e){
	// logger.error("Error while defining dataset / dtasource configuration.  Error: " + e.getMessage());
	// }
	// }
	// }
	//
	//
	// SbiDomains transformer = getAssociatedTransfomerType(exportedDataset, sessionCurrDB, metaAss, importer);
	// if (transformer != null) {
	// dsnew.setTransformer(transformer);
	//
	// }
	// SbiCommonInfo i = new SbiCommonInfo();
	// String userid = "biadmin";
	// if(profile!=null){
	// userid =(String) profile.getUserUniqueIdentifier();
	// }
	//
	// dsnew.setUserIn(userid);
	// dsnew.setTimeIn(new Date());
	// dsnew.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
	// dsnew.setOrganization(((UserProfile) profile).getOrganization());
	//
	// sessionCurrDB.save(dsnew);
	//
	// } catch (EMFUserError e) {
	// logger.error("EMF user Error",e);
	// e.printStackTrace();
	// }
	// finally {
	// logger.debug("OUT");
	// }
	// }

	/**
	 * Load an existing lov and make modifications as per the exported lov in input (existing associations with parameters are maintained).
	 *
	 * @param exportedLov
	 *            the exported lov
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing lov modified as per the exported lov in input
	 * @throws EMFUserError
	 */
	public SbiLov modifyExisting(SbiLov exportedLov, Session sessionCurrDB, Integer existingId, HashMap<String, String> dsExportUser) {
		logger.debug("IN");
		SbiLov existingLov = null;
		try {
			existingLov = (SbiLov) sessionCurrDB.load(SbiLov.class, existingId);
			existingLov.setDefaultVal(exportedLov.getDefaultVal());
			existingLov.setDescr(exportedLov.getDescr());
			existingLov.setLabel(exportedLov.getLabel());

			String lovProvider = exportedLov.getLovProvider();
			try {
				ILovDetail lovDetail = LovDetailFactory.getLovFromXML(lovProvider);
				if (lovDetail instanceof QueryDetail) {
					// if user has associated another datasource then set the
					// associated one, else put the same
					QueryDetail queryDetail = (QueryDetail) lovDetail;
					String dataSource = queryDetail.getDataSource();
					if (dsExportUser != null && dsExportUser.get(dataSource) != null) {
						String newDs = dsExportUser.get(dataSource);
						queryDetail.setDataSource(newDs);
					} else {
						queryDetail.setDataSource(dataSource);
					}
					existingLov.setLovProvider(queryDetail.toXML());
				} else if (lovDetail instanceof DatasetDetail) {
					// update dataset id
					DatasetDetail datasetDetail = (DatasetDetail) lovDetail;

					String datasetLabel = datasetDetail.getDatasetLabel();

					// TODO get Organization from LOV not from user
					Integer datasetId = null;
					try {
						Query query = sessionCurrDB.createQuery("select d.id from SbiDataSet d where d.label = :label and d.active= true");
						query.setString("label", datasetLabel);
						datasetId = (Integer) query.uniqueResult();
					} catch (Exception e) {
						logger.error("More than one dataset active with label " + datasetLabel + ": filter from user organization");
					}
					if (datasetId == null) {
						// if there are more dataset that are retrieved choose the organization one (should take from LOV but it is not handled
						Query query = sessionCurrDB
								.createQuery("select d.id from SbiDataSet d where d.label = :label and d.active= true and d.organization=:organization");
						query.setString("label", datasetLabel);
						String organization = ((UserProfile) profile).getOrganization();
						logger.debug("filter for organization " + organization);
						query.setString("organization", organization);
						datasetId = (Integer) query.uniqueResult();
					}

					datasetDetail.setDatasetId(datasetId.toString());
					existingLov.setLovProvider(datasetDetail.toXML());
				} else {
					existingLov.setLovProvider(lovProvider);
				}
			} catch (Exception e) {
				logger.error("Error in evaluating lov provider for exporter lov [" + exportedLov.getLabel() + "]. It will not be modified", e);
				existingLov.setLovProvider(lovProvider);
			}

			existingLov.setName(exportedLov.getName());
			existingLov.setProfileAttr(exportedLov.getProfileAttr());
		} finally {
			logger.debug("OUT");
		}
		return existingLov;
	}

	/**
	 * Set into the lov the lov type domain associated with the exported lov.
	 *
	 * @param lov
	 *            the lov
	 * @param exportedLov
	 *            the exported lov
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param importer
	 *            the importer
	 * @param metaAss
	 *            the meta ass
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void associateWithExistingEntities(SbiLov lov, SbiLov exportedLov, Session sessionCurrDB, ImporterMetadata importer, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		try {
			// reading existing lov type
			SbiDomains existDom = getAssociatedLovType(exportedLov, sessionCurrDB, metaAss, importer);
			if (existDom != null) {
				lov.setInputType(existDom);
				lov.setInputTypeCd(existDom.getValueCd());
			}
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Set into the datasource the dialect type domain associated with the exported datasource.
	 *
	 * @param datasource
	 *            the datasource
	 * @param exportedDatasource
	 *            the exported lov
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param importer
	 *            the importer
	 * @param metaAss
	 *            the meta ass
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void associateWithExistingEntities(SbiDataSource datasource, SbiDataSource exportedDatasource, Session sessionCurrDB, ImporterMetadata importer,
			MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		try {
			// reading existing lov type
			SbiDomains dialect = getAssociatedDialect(exportedDatasource, sessionCurrDB, metaAss, importer);
			if (dialect != null) {
				datasource.setDialect(dialect);
				datasource.setDialectDescr(dialect.getValueDs());
			}
		} finally {
			logger.debug("OUT");
		}
	}

	private SbiDomains getAssociatedTransfomerType(SbiDataSet exportedDataset, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer)
			throws EMFUserError {
		logger.debug("IN");
		// TODO mettere a posto con nuove tabelle dataset
		/*
		 * SbiDomains transformer = exportedDataset.getTransformer(); if (transformer == null) { logger.debug("Transformer not set for exported dataset [" +
		 * exportedDataset.getLabel() + "]"); return null; } String typeCd = transformer.getValueCd(); Map unique = new HashMap(); unique.put("valuecd",
		 * typeCd); unique.put("domaincd", "TRANSFORMER_TYPE"); SbiDomains existDom = (SbiDomains) importer.checkExistence(unique, sessionCurrDB, new
		 * SbiDomains());
		 */
		logger.debug("OUT");
		return null;
	}

	private SbiDomains getAssociatedParameterType(SbiParameters exportedParameter, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		String typeCd = exportedParameter.getParameterTypeCode();
		Map unique = new HashMap();
		unique.put("valuecd", typeCd);
		unique.put("domaincd", "PAR_TYPE");
		SbiDomains existDom = (SbiDomains) importer.checkExistence(unique, sessionCurrDB, new SbiDomains());
		logger.debug("OUT");
		return existDom;
	}

	private SbiDomains getAssociatedDialect(SbiDataSource exportedDatasource, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer)
			throws EMFUserError {
		logger.debug("IN");
		String valueCd = exportedDatasource.getDialect().getValueCd();
		Map unique = new HashMap();
		unique.put("valuecd", valueCd);
		unique.put("domaincd", "DIALECT_HIB");
		SbiDomains existDom = (SbiDomains) importer.checkExistence(unique, sessionCurrDB, new SbiDomains());
		logger.debug("OUT");
		return existDom;
	}

	private SbiDomains getAssociatedLovType(SbiLov exportedLov, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer)
			throws EMFUserError {
		logger.debug("IN");
		String inpTypeCd = exportedLov.getInputTypeCd();
		Map unique = new HashMap();
		unique.put("valuecd", inpTypeCd);
		unique.put("domaincd", "INPUT_TYPE");
		SbiDomains existDom = (SbiDomains) importer.checkExistence(unique, sessionCurrDB, new SbiDomains());
		logger.debug("OUT");
		return existDom;
	}

	// public SbiParuseDet makeNewSbiParuseDet(SbiParuseDet parusedet, Integer newParuseid, Integer newRoleid) {
	// logger.debug("IN");
	// SbiParuseDetId parusedetid = parusedet.getId();
	// SbiParuseDetId newParusedetid = new SbiParuseDetId();
	// if (newParuseid != null) {
	// SbiParuse sbiparuse = parusedetid.getSbiParuse();
	// SbiParuse newParuse = ImportUtilities.makeNewSbiParuse(sbiparuse, newParuseid);
	// newParusedetid.setSbiParuse(newParuse);
	// }
	// if (newRoleid != null) {
	// SbiExtRoles sbirole = parusedetid.getSbiExtRoles();
	// SbiExtRoles newRole = ImportUtilities.makeNewSbiExtRole(sbirole, newRoleid);
	// newParusedetid.setSbiExtRoles(newRole);
	// }
	// SbiParuseDet newParuseDet = new SbiParuseDet();
	// newParuseDet.setId(newParusedetid);
	// newParuseDet.setDefaultVal(parusedet.getDefaultVal());
	// newParuseDet.setHiddenFl(parusedet.getHiddenFl());
	// newParuseDet.setProg(parusedet.getProg());
	// logger.debug("OUT");
	// return newParuseDet;
	// }

	public SbiParuseDet makeNew(SbiParuseDet expParuseDet, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");

		SbiParuseDet newParuseDet = new SbiParuseDet();
		try {
			newParuseDet.setDefaultVal(expParuseDet.getDefaultVal());
			newParuseDet.setHiddenFl(expParuseDet.getHiddenFl());
			newParuseDet.setProg(expParuseDet.getProg());
			entitiesAssociations(expParuseDet, newParuseDet, sessionCurrDB, metaAss);
		} catch (EMFUserError e) {
			logger.error("Error in making new SbiParuseDet related to SbiParuse " + expParuseDet.getId().getSbiParuse().getLabel());
			throw e;
		}
		logger.debug("OUT");
		return newParuseDet;
	}

	public SbiParuseDet modifyExisting(SbiParuseDet exportedParusedet, SbiParuseDet existingParusedet, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		try {

			existingParusedet.setDefaultVal(exportedParusedet.getDefaultVal());
			existingParusedet.setHiddenFl(exportedParusedet.getHiddenFl());
			existingParusedet.setProg(exportedParusedet.getProg());

			entitiesAssociations(exportedParusedet, existingParusedet, sessionCurrDB, metaAss);

			logger.debug("OUT");

		} catch (EMFUserError e) {
			logger.error("Error in making new SbiParuseDet related to SbiParuse " + exportedParusedet.getId().getSbiParuse().getLabel());
			throw e;
		}
		logger.debug("OUT");

		return existingParusedet;
	}

	public void entitiesAssociations(SbiParuseDet exportedSbiParuseDet, SbiParuseDet newSbiParuseDet, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		// if(exportedSbiParuseDet.getId() == null)exportedSbiParuseDet.setId(new SbiParuseDetId());
		if (newSbiParuseDet.getId() == null)
			newSbiParuseDet.setId(new SbiParuseDetId());

		if (exportedSbiParuseDet.getId().getSbiParuse() != null) {
			Integer newParuseId = (Integer) metaAss.getParuseIDAssociation().get(exportedSbiParuseDet.getId().getSbiParuse().getUseId());
			if (newParuseId != null) {
				SbiParuse newParusePar = (SbiParuse) sessionCurrDB.load(SbiParuse.class, newParuseId);
				newSbiParuseDet.getId().setSbiParuse(newParusePar);
			} else {
				logger.error("could not find corresponding obj par");
				List params = new ArrayList();
				params.add("Sbi_Paruse");
				params.add("Sbi_Paruse_det");
				params.add("...");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		if (exportedSbiParuseDet.getId().getSbiExtRoles() != null) {
			Integer newRoleId = (Integer) metaAss.getRoleIDAssociation().get(exportedSbiParuseDet.getId().getSbiExtRoles().getExtRoleId());
			if (newRoleId != null) {
				SbiExtRoles newRole = (SbiExtRoles) sessionCurrDB.load(SbiExtRoles.class, newRoleId);
				newSbiParuseDet.getId().setSbiExtRoles(newRole);
			} else {
				logger.error("could not find corresponding obj par");
				List params = new ArrayList();
				params.add("Sbi_Role");
				params.add("Sbi_Paruse_det");
				params.add("...");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}

		}
		logger.debug("OUT");
	}

	public SbiParuseCk makeNew(SbiParuseCk expParuseCk, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");

		SbiParuseCk newParuseCk = new SbiParuseCk();
		try {
			newParuseCk.setProg(expParuseCk.getProg());
			entitiesAssociations(expParuseCk, newParuseCk, sessionCurrDB, metaAss);
		} catch (EMFUserError e) {
			logger.error("Error in making new SbiParuseCk related to SbiParuse " + expParuseCk.getId().getSbiParuse().getLabel());
			throw e;
		}
		logger.debug("OUT");
		return newParuseCk;
	}

	public SbiParuseCk modifyExisting(SbiParuseCk exportedParuseCk, SbiParuseCk existingParuseCk, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		try {

			existingParuseCk.setProg(exportedParuseCk.getProg());
			entitiesAssociations(exportedParuseCk, existingParuseCk, sessionCurrDB, metaAss);

		} catch (EMFUserError e) {
			logger.error("Error in making new SbiParuseCk related to SbiParuse " + exportedParuseCk.getId().getSbiParuse().getLabel());
			throw e;
		}
		logger.debug("OUT");

		return existingParuseCk;
	}

	public void entitiesAssociations(SbiParuseCk exportedSbiParuseCk, SbiParuseCk newSbiParuseCk, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		if (newSbiParuseCk.getId() == null)
			newSbiParuseCk.setId(new SbiParuseCkId());

		if (exportedSbiParuseCk.getId().getSbiParuse() != null) {
			Integer newParuseId = (Integer) metaAss.getParuseIDAssociation().get(exportedSbiParuseCk.getId().getSbiParuse().getUseId());
			if (newParuseId != null) {
				SbiParuse newParusePar = (SbiParuse) sessionCurrDB.load(SbiParuse.class, newParuseId);
				newSbiParuseCk.getId().setSbiParuse(newParusePar);
			} else {
				logger.error("could not find corresponding paruse");
				List params = new ArrayList();
				params.add("Sbi_Paruse");
				params.add("Sbi_Paruse_ck");
				params.add("...");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}
		}

		if (exportedSbiParuseCk.getId().getSbiChecks() != null) {
			Integer newCheckId = (Integer) metaAss.getCheckIDAssociation().get(exportedSbiParuseCk.getId().getSbiChecks().getCheckId());
			if (newCheckId != null) {
				SbiChecks newCheck = (SbiChecks) sessionCurrDB.load(SbiChecks.class, newCheckId);
				newSbiParuseCk.getId().setSbiChecks(newCheck);
			} else {
				logger.error("could not find corresponding obj par");
				List params = new ArrayList();
				params.add("Sbi_Check");
				params.add("Sbi_Paruse_ck");
				params.add("...");
				throw new EMFUserError(EMFErrorSeverity.ERROR, "10000", params, ImportManager.messageBundle);
			}

		}
		logger.debug("OUT");
	}

	// public SbiParuseCk makeNewSbiParuseCk(SbiParuseCk paruseck,
	// Integer newParuseid, Integer newCheckid) {
	// logger.debug("IN");
	// // build a new id for the SbiParuseCheck
	// SbiParuseCkId parusecheckid = paruseck.getId();
	// SbiParuseCkId newParusecheckid = new SbiParuseCkId();
	// if (newParuseid != null) {
	// SbiParuse sbiparuse = parusecheckid.getSbiParuse();
	// SbiParuse newParuse = ImportUtilities.SbiParuse(sbiparuse, newParuseid);
	// newParusecheckid.setSbiParuse(newParuse);
	// }
	// if (newCheckid != null) {
	// SbiChecks sbicheck = parusecheckid.getSbiChecks();
	// SbiChecks newCheck = ImportUtilities.makeNewSbiCheck(sbicheck, newCheckid);
	// newParusecheckid.setSbiChecks(newCheck);
	// }
	// SbiParuseCk newParuseck = new SbiParuseCk();
	// newParuseck.setId(newParusecheckid);
	// newParuseck.setProg(paruseck.getProg());
	// logger.debug("OUT");
	// return newParuseck;
	// }

	public SbiFuncRole makeNew(SbiFuncRole functrole, Integer newFunctid, Integer newRoleid) {
		logger.debug("IN");
		SbiFuncRoleId functroleid = functrole.getId();
		SbiFuncRoleId newFunctroleid = new SbiFuncRoleId();
		if (newFunctid != null) {
			SbiFunctions sbifunct = functroleid.getFunction();
			SbiFunctions newFunct = makeNew(sbifunct, newFunctid);
			newFunctroleid.setFunction(newFunct);
		}
		if (newRoleid != null) {
			SbiExtRoles sbirole = functroleid.getRole();
			SbiExtRoles newRole = makeNew(sbirole, newRoleid);
			newFunctroleid.setRole(newRole);
		}
		SbiFuncRole newFunctRole = new SbiFuncRole();
		newFunctRole.setId(newFunctroleid);
		logger.debug("OUT");
		return newFunctRole;
	}

	public static int getImportFileMaxSize() {
		logger.debug("IN");
		int toReturn = MAX_DEFAULT_IMPORT_FILE_SIZE;
		try {
			ConfigSingleton serverConfig = ConfigSingleton.getInstance();
			SourceBean maxSizeSB = (SourceBean) serverConfig.getAttribute("IMPORTEXPORT.IMPORT_FILE_MAX_SIZE");
			if (maxSizeSB != null) {
				String maxSizeStr = maxSizeSB.getCharacters();
				logger.debug("Configuration found for max import file size: " + maxSizeStr);
				Integer maxSizeInt = new Integer(maxSizeStr);
				toReturn = maxSizeInt.intValue();
			} else {
				logger.debug("No configuration found for max import file size");
			}
		} catch (Exception e) {
			logger.error("Error while retrieving max import file size", e);
			logger.debug("Considering default value " + MAX_DEFAULT_IMPORT_FILE_SIZE);
			toReturn = MAX_DEFAULT_IMPORT_FILE_SIZE;
		}
		logger.debug("OUT: max size = " + toReturn);
		return toReturn;
	}

	public static String getImportTempFolderPath() {
		logger.debug("IN");
		String toReturn = null;
		try {
			ConfigSingleton conf = ConfigSingleton.getInstance();
			SourceBean importerSB = (SourceBean) conf.getAttribute("IMPORTEXPORT.IMPORTEROLD");
			toReturn = (String) importerSB.getAttribute("tmpFolder");
			toReturn = GeneralUtilities.checkForSystemProperty(toReturn);
			if (!toReturn.startsWith("/") && toReturn.charAt(1) != ':') {
				String root = ConfigSingleton.getRootPath();
				toReturn = root + "/" + toReturn;
			}
		} catch (Exception e) {
			logger.error("Error while retrieving export temporary folder path", e);
		} finally {
			logger.debug("OUT: export temporary folder path = " + toReturn);
		}
		return toReturn;
	}

	public static IImportManager getImportManagerInstance() throws Exception {
		logger.debug("IN");
		IImportManager toReturn = null;
		try {
			ConfigSingleton conf = ConfigSingleton.getInstance();
			SourceBean importerSB = (SourceBean) conf.getAttribute("IMPORTEXPORT.IMPORTEROLD");
			// instance the importer class
			String impClassName = (String) importerSB.getAttribute("class");
			Class impClass = Class.forName(impClassName);
			toReturn = (IImportManager) impClass.newInstance();
		} catch (Exception e) {
			logger.error("Error while instantiating import manager", e);
			throw e;
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	/**
	 * Creates a new hibernate kpi object.
	 *
	 * @param kpi
	 *            kpi
	 * @return the new hibernate parameter object
	 */
	public SbiKpi makeNew(SbiKpi kpi, Session sessionCurrDB, MetadataAssociations metaAss) {
		logger.debug("IN");
		SbiKpi newKpi = new SbiKpi();
		try {
			newKpi.setDescription(kpi.getDescription());
			newKpi.setCode(kpi.getCode());
			newKpi.setName(kpi.getName());
			newKpi.setSbiKpiDocumentses(kpi.getSbiKpiDocumentses());
			newKpi.setFlgIsFather(kpi.getFlgIsFather());
			newKpi.setMetric(kpi.getMetric());
			newKpi.setWeight(kpi.getWeight());
			newKpi.setInputAttributes(kpi.getInputAttributes());
			newKpi.setInterpretation(kpi.getInterpretation());
			newKpi.setModelReference(kpi.getModelReference());
			newKpi.setTargetAudience(kpi.getTargetAudience());

			// associations
			entitiesAssociations(kpi, newKpi, sessionCurrDB, metaAss);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new kpi " + newKpi.getCode());
		} finally {

		}
		return newKpi;
	}

	/**
	 * Load an existing kpi and make modifications as per the exported kpi in input
	 *
	 * @param exportedKpi
	 *            the exported Kpi
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Kpi modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiKpi modifyExisting(SbiKpi exportedKpi, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		SbiKpi existingKpi = null;
		try {
			// update th Value
			existingKpi = (SbiKpi) sessionCurrDB.load(SbiKpi.class, existingId);
			existingKpi.setCode(exportedKpi.getCode());
			existingKpi.setDescription(exportedKpi.getDescription());
			existingKpi.setSbiKpiDocumentses(exportedKpi.getSbiKpiDocumentses());
			existingKpi.setFlgIsFather(exportedKpi.getFlgIsFather());
			existingKpi.setInputAttributes(exportedKpi.getInputAttributes());
			existingKpi.setInterpretation(exportedKpi.getInterpretation());
			existingKpi.setMetric(exportedKpi.getMetric());
			existingKpi.setModelReference(exportedKpi.getModelReference());
			existingKpi.setName(exportedKpi.getName());
			existingKpi.setTargetAudience(exportedKpi.getTargetAudience());

			// overwrite existging entities (maybe create a function speciic for domains, maybe not)
			entitiesAssociations(exportedKpi, existingKpi, sessionCurrDB, metaAss);

		}

		finally {
			logger.debug("OUT");
		}
		return existingKpi;
	}

	/**
	 * For Kpi search new Ids
	 *
	 * @param exportedKpi
	 *            the exported Kpi
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Kpi modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiKpi exportedKpi, SbiKpi existingKpi, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");

		// Kpi Type
		Map domainIdAss = metaAss.getDomainIDAssociation();
		if (exportedKpi.getSbiDomainsByKpiType() != null) {
			Integer oldKpiTypeId = exportedKpi.getSbiDomainsByKpiType().getValueId();
			Integer newKpiTypeId = (Integer) domainIdAss.get(oldKpiTypeId);
			if (newKpiTypeId == null) {
				logger.error("could not find domain " + exportedKpi.getSbiDomainsByKpiType().getDomainNm());
				existingKpi.setSbiDomainsByKpiType(null);
			} else {
				// I must get the new SbiDomains object
				SbiDomains newSbiKpiType = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newKpiTypeId);
				existingKpi.setSbiDomainsByKpiType(newSbiKpiType);
			}
		} else {
			existingKpi.setSbiDomainsByKpiType(null);
		}

		// Measure Type
		if (exportedKpi.getSbiDomainsByMeasureType() != null) {
			Integer oldMeasureTypeId = exportedKpi.getSbiDomainsByMeasureType().getValueId();
			Integer newMeasureTypeId = (Integer) domainIdAss.get(oldMeasureTypeId);
			if (newMeasureTypeId == null) {
				logger.error("could not find domain " + exportedKpi.getSbiDomainsByMeasureType().getDomainNm());
				existingKpi.setSbiDomainsByMeasureType(null);
			} else {
				// I must get the new SbiDomains object
				SbiDomains newSbiMeasureType = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newMeasureTypeId);
				existingKpi.setSbiDomainsByMeasureType(newSbiMeasureType);
			}
		} else {
			existingKpi.setSbiDomainsByMeasureType(null);
		}

		// Metric Scale type
		if (exportedKpi.getSbiDomainsByMetricScaleType() != null) {
			Integer oldScaleTypeId = exportedKpi.getSbiDomainsByMetricScaleType().getValueId();
			Integer newScaleTypeId = (Integer) domainIdAss.get(oldScaleTypeId);
			if (newScaleTypeId == null) {
				logger.error("could not find domain " + exportedKpi.getSbiDomainsByMetricScaleType().getDomainNm());
				existingKpi.setSbiDomainsByMetricScaleType(null);
			} else {
				// I must get the new SbiDomains object
				SbiDomains newSbiScaleType = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newScaleTypeId);
				existingKpi.setSbiDomainsByMetricScaleType(newSbiScaleType);
			}
		} else {
			existingKpi.setSbiDomainsByMetricScaleType(null);
		}

		// dataset
		Map datasetIdAss = metaAss.getDataSetIDAssociation();
		if (exportedKpi.getSbiDataSet() != null) {
			Integer oldDataSetId = exportedKpi.getSbiDataSet();
			Integer newDataSetId = (Integer) datasetIdAss.get(oldDataSetId);
			if (newDataSetId == null) {
				logger.error("could not find dataset " + exportedKpi.getSbiDataSet());
				existingKpi.setSbiDataSet(null);
			} else {
				// check it has not been already retrieved otherwise hibernate gives error
				SbiDataSet newSbiDataset = null;
				if (datasetMap.get(newDataSetId) != null) {
					newSbiDataset = datasetMap.get(newDataSetId);
				} else {
					Query hibQuery = sessionCurrDB.createQuery("from SbiDataSet h where h.active = ? and h.id.dsId = ?");
					hibQuery.setBoolean(0, true);
					hibQuery.setInteger(1, newDataSetId);
					newSbiDataset = (SbiDataSet) hibQuery.uniqueResult();
				}

				existingKpi.setSbiDataSet(newSbiDataset.getId().getDsId());

			}
		} else {
			existingKpi.setSbiDataSet(null);
		}

		// threshold
		Map thresholdIdAss = metaAss.getTresholdIDAssociation();
		if (exportedKpi.getSbiThreshold() != null) {
			Integer oldThId = exportedKpi.getSbiThreshold().getThresholdId();
			Integer newThId = (Integer) thresholdIdAss.get(oldThId);
			if (newThId == null) {
				logger.error("could not find threshold " + exportedKpi.getSbiThreshold().getCode());
				existingKpi.setSbiThreshold(null);
			} else {
				SbiThreshold newSbiThreshold = (SbiThreshold) sessionCurrDB.load(SbiThreshold.class, newThId);
				existingKpi.setSbiThreshold(newSbiThreshold);
			}
		} else {
			existingKpi.setSbiThreshold(null);
		}

	}

	/**
	 * Creates a new hibernate kpi instance object.
	 *
	 * @param SbiKpiInstance
	 *            kpiInst
	 * @return the new hibernate parameter object
	 */
	public SbiKpiInstance makeNew(SbiKpiInstance kpiInst, Session sessionCurrDB, MetadataAssociations metaAss) {
		logger.debug("IN");
		SbiKpiInstance newKpiInst = new SbiKpiInstance();
		try {
			newKpiInst.setBeginDt(kpiInst.getBeginDt());
			newKpiInst.setTarget(kpiInst.getTarget());
			newKpiInst.setWeight(kpiInst.getWeight());

			// associations
			entitiesAssociations(kpiInst, newKpiInst, sessionCurrDB, metaAss);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new kpi instance with id " + newKpiInst.getIdKpiInstance());
		} finally {

		}
		return newKpiInst;
	}

	/**
	 * Load an existing kpi instance and make modifications as per the exported kpi instance in input
	 *
	 * @param exportedKpiInst
	 *            the exported Kpi Instance
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Kpi modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiKpiInstance modifyExisting(SbiKpiInstance exportedKpiInst, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		SbiKpiInstance existingKpiInst = null;
		try {
			// update th Value
			existingKpiInst = (SbiKpiInstance) sessionCurrDB.load(SbiKpiInstance.class, existingId);
			existingKpiInst.setBeginDt(exportedKpiInst.getBeginDt());
			existingKpiInst.setTarget(exportedKpiInst.getTarget());
			existingKpiInst.setWeight(exportedKpiInst.getWeight());

			// overwrite existging entities (maybe create a function speciic for domains, maybe not)
			entitiesAssociations(exportedKpiInst, existingKpiInst, sessionCurrDB, metaAss);

		}

		finally {
			logger.debug("OUT");
		}
		return existingKpiInst;
	}

	/**
	 * For Kpi Instamce search new Ids
	 *
	 * @param exportedKpiInstance
	 *            the exported Kpi Instance
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Kpi Instance modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiKpiInstance exportedKpiInst, SbiKpiInstance existingKpiInst, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");

		// Kpi Type
		Map domainIdAss = metaAss.getDomainIDAssociation();
		if (exportedKpiInst.getChartType() != null) {
			Integer oldChartTypeId = exportedKpiInst.getChartType().getValueId();
			Integer newChartTypeId = (Integer) domainIdAss.get(oldChartTypeId);
			if (newChartTypeId == null) {
				logger.error("could not find domain " + exportedKpiInst.getChartType().getDomainNm());
				existingKpiInst.setChartType(null);
			} else {
				// I must get the new SbiDomains object
				SbiDomains newSbiChartType = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newChartTypeId);
				existingKpiInst.setChartType(newSbiChartType);
			}
		} else {
			existingKpiInst.setChartType(null);
		}

		// add Kpi

		Map kpiIdAss = metaAss.getKpiIDAssociation();
		if (exportedKpiInst.getSbiKpi() != null) {
			Integer oldKpiId = exportedKpiInst.getSbiKpi().getKpiId();
			Integer newKpiId = (Integer) kpiIdAss.get(oldKpiId);
			if (newKpiId == null) {
				logger.error("could not find kpi between association" + exportedKpiInst.getSbiKpi().getName());
			} else {
				// I must get the new SbiDomains object
				SbiKpi newSbiKpi = (SbiKpi) sessionCurrDB.load(SbiKpi.class, newKpiId);
				existingKpiInst.setSbiKpi(newSbiKpi);
			}
		}

		// add Threshold

		Map thIdAss = metaAss.getTresholdIDAssociation();
		if (exportedKpiInst.getSbiThreshold() != null) {
			Integer oldThId = exportedKpiInst.getSbiThreshold().getThresholdId();
			Integer newThId = (Integer) thIdAss.get(oldThId);
			if (newThId == null) {
				logger.error("could not find Threshold between association" + exportedKpiInst.getSbiThreshold().getName());
				existingKpiInst.setSbiThreshold(null);
			} else {
				// I must get the new SbiDomains object
				SbiThreshold newSbiThreshold = (SbiThreshold) sessionCurrDB.load(SbiThreshold.class, newThId);
				existingKpiInst.setSbiThreshold(newSbiThreshold);
			}
		} else {
			existingKpiInst.setSbiThreshold(null);
		}
	}

	/**
	 * Creates a new hibernate th value object.
	 *
	 * @param ThreasholdValue
	 *            thValue
	 * @return the new hibernate parameter object
	 */
	public SbiThresholdValue makeNew(SbiThresholdValue thresholdValue, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiThresholdValue newThValue = new SbiThresholdValue();
		try {
			newThValue.setLabel(thresholdValue.getLabel());
			newThValue.setColour(thresholdValue.getColour());
			newThValue.setMaxValue(thresholdValue.getMaxValue());
			newThValue.setMinValue(thresholdValue.getMinValue());
			newThValue.setPosition(thresholdValue.getPosition());
			newThValue.setMaxClosed(thresholdValue.getMaxClosed());
			newThValue.setMinClosed(thresholdValue.getMinClosed());
			newThValue.setThValue(thresholdValue.getThValue());

			// associations
			entitiesAssociations(thresholdValue, newThValue, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new kpi threshold Value " + thresholdValue.getLabel());
		} finally {

		}
		return newThValue;
	}

	/**
	 * Load an existing threshold Values and make modifications as per the exported thValue in input
	 *
	 * @param exportedThValue
	 *            the exported Th value
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Threshold Value modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiThresholdValue modifyExisting(SbiThresholdValue exportedThValue, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		SbiThresholdValue existingThValue = null;
		try {
			// update th Value
			existingThValue = (SbiThresholdValue) sessionCurrDB.load(SbiThresholdValue.class, existingId);
			existingThValue.setLabel(exportedThValue.getLabel());
			existingThValue.setColour(exportedThValue.getColour());
			existingThValue.setMaxValue(exportedThValue.getMaxValue());
			existingThValue.setMinValue(exportedThValue.getMinValue());
			existingThValue.setPosition(exportedThValue.getPosition());
			existingThValue.setMaxClosed(exportedThValue.getMaxClosed());
			existingThValue.setMinClosed(exportedThValue.getMinClosed());
			existingThValue.setThValue(exportedThValue.getThValue());

			// associations
			entitiesAssociations(exportedThValue, existingThValue, sessionCurrDB, metaAss, importer);
		}

		finally {
			logger.debug("OUT");
		}
		return existingThValue;
	}

	/**
	 * For ThresholdValues search new Ids
	 *
	 * @param exportedKpi
	 *            the exported Kpi
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Kpi modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiThresholdValue exportedThValue, SbiThresholdValue existingThvalue, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		Map domainIdAss = metaAss.getDomainIDAssociation();
		if (exportedThValue.getSeverity() != null) {
			Integer oldSeverityId = exportedThValue.getSeverity().getValueId();
			Integer newSeverityId = (Integer) domainIdAss.get(oldSeverityId);
			if (newSeverityId == null) {
				logger.error("could not find domain between association" + exportedThValue.getSeverity());
				existingThvalue.setSeverity(null);
			} else {
				// I must get the new SbiDomains object
				SbiDomains newSbiSeverity = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newSeverityId);
				existingThvalue.setSeverity(newSbiSeverity);
			}
		} else {
			existingThvalue.setSeverity(null);
		}

		// add Threshold

		Map thresholdIdAss = metaAss.getTresholdIDAssociation();
		if (exportedThValue.getSbiThreshold() != null) {
			Integer oldThresholdId = exportedThValue.getSbiThreshold().getThresholdId();
			Integer newThresholdId = (Integer) thresholdIdAss.get(oldThresholdId);
			if (newThresholdId == null) {
				logger.error("could not find threshold between association" + exportedThValue.getSbiThreshold().getName());
			} else {
				// I must get the new SbiDomains object
				SbiThreshold newSbiThreshold = (SbiThreshold) sessionCurrDB.load(SbiThreshold.class, newThresholdId);
				existingThvalue.setSbiThreshold(newSbiThreshold);
			}
		}
		// TODO sbi alarm,

	}

	/**
	 * Creates a new hibernate th object.
	 *
	 * @param Threashold
	 *            thresdold
	 * @return the new hibernate parameter object
	 */
	public SbiThreshold makeNew(SbiThreshold threshold, Session sessionCurrDB, MetadataAssociations metaAss) {
		logger.debug("IN");
		SbiThreshold newTh = new SbiThreshold();
		try {
			newTh.setCode(threshold.getCode());
			newTh.setDescription(threshold.getDescription());
			newTh.setName(threshold.getName());

			// associations
			entitiesAssociations(threshold, newTh, sessionCurrDB, metaAss);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new kpi Threshold " + threshold.getCode());
		} finally {

		}
		return newTh;
	}

	/**
	 * Load an existing threshold and make modifications as per the exported thValue in input
	 *
	 * @param exportedThValue
	 *            the exported Th
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Threshold modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiThreshold modifyExisting(SbiThreshold exportedTh, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		SbiThreshold existingTh = null;
		try {
			// update th Value
			existingTh = (SbiThreshold) sessionCurrDB.load(SbiThreshold.class, existingId);
			existingTh.setCode(exportedTh.getCode());
			existingTh.setName(exportedTh.getName());

			// associations
			entitiesAssociations(exportedTh, existingTh, sessionCurrDB, metaAss);
		}

		finally {
			logger.debug("OUT");
		}
		return existingTh;
	}

	/**
	 * For Threshold search new Ids
	 *
	 * @param exportedKpi
	 *            the exported Kpi
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Kpi modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiThreshold exportedTh, SbiThreshold existingTh, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities
		Map domainIdAss = metaAss.getDomainIDAssociation();
		if (exportedTh.getThresholdType() != null) {
			Integer oldThresholdTypeId = exportedTh.getThresholdType().getValueId();
			Integer newThresholdTypeId = (Integer) domainIdAss.get(oldThresholdTypeId);
			if (newThresholdTypeId == null) {
				logger.error("could not find domain " + exportedTh.getThresholdType().getValueCd());
			} else {
				// I must get the new SbiDomains object
				SbiDomains newSbiThresholdType = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newThresholdTypeId);
				existingTh.setThresholdType(newSbiThresholdType);
			}
		}

		// TODO ThresholdValue

	}

	/**
	 * Creates a new hibernate model object.
	 *
	 * @param Model
	 *            model
	 * @return the new hibernate parameter object
	 */
	public SbiKpiModel makeNew(SbiKpiModel model, Session sessionCurrDB, MetadataAssociations metaAss) {
		logger.debug("IN");
		SbiKpiModel newMod = new SbiKpiModel();
		try {
			newMod.setKpiModelLabel(model.getKpiModelLabel());
			newMod.setKpiModelCd(model.getKpiModelCd());
			newMod.setKpiModelDesc(model.getKpiModelDesc());
			newMod.setKpiModelNm(model.getKpiModelNm());

			// if label is null means we are coming from a version < 2.4 (transformator has changed so). Then assign a new unique label
			if (newMod.getKpiModelLabel() == null) {
				UUIDGenerator uuidGen = UUIDGenerator.getInstance();
				UUID uuid = uuidGen.generateTimeBasedUUID();
				newMod.setKpiModelLabel(uuid.toString());
			}

			// associations
			entitiesAssociations(model, newMod, sessionCurrDB, metaAss);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new model " + model.getKpiModelCd());
		} finally {

		}
		return newMod;
	}

	/**
	 * Load an existing Model and make modifications
	 *
	 * @param exportedModel
	 *            the exported Model
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Model modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiKpiModel modifyExisting(SbiKpiModel exportedMod, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		SbiKpiModel existingMod = null;
		try {
			// update th Value
			existingMod = (SbiKpiModel) sessionCurrDB.load(SbiKpiModel.class, existingId);
			existingMod.setKpiModelLabel(exportedMod.getKpiModelLabel());
			existingMod.setKpiModelCd(exportedMod.getKpiModelCd());

			existingMod.setKpiModelNm(exportedMod.getKpiModelNm());
			existingMod.setKpiModelDesc(exportedMod.getKpiModelDesc());

			// associations
			entitiesAssociations(exportedMod, existingMod, sessionCurrDB, metaAss);
		}

		finally {
			logger.debug("OUT");
		}
		return existingMod;
	}

	/**
	 * For Model search new Ids
	 *
	 * @param exportedModel
	 *            the exported Model
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Model modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiKpiModel exportedMod, SbiKpiModel existingMod, Session sessionCurrDB, MetadataAssociations metaAss) throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities
		Map domainIdAss = metaAss.getDomainIDAssociation();
		if (exportedMod.getModelType() != null) {
			Integer oldModelTypeId = exportedMod.getModelType().getValueId();
			Integer newModelTypeId = (Integer) domainIdAss.get(oldModelTypeId);
			if (newModelTypeId == null) {
				logger.error("could not find domain " + exportedMod.getModelType().getValueCd());
			} else {
				// I must get the new SbiDomains object
				SbiDomains newSbiModelType = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newModelTypeId);
				existingMod.setModelType(newSbiModelType);
			}
		}

		// Parent
		Map modelIdAss = metaAss.getModelIDAssociation();
		if (exportedMod.getSbiKpiModel() != null) {
			Integer oldParentId = exportedMod.getSbiKpiModel().getKpiModelId();
			Integer newParentId = (Integer) modelIdAss.get(oldParentId);
			if (newParentId == null) {
				logger.error("could not find parent " + exportedMod.getSbiKpiModel().getKpiModelCd());
				existingMod.setSbiKpiModel(null);
			} else {
				SbiKpiModel newSbiParent = (SbiKpiModel) sessionCurrDB.load(SbiKpiModel.class, newParentId);
				existingMod.setSbiKpiModel(newSbiParent);
			}
		} else {
			existingMod.setSbiKpiModel(null);
		}

		// add Kpi
		Map kpiIdAss = metaAss.getKpiIDAssociation();
		if (exportedMod.getSbiKpi() != null) {
			Integer oldKpiId = exportedMod.getSbiKpi().getKpiId();
			Integer newKpiId = (Integer) kpiIdAss.get(oldKpiId);
			if (newKpiId == null) {
				logger.error("could not find Kpi between association" + exportedMod.getSbiKpi().getKpiId());
				existingMod.setSbiKpi(null);
			} else {
				// I must get the new SbiDomains object
				SbiKpi newSbiKpi = (SbiKpi) sessionCurrDB.load(SbiKpi.class, newKpiId);
				existingMod.setSbiKpi(newSbiKpi);
			}
		} else {
			existingMod.setSbiKpi(null);
		}

	}

	/**
	 * Creates a new hibernate model instance object.
	 *
	 * @param ModelInst
	 *            model instance
	 * @return the new hibernate parameter object
	 */
	public SbiKpiModelInst makeNew(SbiKpiModelInst modelInst, Session sessionCurrDB, MetadataAssociations metaAss) {
		logger.debug("IN");
		SbiKpiModelInst newModInst = new SbiKpiModelInst();
		try {
			newModInst.setDescription(modelInst.getDescription());
			newModInst.setEndDate(modelInst.getEndDate());
			newModInst.setLabel(modelInst.getLabel());
			newModInst.setName(modelInst.getName());
			newModInst.setStartDate(modelInst.getStartDate());
			newModInst.setModelUUID(modelInst.getModelUUID());

			// associations
			entitiesAssociations(modelInst, newModInst, sessionCurrDB, metaAss);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new model " + modelInst.getLabel());
		} finally {

		}
		return newModInst;
	}

	/**
	 * Load an existing Model Inst and make modifications
	 *
	 * @param exportedModelInst
	 *            the exported Model Instance
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Model INSTANCE modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiKpiModelInst modifyExisting(SbiKpiModelInst exportedModInst, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		SbiKpiModelInst existingModInst = null;
		try {
			// update th Value
			existingModInst = (SbiKpiModelInst) sessionCurrDB.load(SbiKpiModelInst.class, existingId);
			existingModInst.setDescription(exportedModInst.getDescription());
			existingModInst.setEndDate(exportedModInst.getEndDate());
			existingModInst.setLabel(exportedModInst.getLabel());
			existingModInst.setName(exportedModInst.getName());
			existingModInst.setStartDate(exportedModInst.getStartDate());
			existingModInst.setModelUUID(exportedModInst.getModelUUID());

			// associations
			entitiesAssociations(exportedModInst, existingModInst, sessionCurrDB, metaAss);
		}

		finally {
			logger.debug("OUT");
		}
		return existingModInst;
	}

	/**
	 * For Model Instance search new Ids
	 *
	 * @param exportedModelInst
	 *            the exported Model Instance
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Model Instance modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiKpiModelInst exportedModInst, SbiKpiModelInst existingModInst, Session sessionCurrDB, MetadataAssociations metaAss)
			throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		// add Model
		Map modelIdAss = metaAss.getModelIDAssociation();
		if (exportedModInst.getSbiKpiModel() != null) {
			Integer oldModelId = exportedModInst.getSbiKpiModel().getKpiModelId();
			Integer newModelId = (Integer) modelIdAss.get(oldModelId);
			if (newModelId == null) {
				logger.error("could not find model between association" + exportedModInst.getSbiKpiModel().getKpiModelId());
				existingModInst.setSbiKpiModel(null);
			} else {
				// I must get the new SbiDomains object
				SbiKpiModel newSbiModel = (SbiKpiModel) sessionCurrDB.load(SbiKpiModel.class, newModelId);
				existingModInst.setSbiKpiModel(newSbiModel);
			}
		} else {
			existingModInst.setSbiKpiModel(null);
		}

		// add Kpi Instance
		Map kpiInstIdAss = metaAss.getKpiInstanceIDAssociation();
		if (exportedModInst.getSbiKpiInstance() != null) {
			Integer oldKpiInstId = exportedModInst.getSbiKpiInstance().getIdKpiInstance();
			Integer newKpiInstId = (Integer) kpiInstIdAss.get(oldKpiInstId);
			if (newKpiInstId == null) {
				logger.error("could not find Kpi iNts between association" + exportedModInst.getSbiKpiInstance().getIdKpiInstance());
				existingModInst.setSbiKpiInstance(null);
			} else {
				// I must get the new SbiDomains object
				SbiKpiInstance newSbiKpiInst = (SbiKpiInstance) sessionCurrDB.load(SbiKpiInstance.class, newKpiInstId);
				existingModInst.setSbiKpiInstance(newSbiKpiInst);
			}
		} else {
			existingModInst.setSbiKpiInstance(null);
		}

		// Parent
		Map modelInstIdAss = metaAss.getModelInstanceIDAssociation();
		if (exportedModInst.getSbiKpiModelInst() != null) {
			Integer oldParentId = exportedModInst.getSbiKpiModelInst().getKpiModelInst();
			Integer newParentId = (Integer) modelInstIdAss.get(oldParentId);
			if (newParentId == null) {
				logger.error("could not find parent with id " + exportedModInst.getSbiKpiModelInst().getKpiModelInst());
				existingModInst.setSbiKpiModelInst(null);
			} else {
				SbiKpiModelInst newSbiParent = (SbiKpiModelInst) sessionCurrDB.load(SbiKpiModelInst.class, newParentId);
				existingModInst.setSbiKpiModelInst(newSbiParent);
			}
		} else {
			existingModInst.setSbiKpiModelInst(null);
		}

	}

	/**
	 * Creates a new hibernate SbiResource object.
	 *
	 * @param Resource
	 *            resource
	 * @return the new hibernate parameter object
	 */
	public SbiResources makeNew(SbiResources resource, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiResources newResource = new SbiResources();
		try {
			newResource.setResourceName(resource.getResourceName());
			newResource.setResourceCode(resource.getResourceCode());
			newResource.setResourceDescr(resource.getResourceDescr());
			newResource.setColumnName(resource.getColumnName());
			newResource.setTableName(resource.getTableName());

			// associations
			entitiesAssociations(resource, newResource, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new kpi Resource " + resource.getResourceName());
		} finally {

		}
		return newResource;
	}

	/**
	 * Load an existing resource and make modifications as per the exported resource in input
	 *
	 * @param exportedResource
	 *            the exported resource
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Threshold Value modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiResources modifyExisting(SbiResources exportedRes, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		SbiResources existingRes = null;
		try {
			// update th Value
			existingRes = (SbiResources) sessionCurrDB.load(SbiResources.class, existingId);
			existingRes.setResourceCode(exportedRes.getResourceCode());
			existingRes.setResourceName(exportedRes.getResourceName());
			existingRes.setResourceDescr(exportedRes.getResourceDescr());
			existingRes.setTableName(exportedRes.getTableName());
			existingRes.setColumnName(exportedRes.getColumnName());

			// associations
			entitiesAssociations(exportedRes, existingRes, sessionCurrDB, metaAss, importer);
		}

		finally {
			logger.debug("OUT");
		}
		return existingRes;
	}

	/**
	 * For Resources search new Ids
	 *
	 * @param exportedRes
	 *            the exported Resource
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Resource modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiResources exportedRes, SbiResources existingResources, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		Map domainIdAss = metaAss.getDomainIDAssociation();
		if (exportedRes.getType() != null) {
			Integer oldTypeId = exportedRes.getType().getValueId();
			Integer newTypeId = (Integer) domainIdAss.get(oldTypeId);
			if (newTypeId == null) {
				logger.error("could not find domain between association" + exportedRes.getType());
			} else {
				// I must get the new SbiDomains object
				SbiDomains newSbiType = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newTypeId);
				existingResources.setType(newSbiType);
			}
		}

	}

	/**
	 * Creates a new hibernate th value object.
	 *
	 * @param Model
	 *            Resource modRes
	 * @return the new hibernate parameter object
	 */
	public SbiKpiModelResources makeNew(SbiKpiModelResources modRes, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiKpiModelResources newModResource = new SbiKpiModelResources();
		try {
			// associations
			entitiesAssociations(modRes, newModResource, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error in creating new kpi, previous id was  " + modRes.getKpiModelResourcesId());
		} finally {

		}
		return newModResource;
	}

	/**
	 * Load an existing Model resource and make modifications as per the exported Model resource in input
	 *
	 * @param exported
	 *            mOdel Resource the exported resource
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Threshold Value modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiKpiModelResources modifyExisting(SbiKpiModelResources exportedModRes, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		SbiKpiModelResources existingModRes = null;
		try {
			// update th Value
			existingModRes = (SbiKpiModelResources) sessionCurrDB.load(SbiKpiModelResources.class, existingId);

			// associations
			entitiesAssociations(exportedModRes, existingModRes, sessionCurrDB, metaAss, importer);
		}

		finally {
			logger.debug("OUT");
		}
		return existingModRes;
	}

	/**
	 * For Model Resources search new Ids
	 *
	 * @param exported
	 *            Model Res the exported Resource
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Model Resource modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiKpiModelResources exportedModRes, SbiKpiModelResources existingModResources, Session sessionCurrDB,
			MetadataAssociations metaAss, ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");

		// Map resource
		Map resourceIdAss = metaAss.getResourcesIDAssociation();
		if (exportedModRes.getSbiResources() != null) {
			Integer oldTypeId = exportedModRes.getSbiResources().getResourceId();
			Integer newTypeId = (Integer) resourceIdAss.get(oldTypeId);
			if (newTypeId == null) {
				logger.error("could not find resource between association" + exportedModRes.getSbiResources().getResourceName());
			} else {
				// I must get the new SbiDomains object
				SbiResources newSbiType = (SbiResources) sessionCurrDB.load(SbiResources.class, newTypeId);
				existingModResources.setSbiResources(newSbiType);
			}
		}

		// Model instance
		Map modelInstIdAss = metaAss.getModelInstanceIDAssociation();
		if (exportedModRes.getSbiKpiModelInst() != null) {
			Integer oldTypeId = exportedModRes.getSbiKpiModelInst().getKpiModelInst();
			Integer newTypeId = (Integer) modelInstIdAss.get(oldTypeId);
			if (newTypeId == null) {
				logger.error("could not find model instance between association" + exportedModRes.getSbiKpiModelInst().getLabel());
			} else {
				// I must get the new SbiDomains object
				SbiKpiModelInst newSbiType = (SbiKpiModelInst) sessionCurrDB.load(SbiKpiModelInst.class, newTypeId);
				existingModResources.setSbiKpiModelInst(newSbiType);
			}
		}

	}

	/**
	 * Creates a new hibernate th value object.
	 *
	 * @param Periodicity
	 *            per
	 * @return the new hibernate parameter object
	 */
	public SbiKpiPeriodicity makeNew(SbiKpiPeriodicity periodicity, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiKpiPeriodicity newPer = new SbiKpiPeriodicity();
		try {
			newPer.setDays(periodicity.getDays());
			newPer.setChronString(periodicity.getChronString());
			newPer.setHours(periodicity.getHours());
			newPer.setMinutes(periodicity.getMinutes());
			newPer.setMonths(periodicity.getMonths());
			newPer.setName(periodicity.getName());

			// associations
			entitiesAssociations(periodicity, newPer, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new kpi Periodicity " + periodicity.getName());
		} finally {

		}
		return newPer;
	}

	/**
	 * Load an existing resource and make modifications as per the exported periodicity in input
	 *
	 * @param exportedPeriodicity
	 *            the exported periodicity
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Periodicity modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiKpiPeriodicity modifyExisting(SbiKpiPeriodicity exportedPer, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		SbiKpiPeriodicity existingPer = null;
		try {
			// update th Value
			existingPer = (SbiKpiPeriodicity) sessionCurrDB.load(SbiKpiPeriodicity.class, existingId);
			existingPer.setName(exportedPer.getName());
			existingPer.setChronString(exportedPer.getChronString());
			existingPer.setDays(exportedPer.getDays());
			existingPer.setHours(exportedPer.getHours());
			existingPer.setMonths(exportedPer.getMonths());
			existingPer.setMinutes(exportedPer.getMinutes());

			// associations
			entitiesAssociations(exportedPer, existingPer, sessionCurrDB, metaAss, importer);
		}

		finally {
			logger.debug("OUT");
		}
		return existingPer;
	}

	/**
	 * For Periodicity search new Ids
	 *
	 * @param exportedRes
	 *            the exported Periodicity
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Periodicity modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiKpiPeriodicity exportedPer, SbiKpiPeriodicity existingPer, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities
		logger.debug("OUT");

	}

	/**
	 * Creates a new hibernate th value object.
	 *
	 * @param KpiInstPeriod
	 *            kpiInstPeriod
	 * @return the new hibernate parameter object
	 */
	public SbiKpiInstPeriod makeNew(SbiKpiInstPeriod kpiInstPeriod, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiKpiInstPeriod newKpiInstPeriod = new SbiKpiInstPeriod();
		try {
			newKpiInstPeriod.setDefault_(kpiInstPeriod.isDefault_());

			// associations
			entitiesAssociations(kpiInstPeriod, newKpiInstPeriod, sessionCurrDB, metaAss, importer);

		} catch (Exception e) {
			logger.error("Error in creating new kpiInstPeriod, previous id was  " + kpiInstPeriod.getKpiInstPeriodId());
		} finally {

		}
		logger.debug("OUT");
		return newKpiInstPeriod;
	}

	/**
	 * Load an existing KpiInstPeriod and make modifications as per the exported KpiInstPeriod in input
	 *
	 * @param exported
	 *            mKpiInstPeriod the exported resource
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing KpiInstPeriod modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiKpiInstPeriod modifyExisting(SbiKpiInstPeriod exportedKpiInstPeriod, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		SbiKpiInstPeriod existingKpiInstPeriod = null;
		try {
			// update th Value
			existingKpiInstPeriod = (SbiKpiInstPeriod) sessionCurrDB.load(SbiKpiInstPeriod.class, existingId);
			existingKpiInstPeriod.setDefault_(exportedKpiInstPeriod.isDefault_());

			// associations
			entitiesAssociations(exportedKpiInstPeriod, existingKpiInstPeriod, sessionCurrDB, metaAss, importer);
		}

		finally {
			logger.debug("OUT");
		}
		return existingKpiInstPeriod;
	}

	/**
	 * For KpiInstPeriod search new Ids
	 *
	 * @param exported
	 *            KpiInstPeriod the exported KpiInstPeriod
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing KpiInstPeriod modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiKpiInstPeriod exportedKpiInstPeriod, SbiKpiInstPeriod existingKpiInstPeriod, Session sessionCurrDB,
			MetadataAssociations metaAss, ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");

		// Map priodicity
		Map periodicityIdAss = metaAss.getPeriodicityIDAssociation();
		if (exportedKpiInstPeriod.getSbiKpiPeriodicity() != null) {
			Integer oldPerId = exportedKpiInstPeriod.getSbiKpiPeriodicity().getIdKpiPeriodicity();
			Integer newPerId = (Integer) periodicityIdAss.get(oldPerId);
			if (newPerId == null) {
				logger.error("could not find periodicity associated to kpiInstPeriod: " + exportedKpiInstPeriod.getSbiKpiPeriodicity().getIdKpiPeriodicity());
			} else {
				// I must get the new SbiDomains object
				SbiKpiPeriodicity newSbiPeriodicity = (SbiKpiPeriodicity) sessionCurrDB.load(SbiKpiPeriodicity.class, newPerId);
				existingKpiInstPeriod.setSbiKpiPeriodicity(newSbiPeriodicity);
			}
		}

		// Kpi Instance
		Map kpiInstIdAss = metaAss.getKpiInstanceIDAssociation();
		if (exportedKpiInstPeriod.getSbiKpiInstance() != null) {
			Integer oldTypeId = exportedKpiInstPeriod.getSbiKpiInstance().getIdKpiInstance();
			Integer newTypeId = (Integer) kpiInstIdAss.get(oldTypeId);
			if (newTypeId == null) {
				logger.error("could not find kpi instance associater to kpiInstPeriod with previous id "
						+ exportedKpiInstPeriod.getSbiKpiInstance().getIdKpiInstance());
			} else {
				// I must get the new SbiDomains object
				SbiKpiInstance newSbiType = (SbiKpiInstance) sessionCurrDB.load(SbiKpiInstance.class, newTypeId);
				existingKpiInstPeriod.setSbiKpiInstance(newSbiType);
			}
		}

	}

	/**
	 * Creates a new hibernate SbiAlarm object.
	 *
	 * @param Alarm
	 *            alarm
	 * @return the new hibernate parameter object
	 */
	public SbiAlarm makeNew(SbiAlarm alarm, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiAlarm newAlarm = new SbiAlarm();
		try {
			newAlarm.setName(alarm.getName());
			newAlarm.setLabel(alarm.getLabel());
			newAlarm.setDescr(alarm.getDescr());
			newAlarm.setText(alarm.getText());
			newAlarm.setUrl(alarm.getUrl());
			newAlarm.setSingleEvent(alarm.isSingleEvent());
			newAlarm.setAutoDisabled(alarm.getAutoDisabled());

			// associations
			entitiesAssociations(alarm, newAlarm, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new alarm " + alarm.getLabel());
		} finally {

		}
		return newAlarm;
	}

	/**
	 * Load an existing Alarm and make modifications as per the exported Alarm in input
	 *
	 * @param exportedAlarm
	 *            the exported Alarm
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Alarm modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiAlarm modifyExisting(SbiAlarm exportedAlarm, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss, ImporterMetadata importer)
			throws EMFUserError {
		logger.debug("IN");
		SbiAlarm existingAlarm = null;
		try {
			// update Alarm
			existingAlarm = (SbiAlarm) sessionCurrDB.load(SbiAlarm.class, existingId);
			existingAlarm.setName(exportedAlarm.getName());
			existingAlarm.setLabel(exportedAlarm.getLabel());
			existingAlarm.setDescr(exportedAlarm.getDescr());
			existingAlarm.setText(exportedAlarm.getText());
			existingAlarm.setUrl(exportedAlarm.getUrl());
			existingAlarm.setSingleEvent(exportedAlarm.isSingleEvent());
			existingAlarm.setAutoDisabled(exportedAlarm.getAutoDisabled());

			// associations
			entitiesAssociations(exportedAlarm, existingAlarm, sessionCurrDB, metaAss, importer);
		}

		finally {
			logger.debug("OUT");
		}
		return existingAlarm;
	}

	/**
	 * For Alarm search new Ids
	 *
	 * @param exportedAlarm
	 *            the exported Alarm
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Alarm modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiAlarm exportedAlarm, SbiAlarm existingAlarm, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		Map domainIdAss = metaAss.getDomainIDAssociation();
		if (exportedAlarm.getModality() != null) {
			Integer oldModalityId = exportedAlarm.getModality().getValueId();
			Integer newModalityId = (Integer) domainIdAss.get(oldModalityId);
			if (newModalityId == null) {
				logger.error("could not find domain between association" + exportedAlarm.getModality());
			} else {
				// I must get the new SbiDomains object
				SbiDomains newSbiModality = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newModalityId);
				existingAlarm.setModality(newSbiModality);
			}
		}

		// Map Kpi Instance
		Map kpiInstanceIdAss = metaAss.getKpiInstanceIDAssociation();
		if (exportedAlarm.getSbiKpiInstance() != null) {
			Integer oldKpiInstId = exportedAlarm.getSbiKpiInstance().getIdKpiInstance();
			Integer newKpiInstId = (Integer) kpiInstanceIdAss.get(oldKpiInstId);
			if (newKpiInstId == null) {
				logger.error("could not find kpi instance associated to current alarm: " + exportedAlarm.getSbiKpiInstance().getIdKpiInstance());
			} else {
				// I must get the new SbiDomains object
				SbiKpiInstance newSbiKpiInstance = (SbiKpiInstance) sessionCurrDB.load(SbiKpiInstance.class, newKpiInstId);
				existingAlarm.setSbiKpiInstance(newSbiKpiInstance);
			}
		}

		// Threshold Value
		Map thValueIdAss = metaAss.getTresholdValueIDAssociation();
		if (exportedAlarm.getSbiThresholdValue() != null) {
			Integer oldThValueId = exportedAlarm.getSbiThresholdValue().getIdThresholdValue();
			Integer newThValueId = (Integer) thValueIdAss.get(oldThValueId);
			if (newThValueId == null) {
				logger.error("could not find ThValue associater to Alarm; with label " + exportedAlarm.getSbiThresholdValue().getLabel());
			} else {
				// I must get the new SbiDomains object
				SbiThresholdValue newSbiThValue = (SbiThresholdValue) sessionCurrDB.load(SbiThresholdValue.class, newThValueId);
				existingAlarm.setSbiThresholdValue(newSbiThValue);
			}
		}

		// fill the set of AlarmContacts

		Set<SbiAlarmContact> setContactsExported = exportedAlarm.getSbiAlarmContacts();

		// recover the Contacts already present
		Set<SbiAlarmContact> setContactsToImport = existingAlarm.getSbiAlarmContacts();

		// I want a Array to fill with Id already present just to check not to insert again an already present association
		Vector<Integer> idsAlready = new Vector<Integer>();
		for (Iterator iterator = setContactsToImport.iterator(); iterator.hasNext();) {
			SbiAlarmContact sbiAlarmContact = (SbiAlarmContact) iterator.next();
			Integer id = sbiAlarmContact.getId();
			idsAlready.add(id);
		}

		for (Iterator iterator = setContactsExported.iterator(); iterator.hasNext();) {
			SbiAlarmContact sbiAlarmContact = (SbiAlarmContact) iterator.next();
			Integer oldId = sbiAlarmContact.getId();
			Map<Integer, Integer> alarmContactAssociation = metaAss.getAlarmContactIDAssociation();
			Integer newId = alarmContactAssociation.get(oldId);
			// load
			if (newId == null) {
				newId = oldId;
			}

			// check id not already present
			if (!idsAlready.contains(newId)) {
				SbiAlarmContact newSbiAlarmContact = (SbiAlarmContact) sessionCurrDB.load(SbiAlarmContact.class, newId);
				setContactsToImport.add(newSbiAlarmContact);
				idsAlready.add(newId);
			}

		}
		existingAlarm.setSbiAlarmContacts(setContactsToImport);
	}

	/**
	 * Creates a new hibernate SbiAlarmContact object.
	 *
	 * @param AlarmContact
	 *            alarmContact
	 * @return the new hibernate parameter object
	 */
	public SbiAlarmContact makeNew(SbiAlarmContact alarmContact, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiAlarmContact newAlarmContact = new SbiAlarmContact();
		try {
			newAlarmContact.setName(alarmContact.getName());
			newAlarmContact.setEmail(alarmContact.getEmail());
			newAlarmContact.setMobile(alarmContact.getMobile());
			newAlarmContact.setResources(alarmContact.getResources());

			// associations
			entitiesAssociations(alarmContact, newAlarmContact, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new alarm Contact " + alarmContact.getName());
		} finally {

		}
		return newAlarmContact;
	}

	/**
	 * Load an existing Alarm Contact and make modifications as per the exported Alarm Contact in input
	 *
	 * @param exportedAlarm
	 *            the exported Alarm Contact
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Alarm Contactmodified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiAlarmContact modifyExisting(SbiAlarmContact exportedAlarmContact, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		SbiAlarmContact existingAlarmContact = null;
		try {
			// update Alarm
			existingAlarmContact = (SbiAlarmContact) sessionCurrDB.load(SbiAlarmContact.class, existingId);
			existingAlarmContact.setName(exportedAlarmContact.getName());
			existingAlarmContact.setEmail(exportedAlarmContact.getEmail());
			existingAlarmContact.setMobile(exportedAlarmContact.getMobile());
			existingAlarmContact.setResources(exportedAlarmContact.getResources());

			// associations
			entitiesAssociations(exportedAlarmContact, existingAlarmContact, sessionCurrDB, metaAss, importer);
		}

		finally {
			logger.debug("OUT");
		}
		return existingAlarmContact;
	}

	/**
	 * For Alarm Contacts search new Ids
	 *
	 * @param exportedAlarm
	 *            the exported Alarm Contacts
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Alarm Contacts modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiAlarmContact exportedAlarm, SbiAlarmContact existingAlarm, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		logger.debug("OUT");
	}

	/**
	 * Creates a new hibernate kpi model attr object.
	 *
	 * @param SbiKpiModelAttr
	 *            kpiModelAttr
	 * @return the new hibernate parameter object
	 */
	// TODO cambiare con i nuovi UDP VAlues
	/*
	 * public SbiKpiModelAttr makeNewSbiKpiModelAttr(SbiKpiModelAttr kpiModelAttr,Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata
	 * importer){ logger.debug("IN"); SbiKpiModelAttr newKpiModelAttr = new SbiKpiModelAttr(); try{ newKpiModelAttr.setKpiModelAttrCd(
	 * kpiModelAttr.getKpiModelAttrCd() ); newKpiModelAttr.setKpiModelAttrNm( kpiModelAttr.getKpiModelAttrNm() ); newKpiModelAttr.setKpiModelAttrDescr(
	 * kpiModelAttr.getKpiModelAttrDescr() );
	 *
	 * // associations entitiesAssociationsSbiKpiModelAttr(kpiModelAttr, newKpiModelAttr, sessionCurrDB, metaAss, importer);
	 *
	 * logger.debug("OUT"); } catch (Exception e) { logger.error("Error inc reating new Model Attr with label "+newKpiModelAttr.getKpiModelAttrCd()); } finally{
	 *
	 * } return newKpiModelAttr; }
	 */

	/**
	 * Load an existing kpiModelAttr and make modifications as per the exported kpiModelAttr in input
	 *
	 * @param exportedKpiModelAttr
	 *            the exported KpiModelAttr
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing Kpi modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	// TODO cambiare con i nuovi UDP VAlues
	/*
	 * public SbiKpiModelAttr modifyExistingSbiKpiModelAttr(SbiKpiModelAttr exportedKpiModelAttr, Session sessionCurrDB, Integer existingId,
	 * MetadataAssociations metaAss, ImporterMetadata importer) throws EMFUserError { logger.debug("IN"); SbiKpiModelAttr existingKpiModelAttr = null; try { //
	 * update th Value existingKpiModelAttr = (SbiKpiModelAttr) sessionCurrDB.load(SbiKpiModelAttr.class, existingId);
	 * existingKpiModelAttr.setKpiModelAttrCd(exportedKpiModelAttr.getKpiModelAttrCd());
	 * existingKpiModelAttr.setKpiModelAttrNm(exportedKpiModelAttr.getKpiModelAttrNm());
	 * existingKpiModelAttr.setKpiModelAttrDescr(exportedKpiModelAttr.getKpiModelAttrDescr());
	 *
	 * // overwrite existging entities (maybe create a function speciic for domains, maybe not) entitiesAssociationsSbiKpiModelAttr(exportedKpiModelAttr,
	 * existingKpiModelAttr, sessionCurrDB, metaAss, importer);
	 *
	 * }
	 *
	 * finally { logger.debug("OUT"); } return existingKpiModelAttr; }
	 */

	/**
	 * For KpiModelAttr search new Ids
	 *
	 * @param exportedKpi
	 *            the exported Kpi
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Kpi modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	// TODO cambiare con i nuovi UDP VAlues
	/*
	 * public void entitiesAssociationsSbiKpiModelAttr(SbiKpiModelAttr exportedKpiModelAttr, SbiKpiModelAttr existingKpiModelAttr,Session sessionCurrDB,
	 * MetadataAssociations metaAss, ImporterMetadata importer) throws EMFUserError { logger.debug("IN"); // overwrite existging entities
	 *
	 * Map domainIdAss=metaAss.getDomainIDAssociation(); if(exportedKpiModelAttr.getSbiDomains()!=null){ Integer
	 * oldDomainId=exportedKpiModelAttr.getSbiDomains().getValueId(); Integer newDomainId=(Integer)domainIdAss.get(oldDomainId); if(newDomainId==null) {
	 * logger.error("could not find domain between association"+exportedKpiModelAttr.getSbiDomains().getValueCd()); existingKpiModelAttr.setSbiDomains(null); }
	 * else{ // I must get the new SbiDomains object SbiDomains newSbiDomain= (SbiDomains) sessionCurrDB.load(SbiDomains.class, newDomainId);
	 * existingKpiModelAttr.setSbiDomains(newSbiDomain); } } else{ existingKpiModelAttr.setSbiDomains(null); } }
	 */

	/**
	 * Creates a new hibernate kpi model attr val object.
	 *
	 * @param SbiKpiModelAttr
	 *            kpiModelAttrVal
	 * @return the new hibernate parameter object
	 */
	// TODO cambiare con i nuovi UDP VAlues
	/*
	 * public SbiKpiModelAttrVal makeNewSbiKpiModelAttrVal(SbiKpiModelAttrVal kpiModelAttrVal,Session sessionCurrDB, MetadataAssociations metaAss,
	 * ImporterMetadata importer){ logger.debug("IN"); SbiKpiModelAttrVal newKpiModelAttrVal = new SbiKpiModelAttrVal(); try{ newKpiModelAttrVal.setValue(
	 * kpiModelAttrVal.getValue() );
	 *
	 * // associations entitiesAssociationsSbiKpiModelAttrVal(kpiModelAttrVal, newKpiModelAttrVal, sessionCurrDB, metaAss, importer);
	 *
	 * logger.debug("OUT"); } catch (Exception e) {
	 * logger.error("Error inc reating new Model Attr Val referring to model "+newKpiModelAttrVal.getSbiKpiModel().getKpiModelNm
	 * ()+" an to attribute "+newKpiModelAttrVal.getSbiKpiModelAttr().getKpiModelAttrNm()); } finally{
	 *
	 * } return newKpiModelAttrVal; }
	 */

	/**
	 * Load an existing kpiModelAttrVal and make modifications as per the exported kpiModelAttr in input
	 *
	 * @param exportedKpiModelAttrVal
	 *            the exported KpiModelAttrVal
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	// TODO cambiare con i nuovi UDP VAlues
	/*
	 * public SbiKpiModelAttrVal modifyExistingSbiKpiModelAttrVal(SbiKpiModelAttrVal exportedKpiModelAttrVal, Session sessionCurrDB, Integer existingId,
	 * MetadataAssociations metaAss, ImporterMetadata importer) throws EMFUserError { logger.debug("IN"); SbiKpiModelAttrVal existingKpiModelAttrVal = null; try
	 * { // update th Value existingKpiModelAttrVal = (SbiKpiModelAttrVal) sessionCurrDB.load(SbiKpiModelAttrVal.class, existingId);
	 * existingKpiModelAttrVal.setSbiKpiModel(exportedKpiModelAttrVal.getSbiKpiModel());
	 * existingKpiModelAttrVal.setSbiKpiModelAttr(exportedKpiModelAttrVal.getSbiKpiModelAttr());
	 *
	 * // overwrite existging entities (maybe create a function speciic for domains, maybe not) entitiesAssociationsSbiKpiModelAttrVal(exportedKpiModelAttrVal,
	 * existingKpiModelAttrVal, sessionCurrDB, metaAss, importer);
	 *
	 * }
	 *
	 * finally { logger.debug("OUT"); } return existingKpiModelAttrVal; }
	 */

	/**
	 * KpiModelAttrVal association entities search new Ids
	 *
	 * @param exportedKpiMdelAtrVal
	 *            the exported KpiModelATtrVal
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Kpi modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	// TODO cambiare con i nuovi UDP VAlues
	/*
	 * public void entitiesAssociationsSbiKpiModelAttrVal(SbiKpiModelAttrVal exportedKpiModelAttrVal, SbiKpiModelAttrVal existingKpiModelAttrVal,Session
	 * sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) throws EMFUserError { logger.debug("IN"); // overwrite existging entities
	 *
	 * // set the model Map modelIdAss=metaAss.getModelIDAssociation(); if(exportedKpiModelAttrVal.getSbiKpiModel()!=null){ Integer
	 * oldModelId=exportedKpiModelAttrVal.getSbiKpiModel().getKpiModelId(); Integer newModelId=(Integer)modelIdAss.get(oldModelId); if(newModelId==null) {
	 * logger.error("could not find model associated with mdeol with label "+exportedKpiModelAttrVal.getSbiKpiModel().getKpiModelLabel());
	 * existingKpiModelAttrVal.setSbiKpiModel(null); } else{ // I must get the new SbiKpiModel newSbiKpiModel= (SbiKpiModel)
	 * sessionCurrDB.load(SbiKpiModel.class, newModelId); existingKpiModelAttrVal.setSbiKpiModel(newSbiKpiModel); } } else{
	 * existingKpiModelAttrVal.setSbiKpiModel(null); }
	 *
	 *
	 * // set the model attr Map modelAttrIdAss=metaAss.getSbiKpiModelAttrIDAssociation(); if(exportedKpiModelAttrVal.getSbiKpiModelAttr()!=null){ Integer
	 * oldModelAttrId=exportedKpiModelAttrVal.getSbiKpiModelAttr().getKpiModelAttrId(); Integer newModelAttrId=(Integer)modelAttrIdAss.get(oldModelAttrId);
	 * if(newModelAttrId==null) {
	 * logger.error("could not find model Attr associated with model Attrwith label "+exportedKpiModelAttrVal.getSbiKpiModelAttr().getKpiModelAttrCd());
	 * existingKpiModelAttrVal.setSbiKpiModelAttr(null); } else{ // I must get the new SbiKpiModelAttr newSbiKpiModelAttr= (SbiKpiModelAttr)
	 * sessionCurrDB.load(SbiKpiModelAttr.class, newModelAttrId); existingKpiModelAttrVal.setSbiKpiModelAttr(newSbiKpiModelAttr); } } else{
	 * existingKpiModelAttrVal.setSbiKpiModelAttr(null); }
	 *
	 * }
	 */

	/**
	 * Creates a new hibernate ObjMetadata.
	 *
	 * @param ObjMetadata
	 *            objMetadata
	 * @return the new hibernate parameter object
	 */
	public SbiObjMetadata makeNew(SbiObjMetadata objMetadata, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiObjMetadata newObjMetadata = new SbiObjMetadata();
		try {
			newObjMetadata.setLabel(objMetadata.getLabel());
			newObjMetadata.setName(objMetadata.getName());
			newObjMetadata.setDescription(objMetadata.getDescription());
			newObjMetadata.setCreationDate(objMetadata.getCreationDate());

			// associations
			entitiesAssociations(objMetadata, newObjMetadata, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error inc reating new ObjMetadata with label " + newObjMetadata.getLabel());
		} finally {

		}
		return newObjMetadata;
	}

	/**
	 * Load an existing ObjMetadata and make modifications as per the exported ObjMetadata in input
	 *
	 * @param ObjMetadata
	 *            objMetadata
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing ObjMetadata modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiObjMetadata modifyExisting(SbiObjMetadata exportedObjMetadata, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		SbiObjMetadata existingObjMetadata = null;
		try {
			existingObjMetadata = (SbiObjMetadata) sessionCurrDB.load(SbiObjMetadata.class, existingId);

			existingObjMetadata.setName(exportedObjMetadata.getName());
			existingObjMetadata.setDescription(exportedObjMetadata.getDescription());
			existingObjMetadata.setCreationDate(exportedObjMetadata.getCreationDate());

			// overwrite existging entities (maybe create a function speciic for domains, maybe not)
			entitiesAssociations(exportedObjMetadata, existingObjMetadata, sessionCurrDB, metaAss, importer);

		}

		finally {
			logger.debug("OUT");
		}
		return existingObjMetadata;
	}

	/**
	 * For ObjMetadata search new Ids
	 *
	 * @param exported
	 *            metadata ObjMetadata
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing Kpi modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiObjMetadata exportedObjMetadata, SbiObjMetadata existingObjMetadata, Session sessionCurrDB,
			MetadataAssociations metaAss, ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		// overwrite existging entities

		Map domainIdAss = metaAss.getDomainIDAssociation();
		if (exportedObjMetadata.getDataType() != null) {
			Integer oldDomainId = exportedObjMetadata.getDataType().getValueId();
			Integer newDomainId = (Integer) domainIdAss.get(oldDomainId);
			if (newDomainId == null) {
				logger.error("could not find domain between association" + exportedObjMetadata.getDataType().getValueCd());
				existingObjMetadata.setDataType(null);
			} else {
				// I must get the new SbiDomains object
				SbiDomains newSbiDomain = (SbiDomains) sessionCurrDB.load(SbiDomains.class, newDomainId);
				existingObjMetadata.setDataType(newSbiDomain);
			}
		} else {
			existingObjMetadata.setDataType(null);
		}
	}

	/**
	 * Creates a new hibernate SbiObjMetacontent object.
	 *
	 * @param iEngUserProfile
	 * @param SbiObjectMetacontent
	 *            metcontent
	 * @return the new hibernate parameter object
	 */
	public SbiObjMetacontents makeNew(SbiObjMetacontents metacontents, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer,
			IEngUserProfile iEngUserProfile) {
		logger.debug("IN");
		SbiObjMetacontents newMetacontents = new SbiObjMetacontents();
		try {
			newMetacontents.setCreationDate(metacontents.getCreationDate());
			newMetacontents.setLastChangeDate(metacontents.getLastChangeDate());
			// associations
			entitiesAssociations(metacontents, newMetacontents, sessionCurrDB, metaAss, importer, iEngUserProfile);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error in creating new metacontent with exported id " + metacontents.getObjMetacontentId());
		} finally {

		}
		return newMetacontents;
	}

	/**
	 * Load an existing ObjMetacontents and make modifications as per the exported ObjMetacontents in input
	 *
	 * @param exportedObjMetacontents
	 *            the exported ObjMetacontents
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @param userProfile
	 * @return the existing ObjMetacontents modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiObjMetacontents modifyExisting(SbiObjMetacontents exportedMetacontents, Session sessionCurrDB, Integer existingId, MetadataAssociations metaAss,
			ImporterMetadata importer, IEngUserProfile userProfile) throws EMFUserError {
		logger.debug("IN");
		SbiObjMetacontents existingMetacontents = null;
		try {
			// update Alarm
			existingMetacontents = (SbiObjMetacontents) sessionCurrDB.load(SbiObjMetacontents.class, existingId);

			existingMetacontents.setCreationDate(exportedMetacontents.getCreationDate());
			existingMetacontents.setLastChangeDate(exportedMetacontents.getLastChangeDate());

			// associations
			entitiesAssociations(exportedMetacontents, existingMetacontents, sessionCurrDB, metaAss, importer, userProfile);
		}

		finally {
			logger.debug("OUT");
		}
		return existingMetacontents;
	}

	/**
	 * For SBiObjMetacontents search new Ids
	 *
	 * @param exportedMetacontent
	 *            the exported SbiObjMetacontent
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param iEngUserProfile
	 * @return the existing ObjMetacontent modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiObjMetacontents exportedMetacontents, SbiObjMetacontents existingMetacontents, Session sessionCurrDB,
			MetadataAssociations metaAss, ImporterMetadata importer, IEngUserProfile profile) throws EMFUserError {
		logger.debug("IN");

		// overwrite existging entities

		// Obj Metadata
		Map metadataIdAss = metaAss.getObjMetadataIDAssociation();
		if (exportedMetacontents.getObjmetaId() != null) {
			Integer oldMetaId = exportedMetacontents.getObjmetaId();
			Integer newMetaId = (Integer) metadataIdAss.get(oldMetaId);
			if (newMetaId == null) {
				logger.error("could not find association with metadata with id " + exportedMetacontents.getObjmetaId());
			} else {
				existingMetacontents.setObjmetaId(newMetaId);
			}
		}

		// SbiObject
		Map objectIdAss = metaAss.getBIobjIDAssociation();
		if (exportedMetacontents.getSbiObjects() != null) {
			Integer oldObjId = exportedMetacontents.getSbiObjects().getBiobjId();
			Integer newObjId = (Integer) objectIdAss.get(oldObjId);
			if (newObjId == null) {
				logger.error("could not find object associated " + exportedMetacontents.getSbiObjects().getLabel());
			} else {
				SbiObjects newSbiObjects = (SbiObjects) sessionCurrDB.load(SbiObjects.class, newObjId);
				existingMetacontents.setSbiObjects(newSbiObjects);
			}
		}

		// SbiSubobject (if present)
		Map subObjectIdAss = metaAss.getObjSubObjectIDAssociation();
		if (exportedMetacontents.getSbiSubObjects() != null) {
			Integer oldSubObjId = exportedMetacontents.getSbiSubObjects().getSubObjId();
			Integer newSubObjId = (Integer) subObjectIdAss.get(oldSubObjId);
			if (newSubObjId == null) {
				logger.error("could not find subobject associated " + exportedMetacontents.getSbiSubObjects().getName());
			} else {
				SbiSubObjects newSbiSubObjects = (SbiSubObjects) sessionCurrDB.load(SbiSubObjects.class, newSubObjId);
				existingMetacontents.setSbiSubObjects(newSbiSubObjects);
			}
		}

		// Binary contents will be always inserted as new
		logger.debug("Insert the binary content associated");
		SbiBinContents exportedBinContent = exportedMetacontents.getSbiBinContents();
		SbiBinContents newBinContents = new SbiBinContents();
		newBinContents.setContent(exportedBinContent.getContent());

		SbiCommonInfo commonInfo = new SbiCommonInfo();

		String userid = (String) profile.getUserUniqueIdentifier();
		commonInfo.setUserIn(userid);
		commonInfo.setTimeIn(new Date());
		commonInfo.setSbiVersionIn(SbiCommonInfo.SBI_VERSION);
		commonInfo.setOrganization(((UserProfile) profile).getOrganization());
		newBinContents.setCommonInfo(commonInfo);

		sessionCurrDB.save(newBinContents);
		existingMetacontents.setSbiBinContents(newBinContents);

		logger.debug("OUT");
	}

	/**
	 * Creates a new hibernate SbiKpiRel object.
	 *
	 * @param SbiKpiRel
	 *            relation
	 * @return the new hibernate parameter object
	 */
	public SbiKpiRel makeNew(SbiKpiRel kpirel, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiKpiRel newSbiKpiRel = new SbiKpiRel();
		try {
			newSbiKpiRel.setParameter(kpirel.getParameter());

			// associations
			entitiesAssociations(kpirel, newSbiKpiRel, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error in creating new kpi relation with exported id " + newSbiKpiRel.getKpiRelId());
		}
		return newSbiKpiRel;
	}

	/**
	 * For SbiKpiRel search new Ids
	 *
	 * @param exportedKpiRel
	 *            the exported SbiKpiRel
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing KPIREL modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiKpiRel exportedKpiRel, SbiKpiRel existingKpiRel, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");

		// overwrite existing entities

		// kpi child
		Map kpiAss = metaAss.getKpiIDAssociation();
		if (exportedKpiRel.getSbiKpiByKpiChildId() != null) {
			Integer oldKpiId = exportedKpiRel.getSbiKpiByKpiChildId().getKpiId();
			Integer newKpiId = (Integer) kpiAss.get(oldKpiId);
			if (newKpiId == null) {
				logger.error("could not find association with kpi child with id " + exportedKpiRel.getSbiKpiByKpiChildId().getName());
				existingKpiRel.setSbiKpiByKpiChildId(null);
			} else {
				SbiKpi newSbiKpi = (SbiKpi) sessionCurrDB.load(SbiKpi.class, newKpiId);
				existingKpiRel.setSbiKpiByKpiChildId(newSbiKpi);
			}
		}
		// kpi father

		if (exportedKpiRel.getSbiKpiByKpiFatherId() != null) {
			Integer oldKpiId = exportedKpiRel.getSbiKpiByKpiFatherId().getKpiId();
			Integer newKpiId = (Integer) kpiAss.get(oldKpiId);
			if (newKpiId == null) {
				logger.error("could not find association with kpi father  with id " + exportedKpiRel.getSbiKpiByKpiFatherId().getName());
				existingKpiRel.setSbiKpiByKpiFatherId(null);
			} else {
				SbiKpi newSbiKpi = (SbiKpi) sessionCurrDB.load(SbiKpi.class, newKpiId);
				existingKpiRel.setSbiKpiByKpiFatherId(newSbiKpi);
			}
		}

		logger.debug("OUT");
	}

	/**
	 * Creates a new hibernate SbiUdp udp object.
	 *
	 * @param SbiUdp
	 *            udp
	 * @return the new hibernate parameter object
	 */
	public SbiUdp makeNew(SbiUdp udp, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiUdp newUdp = new SbiUdp();
		try {
			newUdp.setDescription(udp.getDescription());
			newUdp.setIsMultivalue(udp.isIsMultivalue());
			newUdp.setLabel(udp.getLabel());
			newUdp.setName(udp.getName());

			// associations
			entitiesAssociations(udp, newUdp, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error in creating new udp with exported id " + newUdp.getUdpId());
		}
		return newUdp;
	}

	/**
	 * For SbiUdp search new Ids
	 *
	 * @param exportedSbiUdp
	 *            the exported SbiUdp
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing SbiUdp modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiUdp exportedSbiUdp, SbiUdp existingSbiUdp, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");

		// overwrite existing entities
		// type id
		Map doaminAss = metaAss.getDomainIDAssociation();
		// original value of exported object != null
		if (exportedSbiUdp.getTypeId() != null) {
			Integer oldTypeId = exportedSbiUdp.getTypeId();
			Integer newTypeId = (Integer) doaminAss.get(oldTypeId);
			if (newTypeId == null) {
				logger.error("could not find association with domain type id with id " + exportedSbiUdp.getTypeId());
				existingSbiUdp.setTypeId(null);
			} else {
				existingSbiUdp.setTypeId(newTypeId);
			}
		}
		// family id
		if (exportedSbiUdp.getFamilyId() != null) {
			Integer oldFamilyId = exportedSbiUdp.getFamilyId();
			Integer newFamilyId = (Integer) doaminAss.get(oldFamilyId);
			if (newFamilyId == null) {
				logger.error("could not find association with domain Family Id with id " + exportedSbiUdp.getFamilyId());
				existingSbiUdp.setFamilyId(null);
			} else {
				existingSbiUdp.setFamilyId(newFamilyId);
			}
		}

		logger.debug("OUT");
	}

	/**
	 * Load an existing Udp and make modifications as per the exported udp in input
	 *
	 * @param exportedUdp
	 *            the exported SbiUdp
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingUdpid
	 *            the existing id
	 * @return the existing Udp modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiUdp modifyExisting(SbiUdp exportedUdp, Session sessionCurrDB, Integer existingUdpId) throws EMFUserError {
		logger.debug("IN");
		SbiUdp existingSbiUdp = null;
		try {
			existingSbiUdp = (SbiUdp) sessionCurrDB.load(SbiUdp.class, existingUdpId);

			existingSbiUdp.setDescription(exportedUdp.getDescription());
			existingSbiUdp.setIsMultivalue(exportedUdp.isIsMultivalue());
			existingSbiUdp.setLabel(exportedUdp.getLabel());
			existingSbiUdp.setName(exportedUdp.getName());

		}

		finally {
			logger.debug("OUT");
		}
		return existingSbiUdp;
	}

	/**
	 * Load an existing Kpi Relation and make modifications as per the exported udp in input
	 *
	 * @param exportedKpiRel
	 *            the exported SbiKpiRel
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingKpiRelId
	 *            the existing id
	 * @return the existing SbiKpiRel modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiKpiRel modifyExisting(SbiKpiRel exportedKpiRel, Session sessionCurrDB, Integer existingKpiRelId) throws EMFUserError {
		logger.debug("IN");
		SbiKpiRel existingKpiRel = null;
		try {
			existingKpiRel = (SbiKpiRel) sessionCurrDB.load(SbiKpiRel.class, existingKpiRelId);
			existingKpiRel.setParameter(exportedKpiRel.getParameter());

		}

		finally {
			logger.debug("OUT");
		}
		return existingKpiRel;
	}

	/**
	 * Creates a new hibernate SbiUdpValue udp value object.
	 *
	 * @param SbiUdpValue
	 *            udp value
	 * @return the new hibernate parameter object
	 */
	public SbiUdpValue makeNew(SbiUdpValue udpValue, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiUdpValue newUdpValue = new SbiUdpValue();
		try {
			newUdpValue.setBeginTs(udpValue.getBeginTs());
			newUdpValue.setEndTs(udpValue.getEndTs());
			newUdpValue.setFamily(udpValue.getFamily());
			newUdpValue.setLabel(udpValue.getLabel());
			newUdpValue.setName(udpValue.getName());
			newUdpValue.setProg(udpValue.getProg());
			newUdpValue.setFamily(udpValue.getFamily());
			newUdpValue.setValue(udpValue.getValue());
			// associations
			entitiesAssociations(udpValue, newUdpValue, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error in creating new udp value with exported id " + newUdpValue.getUdpValueId());
		}
		return newUdpValue;
	}

	/**
	 * For SbiUdpValue search new Ids
	 *
	 * @param exportedSbiUdpValue
	 *            the exported SbiUdpValue
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing SbiUdpValue modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiUdpValue exportedSbiUdpValue, SbiUdpValue existingSbiUdpValue, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");

		// overwrite existing entities

		// udp id
		Map udpAss = metaAss.getUdpAssociation();
		// original value of exported object != null
		if (exportedSbiUdpValue.getSbiUdp() != null) {
			Integer oldUdpId = exportedSbiUdpValue.getSbiUdp().getUdpId();
			Integer newUdpId = (Integer) udpAss.get(oldUdpId);
			if (newUdpId == null) {
				logger.error("could not find association with udp id with id " + oldUdpId);
				existingSbiUdpValue.setSbiUdp(null);
			} else {
				SbiUdp newSbiUdp = (SbiUdp) sessionCurrDB.load(SbiUdp.class, newUdpId);
				existingSbiUdpValue.setSbiUdp(newSbiUdp);
			}
		}
		// checks family
		if (exportedSbiUdpValue.getFamily() != null && exportedSbiUdpValue.getFamily().equalsIgnoreCase("Kpi")) {
			// reference id
			Map kpiAss = metaAss.getKpiIDAssociation();
			if (exportedSbiUdpValue.getReferenceId() != null) {
				Integer oldReferenceId = exportedSbiUdpValue.getReferenceId();
				Integer newReferenceId = (Integer) kpiAss.get(oldReferenceId);
				if (newReferenceId == null) {
					logger.error("could not find association with domain kpi reference Id with id " + exportedSbiUdpValue.getReferenceId());
					existingSbiUdpValue.setReferenceId(null);
				} else {
					existingSbiUdpValue.setReferenceId(newReferenceId);
				}
			}
		} else if (exportedSbiUdpValue.getFamily() != null && exportedSbiUdpValue.getFamily().equalsIgnoreCase("Model")) {
			// reference id
			Map modelAss = metaAss.getModelIDAssociation();
			if (exportedSbiUdpValue.getReferenceId() != null) {
				Integer oldReferenceId = exportedSbiUdpValue.getReferenceId();
				Integer newReferenceId = (Integer) modelAss.get(oldReferenceId);
				if (newReferenceId == null) {
					logger.error("could not find association with domain model reference Id with id " + exportedSbiUdpValue.getReferenceId());
					existingSbiUdpValue.setReferenceId(null);
				} else {
					existingSbiUdpValue.setReferenceId(newReferenceId);
				}
			}
		}

		logger.debug("OUT");
	}

	/**
	 * Load an existing Udp value and make modifications as per the exported udp in input
	 *
	 * @param exportedUdp
	 *            the exported SbiUdp
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingUdpid
	 *            the existing id
	 * @return the existing SbiUdpValue modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiUdpValue modifyExisting(SbiUdpValue exportedUdpValue, Session sessionCurrDB, Integer existingUdpValueId) throws EMFUserError {
		logger.debug("IN");
		SbiUdpValue existingSbiUdpValue = null;
		try {
			existingSbiUdpValue = (SbiUdpValue) sessionCurrDB.load(SbiUdpValue.class, existingUdpValueId);
			existingSbiUdpValue.setBeginTs(exportedUdpValue.getBeginTs());
			existingSbiUdpValue.setEndTs(exportedUdpValue.getEndTs());
			existingSbiUdpValue.setFamily(exportedUdpValue.getFamily());
			existingSbiUdpValue.setLabel(exportedUdpValue.getLabel());
			existingSbiUdpValue.setName(exportedUdpValue.getName());
			existingSbiUdpValue.setProg(exportedUdpValue.getProg());
			existingSbiUdpValue.setFamily(exportedUdpValue.getFamily());
			existingSbiUdpValue.setValue(exportedUdpValue.getValue());
		}

		finally {
			logger.debug("OUT");
		}
		return existingSbiUdpValue;
	}

	/**
	 * Creates a new hibernate SbiOrgUnitGrant grant object.
	 *
	 * @param SbiOrgUnitGrant
	 *            grant
	 * @return the new hibernate parameter object
	 */
	public SbiOrgUnitGrant makeNew(SbiOrgUnitGrant grant, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiOrgUnitGrant newGrant = new SbiOrgUnitGrant();
		try {
			newGrant.setDescription(grant.getDescription());
			newGrant.setEndDate(grant.getEndDate());
			newGrant.setLabel(grant.getLabel());
			newGrant.setName(grant.getName());
			newGrant.setStartDate(grant.getStartDate());
			newGrant.setIsAvailable(true);
			// associations
			entitiesAssociations(grant, newGrant, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error in creating new grant with exported id " + grant.getId());
		}
		return newGrant;
	}

	/**
	 * For SbiOrgUnitGrant search new Ids
	 *
	 * @param exportedSbiUdpValue
	 *            the exported SbiOrgUnitGrant
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing SbiOrgUnitGrant modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiOrgUnitGrant exportedGrant, SbiOrgUnitGrant existingGrant, Session sessionCurrDB, MetadataAssociations metaAss,
			ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");
		// model instance id
		Map modInstAss = metaAss.getModelInstanceIDAssociation();
		// original value of exported object != null
		if (exportedGrant.getSbiKpiModelInst() != null) {
			Integer oldMiId = exportedGrant.getSbiKpiModelInst().getKpiModelInst();
			Integer newMiId = (Integer) modInstAss.get(oldMiId);
			if (newMiId == null) {
				logger.error("could not find association with modelinstance id with id " + oldMiId);
			} else {
				SbiKpiModelInst newSbiMi = (SbiKpiModelInst) sessionCurrDB.load(SbiKpiModelInst.class, newMiId);
				existingGrant.setSbiKpiModelInst(newSbiMi);
			}
		}
		// hierarchy id
		Map ouHierAss = metaAss.getOuHierarchiesAssociation();
		// original value of exported object != null
		if (exportedGrant.getSbiOrgUnitHierarchies() != null) {
			Integer oldHierId = exportedGrant.getSbiOrgUnitHierarchies().getId();
			Integer newHierId = (Integer) ouHierAss.get(oldHierId);
			if (newHierId == null) {
				logger.error("could not find association with ou hierarchy id with id " + oldHierId);
			} else {
				SbiOrgUnitHierarchies newSbiHier = (SbiOrgUnitHierarchies) sessionCurrDB.load(SbiOrgUnitHierarchies.class, newHierId);
				existingGrant.setSbiOrgUnitHierarchies(newSbiHier);
			}
		}

		logger.debug("OUT");
	}

	/**
	 * Load an existing grant and make modifications as per the exported grant in input
	 *
	 * @param exportedGrant
	 *            the exported grant
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing grant modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiOrgUnitGrant modifyExisting(SbiOrgUnitGrant exportedGrant, Session sessionCurrDB, Integer existingId) throws EMFUserError {
		logger.debug("IN");
		SbiOrgUnitGrant existingGrant = null;
		try {
			// update Grant
			existingGrant = (SbiOrgUnitGrant) sessionCurrDB.load(SbiOrgUnitGrant.class, existingId);
			existingGrant.setDescription(exportedGrant.getDescription());
			existingGrant.setEndDate(exportedGrant.getEndDate());
			existingGrant.setLabel(exportedGrant.getLabel());
			existingGrant.setName(exportedGrant.getName());
			existingGrant.setStartDate(exportedGrant.getStartDate());
			existingGrant.setIsAvailable(true);
		}

		finally {
			logger.debug("OUT");
		}
		return existingGrant;
	}

	/**
	 * Creates a new hibernate SbiOrgUnitGrantNodes grant node object.
	 *
	 * @param SbiOrgUnitGrantNodes
	 *            grant node
	 * @return the new hibernate parameter object
	 */
	public SbiOrgUnitGrantNodes makeNew(SbiOrgUnitGrantNodes grantNode, Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) {
		logger.debug("IN");
		SbiOrgUnitGrantNodes newGrantNode = new SbiOrgUnitGrantNodes();
		try {
			SbiOrgUnitGrantNodesId id = new SbiOrgUnitGrantNodesId();
			// associations
			entitiesAssociations(id, grantNode, newGrantNode, sessionCurrDB, metaAss, importer);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error in creating new grant node with exported grant id " + grantNode.getId().getGrantId());
		}
		return newGrantNode;
	}

	/**
	 * For SbiOrgUnitGrantNodes search new Ids
	 *
	 * @param exportedGrantNode
	 *            the exported SbiOrgUnitGrantNodes
	 * @param sessionCurrDB
	 *            the session curr db
	 * @return the existing SbiOrgUnitGrantNodes modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void entitiesAssociations(SbiOrgUnitGrantNodesId id, SbiOrgUnitGrantNodes exportedGrantNode, SbiOrgUnitGrantNodes existingGrantNode,
			Session sessionCurrDB, MetadataAssociations metaAss, ImporterMetadata importer) throws EMFUserError {
		logger.debug("IN");

		// model instance id
		Map modInstAss = metaAss.getModelInstanceIDAssociation();
		// original value of exported object != null
		if (exportedGrantNode.getSbiKpiModelInst() != null) {
			Integer oldMiId = exportedGrantNode.getSbiKpiModelInst().getKpiModelInst();
			Integer newMiId = (Integer) modInstAss.get(oldMiId);
			if (newMiId == null) {
				logger.error("could not find association with modelinstance id with id " + oldMiId);
			} else {
				SbiKpiModelInst newSbiMi = (SbiKpiModelInst) sessionCurrDB.load(SbiKpiModelInst.class, newMiId);
				exportedGrantNode.setSbiKpiModelInst(newSbiMi);
				id.setKpiModelInstNodeId(newSbiMi.getKpiModelInst());
			}
		}
		// grant id
		Map grantAss = metaAss.getOuGrantAssociation();
		// original value of exported object != null
		if (exportedGrantNode.getSbiOrgUnitGrant() != null) {
			Integer oldGrantId = exportedGrantNode.getSbiOrgUnitGrant().getId();
			Integer newGrantId = (Integer) grantAss.get(oldGrantId);
			if (newGrantId == null) {
				logger.error("could not find association with grant id with id " + oldGrantId);
			} else {
				SbiOrgUnitGrant newSbiGrant = (SbiOrgUnitGrant) sessionCurrDB.load(SbiOrgUnitGrant.class, newGrantId);
				exportedGrantNode.setSbiOrgUnitGrant(newSbiGrant);
				id.setGrantId(newSbiGrant.getId());
			}
		}
		// node id
		Map nodeAss = metaAss.getOuNodeAssociation();
		// original value of exported object != null
		if (exportedGrantNode.getSbiOrgUnitNodes() != null) {
			Integer oldNodeId = exportedGrantNode.getSbiOrgUnitNodes().getNodeId();
			Integer newNodeId = (Integer) nodeAss.get(oldNodeId);
			if (newNodeId == null) {
				logger.error("could not find association with node id with id " + oldNodeId);
			} else {
				SbiOrgUnitNodes newSbiNode = (SbiOrgUnitNodes) sessionCurrDB.load(SbiOrgUnitNodes.class, newNodeId);
				exportedGrantNode.setSbiOrgUnitNodes(newSbiNode);
				id.setNodeId(newSbiNode.getNodeId());
			}
		}
		existingGrantNode.setId(id);
		logger.debug("OUT");
	}

	/**
	 * Load an existing grant node and make modifications as per the exported grant node in input
	 *
	 * @param exportedGrantNode
	 *            the exported grant node
	 * @param sessionCurrDB
	 *            the session curr db
	 * @param existingId
	 *            the existing id
	 * @return the existing grant modified as per the exported parameter in input
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public SbiOrgUnitGrantNodes modifyExisting(SbiOrgUnitGrantNodes exportedGrantNode, Session sessionCurrDB, SbiOrgUnitGrantNodesId existingId)
			throws EMFUserError {
		logger.debug("IN");
		SbiOrgUnitGrantNodes existingGrantNode = null;
		try {
			// update Grant node
			existingGrantNode = (SbiOrgUnitGrantNodes) sessionCurrDB.load(SbiOrgUnitGrantNodes.class, existingId);

		}

		finally {
			logger.debug("OUT");
		}
		return existingGrantNode;
	}

	/**
	 * Creates a new hibernate authorizations (role) object.
	 *
	 * @param role
	 *            old hibernate authorizations object
	 * @return the new hibernate authorizations object
	 */
	public SbiAuthorizations makeNew(SbiAuthorizations func) {
		logger.debug("IN");
		SbiAuthorizations newAuthorizations = new SbiAuthorizations();
		newAuthorizations.setName(func.getName());
		newAuthorizations.setCommonInfo(func.getCommonInfo());
		logger.debug("OUT");
		return newAuthorizations;
	}

	public List<String> retrieveLovMetadataById(SbiLov sbiLov) {
		logger.debug("IN");

		// ma pcontaining lovs metadata; isnerted as lov label: then a vector of metadata with name and type
		List<String> lovsMetadata = new ArrayList<String>();

		// String hql = "from SbiLov lov where lovId = "+lovId;
		// Query hqlQuery = sessionCurrDB.createQuery(hql);
		// SbiLov sbiLov = (SbiLov)hqlQuery.uniqueResult();
		// // GET METADATA
		String dataDefinition = sbiLov.getLovProvider().trim();
		try {
			SourceBean source = SourceBean.fromXMLString(dataDefinition);
			SourceBean visCol = (SourceBean) source.getAttribute("VISIBLE-COLUMNS");
			SourceBean invisCol = (SourceBean) source.getAttribute("INVISIBLE-COLUMNS");
			String visibleColumns = visCol.getCharacters();
			String invisibleColumns = visCol.getCharacters();
			if ((visibleColumns != null) && !visibleColumns.trim().equalsIgnoreCase("")) {
				String[] visColArr = visibleColumns.split(",");
				for (int i = 0; i < visColArr.length; i++) {
					lovsMetadata.add(visColArr[i]);
				}
			}
			List invisColNames = new ArrayList<String>();
			if ((invisibleColumns != null) && !invisibleColumns.trim().equalsIgnoreCase("")) {
				String[] invisColArr = invisibleColumns.split(",");
				for (int i = 0; i < invisColArr.length; i++) {
					lovsMetadata.add(invisColArr[i]);
				}
			}
			logger.debug("insert metadata for lov with label " + sbiLov.getLabel());
		} catch (SourceBeanException e) {
			logger.error("Error in reading XML of metadata for LOV with label " + sbiLov.getLabel() + "; metadata wil  be put to null");
		}
		logger.debug("OUT");
		return lovsMetadata;
	}

	private SbiDataSetId getDataSetKey(Session aSession, SbiDataSet dataSet, boolean isInsert, UserProfile profile) {
		SbiDataSetId toReturn = new SbiDataSetId();
		// get the next id or version num of the dataset managed
		Integer maxId = null;
		Integer nextId = null;
		String hql = null;
		Query query = null;
		if (isInsert) {
			hql = " select max(sb.id.dsId) as maxId from SbiDataSet sb ";
			toReturn.setVersionNum(new Integer("1"));
			query = aSession.createQuery(hql);
		} else {
			hql = " select max(sb.id.versionNum) as maxId from SbiDataSet sb where sb.label = ? ";
			query = aSession.createQuery(hql);
			query.setString(0, dataSet.getLabel());
			toReturn.setDsId(dataSet.getId().getDsId());
		}

		List result = query.list();
		Iterator it = result.iterator();
		while (it.hasNext()) {
			maxId = (Integer) it.next();
		}
		logger.debug("Current max prog : " + maxId);
		if (maxId == null) {
			nextId = new Integer(1);
		} else {
			nextId = new Integer(maxId.intValue() + 1);
		}

		if (isInsert) {
			logger.debug("Nextid: " + nextId);
			toReturn.setDsId(nextId);
		} else {
			logger.debug("NextVersion: " + nextId);
			toReturn.setVersionNum(nextId);
		}
		toReturn.setOrganization(profile.getOrganization());

		return toReturn;
	}

	public IEngUserProfile getProfile() {
		return profile;
	}

	public void setProfile(IEngUserProfile profile) {
		this.profile = profile;
	}

	public Map<Integer, SbiDataSet> getDatasetMap() {
		return datasetMap;
	}

	public void setDatasetMap(Map<Integer, SbiDataSet> datasetMap) {
		this.datasetMap = datasetMap;
	}

	public MetadataLogger getMetaLog() {
		return metaLog;
	}

	public void setMetaLog(MetadataLogger metaLog) {
		this.metaLog = metaLog;
	}

}
