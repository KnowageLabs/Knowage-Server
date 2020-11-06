/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

angular.module('cockpitModule').service('cockpitModule_helperDescriptors',function(sbiModule_translate,cockpitModule_analyticalDrivers){
	var self=this;
	self.isEmpty = function(obj) {
	    for(var key in obj) {
	        if(obj.hasOwnProperty(key)) return false;
	    }return true;
	}

	self.htmlHelperJSON = function(datasetId,meta,parameters,aggregations,cross,availableDatasets,variables){
		return [
			{
				'label':sbiModule_translate.load('kn.cockpit.html.tag1'),
				'name': 'column',
				'description': sbiModule_translate.load('kn.cockpit.html.tag1.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': sbiModule_translate.load('kn.cockpit.html.nodataset'),
				'inputs': [
					{	'name':'column',
						'type': 'select',
						'options': !datasetId || meta,
						'flex':'flex-100'},
					{	'name':'aggregation',
						'type': 'select',
						'options': aggregations,
						'flex':'flex-100',
						'replacer':" aggregation='***'"},
					{	'name':'row',
						'type': 'number',
						'flex':'flex',
						'replacer':" row='***'"},
					{	'name':'precision',
						'type': 'number',
						'flex':'flex',
						'replacer':" precision='***'"},
					{	'name':'format',
						'label': 'Format to locale',
						'type': 'check',
						'flex':'flex-100',
						'replacer':" format"

					}
				],
				'tag':"[kn-column='%%column%%'%%row%%%%aggregation%%%%precision%%%%format%%]"},
			{
				'label':sbiModule_translate.load('kn.cockpit.html.tag2'),
				'name': 'parameter',
				'description':sbiModule_translate.load('kn.cockpit.html.tag2.desc'),
				'hidden': self.isEmpty(parameters),
				'hiddenMessage': 'no parameter available',
				'inputs': [
					{	'name':'parameter',
						'type': 'select',
						'options': parameters,
						'flex':'flex-100'}
				],
				'tag':"[kn-parameter='%%parameter%%']"},
			{
				'label':sbiModule_translate.load('kn.cockpit.html.tag3'),
				'name': 'repeater',
				'description': sbiModule_translate.load('kn.cockpit.html.tag3.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': sbiModule_translate.load('kn.cockpit.html.nodataset'),
				'inputs': [
					{	'name':'limit',
						'type': 'number',
						'flex':'flex-100',
						'replacer':'limit="***"'}
				],
				'tag':'<div kn-repeat="true" %%limit%%></div>'},
			{
				'label':sbiModule_translate.load('kn.cockpit.html.tag4'),
				'name': 'repeatIndex',
				'description': sbiModule_translate.load('kn.cockpit.html.tag4.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': sbiModule_translate.load('kn.cockpit.html.nodataset'),
				'tag':'[kn-repeat-index]'
				},
			{
				'label':sbiModule_translate.load('kn.cockpit.html.tag5'),
				'name': 'if',
				'description': sbiModule_translate.load('kn.cockpit.html.tag5.desc'),
				'tag':'<div kn-if="%%condition%%"></div>',
				'inputs': [
					{	'name':'condition',
						'type': 'area',
						'flex':'flex-100'}
				]},
				{
					'label':sbiModule_translate.load('kn.cockpit.html.tag8'),
					'name': 'calc',
					'description': sbiModule_translate.load('kn.cockpit.html.tag8.desc'),
					'inputs': [
						{   'name':'calc',
							'type': 'area',
							'flex':'flex-100'},
					 	{   'name':'min',
							'type': 'text',
							'flex':'flex',
							'replacer':" min='***'"},
					 	{   'name':'max',
							'type': 'text',
							'flex':'flex',
							'replacer':" max='***'"},
					 	{   'name':'precision',
							'type': 'number',
							'flex':'flex',
							'replacer':" precision='***'"},
						{   'name':'format',
							'label': 'Format to locale',
							'type': 'check',
							'flex':'flex-100',
					 		'replacer':" format"
						}
					],
				 	'tag':"[kn-calc=(%%calc%%)%%min%%%%max%%%%precision%%%%format%%]"
				 },

			{
				'label':sbiModule_translate.load('kn.cockpit.html.tag6'),
				'name': 'cross',
				'description': sbiModule_translate.load('kn.cockpit.html.tag6.desc'),
				'tag':'<div kn-cross></div>'
				},
			{
				'label':sbiModule_translate.load('kn.cockpit.html.tag7'),
				'name': 'selection',
				'description': sbiModule_translate.load('kn.cockpit.html.tag7.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': sbiModule_translate.load('kn.cockpit.html.nodataset'),
				'tag':'<div kn-selection-column="%%selectioncolumn%%" %%selectionvalue%%></div>',
				'inputs': [
					{	'name':'selectioncolumn',
						'type': 'select',
						'flex':'flex-100',
						'options': !datasetId || meta},
					{	'name':'selectionvalue',
						'type': 'text',
						'flex':'flex-100',
						'replacer':'kn-selection-value=\"***\"'}
				]
				},
			{
				'label':sbiModule_translate.load('kn.cockpit.html.tag9'),
				'name': 'preview',
				'description': sbiModule_translate.load('kn.cockpit.html.tag9.desc'),
				'hidden': !availableDatasets ? true : false,
				'hiddenMessage': sbiModule_translate.load('kn.cockpit.html.nodatasetavailable'),
				'tag':'<div kn-preview%%dataset%%></div>',
				'inputs': [
					{	'name':'dataset',
						'type': 'select',
						'flex':'flex',
						'replacer':'=\"***\"',
						'options': availableDatasets}]
				},
			{
				'label':sbiModule_translate.load('kn.cockpit.html.tag10'),
				'name': 'active-selection',
				'description': sbiModule_translate.load('kn.cockpit.html.tag10.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': sbiModule_translate.load('kn.cockpit.html.nodataset'),
				'tag':"[kn-active-selection='%%column%%']",
				'inputs': [
					{	'name':'column',
						'type': 'select',
						'options': !datasetId || meta,
						'flex':'flex-100'}]
				},
			{
				'label':sbiModule_translate.load('kn.cockpit.html.tag11'),
				'name': 'variable',
				'description': sbiModule_translate.load('kn.cockpit.html.tag11.desc'),
				'hidden': !variables ? true : false,
				'hiddenMessage': 'no variable present',
				'tag':"[kn-variable='%%variable%%']",
				'inputs': [
					{	'name':'variable',
						'type': 'select',
						'flex':'flex-100',
						'options': variables}]
			},{
				'label':sbiModule_translate.load('kn.cockpit.html.tag12'),
				'name': 'internationalization',
				'description': sbiModule_translate.load('kn.cockpit.html.tag12.desc'),
				'tag':"[kn-i18n='%%i18n%%']",
				'inputs': [
					{	'name':'i18n',
						'type': 'text',
						'flex':'flex-100'}]
				}
		]
	}

	self.pythonHelperJSON = function(datasetId,datasetLabel,meta,drivers,aggregations,cross,availableDatasets){
		return [
			{
				'label':sbiModule_translate.load('kn.cockpit.python.tag1'),
				'name': 'column',
				'description': sbiModule_translate.load('kn.cockpit.python.tag1.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': sbiModule_translate.load('kn.cockpit.python.nodataset'),
				'inputs': [
					{	'name':'column',
						'type': 'select',
						'options': !datasetId || meta,
						'flex':'flex-100'}
				],
				'tag': datasetLabel + "[\"%%column%%\"]"},
			{
				'label':sbiModule_translate.load('kn.cockpit.python.tag2'),
				'name': 'driver',
				'description':sbiModule_translate.load('kn.cockpit.python.tag2.desc'),
				'hidden': self.isEmpty(cockpitModule_analyticalDrivers),
				'hiddenMessage': 'no analytical driver available',
				'inputs': [
					{	'name':'driver',
						'type': 'select',
						'options': drivers,
						'flex':'flex-100'}
				],
				'tag':"$P{%%driver%%}"}
		]
	}

	self.rHelperJSON = function(datasetId,datasetLabel,meta,drivers,aggregations,cross,availableDatasets){
		return [
			{
				'label':sbiModule_translate.load('kn.cockpit.R.tag1'),
				'name': 'column',
				'description': sbiModule_translate.load('kn.cockpit.R.tag1.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': sbiModule_translate.load('kn.cockpit.R.nodataset'),
				'inputs': [
					{	'name':'column',
						'type': 'select',
						'options': !datasetId || meta,
						'flex':'flex-100'}
				],
				'tag': datasetLabel + "[\"%%column%%\"]"},
			{
				'label':sbiModule_translate.load('kn.cockpit.R.tag2'),
				'name': 'driver',
				'description':sbiModule_translate.load('kn.cockpit.R.tag2.desc'),
				'hidden': self.isEmpty(cockpitModule_analyticalDrivers),
				'hiddenMessage': 'no analytical driver available',
				'inputs': [
					{	'name':'driver',
						'type': 'select',
						'options': drivers,
						'flex':'flex-100'}
				],
				'tag':"$P{%%driver%%}"}
		]
	}
});