package it.eng.spagobi.tools.dataset.bo;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.hdfs.Hdfs;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.HdfsFileDataProxy;
import it.eng.spagobi.tools.dataset.common.dataproxy.IDataProxy;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.json.JSONUtils;

import org.apache.log4j.Logger;
import org.json.JSONObject;

public class HdfsDataSet extends FileDataSet {

	private static transient Logger logger = Logger.getLogger(FileDataSet.class);

	public static String DS_TYPE = "SbiHdfsDataSet";

	private int maxResults = -1; // number of rows to read in a file, default -1 equals to no limits

	public Hdfs hdfs;

	/**
	 * Instantiates a new empty file data set.
	 */
	public HdfsDataSet() {
		super();
		hdfs = new Hdfs();
		hdfs.init();
	}

	public HdfsDataSet(SpagoBiDataSet dataSetConfig) {
		super(dataSetConfig);
		hdfs = new Hdfs();
		logger.debug("IN");
		try {
			// JSONObject jsonConf =
			// ObjectUtils.toJSONObject(dataSetConfig.getConfiguration());
			String config = JSONUtils.escapeJsonString(dataSetConfig.getConfiguration());
			JSONObject jsonConf = ObjectUtils.toJSONObject(config);
			String fileName = (jsonConf.get(FILE_NAME) != null) ? jsonConf.get(FILE_NAME).toString() : "";
			if (fileName == null || fileName.length() == 0) {
				throw new IllegalArgumentException("fileName member of SpagoBiDataSet object parameter cannot be null or empty"
						+ "while creating a FileDataSet. If you whant to create an empty FileDataSet use the proper constructor.");
			}
			this.setFileName((jsonConf.get(FILE_NAME) != null) ? jsonConf.get(FILE_NAME).toString() : "");
			logger.info("File name: " + fileName);

			this.setResourcePath(StringUtilities.isNotEmpty(jsonConf.optString(RESOURCE_PATH)) ? jsonConf.optString(RESOURCE_PATH) : "");
			logger.info("Resource path: " + this.getResourcePath());
			if (this.dataProxy != null)
				dataProxy.setResPath(this.getResourcePath());
		} catch (Exception e) {
			logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
		}
		// setFileName( dataSetConfig.getFileName() );

		logger.debug("OUT");
	}

	@Override
	public HdfsFileDataProxy getDataProxy() {
		IDataProxy dataProxy;

		dataProxy = super.getDataProxy();

		if (dataProxy == null || !(dataProxy instanceof HdfsFileDataProxy)) {
			dataProxy = new HdfsFileDataProxy(this.getResourcePath());
			setDataProxy(dataProxy);
			if (useTempFile) {
				if (dataProxy instanceof HdfsFileDataProxy) {
					((HdfsFileDataProxy) dataProxy).setUseTempFile(true);
				}
			}
		}

		((HdfsFileDataProxy) dataProxy).setHdfs(hdfs);

		if (!(dataProxy instanceof HdfsFileDataProxy))
			throw new RuntimeException("DataProxy cannot be of type [" + dataProxy.getClass().getName() + "] in FileDataSet");

		return (HdfsFileDataProxy) dataProxy;
	}

	public boolean deleteFile(String path) {
		return hdfs.deleteFile(path);
	}

	@Override
	public SpagoBiDataSet toSpagoBiDataSet() {
		SpagoBiDataSet sbd;

		sbd = super.toSpagoBiDataSet();
		sbd.setPersistedHDFS(this.persistedHDFS);
		sbd.setType(DS_TYPE);
		return sbd;
	}

	@Override
	public String getFileName() {
		return getDataProxy().getFileName();
	}

	@Override
	public void setFileName(String fileName) {
		setFileName(fileName, true);
	}

	@Override
	public void setFileName(String fileName, boolean updateFileFormat) {
		if (fileName == null || fileName.length() == 0) {
			throw new IllegalArgumentException("fileName argument cannot be null or an empty string");
		}
		getDataProxy().setFileName(fileName);

		if (updateFileFormat) {
			try {
				setDataReader(fileName);
			} catch (Exception e) {
				throw new RuntimeException("Missing right exstension", e);
			}
		}
	}

	/**
	 * @return the fileType
	 */
	@Override
	public String getFileType() {
		return fileType;
	}

	// @Override
	// public void loadData(int offset, int fetchSize, int maxResults) {
	// if (getFileName() != null) {
	// String s = "";
	// }
	// }

	// @Override
	// public String getTableNameForReading() {
	// return super.getTableNameForReading();
	// }

	private void adjustMetadata(IDataStore iDataStore) {
		IMetaData metadata = iDataStore.getMetaData();
		try {
			IMetaData definedMetadata = this.getMetadata();
			int count = metadata.getFieldCount();
			for (int i = 0; i < count; i++) {
				IFieldMetaData fieldMetadata = metadata.getFieldMeta(i);
				String name = fieldMetadata.getName();
				int index = definedMetadata.getFieldIndex(name);
				if (index != -1) {
					IFieldMetaData aFieldMetaData = definedMetadata.getFieldMeta(index);
					fieldMetadata.setFieldType(aFieldMetaData.getFieldType());
				}

			}
		} catch (Exception e) {
			logger.error("Cannot adjust metadata", e);
		}

	}

	/**
	 * @param fileType
	 *            the fileType to set
	 */
	@Override
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	@Override
	public String getSignature() {
		// return this.getDataProxy().getMD5Checksum();
		return this.getDataProxy().getFileName();
	}

	/**
	 * @return the useTempFile
	 */
	@Override
	public boolean isUseTempFile() {
		return useTempFile;
	}

	/**
	 * @param useTempFile
	 *            the useTempFile to set
	 */
	@Override
	public void setUseTempFile(boolean useTempFile) {
		this.useTempFile = useTempFile;
	}

	@Override
	public int getMaxResults() {
		return maxResults;
	}

	@Override
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	@Override
	public IDataSource getDataSource() {
		return null;
	}

	public Hdfs getHdfs() {
		return hdfs;
	}

	public void setHdfs(Hdfs hdfs) {
		this.hdfs = hdfs;
	}

	public String getHdfsResourcePath() {
		return hdfs.getFs().getWorkingDirectory().toString();
	}
}
