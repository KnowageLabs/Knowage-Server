/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
 Ext.ns("Sbi.constants");
 
Sbi.constants.qbe = {
		
	// select clause field types (twins java constants are defined in class ISelectField)
	FIELD_TYPE_SIMPLE: 'datamartField'
	, FIELD_TYPE_CALCULATED: 'calculated.field'
	, FIELD_TYPE_INLINE_CALCULATED: 'inline.calculated.field'
			
	// where clause operand types (twins java constants are defined in class AbstractStatement)
	, OPERAND_TYPE_STATIC_VALUE: 'Static Content'
	, OPERAND_TYPE_SUBQUERY: 'Subquery'
	, OPERAND_TYPE_PARENT_FIELD: 'Parent Field Content'
	, OPERAND_TYPE_SIMPLE_FIELD: 'Field Content'
	, OPERAND_TYPE_CALCULATED_FIELD: 'calculated.field'
	, OPERAND_TYPE_INLINE_CALCULATED_FIELD: 'inline.calculated.field'
				
	// tree field types (twins java constants are defined in class ExtJsQbeTreeBuilder)
	, NODE_TYPE_ENTITY: 'entity'
	, NODE_TYPE_SIMPLE_FIELD: 'field'
	, NODE_TYPE_RELATION: 'relation'
	, NODE_TYPE_CALCULATED_FIELD: 'calculatedField'
	, NODE_TYPE_INLINE_CALCULATED_FIELD: 'inLineCalculatedField'
	, NODE_TYPE_HIERARCHY_FIELD: 'hierarchyField'
	, NODE_TYPE_HIERARCHY_LEVEL_FIELD : 'hierarchyLevelField'
		
	, WHERE_CLAUSE_COMPARISON_FUNCTIONS: [
	    ['NONE', LN('sbi.qbe.filtergridpanel.foperators.name.none'), LN('sbi.qbe.filtergridpanel.foperators.desc.none')],
	    ['EQUALS TO', LN('sbi.qbe.filtergridpanel.foperators.name.eq'),  LN('sbi.qbe.filtergridpanel.foperators.desc.eq')],
	    ['NOT EQUALS TO', LN('sbi.qbe.filtergridpanel.foperators.name.noteq'),  LN('sbi.qbe.filtergridpanel.foperators.desc.noteq')],
	    ['GREATER THAN', LN('sbi.qbe.filtergridpanel.foperators.name.gt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.gt')],
	    ['EQUALS OR GREATER THAN', LN('sbi.qbe.filtergridpanel.foperators.name.eqgt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.eqgt')],
	    ['LESS THAN', LN('sbi.qbe.filtergridpanel.foperators.name.lt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.lt')],
	    ['EQUALS OR LESS THAN', LN('sbi.qbe.filtergridpanel.foperators.name.eqlt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.eqlt')],
	    ['STARTS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.starts'),  LN('sbi.qbe.filtergridpanel.foperators.desc.starts')],
	    ['NOT STARTS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.notstarts'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notstarts')],
	    ['ENDS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.ends'),  LN('sbi.qbe.filtergridpanel.foperators.desc.ends')],
	    ['NOT ENDS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.notends'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notends')],
	    ['CONTAINS', LN('sbi.qbe.filtergridpanel.foperators.name.contains'),  LN('sbi.qbe.filtergridpanel.foperators.desc.contains')],
	    ['NOT CONTAINS', LN('sbi.qbe.filtergridpanel.foperators.name.notcontains'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notcontains')],
	    ['BETWEEN', LN('sbi.qbe.filtergridpanel.foperators.name.between'),  LN('sbi.qbe.filtergridpanel.foperators.desc.between')],
	    ['NOT BETWEEN', LN('sbi.qbe.filtergridpanel.foperators.name.notbetween'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notbetween')],
	    ['IN', LN('sbi.qbe.filtergridpanel.foperators.name.in'),  LN('sbi.qbe.filtergridpanel.foperators.desc.in')],
	    ['NOT IN', LN('sbi.qbe.filtergridpanel.foperators.name.notin'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notin')],
	    ['NOT NULL', LN('sbi.qbe.filtergridpanel.foperators.name.notnull'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notnull')],
	    ['IS NULL', LN('sbi.qbe.filtergridpanel.foperators.name.isnull'),  LN('sbi.qbe.filtergridpanel.foperators.desc.isnull')],
	    /* spatial operators (actually the last field is used to filter 'geometry' type operators */
	    ['SPATIAL_CONTAINS', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.contains'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.contains'), 'geometry']
	    ,['SPATIAL_COVERED_BY', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.coveredby'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.coveredby'), 'geometry']
	    ,['SPATIAL_COVERS', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.covers'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.covers'), 'geometry']
	    ,['SPATIAL_DISJOINT', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.disjoint'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.disjoint'), 'geometry']
	    ,['SPATIAL_EQUALS_TO', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.equals'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.equals'), 'geometry']
	    ,['SPATIAL_FILTER', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.filter'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.filter'), 'geometry']
	    ,['SPATIAL_INTERSECTS', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.intersects'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.intersects'), 'geometry']
	    ,['SPATIAL_OVERLAPS', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.overlaps'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.overlaps'), 'geometry']
	    ,['SPATIAL_TOUCHES', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.touches'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.touches'), 'geometry']
	    ,['SPATIAL_INSIDE', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.inside'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.inside'), 'geometry']
	    /* these operators are not used */
