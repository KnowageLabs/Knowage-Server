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

Ext.ns("Sbi.crosstab.core");

Sbi.crosstab.core.CrossTabContextualMenu = function(node, crossTab) {
	
	this.crossTab = crossTab;
	
	if (node.horizontal) {
		this.headers = this.crossTab.columnHeader;
	} else {
		this.headers = this.crossTab.rowHeader;
	}

	var c = {
			id:'feeds-ctx',
			items: this.createMenuItems(node)
    };
	
	Sbi.crosstab.core.CrossTabContextualMenu.superclass.constructor.call(this, c);
	
};
		
Ext.extend(Sbi.crosstab.core.CrossTabContextualMenu, Ext.menu.Menu, {
	crossTab: null,
	headers: null
        
    // For every hidden brother of the node, this method creates
    // a checkbox. If the user checks the checkbox the linked
    // header will be shown.
    , getHiddenCheckboxes: function(node){
    	var header=this.headers[node.level];
    	var checkBoxes= new Array();    	
    	for(var i=0; i<header.length; i++){
    		if(header[i].type!='partialsum'){
	    		var text= header[i].name;   	
	    		var father = header[i].father;
	    		while(father.father!=null){//the node is not the root
	    			text = father.name+" / "+ text;
	    			father = father.father;
	    		}
	
	    		if(header[i].hidden){
		    		var freshCheck = new Ext.menu.CheckItem({
						checked: false,
						text: text,
						id : (i+1)//with 0 it doesn't work
					});
		    		freshCheck.on('checkchange', function(checkBox, checked){
		   
		    			Sbi.crosstab.core.CrossTabShowHideUtility.showHideNode(header[checkBox.id-1], false, false, this.crossTab);
		 
		    		}, this);
		    		checkBoxes.push(freshCheck);
		    		if(i<header.length-1 && (header[i].father.name != header[i+1].father.name)){
		    			checkBoxes.push('-');
		    		}
	    		}
    		}
    	}
    	return checkBoxes;
    }

    //load the checkboxes for the show/hide menu (IT WORKS BUT IS NOT USED)
//    , getCheckboxes : function(horizontal){
//   	
//    	var checkBoxes = new Array();
//    	for(var i=0; i<this.headers[this.headers.length-1].length; i++){
//    		var text= this.headers[this.headers.length-1][i].name;
//    		var father = this.headers[this.headers.length-1][i].father;
//    		while(father.father!=null){//the node is not the root
//    			text = father.name+" / "+ text;
//    			father = father.father;
//    		}
//    		
//    		var freshCheck = new Ext.menu.CheckItem({
//				checked: !(this.headers[this.headers.length-1][i].hidden),
//				text: text,
//				id : (i+1)//with 0 it doesn't work
//			});
//    		freshCheck.on('checkchange', function(checkBox, checked){
//    									if(checked){
//    										Sbi.crosstab.core.CrossTabShowHideUtility.showLine(checkBox.id-1, horizontal, this.crossTab, false);
//    									}else{
//    										Sbi.crosstab.core.CrossTabShowHideUtility.hideLine(checkBox.id-1, horizontal, this.crossTab, false);
//    									}	
//    					}, this);
//    		checkBoxes.push(freshCheck);
//    		if(i<this.headers[this.headers.length-1].length-1 && (this.headers[this.headers.length-1][i].father.name != this.headers[this.headers.length-1][i+1].father.name)){
//    			checkBoxes.push('-');
//    		}
//    	}
//    	return checkBoxes;
//	}     
    
    
    //load the checkboxes for the measures
    , getCheckboxesForMeasures : function(horizontal){
    	
    	if(this.crossTab.misuresOnRow == horizontal){//if the measures live in the other axe
    		return new Array();
    	}
    	
    	var text;
    	var father;
    	var checkBoxes = new Array();
    	var visibleMesures = new Array();
    	var index, leafsLength=0;
   		this.leafs = new Array();
   		
   		//load the measures
       	for(var i=0; i<this.headers[this.headers.length-1].length; i++){
       		text= this.headers[this.headers.length-1][i].name;
       		index = this.leafs.indexOf(text);
       		if(index<0){
       			this.leafs.push(text);
       			//check if all the lines with this measure are visible
       			visibleMesures[leafsLength] = !(this.headers[this.headers.length-1][i].hidden);
       			leafsLength++;
        	}else{
        		visibleMesures[index] = visibleMesures[index] || !(this.headers[this.headers.length-1][i].hidden);
        	}
    	}
    	
    	for(var i=0; i<this.leafs.length; i++){
    		text= this.leafs[i];

    		var freshCheck = new Ext.menu.CheckItem({
				checked: visibleMesures[i],
				text: text,
				id : text
			});
    		freshCheck.on('checkchange', function(checkBox, checked){
        	
    			Sbi.crosstab.core.CrossTabShowHideUtility.showHideMeasure(checkBox.id, !checked, horizontal, this.crossTab);
    
    		}, this);
    		checkBoxes.push(freshCheck);
    	}
    	return checkBoxes;
	}
    
    , addCalculatedFieldHandler: function(header) {	
   		this.crossTab.showCFWizard(header, 'new'); 
   	}
    
    , modifyCalculatedFieldHandler: function(header) {
   		this.crossTab.showCFWizard(header, 'edit');
   	}
    
    , createMenuItems: function (node) {
    	var toReturn = ['-'];
    	if (node.type == 'CF' && node.cfExpression !== undefined) {
    		toReturn.push({
		       	text: LN('sbi.crosstab.menu.removecalculatedfield'),
		       	iconCls: 'remove',
		       	handler: function() {this.crossTab.removeCalculatedField(node);},
		       	scope: this
    		});
    		toReturn.push({
		       	text: LN('sbi.crosstab.menu.modifycalculatedfield'),
		       	iconCls: 'edit',
		       	handler: this.modifyCalculatedFieldHandler.createDelegate(this, [node], 0),
		       	scope: this
    		});
    	}
    	if(node.type!='partialsum'){
	    	toReturn = toReturn.concat([
			        {
				       	text: LN('sbi.crosstab.menu.addcalculatedfield'),
				       	iconCls: 'add',
				       	handler: this.addCalculatedFieldHandler.createDelegate(this, [node], 0),
				       	scope: this
			        },
			        '-']);
    	}
       	if(!(node.childs.length==0) || ((node.childs.length==0)&&(this.crossTab.misuresOnRow == node.horizontal))){
       		toReturn = toReturn.concat([
		        {
		        	text: LN('sbi.crosstab.menu.hideheader'),
		        	iconCls:'hide',
		        	handler:function(){
		        	
		        		Sbi.crosstab.core.CrossTabShowHideUtility.showHideNode(node, true, false, this.crossTab) ;       
		        	
		        	},
		        	scope: this
		        },
		        {
		        	text: LN('sbi.crosstab.menu.hideheadertype'),
		        	iconCls:'hide',
		        	handler:function(){
		        		
		        		Sbi.crosstab.core.CrossTabShowHideUtility.showHideAllNodes(node, true, this.crossTab, false);
		        	
		        	},
		        	scope: this
		        },
		        {
		        	text: LN('sbi.crosstab.menu.hiddenheader'),
		        	iconCls:'show',
		        	menu:  new Ext.menu.Menu({
		        		items: this.getHiddenCheckboxes(node)
		        	})
		        }
       		 ]);
    	}
    
       	toReturn = toReturn.concat([ 

		        {
		        	text: LN('sbi.crosstab.menu.hidemeasure'),
		        	iconCls:'show',
		        	menu:  new Ext.menu.Menu({
		        		items: this.getCheckboxesForMeasures(node.horizontal)
		        	})
		        }
		]);
    	return toReturn;
    	
    }
    
});
