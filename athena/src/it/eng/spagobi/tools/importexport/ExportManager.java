/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISubreportDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.behaviouralmodel.check.bo.Check;
import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;
import it.eng.spagobi.behaviouralmodel.lov.bo.QueryDetail;
import it.eng.spagobi.behaviouralmodel.lov.dao.IModalitiesValueDAO;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiAuthorizations;
import it.eng.spagobi.commons.utilities.FileUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNode;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.importexport.typesmanager.ITypesExportManager;
import it.eng.spagobi.tools.importexport.typesmanager.TypesExportManagerFactory;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;
import it.eng.spagobi.tools.udp.bo.Udp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Implements the interface which defines methods for managing the export
 * requests
 */
public class ExportManager implements IExportManager {

	static private Logger logger = Logger.getLogger(ExportManager.class);
	private String nameExportFile = "";
	private String pathExportFolder = "";
	private String pathBaseFolder = "";
	private String pathDBFolder = "";
	private String pathContentFolder = "";
	private SessionFactory sessionFactory = null;
	private Session session = null;
	private ExporterMetadata exporter = null;
	private boolean exportSubObjects = false;
	private boolean exportSnapshots = false;
	private List objectsInserted = null;

	/**
	 * Prepare the environment for export.
	 * 
	 * @param pathExpFold
	 *            Path of the export folder
	 * @param nameExpFile
	 *            the name to give to the exported file
	 * @param expSubObj
	 *            Flag which tells if it's necessary to export subobjects
	 * @param expSnaps
	 *            Flag which tells if it's necessary to export snapshots
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void prepareExport(String pathExpFold, String nameExpFile, boolean expSubObj, boolean expSnaps) throws EMFUserError {
		logger.debug("IN. pathExpFold=" + pathExpFold + " nameExpFile=" + nameExpFile + " expSubObj=" + expSubObj + " expSnaps" + expSnaps);
		try {
			nameExportFile = nameExpFile;
			pathExportFolder = pathExpFold;
			exportSubObjects = expSubObj;
			exportSnapshots = expSnaps;
			if (pathExportFolder.endsWith("/") || pathExportFolder.endsWith("\\")) {
				pathExportFolder = pathExportFolder.substring(0, pathExportFolder.length() - 1);
			}
			pathBaseFolder = pathExportFolder + "/" + nameExportFile;
			File baseFold = new File(pathBaseFolder);
			// if folder exist delete it
			if (baseFold.exists()) {
				FileUtilities.deleteDir(baseFold);
			}
			baseFold.mkdirs();
			pathDBFolder = pathBaseFolder + "/metadata";
			File dbFold = new File(pathDBFolder);
			dbFold.mkdirs();
			pathContentFolder = pathBaseFolder + "/contents";
			File contFold = new File(pathContentFolder);
			contFold.mkdirs();
			ExportUtilities.copyMetadataScript(pathDBFolder);
			ExportUtilities.copyMetadataScriptProperties(pathDBFolder);
			sessionFactory = ExportUtilities.getHibSessionExportDB(pathDBFolder);
			session = sessionFactory.openSession();
			exporter = new ExporterMetadata();
			objectsInserted = new ArrayList();
		} catch (Exception e) {
			logger.error("Error while creating export environment ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Exports objects and creates the archive export file.
	 * 
	 * @param objIds
	 *            the obj ids
	 * 
	 * @throws EMFUserError
	 *             the EMF user error
	 */
	public void exportObjects(List objIds) throws EMFUserError {
		logger.debug("IN");
		try {
			exportPropertiesFile();
			logger.debug("export domains ");
			exportDomains();

			logger.debug("export authorizations ");
			// exportAuthorizations();

			logger.debug("export metadata categories");
			exportObjectMetadata();
			logger.debug("export udp");
			exportUdp();
			logger.debug("export organizational units");
			exportOu();
			logger.debug("export ou hierarchies");
			exportOuHierarchies();
			logger.debug("export ou nodes");
			exportOuNodes();

			Iterator iterObjs = objIds.iterator();
			while (iterObjs.hasNext()) {
				String idobj = (String) iterObjs.next();
				exportSingleObj(idobj);
			}
			closeSession();
			// createExportArchive();
			// deleteTmpFolder();
		} catch (EMFUserError emfue) {
			throw emfue;
		} catch (Exception e) {
			logger.error("Error while exporting objects ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Delete the temporary folder created for export purpose
	 */
	private void deleteTmpFolder() {
		logger.debug("IN");
		String folderTmpPath = pathExportFolder + "/" + nameExportFile;
		File folderTmp = new File(folderTmpPath);
		FileUtilities.deleteDir(folderTmp);
		logger.debug("OUT");
	}

	/**
	 * Creates the compress export file
	 * 
	 * @throws EMFUserError
	 */
	public void createExportArchive() throws EMFUserError {
		logger.debug("IN");
		FileOutputStream fos = null;
		ZipOutputStream out = null;
		String archivePath = pathExportFolder + "/" + nameExportFile + ".zip";
		try {
			File archiveFile = new File(archivePath);
			if (archiveFile.exists()) {
				archiveFile.delete();
			}
			fos = new FileOutputStream(archivePath);
			out = new ZipOutputStream(fos);
			compressFolder(pathBaseFolder, out);
			out.flush();
		} catch (Exception e) {
			logger.error("Error while creating archive file ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (fos != null) {
					fos.close();
				}
			} catch (Exception e) {
				logger.error("Error while closing streams ", e);
			}
			logger.debug("OUT");
		}
		deleteTmpFolder();
	}

	/**
	 * Compress contents of a folder into an output stream
	 * 
	 * @param pathFolder
	 *            The path of the folder to compress
	 * @param out
	 *            The Compress output stream
	 * @throws EMFUserError
	 */
	private void compressFolder(String pathFolder, ZipOutputStream out) throws EMFUserError {
		logger.debug("IN");
		File folder = new File(pathFolder);
		String[] entries = folder.list();
		byte[] buffer = new byte[4096];
		int bytes_read;
		FileInputStream in = null;
		try {
			for (int i = 0; i < entries.length; i++) {
				File f = new File(folder, entries[i]);
				if (f.isDirectory()) {
					compressFolder(pathFolder + "/" + f.getName(), out);
				} else {
					in = new FileInputStream(f);
					String completeFileName = pathFolder + "/" + f.getName();
					String relativeFileName = f.getName();
					if (completeFileName.lastIndexOf(pathExportFolder) != -1) {
						int index = completeFileName.lastIndexOf(pathExportFolder);
						int len = pathExportFolder.length();
						relativeFileName = completeFileName.substring(index + len + 1);
					}
					ZipEntry entry = new ZipEntry(relativeFileName);
					out.putNextEntry(entry);
					while ((bytes_read = in.read(buffer)) != -1)
						out.write(buffer, 0, bytes_read);
					in.close();
				}
			}
		} catch (Exception e) {
			logger.error("Error while creating archive file ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				logger.error("Error while closing streams ", e);
			}
			logger.debug("OUT");
		}
	}

	/**
	 * Creates the export properties file
	 * 
	 * @throws EMFUserError
	 */
	private void exportPropertiesFile() throws EMFUserError {
		logger.debug("IN");
		FileOutputStream fos = null;
		try {
			String propFilePath = pathBaseFolder + "/export.properties";
			fos = new FileOutputStream(propFilePath);
			ConfigSingleton config = ConfigSingleton.getInstance();
			SourceBean currentVersionSB = (SourceBean) config.getAttribute("IMPORTEXPORT.CURRENTVERSION");
			String version = (String) currentVersionSB.getAttribute("version");
			String properties = "spagobi-version=" + version + "\n";
			fos.write(properties.getBytes());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			logger.error("Error while exporting properties file ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (Exception e) {
				logger.error("Error while closing streams ", e);
			}
			logger.debug("OUT");
		}
	}

	/**
	 * Exports SpagoBI Domain Objects
	 * 
	 * @throws EMFUserError
	 */
	private void exportDomains() throws EMFUserError {
		logger.debug("IN");
		try {
			List domains = DAOFactory.getDomainDAO().loadListDomains();
			Iterator itDom = domains.iterator();
			while (itDom.hasNext()) {
				Domain dom = (Domain) itDom.next();
				exporter.insertDomain(dom, session);
			}
		} catch (Exception e) {
			logger.error("Error while exporting domains ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Exports SpagoBI authorizations
	 * 
	 * @throws EMFUserError
	 */
	private void exportAuthorizations() throws EMFUserError {
		logger.debug("IN");
		try {
			List authorizations = DAOFactory.getRoleDAO().loadAllAuthorizations();
			Iterator itAuth = authorizations.iterator();
			while (itAuth.hasNext()) {
				SbiAuthorizations auth = (SbiAuthorizations) itAuth.next();
				exporter.insertAuthorizations(auth, session);
			}
		} catch (Exception e) {
			logger.error("Error while exporting domains ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Exports SpagoBI Udp Items
	 * 
	 * @throws EMFUserError
	 */
	private void exportUdp() throws EMFUserError {
		logger.debug("IN");
		try {
			List udpFamilies = DAOFactory.getDomainDAO().loadListDomainsByType("UDP_FAMILY");
			if (udpFamilies != null) {
				for (int i = 0; i < udpFamilies.size(); i++) {
					Domain type = (Domain) udpFamilies.get(i);
					// kpi udp/model udp
					List<Udp> udpList = DAOFactory.getUdpDAO().loadAllByFamily(type.getValueCd());
					if (udpList != null && !udpList.isEmpty()) {
						for (Iterator iterator = udpList.iterator(); iterator.hasNext();) {
							Udp udp = (Udp) iterator.next();
							exporter.insertUdp(udp, session);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while exporting udp ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Exports SpagoBI OU Items
	 * 
	 * @throws EMFUserError
	 */
	private void exportOu() throws EMFUserError {
		logger.debug("IN");
		try {

			List<OrganizationalUnit> ouList = DAOFactory.getOrganizationalUnitDAO().getOrganizationalUnitList();
			if (ouList != null && !ouList.isEmpty()) {
				for (Iterator iterator = ouList.iterator(); iterator.hasNext();) {
					OrganizationalUnit ou = (OrganizationalUnit) iterator.next();
					exporter.insertOu(ou, session);
				}
			}

		} catch (Exception e) {
			logger.error("Error while exporting ou ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Exports SpagoBI OU Hierarchies Items
	 * 
	 * @throws EMFUserError
	 */
	private void exportOuHierarchies() throws EMFUserError {
		logger.debug("IN");
		try {

			List<OrganizationalUnitHierarchy> hierList = DAOFactory.getOrganizationalUnitDAO().getHierarchiesList();
			if (hierList != null && !hierList.isEmpty()) {
				for (Iterator iterator = hierList.iterator(); iterator.hasNext();) {
					OrganizationalUnitHierarchy hier = (OrganizationalUnitHierarchy) iterator.next();
					exporter.insertHierarchy(hier, session);
				}
			}

		} catch (Exception e) {
			logger.error("Error while exporting hierarchy ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Exports SpagoBI OU nodes Items
	 * 
	 * @throws EMFUserError
	 */
	private void exportOuNodes() throws EMFUserError {
		logger.debug("IN");
		try {

			List<OrganizationalUnitNode> nodesList = DAOFactory.getOrganizationalUnitDAO().getNodes();
			if (nodesList != null && !nodesList.isEmpty()) {
				for (Iterator iterator = nodesList.iterator(); iterator.hasNext();) {
					OrganizationalUnitNode node = (OrganizationalUnitNode) iterator.next();
					exporter.insertOuNode(node, session);
				}
			}

		} catch (Exception e) {
			logger.error("Error while exporting ou nodes ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Exports SpagoBI Object Metadata (metadata categories)
	 * 
	 * @throws EMFUserError
	 */
	private void exportObjectMetadata() throws EMFUserError {
		logger.debug("IN");
		try {
			List metadatas = DAOFactory.getObjMetadataDAO().loadAllObjMetadata();

			Iterator itM = metadatas.iterator();
			while (itM.hasNext()) {
				ObjMetadata meta = (ObjMetadata) itM.next();
				exporter.insertObjMetadata(meta, session);
			}
		} catch (Exception e) {
			logger.error("Error while exporting object metadata ", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Export A single SpagoBI BiObject with Template, SubObject and Snapshot
	 * 
	 * @param idObj
	 *            The idObj to export
	 * @throws EMFUserError
	 */
	public void exportSingleObj(String idObj) throws EMFUserError {
		logger.debug("IN");

		if (objectsInserted.contains(Integer.valueOf(idObj))) {
			logger.warn("object already inserted");
			return;
		}

		try {
			if ((idObj == null) || idObj.trim().equals(""))
				return;
			IBIObjectDAO biobjDAO = DAOFactory.getBIObjectDAO();
			BIObject biobj = biobjDAO.loadBIObjectForDetail(new Integer(idObj));

			IDataSourceDAO dataSourceDao = DAOFactory.getDataSourceDAO();
			IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
			// Data source, if present
			Integer objataSourceId = biobj.getDataSourceId();
			if (objataSourceId != null) {
				IDataSource ds = dataSourceDao.loadDataSourceByID(objataSourceId);
				exporter.insertDataSource(ds, session);
			}

			// Data set if present
			Integer objDataSetId = biobj.getDataSetId();
			if (objDataSetId != null) {

				IDataSet genericDs = dataSetDao.loadDataSetById(objDataSetId);
				if (genericDs != null) {
					// exporter.insertDataSet(genericDs, session);

					// when inserting dataset in export must mantain same id and
					// version to preserve costraints
					exporter.insertDataSetAndDataSource(genericDs, session);
				}

			}

			// Engine if present, and data source if engine uses data source
			Engine engine = biobj.getEngine();
			// if (engine.getUseDataSource() && engine.getDataSourceId() !=
			// null) {
			// Integer engineDataSourceId = engine.getDataSourceId();
			// IDataSource ds =
			// dataSourceDao.loadDataSourceByID(engineDataSourceId);
			// exporter.insertDataSource(ds, session);
			// }

			exporter.insertEngine(engine, session);
			exporter.insertBIObject(biobj, session, false); // do not insert
															// dataset

			logger.debug("Export metadata associated to the object");
			IObjMetacontentDAO objMetacontentDAO = DAOFactory.getObjMetacontentDAO();
			// get metacontents associated to object
			List metacontents = objMetacontentDAO.loadObjOrSubObjMetacontents(biobj.getId(), null);
			for (Iterator iterator = metacontents.iterator(); iterator.hasNext();) {
				ObjMetacontent metacontent = (ObjMetacontent) iterator.next();
				exporter.insertObjMetacontent(metacontent, session);
			}

			// if the document is a chart, export the relevant dataset that is
			// referenced by the template
			boolean isChart = false;
			if (biobj.getBiObjectTypeCode().equalsIgnoreCase("DASH") && engine.getClassName() != null
					&& engine.getClassName().equals("it.eng.spagobi.engines.chart.SpagoBIChartInternalEngine")) {
				isChart = true;
			}

			if (isChart) {
				ObjTemplate template = biobj.getActiveTemplate();
				if (template != null) {
					try {
						byte[] tempFileCont = template.getContent();
						String tempFileStr = new String(tempFileCont);
						SourceBean tempFileSB = SourceBean.fromXMLString(tempFileStr);
						SourceBean datasetnameSB = (SourceBean) tempFileSB.getFilteredSourceBeanAttribute("CONF.PARAMETER", "name", "confdataset");
						if (datasetnameSB != null) {
							String datasetLabel = (String) datasetnameSB.getAttribute("value");
							IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
							IDataSet dataset = datasetDao.loadDataSetByLabel(datasetLabel);
							IDataSet guiGenericDataSet = datasetDao.loadDataSetByLabel(datasetLabel);

							if (dataset == null) {
								logger.warn("Error while exporting dashboard with id " + idObj + " and label " + biobj.getLabel() + " : "
										+ "the template refers to a dataset with label " + datasetLabel + " that does not exist!");
							} else {
								exporter.insertDataSet(guiGenericDataSet, session, false);
							}
						}
					} catch (Exception e) {
						logger.error("Error while exporting dashboard with id " + idObj + " and label " + biobj.getLabel() + " : "
								+ "could not find dataset reference in its template.");
					}
				}
			}

			// use Types Manager to handle specific export types, by now only
			// KPI and CONSOLE.. TODO with all types

			ITypesExportManager typeManager = TypesExportManagerFactory.createTypesExportManager(biobj, engine, exporter, this);
			// if null means it is not defined
			if (typeManager != null)
				typeManager.manageExport(biobj, session);

			// if it is qb, registry or smartfilter export associated model

			// maps kpi export
			// boolean isKpi = false;
			// if (biobj.getBiObjectTypeCode().equalsIgnoreCase("KPI")
			// && engine.getClassName() != null &&
			// engine.getClassName().equals("it.eng.spagobi.engines.kpi.SpagoBIKpiInternalEngine"))
			// {
			// isKpi = true;
			// }

			// if (isKpi) {
			// List objsToInsert=new ArrayList();
			// ObjTemplate template = biobj.getActiveTemplate();
			// if (template != null) {
			// try {
			// byte[] tempFileCont = template.getContent();
			// String tempFileStr = new String(tempFileCont);
			// SourceBean tempFileSB = SourceBean.fromXMLString(tempFileStr);
			//
			//
			// String modelInstanceLabel = (String)
			// tempFileSB.getAttribute("model_node_instance");
			//
			// // biObjectToInsert keeps track of objects that have to be
			// inserted beacuse related to Kpi
			//
			// if (modelInstanceLabel != null) {
			// IModelInstanceDAO modelInstanceDao =
			// DAOFactory.getModelInstanceDAO();
			// ModelInstance modelInstance =
			// modelInstanceDao.loadModelInstanceWithoutChildrenByLabel(modelInstanceLabel);
			// if (modelInstance == null) {
			// logger.warn("Error while exporting kpi with id " + idObj +
			// " and label " + biobj.getLabel() + " : " +
			// "the template refers to a Model Instance with label " +
			// modelInstanceLabel + " that does not exist!");
			// } else {
			// objsToInsert=exporter.insertAllFromModelInstance(modelInstance,
			// session);
			// //exporter.insertModelInstance(modelInstance, session);
			// }
			// }
			// } catch (Exception e) {
			// logger.error("Error while exporting kpi with id " + idObj +
			// " and label " + biobj.getLabel());
			// throw new EMFUserError(EMFErrorSeverity.ERROR, "8010",
			// ImportManager.messageBundle);
			//
			// }
			// }
			//
			// for (Iterator iterator = objsToInsert.iterator();
			// iterator.hasNext();) {
			// Integer id = (Integer) iterator.next();
			// BIObject obj=(BIObject)biobjDAO.loadBIObjectById(id);
			// if(obj!=null){
			// exportSingleObj(obj.getId().toString());
			// }
			// else{
			// logger.error("Could not find object with id"+id);
			// }
			// }
			//
			// }

			// maps catalogue export
			boolean isMap = false;
			if (biobj.getBiObjectTypeCode().equalsIgnoreCase("MAP"))
				isMap = true;
			if (isMap) {
				exporter.insertMapCatalogue(session);
			}

			if (exportSubObjects) {
				ISubObjectDAO subDao = DAOFactory.getSubObjectDAO();
				List subObjectLis = subDao.getSubObjects(biobj.getId());
				if (subObjectLis != null && !subObjectLis.isEmpty())
					exporter.insertAllSubObject(biobj, subObjectLis, session);
			}
			if (exportSnapshots) {
				ISnapshotDAO subDao = DAOFactory.getSnapshotDAO();
				List snapshotLis = subDao.getSnapshots(biobj.getId());
				if (snapshotLis != null && !snapshotLis.isEmpty())
					exporter.insertAllSnapshot(biobj, snapshotLis, session);
			}

			// insert functionalities and association with object
			List functs = biobj.getFunctionalities();
			Iterator iterFunct = functs.iterator();
			while (iterFunct.hasNext()) {
				Integer functId = (Integer) iterFunct.next();
				ILowFunctionalityDAO lowFunctDAO = DAOFactory.getLowFunctionalityDAO();
				LowFunctionality funct = lowFunctDAO.loadLowFunctionalityByID(functId, false);
				if (funct.getCodType().equals(SpagoBIConstants.USER_FUNCTIONALITY_TYPE_CODE)) {
					logger.debug("User folder [" + funct.getPath() + "] will be not exported.");
					// if the folder is a personal folder, it is not exported
					continue;
				}
				exporter.insertFunctionality(funct, session);
				exporter.insertObjFunct(biobj, funct, session);
			}
			// export parameters
			List biparams = biobjDAO.getBIObjectParameters(biobj);
			exportBIParamsBIObj(biparams, biobj);
			// export parameters dependecies
			exporter.insertBiParamDepend(biparams, session);

			// export parameters visual dependecies
			exporter.insertBiParamViewDepend(biparams, session);

			// export viewPoints
			exporter.insertBiViewpoints(biobj, session);

			// export subReport relation
			ISubreportDAO subRepDao = DAOFactory.getSubreportDAO();
			List subList = subRepDao.loadSubreportsByMasterRptId(biobj.getId());
			Iterator itersub = subList.iterator();
			while (itersub.hasNext()) {
				Subreport subRep = (Subreport) itersub.next();
				exporter.insertSubReportAssociation(subRep, session);
				exportSingleObj(subRep.getSub_rpt_id().toString());
			}
			objectsInserted.add(Integer.valueOf(idObj));
		} catch (EMFUserError emfue) {
			throw emfue;
		} catch (Exception e) {
			logger.error("Error while exporting document with id " + idObj + " :", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * Exports the BIParameters of a BIObject
	 * 
	 * @param biparams
	 *            List ot the BIParameters belonging to the BIObject
	 * @param biobj
	 *            The BIObject to which the parametes belong
	 * @throws EMFUserError
	 */
	private void exportBIParamsBIObj(List biparams, BIObject biobj) throws EMFUserError {
		logger.debug("IN");
		Iterator iterBIParams = biparams.iterator();
		while (iterBIParams.hasNext()) {
			BIObjectParameter biparam = (BIObjectParameter) iterBIParams.next();
			IParameterDAO parDAO = DAOFactory.getParameterDAO();
			Parameter param = parDAO.loadForDetailByParameterID(biparam.getParameter().getId());
			exporter.insertParameter(param, session);
			exporter.insertBIObjectParameter(biparam, session);
			IParameterUseDAO paruseDAO = DAOFactory.getParameterUseDAO();
			List paruses = paruseDAO.loadParametersUseByParId(param.getId());
			exportParUses(paruses);
		}
		logger.debug("OUT");
	}

	/**
	 * Export a list ot Parameter use Objects
	 * 
	 * @param paruses
	 *            The list of parameter use objects
	 * @throws EMFUserError
	 */
	private void exportParUses(List paruses) throws EMFUserError {
		logger.debug("IN");
		Iterator iterUses = paruses.iterator();
		while (iterUses.hasNext()) {
			ParameterUse paruse = (ParameterUse) iterUses.next();
			Integer idLov = paruse.getIdLov();
			if (idLov != null) {
				IModalitiesValueDAO lovDAO = DAOFactory.getModalitiesValueDAO();
				ModalitiesValue lov = lovDAO.loadModalitiesValueByID(idLov);
				checkDataSource(lov);
				exporter.insertLov(lov, session);
			}
			Integer idLovForDefault = paruse.getIdLovForDefault();
			if (idLovForDefault != null) {
				IModalitiesValueDAO lovDAO = DAOFactory.getModalitiesValueDAO();
				ModalitiesValue lov = lovDAO.loadModalitiesValueByID(idLovForDefault);
				checkDataSource(lov);
				exporter.insertLov(lov, session);
			}
			exporter.insertParUse(paruse, session);
			List checks = paruse.getAssociatedChecks();
			Iterator iterChecks = checks.iterator();
			while (iterChecks.hasNext()) {
				Check check = (Check) iterChecks.next();
				exporter.insertCheck(check, session);
				exporter.insertParuseCheck(paruse, check, session);
			}
			List roles = paruse.getAssociatedRoles();
			exportRoles(roles);
			Iterator iterRoles = roles.iterator();
			while (iterRoles.hasNext()) {
				Role role = (Role) iterRoles.next();
				exporter.insertParuseRole(paruse, role, session);
			}
		}
		logger.debug("OUT");
	}

	/**
	 * Checks if a list of value object is a query type and in this case exports
	 * the name of the SpagoBI data source associated to the query
	 * 
	 * @param lov
	 *            List of values Object
	 * @throws EMFUserError
	 */
	private void checkDataSource(ModalitiesValue lov) throws EMFUserError {
		logger.debug("IN");
		try {
			String type = lov.getITypeCd();
			if (type.equalsIgnoreCase("QUERY")) {
				String provider = lov.getLovProvider();
				QueryDetail queryDet = QueryDetail.fromXML(provider);
				String datasourceName = queryDet.getDataSource();
				IDataSourceDAO dsDAO = DAOFactory.getDataSourceDAO();
				List allDS = dsDAO.loadAllDataSources();
				Iterator allDSIt = allDS.iterator();
				IDataSource dsFound = null;
				while (allDSIt.hasNext()) {
					IDataSource ds = (IDataSource) allDSIt.next();
					if (ds.getLabel().equals(datasourceName)) {
						dsFound = ds;
						break;
					}
				}
				if (dsFound == null) {
					logger.error("Data source pool name " + datasourceName + " not found");
					List paramsErr = new ArrayList();
					paramsErr.add(lov.getLabel());
					paramsErr.add(datasourceName);
					throw new EMFUserError(EMFErrorSeverity.ERROR, "8008", paramsErr, ImportManager.messageBundle);
				} else {
					exporter.insertDataSource(dsFound, session);
				}
			}
		} catch (EMFUserError emfue) {
			throw emfue;
		} catch (Exception e) {
			logger.error("Error while checking connection" + e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "8005", ImportManager.messageBundle);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Export a list of SpagoBI roles
	 * 
	 * @param roles
	 *            The list of roles to export
	 * @throws EMFUserError
	 */
	private void exportRoles(List roles) throws EMFUserError {
		logger.debug("IN");
		Iterator iterRoles = roles.iterator();
		while (iterRoles.hasNext()) {
			Role role = (Role) iterRoles.next();
			exporter.insertRole(role, session);
			// exporter.insertAuthorizationsRole(role, session);
		}

		logger.debug("OUT");
	}

	/**
	 * Close hibernate session and session factory relative to the export
	 * database
	 */
	private void closeSession() {
		logger.debug("IN");
		if (session != null) {
			if (session.isOpen())
				session.close();
		}
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		logger.debug("OUT");
	}

	/**
	 * Clean the export environment (close sessions and delete temporary files).
	 */
	public void cleanExportEnvironment() {
		logger.debug("IN");
		closeSession();
		deleteTmpFolder();
		logger.debug("OUT");
	}

	/*
	 * public void exportResources() throws EMFUserError { logger.debug("IN");
	 * try { SourceBean config = (SourceBean)
	 * ConfigSingleton.getInstance().getAttribute
	 * ("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
	 * logger.debug("RESOURCE_PATH_JNDI_NAME configuration found: " + config);
	 * String resourcePathJndiName = config.getCharacters(); Context ctx = new
	 * InitialContext(); String value =
	 * (String)ctx.lookup(resourcePathJndiName);
	 * logger.debug("Resource path found from jndi: " + value); File
	 * resourcesDir = new File(value); File destDir = new File(pathBaseFolder +
	 * "/resources"); FileUtilities.copyDirectory(resourcesDir, destDir, true,
	 * true, false); } catch (Exception e) {
	 * logger.error("Error during the copy of maps files" , e); throw new
	 * EMFUserError(EMFErrorSeverity.ERROR, "100", ImportManager.messageBundle);
	 * } finally { logger.debug("OUT"); } }
	 */

}
