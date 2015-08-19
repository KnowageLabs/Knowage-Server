/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *   
 *  @author
 *  Monica Franceschini
 */
 
  
Ext.define('Sbi.datamining.ResultPanel', {
	extend: 'Ext.panel.Panel',
	layout: {
        type: 'fit'
		, flex: 1
		, border: 0
    },
	
	config:{
		 autoScroll: true
		, border:0
		, padding: 3
		, width: 800
		, style: 'margin-bottom: 5px; '
	},
	
	dataminingParentPanel: null,
	
	type: null,
	result: '',
	resultTitleStyle: 'font-weight: bold; color: #28596A; padding-bottom: 2px; background: #CDD2D4; padding: 3px; margin: 0px;',
	plotStyle: 'font-weight: normal; padding:5px;',
	videoStyle: 'font-weight: normal; padding:5px; border: 1px solid #CDD2D4; width:100%; margin-top: 0px;',
	mode:'manual',
	
	constructor : function(config) {
		this.initConfig(config||{});
		
		this.command = config.command;
		this.output = config.output;
		this.mode = config.mode;
		
		this.outputPanel = config.itsParent;
		
		this.callParent(arguments);
	},

	initComponent: function() {

		this.callParent();

	}

	, getResult: function(rerun){
		
		var thisPanel = this;
		if(rerun == undefined || rerun === undefined || rerun == null){
			rerun = false;
		}
		var service = Ext.create("Sbi.service.RestService",{
			url: "result"
			,pathParams: [this.command, this.output, rerun]
		});
		
		var functionSuccess = function(response){
			
			if(thisPanel.outputPanel.itsParent.dmMask !== undefined && thisPanel.outputPanel.itsParent.dmMask != null){
				thisPanel.outputPanel.itsParent.dmMask.hide();
			}
			if(response != null && response.responseText !== undefined && response.responseText !== null && response.responseText !== ''){
			
				var res = Ext.decode(response.responseText);				
				var html='';
				if(res != null){
					var output = res;
					var type = output.outputType;
					var result = output.result;
					var varName = output.variablename;
					var plotName = output.plotName;
					
					if(type == Sbi.settings.datamining.execution.typeImage){
						html+='<div style="'+this.resultTitleStyle+'">'+plotName+' : </div><br/><p style="'+this.plotStyle+'"><img width="480px" height="480px" style="image-resolution: 72dpi;" alt="Result for '+plotName+'" src="data:image/png;base64,'+result+'" /></p><br/><br/><br/>';
						
					}else if(type == Sbi.settings.datamining.execution.typeText){
						if(varName == undefined || varName == null || varName == ''){
							varName = output.ouputLabel;
						}
						html+='<div style="'+this.resultTitleStyle+'">'+varName+' : </div><br/><p style="'+this.videoStyle+'">'+result+'</p><br/><br/><br/>';
					}else{
						if(varName == undefined || varName == null || varName == ''){
							varName = output.ouputLabel;
						}
						html+='<div style="'+this.resultTitleStyle+'">'+varName+' : </div><br/><p style="'+this.videoStyle+'">'+result+'</p><br/><br/><br/>';
						
					}
				}else {
					html='no result';
				}
				thisPanel.update(html);
			}
		};
		
		service.callService(this, functionSuccess);
	}

});