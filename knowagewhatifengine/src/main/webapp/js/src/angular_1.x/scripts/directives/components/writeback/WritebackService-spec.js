
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

/*
 * @author Dragan Pirkovic
 * Test  for WritebackService.js
 */


'use strict';

var localWritebackService ;

var editableMeasures = ["Sales Edit","Sales Unit"];

var lastEditedCellid = '2';



beforeEach(module('writeback'));



beforeEach(inject(function(_WritebackService_) {

	localWritebackService = _WritebackService_;

    
}));

describe('Testing WritebackService', function () {
	
	
	
	it('should return true',function(){
		
		
		expect(localWritebackService.test()).toBe(true);
	});
	
	it('should return true',function(){
		
		var measureName = "Sales Unit";
		expect(localWritebackService.isMeasureEditable(measureName,editableMeasures)).toBe(true);
	});
	
	it('should set lastEditedFormula to "1+1"',function(){
		
		var lastEditedFormula = "1+1";
		localWritebackService.setLastEditedFormula(lastEditedFormula);
		expect(localWritebackService.getLastEditedFormula()).toBe("1+1");
	});
	
	it('should set lastEditedCellId to "2"',function(){
		
		var lastEditedCellId = '2';
		localWritebackService.setLastEditedCellId(lastEditedCellId);
		expect(localWritebackService.getLastEditedCellId()).toBe("2");
	});
	
	
	
})