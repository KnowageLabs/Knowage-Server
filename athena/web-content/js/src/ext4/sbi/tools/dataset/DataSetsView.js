/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * 
 * Data view for a browser style. It define a layout and provides the stubs
 * methods for the icons browser. This methods should be overridden to define
 * the logic. Only the methods onAddNewRow, onGridSelect and onCloneRow are
 * implemented. The configuration dataView must be passed to the object. It
 * should contains the method setFormState (it is used by onGridSelect)
 * 
 * 
 * @example Ext.define('Sbi.tools.datasource.DataSourceListDetailPanel', {
 *          extend: 'Sbi.widgets.compositepannel.ListDetailPanel' , constructor:
 *          function(config) { //init services this.initServices(); //init form
 *          this.form =
 *          Ext.create('Sbi.tools.datasource.DataSourceDetailPanel',{services:
 *          this.services}); this.columns = [{dataIndex:"DATASOURCE_LABEL",
 *          header:"Name"}, {dataIndex:"DESCRIPTION", header:"description"}];
 *          this.fields =
 *          ["DATASOURCE_ID","DATASOURCE_LABEL","DESCRIPTION","DRIVER","DIALECT_ID","DIALECT_CLASS","DIALECT_NAME","JNDI_URL","USER","PASSWORD","SCHEMA","MULTISCHEMA","CONNECTION_URL"];
 *          this.form.on("save",this.onFormSave,this);
 *          this.callParent(arguments); } , initServices: function(baseParams){
 *          this.services["getAllValues"]=
 *          Sbi.config.serviceRegistry.getRestServiceUrl({ serviceName:
 *          'datasources/listall' , baseParams: baseParams }); ... } ,
 *          onDeleteRow: function(record){ Ext.Ajax.request({ url:
 *          this.services["delete"], params: {DATASOURCE_ID:
 *          record.get('DATASOURCE_ID')}, ... } , onFormSave: function(record){
 *          Ext.Ajax.request({ url: this.services["save"], params: record, ... }
 *          }); ...
 * 
 * 
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */

Ext.define('Sbi.tools.dataset.DataSetsView', {
	extend : 'Ext.DataView'

	,
	config : {
		/**
		 * The Ext.data.Store to bind this DataView to.
		 */
		store : null,

		/**
		 * A simple CSS selector that will be used to determine what nodes this
		 * DataView will be working with.
		 */
		itemSelector : null,

		/**
		 * The definition of the columns of the grid.
		 * {@link Sbi.widgets.store.InMemoryFilteredStore#InMemoryFilteredStore}
		 */
		columns : [],
		/**
		 * The list of the properties that should be filtered
		 */
		filteredProperties : new Array(),
		
		sorters : new Array(),
		
		autoScroll : false,
		
		fromMyDataCtx : true,
		
	    DETAIL_DOCUMENT: 'DocumentDetailManagement',
	    CREATE_DOCUMENT: 'CreateDocument',
	    
	    ckanFilter: 'NOFILTER',
	    ckanCounter: 0,
	    
	    CKAN_COUNTER_STEP: 200

	}

	/**
	 * In this constructor you must pass configuration
	 */
	,
	constructor : function(config) {
				
		this.initConfig(config);
		this.initTemplate();
		 
		Ext.apply(this, config || {});
		
		this.itemSelector = 'dd';
		this.trackOver = true;
		this.overItemCls = 'over';
		this.frame = true;
		this.emptyText = LN('sbi.ds.noDataset');
		this.inline = {
			wrap : false
		};
		this.scrollable = 'horizontal';
		
		this.callParent(arguments);

		this.addListener('itemclick', this.onClick, this);
		this.addListener('itemmouseenter', this.onMouseOverX, this);
		this.addListener('itemmouseleave', this.onMouseOutX, this);
		
		this.addEvents('detail');
		this.addEvents('share');
		this.addEvents('info');

	}
	
	, 
	initTemplate : function() {
		// BUILD THE TPL
		Sbi.debug('DataViewPanel bulding the tpl...');

		var noItem = LN('sbi.browser.folderdetailpanel.emptytext');
		var changed = LN('sbi.ds.changedon');
		var title = LN('sbi.ds.listTitle');
	
	
		var img = Sbi.config.contextName + '/themes/'+ Sbi.config.currTheme	+ '/img/dataset/img-dataset-1.jpg';
//		var img = "csv-xls-smaller.png";
//		if (!this.fromMyDataCtx ){
//			img = "csv-xls-small.png";
//		}
		
		var author = LN('sbi.generic.author');
		var currentUser = this.config.user;

		this.tpl = new Ext.XTemplate(
				'<div id="list-container" class="main-datasets-list">', 	            
//	 	           '<div class="dataset-group-view">',
//	 	            '<ul>',
	 	            	'<tpl if="root.length == 0">',
	 	            		'<div id="empty-group-message">',
	 	            		noItem,
	 	            		'</div>',
	 	            	'</tpl>', 
	 	            	'<tpl for=".">',
							'<dd class="box">',
								'<div class="box-container">',
									'<div class="box-figure">',
										'<img  align="center" src="'+img+'" alt=" " />',
										'<span class="shadow"></span>',
										'<div class="hover">',
			                                '<div class="box-actions-container">'+
								            '    <ul class="box-actions">'+	    
								            '		<tpl for="actions">'+  
								        	' 			<tpl if="name != \'delete\' && this.includeAction(name) == true ">'+
								        	'					<tpl if="name == \'detaildataset\' && ( parent.dsTypeCd == \'File\' || parent.dsTypeCd == \'Qbe\' || parent.dsTypeCd == \'Ckan\' ) ">'+
									        ' 	       				<li class="{name}"><a href="#" title="{description}"></a></li>'+
									        '					</tpl>													  '+
								        	'					<tpl if="name != \'detaildataset\' ">'+
									        ' 	       				<li class="{name}"><a href="#" title="{description}"></a></li>'+
									        '					</tpl>													  '+									        
									        '			</tpl>'+
									        '		</tpl>'+
								            '    </ul>'+
								            '</div>'+
								            '<tpl for="actions">'+   //TO OPTIMIZE WITHOUT CICLE ON ACTIONS!!!!
								            '	<tpl if="name == \'delete\'">'+
								            '		<a href="#" class="delete" title="{description}">Cancella</a>'+
								            '	</tpl>' +
								            '</tpl>' +
		                                '</div>',
									'</div>',
									'<div title="{name}" class="box-text">',
										'<h2>{name}</h2>',
										'<p>{[Ext.String.ellipsis(values.description, 100, false)]}</p>'+
										'<tpl if="dsTypeCd != \'Ckan\'">'+
											'<p><b>'+author+':</b> {owner}</p>'+
											'<p class="modified">'+changed+' {dateIn}</p>'+
										'</tpl>'+
									'</div>',
								'</div>',
//								'<div class="fav-container" >',
//									'<div class="fav"  title="Favourites">',
//									'    <span class="icon"><a href="#" onclick="alert(\'Functionality not supported yet!\');"/></span> '+
//									'</div>',
//								'</div>',
//								'<tpl if="dsTypeCd == \'Ckan\'">'+
//									'<div class="fav-container" >',
//							            '<tpl if="configuration.spagobi.isBookmarked == false">'+
//										'	<div class="bookmark"  title="'+LN('sbi.mydata.notbookmarkeddataset')+'">',
//										'   <span class="icon"></span> '+
//										'	</div>',
//							            '</tpl>'+
//							            '<tpl if="configuration.spagobi.isBookmarked == true">'+
//										'	<div class="bookmark"  title="'+LN('sbi.mydata.bookmarkeddataset')+'">',
//										'   <span class="iconActive"></span> '+
//										'	</div>',
//							            '</tpl>'+					            
//									'</div>',		
//								'</tpl>'+
								'<tpl if="owner == \''+currentUser+'\' && dsTypeCd != \'Ckan\'">'+
									'<div class="fav-container" >',
						            '<tpl if="isPublic == false">'+
									'	<div class="share"  title="'+LN('sbi.mydata.sharedataset')+'">',
									'    <a href="#"><span class="icon"></span></a> '+
									'	</div>',
						            '</tpl>'+
						            '<tpl if="isPublic == true">'+
									'	<div class="share"  title="'+LN('sbi.mydata.unsharedataset')+'">',
									'    <a href="#"><span class="iconActive"></span></a> '+
									'	</div>',
						            '</tpl>'+					            
									'</div>',		
								'</tpl>'+
							'</dd>',
							'<tpl if="xindex === xcount-1 && dsTypeCd == \'Ckan\'">'+
							'<div style="clear:left"></div>'+
			 				'<div><ul class="list-tab" id="footer-dataview"><li class="active" id="moreDatasets"><a href="#" onclick="javascript:Ext.getCmp(\'this\').moreDataset()">'+LN('sbi.mydata.ckandataset.more')+'</a></li></ul></div>'+
			 				'</tpl>'+
						 '</tpl>'+
						'<div style="clear:left"></div>'+
//					'</ul>',
//				'</div>',
			'</div>',{
			        	includeAction: function(n){
			        		var toReturn = true;
			        		if (Sbi.settings.mydata.hiddenActions && Sbi.settings.mydata.hiddenActions.length > 0){
			        			for (var i=0; i<Sbi.settings.mydata.hiddenActions.length; i++){
			        				var iconToHide = Sbi.settings.mydata.hiddenActions[i];
			        				if (iconToHide == n){
			        					toReturn = false;
			        					break;
			        				}
			        			}
			        		}
			        		
			        		return toReturn;
			        	}
			        		
			        }
		);
     
		Sbi.debug('DataViewPanel tpl built.');

		return this.tpl;
	}
	
	,
	onRender : function(obj, opt) {
		Ext.DataView.superclass.onRender.call(this, opt);
	}

	, 
	onClick : function(obj, record, item, index, e, eOpts) {
		var scope = this;

    	var actionDetail = e.getTarget('li[class=detaildataset]', 10, true);
    	var actionWorksheet = e.getTarget('li[class=worksheet]', 10, true);
    	var actionQbe = e.getTarget('li[class=qbe]', 10, true);
    	var actionGeoreport = e.getTarget('li[class=georeport]', 10, true);
        var actionInfo = e.getTarget('li[class=info]',10,true);				// for CKAN
        var actionDelete = e.getTarget('a[class=delete]', 10, true);
       // var actionFavourite = e.getTarget('span.icon', 10, true); //TBD
        
        var actionShareDataset = e.getTarget('div[class=share]',10,true);
        
        //if (!this.fromMyDataCtx) actionWorksheet = true;
        
        delete record.data.actions; 
        if (actionDetail != null){
        	Sbi.debug('DataSetView view detail raise event...');        	
        	scope.fireEvent('detail', record.data);   
        } else if (actionDelete != null){
        	Sbi.debug('DataSetView delete dataset raise event...');        	
        	scope.fireEvent('delete', record.data);
        } else if (actionWorksheet != null){
        	Sbi.debug('DataSetView actionWorksheet raise event...');
	        if (record.data.pars != undefined && record.data.pars != ''){
	        	Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.ds.noWorksheetDesigner'));
	        	return true;
	        }
	   		scope.fireEvent('executeDocument','WORKSHEET','DATASET',record);
        } else if (actionQbe != null){
        	Sbi.debug('DataSetView actionQbe raise event...'); 
	        if (record.data.pars != undefined && record.data.pars != ''){
	        	Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.ds.noQbeDesigner'));
	        	return true;
	        }
	   		scope.fireEvent('executeDocument','QBE','DATASET',record);
        } else if (actionGeoreport != null){
        	Sbi.debug('DataSetView actionGeoreport raise event...'); 
        	if (record.data.pars != undefined && record.data.pars != ''){
        		Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.ds.noGeoreportDesigner'));
        		return true;
        	}
   			scope.fireEvent('executeDocument','GEOREPORT','DATASET',record);
        } else if (actionShareDataset != null){
        	Sbi.debug('DataSetView actionShareDataset raise event...'); 
        	scope.fireEvent('share', record.data);
//        } else if (actionBookmarkDataset != null) {
//        	Sbi.debug('DataSetView actionBookmarkDataset raise event...'); 
//        	scope.fireEvent('detail', record.data);
        } else if (actionInfo != null) {
        	Sbi.debug('DataSetView actionInfo raise event...'); 
        	scope.fireEvent('info', record.data);
        	
        } /*else {
        	Sbi.debug('DataSetView default click event...'); 
        	if (record.data.pars != undefined && record.data.pars != ''){
        		Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.ds.noWorksheetDesigner'));
        		return true;
        	}
   			scope.fireEvent('executeDocument','WORKSHEET','DATASET',record);      	
        }*/
        
        
        return true;
    }

	,
	onMouseOverX : function( obj, record, item, index, e, eOpts ) {
		
		var group = e.getTarget('div[class=group-header]', 10, true);
		if (!group) {
			var d = e.getTarget('[class*=group-item]', 5, true);
			if (d) {
				var t = d.first('div[class*=item-control-panel]', false);
				if (t) {
					t.applyStyles('visibility:visible');
				}
			}
		}
		
		return true;
	}
	

	,
	onMouseOutX : function( obj, record, item, index, e, eOpts ) {
		var group = e.getTarget('div[class=group-header]', 10, true);
		if (!group) {
			var d = e.getTarget('[class*=group-item]', 5, true);
			if (d) {
				var t = d.first('div[class*=item-control-panel]', false);
				if (t) {
					t.applyStyles('visibility:hidden');
				}
			}
		}
		
		return true;
	}

	, isAction: function(o) {
	
		if((typeof o != undefined) && o != null && o=='delete'){
			return false;
		}else{
			return true;
		}
		
	}
	, isAbleToCreateDocument: function(o){
		if (o!='detail') return true;
		
    	var funcs = Sbi.user.functionalities;
    	if (funcs == null || funcs == undefined) return false;
    	
    	for (f in funcs){
    		if (funcs[f] == this.DETAIL_DOCUMENT || funcs[f] == this.CREATE_DOCUMENT){	    	    			
    			return true;
    			break;
    		}
    	}
    	
    	return false;
    }


});