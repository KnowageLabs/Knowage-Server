
/**
 * SpagoBI - The Business Intelligence Free Platform
 *
 * Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 **/
 

/**
  * Object name 
  * 
  * 
  * 
  * 
This class act in the same way of LookupFieldWithMaximize for a lokk up

TO ACTIVATE THIS CLASS YOU SHOULD APPLY THOSE CHANGES TO THE OBJECT Sbi.widgets.LookupField

Ext.ns("Sbi.widgets");

Sbi.widgets.LookupField = function(config) {
	
	Ext.apply(this, config);
	
	this.store = config.store;
	if(config.cm){
	    this.cm = config.cm;
    }
	this.store.on('metachange', function( store, meta ) {
		this.updateMeta( meta );
	}, this);
	this.store.on('load', function( store, records, options  ) {
		this.applySelection();		
	}, this);
		
	if(config.drawFilterToolbar !== undefined && config.drawFilterToolbar !== null && config.drawFilterToolbar==false){
		this.drawFilterToolbar = false;
	}else{
		this.drawFilterToolbar = true;
	}
	this.store.baseParams  = config.params;
	this.params = config.params;
	this.initWin();
	
	***************STARTCHANGE*************************
	var c = Ext.apply({}, config, {
		trigger1Class: 'x-form-search-trigger'
	    ,trigger2Class:'x-exec-lookup-hidden-twin'
	    ,onTrigger1Click:function(e) {
				if(!this.disabled) {
					this.onLookUp(); 
				}
			}
		, enableKeyEvents: true
		,  width: 150
		//, 	readOnly: true
	});   
	    	*************END**************************
	// constructor
	Sbi.widgets.LookupField.superclass.constructor.call(this, c);
	
	

		****************STARTCHANGE***************************
	this.on("render", function(field) {
		field.el.on("keyup", function(e) {
			this.xdirty = true;
		}, this);
	}, this);
	    *****************END**************************
	this.addEvents('select');	
	
	
};
	*****************STARTCHANGE**************************
Ext.extend(Sbi.widgets.LookupField, Ext.form.TwinTriggerField, {
    ******************END************************

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

Ext.ns("Sbi.execution");

Sbi.execution.LookupFieldWithTableWindow = function(config) {
	
	Ext.apply(this, config);
	

	
	var c = Ext.apply({}, config, {
	    trigger2Class:'x-exec-lookup-twin-maximize'
	    ,onTrigger2Click: this.showWindow

	});   
	
	// constructor
	Sbi.execution.LookupFieldWithTableWindow.superclass.constructor.call(this, c);
	
	
	
};

Ext.extend(Sbi.execution.LookupFieldWithTableWindow, Sbi.widgets.LookupField, {

    
    getListOfValues: function(HTMLtable){
    	var list = new Array();
    	var tdPositionStart = null;
    	var tdPositionEnd = null;
    	var tdClosePositionStart = null;
    	var itemEndPosition = null;
    	var value;
    	var td = "td";
    	
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
        		value =  Ext.util.Format.htmlDecode(value);
        		value =  Ext.util.Format.stripTags(value);
        		value = value.replace(/&nbsp;/g," ");
        		//Add to the list the content of the td tag 
        		list.push(value);
        		
        		//update the HTMLtable
        		HTMLtable = HTMLtable.substring(tdClosePositionStart);
        		
        	}	
    	}else{
    		
    		HTMLtable =  Ext.util.Format.htmlDecode(HTMLtable);
    		HTMLtable =  Ext.util.Format.stripTags(HTMLtable);
    		HTMLtable = HTMLtable.replace(/&nbsp;/g," ");
    		
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
    	
    	var string ="";
    	for(var i=0; i<list.length; i++){
    		string = string+list[i]+';';
    	}
    	return string;
    }
    

	
	,showWindow: function(){
		var thisPanel = this;

		if(!this.window){
			var htmleditor = new Ext.form.HtmlEditor({
		        fieldLabel: 'Message text',
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
		        title: 'Insert the values or copy from a xls',
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
		        items: htmleditor,
		        buttons: [{
		            text: 'Ok',
		            handler: function(){
		            	var v =htmleditor.getValue(); 
		            	var vlist = thisPanel.getListOfValues(v);
		            	v =  thisPanel.fromListToString( vlist);
		            	thisPanel.xselection  = {};//se the value for the look up field
		            	
		            	for(var y =0; y<vlist.length; y++){
		            		if(vlist[y]!=undefined && vlist[y]!=null && vlist[y].length>0){
		            			thisPanel.xselection[vlist[y]] = vlist[y];
		            		}
		            		 
		            	}
		            	//thisPanel.onOk();
		            	thisPanel.setValue(thisPanel.xselection);
		            	//thisPanel.setValue(v);
		            	thisPanel.window.hide();
		            }
		        },{
		            text: 'Cancel',
		            	handler: function(){
		            		thisPanel.window.hide();
			            }
		        }]
		    });
		}
		
		this.window.on("beforeclose",function(){this.window.hide();return false;},this);
		
		this.window.show();
		
	}
	
});