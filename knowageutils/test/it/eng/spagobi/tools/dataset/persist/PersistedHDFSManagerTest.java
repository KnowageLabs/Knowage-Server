package it.eng.spagobi.tools.dataset.persist;

import it.eng.spagobi.UtilitiesForTest;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;
import org.mockito.Mockito;

public class PersistedHDFSManagerTest extends TestCase {
	private PersistedHDFSManager hdfsManager = null;

	@Test
	public void testInitConfiguration() {
		String myPath = "hdfs://localhost:8020";
		Configuration conf = new Configuration();
		conf.set("fs.default.name", myPath);
		Configuration confHdfs = hdfsManager.initConfiguration();
		assertEquals(confHdfs.toString(), conf.toString());
	}

	@Test
	public void testInitializeFileSystem() {
		Configuration confHdfs = hdfsManager.initConfiguration();
		FileSystem fs = hdfsManager.initializeFileSystem(confHdfs);
		assertNotNull(fs);
	}

	@Test
	public void testPersistDataSet() {
		IDataStore dataStore = Mockito.mock(IDataStore.class);
		IMetaData metaData = Mockito.mock(IMetaData.class);
		IRecord record = Mockito.mock(IRecord.class);
		IField fieldInt = Mockito.mock(IField.class);
		IField fieldStr = Mockito.mock(IField.class);

		Mockito.when(dataStore.getMetaData()).thenReturn(metaData);
		Mockito.when(dataStore.getRecordAt(Mockito.anyInt())).thenReturn(record);
		Mockito.when(dataStore.getRecordsCount()).thenReturn(10L);
		Mockito.when(metaData.getFieldCount()).thenReturn(2);
		Mockito.when(metaData.getFieldName(1)).thenReturn("column_Int");
		Mockito.when(metaData.getFieldName(2)).thenReturn("column_Str");
		Mockito.when(metaData.getFieldType(1)).thenReturn(Integer.class);
		Mockito.when(metaData.getFieldType(2)).thenReturn(String.class);
		Mockito.when(record.getFieldAt(1)).thenReturn(fieldInt);
		Mockito.when(record.getFieldAt(2)).thenReturn(fieldStr);
		Mockito.when(fieldInt.getValue()).thenReturn(new Integer(1));
		Mockito.when(fieldStr.getValue()).thenReturn(new String("test"));
		Configuration confHdfs = hdfsManager.initConfiguration("./resources-test/core-site.xml");
		confHdfs.addResource(new Path("./resources-test/hdfs-site.xml"));
		confHdfs.set("hadoop.job.ugi", "spagobi");
		hdfsManager.initializeFileSystem(confHdfs);
		FSDataOutputStream fsOS = (FSDataOutputStream) hdfsManager.persistDataSet(dataStore, "test_table", "signature_xyz");
		assertNotNull(fsOS);
		assertEquals(fsOS.size(), 232);
	}

	@Override
	public void setUp() {
		System.setProperty("user.name", "spagobi");
		System.setProperty("HADOOP_USER_NAME", "spagobi");
		hdfsManager = new PersistedHDFSManager();
		try {
			SingletonConfig.getInstance();
			UtilitiesForTest.setUpMasterConfiguration();
			UtilitiesForTest.setUpTestJNDI();
		} catch (Exception e) {
			// nothing
		}
	}
}
