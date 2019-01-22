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
	
	self.htmlHelperJSON = function(datasetId,meta,parameters,cross,availableDatasets){
		return [
			{
				'label':'Columns Data',
				'name': 'column',
				'description': sbiModule_translate.load('kn.cockpit.html.tag1.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': 'No dataset selected',
				'inputs': [
					{	'name':'column',
						'type': 'select',
						'options': !datasetId || meta,
						'flex':'flex-100'},
					{	'name':'row',
						'type': 'number',
						'flex':'flex',
						'replacer':" row='***'"},
					{	'name':'precision',
						'type': 'number',
						'flex':'flex',
						'replacer':" precision='***'"}
				],
				'tag':"[kn-column='%%column%%'%%row%%%%precision%%]"},
			{
				'label':'Parameters',
				'name': 'parameter',
				'description':sbiModule_translate.load('kn.cockpit.html.tag2.desc'),
				'hidden': self.isEmpty(cockpitModule_analyticalDrivers),
				'hiddenMessage': 'no parameter available',
				'inputs': [
					{	'name':'parameter',
						'type': 'select',
						'options': parameters,
						'flex':'flex-100'}
				],
				'tag':"[kn-parameter='%%parameter%%']"},
			{
				'label':'Repeater',
				'name': 'repeater',
				'description': sbiModule_translate.load('kn.cockpit.html.tag3.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': 'No dataset selected',
				'inputs': [
					{	'name':'limit',
						'type': 'number',
						'flex':'flex-100',
						'replacer':'limit="***"'}
				],
				'tag':'<div kn-repeat="true" %%limit%%></div>'},
			{
				'label':'Repeat index',
				'name': 'repeatIndex',
				'description': sbiModule_translate.load('kn.cockpit.html.tag4.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': 'No dataset selected',
				'tag':'[kn-repeat-index]'
				},
			{
				'label':'Conditional container',
				'name': 'if',
				'description': sbiModule_translate.load('kn.cockpit.html.tag5.desc'),
				'tag':'<div kn-if="%%condition%%"></div>',
				'inputs': [
					{	'name':'condition',
						'type': 'area',
						'flex':'flex-100'}
				]},
			{
				'label':'Cross navigation',
				'name': 'cross',
				'description': sbiModule_translate.load('kn.cockpit.html.tag4.desc'),
				'hidden': self.isEmpty(cross) ? true : false,
				'hiddenMessage': 'No cross defined',
				'tag':'<div kn-cross></div>'
				},
			{
				'label':'Selection',
				'name': 'selection',
				'description': sbiModule_translate.load('kn.cockpit.html.tag4.desc'),
				'hidden': !datasetId ? true : false,
				'hiddenMessage': 'No dataset selected',
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
				'label':'Dataset Preview',
				'name': 'preview',
				'description': sbiModule_translate.load('kn.cockpit.html.tag4.desc'),
				'hidden': !availableDatasets ? true : false,
				'hiddenMessage': 'No dataset available',
				'tag':'<div kn-preview%%dataset%%></div>',
				'inputs': [
					{	'name':'dataset',
						'type': 'select',
						'flex':'flex',
						'replacer':'=\"***\"',
						'options': availableDatasets}]
				}
		]
	}
});