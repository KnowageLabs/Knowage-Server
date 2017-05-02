package it.eng.spagobi.tools.dataset.common.iterator;

import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

import java.util.Iterator;

public interface DataIterator extends Iterator<IRecord>, AutoCloseable {

	@Override
	public void close();

	public IMetaData getMetaData();

}
