/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
Ext.ns("Sbi.commons");
 
Sbi.commons.Constants = {
		
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
		
	, WHERE_CLAUSE_COMPARISON_FUNCTIONS: [
	    ['NONE', LN('sbi.cockpit.filtergridpanel.foperators.name.none'), LN('sbi.cockpit.filtergridpanel.foperators.desc.none')],
	    ['EQUALS TO', LN('sbi.cockpit.filtergridpanel.foperators.name.eq'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.eq')],
	    ['NOT EQUALS TO', LN('sbi.cockpit.filtergridpanel.foperators.name.noteq'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.noteq')],
	    ['GREATER THAN', LN('sbi.cockpit.filtergridpanel.foperators.name.gt'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.gt')],
	    ['EQUALS OR GREATER THAN', LN('sbi.cockpit.filtergridpanel.foperators.name.eqgt'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.eqgt')],
	    ['LESS THAN', LN('sbi.cockpit.filtergridpanel.foperators.name.lt'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.lt')],
	    ['EQUALS OR LESS THAN', LN('sbi.cockpit.filtergridpanel.foperators.name.eqlt'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.eqlt')],
	    ['STARTS WITH', LN('sbi.cockpit.filtergridpanel.foperators.name.starts'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.starts')],
	    ['NOT STARTS WITH', LN('sbi.cockpit.filtergridpanel.foperators.name.notstarts'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notstarts')],
	    ['ENDS WITH', LN('sbi.cockpit.filtergridpanel.foperators.name.ends'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.ends')],
	    ['NOT ENDS WITH', LN('sbi.cockpit.filtergridpanel.foperators.name.notends'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notends')],
	    ['CONTAINS', LN('sbi.cockpit.filtergridpanel.foperators.name.contains'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.contains')],
	    ['NOT CONTAINS', LN('sbi.cockpit.filtergridpanel.foperators.name.notcontains'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notcontains')],
	    ['BETWEEN', LN('sbi.cockpit.filtergridpanel.foperators.name.between'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.between')],
	    ['NOT BETWEEN', LN('sbi.cockpit.filtergridpanel.foperators.name.notbetween'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notbetween')],
	    ['IN', LN('sbi.cockpit.filtergridpanel.foperators.name.in'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.in')],
	    ['NOT IN', LN('sbi.cockpit.filtergridpanel.foperators.name.notin'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notin')],
	    ['NOT NULL', LN('sbi.cockpit.filtergridpanel.foperators.name.notnull'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notnull')],
	    ['IS NULL', LN('sbi.cockpit.filtergridpanel.foperators.name.isnull'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.isnull')]
	]

	, HAVING_CLAUSE_COMPARISON_FUNCTIONS : [
        ['NONE', LN('sbi.cockpit.filtergridpanel.foperators.name.none'), LN('sbi.cockpit.filtergridpanel.foperators.desc.none')],
        ['EQUALS TO', LN('sbi.cockpit.filtergridpanel.foperators.name.eq'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.eq')],
        ['NOT EQUALS TO', LN('sbi.cockpit.filtergridpanel.foperators.name.noteq'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.noteq')],
        ['GREATER THAN', LN('sbi.cockpit.filtergridpanel.foperators.name.gt'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.gt')],
        ['EQUALS OR GREATER THAN', LN('sbi.cockpit.filtergridpanel.foperators.name.eqgt'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.eqgt')],
        ['LESS THAN', LN('sbi.cockpit.filtergridpanel.foperators.name.lt'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.lt')],
        ['EQUALS OR LESS THAN', LN('sbi.cockpit.filtergridpanel.foperators.name.eqlt'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.eqlt')],
        ['STARTS WITH', LN('sbi.cockpit.filtergridpanel.foperators.name.starts'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.starts')],
        ['NOT STARTS WITH', LN('sbi.cockpit.filtergridpanel.foperators.name.notstarts'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notstarts')],
        ['ENDS WITH', LN('sbi.cockpit.filtergridpanel.foperators.name.ends'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.ends')],
        ['NOT ENDS WITH', LN('sbi.cockpit.filtergridpanel.foperators.name.notends'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notends')],
        ['CONTAINS', LN('sbi.cockpit.filtergridpanel.foperators.name.contains'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.contains')],
        ['NOT CONTAINS', LN('sbi.cockpit.filtergridpanel.foperators.name.notcontains'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notcontains')],        
        ['BETWEEN', LN('sbi.cockpit.filtergridpanel.foperators.name.between'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.between')],
        ['NOT BETWEEN', LN('sbi.cockpit.filtergridpanel.foperators.name.notbetween'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notbetween')],
        ['IN', LN('sbi.cockpit.filtergridpanel.foperators.name.in'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.in')],
        ['NOT IN', LN('sbi.cockpit.filtergridpanel.foperators.name.notin'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notin')],        
        ['NOT NULL', LN('sbi.cockpit.filtergridpanel.foperators.name.notnull'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.notnull')],
        ['IS NULL', LN('sbi.cockpit.filtergridpanel.foperators.name.isnull'),  LN('sbi.cockpit.filtergridpanel.foperators.desc.isnull')]
    ]
	
	, HAVING_CLAUSE_AGGREGATION_FUNCTION: [
        ['NONE', LN('sbi.cockpit.selectgridpanel.aggfunc.name.none'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.none')],
        ['SUM', LN('sbi.cockpit.selectgridpanel.aggfunc.name.sum'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.sum')],
        ['AVG', LN('sbi.cockpit.selectgridpanel.aggfunc.name.avg'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.avg')],
        ['MAX', LN('sbi.cockpit.selectgridpanel.aggfunc.name.max'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.max')],
        ['MIN', LN('sbi.cockpit.selectgridpanel.aggfunc.name.min'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.min')],
        ['COUNT', LN('sbi.cockpit.selectgridpanel.aggfunc.name.count'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.count')],
        ['COUNT_DISTINCT', LN('sbi.cockpit.selectgridpanel.aggfunc.name.countdistinct'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.countdistinct')]
     ] 

	, SELECT_CLAUSE_AGGREGATION_FUNCTION : [
        ['NONE', LN('sbi.cockpit.selectgridpanel.aggfunc.name.none'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.none')],
        ['SUM', LN('sbi.cockpit.selectgridpanel.aggfunc.name.sum'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.sum')],
        ['AVG', LN('sbi.cockpit.selectgridpanel.aggfunc.name.avg'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.avg')],
        ['MAX', LN('sbi.cockpit.selectgridpanel.aggfunc.name.max'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.max')],
        ['MIN', LN('sbi.cockpit.selectgridpanel.aggfunc.name.min'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.min')],
        ['COUNT', LN('sbi.cockpit.selectgridpanel.aggfunc.name.count'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.count')],
        ['COUNT_DISTINCT', LN('sbi.cockpit.selectgridpanel.aggfunc.name.countdistinct'), LN('sbi.cockpit.selectgridpanel.aggfunc.desc.countdistinct')]
    ] 

	, ARITHMETIC_FUNCTIONS : [
        {
        	text: '+'
            , qtip: LN('sbi.cockpit.selectgridpanel.func.sum.tip')
            , type: 'function'
            , value: Ext.util.Format.htmlEncode('+')
            , alias: Ext.util.Format.htmlEncode('+')
        }, {
            text: '-' 
            , qtip: LN('sbi.cockpit.selectgridpanel.func.difference.tip')
            , type: 'function'
            , value: Ext.util.Format.htmlEncode('-')
            , alias: Ext.util.Format.htmlEncode('-')
        }, {
            text: '*'
            , qtip: LN('sbi.cockpit.selectgridpanel.func.multiplication.tip')
            , type: 'function'
            , value: Ext.util.Format.htmlEncode('*')
            , alias: Ext.util.Format.htmlEncode('*')
         }, {
            text: '/'
            , qtip: LN('sbi.cockpit.selectgridpanel.func.division.tip')
            , type: 'function'
            , value: Ext.util.Format.htmlEncode('/')
            , alias: Ext.util.Format.htmlEncode('/')
         }, {
            text: '||'
         	, qtip: LN('sbi.cockpit.selectgridpanel.func.pipe.tip')
         	, type: 'function'
         	, value: Ext.util.Format.htmlEncode('||')
         	, alias: Ext.util.Format.htmlEncode('||')
         }, {
            text: '('
         	, qtip: LN('sbi.cockpit.selectgridpanel.func.openpar.tip')
         	, type: 'function'
         	, value: Ext.util.Format.htmlEncode('(')
            , alias: Ext.util.Format.htmlEncode('(')
         }, {
            text: ')'
            , qtip: LN('sbi.cockpit.selectgridpanel.func.closepar.tip')
            , type: 'function'
            , value: Ext.util.Format.htmlEncode(')')
            , alias: Ext.util.Format.htmlEncode(')')
         }
    ]

	, AGGREGATION_FUNCTIONS : [
       {
    	   text: 'SUM'
           , qtip: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.sum')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('SUM(op1)')
           , alias: Ext.util.Format.htmlEncode('SUM(op1)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpSum')}]
       }, {
    	   text: 'MIN'  
           , qtip: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.min')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('MIN(op1)')
           , alias: Ext.util.Format.htmlEncode('MIN(op1)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpMin')}]
       }, {
           text: 'MAX' 
           , qtip: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.max')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('MAX(op1)')
           , alias: Ext.util.Format.htmlEncode('MAX(op1)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpMax')}]
       }, {
           text: 'COUNT'
           , qtip: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.count')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('COUNT(op1)')
           , alias: Ext.util.Format.htmlEncode('COUNT(op1)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpCount')}]
       }, {
           text: 'COUNT_DISTINCT'
           , qtip: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.countdistinct')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('COUNT(DISTINCT op1)')
           , alias: Ext.util.Format.htmlEncode('COUNT(DISTINCT op1)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpCountDist')}]
       }, {
           text: 'AVG'
           , qtip: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.avg')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('AVG(op1)')
           , alias: Ext.util.Format.htmlEncode('AVG(op1)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpAVG')}]
       }
    ]

	, DATE_FUNCTIONS : [
       {
           text: 'GG_between_dates'
           , qtip: LN('sbi.cockpit.selectgridpanel.datefunc.desc.ggbetweendates')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('GG_between_dates(op1,op2)')
           , alias: Ext.util.Format.htmlEncode('GG_between_dates(op1,op2)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpDate1')}
           , {label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpDate2')}]
       }, {
           text: 'MM_between_dates'
           , qtip: LN('sbi.cockpit.selectgridpanel.datefunc.desc.mmbetweendates')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('MM_between_dates(op1,op2)')
           , alias: Ext.util.Format.htmlEncode('MM_between_dates(op1,op2)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpDate1')}
           , {label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpDate2')}]
       },{
    	   text: 'AA_between_dates'
           , qtip: LN('sbi.cockpit.selectgridpanel.datefunc.desc.aabetweendates')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('AA_between_dates(op1,op2)')
           , alias: Ext.util.Format.htmlEncode('AA_between_dates(op1,op2)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpDate1')}
           , {label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpDate2')}]
       }, {
           text: 'GG_up_today'
           , qtip: LN('sbi.cockpit.selectgridpanel.datefunc.desc.gguptoday')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('GG_up_today(op1)')
           , alias: Ext.util.Format.htmlEncode('GG_up_today(op1)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpDate')}]
        }, {
           text: 'MM_up_today'
           , qtip: LN('sbi.cockpit.selectgridpanel.datefunc.desc.mmuptoday')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('MM_up_today(op1)')
           , alias: Ext.util.Format.htmlEncode('MM_up_today(op1)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpDate')}]
        }, {
           text: 'AA_up_today'
           , qtip: LN('sbi.cockpit.selectgridpanel.datefunc.desc.aauptoday')
           , type: 'function'
           , value: Ext.util.Format.htmlEncode('AA_up_today(op1)')
           , alias: Ext.util.Format.htmlEncode('AA_up_today(op1)')
           , operands: [{label: LN('sbi.cockpit.selectgridpanel.aggfunc.desc.labelOpDate')}]
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

// arithmetic functions
Sbi.commons.Constants.CALCULATED_FIELD_EDITOR_ARITHMETIC_FUNCTIONS = Sbi.commons.Constants.ARITHMETIC_FUNCTIONS;
Sbi.commons.Constants.INLINE_CALCULATED_FIELD_EDITOR_ARITHMETIC_FUNCTIONS = Sbi.commons.Constants.ARITHMETIC_FUNCTIONS;
Sbi.commons.Constants.SLOTS_EDITOR_ARITHMETIC_FUNCTIONS = Sbi.commons.Constants.ARITHMETIC_FUNCTIONS;
//date functions
Sbi.commons.Constants.CALCULATED_FIELD_EDITOR_DATE_FUNCTIONS = Sbi.commons.Constants.DATE_FUNCTIONS; // ???
Sbi.commons.Constants.INLINE_CALCULATED_FIELD_EDITOR_DATE_FUNCTIONS = Sbi.commons.Constants.DATE_FUNCTIONS;
Sbi.commons.Constants.SLOTS_EDITOR_DATE_FUNCTIONS = Sbi.commons.Constants.DATE_FUNCTIONS;
// aggregation functions
Sbi.commons.Constants.INLINE_CALCULATED_FIELD_EDITOR_AGGREGATION_FUNCTIONS = Sbi.commons.Constants.AGGREGATION_FUNCTIONS;
Sbi.commons.Constants.SLOTS_EDITOR_AGGREGATION_FUNCTIONS = Sbi.commons.Constants.AGGREGATION_FUNCTIONS;
// script functions
Sbi.commons.Constants.CALCULATED_FIELD_EDITOR_SCRIPT_FUNCTIONS = Sbi.commons.Constants.SCRIPT_FUNCTIONS;

//Sbi.constants.worksheet = {
Sbi.commons.Constants.worksheet = {
		
};

//Sbi.constants.formviewer = {
Sbi.commons.Constants.formviewer = {
		
};