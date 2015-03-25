/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.dataset.crosstab;


import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Alberto Ghedin
 * @author Marco Cortella (marco.cortella@eng.it)
 * This Class encapsulates the crossTab
 * The publics methods are:
 * - CrossTab(IDataStore dataStore, CrosstabDefinition crosstabDefinition) that builds
 *   the crossTab (headers structure and data)
 * - getJSONCrossTab() that returns the JSON representation of the crosstab
 */
public class CrossTab {
	
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
	
	private static final String PATH_SEPARATOR = "_S_";
	private static final String DATA_MATRIX_NA = "NA";

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "dd/MM/yyyy" );
	private static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
	
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
	
	public static final String MEASURE_NAME = "name";
	public static final String MEASURE_TYPE = "type";
	public static final String MEASURE_FORMAT = "format";
	public static final String MEASURE_POSITION = "measurePosition";

	
	private static Logger logger = Logger.getLogger(CrossTab.class);

	
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
	
	
	//**********************************************************************
	// Utility methods
	//**********************************************************************
	
	private JSONArray serializeCellType(List<CellType> celltypes){
		JSONArray types = new JSONArray();
		for(int i=0; i<celltypes.size(); i++){
			types.put(celltypes.get(i).getValue());
		}
		return types;
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
	
	
	private <T extends Attribute> JSONArray getHeaderDescriptions(List<T> lines){
		JSONArray descriptions = new JSONArray();
		for (int i = 0; i < lines.size(); i++) {
			descriptions.put(lines.get(i).getAlias());
		}
		return descriptions;
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
	
	
	
	//********************************************
	// Static internal classes
	//********************************************
	
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
}
