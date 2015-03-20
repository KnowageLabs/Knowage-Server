/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package test;

import it.eng.spagobi.tools.dataset.bo.AbstractCustomDataSet;
import it.eng.spagobi.tools.dataset.common.behaviour.FilteringBehaviour;
import it.eng.spagobi.tools.dataset.common.behaviour.SelectableFieldsBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStoreFilter;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

public class FakeDataset extends AbstractCustomDataSet {
	
	public static transient Logger logger = Logger.getLogger(FakeDataset.class);
	
	public static String[] sessi = { "0", "1" }; // { "Maschio", "Femmina" };
	public static String[] statimatrimoniali = { "0", "1" }; // { "Sposato", "Non sposato" };
	public static String[] occupazioni = { "Impiegato", "Operario", "Agricoltore", "Artista", "Medico", "Sportivo" };
	public static String[] titoli = { "0", "1", "2", "3" }; // { "Laureato", "Diplomato", "Post-doc", "Master" };
	public static String[] regioni = { "Veneto", "Lombardia", "Calabria", "Emilia Romagna", "Trentino Alto Adige" };
	public static String[] lecittacodici = { "PD", "MI", "RC", "RE", "TN", "VE", "BR", "CT", "BO", "BZ" };
	public static String[] lecittadescrizioni = { "Padova", "Milano", "Reggio Calabria", "Reggio Emilia", "Trento", "Venezia", "Brescia", "Catanzato", "Bologna", "Bolzano" };
	public static String[] sportelli = new String[30];

//	public static Integer[] mesi = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
	public static String[] anni = new String[] { "2000", "2001", "2002", "2003" };
	public static String[] enti = new String[] { "Ente 1", "Ente 2", "Ente 3",
			"Ente 4", "Ente 5", "Ente 6", "Ente 7", "Ente 8" };
	
	private static IDataStore datastore = null;
	
	private static Map<String, String[]> mappa = new HashMap<String, String[]>();
	
	private static Random randomGenerator = new Random();
	
	static {
		for (int i = 0 ; i < 30; i++) {
			sportelli[i] = new Integer(i).toString();
		}
		
		mappa.put("sesso", sessi);
		mappa.put("statomatrimoniale", statimatrimoniali);
		mappa.put("occupazione", occupazioni);
		mappa.put("titolo", titoli);
		mappa.put("regione", regioni);
		mappa.put("citta", lecittacodici);
		mappa.put("sportello", sportelli);
		mappa.put("anno", anni);
		mappa.put("ente", enti);
		
		datastore = createStore();
	}

	public IDataStore getDomainValues(String fieldName, Integer start,
			Integer limit, IDataStoreFilter filter) {
		
		logParams();
		logFilters();
		
		String[] values = mappa.get(fieldName);
		List<String> temp = new ArrayList<String>();
		for ( int i = start; i < limit && i < values.length; i++ ) {
			temp.add(values[i]);
		}
		
		String[] limitValues = temp.toArray(new String[]{});
		
		IDataStore dataStore = new DataStore();
		IMetaData meta = dataStore.getMetaData();
		
		FieldMetadata fieldMeta = new FieldMetadata(fieldName, String.class);
		meta.addFiedMeta(fieldMeta);
		
		IField f = null;
		IRecord rec = null;

		int totale = limitValues.length;
		
		for (int i = 0; i < totale; i++) {
			rec = new Record();
			f = new Field(); f.setValue( limitValues[i] ); rec.appendField(f);
			dataStore.appendRecord(rec);
		}
		
		
		return dataStore;
	}


	private void logFilters() {
		FilteringBehaviour behaviour = (FilteringBehaviour) this.getBehaviour(FilteringBehaviour.ID);
		Map<String, List<String>> filters = behaviour.getFilters();
		System.out.println("Filters: " + filters);
	}
	
	private void logSelectedFields() {
		SelectableFieldsBehaviour behaviour = (SelectableFieldsBehaviour) this.getBehaviour(SelectableFieldsBehaviour.ID);
		List<String> fields = behaviour.getSelectedFields();
		System.out.println("Selected fields: " + fields);
	}
	
	private void logParams() {
		System.out.println("Params: " + this.getParamsMap());
	}


	public IDataStore test() {
		return datastore;
	}


