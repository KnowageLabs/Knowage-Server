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

/**
 * @authors Radmila Selakovic (radmila.selakovic@eng.it)
 *
 */
angular.module("cockpitModule").service("cockpitModule_customWidgetServices",function(cockpitModule_datasetServices,sbiModule_util){
	this.metadata = [];
	this.dataset = null;

	this.getDataSet = function (id){
		return this.dataset;
	};

	this.setDataSet = function (dsId){
		this.dataset = cockpitModule_datasetServices.getDatasetById(dsId);
	};

	this.setMetadata = function (dsId){
		this.metadata = this.dataset.metadata.fieldsMeta;
	};

	this.getMetadata = function (){
		return this.metadata;
	};

	this.createColumnSelectedOfDataset = function(){
		var columnSelectedOfDataset = [];
		if(arguments.length==0){
			columnSelectedOfDataset = this.metadata;
		} else {
			for(var i=0; i<arguments.length; i++){

				var index = sbiModule_util.findInArray(this.metadata, 'alias', arguments[i]);
				if(index>-1){
					columnSelectedOfDataset.push(this.metadata[index]);
				}
			}
		}

		return columnSelectedOfDataset;
	}

});

