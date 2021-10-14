package it.eng.knowage.engine.cockpit.api.export.excel.crosstab;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellType;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.export.excel.Threshold;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;

/**
 * Exports the crosstab data (formatted as a JSON object in input) into a XLSX file.
 * The JSON object should have this structure (a node is {node_key:"Text", node_childs:[...]}):
 * 		columns: {...} contains tree node structure of the columns' headers
 * 		rows: {...} contains tree node structure of the rows' headers
 * 		data: [[...], [...], ...] 2-dimensional matrix containing crosstab data
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it), Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class CrosstabXLSXExporter extends CrosstabXLSExporter {

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(CrosstabXLSExporter.class);

	public CrosstabXLSXExporter(Properties properties, JSONObject variables) {
		super(properties, variables);
	}

	public CrosstabXLSXExporter(Properties properties, JSONObject variables, Map<String, List<Threshold>> thresholdColorsMap) {
		super(properties, variables, thresholdColorsMap);
	}

	@Override
	protected CellType getCellTypeNumeric() {
		return CellType.NUMERIC;
	}

	@Override
	protected CellType getCellTypeString() {
		return CellType.STRING;
	}

}