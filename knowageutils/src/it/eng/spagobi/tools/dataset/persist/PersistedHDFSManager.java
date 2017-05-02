package it.eng.spagobi.tools.dataset.persist;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.hdfs.Hdfs;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class PersistedHDFSManager implements IPersistedManager {

	private static final Logger logger = Logger.getLogger(PersistedHDFSManager.class);
	private static final String TENANT_ID = "TENANT_ID";

	private IEngUserProfile profile = null;
	private Hdfs hdfs = null;

	public PersistedHDFSManager() {
		this.hdfs = new Hdfs();
		this.hdfs.init();
	}

	public PersistedHDFSManager(Hdfs hdfs) {
		this.hdfs = hdfs;
	}

	public PersistedHDFSManager(IEngUserProfile profile) {
		this.profile = profile;
		this.hdfs = new Hdfs();
		this.hdfs.init();
	}

	public PersistedHDFSManager(String label, String description) {
		this.hdfs = new Hdfs(label, description);
		this.hdfs.init();
	}

	public PersistedHDFSManager(IEngUserProfile profile, String label, String description) {
		this.profile = profile;
		this.hdfs = new Hdfs(label, description);
		this.hdfs.init();
	}

	@Override
	public void persistDataSet(IDataSet dataSet) throws Exception {
		logger.debug("Start persisting DataSet");
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_h.mm.ss");
		// String folderName = Helper.sha256(dataSet.getSignature());
		String tenantName = profile != null ? profile.getUserAttribute(TENANT_ID).toString() : TenantManager.getTenant().getName();
		String folderName = tenantName + Path.SEPARATOR + dataSet.getLabel();
		String fileName = dataSet.getTableNameForReading() + "_" + sdf.format(date);

		dataSet.setPersisted(false);
		dataSet.loadData();
		IDataStore dataStore = dataSet.getDataStore();
		persistDataStore(dataStore, fileName, folderName);

		logger.debug("Finish persisting DataSet");
	}

	public void persistDataSet(IDataSet dataSet, String fileName) throws Exception {
		logger.debug("Start persisting DataSet");
		String folderName = Helper.sha256(dataSet.getSignature());
		dataSet.loadData();
		IDataStore dataStore = dataSet.getDataStore();
		persistDataStore(dataStore, fileName, folderName);
		logger.debug("Finish persisting DataSet");
	}

	public Object persistDataStore(IDataStore dataStore, String fileName, String folderName) {

		FSDataOutputStream fsOS = openHdfsFile(fileName, folderName);
		IMetaData mt = dataStore.getMetaData();
		int nFields = mt.getFieldCount();
		try {
			logger.debug("Starting writing the DataSet metadata");
			for (int f = 0; f < nFields; f++) {
				String sep = f == (nFields - 1) ? "\n" : ",";
				fsOS.writeChars("\"" + mt.getFieldName(f) + "\"" + sep);
			}
			logger.debug("End metadata writing. Starting writing the data");
			long nRecords = dataStore.getRecordsCount();
			for (int i = 0; i < nRecords; i++) {
				IRecord record = dataStore.getRecordAt(i);
				for (int f = 0; f < nFields; f++) {
					String sep = f == (nFields - 1) ? "\n" : ",";
					IField field = record.getFieldAt(f);
					Class clz = mt.getFieldType(f);
					Object value = field.getValue();
					appendObjectWithCast(fsOS, value, clz);
					fsOS.writeChars(sep);
				}
			}
			logger.debug("End data writing. Closing file..");
			fsOS.close();
		} catch (IOException e) {
			logger.error("Impossible to write on hdfs, error during writing ");
			throw new SpagoBIRuntimeException("Impossible to write on hdfs, error during writing " + e);
		}
		return fsOS;
	}

	public FSDataOutputStream openHdfsFile(String fileName, String folderName) {
		logger.debug("Begin file opening");
		FSDataOutputStream fsOS = null;
		Path filePath = null;
		try {
			FileSystem fs = hdfs.getFs();
			filePath = fs.getWorkingDirectory();
			if (folderName != null && folderName.length() > 0) {
				filePath = Path.mergePaths(filePath, new Path(Path.SEPARATOR, folderName));
				if (!fs.exists(filePath) || !fs.isDirectory(filePath)) {
					fs.mkdirs(filePath);
				}
			}
			filePath = Path.mergePaths(filePath, new Path(Path.SEPARATOR + fileName));
			boolean existsFile = fs.exists(filePath);
			if (existsFile) {
				logger.debug("File is already present in folder, it will be deleted and replaced with new file");
				fs.delete(filePath, true);
			}
			fsOS = fs.create(filePath, true);
		} catch (IOException e) {
			logger.error("Impossible to open file in File System");
			throw new SpagoBIRuntimeException("Impossible to open file in File System" + e);
		}
		logger.debug("File opened");
		return fsOS;
	}

	private void appendObjectWithCast(FSDataOutputStream fsOS, Object value, Class clz) {
		try {
			if (value == null) {
				fsOS.writeChars("\"NULL\"");
				return;
			}
			Class objClz = value.getClass();
			if (clz.equals(String.class)) {
				fsOS.writeChars("\"" + (String) value + "\"");
			} else if (clz.equals(Integer.class)) {
				fsOS.writeChars(value.toString());
			} else if (clz.equals(Double.class)) {
				fsOS.writeChars(value.toString());
			} else if (clz.equals(Long.class)) {
				fsOS.writeChars(value.toString());
			} else if (clz.equals(Date.class)) {
				Date dt = (Date) value;
				fsOS.writeChars("\"" + dt.toString() + "\"");
			} else if (clz.equals(Timestamp.class)) {
				Timestamp ts = (Timestamp) value;
				fsOS.writeChars("\"" + ts.toString() + "\"");
			} else if (clz.equals(Boolean.class)) {
				fsOS.writeChars(value.toString());
			} else {
				fsOS.writeChars((String) value);
			}
		} catch (IOException e) {
			logger.error("Impossible to write on hdfs, error during casting object for writing ");
			throw new SpagoBIRuntimeException("Impossible to write on hdfs, error during casting object for writing " + e);
		}
	}

	public String getFilePathFromHdfsResourcePath(String fileName) {
		return SpagoBIUtilities.getResourcePath() + File.separatorChar + "hdfs" + File.separatorChar + fileName;
	}

	public Hdfs getHdfs() {
		return hdfs;
	}

	public void setHdfs(Hdfs hdfs) {
		this.hdfs = hdfs;
	}

	public IEngUserProfile getProfile() {
		return profile;
	}

	public void setProfile(IEngUserProfile profile) {
		this.profile = profile;
	}

}
