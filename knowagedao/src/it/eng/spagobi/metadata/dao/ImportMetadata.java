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
package it.eng.spagobi.metadata.dao;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.metadata.etl.ETLComponent;
import it.eng.spagobi.metadata.etl.ETLMetadata;
import it.eng.spagobi.metadata.etl.ETLRDBMSSource;
import it.eng.spagobi.metadata.metadata.SbiMetaBc;
import it.eng.spagobi.metadata.metadata.SbiMetaBcAttribute;
import it.eng.spagobi.metadata.metadata.SbiMetaJob;
import it.eng.spagobi.metadata.metadata.SbiMetaJobSource;
import it.eng.spagobi.metadata.metadata.SbiMetaJobTable;
import it.eng.spagobi.metadata.metadata.SbiMetaSource;
import it.eng.spagobi.metadata.metadata.SbiMetaTable;
import it.eng.spagobi.metadata.metadata.SbiMetaTableBc;
import it.eng.spagobi.metadata.metadata.SbiMetaTableBcId;
import it.eng.spagobi.metadata.metadata.SbiMetaTableColumn;

import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class ImportMetadata extends AbstractHibernateDAO {

	static private Logger logger = Logger.getLogger(ImportMetadata.class);
	private boolean existElement = false;
	public static final String FILE_SOURCE_TYPE = "file";
	public static final String ROLE_SOURCE = "source";
	public static final String ROLE_TARGET = "target";

	public void importETLMetadata(String jobName, ETLMetadata etlMetadata) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;
		try {

			aSession = getSession();
			tx = aSession.beginTransaction();
			ISbiMetaSourceDAO msDao = DAOFactory.getSbiMetaSourceDAO();

			// 1 - Create or update Job (SBI_META_JOB)
			SbiMetaJob aJob = new SbiMetaJob(jobName, false);
			Integer jobId = saveJob(aSession, aJob);
			aJob.setJobId(jobId);

			// 2 - Retrieve rdbms data sources of the job
			Set<ETLRDBMSSource> sources = etlMetadata.getRdbmsSources();
			// get corresponding source record from db
			HashMap<String, SbiMetaSource> sourcesMap = new HashMap<String, SbiMetaSource>();
			for (ETLRDBMSSource source : sources) {
				String sourceUniqueName = source.getUniqueName();
				// get corresponding source name on SBI_META_SOURCE mapped by .properties file
				String sourceName = etlMetadata.getSourceName(sourceUniqueName);
				SbiMetaSource sourceMeta = msDao.loadSourceByName(aSession, sourceName);

				if (sourceMeta != null) {
					// temporary save to map
					sourcesMap.put(sourceName, sourceMeta);

					// create or update Job-Source association (SBI_META_JOB_SOURCE)
					SbiMetaJobSource jobSource = new SbiMetaJobSource();
					jobSource.setSbiMetaJob(aJob);
					jobSource.setSbiMetaSource(sourceMeta);

					saveJobSource(aSession, jobSource);

				}
			}

			// 3 - Retrieve source tables
			Set<ETLComponent> sourceTables = etlMetadata.getSourceTables();
			for (ETLComponent sourceTable : sourceTables) {
				// for each table get the corresponding source
				String sourceComponentName = sourceTable.getConnectionComponentName();
				ETLRDBMSSource rdbmsSource = etlMetadata.getRDBMSSourceByComponentName(sourceComponentName);
				// get corresponding unique name (key)
				String sourceKey = rdbmsSource.getUniqueName();
				String sourceName = etlMetadata.getSourceName(sourceKey);

				// Check if that source was mapped in the properties file
				SbiMetaSource sbiSource = sourcesMap.get(sourceName);
				if (sbiSource != null) {
					String tableName = sourceTable.getValue();
					saveJobTable(aSession, sbiSource, tableName, ROLE_SOURCE, aJob);
				}
			}

			// 4 - Retrieve target Tables
			Set<ETLComponent> targetTables = etlMetadata.getTargetTables();
			for (ETLComponent targetTable : targetTables) {
				// for each table get the corresponding source
				String sourceComponentName = targetTable.getConnectionComponentName();
				ETLRDBMSSource rdbmsSource = etlMetadata.getRDBMSSourceByComponentName(sourceComponentName);
				// get corresponding unique name (key)
				String sourceKey = rdbmsSource.getUniqueName();
				String sourceName = etlMetadata.getSourceName(sourceKey);

				// Check if that source was mapped in the properties file
				SbiMetaSource sbiSource = sourcesMap.get(sourceName);
				if (sbiSource != null) {
					String tableName = targetTable.getValue();
					saveJobTable(aSession, sbiSource, tableName, ROLE_TARGET, aJob);
				}
			}
			// 5 - Retrieve source files
			Set<String> sourceFiles = etlMetadata.getSourceFiles();
			for (String sourceFile : sourceFiles) {
				SbiMetaSource sbiMetaSource = new SbiMetaSource();
				String fileName = Paths.get(sourceFile).getFileName().toString();
				sbiMetaSource.setName(fileName);
				sbiMetaSource.setLocation(sourceFile);
				sbiMetaSource.setType(FILE_SOURCE_TYPE);
				sbiMetaSource.setRole(ROLE_SOURCE);
				// save to db SBI_META_SOURCE
				Integer sourceId = saveSource(aSession, sbiMetaSource);
				sbiMetaSource.setSourceId(sourceId);
			}

			// 5 - Retrieve target files
			Set<String> targetFiles = etlMetadata.getTargetFiles();
			for (String targetFile : targetFiles) {
				SbiMetaSource sbiMetaSource = new SbiMetaSource();
				String fileName = Paths.get(targetFile).getFileName().toString();
				sbiMetaSource.setName(fileName);
				sbiMetaSource.setLocation(targetFile);
				sbiMetaSource.setType(FILE_SOURCE_TYPE);
				sbiMetaSource.setRole(ROLE_TARGET);
				// save to db SBI_META_SOURCE
				Integer sourceId = saveSource(aSession, sbiMetaSource);
				sbiMetaSource.setSourceId(sourceId);
			}
			tx.commit();
			logger.debug("Import etl metadata [" + jobName + "] ended correctly!");

		} catch (Exception e) {
			logException(e);

			if (tx != null)
				tx.rollback();

			logger.error("An error occurred while trying to insert metadata information from job named:" + jobName, e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}

	}

	public void importBusinessModel(int businessModelId, SbiMetaSource aMetaSource, List<SbiMetaBc> bcList) throws EMFUserError {
		logger.debug("IN");

		Session aSession = null;
		Transaction tx = null;
		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			HashMap<String, SbiMetaTable> tablesMap = new HashMap<String, SbiMetaTable>();
			HashMap<String, SbiMetaTableColumn> columnsMap = new HashMap<String, SbiMetaTableColumn>();

			// get informations and call DAO insert methods
			// SBI_META_SOURCE
			Integer sourceId = saveSource(aSession, aMetaSource);
			aMetaSource.setSourceId(sourceId);
			Integer tableId = null;

			Set<SbiMetaTable> metaTbs = aMetaSource.getSbiMetaTables();
			for (SbiMetaTable metaTb : metaTbs) {
				// SBI_META_TABLE
				metaTb.setSbiMetaSource(aMetaSource);
				tableId = saveTable(aSession, metaTb);
				metaTb.setTableId(tableId);
				tablesMap.put(metaTb.getName(), metaTb);

				Set<SbiMetaTableColumn> metaTbCols = metaTb.getSbiMetaTableColumns();
				HashMap<String, SbiMetaTableColumn> originalColumnsMap = null;

				if (isExistElement()) {
					// if the phisical table already exists,
					// get the previous version of the table columns
					originalColumnsMap = getColumnsAsMap(aSession, tableId);
				}

				// insert/update columns of the last version
				for (SbiMetaTableColumn metaTbCol : metaTbCols) {
					// SBI_META_TABLE_COLUMN
					metaTbCol.setSbiMetaTable(metaTb);
					Integer idTbCol = saveTableColumn(aSession, metaTbCol);
					metaTbCol.setColumnId(idTbCol);
					columnsMap.put(metaTbCol.getName(), metaTbCol);

				}

				if (isExistElement()) {
					// if the phisical table already exists,
					// manage logical delete on columns that don't exist anymore

					Iterator iter = originalColumnsMap.keySet().iterator();
					while (iter.hasNext()) {
						String key = (String) iter.next();
						SbiMetaTableColumn origTbCol = originalColumnsMap.get(key);
						if (columnsMap.get(key) == null) {
							// delete logically the column that doesn't exist anymore
							origTbCol.setDeleted(true);
							origTbCol.getCommonInfo().setTimeDe(new Date());
							saveTableColumn(aSession, origTbCol);
						}
					}
				}
			}
			// Business Class informations
			for (SbiMetaBc aMetaBc : bcList) {
				String bcName = null;
				String tablename = null;

				// get logical and phisical table name
				int pos = aMetaBc.getName().indexOf("|");
				tablename = aMetaBc.getName().substring(0, pos);
				bcName = aMetaBc.getName().substring(pos + 1);
				aMetaBc.setName(bcName);

				// SBI_META_BC
				Integer bcId = saveBC(aSession, aMetaBc);
				aMetaBc.setBcId(bcId);

				Set<SbiMetaBcAttribute> metaAttrs = aMetaBc.getSbiMetaBcAttributes();
				for (SbiMetaBcAttribute metaAttr : metaAttrs) {

					// set phisical table column id
					// SbiMetaTableColumn tableColumn = columnsMap.get(metaAttr.getSbiMetaTableColumn().getSbiMetaTable().getName());
					SbiMetaTableColumn tableColumn = columnsMap.get(metaAttr.getSbiMetaTableColumn().getName());
					metaAttr.setSbiMetaTableColumn(tableColumn);
					metaAttr.setSbiMetaBc(aMetaBc);
					// SBI_META_BC_ATTRIBUTES
					Integer bcAttrId = saveBCAttribute(aSession, metaAttr);
					metaAttr.setAttributeId(bcAttrId);
				}
				aMetaBc.setSbiMetaBcAttributes(metaAttrs);

				// SBI_META_TABLE_BC
				SbiMetaTableBc smTableBc = new SbiMetaTableBc();
				SbiMetaTableBcId smTableBcId = new SbiMetaTableBcId();
				smTableBcId.setTableId(tablesMap.get(tablename).getTableId());
				smTableBcId.setBcId(aMetaBc.getBcId());
				smTableBc.setId(smTableBcId);
				smTableBc.setSbiMetaBc(aMetaBc);
				smTableBc.setSbiMetaTable(tablesMap.get(tablename));
				saveTableBC(aSession, smTableBc);
			}

			tx.commit();
			logger.debug("Import metadata ended correctly!");

			// ONLY FOR TEST
			// System.out.println("FINISHHH!!!");
			// END TEST

		} catch (Exception e) {
			logException(e);

			if (tx != null)
				tx.rollback();

			logger.error("An error occurred while trying to insert metadata information from model with id:" + businessModelId, e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
			logger.debug("OUT");
		}
	}

	private Integer saveJob(Session session, SbiMetaJob aMetaJob) throws Exception {
		logger.debug("IN");

		ISbiMetaJobDAO mjDao = DAOFactory.getSbiMetaJobDAO();

		try {
			Integer idJob = null;
			SbiMetaJob sbiJob = mjDao.loadJobByName(session, aMetaJob.getName());

			if (sbiJob == null)
				// insert the new one...
				idJob = mjDao.insertJob(session, aMetaJob);
			else {
				// update the existing...
				idJob = sbiJob.getJobId();
				aMetaJob.setJobId(idJob);
				mjDao.modifyJob(session, aMetaJob);
			}
			return idJob;

		} catch (Exception e) {
			logger.error("An error occurred while trying to save metadata information for job [" + aMetaJob.getName() + "]", e);
			throw new Exception(e);

		} finally {
			logger.debug("OUT");
		}
	}

	private void saveJobSource(Session session, SbiMetaJobSource aMetaJobSource) throws Exception {
		logger.debug("IN");

		ISbiJobSourceDAO jsDao = DAOFactory.getSbiJobSourceDAO();

		try {
			SbiMetaJobSource sbiJobSource = jsDao.loadJobSource(session, aMetaJobSource.getSbiMetaJob().getJobId(), aMetaJobSource.getSbiMetaSource()
					.getSourceId());

			if (sbiJobSource == null) {
				// insert the new one...
				jsDao.insertJobSource(session, aMetaJobSource);
			}

		} catch (Exception e) {
			logger.error("An error occurred while trying to save job-source relation for job id [" + aMetaJobSource.getSbiMetaJob().getJobId()
					+ "] and source id [" + aMetaJobSource.getSbiMetaSource().getSourceId() + "]", e);
			throw new Exception(e);

		} finally {
			logger.debug("OUT");
		}
	}

	private void saveJobTable(Session session, SbiMetaSource aMetaSource, String tableName, String role, SbiMetaJob aJob) throws Exception {
		logger.debug("IN");

		ISbiMetaSourceDAO sourceDao = DAOFactory.getSbiMetaSourceDAO();
		ISbiJobTableDAO jobTableDAO = DAOFactory.getSbiJobTableDAO();

		try {
			List<SbiMetaTable> tables = sourceDao.loadMetaTables(session, aMetaSource.getSourceId());
			for (SbiMetaTable table : tables) {
				if (table.getName().equals(tableName)) {

					SbiMetaJobTable jobTable = new SbiMetaJobTable();
					jobTable.setRole(role);
					jobTable.setSbiMetaTable(table);
					jobTable.setSbiMetaJob(aJob);

					SbiMetaJobTable sbiJobTable = jobTableDAO.loadJobTable(session, aJob.getJobId(), table.getTableId());
					if (sbiJobTable == null) {
						// insert the new one
						jobTableDAO.insertJobTable(session, jobTable);
					}
					break;
				}
			}

		} catch (Exception e) {
			logger.error("An error occurred while trying to save job-table relation for job id [" + aJob.getJobId() + "] and table id [" + tableName + "]", e);
			throw new Exception(e);

		} finally {
			logger.debug("OUT");
		}
	}

	private Integer saveSource(Session session, SbiMetaSource aMetaSource) throws Exception {
		logger.debug("IN");

		ISbiMetaSourceDAO msDao = DAOFactory.getSbiMetaSourceDAO();

		try {
			Integer idSource = null;
			SbiMetaSource sbm = msDao.loadSourceByNameAndType(session, aMetaSource.getName(), aMetaSource.getType());

			if (sbm == null)
				// insert the new one...
				idSource = msDao.insertSource(session, aMetaSource);
			else {
				// update the existing...
				idSource = sbm.getSourceId();
				aMetaSource.setSourceId(idSource);
				msDao.modifySource(session, aMetaSource);
			}
			return idSource;

		} catch (Exception e) {
			logger.error("An error occurred while trying to save metadata information for source [" + aMetaSource.getName() + "]", e);
			throw new Exception(e);

		} finally {
			logger.debug("OUT");
		}
	}

	private Integer saveBC(Session session, SbiMetaBc aMetaBC) throws Exception {
		logger.debug("IN");

		ISbiMetaBCDAO msDao = DAOFactory.getSbiMetaBCDAO();

		try {
			Integer idBc = null;
			SbiMetaBc sbm = msDao.loadBcByName(session, aMetaBC.getName());

			if (sbm == null)
				// insert the new one...
				idBc = msDao.insertBc(session, aMetaBC);
			else {
				// update the existing...
				idBc = sbm.getBcId();
				aMetaBC.setBcId(idBc);
				msDao.modifyBc(session, aMetaBC);
			}
			return idBc;

		} catch (Exception e) {
			logger.error("An error occurred while trying to save metadata information for source [" + aMetaBC.getName() + "]", e);
			throw new Exception(e);

		} finally {
			logger.debug("OUT");
		}
	}

	private Integer saveBCAttribute(Session session, SbiMetaBcAttribute aMetaBCAttribute) throws Exception {
		logger.debug("IN");

		ISbiMetaBCAttributeDAO msDao = DAOFactory.getSbiMetaBCAttributeDAO();

		try {
			Integer idBcAttribute = null;
			SbiMetaBcAttribute sbm = msDao.loadBcAttributeByNameAndBc(session, aMetaBCAttribute.getName(), aMetaBCAttribute.getSbiMetaBc().getBcId());

			if (sbm == null)
				// insert the new one...
				idBcAttribute = msDao.insertBcAttribute(session, aMetaBCAttribute);
			else {
				// update the existing...
				idBcAttribute = sbm.getAttributeId();
				aMetaBCAttribute.setAttributeId(idBcAttribute);
				msDao.modifyBcAttribute(session, aMetaBCAttribute);
			}
			return idBcAttribute;

		} catch (Exception e) {
			logger.error("An error occurred while trying to save metadata information for source [" + aMetaBCAttribute.getName() + "]", e);
			throw new Exception(e);

		} finally {
			logger.debug("OUT");
		}
	}

	private int saveTable(Session session, SbiMetaTable newTable) throws Exception {
		logger.debug("IN");

		ISbiMetaTableDAO msDao = DAOFactory.getSbiMetaTableDAO();

		try {
			Integer idTable = null;
			SbiMetaTable sbm = msDao.loadTableByNameAndSource(session, newTable.getName(), newTable.getSbiMetaSource().getSourceId());

			if (sbm == null) {
				// insert the new one...
				idTable = msDao.insertTable(session, newTable);
				setExistElement(false);
			} else {
				// update the existing...
				idTable = sbm.getTableId();
				newTable.setTableId(idTable);
				msDao.modifyTable(session, newTable);
				setExistElement(true);
			}
			return idTable;

		} catch (Exception e) {
			logger.error("An error occurred while trying to save metadata information for table [" + newTable.getName() + "]", e);
			throw new Exception(e);

		} finally {
			logger.debug("OUT");
		}
	}

	private void saveTableBC(Session session, SbiMetaTableBc aMetaTableBc) throws Exception {
		logger.debug("IN");

		ISbiTableBcDAO msDao = DAOFactory.getSbiTableBCDAO();

		try {
			SbiMetaTableBc sbm = msDao.loadTableBcByBcIdAndTableId(session, aMetaTableBc.getId());

			if (sbm == null) {
				// insert the new one...
				msDao.insertTableBc(session, aMetaTableBc);
			} else {
				// update the existing...
				msDao.modifyTableBc(session, aMetaTableBc);
			}

		} catch (Exception e) {
			logger.error("An error occurred while trying to save metadata information for relation table-bc with tableId [" + aMetaTableBc.getId().getTableId()
					+ "] and bcId [" + aMetaTableBc.getId().getBcId() + "]", e);
			throw new Exception(e);

		} finally {
			logger.debug("OUT");
		}
	}

	private int saveTableColumn(Session session, SbiMetaTableColumn newColumn) throws Exception {
		logger.debug("IN");

		ISbiMetaTableColumnDAO msDao = DAOFactory.getSbiMetaTableColumnDAO();

		try {
			Integer idColumn = null;
			SbiMetaTableColumn sbm = msDao.loadTableColumnByNameAndTable(session, newColumn.getName(), newColumn.getSbiMetaTable().getTableId());

			if (sbm == null)
				// insert the new one...
				idColumn = msDao.insertTableColumn(session, newColumn);
			else {
				// update the existing...
				idColumn = sbm.getColumnId();
				newColumn.setColumnId(idColumn);
				msDao.modifyTableColumn(session, newColumn);
			}
			return idColumn;

		} catch (Exception e) {
			logger.error("An error occurred while trying to save metadata information for table column [" + newColumn.getName() + "]", e);
			throw new Exception(e);

		} finally {
			logger.debug("OUT");
		}
	}

	private HashMap<String, SbiMetaTableColumn> getColumnsAsMap(Session session, int tableId) throws Exception {
		logger.debug("IN");

		ISbiMetaTableColumnDAO msDao = DAOFactory.getSbiMetaTableColumnDAO();
		HashMap<String, SbiMetaTableColumn> toReturn = new HashMap<String, SbiMetaTableColumn>();

		try {
			List<SbiMetaTableColumn> cols = msDao.loadTableColumnsFromTable(session, tableId);

			if (cols == null) {
				// no columns for the input table.
				logger.error("No columns found for the table with id [" + tableId + "] ");
			}
			// convert the list in map
			for (SbiMetaTableColumn col : cols) {
				toReturn.put(col.getName(), col);
			}
			return toReturn;

		} catch (Exception e) {
			logger.error("An error occurred while trying to get columns for table with id [" + tableId + "]");
			throw new Exception(e);

		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Retrun column from the set with the input colName
	 *
	 * @param metaTbCols
	 * @param colTest
	 * @return the column if exists, null otherways
	 */
	private SbiMetaTableColumn getMetaColumn(Set<SbiMetaTableColumn> metaTbCols, String colTest) {
		SbiMetaTableColumn toReturn = null;

		for (SbiMetaTableColumn metaTbCol : metaTbCols) {
			if (metaTbCol.getName().equals(colTest)) {
				toReturn = metaTbCol;
				break;
			}
		}
		return toReturn;

	}

	/**
	 * @return the existElement
	 */
	public boolean isExistElement() {
		return existElement;
	}

	/**
	 * @param existElement
	 *            the existElement to set
	 */
	public void setExistElement(boolean existElement) {
		this.existElement = existElement;
	}

}