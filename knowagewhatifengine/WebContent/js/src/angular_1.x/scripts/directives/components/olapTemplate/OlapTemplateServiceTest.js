
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
 * Test  for OlapTemplateService.js
 */


'use strict';

	var localOlapTemplateService;
	
	var validMdxQueryObj = {mdxQuery:'SELECT Subset({[Measures].[Unit Sales]},0,50) ON COLUMNS,Subset({[Product]},24,50) ON ROWS FROM [Sales_V] WHERE CrossJoin([Customers].[Canada], [Region].[Mexico West])'};
	
	var invalidMdxQueryObj = {mxQuery:'this is query'};
	
	var anaDrParams = [
	                  
	      			{
	      					memberUniqueName:"[Customers].[Canada]",
	      					replaceName:"[Customers].[${state}]",
	      					parameter:{as:"twoUrl",name:"two"}
	      			},
	      			{
	      				memberUniqueName:"[Region].[Mexico West]",
	      				replaceName:"[Region].[${name}]"
	      			}
	      			
	      			]
	var anaDrParamsInvalid = [
		                  
		      			{
		      					memberUniquName:"[Customers].[Canada]",
		      					replaceName:"[Customers].[${state}]",
		      					parameter:{as:"twoUrl",name:"two"}
		      			},
		      			{
		      				memberUniquName:"[Region].[Mexico West]",
		      				replaceName:"[Region].[${name}]"
		      			}
		      			
		      			]
	var validClickables = [
	                       {
        
        					uniqueName:"[Product].[Product Family]",
        					clickParameter:{"name":"ParProdFamily","value":"{0}"}
	                       
    						},
    						{
        
    						uniqueName:"[Product].[Product Name]",
    						clickParameter:{"name":"ParProdFamily","value":"{0}"}
    						
    						}
    						]
	
	var invalidClickables = [
		                       {
		                           
		        					uniqueName:"[Product].[Product Family]",
		        					clickParameter:{"name":"ParProdFamily","value":"{0}"}
			                       
		    						},
		    						{
		        
		    						uniqueName:"[Product].[Product Name]",
		    						clickParameter:{"name":"ParProdFamily","value":"{0}"}
		    						
		    						}
		    					]
	
	var validScenario = {
		      name: "scenario",
		      editCube: "Sales_Edit",
		      initialVersion: "1",
		      measures: [
		       {name:"Store Sales"},
		       {name:"Store Cost"}
		      ],
		      variables: [
		        {name: "var",value: "5"},
		        {name: "PD"	,value: "[Product].[Drink.Dairy]",type: "string"}
		      ]
		    }
	var scenarioWithoutEditCube = {
		      
		      initialVersion: "1",
		      measures: [
		       {name:"Store Sales"},
		       {name:"Store Cost"}
		      ],
		      variables: [
		        {name: "var",value: "5"},
		        {name: "PD"	,value: "[Product].[Drink.Dairy]",type: "string"}
		      ]
		    }
	var scenarioWithoutMeasures = {
			  
			  editCube: "Sales_Edit",
		   
		    }
	
	var scenarioBadMeasureName = {
		      name: "scenario",
		      editCube: "Sales_Edit",
		      initialVersion: "1",
		      measures: [
		       {names:"Store Sales"},
		       {name:"Store Cost"}
		      ],
		      variables: [
		        {name: "var",value: "5"},
		        {name: "PD"	,value: "[Product].[Drink.Dairy]",type: "string"}
		      ]
		    }
	var scenarioEmptyMeasures = {
		      name: "scenario",
		      editCube: "Sales_Edit",
		      initialVersion: "1",
		      measures: [
		      
		      ],
		      variables: [
		        {name: "var",value: "5"},
		        {name: "PD"	,value: "[Product].[Drink.Dairy]",type: "string"}
		      ]
		    }
	
	var scenarioEmptyVariables = {
		      name: "scenario",
		      editCube: "Sales_Edit",
		      initialVersion: "1",
		      measures: [
		       {name:"Store Sales"},
		       {name:"Store Cost"}
		      ],
		      variables: []
		    }
	
	var scenarioBadFormatVariables = {
		      name: "scenario",
		      editCube: "Sales_Edit",
		      initialVersion: "1",
		      measures: [
		       {name:"Store Sales"},
		       {name:"Store Cost"}
		      ],
		      variables: [{names: "var",value: "5"},
		  		        {names: "PD"	,value: "[Product].[Drink.Dairy]",type: "string"}]
		    }

	beforeEach(module('olap_template'));
	
	

	beforeEach(inject(function(_OlapTemplateService_) {

		localOlapTemplateService = _OlapTemplateService_;

	    
	}));

	describe('Testing OlapTemplateService', function () {
		
		
		
		it('should been defined',function(){
			
			
			expect(localOlapTemplateService).toBeDefined();
		});
		
		describe('olap property', function () {
			
			
			it('should been defined',function(){
				
				var olapTag = localOlapTemplateService.getOlapTag();
				
				expect(olapTag).toBeDefined();
			});
			
			it('should not been defined',function(){
				
				localOlapTemplateService.deleteOlapTag();
				var olapTag = localOlapTemplateService.getOlapTag();
				
				expect(olapTag).not.toBeDefined();
			});
		})
		
			describe('cube property', function () {
			
				it('should set object with property reference Sales',function(){
				
					localOlapTemplateService.setCubeTag("Sales");
					var cubeTag = localOlapTemplateService.getCubeTag();
					
					var jsonTemplateTagValue = localOlapTemplateService.getJsonTemplateTag().XML_TAG_TEXT_CONTENT;
					localOlapTemplateService.deleteJsonTemplateTag();
					expect(localOlapTemplateService.getTemplateObject()).toEqual(jsonTemplateTagValue);
					
					expect(localOlapTemplateService.getCubeReference()).toEqual('Sales');
				});
			
				it('should return false on cube name "" and delete property',function(){
				
					var success = localOlapTemplateService.setCubeTag("");
				
					expect(success).toBe(false);
					expect(localOlapTemplateService.getCubeTag()).not.toBeDefined();
				});
			
			})
	
			describe('MDXQUERY property', function () {
			
				it('should set and return true',function(){
				
					var success = localOlapTemplateService.setMdxQueryTag(validMdxQueryObj);
					
					var jsonTemplateTagValue = localOlapTemplateService.getJsonTemplateTag().XML_TAG_TEXT_CONTENT;
					localOlapTemplateService.deleteJsonTemplateTag();
					expect(localOlapTemplateService.getTemplateObject()).toEqual(jsonTemplateTagValue);
					expect(localOlapTemplateService.getMdxQueryTag()).toBeDefined();
					expect(success).toBe(true);
					
				});
			
				it('should return false: invalid input parameter',function(){
				
					var success = localOlapTemplateService.setMdxQueryTag(invalidMdxQueryObj);
					expect(success).toBe(false);
					expect(localOlapTemplateService.getMdxQuery()).not.toBeDefined();
				});
			
			
				describe('parameters', function () {
				
					it('should be injected in mdxQuery',function(){
					
						var expectedMdxQuery = 'SELECT Subset({[Measures].[Unit Sales]},0,50) ON COLUMNS,Subset({[Product]},24,50) ON ROWS FROM [Sales_V] WHERE CrossJoin([Customers].[${state}], [Region].[${name}])';
						var params =[];
						var success = false;
						localOlapTemplateService.setMdxQueryTag(validMdxQueryObj);
						success = localOlapTemplateService.injectParametersToMdxQueryTag(anaDrParams);
						params = localOlapTemplateService.getAnaliticalDriverParams();
						var jsonTemplateTagValue = localOlapTemplateService.getJsonTemplateTag().XML_TAG_TEXT_CONTENT;
						localOlapTemplateService.deleteJsonTemplateTag();
						
					expect(localOlapTemplateService.getTemplateObject()).toEqual(jsonTemplateTagValue);
					expect(success).toEqual(true);
					expect(params.length).toEqual(1);
					expect(params[0].as).toEqual('twoUrl');
					expect(params[0].name).toEqual('two');
					expect(localOlapTemplateService.getMdxQuery()).toEqual(expectedMdxQuery);
					
					});
				
					it('should reject parameters and return false: mdxQuery is not defined',function(){
					
						var expectedMdxQuery = 'SELECT Subset({[Measures].[Unit Sales]},0,50) ON COLUMNS,Subset({[Product]},24,50) ON ROWS FROM [Sales_V] WHERE CrossJoin([Customers].[${state}], [Region].[${name}])';
						var success = localOlapTemplateService.injectParametersToMdxQueryTag(anaDrParams);
						expect(success).toBe(false);
					
					});
					
					it('should reject parameters and return false: mandatory properties:memberUniqueName,replaceName',function(){
						localOlapTemplateService.setMdxQueryTag(validMdxQueryObj);
						var expectedMdxQuery = 'SELECT Subset({[Measures].[Unit Sales]},0,50) ON COLUMNS,Subset({[Product]},24,50) ON ROWS FROM [Sales_V] WHERE CrossJoin([Customers].[${state}], [Region].[${name}])';
						var success = localOlapTemplateService.injectParametersToMdxQueryTag(anaDrParamsInvalid);
						var params = localOlapTemplateService.getAnaliticalDriverParams();
						var expectedParams = [];
						
						expect(params).toEqual(expectedParams);
						expect(success).toBe(false);
					
					});
				
				})
			
				describe('clickable array property', function () {
				
					it('should be set and return true',function(){
					
						localOlapTemplateService.setMdxQueryTag(validMdxQueryObj);
						var success = localOlapTemplateService.setClickableTag(validClickables);
						var jsonTemplateTagValue = localOlapTemplateService.getJsonTemplateTag().XML_TAG_TEXT_CONTENT;
						localOlapTemplateService.deleteJsonTemplateTag();
						
						expect(localOlapTemplateService.getTemplateObject()).toEqual(jsonTemplateTagValue);
						expect(success).toBe(true);
					})
				
					it('should return false:MDXQUERY is not defined',function(){
			
						var success = localOlapTemplateService.setClickableTag(validClickables);
						expect(localOlapTemplateService.getMdxQueryClickables()).not.toBeDefined();
						expect(success).toBe(false);
					})
		
					it('should return false:input parameter is not array',function(){
			
						localOlapTemplateService.setMdxQueryTag(validMdxQueryObj);
						var success = localOlapTemplateService.setClickableTag(invalidMdxQueryObj);
						expect(localOlapTemplateService.getMdxQueryClickables()).not.toBeDefined();
						expect(success).toBe(false);
					})
		
		
					it('deleted. should not be defined ',function(){
					
						localOlapTemplateService.setMdxQueryTag(validMdxQueryObj);
						localOlapTemplateService.setClickableTag(validClickables);
						localOlapTemplateService.deleteClickableTag();			
						expect(localOlapTemplateService.getMdxQueryClickables()).not.toBeDefined();
					})
				
					describe('clickable object', function () {
					
						it('should return false: mandatory properties missing',function(){
						
							localOlapTemplateService.setMdxQueryTag(validMdxQueryObj);
							var success = localOlapTemplateService.setClickableTag(anaDrParams);
							expect(localOlapTemplateService.getMdxQueryClickables()).not.toBeDefined();
							expect(success).toBe(false);
						})
					})
			
				})
			
			})
			
			describe('scenario property', function () {
					
						it('should be set and return true',function(){

							var success = localOlapTemplateService.setScenarioTag(validScenario);
							var jsonTemplateTagValue = localOlapTemplateService.getJsonTemplateTag().XML_TAG_TEXT_CONTENT;
							localOlapTemplateService.deleteJsonTemplateTag();
							
							expect(localOlapTemplateService.getTemplateObject()).toEqual(jsonTemplateTagValue);
							expect(localOlapTemplateService.getScenarioTag()).toBeDefined();
							expect(success).toBe(true);
						})
						
						it('should not be set and return false:no editCube property ',function(){

							var success = localOlapTemplateService.setScenarioTag(scenarioWithoutEditCube);
							expect(localOlapTemplateService.getScenarioTag()).not.toBeDefined();
							expect(success).toBe(false);
						})
						
						it('should not be set and return false:no measures property ',function(){

								var success = localOlapTemplateService.setScenarioTag(scenarioWithoutMeasures);
								expect(localOlapTemplateService.getScenarioTag()).not.toBeDefined();
								expect(success).toBe(false);
							})
						
						it('should not be set and return false:measures array is empty  ',function(){

								var success = localOlapTemplateService.setScenarioTag(scenarioBadMeasureName);
								expect(localOlapTemplateService.getScenarioTag().MEASURE.length).toEqual(1);
								expect(success).toBe(true);
							})
						
						it('should not be set and return false:bad format of objects in variables array  ',function(){

								var success = localOlapTemplateService.setScenarioTag(scenarioBadFormatVariables);
								expect(localOlapTemplateService.getScenarioTag()).not.toBeDefined();
								expect(success).toBe(false);
							})
						
						describe('MEASURE property array', function () {
							
							it('should set only one measure and return true:no property name ',function(){

								var success = localOlapTemplateService.setScenarioTag(scenarioEmptyMeasures);
								var jsonTemplateTagValue = localOlapTemplateService.getJsonTemplateTag().XML_TAG_TEXT_CONTENT;
								localOlapTemplateService.deleteJsonTemplateTag();
								
								expect(localOlapTemplateService.getTemplateObject()).toEqual(jsonTemplateTagValue);
								expect(localOlapTemplateService.getScenarioTag()).not.toBeDefined();
								expect(success).toBe(false);
							})
							
							
						})
						
						describe('VARIABLE property array', function () {
							it('should not set: variables is emtpy',function(){

								var success = localOlapTemplateService.setScenarioTag(scenarioEmptyVariables);
								expect(localOlapTemplateService.getScenarioTag().VARIABLE).not.toBeDefined();
								
							})
							
							
						})
						
						
						
						
					})
					
				describe('JSONTEMPLATE property', function () {
					
					it('should set and return true',function(){

						var success = localOlapTemplateService.setJsonTemplateTag();
						
						
						
						expect(localOlapTemplateService.getJsonTemplateTag()).toBeDefined();
						expect(success).toBe(true);
						
					})
					
					it('should set and be equal to template without JSONTEMPLATE tag',function(){
						
						localOlapTemplateService.setJsonTemplateTag();
						var jsonTemplateTagValue = localOlapTemplateService.getJsonTemplateTag().XML_TAG_TEXT_CONTENT;
						localOlapTemplateService.deleteJsonTemplateTag();
						expect(localOlapTemplateService.getTemplateObject()).toEqual(jsonTemplateTagValue);
					})
						
					
					
				})

		});
