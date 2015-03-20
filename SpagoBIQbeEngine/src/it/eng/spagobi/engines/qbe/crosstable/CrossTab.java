/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.qbe.crosstable;

import groovy.util.Eval;
import it.eng.spagobi.engines.worksheet.WorksheetEngineInstance;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.engines.worksheet.bo.FieldOption;
import it.eng.spagobi.engines.worksheet.bo.FieldOptions;
import it.eng.spagobi.engines.worksheet.bo.Measure;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.engines.worksheet.bo.WorksheetFieldsOptions;
import it.eng.spagobi.engines.worksheet.serializer.json.WorkSheetSerializationUtils;
import it.eng.spagobi.engines.worksheet.widgets.CrosstabDefinition;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Alberto Ghedin
 * 
 * This Class encapsulates the crossTab
 * The publics methods are:
 * - CrossTab(IDataStore dataStore, CrosstabDefinition crosstabDefinition) that builds
 *   the crossTab (headers structure and data)
 * - getJSONCrossTab() that returns the JSON representation of the crosstab
 */

public class CrossTab {
	
	public static final String CROSSTAB_NODE_JSON_KEY = "node_key";
	public static final String CROSSTAB_NODE_JSON_CHILDS = "node_childs";
	public static final String CROSSTAB_JSON_ROWS_HEADERS = "rows";
	public static final String CROSSTAB_JSON_ROWS_HEADERS_DESCRIPTION = "rows_description";
	public static final String CROSSTAB_JSON_COLUMNS_HEADERS = "columns";
	public static final String CROSSTAB_JSON_DATA = "data";
	public static final String CROSSTAB_JSON_CONFIG = "config";
	public static final String CROSSTAB_JSON_MEASURES_METADATA = "measures_metadata";
	public static final String CROSSTAB_JSON_ROWS_HEADER_TITLE = "rowHeadersTitle";
	public static final String CROSSTAB_CELLTYPEOFCOLUMNS = "celltypeOfColumns";
	public static final String CROSSTAB_CELLTYPEOFROWS = "celltypeOfRows";
	
	public static final String CROSSTAB_JSON_VALUE_DESCRIPTION_MAP = "valueDescriptionMap";
	public static final String MEASURE_NAME = "name";
	public static final String MEASURE_TYPE = "type";
	public static final String MEASURE_FORMAT = "format";
	public static final String MEASURE_POSITION = "measurePosition";
	public static final String TOTAL = "Total";
	public static final String SUBTOTAL = "SubTotal";