	public IDataStore test(int offset, int fetchSize, int maxResults) {
	
		logParams();
		
		DataStore toReturn = new DataStore();
		
		IRecord rec = null;
		Integer resultNumber = (Integer) this.getMetadata().getProperty("resultNumber");
		int count = 0;
		for ( int i = offset; i < offset + fetchSize && i < resultNumber; i++ ) {
			rec = datastore.getRecordAt(i);
			toReturn.appendRecord(rec);
			count++;
		}
		
		toReturn.setMetaData(this.getMetadata());
		
		return toReturn;
	}
	
	static public IDataStore createStore(){
		IDataStore dataStore = new DataStore();
		IMetaData meta = dataStore.getMetaData();
		
		FieldMetadata sesso = new FieldMetadata("sesso", String.class);
		sesso.setAlias("Sesso del cliente");
		sesso.setProperty("size", 10);
		meta.addFiedMeta(sesso);
		
		FieldMetadata statomatrimoniale = new FieldMetadata("statomatrimoniale", String.class);
		statomatrimoniale.setAlias("Stato matrimoniale del cliente");
		statomatrimoniale.setProperty("size", 20);
		meta.addFiedMeta(statomatrimoniale);
		
		FieldMetadata occupazione = new FieldMetadata("occupazione", String.class);
		occupazione.setAlias("Occupazione");
		occupazione.setProperty("size", 20);
		meta.addFiedMeta(occupazione);
		
		FieldMetadata titolo = new FieldMetadata("titolo", String.class);
		titolo.setAlias("Titolo di studio");
		titolo.setProperty("size", 20);
		meta.addFiedMeta(titolo);
		
		FieldMetadata regione = new FieldMetadata("regione", String.class);
		regione.setAlias("Regione");
		regione.setProperty("size", 20);
		meta.addFiedMeta(regione);
		
		FieldMetadata citta = new FieldMetadata("citta", String.class);
		citta.setAlias("Città");
		citta.setProperty("size", 20);
		meta.addFiedMeta(citta);
		
		FieldMetadata sportello = new FieldMetadata("sportello", String.class);
		sportello.setAlias("Sportello");
		sportello.setProperty("size", 4);
		meta.addFiedMeta(sportello);
		
//		FieldMetadata mese = new FieldMetadata("mese", String.class);
//		mese.setAlias("mese");
//		meta.addFiedMeta(mese);

		FieldMetadata anno = new FieldMetadata("anno", Integer.class);
		anno.setAlias("Anno di analisi");
		anno.setProperty("size", 4);
		meta.addFiedMeta(anno);
		
		FieldMetadata ente = new FieldMetadata("ente", String.class);
		ente.setAlias("Ente");
		ente.setProperty("size", 20);
		ente.setProperty("isSegmentAttribute", Boolean.TRUE);
		meta.addFiedMeta(ente);
		
		FieldMetadata spesa = new FieldMetadata("spesa", Float.class);
		spesa.setAlias("Spesa sostenuta");
		spesa.setProperty("isMandatoryMeasure", Boolean.TRUE);
		spesa.setProperty("decimalPrecision", "4");
		spesa.setFieldType(FieldType.MEASURE);
		meta.addFiedMeta(spesa);
		
		FieldMetadata guadagno = new FieldMetadata("guadagno", Integer.class);
		guadagno.setAlias("Guadagno");
		guadagno.setProperty("aggregationFunction", "SUM");
		guadagno.setProperty("decimalPrecision", "0");
		guadagno.setFieldType(FieldType.MEASURE);
		meta.addFiedMeta(guadagno);
		
		IField f = null;
		IRecord rec = null;

		int totale = sessi.length * statimatrimoniali.length * occupazioni.length * titoli.length * sportelli.length * anni.length * enti.length;
		
		meta.setProperty("resultNumber", totale);
		
		for (int i = 0; i < totale; i++) {
			rec = new Record();

			f = new Field(); f.setValue( sessi[ (i < totale / 2) ? 0 : 1 ] ); rec.appendField(f);
			f = new Field(); f.setValue( statimatrimoniali[ i % statimatrimoniali.length ] ); rec.appendField(f);
			f = new Field(); f.setValue( occupazioni[ randomGenerator.nextInt(occupazioni.length) ] ); rec.appendField(f);
			f = new Field(); f.setValue( titoli[ randomGenerator.nextInt(titoli.length) ] ); rec.appendField(f);
			f = new Field(); f.setValue( regioni[ ( (i % sportelli.length) % lecittacodici.length ) % regioni.length ] ); rec.appendField(f);
			f = new Field(); f.setValue( lecittacodici[ (i % sportelli.length) % lecittacodici.length ] ); rec.appendField(f);
			f = new Field(); f.setValue( sportelli[ i % sportelli.length ] ); rec.appendField(f);
//			f = new Field(); f.setValue( mesi[ i % mesi.length ] ); rec.appendField(f);
			
			int lunghezzaGruppo = totale / anni.length; 
			int annoIndex = new Double(Math.floor( i / lunghezzaGruppo)).intValue();
			f = new Field(); f.setValue( anni[ annoIndex ] ); rec.appendField(f);
			f = new Field(); f.setValue( enti[ randomGenerator.nextInt(enti.length) ] ); rec.appendField(f);
			f = new Field(); f.setValue( Math.random() ); rec.appendField(f); // spesa
			f = new Field(); f.setValue( 1 ); rec.appendField(f); // guadagno

			dataStore.appendRecord(rec);
		}

//		print(dataStore);

		return dataStore;
	}


