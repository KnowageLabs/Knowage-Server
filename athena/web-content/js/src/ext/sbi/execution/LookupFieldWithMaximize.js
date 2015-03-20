/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
 
 
 

/**
  * Object name 
  * 
  * a text field with a look up that allow the user to insert copied part from a xls or a libra office document
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

Ext.ns("Sbi.execution");

Sbi.execution.LookupFieldWithMaximize = function(config) {
	
	Ext.apply(this, config);
	

	
	var c = Ext.apply({}, config, {
		triggerClass:'x-exec-lookup-twin-maximize'
	    ,onTriggerClick: this.showWindow

	});   
	
	// constructor
	Sbi.execution.LookupFieldWithMaximize.superclass.constructor.call(this, c);
	
	
	
};

Ext.extend(Sbi.execution.LookupFieldWithMaximize,  Ext.form.TriggerField, {

    
    getListOfValues: function(HTMLtable){
    	var list = new Array();
    	var tdPositionStart = null;
    	var tdPositionEnd = null;
    	var tdClosePositionStart = null;
    	var itemEndPosition = null;
    	var value;
    	var td = "td";
    	
    	HTMLtable = HTMLtable.replace(/&nbsp;/g," ");
    	HTMLtable =  Ext.util.Format.htmlDecode(HTMLtable);
    	HTMLtable = HTMLtable.trim();
    	
    	if(HTMLtable.indexOf("<TD")>0){
    		td = "TD";
    	}
    	
    	if(HTMLtable.indexOf("<"+td)>0){
    		while(HTMLtable.length>0){
        		//search the beginning of a open td tag
        		tdPositionStart = HTMLtable.indexOf("<"+td);
        		if(tdPositionStart<0){
        			return list;
        		}
        		//search the end of a open td tag
        		tdPositionEnd = HTMLtable.indexOf(">",tdPositionStart+1);
        		//search the beginning of a close td tag
        		tdClosePositionStart =  HTMLtable.indexOf("</"+td,tdPositionEnd);
        		
        		value = HTMLtable.substring(tdPositionEnd+1,tdClosePositionStart);
        		value =  Ext.util.Format.stripTags(value);
        		
        		//Add to the list the content of the td tag 
        		list.push(value);
        		
        		//update the HTMLtable
        		HTMLtable = HTMLtable.substring(tdClosePositionStart);
        		
        	}	
    	}else{
    		
    		//HTMLtable =  Ext.util.Format.htmlDecode(HTMLtable);
    		HTMLtable =  Ext.util.Format.stripTags(HTMLtable);

    		
    		while(HTMLtable.length>0){
    			itemEndPosition = HTMLtable.indexOf(";");
        		if(itemEndPosition<0){
        			list.push(HTMLtable);
        			return list;
        		}
        		list.push(HTMLtable.substring(0,itemEndPosition));
        		//update the HTMLtable
        		HTMLtable = HTMLtable.substring(itemEndPosition+1);
        	}
    		
    	}
    	
    	
    	
    	return list;
    }
    
    ,fromListToString: function(list){
    	if(list.length==1){
    		return list[0];
    	}
    	var string ="";
    	for(var i=0; i<list.length; i++){
    		if(list[i]!=undefined && list[i]!=null && list[i].length>0){
    			string = string+list[i]+';';
    		}
    	}
    	return string;
    }
    

	
	,showWindow: function(){
		var thisPanel = this;

		if(!this.window){
			this.htmleditor = new Ext.form.HtmlEditor({
		        fieldLabel: 'Value',
			    enableAlignments : false,
			    enableColors : false,
			    enableFont :  false,
			    enableFontSize : false, 
			    enableFormat : false,
			    enableLinks :  false,
			    enableLists : false,
			    enableSourceEdit : false,
		        hideLabel: true,
		        name: 'msg',
		        flex: 1  // Take up all *remaining* vertical space
			});
			
			this.window = new Ext.Window({
		        title: LN('sbi.execution.parametersselection.maximizelookup.title'),
		        collapsible: true,
		        maximizable: true,
		        width: 500,
		        height: 400,
		        minWidth: 300,
		        minHeight: 200,
		        layout: 'fit',
		        plain: true,
		        bodyStyle: 'padding:5px;',
		        buttonAlign: 'center',
		        items: thisPanel.htmleditor,
		        buttons: [{
		            text: LN('sbi.execution.parametersselection.maximizelookup.ok'),
		            handler: function(){
		            	var v =thisPanel.htmleditor.getValue(); 
		            	var vlist = thisPanel.getListOfValues(v);
		            	v =  thisPanel.fromListToString( vlist);
		            	thisPanel.setValue(v);
		            	thisPanel.window.hide();
		            }
		        },{
		            text: LN('sbi.execution.parametersselection.maximizelookup.clear'),
	            	handler: function(){
	            		thisPanel.htmleditor.reset();
	            		thisPanel.htmleditor.setValue('');
		            }
		        },{
		            text: LN('sbi.execution.parametersselection.maximizelookup.cancel'),
		            	handler: function(){
		            		thisPanel.window.hide();
			            }
		        }]
		    });
		}
		
		this.htmleditor.reset();
		var initValue = thisPanel.getValue();
		if(initValue!=undefined && initValue!=null && initValue!=' ' && initValue!=''){
			this.htmleditor.setValue(initValue);
		}
			
		this.window.on("beforeclose",function(){this.window.hide();return false;},this);
		
		this.window.show();
		
	}
	
});