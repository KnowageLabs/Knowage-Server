/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.widgets");

/**
 * Class: Sbi.widgets.DatasetsBrowserPanel
 * A panel that shows the list of dataset available to the logged in user grouped by category.
 * In line sort and filter are also provided.
 */
Sbi.widgets.DatasetsBrowserPanel = function(config) {

	Sbi.trace("[DatasetsBrowserPanel.constructor]: IN");

	var defaultSettings = {
		autoScroll: false
	  , layout: "border"
	  , frame: false
	  , border: false
	};
	var settings = Sbi.getObjectSettings('Sbi.widgets.DatasetsBrowserPanel', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	this.filterOption = this.defaultFilterOption;
	Sbi.trace("[DatasetsBrowserPanel.constructor]: initially the list will be filtered on type [" + this.filterOption + "]");

	this.initServices();
	this.initDatasetStore();
	this.init();


	this.items = [{
		region      			: 'north',
		split       			: false,
		collapsible 			: false,
		collapsed   			: false,
		autoScroll				: true,
		layout					: 'fit',
		margins     			: '0 0 0 0',
		cmargins    			: '0 0 0 0',
		collapseMode			: 'mini',
        hideCollapseTool		: true,
		hideBorders				: true,
		border					: true,
		frame					: false,
		items					: [this.toolbar]
	}, {
		region      			: 'center',
		split       			: false,
		collapsible 			: false,
		collapsed   			: false,
		autoScroll				: true,
		layout					: 'fit',
		margins     			: '0 0 0 0',
		cmargins    			: '0 0 0 0',
		collapseMode			: 'mini',
        hideCollapseTool		: true,
		hideBorders				: true,
		border					: false,
		frame					: false,
		items					: [this.viewPanel]
	}];

	Sbi.widgets.DatasetsBrowserPanel.superclass.constructor.call(this, c);

	this.addEvents("select");

	this.on("render", function() {
		Sbi.trace("[DatasetsBrowserPanel.constructor]: panel succesfully rendered.");
		this.refresh();
	}, this);

	Sbi.trace("[DatasetsBrowserPanel.constructor]: IN");

};

Ext.extend(Sbi.widgets.DatasetsBrowserPanel, Ext.Panel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	id:'this'  // TODO remove

	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	, services: null

	/**
     * @property {Ext.Panel} toolbar
     * The inline toolbar that contains category buttons, filter and sort options
     */
	, toolbar: null

	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	, viewPanel: null


	/**
     * @property {Ext.data.Store} datasetStore
     * This store contains all the dataset visualized by this panel
     */
	, datasetStore: null

	/**
	 * @property {String} filterOption
     * The value of the filter options. Usually it is set by the user by means of one filter buttons contained in this panel toolbar.
     * Admissible values are: UsedDataSet, EnterpriseDataSet, SharedDataSet, AllDataSet.
	 */
	, filterOption: null
	, defaultFilterOption: Sbi.settings.mydata.defaultFilter || "UsedDataSet"

	, searchOption: null
	, defaultSearchOption: Sbi.settings.mydata.defaultSortOption || null

	, sortOption: null
	, defaultSortOption: Sbi.settings.mydata.defaultSortOption || "dateIn"


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, resetToolbarOptions: function() {
		this.resetFilter(true);
		this.resetSearch(true);
		this.resetSort(true);
	}

	, getUsedDatasets: function() {
		return this.viewPanel.getUsedDatasets();
	}

	, setUsedDatasets: function(datasets) {
		this.viewPanel.setUsedDatasets(datasets);
	}

	, getSelectedDataset: function() {
		var datasetLabel = null;
		if (this.viewPanel){
			datasetLabel = this.viewPanel.selectedDataset;
		}
		return datasetLabel;
	}

	, setSelectedDataset: function(datasetLabel) {
		Sbi.trace("[DatasetsBrowserPanel.setSelectedDataset]: IN");
		if (this.viewPanel){
			this.viewPanel.selectedDataset = datasetLabel;
			Sbi.trace("[DatasetsBrowserPanel.setSelectedDataset]: selected dataset set to [" + this.viewPanel.selectedDataset + "]");
		}
		Sbi.trace("[DatasetsBrowserPanel.setSelectedDataset]: OUT");
	}

	, getSelection: function() {
		return Sbi.isValorized(this.getSelectedDataset())? [this.getSelectedDataset()]: [];
	}

	/**
	 * @method
	 *
	 * If there is a selected dataset it deselects it and the force a GUI refresh
	 */
	, resetSelection: function() {
		Sbi.trace("[DatasetsBrowserPanel.resetDatasetSelection]: IN");
		if(this.getSelectedDataset() !== null) {
			Sbi.trace("[DatasetsBrowserPanel.resetDatasetSelection]: deselected dataset [" + this.getSelectedDataset() + "]");
			this.setSelectedDataset(null);
			this.refreshView();
		}
		Sbi.trace("[DatasetsBrowserPanel.resetDatasetSelection]: OUT");
	}

	/**
	 * @method
	 *
	 * Selects a dataset whose label is equal to parameter #datasetLabel. Then force a GUI refresh if parameter #updateControl is true.
	 *
	 * @param {String} datasetLabel The label of the dataset.
	 * @param {boolean} updateControl true to force the GUI refresh false otherwise. The default value is false.
	 */
	, select: function(datasetLabel, updateControl) {
		Sbi.trace("[DatasetsBrowserPanel.select]: IN");

		Sbi.trace("[DatasetsBrowserPanel.select]: dataset to select is equal to [" + datasetLabel + "]");
		Sbi.trace("[DatasetsBrowserPanel.select]: dataset currently selected is equal to [" + this.getSelectedDataset() + "]");

		var previouslySelectedDatasetLabel = this.getSelectedDataset();
		this.setSelectedDataset(datasetLabel);

	    if(updateControl === true) {
	    	this.unselectDatasetComponent(previouslySelectedDatasetLabel);
	    	this.selectDatasetComponent(this.getSelectedDataset());
	    }

	    Sbi.trace("[DatasetsBrowserPanel.select]: OUT");
	}

	/**
     * @method
     *
     * Filter the list of dataset contained in the DatasetBrowserView by dataset type type.
     *
     * @param {String} filterOption the dataset type to use for filtering the list. Admissible types
     * are: UsedDataSet, EnterpriseDataSet, SharedDataSet, AllDataSet
     */
	, filter: function(filterOption, updateControl) {
		Sbi.trace("[DatasetsBrowserPanel.filter]: IN");

		Sbi.trace("[DatasetsBrowserPanel.filter]: dataset filterOption parameter is equal to [" + filterOption + "]");

		if (filterOption == 'UsedDataSet' || filterOption == 'MyDataSet'
			|| filterOption == 'EnterpriseDataSet' || filterOption == 'SharedDataSet'
			|| filterOption == 'AllDataSet'){
			this.filterOption = filterOption;
			this.refresh();
			if(updateControl === true) {
				this.setFilterControl(filterOption);
			}

		} else {
			Sbi.trace("[DatasetsBrowserPanel.filter]: value [" + filterOption + "] is not a valid dataset type");
		}

		Sbi.trace("[DatasetsBrowserPanel.filter]: OUT");
	}

	, resetFilter: function(updateControl) {
		Sbi.trace("[DatasetsBrowserPanel.resetFilter]: IN");
		this.filterOption = this.defaultFilterOption;
		if(updateControl === true) {
			this.setFilterControl(this.filterOption);
		}
		Sbi.trace("[DatasetsBrowserPanel.resetFilter]: OUT");
	}

	, search: function(query, updateControl) {

		Sbi.trace("[DatasetsBrowserPanel.search]: IN");

		this.filteredProperties =  [ "label","name","description","owner" ];

		if(query!=null && query!=undefined && query!=''){
			this.datasetStore.filterBy(function(record,id){
				if(Sbi.isValorized(record.data)){
					var data = record.data;
					for(var p in data){
						if(this.filteredProperties.indexOf(p)>=0){//if the column should be considered by the filter
							if(data[p]!=null && data[p]!=undefined && ((""+data[p]).toUpperCase()).indexOf(query.toUpperCase())>=0){
								return true;
							}
						}
					}
				}
				return false;
			}, this);
		} else{
			this.datasetStore.clearFilter();
		}

		if(updateControl === true) {
			this.setSearchControl(query);
		}

		Sbi.trace("[DatasetsBrowserPanel.search]: OUT");
	}

	, resetSearch: function(updateControl) {
		Sbi.trace("[DatasetsBrowserPanel.resetSearch]: IN");
		this.searchOption = this.defaultSearchOption;
		this.search(this.searchOption, updateControl);
		Sbi.trace("[DatasetsBrowserPanel.resetSearch]: OUT");
	}

	, sort: function(sortOption, updateControl) {
		Sbi.trace("[DatasetsBrowserPanel.sort]: IN");

		this.sortOption = sortOption;

		var sorters = [{property : 'dateIn', direction: 'DESC', description: LN('sbi.ds.moreRecent')},
		               {property : 'label', direction: 'ASC', description:  LN('sbi.ds.label')},
		               {property : 'name', direction: 'ASC', description: LN('sbi.ds.name')},
		               {property : 'owner', direction: 'ASC', description: LN('sbi.ds.owner')}];


		for (sort in sorters){
			var s = sorters[sort];
			if (s.property == sortOption){
				this.datasetStore.sort(s.property, s.direction);
				break;
			}
		}

		this.refreshView();

		if(updateControl === true) {
			this.setSortControl(sortOption);
		}

		Sbi.trace("[DatasetsBrowserPanel.sort]: OUT");
	}

	, resetSort: function(updateControl) {
		this.sortOption = this.defaultSortOption;
		this.sort(this.sortOption, updateControl);
	}

	, refresh: function() {
		Sbi.trace("[DatasetsBrowserPanel.refreshDatasetList]: IN");

		this.initDatasetListService();
		this.initDatasetStore();

		this.datasetStore.on('load', function() {
			Sbi.trace("[DatasetsBrowserPanel.ondatasetStoreLoad]: IN");
			Sbi.trace("[DatasetsBrowserPanel.refreshDatasetList]: refreshing view panel ...");

			Sbi.trace("[DatasetsBrowserPanel.refreshDatasetList]: record in store [" + this.datasetStore.getTotalCount() + "]");


			if (this.viewPanel){
				this.viewPanel.filterOption = this.filterOption;
				this.viewPanel.bindStore(this.datasetStore);
				Sbi.trace("[DatasetsBrowserPanel.refreshDatasetList]: refreshing view panel 1");
				this.refreshView();

				Sbi.trace("[DatasetsBrowserPanel.refreshDatasetList]: refreshing view panel 2");
				Sbi.trace("[DatasetsBrowserPanel.ondatasetStoreLoad]: OUT");
			}
		}, this);

		this.datasetStore.load();

		Sbi.trace("[DatasetsBrowserPanel.refreshDatasetList]: OUT");
	}

	, refreshView: function() {
		Sbi.trace("[DatasetsBrowserPanel.refreshView]: IN");
		if(this.viewPanel && this.viewPanel.rendered === true) {
			this.viewPanel.refresh();
			Sbi.trace("[DatasetsBrowserPanel.refreshView]: view succesfully refreshed");
		}
		Sbi.trace("[DatasetsBrowserPanel.refreshView]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	/**
	 * @method
	 *
	 * Initialize the following services exploited by this component:
	 *
	 *    - list
	 */
	, initServices: function() {
		this.services = [];
		this.initDatasetListService();
	}

	, initDatasetListService: function(){
		Sbi.trace("[DatasetsBrowserPanel.initDatasetListService]: IN");
		if (this.filterOption == 'MyDataSet'){
			this.services["list"] = Sbi.config.serviceReg.getServiceUrl('loadOwnedDataSets', {
				queryParams : {typeDoc: this.typeDoc}
			});
		} else if (this.filterOption == 'EnterpriseDataSet'){
			this.services["list"] = Sbi.config.serviceReg.getServiceUrl('loadEnterpriseDataSets', {
				queryParams : {typeDoc: this.typeDoc}
			});
		} else if (this.filterOption == 'SharedDataSet'){
			this.services["list"] = Sbi.config.serviceReg.getServiceUrl('loadSharedDatasSets', {
				queryParams : {typeDoc: this.typeDoc}
			});
		} else if (this.filterOption == 'AllDataSet' || this.filterOption == 'UsedDataSet'){
			this.services["list"] = Sbi.config.serviceReg.getServiceUrl('loadMyDataDataSets', {
				queryParams : {typeDoc: this.typeDoc}
			});
		}
		Sbi.trace("[DatasetsBrowserPanel.initDatasetListService]: OUT");
	}

	, initDatasetStore: function() {
		Sbi.trace("[DatasetsBrowserPanel.initDatasetStore]: IN");

		Ext.define('DataSet', {
		    extend: 'Ext.data.Model',
		    fields: ["id",
			    	 	"label",
			    	 	"name",
			    	 	"description",
			    	 	"typeCode",
			    	 	"typeId",
			    	 	"encrypt",
			    	 	"visible",
			    	 	"engine",
			    	 	"engineId",
			    	 	"dataset",
			    	 	"stateCode",
			    	 	"stateId",
			    	 	"functionalities",
			    	 	"dateIn",
			    	 	"owner",
			    	 	"refreshSeconds",
			    	 	"isPublic",
			    	 	"actions",
			    	 	"exporters",
			    	 	"decorators",
			    	 	"previewFile",
			    	 	"isUsed", 	/*local property*/
			    	 	"myDSLabel" /*local property*/
			    	]
		});

		this.datasetStore =  Ext.create('Ext.data.Store', {
		    model: 'DataSet',
		    proxy: {
		        type: 'ajax',
		        url : this.services['list'],
		        reader: {
		        	type: 'json',
		        	root: 'root'
		        }
		    }
		});

//		this.datasetStore = new Ext.data.JsonStore({
//			 url: this.services['list']
//			 , filteredProperties : this.filteredProperties
//			 , sorters: []
//			 , root: 'root'
//			 , fields: ["id",
//			    	 	"label",
//			    	 	"name",
//			    	 	"description",
//			    	 	"typeCode",
//			    	 	"typeId",
//			    	 	"encrypt",
//			    	 	"visible",
//			    	 	"engine",
//			    	 	"engineId",
//			    	 	"dataset",
//			    	 	"stateCode",
//			    	 	"stateId",
//			    	 	"functionalities",
//			    	 	"dateIn",
//			    	 	"owner",
//			    	 	"refreshSeconds",
//			    	 	"isPublic",
//			    	 	"actions",
//			    	 	"exporters",
//			    	 	"decorators",
//			    	 	"previewFile",
//			    	 	"isUsed", 	/*local property*/
//			    	 	"myDSLabel" /*local property*/]
//		});

		Sbi.trace("[DatasetsBrowserPanel.initDatasetStore]: OUT");
	}

	/**
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function() {
		Sbi.trace("[DatasetsBrowserPanel.init]: IN");
		this.initToolbar();
		this.initViewPanel();
		Sbi.trace("[DatasetsBrowserPanel.init]: OUT");
	}


	/**
	 * @method
	 *
	 * Initialize the toolbar
	 */
	, initToolbar: function() {

		Sbi.trace("[DatasetsBrowserPanel.initToolbar]: IN");

		var toolbarInnerHtml = this.getToolbarInnerHtml({});
		this.toolbar = new Ext.Panel({
			height: 70,
			border: false,
		   	autoScroll: false,
		   	html: toolbarInnerHtml
		});

		Sbi.trace("[DatasetsBrowserPanel.initToolbar]: OUT");
	}


	/**
	 * @method
	 *
	 * Create the inner html of the toolbar
	 *
	 * @param {???} communities ???
	 *
	 * @return {String} the html code of the toolbar
	 */
	, getToolbarInnerHtml: function(communities){
		var activeClass = '';

        var  toolbarInnerHtml = ''+
     		'<div class="main-datasets-list"> '+
    		'    <div class="list-actions-container"> ';

        toolbarInnerHtml += this.getDatasetFilterControlHtml();
        toolbarInnerHtml +=
    		'	    <div id="list-actions" class="list-actions"> '+
    		this.getDatasetSearchControlHtml() +
    		this.getDatasetSortControlHtml() +
    		'	    </div> '+
    		'	</div> '+
    		'</div>' ;

        return toolbarInnerHtml;
    }

	, getDatasetFilterControlHtml: function() {

		var buttonsHtml = '';

		buttonsHtml += '<ul class="list-tab" id="list-tab"> ';

		if (Sbi.settings.mydata.showMyDataSetFilter){
	     	if (this.filterOption == 'MyDataSet'){
	     		activeClass = 'active';
	     	} else {
	     		activeClass = '';
	     	}
	     	buttonsHtml +=
	     	'<li class="first '+activeClass+'" id="MyDataSet"><a href="#" onclick="javascript:Ext.getCmp(\'this\').filter( \'MyDataSet\', true)">'+LN('sbi.mydata.mydataset')+'</a></li> ';
	     }

	     if (Sbi.settings.mydata.showEnterpriseDataSetFilter){
	     	if (this.filterOption == 'EnterpriseDataSet'){
	     		activeClass = 'active';
	     	} else {
	     		activeClass = '';
	     	}
	     	buttonsHtml +=
	 		'	    	<li class="'+activeClass+'" id="EnterpriseDataSet"><a href="#" onclick="javascript:Ext.getCmp(\'this\').filter( \'EnterpriseDataSet\', true)">'+LN('sbi.mydata.enterprisedataset')+'</a></li> ';
	     }
	     if (Sbi.settings.mydata.showSharedDataSetFilter){
	      	if (this.filterOption == 'SharedDataSet'){
	     		activeClass = 'active';
	     	} else {
	     		activeClass = '';
	     	}
	      	buttonsHtml +=
	  		'	    	<li class="'+activeClass+'" id="SharedDataSet"><a href="#" onclick="javascript:Ext.getCmp(\'this\').filter( \'SharedDataSet\', true)">'+LN('sbi.mydata.shareddataset')+'</a></li> ';
	     }

	     if (Sbi.settings.mydata.showAllDataSetFilter){
	       	if (this.filterOption == 'AllDataSet'){
	     		activeClass = 'active';
	     	} else {
	     		activeClass = '';
	     	}
	       	buttonsHtml +=
	 		'	    	<li id="AllDataSet" class="last '+activeClass+'"><a href="#" onclick="javascript:Ext.getCmp(\'this\').filter( \'AllDataSet\', true)">'+LN('sbi.mydata.alldataset')+'</a></li> ';
	     }
	     if (Sbi.settings.mydata.showUsedDataSetFilter){
		    	if (this.filterOption == 'UsedDataSet'){
		    		activeClass = 'active';
		    	} else {
		    		activeClass = '';
		    	}
		    	buttonsHtml +=
		    	'	    	<li class="first '+activeClass+'" id="UsedDataSet"><a href="#" onclick="javascript:Ext.getCmp(\'this\').filter( \'UsedDataSet\', true)">'+LN('sbi.mydata.useddataset')+'</a></li> ';
	      }

	      buttonsHtml+= '</ul>';

	      return buttonsHtml;
	}

	, getDatasetSearchControlHtml: function() {
		var html =
			'	        <form action="#" method="get" class="search-form"> '+
    		'	            <fieldset> '+
    		'	                <div class="field"> '+
    		'	                    <label for="search">'+LN('sbi.browser.document.searchDatasets')+'</label> '+
    		'	                    <input type="text" name="search" id="search" onclick="this.value=\'\'" onkeyup="javascript:Ext.getCmp(\'this\').search(this.value)" value="'+LN('sbi.browser.document.searchKeyword')+'" /> '+
    		'	                </div> '+
    		'	                <div class="submit"> '+
    		'	                    <input type="text" value="Cerca" /> '+
    		'	                </div> '+
    		'	            </fieldset> '+
    		'	        </form> ';

    	return html;
	}

	, getDatasetSortControlHtml: function() {
		var html =
			'	        <ul class="order" id="sortList">'+
    		'	            <li id="dateIn" class="active"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sort(\'dateIn\')">'+LN('sbi.ds.moreRecent')+'</a> </li> '+
    		'	            <li id="name"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sort(\'name\')">'+LN('sbi.generic.name')+'</a></li> '+
    		'	            <li id="owner"><a href="#" onclick="javascript:Ext.getCmp(\'this\').sort(\'owner\')">'+LN('sbi.generic.owner')+'</a></li> '+
    		'	        </ul> ';

		return html;
	}

	, initViewPanel: function() {

		Sbi.trace("[DatasetsBrowserPanel.initViewPanel]: IN");

		var config = {};
		//config.services = this.services;
		config.store = this.datasetStore;
		config.selectedDataset = this.getSelectedDataset();
		config.actions = this.actions;
		config.user = this.user;
		config.fromMyDataCtx = this.displayToolbar;
		config.filterOption = this.filterOption;

		this.viewPanel = new Sbi.widgets.DatasetsBrowserView(config);
		this.viewPanel.addListener('itemclick', this.onClick, this);

		Sbi.trace("[DatasetsBrowserPanel.initViewPanel]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // GUI utils
	// -----------------------------------------------------------------------------------------------------------------

	// TODO : the followings methods work only on the gui. They do not modify the state

	, selectDatasetComponent: function(datasetLabel) {
		Sbi.trace("[DatasetsBrowserPanel.selectDatasetComponent]: IN");
		this.viewPanel.selectDatasetComponent(datasetLabel);
		Sbi.trace("[DatasetsBrowserPanel.selectDatasetComponent]: OUT");
	}

	, unselectDatasetComponent: function(datasetLabel) {
		Sbi.trace("[DatasetsBrowserPanel.selectDatasetComponent]: IN");
		this.viewPanel.unselectDatasetComponent(datasetLabel);
		Sbi.trace("[DatasetsBrowserPanel.selectDatasetComponent]: OUT");
	}


	, setFilterControl: function(filterOption) {
		Sbi.trace("[DatasetsBrowserPanel.setFilterControl]: IN");
		if(this.rendered === true) {
			if (Ext.get('list-tab') != null){
				var tabEls = Ext.get('list-tab').dom.childNodes;

				//Change active dataset type on toolbar
				for(var i=0; i< tabEls.length; i++){
					//nodeType == 1 is  Node.ELEMENT_NODE
					if (tabEls[i].nodeType == 1){
						if (tabEls[i].id === filterOption){
							tabEls[i].className += ' active '; //append class name to existing others
						} else {
							tabEls[i].className = tabEls[i].className.replace( /(?:^|\s)active(?!\S)/g , '' ); //remove active class
						}
					}
				}
			}
			Sbi.trace("[DatasetsBrowserPanel.setFilterControl]: filter control succesfully set");
		}
		Sbi.trace("[DatasetsBrowserPanel.setFilterControl]: OUT");
	}

	, resetFilterControl: function() {
		Sbi.trace("[DatasetsBrowserPanel.resetFilterControl]: IN");
		this.setFilterControl(null);
		Sbi.trace("[DatasetsBrowserPanel.resetFilterControl]: OUT");
	}

	, setSearchControl: function(searchOption) {
		Sbi.trace("[DatasetsBrowserPanel.setSearchControl]: IN");
		if(this.rendered === true) {
			var inputField = Ext.get('search').dom;
			inputField.value = searchOption || '';
			Sbi.trace("[DatasetsBrowserPanel.setSearchControl]: search control succesfully set");
		}
		Sbi.trace("[DatasetsBrowserPanel.setSearchControl]: OUT");
	}

	, resetSearchControl: function() {
		Sbi.trace("[DatasetsBrowserPanel.resetSearchControl]: IN");
		this.setSearchControl(null);
		Sbi.trace("[DatasetsBrowserPanel.resetSearchControl]: OUT");
	}

	, setSortControl: function(sortOption) {
		Sbi.trace("[DatasetsBrowserPanel.setSortControl]: IN");
		if(this.rendered === true) {
			var sortEls = Ext.get('sortList').dom.childNodes;

			//move the selected value to the first element
			for(var i=0; i< sortEls.length; i++){
				if (sortEls[i].id === sortOption){
					sortEls[i].className = 'active';
					break;
				}
			}
			//append others elements
			for(var i=0; i< sortEls.length; i++){
				if (sortEls[i].id !== sortOption){
					sortEls[i].className = '';
				}
			}

			Sbi.trace("[DatasetsBrowserPanel.setSortControl]: sort control succesfully set");
		}
		Sbi.trace("[DatasetsBrowserPanel.setSortControl]: OUT");
	}

	, resetSortControl: function() {
		Sbi.trace("[DatasetsBrowserPanel.resetSortControl]: IN");
		this.setSortControl(null);
		Sbi.trace("[DatasetsBrowserPanel.resetSortControl]: OUT");
	}



	// -----------------------------------------------------------------------------------------------------------------
    // General utils
	// -----------------------------------------------------------------------------------------------------------------

	, onClick : function(obj, i, node, e) {
		Sbi.trace("[DatasetsBrowserPanel.onClick]: IN");

		var store = obj.getStore();
		var record = store.getAt(store.findExact('label',node.id));
		if (record){
			record = record.data;
		}
		var clickedDatasetLabel = record.label;
		Sbi.trace("[DatasetsBrowserPanel.onClick]: clicked dataset label is equal to [" + clickedDatasetLabel + "]");
		Sbi.trace("[DatasetsBrowserPanel.onClick]: selected dataset label is equal to [" + this.getSelectedDataset() + "]");

		if (clickedDatasetLabel == this.getSelectedDataset()){  // it's an unselect
			this.resetSelection();
	    } else {
	    	this.select(clickedDatasetLabel, true);
	    }

		Sbi.trace("[DatasetsBrowserPanel.onClick]: OUT");

    	return true;
	}




});
