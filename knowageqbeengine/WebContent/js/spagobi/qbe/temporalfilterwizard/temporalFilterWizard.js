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
	  var timeReader = new Ext.data.JsonReader(
	          {
	        	  root: 'data',
	              id: 'id'
	          },
	          [
	           'id',
	           'name',
	           'type',
	           'staticFilter',
	           'definition'
	           ]
	      );
	  
	  var tree = qbe.queryEditorPanel.currentDataMartStructurePanel.tree.root.childNodes;
	  var n;
	  var hierachiesColumnTypes = "";
	  for (n in tree){
		  if (tree[n].attributes.iconCls && tree[n].attributes.iconCls == 'temporal_dimension'){
			  var temporalDimension = tree[n];
			  
			  for(var i in temporalDimension.attributes.children) {
				  var hierarchy = temporalDimension.attributes.children[i];
				  if(hierarchy.cls == 'default_hierarchies') {
					  for (j in hierarchy.children) {
						  if(hierarchy.children[j].alias) {
							  hierachiesColumnTypes += (""!=hierachiesColumnTypes?",":"") + hierarchy.children[j].type;
						  }
					  }
				  }
			  }
			  
			  break;
		  }
	  }
	  
	  var timeStore = new Ext.data.Store({
		  storeId:'tsDataStore',
		  reader: timeReader
	  });
	  
	  var params = {
			  types: hierachiesColumnTypes
	  };

	  var serviceurl = Sbi.config.contextName+'/restful-services/1.0/timespan/listTimespan';
	  Ext.Ajax.request({
          url: serviceurl,
          method: "GET",
          timeout: 60,
          disableCaching: false,
          params: params,
          success: function(response) {
        	  setTimeout(function(){
        			 timeStore.loadData(Ext.util.JSON.decode(response.responseText));}, 100);
          }
      });
	  
	  var temporalfiltersgrid = new Ext.grid.GridPanel({
		    store: timeStore,
		    colModel: new Ext.grid.ColumnModel({
		        defaults: {
		            sortable: true
		        },
		        columns: [
		            {header: 'Filter name', dataIndex: 'name'}
		        ],
		    }),
		    viewConfig: {
		        forceFit: true,
		    },
		    sm: new Ext.grid.RowSelectionModel({singleSelect:true}),
		    listeners: {
		    	rowclick: function( el, rowIndex, e ) {
		    		var definition = timeStore.getAt(rowIndex).get('definition');
		    		
		    		if(timeStore.getAt(rowIndex).get('staticFilter')){
		    			definition.leftOperandType = Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD;
		    			caller.addFilter(definition);
		    		} else {
			    		definition.forEach(function(span, index, array) {
			    			var filter;
			    			if (timeStore.getAt(rowIndex).get('type') == 'temporal'){
			    				filter = {
			    						leftOperandValue: 'it.eng.spagobi.meta.Time_by_day:the_date',
			    						leftOperandDescription: 'Time by day : The date',
			    						leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD,
			    						operator : 'BETWEEN',
			    						rightOperandValue : [span.from+' 00:00:00',span.to+' 00:00:00'],
			    						rightOperandDescription: span.from+' 00:00:00 ---- '+span.to+' 00:00:00',
			    						rightOperandDefaultValue: ['']
			    				};
			    				caller.addFilter(filter);
			    			}
			    			if (timeStore.getAt(rowIndex).get('type') == 'time'){
			    				var f = span.from;
			    				var t = span.to;
			    				filter = {
			    						leftOperandValue: 'it.eng.spagobi.meta.Time_by_minute:ID',
			    						leftOperandDescription: 'Time by minute : ID',
			    						leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD,
			    						operator : 'BETWEEN',
			    						rightOperandValue : [f.replace(':',''),t.replace(':','')],
			    						rightOperandDescription: span.from+' ---- '+span.to,
			    						rightOperandLongDescription: span.from+' ---- '+span.to,
			    						rightOperandDefaultValue: ['']
			    				};
			    				caller.addFilter(filter);
			    			}
			    		});
		    		}
		    		getWizardWindow().hide();
		    	}
		    }
		});
	  
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
