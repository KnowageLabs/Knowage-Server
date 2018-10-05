/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

var geoM=angular.module('geoModule');
geoM.service('geoModule_ranges',function(){
	var self = this;
	var rangeableColumns = [];
	var tempData;

	self.getRangeableColumns = function(data, rangesNames){
		tempData = data;
		for(var k in data.metaData.fields){
			if(data.metaData.fields[k] && data.metaData.fields[k].role == "MEASURE"){
				self.getRanges(data.rows,data.metaData.fields[k].dataIndex,rangesNames);
			}
		}
		return tempData.rows;
	}
	
	self.getRanges = function(rows,columnIndex,rangesNames){
		var rangesNumber = rangesNames.length;
		var numbers = [];
		var ranges = [];
		for(var j in rows){
			if(!isNaN(parseFloat(rows[j][columnIndex]))){
				numbers.push(parseFloat(rows[j][columnIndex]));
			}
			if(j==rows.length-1){
				var max = Math.max.apply(Math,numbers);
				var min = Math.min.apply(Math,numbers);
				var rangeValue = (max-min)/rangesNumber;
				for(var k = 0;k<rangesNumber;k++){
					ranges.push({"min":min,"max":min+rangeValue});
					min = min+rangeValue;
					if(k==rangesNumber-1){
						self.assignRanges(columnIndex,ranges,rangesNames);
					}
				}
			}
		}
	}
	
	self.assignRanges = function(columnIndex,ranges,rangesNames){
		for(var y in tempData.rows){
			for(var k = 0;k<ranges.length;k++){
				if(parseFloat(tempData.rows[y][columnIndex])>=ranges[k].min && parseFloat(tempData.rows[y][columnIndex])<ranges[k].max){
					tempData.rows[y][columnIndex] = rangesNames[k];
				}if(ranges.length-1 == k && parseFloat(tempData.rows[y][columnIndex])>=ranges[k].max){
					tempData.rows[y][columnIndex] = rangesNames[ranges.length-1];
				}if(!tempData.rows[y][columnIndex]){
					tempData.rows[y][columnIndex] = "missing data";
				}
			}
		}
	}
	
	
});