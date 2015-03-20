/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
/**
  * Object name 
  * 
  * [description]
  * 
  * 
  * Public Properties
  * 
  * [list]
  * 
  * 
  * Public Methods
  * 
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Alberto Ghedin (alberto.ghedin@eng.it)
  */

Ext.ns("Sbi.widgets");

Sbi.widgets.ModelInstanceTree = function(){
	
    // public space
	return {
    

    createGoalModelInstanceTree: function(conf){
    	var c = {
        		border: false,
    	        height: 300,
    	        autoScroll:true,
    	        title: ' ',
    			autoWidth : true,
    			layout: 'fit',
    			userArrows : true,
    			animate : true,
    			autoScroll : true,		
                style: {
                    "border":"none"
                },
    			scope : this,
    			shadow : true,
    			enableDD: false,
    			root : conf.rootNode, 
    	        columns:[{
    	            header:'kpi',
    	            columnId: 'kpi',
    	            width:200,
    	            dataIndex:'modelText',
    	            fieldType: 'text'
    	        },{
    	            header:'kpiId',
    	            columnId: 'id',
    	            width:0,
    	            visibility: 'hidden',
    	            dataIndex:'modelText',
    	            fieldType: 'text'
    	        },{
    	            header:' ',
    	            columnId: 'check',
    	            width:30,
    	            dataIndex:'checked',
    	            fieldType: 'checkbox'
    	        },{
    	            header: LN('sbi.goals.weight1'),
    	            columnId: 'weight1',
    	            width:70,
    	            dataIndex:'weight1',
    	            fieldType: 'input'
    	        },{
    	            header: LN('sbi.goals.weight2'),
    	            columnId: 'weight2',
    	            width:70,
    	            dataIndex:'weight2',
    	            fieldType: 'input'
    	        },{
    	            header:' ',
    	            columnId: 'threshold1',
    	            width:70,
    	            dataIndex:'threshold1',
    	            fieldType: 'input'
    	        },{
    	            header:' ',
    	            columnId: 'sign1',
    	            width:55,
    	            dataIndex:'sign1',
    	            fieldType: 'combo'
    	        },{
    	            header:' ',
    	            columnId: 'sign2',
    	            width:55,
    	            dataIndex:'sign2',
    	            fieldType: 'combo'
    	        },{
    	            header:' ',
    	            columnId: 'threshold2',
    	            width:70,
    	            dataIndex:'threshold2',
    	            fieldType: 'input'
    	        }],

    	        loader: new Ext.tree.TreeLoader({
    	           
    	            uiProviders:{
    	                'col': Sbi.widgets.ColumnNodeUI
    	            },
    				nodeParameter: 'modelInstId',
    				dataUrl: conf.manageTreeService,
    				baseParams : conf.treeLoaderBaseParameters,
    		        createNode: function(attr) {
    		        	attr.uiProvider='col';
    		            if (attr.modelInstId) {
    		                attr.id = attr.modelInstId;
    		            }

    		    		if (attr.kpiInstId !== undefined && attr.kpiInstId !== null
    		    				&& attr.kpiInstId != '') {
    		    			attr.iconCls = 'has-kpi';
    		    		}
    		    		if (attr.error !== undefined && attr.error !== false) {
    		    			attr.cls = 'has-error';
    		    		}
    		    		var attrKpiCode = '';
    		    		if(attr.kpiCode !== undefined){
    		    			attrKpiCode = ' - '+attr.kpiCode;
    		    		}

    		    		if(attr.kpiInstActive !== undefined && !attr.kpiInstActive){
    		    			attr.disabled = true;
    		    			attr.cls = attr.cls+' line-through';
    		    		}

    					attr.checked= attr.weight1!=undefined;
    					
    		    		attr.qtip = attr.modelCode+' - '+attr.name+ attrKpiCode;
    		    		
    		    		var node = Ext.tree.TreeLoader.prototype.createNode.call(this, attr);
    		    		return node;
    		        }
    	        })

    	    }
    	
    	c = Ext.apply(c,conf);
    	return new Sbi.widgets.ColumnTree(c);
    }
	};
}();
