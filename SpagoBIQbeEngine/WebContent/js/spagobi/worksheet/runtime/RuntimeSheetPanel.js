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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
 */
Ext.ns("Sbi.worksheet.runtime");

Sbi.worksheet.runtime.RuntimeSheetPanel = function(config) { 

	
	var defaultSettings = {};

	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.runtime.runtimeSheetPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.runtime.runtimeSheetPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);
	
	c = {
		border: false,
		title: this.sheetConfig.name,
        items: [new Ext.Panel({})],
        autoScroll: true
	};
	
	c = Ext.apply(config, c);
	
	this.addEvents('contentloaded');
	
	this.on('activate', this.renderContent, this);
	
	Sbi.worksheet.runtime.RuntimeSheetPanel.superclass.constructor.call(this, c);	 	
};

Ext.extend(Sbi.worksheet.runtime.RuntimeSheetPanel, Ext.Panel, {
	content: null,
	filtersPanel : null,
	contentLoaded: false,
	filtersInfoPanel : null,
	filtersInfoButton : null,	
	filtersInfoWindow : null,	
	exportContent: function(mimeType){
		var exportedContent = this.content.exportContent();
		var header = this.sheetConfig.header;	
		if(header!=null && header!=undefined){
			var headerTitleHTML = this.sheetConfig.header.title;
			if (mimeType == 'application/vnd.ms-excel') {
				var headerTitle = Ext.util.Format.stripTags( headerTitleHTML );
				header.title = headerTitle;
			} else {
				header.title = headerTitleHTML;
			}
		}
		var footer = this.sheetConfig.footer;
		if(footer!=null && footer!=undefined){
			var footerTitleHTML = this.sheetConfig.footer.title;
			if (mimeType == 'application/vnd.ms-excel') {
				var footerTitle = Ext.util.Format.stripTags( footerTitleHTML );
				footer.title = footerTitle;
			} else {
				footer.title = footerTitleHTML;
			}
		}
		var filters =null;
		if(this.filtersPanel!=null && this.filtersPanel!=undefined){
			filters = this.filtersPanel.getFormState(true);
		}
		var completedExportedContent = {
				HEADER: header,
				FOOTER: footer,
				CONTENT: exportedContent,
				FILTERS: filters
				};
			completedExportedContent.sheetName=this.sheetConfig.name;
		return completedExportedContent;
	},
	
	initPanels: function(){
		var items = [];
		var dynamicFilters = [];
		var hiddenContent = false;
		
		
		//prepare the filters.. we need to do this before build the content 
		//because we need to know if there is same mandatory filter
		if (this.sheetConfig.filters != undefined && this.sheetConfig.filters != null &&  this.sheetConfig.filters.filters != undefined &&  this.sheetConfig.filters.filters != null && this.sheetConfig.filters.filters.length > 0) {
			var i = 0;
			for (; i < this.sheetConfig.filters.filters.length; i++ ) {
				var aDynamicFilter = this.getDynamicFilterDefinition(this.sheetConfig.filters.filters[i]);
				if(!hiddenContent && aDynamicFilter.allowBlank!=undefined && aDynamicFilter.allowBlank!=null && !aDynamicFilter.allowBlank){
					hiddenContent=true;
				}
				dynamicFilters.push(aDynamicFilter);	
			}
		}
		
		// filters button
		this.addFiltersInfoPanelButton(items);
		
		//Builds the content
		this.content = new Sbi.worksheet.runtime.RuntimeSheetContentPanel(
				Ext.apply({
					style : 'float: left; width: 100%',
					hiddenContent : hiddenContent
				}, {
					contentConfig : this.sheetConfig.content
					, sheetName : this.sheetConfig.name
					, fieldsOptions: this.fieldsOptions
				})
		);
		//catch the event of the contentloaded from the component and hide the loading mask
		this.content.on('contentloaded',this.hideMask,this);
		this.content.on('contentloading',this.showMask,this);
		
		//show the loading mask
		if(this.rendered){
			this.showMask();
		} else{
			this.on('afterlayout',this.showMask,this);
		}
		
		
			
		//Builds the header
		if (this.sheetConfig.header!=undefined && this.sheetConfig.header!=null){
			this.addTitle(this.sheetConfig.header,items, true);		
		}

		if (this.sheetConfig.filters != undefined && this.sheetConfig.filters != null 
					&& this.sheetConfig.filters.filters != undefined && this.sheetConfig.filters.filters != null 
						&& this.sheetConfig.filters.filters.length > 0) {
			var filterConf = {
					title : LN('sbi.worksheet.runtime.runtimesheetpanel.filterspanel.title')
					, layout: 'auto'
					, autoScroll :true
					, style:'padding: 5px 15px 10px 15px;'	
					, position : 'top'
//					, tools:  [{
//						id: 'gear'
//				        	, handler: this.applyFilters
//				          	, scope: this
//				          	, qtip: LN('sbi.worksheet.runtime.runtimesheetpanel.filterspanel.filter.qtip')
//					}]
				};
			
			if ( this.sheetConfig.filters.position=='left') {
				filterConf.width= 250;
				filterConf.autoWidth = false;
				filterConf.style = 'float: left; padding: 15px 0px 10px 15px';
				filterConf.position = 'left';
			}
			/* this was an attempt to make the filters panel collapsible, but it has a side-effect: combo-boxes are narrow
			 * when displayed on top of a chart
			 else {  
				filterConf.collapsible = true;
				filterConf.collapsed = true;
			}
			*/
			
			filterConf.sheetName = this.sheetConfig.name;
			this.filtersPanel = new Sbi.worksheet.RuntimeSheetFiltersPanel(dynamicFilters, filterConf);
			this.filtersPanel.on('apply', this.applyFilters, this);

			if ( this.sheetConfig.filters.position=='left') {
				var filterContentPanel = new Ext.Panel({
		            border: false,
					items:[this.filtersPanel,this.content]			       
				});
								
				items.push(filterContentPanel);
			}else{
				items.push(this.filtersPanel);
			}			
		}
			
		if (this.sheetConfig.filters==undefined || this.sheetConfig.filters==null || this.sheetConfig.filters.filters==undefined  || this.sheetConfig.filters.filters==null  || this.sheetConfig.filters.filters.length<=0 || this.sheetConfig.filters.position!='left') {
			items.push(this.content);
		}
		
//		this.addFiltersInfoPanelButton(items);
		
		//Builds the footer
		if (this.sheetConfig.footer!=undefined && this.sheetConfig.footer!=null){
			this.addTitle(this.sheetConfig.footer,items, false);
		}

		return items;
	}
	
	
	, addFiltersInfoPanelButton : function(items) {

		this.filtersInfoPanel = new Sbi.worksheet.runtime.RuntimeSheetFiltersInfoPanel({
			filtersInfo : this.sheetConfig.filtersInfo
		});	

		this.filtersInfoWindow = new Ext.Window({
			id:'DesignFilersId',
			layout:'fit',
			width:500,
			height:300,
			x:500,
			y:150,
			closeAction:'hide',
			frame:true,
			items: this.filtersInfoPanel
//			, buttons: [{
//				text: 'Close',
//				handler: function(){
//					this.filtersInfoWindow.hide();
//				}
//			}]
		});
		
		this.filtersInfoButton = new Ext.Button({
			//text: LN('filtri'),
			iconCls: 'infoImgIcon',
			x:100,
			y:50,
			handler: function(){
				this.filtersInfoWindow.show(this);
	        	//this.close();
	        }
	        , scope: this
		});
		
		items.push(this.filtersInfoButton);
		
		

		


	}
	
	/**
	 * Build the html for the header or the footer
	 * @param: title: the configuration {tilte, img, position}
	 * @param: items: the array of the panel item
	 * @param: header: true if it builds the header, false otherwise
	 * @return: the html for the title
	 */
	, addTitle: function(title, items, header){
		if(title.title!='' || title.img!=null){

			var titleHTML = '<div style="width: 100%; padding: 4px">'+title.title+'</div>';
			var html = titleHTML;
			
			if(title.img!=undefined && title.img!=null){

				var loadHeaderImgService = Sbi.config.serviceRegistry.getServiceUrl({
					serviceName: 'GET_IMAGE_CONTENT_ACTION'
					, baseParams: {FILE_NAME: title.img}
				});
				
				var imgWidth = '20';//default width 
				if(title.width!=undefined && title.width!=null && title.width!=''){
					imgWidth = title.width;
				}

				var imgHTML = '<img width="'+imgWidth+'px" src="'+loadHeaderImgService+'"></img>';
		
				if(title.position=='right') {
					html = '<table style="border-style: none; width:100%;"><tbody><tr><td>'+titleHTML+'</td><td width="'+ imgWidth+'px">'+imgHTML+'</td></tr></tbody></table>';	
				} else  if(title.position=='left') {
					html = '<table style="border-style: none; width:100%;"><tbody><tr><td width="'+imgWidth +'px">'+imgHTML+'</td><td>'+titleHTML+'</td></tr></tbody></table>';
				} else {
					if(title.title==undefined || title.title==null || title.title==''){
						titleHTML='';
					}
					if(header){ //position is center
						html = '<div style="text-align:center; width: 100%;"><img src="'+loadHeaderImgService+'"></img></div>'+titleHTML;
					}else {
						html = titleHTML+'<div style="text-align:center; width: 100%;"><img src="'+loadHeaderImgService+'"></img></div>';
					}
				}
			}
		
			var titlePanel = new Ext.Panel({
				style: "padding: 10px;",
				border: false,
				autoHeight: true,
				html : html
			});
			
		   	items.push(titlePanel);
		}
	}	
	
	, getDynamicFilterDefinition: function (aField) {
		var dynamicFilter = {
	            "text": aField.alias,
	            "field": aField.id,
	            "id": aField.id,
	            "operator": "IN",
	            "values": aField.values,
	            "orderType": aField.orderType
			};
		if(aField.mandatory=='yes'){
			dynamicFilter.allowBlank=false;
		}
		if(aField.selection=='singlevalue'){
			dynamicFilter.maxSelectedNumber=1;
		}
		if(aField.splittingFilter!=undefined && aField.splittingFilter!=null && (aField.splittingFilter=='on' )){
			dynamicFilter.splittingFilter='on';
		}
		
		// if a sheet filter on domain values is defined, put the filter admissible values
		var sheetFilter = this.getSheetFilterOnDomainValues(aField);
		if (sheetFilter != null) {
			dynamicFilter.values = sheetFilter.values;
		}
		return dynamicFilter;
	}
	
	, getSheetFilterOnDomainValues : function(aField) {
		var toReturn = null;
		for (var i = 0; i < this.sheetConfig.filtersOnDomainValues.length; i++) {
			var aFilter = this.sheetConfig.filtersOnDomainValues[i];
			if (aField.alias == aFilter.alias) {
				toReturn = aFilter;
				break;
			}
		}
		return toReturn;
	}
	
	//render the content after the sheet has been activated
	, renderContent: function(){
		this.un('activate', this.renderContent, this);
		this.removeAll();
		this.add(this.initPanels());
	}
	
	, applyFilters: function(filterPanel, formState){
		this.content.applyFilters(formState);
		var filtersInfo = filterPanel.getFiltersInfo();
		this.filtersInfoPanel.update(filtersInfo);
	}
	
	/**
	 * Opens the loading mask 
	 */
    , showMask : function(){
    	this.un('afterlayout',this.showMask,this);
    	if (this.loadMask == null) {//'runtimeworksheet'
    		this.loadMask = new Ext.LoadMask('runtimeworksheet', {msg: "Loading.."});
    		//this.loadMask = new Ext.LoadMask(this.getId(), {msg: "Loading.."});
    	}
    	this.loadMask.show();
    }
	
	/**
	 * Closes the loading mask
	 */
	, hideMask: function() {		
    	if (this.loadMask != null) {
    		this.loadMask.hide();
    	}
    	this.contentLoaded = true;
    	this.fireEvent('contentloaded');
	}
	
	, getAdditionalData: function(){
		var c ={};
		if(this.content!= undefined && this.content!=null){
			 c.data = this.content.getAdditionalData();
		}
		return c;
	}

});