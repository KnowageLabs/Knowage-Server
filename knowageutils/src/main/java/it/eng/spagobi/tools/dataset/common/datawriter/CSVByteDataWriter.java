/**
 *
 */
package it.eng.spagobi.tools.dataset.common.datawriter;

import java.io.UnsupportedEncodingException;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

/**
 * @author Dragan Pirkovic
 *
 */
public class CSVByteDataWriter implements IDataWriter {

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.common.datawriter.IDataWriter#write(it.eng.spagobi.tools.dataset.common.datastore.IDataStore)
	 */
	@Override
	public Object write(IDataStore dataStore) {
		StringBuilder csv = new StringBuilder();

		try {
			return csv.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