//	    ,['SPATIAL_NOT_CONTAINS', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.notcontains'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.notcontains'), 'geometry']
//	    ,['SPATIAL_NOT_COVERED_BY', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.notcoveredby'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.notcoveredby'), 'geometry']
//	    ,['SPATIAL_NOT_COVERS', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.notcovers'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.notcovers'), 'geometry']
//	    ,['SPATIAL_NOT_EQUALS_TO', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.notequals'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.notequals'), 'geometry']
//	    ,['SPATIAL_NOT_OVERLAPS', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.notoverlaps'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.notoverlaps'), 'geometry']
//	    ,['SPATIAL_NOT_TOUCHES', LN('sbi.qbe.filtergridpanel.foperators.spatial.name.nottouches'), LN('sbi.qbe.filtergridpanel.foperators.spatial.desc.nottouches'), 'geometry']
	]

	, HAVING_CLAUSE_COMPARISON_FUNCTIONS : [
        ['NONE', LN('sbi.qbe.filtergridpanel.foperators.name.none'), LN('sbi.qbe.filtergridpanel.foperators.desc.none')],
        ['EQUALS TO', LN('sbi.qbe.filtergridpanel.foperators.name.eq'),  LN('sbi.qbe.filtergridpanel.foperators.desc.eq')],
        ['NOT EQUALS TO', LN('sbi.qbe.filtergridpanel.foperators.name.noteq'),  LN('sbi.qbe.filtergridpanel.foperators.desc.noteq')],
        ['GREATER THAN', LN('sbi.qbe.filtergridpanel.foperators.name.gt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.gt')],
        ['EQUALS OR GREATER THAN', LN('sbi.qbe.filtergridpanel.foperators.name.eqgt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.eqgt')],
        ['LESS THAN', LN('sbi.qbe.filtergridpanel.foperators.name.lt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.lt')],
        ['EQUALS OR LESS THAN', LN('sbi.qbe.filtergridpanel.foperators.name.eqlt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.eqlt')],
        ['STARTS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.starts'),  LN('sbi.qbe.filtergridpanel.foperators.desc.starts')],
        ['NOT STARTS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.notstarts'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notstarts')],
        ['ENDS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.ends'),  LN('sbi.qbe.filtergridpanel.foperators.desc.ends')],
        ['NOT ENDS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.notends'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notends')],
        ['CONTAINS', LN('sbi.qbe.filtergridpanel.foperators.name.contains'),  LN('sbi.qbe.filtergridpanel.foperators.desc.contains')],
        ['NOT CONTAINS', LN('sbi.qbe.filtergridpanel.foperators.name.notcontains'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notcontains')],        
        ['BETWEEN', LN('sbi.qbe.filtergridpanel.foperators.name.between'),  LN('sbi.qbe.filtergridpanel.foperators.desc.between')],
        ['NOT BETWEEN', LN('sbi.qbe.filtergridpanel.foperators.name.notbetween'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notbetween')],
        ['IN', LN('sbi.qbe.filtergridpanel.foperators.name.in'),  LN('sbi.qbe.filtergridpanel.foperators.desc.in')],
        ['NOT IN', LN('sbi.qbe.filtergridpanel.foperators.name.notin'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notin')],        
        ['NOT NULL', LN('sbi.qbe.filtergridpanel.foperators.name.notnull'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notnull')],
        ['IS NULL', LN('sbi.qbe.filtergridpanel.foperators.name.isnull'),  LN('sbi.qbe.filtergridpanel.foperators.desc.isnull')]
    ]
	
	, HAVING_CLAUSE_AGGREGATION_FUNCTION: [
        ['NONE', LN('sbi.qbe.selectgridpanel.aggfunc.name.none'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.none')],
        ['SUM', LN('sbi.qbe.selectgridpanel.aggfunc.name.sum'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.sum')],
        ['AVG', LN('sbi.qbe.selectgridpanel.aggfunc.name.avg'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.avg')],
        ['MAX', LN('sbi.qbe.selectgridpanel.aggfunc.name.max'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.max')],
        ['MIN', LN('sbi.qbe.selectgridpanel.aggfunc.name.min'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.min')],
        ['COUNT', LN('sbi.qbe.selectgridpanel.aggfunc.name.count'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.count')],
        ['COUNT_DISTINCT', LN('sbi.qbe.selectgridpanel.aggfunc.name.countdistinct'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.countdistinct')]
     ] 

	, SELECT_CLAUSE_AGGREGATION_FUNCTION : [
        ['NONE', LN('sbi.qbe.selectgridpanel.aggfunc.name.none'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.none')],
        ['SUM', LN('sbi.qbe.selectgridpanel.aggfunc.name.sum'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.sum')],
        ['AVG', LN('sbi.qbe.selectgridpanel.aggfunc.name.avg'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.avg')],
        ['MAX', LN('sbi.qbe.selectgridpanel.aggfunc.name.max'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.max')],
        ['MIN', LN('sbi.qbe.selectgridpanel.aggfunc.name.min'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.min')],
        ['COUNT', LN('sbi.qbe.selectgridpanel.aggfunc.name.count'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.count')],
        ['COUNT_DISTINCT', LN('sbi.qbe.selectgridpanel.aggfunc.name.countdistinct'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.countdistinct')]
    ] 

	, SPATIAL_FUNCTIONS : [
	    {
	    	text: 'length'
	    	, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.length')
            , type: 'function'
            , value: 'length(op1, op2, \'unit=op3\')'
            , alias: 'length(op1, op2, \'unit=op3\')'
            , operands: [{label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOpLength')}]
	    	, freeOperands: [
	    	  {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOpTolerance')}
	    	 ,{label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelUnit')}]
	    }, {
	    	text: 'difference'
	    	, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.diff')
            , type: 'function'
            , value: 'difference(op1, op2, op3)'
            , alias: 'difference(op1, op2, op3)'
            , operands: [
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp1Diff')},
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp2Diff')}
            ]
	    	, freeOperands: [{label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOpTolerance')}]
	    }, {
	    	text: 'distance'
	    	, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.dist')
    		, type: 'function'
            , value: 'distance(op1, op2, op3, \'unit=op4\')'
            , alias: 'distance(op1, op2, op3, \'unit=op4\')'
            , operands: [
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp1Dist')}, 
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp2Dist')}
            ]
	    	, freeOperands: [
	    	  {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOpTolerance')}
	    	 ,{label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelUnit')}]
	    }, {
	    	text: 'dwithin'
	    	, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.distw')
    		, type: 'function'
            , value: 'dwithin(op1, op2, \'distance=op3,unit=op4\')'
            , alias: 'dwithin(op1, op2, \'distance=op3,unit=op4\')'
            , operands: [
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp1Distw')}, 
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp2Distw')}
            ]
	    	, freeOperands: [
	    	  {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp3Distw')}
	    	 ,{label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelUnit')}]
	    }, {
	    	text: 'relate'
    		, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.relate')
    		, type: 'function'
            , value: 'relate(op1, \'mask=op3\', op2, op4)'
            , alias: 'relate(op1, \'mask=op3\', op2, op4)'
            , operands: [
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp1Rel')},
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp2Rel')}
            ]
	    	, freeOperands: [
	    	  {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOpMask')},
	    	  {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOpTolerance')}
	    	]
	    }, {
	    	text: 'dimension'
    		, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.getDims')
    		, type: 'function'
            , value: 'dimension(op1)'
            , alias: 'dimension(op1)'
            , operands: [{label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOpDim')}]
	    }, {
	    	text: 'centroid'
    		, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.centroid')
    		, type: 'function'
            , value: 'centroid(op1, op2)'
            , alias: 'centroid(op1, op2)'
            , operands: [{label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOpCentroid')}]
	    	, freeOperands: [{label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOpTolerance')}]
	    }, {
	    	text: 'geometrytype'
    		, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.getType')
    		, type: 'function'
            , value: 'geometrytype(op1)'
            , alias: 'geometrytype(op1)'
            , operands: [{label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOpGeometryType')}]
	    }, {
	    	text: 'union'
    		, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.union')
    		, type: 'function'
            , value: 'geomunion(op1, op2)'
            , alias: 'geomunion(op1, op2)'
        	, operands: [
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp1Union')}, 
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp2Union')}
            ]
	    }, {
	    	text: 'latitude'
    		, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.latitude')
    		, type: 'function'
            , value: 'latitude(op1)'
            , alias: 'latitude(op1)'
        	, operands: [
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp1Latitude')}
            ]
	    }, {
	    	text: 'longitude'
    		, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.longitude')
    		, type: 'function'
            , value: 'longitude(op1)'
            , alias: 'longitude(op1)'
        	, operands: [
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp1Longitude')}
            ]
	    }, {
	    	text: 'toKM'
    		, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.toKM')
    		, type: 'function'
            , value: 'to_km(op1)'
            , alias: 'to_km(op1)'
        	, operands: [
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp1ToKM')}
            ]
	    }, {
	    	text: 'toNM'
    		, qtip: LN('sbi.qbe.selectgridpanel.spatial.desc.toNM')
    		, type: 'function'
            , value: 'to_nm(op1)'
            , alias: 'to_nm(op1)'
        	, operands: [
              {label: LN('sbi.qbe.selectgridpanel.spatial.desc.labelOp1ToNM')}
            ]
	    }
	]

	, ARITHMETIC_FUNCTIONS : [
        {
        	text: '+'
            , qtip: LN('sbi.qbe.selectgridpanel.func.sum.tip')
            , type: 'function'
            , value: Ext.util.Format.htmlEncode('+')
            , alias: Ext.util.Format.htmlEncode('+')
        }, {
            text: '-' 
            , qtip: LN('sbi.qbe.selectgridpanel.func.difference.tip')
            , type: 'function'
            , value: Ext.util.Format.htmlEncode('-')
            , alias: Ext.util.Format.htmlEncode('-')
        }, {
            text: '*'
            , qtip: LN('sbi.qbe.selectgridpanel.func.multiplication.tip')
            , type: 'function'
            , value: Ext.util.Format.htmlEncode('*')
            , alias: Ext.util.Format.htmlEncode('*')
         }, {
            text: '/'
            , qtip: LN('sbi.qbe.selectgridpanel.func.division.tip')
            , type: 'function'
            , value: Ext.util.Format.htmlEncode('/')
            , alias: Ext.util.Format.htmlEncode('/')
         }, {
            text: '||'
         	, qtip: LN('sbi.qbe.selectgridpanel.func.pipe.tip')
         	, type: 'function'
         	, value: Ext.util.Format.htmlEncode('||')
         	, alias: Ext.util.Format.htmlEncode('||')
         }, {
            text: '('
         	, qtip: LN('sbi.qbe.selectgridpanel.func.openpar.tip')
         	, type: 'function'
         	, value: Ext.util.Format.htmlEncode('(')
            , alias: Ext.util.Format.htmlEncode('(')
         }, {
            text: ')'
            , qtip: LN('sbi.qbe.selectgridpanel.func.closepar.tip')
            , type: 'function'
            , value: Ext.util.Format.htmlEncode(')')
            , alias: Ext.util.Format.htmlEncode(')')
         }
    ]

	, AGGREGATION_FUNCTIONS : [
       {
    	   text: 'SUM'
           , qtip: LN('sbi.qbe.selectgridpanel.aggfunc.desc.sum')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('SUM(op1)')
           , alias: Ext.util.Format.htmlEncode('SUM(op1)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpSum')}]
       }, {
    	   text: 'MIN'  
           , qtip: LN('sbi.qbe.selectgridpanel.aggfunc.desc.min')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('MIN(op1)')
           , alias: Ext.util.Format.htmlEncode('MIN(op1)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpMin')}]
       }, {
           text: 'MAX' 
           , qtip: LN('sbi.qbe.selectgridpanel.aggfunc.desc.max')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('MAX(op1)')
           , alias: Ext.util.Format.htmlEncode('MAX(op1)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpMax')}]
       }, {
           text: 'COUNT'
           , qtip: LN('sbi.qbe.selectgridpanel.aggfunc.desc.count')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('COUNT(op1)')
           , alias: Ext.util.Format.htmlEncode('COUNT(op1)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpCount')}]
       }, {
           text: 'COUNT_DISTINCT'
           , qtip: LN('sbi.qbe.selectgridpanel.aggfunc.desc.countdistinct')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('COUNT(DISTINCT op1)')
           , alias: Ext.util.Format.htmlEncode('COUNT(DISTINCT op1)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpCountDist')}]
       }, {
           text: 'AVG'
           , qtip: LN('sbi.qbe.selectgridpanel.aggfunc.desc.avg')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('AVG(op1)')
           , alias: Ext.util.Format.htmlEncode('AVG(op1)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpAVG')}]
       }
    ]

	, DATE_FUNCTIONS : [
       {
           text: 'GG_between_dates'
           , qtip: LN('sbi.qbe.selectgridpanel.datefunc.desc.ggbetweendates')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('GG_between_dates(op1,op2)')
           , alias: Ext.util.Format.htmlEncode('GG_between_dates(op1,op2)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpDate1')}
           , {label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpDate2')}]
       }, {
           text: 'MM_between_dates'
           , qtip: LN('sbi.qbe.selectgridpanel.datefunc.desc.mmbetweendates')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('MM_between_dates(op1,op2)')
           , alias: Ext.util.Format.htmlEncode('MM_between_dates(op1,op2)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpDate1')}
           , {label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpDate2')}]
       },{
    	   text: 'AA_between_dates'
           , qtip: LN('sbi.qbe.selectgridpanel.datefunc.desc.aabetweendates')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('AA_between_dates(op1,op2)')
           , alias: Ext.util.Format.htmlEncode('AA_between_dates(op1,op2)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpDate1')}
           , {label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpDate2')}]
       }, {
           text: 'GG_up_today'
           , qtip: LN('sbi.qbe.selectgridpanel.datefunc.desc.gguptoday')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('GG_up_today(op1)')
           , alias: Ext.util.Format.htmlEncode('GG_up_today(op1)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpDate')}]
        }, {
           text: 'MM_up_today'
           , qtip: LN('sbi.qbe.selectgridpanel.datefunc.desc.mmuptoday')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('MM_up_today(op1)')
           , alias: Ext.util.Format.htmlEncode('MM_up_today(op1)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpDate')}]
        }, {
           text: 'AA_up_today'
           , qtip: LN('sbi.qbe.selectgridpanel.datefunc.desc.aauptoday')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('AA_up_today(op1)')
           , alias: Ext.util.Format.htmlEncode('AA_up_today(op1)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpDate')}]
        }, {
           text: 'extract'
           , qtip: LN('sbi.qbe.selectgridpanel.datefunc.desc.extract')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('extract(op2, op1)')
           , alias: Ext.util.Format.htmlEncode('extract(op2, op1)')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.extract')}]
           , freeOperands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.extract.op2')}]
        }, {
           text: 'toLocalTime'
           , qtip: LN('sbi.qbe.selectgridpanel.datefunc.desc.toLocalTime')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('to_timezone(op1,'+((new Date()).getTimezoneOffset()/60*(-1))+')')
           , alias: Ext.util.Format.htmlEncode('to_timezone(op1,'+((new Date()).getTimezoneOffset()/60*(-1))+')')
           , operands: [{label: LN('sbi.qbe.selectgridpanel.aggfunc.desc.labelOpToLocalTime')}]
        }
    ]

	, SCRIPT_FUNCTIONS : [
	     {
	        text: 'link'
	        , qtip: 'create a link to external web page'
	        , type: 'function'
	        //, value: Ext.util.Format.htmlEncode('\'<a href="${URL}">\' + ${LABEL} + \'</a>\'')
	        //, alias: Ext.util.Format.htmlEncode('\'<a href="${URL}">\' + ${LABEL} + \'</a>\'')
	        , value: Ext.util.Format.htmlEncode('return api.getLink("${URL}", "${TEXT}");')
	        , alias: Ext.util.Format.htmlEncode('return api.getLink("${URL}", "${TEXT}");')
	     }, {
	    	 text: 'image'
	    	 , qtip: 'include an external image'
	    	 , type: 'function'
	         //, value: Ext.util.Format.htmlEncode('\'<img src="${URL}"></img>\'')
	         //, alias: Ext.util.Format.htmlEncode('\'<img src="${URL}"></img>\'')
	         , value: Ext.util.Format.htmlEncode('return api.getImageLink("${IMAGE_URL}");')
	         , alias: Ext.util.Format.htmlEncode('return api.getImageLink("${IMAGE_URL}");')
	     }, {
	    	 text: 'cross-navigation'
	    	 , qtip: 'create a cross navigation link'
	         , type: 'function'
	         , value: Ext.util.Format.htmlEncode('return api.getCrossNavigationLink("${TEXT}", "${TARGET_DOCUMENT_LABEL}", "${PARAMETERS}", "${SUBOBJECT");')
//	         Ext.util.Format.htmlEncode("String label = 'bestByRegion';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("String text= fields['salesRegion'];") + '<br>' + 
//	         Ext.util.Format.htmlEncode("String params = 'region=5';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("String subobject;") + '<p>' + 
//	         Ext.util.Format.htmlEncode("String result = '';") + '<p>' + 
//	         Ext.util.Format.htmlEncode("result +='<a href=\"#\" onclick=\"javascript:sendMessage({';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("result +='\\'label\\':\\'' + label + '\\'';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("result +=', parameters:\\'' + params + '\\'';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("result +=', windowName: this.name';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("if(subobject != null) result +=', subobject:\\'' + subobject +'\\'';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("result += '},\\'crossnavigation\\')\"';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("result += '>' + text + '</a>';") + '<p>' + 
//	         Ext.util.Format.htmlEncode("return result;")
	         , alias: Ext.util.Format.htmlEncode('return api.getCrossNavigationLink("${TEXT}", "${TARGET_DOCUMENT_LABEL}", "${PARAMETERS}", "${SUBOBJECT");')
//	         Ext.util.Format.htmlEncode("String label = 'bestByRegion';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("String text= fields['salesRegion'];") + '<br>' + 
//	         Ext.util.Format.htmlEncode("String params= 'region=5';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("String subobject;") + '<p>' + 
//	         Ext.util.Format.htmlEncode("String result = '';") + '<p>' + 
//	         Ext.util.Format.htmlEncode("result +='<a href=\"#\" onclick=\"javascript:sendMessage({';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("result +='\\'label\\':\\'' + label + '\\'';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("result +=', parameters:\\'' + params + '\\'';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("result +=', windowName: this.name';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("if(subobject != null) result +=', subobject:\\'' + subobject +'\\'';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("result += '},\\'crossnavigation\\')\"';") + '<br>' + 
//	         Ext.util.Format.htmlEncode("result += '>' + text + '</a>';") + '<p>' + 
//	         Ext.util.Format.htmlEncode("return result;")
	     }
	 ]
};

// spatial functions
Sbi.constants.qbe.CALCULATED_FIELD_EDITOR_SPATIAL_FUNCTIONS = Sbi.constants.qbe.SPATIAL_FUNCTIONS;
// arithmetic functions
Sbi.constants.qbe.CALCULATED_FIELD_EDITOR_ARITHMETIC_FUNCTIONS = Sbi.constants.qbe.ARITHMETIC_FUNCTIONS;
Sbi.constants.qbe.INLINE_CALCULATED_FIELD_EDITOR_ARITHMETIC_FUNCTIONS = Sbi.constants.qbe.ARITHMETIC_FUNCTIONS;
Sbi.constants.qbe.SLOTS_EDITOR_ARITHMETIC_FUNCTIONS = Sbi.constants.qbe.ARITHMETIC_FUNCTIONS;
//date functions
Sbi.constants.qbe.CALCULATED_FIELD_EDITOR_DATE_FUNCTIONS = Sbi.constants.qbe.DATE_FUNCTIONS; // ???
Sbi.constants.qbe.INLINE_CALCULATED_FIELD_EDITOR_DATE_FUNCTIONS = Sbi.constants.qbe.DATE_FUNCTIONS;
Sbi.constants.qbe.SLOTS_EDITOR_DATE_FUNCTIONS = Sbi.constants.qbe.DATE_FUNCTIONS;
// aggregation functions
Sbi.constants.qbe.INLINE_CALCULATED_FIELD_EDITOR_AGGREGATION_FUNCTIONS = Sbi.constants.qbe.AGGREGATION_FUNCTIONS;
Sbi.constants.qbe.SLOTS_EDITOR_AGGREGATION_FUNCTIONS = Sbi.constants.qbe.AGGREGATION_FUNCTIONS;
// script functions
Sbi.constants.qbe.CALCULATED_FIELD_EDITOR_SCRIPT_FUNCTIONS = Sbi.constants.qbe.SCRIPT_FUNCTIONS;

Sbi.constants.worksheet = {
		
};

Sbi.constants.formviewer = {
		
};