	private static final String PATH_SEPARATOR = "_S_";
	private static final String DATA_MATRIX_NA = "NA";
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "dd/MM/yyyy" );
	private static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );

	private static Logger logger = Logger.getLogger(CrossTab.class);
	
	private Node columnsRoot;
	private Node rowsRoot;
	String[][] dataMatrix;
	private JSONObject config;
	private List<MeasureInfo> measures;
	private  CrosstabDefinition crosstabDefinition;
	private  List<String> rowHeadersTitles;
	private List<String[]> rowsSum; //sum calculate for the rows (summ the row 1.. )
	private List<String[]> columnsSum; //sum calculate for the rows (summ the row 1.. )
	private boolean measuresOnRow;
	
	public enum CellType {
		DATA("data"), CF("cf"), SUBTOTAL("partialsum"), TOTAL("totals");
			CellType(String value){
				this.value = value;
			}
			private String value;
			
			public String getValue(){
				return this.value;
			}
			
		}
	
	private List<CellType> celltypeOfColumns;
	private List<CellType> celltypeOfRows;
	
	public CrossTab(){}
	
	/**
	 * Builds the crossTab (headers structure and data)
	 * @param dataStore: the source of the data
	 * @param crosstabDefinition: the definition of the crossTab
	 * @param fieldOptions: fieldOptions
	 * @param calculateFields: array of JSONObjects the CF
	 */
	public CrossTab(IDataStore dataStore, CrosstabDefinition crosstabDefinition, WorksheetEngineInstance worksheetEngineInstance, JSONArray calculateFields) throws JSONException{
		this(dataStore, crosstabDefinition);
		
		rowsSum =  getTotalsOnRows(measuresOnRow);
		columnsSum =  getTotalsOnColumns(measuresOnRow);
		
		if(calculateFields!=null){
			for(int i=0; i<calculateFields.length(); i++){
				
				JSONObject cf = calculateFields.getJSONObject(i);
				boolean horizontal =  cf.getBoolean("horizontal");
				calculateCF(cf.getString("operation"), horizontal, cf.getInt("level"), cf.getString("name"), CellType.CF);
			}
		}
		addMeasuresScaleFactor(worksheetEngineInstance);
		addSubtotals();
		
		addTotals();
		List<CrosstabDefinition.Column> columns =  crosstabDefinition.getColumns();
		addHeaderTitles(columns, 0, columnsRoot);
	}
	


	/**
	 * Builds the crossTab (headers structure and data)
	 * @param dataStore: the source of the data
	 * @param crosstabDefinition: the definition of the crossTab
	 */
	public CrossTab(IDataStore valuesDataStore, CrosstabDefinition crosstabDefinition) throws JSONException{
		IRecord valueRecord;
		String rowPath;
		String columnPath;
		this.config = crosstabDefinition.getConfig();
		this.crosstabDefinition = crosstabDefinition;
		int cellLimit = crosstabDefinition.getCellLimit();
		boolean columnsOverflow = false; //true if the number of cell shown in the crosstab is less than the total number of cells
		boolean measuresOnColumns = crosstabDefinition.isMeasuresOnColumns();
		measuresOnRow = config.getString("measureson").equals("rows");
		int rowsCount = crosstabDefinition.getRows().size();
		int columnsCount = crosstabDefinition.getColumns().size();
		int measuresCount = crosstabDefinition.getMeasures().size();
		int index;
		
//		if(crosstabDefinition.isPivotTable()){
//			cellLimit = cellLimit/measuresCount;
//		}else{
//			cellLimit=0;
//		}
//		
		
		List<String> rowCordinates = new ArrayList<String>();
		List<String> columnCordinates = new ArrayList<String>();
		List<String> data = new ArrayList<String>();

		columnsRoot = new Node("rootC");
		rowsRoot = new Node("rootR");
		
		int cellCount = 0;
		int actualRows = 0;
		int actualColumns = 0;
		for(index = 0; index<valuesDataStore.getRecordsCount() && (cellLimit<=0 || cellCount<cellLimit); index++){
			valueRecord = valuesDataStore.getRecordAt(index);

			boolean columnInserted = addRecord(columnsRoot, valueRecord, 0, columnsCount);
			boolean rowInserted = addRecord(rowsRoot, valueRecord, columnsCount, columnsCount+rowsCount);
			actualRows += rowInserted ? 1 : 0;
			actualColumns += columnInserted ? 1 : 0;
			cellCount = actualRows * actualColumns * measuresCount;
		}
		
		columnsRoot.orderedSubtree();
		rowsRoot.orderedSubtree();
		
		if(index<valuesDataStore.getRecordsCount()){
			logger.debug("Crosstab cells number limit exceeded");
			Node completeColumnsRoot =  new Node("rootCompleteC");
			for(index = 0; index<valuesDataStore.getRecordsCount(); index++){
				valueRecord = valuesDataStore.getRecordAt(index);
				
				addRecord(completeColumnsRoot, valueRecord, 0, columnsCount);
			}
			columnsOverflow =  columnsRoot.getLeafsNumber()<completeColumnsRoot.getLeafsNumber();
		}
				
		for(index = 0; index<valuesDataStore.getRecordsCount(); index++){
			valueRecord = valuesDataStore.getRecordAt(index);
			List<IField> fields= valueRecord.getFields();
			columnPath="";
			for(int i=0; i<columnsCount; i++){
				Object value = fields.get(i).getValue();
				String valueStr = null;
				if (value == null){
					valueStr = "null";
				} else {
					valueStr = value.toString();
				}
				columnPath = columnPath + PATH_SEPARATOR + valueStr;
			}
						
			rowPath="";
			for(int i=columnsCount; i<valueRecord.getFields().size()-measuresCount; i++){
				Object value = fields.get(i).getValue();
				String valueStr = null;
				if (value == null){
					valueStr = "null";
				} else {
					valueStr = value.toString();
				}
				rowPath = rowPath + PATH_SEPARATOR+ valueStr.toString();
			}
			
				
			for(int i=valueRecord.getFields().size()-measuresCount; i<valueRecord.getFields().size(); i++){
				columnCordinates.add(columnPath);
				rowCordinates.add(rowPath);
				data.add(""+getStringValue(fields.get(i).getValue()));
			}
		}
		
		List<String> columnsSpecification = getLeafsPathList(columnsRoot);
		List<String> rowsSpecification = getLeafsPathList(rowsRoot);
		
		if(measuresOnColumns){
			addMeasuresToTree(columnsRoot, crosstabDefinition.getMeasures());
		}else{
			addMeasuresToTree(rowsRoot, crosstabDefinition.getMeasures());
		}
		config.put("columnsOverflow", columnsOverflow);
		dataMatrix = getDataMatrix(columnsSpecification, rowsSpecification, columnCordinates, rowCordinates, data, measuresOnColumns, measuresCount, columnsRoot.getLeafsNumber());
		
		// put measures' info into measures variable 
		measures = new ArrayList<CrossTab.MeasureInfo>();
		IMetaData meta = valuesDataStore.getMetaData();
		for(int i = meta.getFieldCount() - measuresCount; i < meta.getFieldCount(); i++){
			// the field number i contains the measure number (i - <number of dimensions>)
			// but <number of dimension> is <total fields count> - <total measures count>
			IFieldMetaData fieldMeta = meta.getFieldMeta(i);
			Measure relevantMeasure = crosstabDefinition.getMeasures().get( i - (meta.getFieldCount() - measuresCount));
			measures.add(getMeasureInfo(fieldMeta, relevantMeasure));
		}
		
		
		celltypeOfColumns = new ArrayList<CrossTab.CellType>();
		celltypeOfRows = new ArrayList<CrossTab.CellType>();
		
		for(int i=0; i< dataMatrix.length; i++){
			celltypeOfRows.add(CellType.DATA);
		}
		
		for(int i=0; i< dataMatrix[0].length; i++){
			celltypeOfColumns.add(CellType.DATA);
		}
		
	}
	
	private <T extends Attribute> void addHeaderTitles(List<T> lines, int linesIndex, Node node){
		if(linesIndex<lines.size()){
			Node descriptionNode;
			if(node.getValue().equals(TOTAL)){
				descriptionNode = new Node(TOTAL);	
			}else{
				descriptionNode = new Node(lines.get(linesIndex).getAlias());
			}
			linesIndex++;
			List<Node> children = node.getChilds();
			List<Node> newchildren = new ArrayList<Node>();
			newchildren.add(descriptionNode);
			for (int i = 0; i < children.size(); i++) {
				descriptionNode.addChild(node.getChilds().get(i));
				addHeaderTitles(lines,linesIndex , node.getChilds().get(i));
			}
			node.setChilds(newchildren);
		}	
	}
	
	private <T extends Attribute> JSONArray getHeaderDescriptions(List<T> lines){
		JSONArray descriptions = new JSONArray();
		for (int i = 0; i < lines.size(); i++) {
			descriptions.put(lines.get(i).getAlias());
		}
		return descriptions;
	}
	

	/**
	 * Get the JSON representation of the cross tab  
	 * @return JSON representation of the cross tab  
	 * @throws JSONException
	 */
	public JSONObject getJSONCrossTab() throws JSONException{
		JSONObject crossTabDefinition = new JSONObject();
		crossTabDefinition.put(CROSSTAB_JSON_MEASURES_METADATA, getJSONMeasuresMetadata());
		crossTabDefinition.put(CROSSTAB_JSON_ROWS_HEADERS, rowsRoot.toJSONObject());
		
		JSONArray descriptions = getHeaderDescriptions( crosstabDefinition.getRows());
		crossTabDefinition.put(CROSSTAB_JSON_ROWS_HEADERS_DESCRIPTION, descriptions);
		
		crossTabDefinition.put(CROSSTAB_JSON_COLUMNS_HEADERS, columnsRoot.toJSONObject());
		crossTabDefinition.put(CROSSTAB_JSON_DATA,  getJSONDataMatrix());
		crossTabDefinition.put(CROSSTAB_CELLTYPEOFCOLUMNS,  serializeCellType(this.celltypeOfColumns));
		crossTabDefinition.put(CROSSTAB_CELLTYPEOFROWS,  serializeCellType(this.celltypeOfRows));
		crossTabDefinition.put(CROSSTAB_JSON_CONFIG,  config);
		return crossTabDefinition;
	}
	
	private JSONArray serializeCellType(List<CellType> celltypes){
		JSONArray types = new JSONArray();
		for(int i=0; i<celltypes.size(); i++){
			types.put(celltypes.get(i).getValue());
		}
		return types;
	}
	
	

	/**
	 * Get the matrix that represent the data
	 * @param columnsSpecification: A list with all the possible coordinates of the columns 
	 * @param rowsSpecification: A list with all the possible coordinates of the rows
	 * @param columnCordinates: A list with the column coordinates of all the data
	 * @param rowCordinates: A list with the column rows of all the data
	 * @param data: A list with the data
	 * @param measuresOnColumns: true if the measures live in the columns, false if the measures live in the rows
	 * @param measuresLength: the number of the measures
	 * @return the matrix that represent the data
	 */
	private String[][] getDataMatrix(List<String> columnsSpecification, List<String> rowsSpecification, List<String> columnCordinates, List<String> rowCordinates,  List<String> data, boolean measuresOnColumns, int measuresLength, int columnsN){
		String[][] dataMatrix;
		int x,y;
		int rowsN;
		
		if (measuresOnColumns) {
			rowsN = (rowsSpecification.size() > 0 ? rowsSpecification.size() : 1);
		} else {
			rowsN = (rowsSpecification.size() > 0 ? rowsSpecification.size() : 1)*measuresLength;
		}

		
		dataMatrix = new String[rowsN][columnsN];
		
		//init the matrix
		for(int i=0; i<rowsN; i++){
			for(int j=0; j<columnsN; j++){
				dataMatrix[i][j] = DATA_MATRIX_NA;
			}
		}
		
		if(measuresOnColumns){
			for(int i=0; i<data.size(); i=i+measuresLength){
				for(int j=0; j<measuresLength; j++){
					if ( rowsSpecification.size() > 0 ) {
						x = rowsSpecification.indexOf(rowCordinates.get(i+j));
						if ( x < 0 ) {
							continue; // elements not found because crosstab is too big and it was truncated
						}
					} else {
						x = 0; // crosstab with no attributes on rows
					}
					if ( columnsSpecification.size() > 0 ) {
						y = columnsSpecification.indexOf(columnCordinates.get(i+j));
						if ( y < 0 ) {
							continue; // elements not found because crosstab is too big and it was truncated
						}
					} else {
						y = 0; // crosstab with no attributes on columns
					}
					if((y*measuresLength+j)<columnsN && (y*measuresLength+j)>=0){
						dataMatrix[x][y*measuresLength+j]=data.get(i+j);
					}
				}
			}
		}else{
			for(int i=0; i<data.size(); i=i+measuresLength){
				for(int j=0; j<measuresLength; j++){
					if ( rowsSpecification.size() > 0 ) {
						x = rowsSpecification.indexOf(rowCordinates.get(i+j));
						if ( x < 0 ) {
							continue; // elements not found because crosstab is too big and it was truncated
						}
					} else {
						x = 0; // crosstab with no attributes on rows
					}


					if ( columnsSpecification.size() > 0 ) {
						y = columnsSpecification.indexOf(columnCordinates.get(i+j));
						if ( y < 0 ) {
							continue; // elements not found because crosstab is too big and it was truncated
						}
					} else {
						y = 0; // crosstab with no attributes on columns
					}
					
					if(y<columnsN && y>=0){
						dataMatrix[x*measuresLength+j][y]=data.get(i+j);
					}
				}
			}
		}		
		
		return dataMatrix;
		
	}
	
	/**
	 * Serialize the matrix in a JSON format
	 * @return the matrix in a JSON format
	 */
	public JSONArray getJSONDataMatrix(){
	
		JSONArray matrix = new JSONArray();
		JSONArray row = new JSONArray();
		
		//transform the matrix
		for(int i=0; i<dataMatrix.length; i++){
			row = new JSONArray();
			for(int j=0; j<dataMatrix[i].length; j++){
				row.put(dataMatrix[i][j]);
			}
			matrix.put(row);
		}
	
		return matrix;
	}

	/**
	 * Add to the root (columnRoot or rowRoot) a path from the root to a leaf. 
	 * A record contains both the columns definition and the rows definition:
	 * (it may be something like that: C1 C2 C3 R1 R2 M1 M1, where Ci represent a column,
	 * Ri represent a row, Mi a measure). So for take a column path (C1 C2 C3), we need
	 * need a start and end position in the record (in this case 0,3) 
	 * @param root: the node in witch add the record
	 * @param record
	 * @param startPosition 
	 * @param endPosition
	 */
	private boolean addRecord(Node root, IRecord valueRecord, int startPosition, int endPosition){
		boolean toReturn = false;
		IField valueField;
		Node node;
		Node nodeToCheck = root;
		int nodePosition;
		List<IField> valueFields= new ArrayList<IField>();
		
		valueFields = valueRecord.getFields();

		for(int indexFields = startPosition; indexFields<endPosition; indexFields++){
			valueField = valueFields.get(indexFields);
			//there is a description
			//there is only the value
			if (valueField.getValue() != null) {
				if (valueField.getDescription() != null) {
					node = new Node(valueField.getValue().toString(), valueField.getDescription().toString());
				}else{
					node = new Node(valueField.getValue().toString());
				}
			}else {
				node = new Node("null");
			}
			nodePosition = nodeToCheck.getChilds().indexOf(node);
			if(nodePosition<0){
				toReturn = true;
				nodeToCheck.addChild(node);
				nodeToCheck = node;
			}else{
				nodeToCheck = nodeToCheck.getChilds().get(nodePosition);
			}
		}
		return toReturn;
	}
	
	/**
	 * Return a list with all the path from the node n to the leafs
	 * @param n: the root node  
	 * @return list with all the path from the node n to the leafs
	 */
	private List<String> getLeafsPathList(Node n){
		List<String> toReturn = new ArrayList<String>();
		for(int i=0; i<n.getChilds().size(); i++){
			toReturn.addAll(visit(n.getChilds().get(i), PATH_SEPARATOR));
		}
		return toReturn;
	}
	
	private List<String> visit(Node n, String prefix) {
		List<String> toReturn = new ArrayList<String>();
		if (n.getChilds().size() == 0) {
			if (prefix.equals(PATH_SEPARATOR)) {
				toReturn.add(prefix + (String) (n.getValue()));
			} else {
				toReturn.add(prefix + PATH_SEPARATOR + (String) (n.getValue()));
			}
			return toReturn;
		} else {
			for (int i = 0; i < n.getChilds().size(); i++) {
				if (prefix.equals(PATH_SEPARATOR)) {
					toReturn.addAll(visit(n.getChilds().get(i), prefix
							+ (String) (n.getValue())));
				} else {
					toReturn.addAll(visit(n.getChilds().get(i), prefix
							+ PATH_SEPARATOR + (String) (n.getValue())));
				}
			}
			return toReturn;
		}
	}
	
	/**
	 * Add the measures as leafs to all the leafs 
	 * @param root
	 * @param measures
	 */
	private void addMeasuresToTree(Node root, List<Measure> measures){
		List<Node> measuresNodes = new ArrayList<Node>();
		for(int i=0; i<measures.size(); i++){
			measuresNodes.add(new Node(measures.get(i).getAlias()));
		}
		addMeasuresToLeafs(root,measuresNodes);
		
	}
	
	//It's ok that the list of the measures is the same for every leaf
	private void addMeasuresToLeafs(Node node, List<Node> measuresNodes){
		if(node.getChilds().size()==0){
			for(int i=0; i<measuresNodes.size(); i++){
				Node n = measuresNodes.get(i).clone();
				node.addChild(n);
			}
		}else{
			for(int i=0; i<node.getChilds().size(); i++){
				addMeasuresToLeafs(node.getChilds().get(i),measuresNodes);
			}
		}
	}

	
	private static String getStringValue(Object obj){
		
		if (obj == null) {
			return "NULL";
		}
		String fieldValue = null;
		
		Class clazz = obj.getClass();
		if (clazz == null) {
			clazz = String.class;
		} 
		if (Timestamp.class.isAssignableFrom(clazz)) {
			fieldValue =  TIMESTAMP_FORMATTER.format(  obj );
		} else if (Date.class.isAssignableFrom(clazz)) {
			fieldValue =  DATE_FORMATTER.format( obj );
		} else {
			fieldValue =  obj.toString();
		}
		
		return fieldValue;

	}
	

	
	private MeasureInfo getMeasureInfo(IFieldMetaData fieldMeta, Measure measure) {
		Class clazz = fieldMeta.getType();
		if (clazz == null) {
			clazz = String.class;
		} 
		
		String fieldName = measure.getAlias();  // the measure name is not the name (or alias) of the field coming with the datastore
												// since it is something like SUM(col_0_0_) (see how crosstab datastore query is created)
		
		if( Number.class.isAssignableFrom(clazz) ) {
			
			//BigInteger, Integer, Long, Short, Byte
			if(Integer.class.isAssignableFrom(clazz) 
		       || BigInteger.class.isAssignableFrom(clazz) 
			   || Long.class.isAssignableFrom(clazz) 
			   || Short.class.isAssignableFrom(clazz)
			   || Byte.class.isAssignableFrom(clazz)) {
				return new MeasureInfo(fieldName, measure.getEntityId(), "int", null);
			} else {
				String decimalPrecision = (String)fieldMeta.getProperty(IFieldMetaData.DECIMALPRECISION);
				if(decimalPrecision!=null){
					return new MeasureInfo(fieldName,measure.getEntityId(), "float", "{decimalPrecision:"+decimalPrecision+"}");
				}else{
					return new MeasureInfo(fieldName,measure.getEntityId(), "float", null);
				}
			}
			
		} else if( Timestamp.class.isAssignableFrom(clazz) ) {
			return new MeasureInfo(fieldName, measure.getEntityId(),"timestamp", "d/m/Y H:i:s");
		} else if( Date.class.isAssignableFrom(clazz) ) {
			return new MeasureInfo(fieldName,measure.getEntityId(), "date", "d/m/Y");
		} else {
			return new MeasureInfo(fieldName,measure.getEntityId(), "string", null);
		}
	}
	
	
	private JSONArray getJSONMeasuresMetadata() throws JSONException {
		JSONArray array = new JSONArray();
		for(int i=0; i<measures.size(); i++){
			MeasureInfo mi = measures.get(i);
			JSONObject jsonMi = new JSONObject();
			jsonMi.put(MEASURE_NAME, mi.getName());
			jsonMi.put(MEASURE_TYPE, mi.getType());
			jsonMi.put(MEASURE_FORMAT, mi.getFormat() != null ? mi.getFormat() : "");
			jsonMi.put(MEASURE_POSITION, i);
			array.put(jsonMi);
		}
		return array;
	}
	
	public String getMeasureScaleFactor(String name) {
		Iterator<MeasureInfo> it = measures.iterator();
		while (it.hasNext()) {
			MeasureInfo mi = it.next();
			if(mi.getName().equals(name)){
				return mi.getScaleFactor();
			}
		}
		return "";
	}
	
	public static class MeasureInfo {
		
		String name;
		String type;
		String format;
		String id;
		String scaleFactor;
		
		public MeasureInfo(String name, String id, String type, String format) {
			this.name = name;
			this.type = type;
			this.format = format;
			this.id = id;
		}

		public String getName() {
			return name;
		}
		public String getType() {
			return type;
		}
		public String getFormat() {
			return format;
		}

		public String getScaleFactor() {
			return scaleFactor;
		}

		public void setScaleFactor(String scaleFactor) {
			this.scaleFactor = scaleFactor;
		}

		public String getId() {
			return id;
		}
		
		
	}
	


	/*************************************************
	 *               CALCULATED FIELDS
	 ************************************************/

	/**
	 * Get a list of nodes and merge them..
	 * It builds a tree with all the node in common with the subtree in with
	 * radix in input..
	 * For example nodes=[A,[1,2,3], A,[1,6]] the result is [A,[1]]
	 * In the leafs (in this case 1) it add the position of that
	 * header in the matrix..
	 * In this case, suppose the id of the first occurence of A.1 is at row 3 and the second in row 7
	 * the leafs with id A.1 has this list [3,7]
	 * @param nodes
	 * @param NodeValue
	 * @return
	 */
	private Node mergeNodes(List<Node> nodes, String NodeValue){
		Assert.assertNotNull(nodes, "We need at least a node to merge");
		Assert.assertTrue(nodes.size()>0, "We need at least a node to merge");
		int index;
		List<Node> commonChildNode;
		Node newNode = new Node(NodeValue);
		newNode.setCellType(CellType.CF);
		List<Node> newchilds = new ArrayList<Node>();
		if(nodes.size()>0){
			//get the first node. If a child of the first node
			//is not a child of the other nodes is not in common... 
			Node firstNode = nodes.get(0);
			List<Node> firstNodeChilds = firstNode.getChilds();
			if(firstNodeChilds!=null && firstNodeChilds.size()>0){
				for(int i=0; i<firstNodeChilds.size(); i++){
					commonChildNode = new ArrayList<Node>();
					commonChildNode.add(firstNodeChilds.get(i));
					//look for the child in all other nodes
					for(int j=1; j<nodes.size(); j++){
						index = nodes.get(j).getChilds().indexOf(firstNodeChilds.get(i));
						if(index>=0){
							commonChildNode.add(nodes.get(j).getChilds().get(index));
						}else{
							commonChildNode = null;
							break;
						}
					}
					if(commonChildNode!=null){
						newchilds.add(mergeNodes(commonChildNode, firstNodeChilds.get(i).getValue()));
					}
				}
			}else{
				//we are the leafs.. so we want the id of the node
				List<Integer> leafPositions= new ArrayList<Integer>();
				for(int j=0; j<nodes.size(); j++){
					leafPositions.add(nodes.get(j).getLeafPosition());
				}
				newNode.setLeafPositionsForCF(leafPositions);
			}
		}
		newNode.setChilds(newchilds);
		return newNode;
	}
	
	/**
	 * Remove the leafs not in the last level of the tree
	 * @param node
	 * @param level
	 */
	private void cleanTreeAfterMerge(Node node, int level){
		int treeDepth = node.getSubTreeDepth();
		List<Node> listOfNodesToRemove = cleanTreeAfterMergeRecorsive(node, treeDepth, level);
		for (Iterator iterator = listOfNodesToRemove.iterator(); iterator.hasNext();) {
			Node node2 = (Node) iterator.next();
			node2.removeNodeFromTree();
			
		}
	}
	
	/**
	 * Remove the dead nodes (the inner nodes with no leafs)
	 * @param node
	 * @param treeDepth
	 * @param level
	 * @return
	 */
	private List<Node> cleanTreeAfterMergeRecorsive(Node node, int treeDepth, int level){
		List<Node> listOfNodesToRemove = new ArrayList<Node>();
		if(node.getChilds().size()==0){
			if(level<treeDepth-1){
				listOfNodesToRemove.add(node);
			}
		}else{
			for(int i=0; i<node.getChilds().size(); i++){
				listOfNodesToRemove.addAll(cleanTreeAfterMergeRecorsive(node.getChilds().get(i), treeDepth, level+1));
			}
		}
		return listOfNodesToRemove;
	}
	
	
	/**
	 * Calculate the calculated fields and add the result in the structure
	 * @param operation 
	 * @param horizontal
	 * @param level
	 * @param cfName
	 * @param celltype
	 */
	private void calculateCF(String operation,  boolean horizontal, int level, String cfName, CellType celltype){
		Node rootNode;
		List<Node> fathersOfTheNodesOfTheLevel;
		if(horizontal){
			rootNode = columnsRoot;
		}else{
			rootNode = rowsRoot;
		}
		
		if(horizontal){
			fathersOfTheNodesOfTheLevel = rootNode.getLevel(level-2);//because of the title of the headers
		}else{
			fathersOfTheNodesOfTheLevel = rootNode.getLevel(level-1);
		}
	
		for(int i=0; i<fathersOfTheNodesOfTheLevel.size(); i++){
			rootNode.setLeafPositions();
			calculateCFSub(operation, fathersOfTheNodesOfTheLevel.get(i), horizontal, level, cfName, celltype);
		}
	}
	
	/**
	 * Calculate the calculated fields and add the result in the structure
	 * @param operation
	 * @param node the result of the calculated fields will add as child of this node
	 * @param horizontal
	 * @param level
	 * @param cfName
	 * @param celltype
	 */
	private void calculateCF(String operation, Node node, boolean horizontal, int level, String cfName, CellType celltype){
		Node rootNode;
		if(horizontal){
			rootNode = columnsRoot;
		}else{
			rootNode = rowsRoot;
		}
		
		List<Node> fathersOfTheNodesOfTheLevel = new ArrayList<Node>();
		fathersOfTheNodesOfTheLevel.add(node);
		
		for(int i=0; i<fathersOfTheNodesOfTheLevel.size(); i++){
			rootNode.setLeafPositions();
			calculateCFSub(operation, fathersOfTheNodesOfTheLevel.get(i), horizontal, level, cfName, celltype);
		}
	}
	
	/**
	 * 
	 * @param operation
	 * @param node the parent node of the CF
	 * @param horizontal
	 * @param level
	 * @param cfName
	 * @param celltype
	 */
	private void calculateCFSub(String operation, Node node, boolean horizontal, int level, String cfName,  CellType celltype){
		List<String[]> calculatedFieldResult = new ArrayList<String[]>();
		List<String> operationParsed;
		List<String> operationExpsNames;
		List<List<String>> parseOperationR = parseOperation(operation);
		operationParsed = parseOperationR.get(0);
		operationExpsNames = parseOperationR.get(1);
		
		List<Node> levelNodes = node.getChilds();
		
		Object[] expressionMap = buildExpressionMap(levelNodes, operationExpsNames);
		Map<String, Integer> expressionToIndexMap = (Map<String, Integer>) expressionMap[1];
		List<Node> nodeInvolvedInTheOperation = (List<Node>) expressionMap[0];
		if(nodeInvolvedInTheOperation.size()>0){
			
			//add the header
			int positionToAdd = node.getRightMostLeafPositionCF()+1;
			
			Node mergedNode = mergeNodes(nodeInvolvedInTheOperation, cfName);
			cleanTreeAfterMerge(mergedNode, level);
			List<Node> mergedNodeLeafs = mergedNode.getLeafs();
			for(int i=0; i<mergedNodeLeafs.size(); i++){
				List<Integer> leafPositions =  mergedNodeLeafs.get(i).getLeafPositionsForCF();
				List<String[]> arraysInvolvedInTheOperation = getArraysInvolvedInTheOperation(horizontal, operationExpsNames, expressionToIndexMap,leafPositions);
				calculatedFieldResult.add(executeOperationOnArrays(arraysInvolvedInTheOperation, operationParsed));
				List<String[]> totalArraysInvolvedInTheOperation = getTotalArraysInvolvedInTheOperation(horizontal,  leafPositions);
				executeOperationOnTotalArraysAndUpdate(totalArraysInvolvedInTheOperation, operationParsed, horizontal, positionToAdd);
			}

			
			node.addChild(mergedNode);
			addCrosstabDataLine(positionToAdd, calculatedFieldResult, horizontal, celltype);
		}
	}
	
	/**
	 * Parse the operation
	 * @param operation
	 * @return
	 */
	private static List<List<String>> parseOperation(String operation){
		String freshOp = " "+operation;
		List<String> operationParsed = new ArrayList<String>();
		List<String> operationExpsNames = new ArrayList<String>();
		int index =0;
    	//parse the operation
    	while(freshOp.indexOf("field[")>=0){
    		index =  freshOp.indexOf("field[")+6;
    		operationParsed.add(freshOp.substring(0,index-6));
    		freshOp = freshOp.substring(index);
    		index = freshOp.indexOf("]");
    		operationExpsNames.add(freshOp.substring(0, index));
    		freshOp = freshOp.substring(index+1);
    	}
    	operationParsed.add(freshOp);
    	List<List<String>> toReturn=  new ArrayList<List<String>>();
    	toReturn.add(operationParsed);
    	toReturn.add(operationExpsNames);
    	return toReturn;
	}
	
	/**
	 * Build the list of the nodes involved in the operation and the map of the indexes
	 * es: 
	 * 	nodes: [A,[1,2]], [B[1,3]] , [C[1,3]]
	 *  operationExpsNames: [A, C]
	 *  nodeInvolvedInTheOperation: [A,[1,2]], [B[1,3]]
	 *  expressionToIndexMap: 0,2
	 * @param nodes the list of the nodes of the level involved in the cf
	 * @param operationExpsNames the alias of the fields in the query
	 * @return
	 */
	private Object[] buildExpressionMap(List<Node> nodes, List<String> operationExpsNames){
		Map<String, Integer> expressionToIndexMap = new HashMap<String, Integer>();
		List<Node> nodeInvolvedInTheOperation = new ArrayList<Node>();
		int foundNode=0;
		for (Iterator<String> iterator = operationExpsNames.iterator(); iterator.hasNext();) {
			String operationElement = iterator.next();
			if(!expressionToIndexMap.containsKey(operationElement)){
				expressionToIndexMap.put(operationElement, foundNode);
				for(int y=0; y<nodes.size();y++){
					if(nodes.get(y).getValue().equals(operationElement)){
						nodeInvolvedInTheOperation.add(nodes.get(y));
						foundNode++;
						break;
					}
				}
			}
		}
		Object[] toReturn= new Object[2]; 
		toReturn[0]=nodeInvolvedInTheOperation;
		toReturn[1]=expressionToIndexMap;

		return toReturn;
	}
	
	/**
	 * 
	 * @param horizontal
	 * @param operationExpsNames the names of the operation : A+ D+C-(A*C) = A,D,C,A,C
	 * @param expressionToIndexMap if the operation is the same of before and the Nodes of the level are A,B,C,D the map is (A->0, B->1...)
	 * @param indexInTheArray
	 * @return
	 */
	private List<String[]> getArraysInvolvedInTheOperation(boolean horizontal, List<String> operationExpsNames,  Map<String, Integer> expressionToIndexMap, List<Integer> indexInTheArray){
		List<String[]> toReturn = new ArrayList<String[]>();
		for (int y=0; y<operationExpsNames.size(); y++) {
			String alias = operationExpsNames.get(y);
			int index = expressionToIndexMap.get(alias);
			if(horizontal){
				toReturn.add(getCrosstabDataColumn(indexInTheArray.get(index)));
			}else{
				toReturn.add(getCrosstabDataRow(indexInTheArray.get(index)));
			}
		}
		return toReturn;
	}
	

	private List<String[]> getTotalArraysInvolvedInTheOperation(boolean horizontal,  List<Integer> indexInTheArray){
		List<String[]> toReturn = new ArrayList<String[]>();
		
		for (int y=0; y<indexInTheArray.size(); y++) {
			if(horizontal){
				String[] sum = new String[columnsSum.size()];
				for(int i=0; i<columnsSum.size(); i++){
					sum[i] = columnsSum.get(i)[(indexInTheArray.get(y))];
				}
				toReturn.add(sum);
			}else{
				String[] sum = new String[rowsSum.size()];
				for(int i=0; i<rowsSum.size(); i++){
					sum[i] = rowsSum.get(i)[(indexInTheArray.get(y))];
				}
				toReturn.add(sum);
			}
		}
		return toReturn;
	}
	
	/**
	 * Build and execute the operation.. For example x+y = [4,6]
	 * @param data list of rows/columns members of the operation l'operazione es [1,2], [3,4]
	 * @param operation the operation es: [+]
	 * @return
	 */
	private String[] executeOperationOnArrays(List<String[]> data, List<String> operation){
		List<String> operationElements;
		int datalength = data.get(0).length;
		String[] operationResult = new String[datalength];
		for(int i =0; i<datalength; i++){
			operationElements = new ArrayList<String>();
			for(int j =0; j<data.size(); j++){
				operationElements.add(data.get(j)[i]);
			}	
			operationResult[i] = executeOperationOnNumbers(operationElements, operation);
		}
		return operationResult;
	}
	
	private void executeOperationOnTotalArraysAndUpdate(List<String[]> data, List<String> operation, boolean horizontal, int positionToAdd){
		String[] executeOperationOnArrays = executeOperationOnArrays(data, operation);
		if(horizontal){
			for(int i=0; i<executeOperationOnArrays.length; i++){
				String [] sum = columnsSum.get(i);
				String [] newSum = new String[sum.length+1];
				for(int y=0; y<sum.length+1; y++){
					if(y<positionToAdd){
						newSum[y]= sum[y];
					}else if (y==positionToAdd) {
						newSum[y] = executeOperationOnArrays[i];
					}else{
						newSum[y]= sum[y-1];
					}
				}
				columnsSum.set(i, newSum);
			}
		}else{
			for(int i=0; i<executeOperationOnArrays.length; i++){
				String [] sum = rowsSum.get(i);
				String [] newSum = new String[sum.length+1];
				for(int y=0; y<sum.length+1; y++){
					if(y<positionToAdd){
						newSum[y]= sum[y];
					}else if (y==positionToAdd) {
						newSum[y] = executeOperationOnArrays[i];
					}else{
						newSum[y]= sum[y-1];
					}
				}
				rowsSum.set(i, newSum);
			}
		}
	}
	
	/**
	 * Execute the operation
	 * 1+2-(2*4)
	 * @param data the members of the operation.. es: 1,2,3,4
	 * @param op the list of operator: +,-(,*,)
	 * @return
	 */
	private String executeOperationOnNumbers(List<String> data, List<String> op){
    	String operation ="";
    	int i=0;
    	for(i=0; i<op.size()-1; i++){
    		operation = operation+op.get(i);
    		
    		if(data.get(i)==DATA_MATRIX_NA|| data.get(i)=="null"  || data.get(i)==null){
    			operation = operation+"0";
    		}else{
    			operation = operation+data.get(i);
    		}
    	}
    	operation = operation + op.get(i);
    	String evalued = (Eval.me(operation)).toString();
    	return evalued;
	}

	
	/************************************************************
	  							TOTALS
	 ***********************************************************  */
	
	
	/**
	 * Sum the values of the rows (the right pannel)
	 * @param measuresOnRow
	 */
	private List<String[]> getTotalsOnRows(boolean measuresOnRow){
		List<String[]> sum = new ArrayList<String[]>();
		double[] st;
		int measures = 1;
		if(!measuresOnRow){
			measures= this.measures.size();
		}
		int iteration = dataMatrix[0].length/measures;
		for(int measureId=0; measureId<measures; measureId++){
			st = new double[dataMatrix.length];
			for(int i=0; i<dataMatrix.length; i++){
				for(int j=0; j<iteration; j++){
					try{
						if(getCellType(i,j*measures+measureId).equals(CellType.DATA) || getCellType(i,j*measures+measureId).equals(CellType.TOTAL)){
							String value = dataMatrix[i][j*measures+measureId];
							if(!value.equals(DATA_MATRIX_NA)){
								st[i] = st[i] + new Double(value);
							}
						}
					} catch (Exception e) {
						logger.debug("Cant format the number "+ (dataMatrix[i][j*measures+measureId]));
					}
				}
			}
			sum.add(toStringArray(st));
		}
		return sum;
	}
	
	private String[] getTotalsOfRows(int start, int length){
		double[] st = new double[dataMatrix[0].length];

		for(int i=0; i<dataMatrix[0].length; i++){
			for(int j=start; j<length+start; j++){
				try {
					if(getCellType(j,i).equals(CellType.DATA)){
						String value = dataMatrix[j][i];
						if(!value.equals(DATA_MATRIX_NA)){
							st[i] = st[i] + new Double(value);
						}
					}
				} catch (Exception e) {
					logger.debug("Cant format the number "+ (dataMatrix[j][i]));
				}
			}
		}

		return toStringArray(st);
	}
	
	private String[] toStringArray(double[] doubleArray){
		String[] strings = new String[doubleArray.length];
		for(int i=0; i<doubleArray.length; i++){
			strings[i] = ""+(doubleArray[i]);
		}
		return strings;
	}
	
	/**
	 * Sum the values of the columns (the bottom pannel)
	 * @param measuresOnRow
	 * @return
	 */
	private List<String[]> getTotalsOnColumns(boolean measuresOnRow){
		List<String[]> sum = new ArrayList<String[]>();
		double[] st;
		int measures = 1;
		if(measuresOnRow){
			measures= this.measures.size();
		}
		int iteration = dataMatrix.length/measures;
		for(int measureId=0; measureId<measures; measureId++){
			st = new double[dataMatrix[0].length];
			for(int i=0; i<iteration; i++){
				for(int j=0; j<dataMatrix[0].length; j++){
					try {
						if(getCellType(i*measures+measureId,j).equals(CellType.DATA) || getCellType(i*measures+measureId,j).equals(CellType.TOTAL)){
							String value = dataMatrix[i*measures+measureId][j];
							if(!value.equals(DATA_MATRIX_NA)){
								st[j] = st[j] + new Double(value);	
							}
						}
					} catch (Exception e) {
						logger.debug("Cant format the number "+ (dataMatrix[i*measures+measureId][j]));
					}
				}
			}
			sum.add(toStringArray(st));
		}
		return sum;
	}
	
	private String[] getTotalsOfColumns(int start, int length){
		double[] st = new double[dataMatrix.length];

		for(int i=0; i<dataMatrix.length; i++){
			for(int j=start; j<length+start; j++){
				try {
					if(getCellType(i,j).equals(CellType.DATA)){
						String value = dataMatrix[i][j];
						if(!value.equals(DATA_MATRIX_NA)){
							st[i] = st[i] + new Double(value);
						}
					}
				} catch (Exception e) {
					logger.debug("Cant format the number "+ (dataMatrix[i][j]));
				}
			}
		}

		return toStringArray(st);
	}
	
	
	private double getTotalsOfColumn(int colunm, CellType type){

		double sum =0;
		for(int y=0; y<dataMatrix.length; y++){
			if( celltypeOfColumns.get(colunm).equals(type)){
				String value = dataMatrix[y][colunm];
				if(!value.equals(DATA_MATRIX_NA)){
					sum = sum+new Double(value);
				}
			}
		}
		return sum;
	}
	
	private String[] getTotalsOfColumnWithMeasure(int colunm){

		double[] st = new double[measures.size()];
		int measurescount =0;

		for(int y=0; y<dataMatrix.length; y++){
			if( !celltypeOfRows.get(y).equals(CellType.CF)){
				st[measurescount%measures.size()] = st[measurescount%measures.size()]+new Double(dataMatrix[y][colunm]);
				measurescount++;
			}
		}
		
		return toStringArray(st);
	}
	
	private double getTotalsOfRow(int row, CellType type){

		double sum =0;
		for(int y=0; y<dataMatrix[0].length; y++){
			if( celltypeOfRows.get(y).equals(type)){
				String value = dataMatrix[row][y];
				if(!value.equals(DATA_MATRIX_NA)){
					sum = sum+new Double(value);
				}
			}
		}
		return sum;
	}
	
	private String[] getTotalsOfRowWithMeasure(int row){

		double[] st = new double[measures.size()];
		int measurescount =0;

		for(int y=0; y<dataMatrix[0].length; y++){
			if( !celltypeOfColumns.get(y).equals(CellType.CF)){
				String value = dataMatrix[row][y];
				if(!value.equals(DATA_MATRIX_NA)){
					st[measurescount%measures.size()] = st[measurescount%measures.size()]+new Double(value);
				}
				measurescount++;
			}
		}
		
		return toStringArray(st);
	}

	
	/**
	 * Get the super total (sum of sums)
	 * @param measuresNumber the number of the measures
	 * @return
	 */
	private String[] getSuperTotal(int measuresNumber){
		
		double[] st = new double[measuresNumber];
		

		if(measuresOnRow){
			for(int i=0; i<measuresNumber; i++){
				for(int y=0; y<columnsSum.get(0).length; y++){
					if( celltypeOfColumns.get(y).equals(CellType.DATA)){
						String value = columnsSum.get(i)[y];
						if(!value.equals(DATA_MATRIX_NA)){
							st[i] = st[i]+new Double(value);
						}
					}
				}
			}
		}else{
			int measureIteration =0;
			for(int y=0; y<columnsSum.get(0).length; y++){
				if( celltypeOfColumns.get(y).equals(CellType.DATA)){
					String value = columnsSum.get(0)[y];
					if(!value.equals(DATA_MATRIX_NA)){
						st[measureIteration%measuresNumber] = st[measureIteration%measuresNumber]+new Double(value);	
					}
					measureIteration++;
				}
			}
		}

		return toStringArray(st);
	}
	
	/**
	 * 
	 * @param withMeasures
	 * @param deepth = tree depth-1
	 * @return
	 */
	private Node getHeaderTotalSubTree(boolean withMeasures, int deepth){
		Node node= new Node(TOTAL);
		if(withMeasures && deepth==2){
			for(int i=0; i<measures.size(); i++){
				node.addChild(new Node(measures.get(i).getName()));
			}
		}else{
			if(deepth>1){
				node.addChild(getHeaderTotalSubTree(withMeasures, deepth-1));
			}
		}
		return node;
	}
	
	
	private void addTotals() throws JSONException{
		
		String rowsTotals =  config.optString("calculatetotalsoncolumns");
		String columnsTotals =  config.optString("calculatetotalsonrows");
		
		
		if(rowsTotals!=null && rowsTotals.equals("on")){
			rowsRoot.addChild(getHeaderTotalSubTree(measuresOnRow, rowsRoot.getSubTreeDepth()-1));
			addCrosstabDataRow(dataMatrix.length, columnsSum, CellType.TOTAL);
		}
		
		if(columnsTotals!=null && columnsTotals.equals("on")){
			//add the total of totals
			int measures = this.measures.size();
			String[] superTotals = getSuperTotal(measures);
			
			if(measuresOnRow){
				String[] freshRowsSum = Arrays.copyOf(rowsSum.get(0), rowsSum.get(0).length+measures);
				for (int j = 0; j < superTotals.length; j++) {
					freshRowsSum[rowsSum.get(0).length+j] = superTotals[j];
				}
				rowsSum.set(0,freshRowsSum);
			}else{
				for (int j = 0; j < superTotals.length; j++) {
					String[] freshRowsSum = Arrays.copyOf(rowsSum.get(j), rowsSum.get(j).length+1);
					freshRowsSum[rowsSum.get(j).length] = superTotals[j];
					rowsSum.set(j,freshRowsSum);
				}
			}
			columnsRoot.addChild(getHeaderTotalSubTree(!measuresOnRow, columnsRoot.getSubTreeDepth()-1));
			addCrosstabDataColumns(dataMatrix[0].length, rowsSum, CellType.TOTAL);
		}
	}

	
	/************************************************************************
	 *								 SUBTOTALS
	********************************************************************** */
	public void addSubtotals(){
		String  rowsTotals = config.optString("calculatesubtotalsonrows");
		String columnsTotals =  config.optString("calculatesubtotalsoncolumns");

		if(measuresOnRow){
			if(rowsTotals!=null && rowsTotals.equals("on")){
				if(!measuresOnRow){
					addSubtotalsToTheNodeFirstLevel(columnsRoot, true, 0);
					addSubtotalsToTheTree(columnsRoot, true, 0);	
				}else{
					int startPosition = 0;
					for(int i=0; i<columnsRoot.getChilds().size();i++){
						startPosition = addSubtotalsToTheTreeNoMeasure(columnsRoot.getChilds().get(i), true, startPosition);
					}
				}
			}
			
			if(columnsTotals!=null && columnsTotals.equals("on")){
				if(measuresOnRow){
					addSubtotalsToTheNodeFirstLevel(rowsRoot, false, 0);
					addSubtotalsToTheTree(rowsRoot, false, 0);	
				}else{
					int startPosition = 0;
					for(int i=0; i<rowsRoot.getChilds().size();i++){
						startPosition = addSubtotalsToTheTreeNoMeasure(rowsRoot.getChilds().get(i), false, startPosition);
					}
				}
			}
		}else{
		
			if(columnsTotals!=null && columnsTotals.equals("on")){
				if(measuresOnRow){
					addSubtotalsToTheNodeFirstLevel(rowsRoot, false, 0);
					addSubtotalsToTheTree(rowsRoot, false, 0);	
				}else{
					int startPosition = 0;
					for(int i=0; i<rowsRoot.getChilds().size();i++){
						startPosition = addSubtotalsToTheTreeNoMeasure(rowsRoot.getChilds().get(i), false, startPosition);
					}
				}
			}
			if(rowsTotals!=null && rowsTotals.equals("on")){
				if(!measuresOnRow){
					addSubtotalsToTheNodeFirstLevel(columnsRoot, true, 0);
					addSubtotalsToTheTree(columnsRoot, true, 0);	
				}else{
					int startPosition = 0;
					for(int i=0; i<columnsRoot.getChilds().size();i++){
						startPosition = addSubtotalsToTheTreeNoMeasure(columnsRoot.getChilds().get(i), true, startPosition);
					}
				}
			}
		}
		

		
		
//		addSubtotalsToTheNodeFirstLevel(columnsRoot, true, 0);
//		addSubtotalsToTheTree(columnsRoot, true, 0);
//		if(rowsTotals!=null && rowsTotals.equals("on")){
//			List<Node> childOfRoot = columnsRoot.getChilds();
//			for(int i=0; i<childOfRoot.size(); i++){
//				addSubtotalsToTheNodeFirstLevel(childOfRoot.get(i), true, 2, measuresOnRow);
//			}
//		}
		
//		if(columnsTotals!=null && columnsTotals.equals("on")){
//			List<Node> childOfRoot = rowsRoot.getChilds();
//			for(int i=0; i<childOfRoot.size(); i++){
//				addSubtotalsToTheNodeFirstLevel(childOfRoot.get(i), false, 2, measuresOnRow);
//			}
//		}
	}

