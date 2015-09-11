/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
  
 
 
// create namespace
Ext.namespace('it.eng.spagobi.engines.qbe.temporalfilterwizard');
 
// create module
it.eng.spagobi.engines.qbe.temporalfilterwizard = function() {
  // do NOT access DOM from here; elements don't exist yet
 
  // private variables
  var win = undefined;
  
  // true only after the first time the window get rendered (used for lazy initialization)
  var active = false;
  
  // used if someone want to set the expression before the first visualization
  var expression = undefined;
  
  var caller = undefined;
   
  var getWizardWindow =  function() {
    if(!win) {
      win = new Ext.Window({
        id:'temporalFilterWizard',
        title: LN('sbi.qbe.temporalfilter.title'),
	 	layout:'fit',
		width:400,
		height:200,
		closeAction:'hide',
		items: [temporalfiltersgrid]
      });
    }
      
    return win;
  };    
  
     
  //  ------------------------------------------------------------------------
  //  CENTER Region (Expression editor)
  //  ------------------------------------------------------------------------
  
//create a Record constructor:
  var rt = Ext.data.Record.create([
      {name: 'filterId'},
      {name: 'label'},
      {name: 'filterExpression'},
  ]);
  var myStore = new Ext.data.Store({
      // explicitly create reader
      reader: new Ext.data.ArrayReader(
          {
              idIndex: 0  // id for each record will be the first element
          },
          rt // recordType
      )
  });

  var 
  yearFilter = {
		  leftOperandValue:  'TIME',
		  leftOperandDescription: 'YEAR',
		  leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD,
		  operator : 'EQUALS TO',
		  rightOperandValue : ['Current'],
		  rightOperandDescription: ['Current'],
		  temporalOperand: 
			{
				type: 	'CURRENT',
				period: 'YEAR'
			}
  },
  monthFilter = {
		  leftOperandValue:  'TIME',
		  leftOperandDescription: 'MONTH',
		  leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD,
		  operator : 'EQUALS TO',
		  rightOperandValue : ['Current'],
		  rightOperandDescription: ['Current']
  },
  dayFilter = {
		  leftOperandValue:  'TIME',
		  leftOperandDescription: 'DAY',
		  leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD,
		  operator : 'EQUALS TO',
		  rightOperandValue : ['Current'],
		  rightOperandDescription: ['Current']
  },
  
  previousYear = {

		  leftOperandValue:  'TIME',
		  leftOperandDescription: 'YEAR',
		  leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD,
		  operator : 'LAST',
		  rightOperandValue : ['22'],
		  rightOperandDescription: ['22']
  };
  
  var myData = [
   ['cy',	'CURRENT YEAR', 	[yearFilter] ],
   ['cm',	'CURRENT MONTH', 	[yearFilter,monthFilter] ],
   ['td',	'TODAY', 			[yearFilter,monthFilter,dayFilter] ],
   ['py',	'PREVIOUS_22YEARS', [previousYear] ]
  ];
  myStore.loadData(myData);
  
  
  var temporalfiltersgrid = new Ext.grid.GridPanel({
	    store: myStore,
	    colModel: new Ext.grid.ColumnModel({
	        defaults: {
	            sortable: true
	        },
	        columns: [
	            {header: 'Filter name', dataIndex: 'label'}
	        ],
	    }),
	    viewConfig: {
	        forceFit: true,
	    },
	    sm: new Ext.grid.RowSelectionModel({singleSelect:true}),
	    listeners: {
	    	rowclick: function( el, rowIndex, e ) {
	           var filters = myStore.getAt(rowIndex).get('filterExpression');
	           filters.forEach(function(filter, index, array) {
	        	   caller.addFilter(filter);
        		   console.log(filter);
	           });
	           getWizardWindow().hide();
	    	}
	    }
	});
  

  var getExpression = function() {
    return expression;
  };
  
  var setExpression = function(exp) {
  		expression = exp;
  };

  var getCaller = function() {
	  return caller;
  };

  var setCaller = function(c) {
	  caller = c;
  };

         
    
 
    // public space
    return {
        // public properties, e.g. strings to translate
        
        // public methods
        init: function() { 
          // lazy initialization...
          getWizardWindow();
        },
        
        setExpression: function(exp) {
          setExpression(exp, true);
        },
        
        getExpression: function() {
          return getExpression();
        },
        
        show: function(caller) {
          setCaller(caller);
          getWizardWindow().show();
        },
        
        hide: function(){
          getWizardWindow().hide();
        }
    };
}(); // end of app
