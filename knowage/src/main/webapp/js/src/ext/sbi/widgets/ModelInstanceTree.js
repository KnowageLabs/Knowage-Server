/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
 
  
 
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