//	/**
//	 * Prepare and execute a CF for the subtotals
//	 * @param n
//	 * @param horizontal
//	 * @param level
//	 * @param measuresOnRow
//	 */
//	public void addSubtotalsToTheNode(Node n, boolean horizontal, int level, boolean measuresOnRow){
//		List<Node> childs = n.getChilds();
//		if(measuresOnRow){
//			if((!horizontal && level<2) ||  //if level == 1 the subtotals are equals to the total
//				(childs.size()>0 && childs.get(0).getChilds().size()==0 ) ){ //don't calculate the subtotals between neasures
//				return;
//			}
//		}else{
//			if(horizontal && level<2 || (childs.size()>0 && childs.get(0).getChilds().size()==0 )){
//				return;
//			}
//		}
//		if(n.getCellType()!=CellType.CF && childs.size()>0 && n.getValue()!=TOTAL && n.getValue()!=SUBTOTAL ){
//			//build the calcuated field for the sum
//			StringBuilder sb = new StringBuilder(" ");
//			for(int i=0; i<childs.size(); i++){
//				if(childs.get(i).getCellType()!=CellType.CF && childs.get(i).getValue()!=TOTAL && childs.get(i).getValue()!=SUBTOTAL ){
//					sb.append("field[");
//					sb.append(childs.get(i).getValue());
//					sb.append("] +");
//				}
//			}
//			sb.delete(sb.length()-1, sb.length());		
//			calculateCF(sb.toString(), n, horizontal, level, SUBTOTAL, CellType.SUBTOTAL);
//			for(int i=0; i<childs.size(); i++){
//				addSubtotalsToTheNode(childs.get(i), horizontal, level-1, measuresOnRow);
//			}
//		}
//	}
	
	
	