	static void print(IDataStore dstore){


		for(int i = 0; i < dstore.getRecordsCount() ; i++){
			IRecord rec = dstore.getRecordAt(i);
			String print = "";

			List fields = rec.getFields();
			for (Iterator iterator = fields.iterator(); iterator.hasNext();) {
				IField f = (IField) iterator.next();
				print+=f.getValue().toString();
				print += " ";
			}
			System.out.println(print);			
		}

	}

	@Override
	public String getSignature() {
		logger.debug("IN");
		
		logParams();
		logFilters();
		logSelectedFields();
		
		StringBuffer buffer = new StringBuffer();
		
		// considero i SelectableFieldsBehaviour
		SelectableFieldsBehaviour sfb = (SelectableFieldsBehaviour) this.getBehaviour(SelectableFieldsBehaviour.ID);
		List<String> fields = sfb.getSelectedFields();
		Collections.sort(fields); // li ordino in modo che l'ordine con cui vengono impostati sia irrilevante
		String result = join(fields, ",");
		buffer.append("SelectedFields:" + result + ";");
		
		// considero i FilteringBehaviour
		FilteringBehaviour fb = (FilteringBehaviour) this.getBehaviour(FilteringBehaviour.ID);
		Map<String, List<String>> filters = fb.getFilters();
		Set<String> keys = filters.keySet();
		List<String> keysList = new ArrayList<String>();
		keysList.addAll(keys);
		Collections.sort(keysList); // li ordino in modo che l'ordine con cui vengono impostati sia irrilevante
		Iterator<String> it = keysList.iterator();
		buffer.append("Filters:");
		while (it.hasNext()) {
			String aKey = it.next();
			List<String> filterValues = filters.get(aKey);
			Collections.sort(filterValues);  // li ordino in modo che l'ordine con cui vengono impostati sia irrilevante
			buffer.append(aKey + "=" + join(filterValues, ",") + ";");
		}
		
		// considero i driver analitici anno ed ente
		buffer.append("Analytical drivers:");
		String yearStr = (String) this.getParamsMap().get("anno");
		String[] years = yearStr.split(","); // i valori sono separati da virgola
		Arrays.sort(years); // li ordino in modo che l'ordine con cui vengono impostati sia irrilevante
		buffer.append("anno=" + join(years, ",") + ";");
		
		String enteStr = (String) this.getParamsMap().get("ente");
		String[] ente = enteStr.split(","); // i valori sono separati da virgola
		Arrays.sort(ente); // li ordino in modo che l'ordine con cui vengono impostati sia irrilevante
		buffer.append("ente=" + join(ente, ",") + ";");
		
		String toReturn = buffer.toString();
		
		logger.debug("OUT: " + toReturn);
		
		return toReturn;
	}
	
	static public String join(Collection<String> list, String conjunction) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : list) {
			if (first)
				first = false;
			else
				sb.append(conjunction);
			sb.append(item);
		}
		return sb.toString();
	}
	
	static public String join(String[] array, String conjunction) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : array) {
			if (first)
				first = false;
			else
				sb.append(conjunction);
			sb.append(item);
		}
		return sb.toString();
	}

	@Override
	public IDataSetTableDescriptor persist(String tableName, IDataSource dataSource) {
		logParams();
		logFilters();
		logSelectedFields();
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			IDataSetTableDescriptor toReturn = this.createTemporaryTable(tableName, connection);
			this.populateTable(connection, tableName);
			return toReturn;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error persisting dataset", e);
		} finally {
			if ( connection != null ) {
				try {
					if (!connection.isClosed()) {
						connection.close();
					}
				} catch (SQLException e) {
					logger.error("Error while closing connection", e);
				}
			}
		}

	}
	
	@Override
	public IMetaData getMetadata() {
		return datastore.getMetaData();
	}

	private void populateTable(Connection conn, String tableName) {
		logger.debug("IN");

		Statement st = null;
		String query = null;

		try {
			SelectableFieldsBehaviour behaviour = (SelectableFieldsBehaviour) this.getBehaviour(SelectableFieldsBehaviour.ID);
			List<String> selectedFields = behaviour.getSelectedFields();
			InsertCommand insertCommand = new InsertCommand(this.getMetadata(), tableName);

			Iterator it = datastore.iterator();
			while (it.hasNext()) {
				IRecord record = (IRecord) it.next();
				
				if (this.satisfyFilters(record)) {
					
					insertCommand.setRecord(record);
					
					// after built columns create SQL Query
					query = insertCommand.createSQLQuery(selectedFields);
					System.out.println(query);
					// execute 
					st = conn.createStatement();
					st.execute(query);
					
				}

			}
		} catch (SQLException e) {
			logger.error("Error in excuting statement " + query, e);
			throw new SpagoBIRuntimeException("Error creating temporary table", e);
		}
		finally {
			try {
				if ( st != null ) {
					st.close();
				}
			} catch (SQLException e) {
				logger.error("could not free resources ", e);
			}
		}
		logger.debug("OUT");
		
	}


	private boolean satisfyFilters(IRecord record) {
		// considero i driver analitici anno ed ente
		String annoStr = (String) this.getParamsMap().get("anno");
		String[] annoArr = annoStr.split(","); // i valori sono separati da virgola
		List<String> anno = Arrays.asList(annoArr);
		String recordAnno = record.getFieldAt(7).getValue().toString();
		if (!anno.contains(recordAnno)) {
			return false;
		}
		
		String enteStr = (String) this.getParamsMap().get("ente");
		String[] enteArr = enteStr.split(","); // i valori sono separati da virgola
		List<String> ente = Arrays.asList(enteArr);
		String recordEnte = record.getFieldAt(8).getValue().toString();
		if (!ente.contains(recordEnte)) {
			return false;
		}
		
		FilteringBehaviour behaviour = (FilteringBehaviour) this.getBehaviour(FilteringBehaviour.ID);
		Map<String, List<String>> filters = behaviour.getFilters();
		Iterator<String> it = filters.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			List<String> filterValues = filters.get(key);
			int index = this.getMetadata().getFieldIndex(key);
			String value = record.getFieldAt(index).getValue().toString();
			if (!filterValues.contains(value)) {
				return false;
			}
		}
		
		return true;
	}
	

	public Map<String, List<String>> getDomainDescriptions(
			Map<String, List<String>> codes) {
		Map map = new HashMap<String, List<String>>();
		for (Iterator iterator = codes.keySet().iterator(); iterator.hasNext();) {
			String type = (String) iterator.next();
			
			List valuesList = codes.get(type);
			List l = new ArrayList<String>();
			
			if (type.equals("sesso")) {
				for (Iterator iterator2 = valuesList.iterator(); iterator2.hasNext();) {
					String s = (String) iterator2.next();
					if (s.equals("0")) {
						l.add("Maschio");
					} else {
						l.add("Femmina");
					}
				}
			} else if (type.equals("statomatrimoniale")) {
				for (Iterator iterator2 = valuesList.iterator(); iterator2.hasNext();) {
					String s = (String) iterator2.next();
					if (s.equals("0")) {
						l.add("Sposato");
					} else {
						l.add("Non sposato");
					}
				}
			} else if (type.equals("titolo")) {
				for (Iterator iterator2 = valuesList.iterator(); iterator2.hasNext();) {
					Integer s = Integer.parseInt((String) iterator2.next());
					switch (s) {
						case 0 :
							l.add("Laureato");
							break;
						case 1 :
							l.add("Diplomato");
							break;
						case 2 :
							l.add("Diplomato");
							break;
						case 3 :
							l.add("Master");
							break;
					}
				}
			} else {
				for (Iterator iterator2 = valuesList.iterator(); iterator2.hasNext();) {
					String s = (String) iterator2.next();
					l.add(s+ " descrizione");
				}
			}
			map.put(type, l);

		}

		return map;
	}


	public void setDataSource(IDataSource dataSource) {
		// TODO Auto-generated method stub
		
	}


	public IDataSource getDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