//	/**
//	 * Prepare and execute a CF for the subtotals
//	 * @param n
//	 * @param horizontal
//	 * @param level
//	 * @param measuresOnRow
//	 */
//	public void addSubtotalsToTheNodeFirstLevel(Node n, boolean horizontal, int level, boolean measuresOnRow){
//		List<Node> childs = n.getChilds();
//		
//		
//		
//		if(n.getCellType()!=CellType.CF && childs.size()>0 && n.getValue()!=TOTAL && n.getValue()!=SUBTOTAL ){
//			//build the calcuated field for the sum
//			StringBuilder sb = new StringBuilder(" ");
//			for(int i=0; i<childs.size(); i++){
//				if(childs.get(i).getCellType()!=CellType.CF && childs.get(i).getValue()!=TOTAL && childs.get(i).getValue()!=SUBTOTAL ){
//					sb.append("field[");
//					sb.append(childs.get(i).getValue());
//					sb.append("] +");
//				}
//			}
//			sb.delete(sb.length()-1, sb.length());		
//			calculateCF(sb.toString(), n, horizontal, level, SUBTOTAL, CellType.SUBTOTAL);
//		}
//	}
	
	public int addSubtotalsToTheTreeNoMeasure(Node node, boolean horizontal, int startingPosition){
		int start = startingPosition;
		int length = node.getLeafsNumber();
		String[] total;
		List<Node> children = node.getChilds();
		if(children.size()>0){
			int freshStartingPosition = startingPosition;
			
			if(horizontal){
				total = getTotalsOfColumns(start, length);
			}else{
				total = getTotalsOfRows(start, length);
			}
				
			List<String[]> linesums = new ArrayList<String[]>();
			linesums.add(total);
			addCrosstabDataLine(freshStartingPosition+node.getLeafsNumber(), linesums, horizontal, CellType.SUBTOTAL);
					
			for(int i=0; i<children.size(); i++){
				freshStartingPosition = addSubtotalsToTheTreeNoMeasure(children.get(i), horizontal, freshStartingPosition);
			}
			
			Node totalNode = buildSubtotalNode(node.getSubTreeDepth()-1, false);
			node.addChild(totalNode);
			return startingPosition+node.getLeafsNumber();
		}
		return startingPosition+1;
		

	}

	public void addSubtotalsToTheTree(Node node, boolean horizontal, int startingPosition){
		if(node.getSubTreeDepth()<=4){
			return;
		}else{
			for(int i=0; i<node.getChilds().size(); i++){
				addSubtotalsToTheTree(node.getChilds().get(i), horizontal, startingPosition);
				startingPosition = addSubtotalsToTheNodeUpLevel(node.getChilds().get(i), horizontal, startingPosition);
			}
		}
	}
	
	public int addSubtotalsToTheNodeUpLevel(Node node, boolean horizontal, int startingPosition){
		List<Node> children = node.getChilds();
		List<List<Integer>> valuesTosum = new ArrayList<List<Integer>>(); 
		List<String[]> linesums = new ArrayList<String[]>();
		
		for(int y=0; y<measures.size(); y++){
			valuesTosum.add(new ArrayList<Integer>());
		}
		
		for(int i=0; i<children.size(); i++){
			startingPosition = startingPosition+children.get(i).getLeafsNumber();
			for(int y=0; y<measures.size(); y++){
				List<Integer> indexformeasuery = valuesTosum.get(y);
				indexformeasuery.add(startingPosition-measures.size()+y);
			}
		}
		
		Node subtotalNode = buildSubtotalNode(node.getSubTreeDepth()-2, true);
		node.addChild(subtotalNode);
		
		for(int y=0; y<valuesTosum.size(); y++){
			List<Integer> linesToSum = valuesTosum.get(y);
			linesums.add(getTotals(linesToSum, horizontal));
		}
		
		addCrosstabDataLine(startingPosition, linesums, horizontal, CellType.SUBTOTAL);
		
		return startingPosition+measures.size();
	}
	
	public int addSubtotalsToTheNodeFirstLevel(Node node, boolean horizontal, int positionToAddNode){
		Node n = node;
		List<String[]> linesums = new ArrayList<String[]>();
		if(n.getChilds().size()>0 && //has children
			  n.getChilds().get(0).getChilds().size()>0 && //has granchildren 
			  n.getChilds().get(0).getChilds().get(0).getChilds().size()>0){ //the granchildren are not leaf 
			for(int i=0; i<n.getChilds().size(); i++){
				positionToAddNode = addSubtotalsToTheNodeFirstLevel(n.getChilds().get(i),horizontal, positionToAddNode);
			}
		}else{
			Node subtotalNode = buildSubtotalNode(1, true);
			for(int y=0; y<measures.size(); y++){
				List<Integer> linesToSum = new ArrayList<Integer>();
				for(int k=0; k<n.getChilds().size(); k++){
					linesToSum.add(positionToAddNode+measures.size()*k+y);
				}
				linesums.add(getTotals(linesToSum, horizontal));
			}
			positionToAddNode = positionToAddNode+measures.size()*n.getChilds().size();
			node.addChild(subtotalNode);
			addCrosstabDataLine(positionToAddNode, linesums, horizontal, CellType.SUBTOTAL);
			positionToAddNode = positionToAddNode+linesums.size();
		}
		return positionToAddNode;
		
	}
	
	
	public Node buildSubtotalNode(int totalHeadersNumber, boolean withMeasures){
		Node node = new Node(SUBTOTAL);
		Node toReturn;
		int i=1;
		if(withMeasures){
			for(int j=0; j<measures.size(); j++){
				node.addChild(new Node(measures.get(j).getName()));
			}
		}
		toReturn = node;
		for(; i<totalHeadersNumber;i++){
			toReturn = new Node(SUBTOTAL);
			toReturn.addChild(node);
			node = toReturn;
		}	
		
		return toReturn;
		
	}
	
	private String[] getTotals(List<Integer> lines, boolean horizontal){
		double sum[];
		if(!horizontal){
			sum = new double[dataMatrix[0].length];
			for(int i=0; i<dataMatrix[0].length; i++){
				String value = dataMatrix[lines.get(0)][i];
				if(!value.equals(DATA_MATRIX_NA) && (getCellType(lines.get(0), i).equals(CellType.DATA))){
					sum[i] = new Double(value);
				}
			}
			for(int j=1; j<lines.size(); j++){
				for(int i=0; i<dataMatrix[0].length; i++){
					String value = dataMatrix[lines.get(j)][i];
					if(!value.equals(DATA_MATRIX_NA)&& (getCellType(lines.get(j), i).equals(CellType.DATA))){
						sum[i] = sum[i] + new Double(value);
					}
				}
			}
		}else{
			sum = new double[dataMatrix.length];
			for(int i=0; i<dataMatrix.length; i++){
				String value = dataMatrix[i][lines.get(0)];
				if(!value.equals(DATA_MATRIX_NA)&& (getCellType(i, lines.get(0)).equals(CellType.DATA))){
					sum[i] = new Double(value);
				}
			}
			for(int j=1; j<lines.size(); j++){
				for(int i=0; i<dataMatrix.length; i++){
					String value = dataMatrix[i][lines.get(j)];
					if(!value.equals(DATA_MATRIX_NA)&& (getCellType(i, lines.get(j)).equals(CellType.DATA))){
						sum[i] = sum[i] + new Double(value);
					}
					
				}
			}
		}

		return toStringArray(sum);
	}


	
	/********************************************************
	                 UTILITY METHODS
	************************************************************/
	
	/**
	 * Returns a column of the data matrix
	 * @param i the id of the column to get
	 * @return the i-th column of the data matrix
	 */
	private String[] getCrosstabDataColumn(int i){
		String[] column = new String[dataMatrix.length];
		for (int j = 0; j < dataMatrix.length; j++) {
			column[j] = dataMatrix[j][i];
		}
		return column;
	}
	
	/**
	 * Returns a row of the data matrix
	 * @param i the id of the row to get
	 * @return the i-th row of the data matrix
	 */
	private String[] getCrosstabDataRow(int i){
		return dataMatrix[i];
	}
	
	/**
	 * Inserts lines in the crosstab data matrix
	 * @param startposition the position where insert the rows/columns into the matrix
	 * @param line the lines to insert
	 * @param horizontal true to insert columns/false to insert rows
	 * @param type the type of the data
	 */
	public void addCrosstabDataLine(int startposition, List<String[]> line, boolean horizontal, CellType type){
		if(horizontal){
			addCrosstabDataColumns(startposition, line, type);
		}else{
			addCrosstabDataRow(startposition, line, type);
		}
	}
	
	/**
	 * Inserts columns in the crosstab data matrix
	 * @param startposition the position where insert the columns into the matrix
	 * @param colums the lines to insert
	 * @param type the type of the data
	 */
	public void addCrosstabDataColumns(int startposition, List<String[]> colums, CellType type){
		Assert.assertNotNull(dataMatrix, "The data matrix must not be null");
		Assert.assertTrue(startposition<=dataMatrix[0].length, "The position you want to add the columns is bigger than the table size ts="+dataMatrix[0].length+" position= "+startposition);
		String[][] newData = new String[dataMatrix.length][dataMatrix[0].length+colums.size()];
		int columnsToAddSize = colums.size();
		for (int i = 0; i < dataMatrix.length; i++) {
			for(int x=0; x<startposition; x++){
				newData[i][x] =dataMatrix[i][x]; 
			}
			
			for(int x=0; x<columnsToAddSize; x++){
				newData[i][startposition+x] =colums.get(x)[i]; 
			}
			
			for(int x=0; x<dataMatrix[0].length-startposition; x++){
				newData[i][startposition+columnsToAddSize+x] =dataMatrix[i][startposition+x]; 
			}
		}
		//update the list of columns type
		for(int i=0; i< colums.size(); i++){
			celltypeOfColumns.add(i+startposition, type);
		}
		
		dataMatrix = newData;
		
		if(type.equals(CellType.SUBTOTAL)){
			if(!measuresOnRow){
				for(int j=0; j<columnsSum.size(); j++){
					String[] aColumnsSum = new String[columnsSum.get(j).length+colums.size()];
					for(int y=0; y<startposition; y++){
						aColumnsSum[y]= columnsSum.get(j)[y];
					}
					
					for(int y=0; y<colums.size(); y++){
						aColumnsSum[startposition+y]= ""+getTotalsOfColumn(startposition+y, CellType.SUBTOTAL);
					}
					
					for(int y=startposition; y<columnsSum.get(j).length; y++){
						aColumnsSum[y+colums.size()]= columnsSum.get(j)[y];
					}
					columnsSum.set(j, aColumnsSum);
				}	
			}else{
				//colums.size() is 1
				String[] subtotal = getTotalsOfColumnWithMeasure(startposition);
				for(int j=0; j<columnsSum.size(); j++){
					
					String[] aColumnsSum = new String[columnsSum.get(j).length+1];
					for(int y=0; y<startposition; y++){
						aColumnsSum[y]= columnsSum.get(j)[y];
					}
				
					aColumnsSum[startposition]= subtotal[j];
					
					for(int y=startposition; y<columnsSum.get(j).length; y++){
						aColumnsSum[y+1]= columnsSum.get(j)[y];
					}
					columnsSum.set(j, aColumnsSum);
				}	
			}
			
		}
		
	}
	
	/**
	 * Inserts rows in the crosstab data matrix
	 * @param startposition the position where insert the rows into the matrix
	 * @param colums the lines to insert
	 * @param type the type of the data
	 */
	public void addCrosstabDataRow(int startposition, List<String[]> rows, CellType type){
		Assert.assertNotNull(dataMatrix, "The data matrix must not be null");
		Assert.assertTrue(startposition<=dataMatrix.length, "The position you want to add the rows is bigger than the table size ts="+dataMatrix[0].length+" position= "+startposition);
		
		String[][] newData = new String[dataMatrix.length+rows.size()][];
		int rowsToAddSize = rows.size();
		
		for(int x=0; x<startposition; x++){
			newData[x] =dataMatrix[x]; 
		}
			
		for(int x=0; x<rowsToAddSize; x++){
			newData[startposition+x] =rows.get(x); 
		}
		
		for(int x=0; x<dataMatrix.length-startposition; x++){
			newData[startposition+rowsToAddSize+x] =dataMatrix[startposition+x]; 
		}
		//update the list of rows type
		for(int i=0; i< rows.size(); i++){
			celltypeOfRows.add(i+startposition, type);
		}
		
		dataMatrix = newData;
		
		if(type.equals(CellType.SUBTOTAL)){
			if(measuresOnRow){
				for(int j=0; j<rowsSum.size(); j++){
					String[] aRowsSum = new String[rowsSum.get(j).length+rows.size()];
					for(int y=0; y<startposition; y++){
						aRowsSum[y]= rowsSum.get(j)[y];
					}
					
					for(int y=0; y<rows.size(); y++){
						aRowsSum[startposition+y]= ""+getTotalsOfRow(startposition+y, CellType.SUBTOTAL);
					}
					
					for(int y=startposition; y<rowsSum.get(j).length; y++){
						aRowsSum[y+rows.size()]= rowsSum.get(j)[y];
					}
					rowsSum.set(j, aRowsSum);
				}	
			}else{
				//colums.size() is 1
				String[] subtotal = getTotalsOfRowWithMeasure(startposition);
				for(int j=0; j<rowsSum.size(); j++){
					String[] aRowsSum = new String[rowsSum.get(j).length+rows.size()];
					for(int y=0; y<startposition; y++){
						aRowsSum[y]= rowsSum.get(j)[y];
					}
					
					aRowsSum[startposition]= subtotal[j];
					
					for(int y=startposition; y<rowsSum.get(j).length; y++){
						aRowsSum[y+rows.size()]= rowsSum.get(j)[y];
					}
					rowsSum.set(j, aRowsSum);
				}	
			}
		}
	}

	/**
	 * Get the CellType of the cell
	 * @param row the row
	 * @param column the column
	 * @return the celltype of the cell
	 */
	public CellType getCellType(int row, int column){
		CellType cellCellType;
		CellType rowCellType = celltypeOfRows.get(row);
		CellType columnCellType = celltypeOfColumns.get(column);
		cellCellType = rowCellType;
		if(columnCellType.compareTo(rowCellType)>0){
			cellCellType =  columnCellType;
		}
		return cellCellType;
	}
	
	public Node getColumnsRoot() {
		return columnsRoot;
	}

	public Node getRowsRoot() {
		return rowsRoot;
	}

	public String[][] getDataMatrix() {
		return dataMatrix;
	}
	
	private void addMeasuresScaleFactor(
			WorksheetEngineInstance engineInstance) {
		if (engineInstance != null) {
			
			WorkSheetDefinition definition = engineInstance.getTemplate().getWorkSheetDefinition();
			WorksheetFieldsOptions options = definition.getFieldsOptions();
			
			for (int i = 0; i < measures.size(); i++) {
				MeasureInfo measure = measures.get(i);
				FieldOptions aFieldOptions = options.getOptionsForFieldByFieldId(measure.getId());
				if (aFieldOptions != null) {
					List<FieldOption> opts = aFieldOptions.getOptions();
					for (int j = 0; j < opts.size(); j++) {
						FieldOption anOption = opts.get(j);
						if (anOption.getName().equals(WorkSheetSerializationUtils.WORKSHEETS_ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR)) {
							String value = anOption.getValue().toString();
							measure.setScaleFactor(value);
						}
					}
				}
			}
		}

	}

	public List<String> getRowHeadersTitles() {
		if(rowHeadersTitles==null){
			rowHeadersTitles = new ArrayList<String>();
			List<CrosstabDefinition.Row> rows =  crosstabDefinition.getRows();
			for(int i=0; i<rows.size(); i++){
				rowHeadersTitles.add(rows.get(i).getAlias());
			}
		}
		return rowHeadersTitles;
	}

	public List<MeasureInfo> getMeasures() {
		return measures;
	}
	
	public boolean isMeasureOnRow(){
		try {
			return config.getString("measureson").equals("rows");
		} catch (Exception e) {
			logger.error("Error reading the configuration of the crosstab", e);
			throw new SpagoBIRuntimeException("Error reading the configuration of the crosstab", e);
		}
	}

	public String getHTMLCrossTab(Locale locale) {
		CrossTabHTMLSerializer serializer = new CrossTabHTMLSerializer(locale);
		String html = serializer.serialize(this);
		return html;
	}
	
	public CrosstabDefinition getCrosstabDefinition() {
		return crosstabDefinition;
	}
		
}
