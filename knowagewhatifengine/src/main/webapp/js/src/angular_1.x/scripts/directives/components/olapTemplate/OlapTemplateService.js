
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
 * angular service for building Template for olap document in json format
 */


angular.module('olap_template')
		.service('OlapTemplateService',function(){

			const SCENARIO_NAME = "scenario";
			this.template = {};
			var olap={};


			/*
			 * Template object getter
			 */
			this.getTemplateObject = function(){
				return this.template;
			}

			/*
			 * Final Template object Json
			 */
			this.getTempateJson = function(){

				if(this.getTemplateObject()){
					return angular.toJson(this.getTemplateObject());
				}
			}


			/*
			 * Setter for Template object
			 */
			this.setTemplateObject = function(templateObject){
				this.template = templateObject;
			}


			/*
			 * Getter for olap Tag
			 * @return object
			 */
			this.getOlapTag = function(){

				if(this.getTemplateObject()){

					return this.getTemplateObject().olap;

				}else{

				console.log("template is undefined!!!");
				}
			}

			/*
			 * Setter for olap tag
			 */
			this.setOlapTag = function(olap){
				if(this.getTemplateObject()){

						this.getTemplateObject().olap = olap;
						this.setJsonTemplateTag();

				}else{
					console.log("template is undefined!!!");
				}
			}

			/*
			 * Deleting olap tag
			 */
			this.deleteOlapTag = function(){
				delete this.getTemplateObject().olap;
			}

			/*
			 * Getter for cube tag
			 * @return cube object
			 * {"reference":"cubeName"}
			 */
			this.getCubeTag = function(){
				if(this.getOlapTag()){
					return this.getOlapTag().cube;
				}else{
					console.log("Olap object is undefined!!!");
				}

			}

			/*
			 * Getting cube reference
			 * @return string
			 *
			 */
			this.getCubeReference = function(){
				if(this.getCubeTag()){
					return this.getCubeTag().reference;
				}
			}

			/*
			 * Setter for cube tag
			 * @param cubeName type string
			 * return boolean
			 * true if setting is successful
			 */
			this.setCubeTag = function(cubeName){
				if(this.getOlapTag()){
					if(!this.getCubeTag()){
						this.getOlapTag().cube = {};
					}
					if(cubeName&&cubeName!==""){
						this.getCubeTag().reference = cubeName;

					}else{
						console.log("Cube name is empty!!!");
						this.deleteCubeTag();
						return false;
					}

				}else{
					console.log("Olap object is undefined!!!");
					return false;
				}
				//console.log(cubeName.startsWith('a'));
				this.setJsonTemplateTag();
				return true;
			}

			/*
			 * Deleting cube tag
			 */
			this.deleteCubeTag = function(){
				if(this.getOlapTag()){
					 delete this.getOlapTag().cube;
				}else{
					console.log("Olap object is undefined!!!");
				}

			}

			/**************************************************************************/
			this.getPaginationTag = function(){
				if(this.getOlapTag()){
					return this.getOlapTag().pagination;
				}else{
					console.log("Olap object is undefined!!!");
				}

			}

			/*
			 * Getter for pagination
			 * @return pagination	type string
			 */

			this.getPagination = function(){
				if(this.getOlapTag()){
					if(this.getPaginationTag()){
						return this.getPaginationTag().XML_TAG_TEXT_CONTENT;
					}

				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			/*
			 * Setter for pagination tag
			 * @param pagination 	type boolean
			 * return boolean
			 * true if setting is successful
			 *
			 */
			this.setPaginationTag = function(pagination){
				if(this.getOlapTag()){
					 if(!this.getPaginationTag()){
						 this.getOlapTag().pagination = {};
					 }
					 if(pagination!=undefined){
						 this.getPaginationTag().XML_TAG_TEXT_CONTENT = pagination;
						 this.setJsonTemplateTag();
						 return true;
					 }else{
						 console.log("pagination is empty!!!");
						 this.deletePaginationTag();
						 return false;
					 }


				}else{
					console.log("Olap object is undefined!!!");
				}

				return false;

			}

			/*
			 * Deleting Pagination tag
			 */
			this.deletePaginationTag = function(){
				if(this.getOlapTag()){
					 delete this.getOlapTag().pagination;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}
			/**************************************************************************/

			/*
			 * Getter for mdxQueryTag object
			 * @return mdxQueryTag object
			 * {"mdxQuery":"SELECT {[Measures].[Unit Sales]} ON COLUMNS, {[Product]} ON ROWS FROM [Sales_V]",
               "clickable":[{
        						"uniqueName":"[Product].[Product Name]",
         						"clickParameter":{"name":"ParProdFamily","value":"{0}"}
    						}
    						]
   				}
			 */
			this.getMdxQueryTag = function(){
				if(this.getOlapTag()){
					return this.getOlapTag().MDXQUERY;
				}else{
					console.log("Olap object is undefined!!!");
				}

			}

			/*
			 * Getting mdx query
			 * @return mdx type string
			 */

			this.getMdxQuery = function(){
				if(this.getMdxQueryTag()){
					return this.getMdxQueryTag().XML_TAG_TEXT_CONTENT;
				}else{
					console.log("MdxQueryTag object is undefined!!!");
				}
			}

			/*
			 * Getter for clickables
			 * @return 	type	array
			 * [{"uniqueName":"[Product].[Product Name]",
         		 "clickParameter":{"name":"ParProdFamily","value":"{0}"}
         	   ]
			 */
			this.getMdxQueryClickables = function(){
				if(this.getMdxQueryTag()){
					return this.getMdxQueryTag().clickable;
				}else{
					console.log("MdxQueryTag object is undefined!!!");
				}
			}


			/*
			 * Setter for MdxQuery tag
			 * return boolean
			 * true if setting is successful
			 * @param mdxQueryObj
			 *
			 * @object definition  mdxQueryObj
			 * mandatory mdxQueryObj properties:
			 * mdxQuery		type 	string
			 */
			this.setMdxQueryTag = function(mdxQueryObj){
				if(this.getOlapTag()){
					 if(!this.getMdxQueryTag()){
						 this.getOlapTag().MDXQUERY = {};
					 }

					 if(mdxQueryObj&&mdxQueryObj.mdxQuery){
						 this.getMdxQueryTag().XML_TAG_TEXT_CONTENT = mdxQueryObj.mdxQuery;


					 }else{
						 console.log("Bad format of mdxQueryObj!!!Mandatory property: mdxQuery ") ;
						 this.deleteMdxQueryTag();
						 return false;
					 }



				}else{
					console.log("Olap object is undefined!!!");
					return false;
				}
				this.setJsonTemplateTag();
				return true;
			}


			/*
			 * Setter for Clikable tag
			 * return boolean
			 * true if setting is successful
			 * @param clikables type array
			 *
			 * @array definition:clickables
			 * mandatory clickables elements
			 * clikable type object
			 *
			 * @object definition:  clickable
			 * mandatory clickable properties:
			 * name				type	string
			 * clickParameter	type	object
			 *
			 * @object definition: clickParameter
			 * mandatory clickParameter properties:
			 * name		type 	string
			 * value	type	string
			 *
			 */
			this.setClickableTag = function(clickables){
				if(this.getOlapTag()){

					if(this.getMdxQueryTag()){
						if(clickables&&clickables.constructor === Array&&clickables.length>0){
							 this.getMdxQueryTag().clickable = [];
							 	for(var i= 0;i<clickables.length;i++){
							 		var clickable = clickables[i];
							 		if(clickable&&clickable.uniqueName&&clickable.clickParameter){
							 			if(clickable.clickParameter.name&&clickable.clickParameter.value){
							 				 this.getMdxQueryTag().clickable.push(clickable);
							 			}else{
							 				console.log("Bad format of clickParameter!!!Mandatory properties: name,value ");
							 				this.deleteClickableTag();
							 				return false;
							 			}
							 		}else{
							 			console.log("Bad format of clickable!!!Mandatory properties: uniqueName,clickParameter ");
							 			this.deleteClickableTag();
							 			return false;
							 		}
							 	}
						 }else{
							 console.log("Bad format of clickables!!!Mandatory clickables is array of clickable ");
							 this.deleteClickableTag();
							 return false;
						 }
					 }else{
						 console.log('MdxQueryTag is not defined')
						 return false;
					 }






				}else{

					console.log("Olap object is undefined!!!");
					return false;
				}

				this.setJsonTemplateTag();
				return true;
			}

			/*
			 * Deleting clickable tag
			 */
			this.deleteClickableTag = function(){
				if(this.getOlapTag()){
					if(this.getMdxQueryTag()){
						delete this.getMdxQueryTag().clickable;
					}else{
						console.log("MdxQuery object is undefined!!!");
					}

				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			/*
			 * Adding parameters to MDX query
			 */

			this.injectParametersToMdxQueryTag = function(anaDrParams){

				if(this.getOlapTag()){

					if(this.getMdxQueryTag()){
						this.getMdxQueryTag().parameter = [];
						var mdxQuery = this.getMdxQuery();
						var newMdxQuery;
						var mdxObject = {};
						if(mdxQuery){
							for(var i = 0;i<anaDrParams.length;i++){

								var memberObj = anaDrParams[i];
								if(memberObj.memberUniqueName&&memberObj.replaceName){

									mdxQuery = mdxQuery.replace(memberObj.memberUniqueName,memberObj.replaceName);

									if(memberObj.parameter){
										if(memberObj.parameter.name&&memberObj.parameter.as){
											this.getMdxQueryTag().parameter.push(memberObj.parameter);
										}else{
											console.log('Bad format of parameter.Mandatory properties name,as');
											return false;
										}

									}
									mdxObject.mdxQuery = mdxQuery;
									this.setMdxQueryTag(mdxObject);

								}else{
									console.log('Bad format of memberObj.Mandatory properties memberUniqueName,replaceName');
									return false;
								}

							}
							this.setJsonTemplateTag();
							return true;
						}

					}else{
						console.log('MdxQuery tag is undefined');
					}
				}else{
					console.log("Olap object is undefined!!!");
				}

				return false;


			}

			/*
			 * Getting analitical drivers params
			 *  @return 	type	array
			 * [{"alias":"twoUrl",name:"two"},{"alias":"one",name:"one"}]
			 */

			this.getAnaliticalDriverParams = function(){
				var params = [];
				if(this.getOlapTag()){
					if(this.getMdxQueryTag()){
						if(this.getMdxQueryTag().parameter){
							params = this.getMdxQueryTag().parameter;
						}

					}else{
						console.log('MdxQuery tag is undefined');
					}
				}else{
					console.log("Olap object is undefined!!!");
				}

				return params;
			}


			/*
			 * Deleting MdxQuery tag
			 */

			this.deleteMdxQueryTag = function(){
				if(this.getOlapTag()){
					 delete this.getOlapTag().MDXQUERY;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			this.getMDXMondrianQueryTag = function(){
				if(this.getOlapTag()){
					return this.getOlapTag().MDXMondrianQuery;
				}else{
					console.log("Olap object is undefined!!!");
				}

			}

			/*
			 * Getter for MDXMondrianQuery
			 * @return MDXMondrianQuery	type string
			 */

			this.getMDXMondrianQuery = function(){
				if(this.getOlapTag()){
					if(this.getMDXMondrianQueryTag()){
						return this.getMDXMondrianQueryTag().XML_TAG_TEXT_CONTENT;
					}

				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			/*
			 * Setter for MDXMondrianQuery tag
			 * @param MDXMondrianQuery 	type string
			 * return boolean
			 * true if setting is successful
			 *
			 */
			this.setMDXMondrianQueryTag = function(MDXMondrianQuery){
				if(this.getOlapTag()){
					 if(!this.getMDXMondrianQueryTag()){
						 this.getOlapTag().MDXMondrianQuery = {};
					 }
					 if(MDXMondrianQuery&&MDXMondrianQuery!==""){
						 this.getMDXMondrianQueryTag().XML_TAG_TEXT_CONTENT = MDXMondrianQuery;
						 this.setJsonTemplateTag();
						 return true;
					 }else{
						 console.log("mdxMondrianQuery is empty!!!");
						 this.deleteMDXMondrianQueryTag();
						 return false;
					 }


				}else{
					console.log("Olap object is undefined!!!");
				}

				return false;

			}

			/*
			 * Deleting MDXMondrianQuery tag
			 */
			this.deleteMDXMondrianQueryTag = function(){
				if(this.getOlapTag()){
					 delete this.getOlapTag().MDXMondrianQuery;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}



			/*
			 * Getter of scenarioTag
			 * @return	scenarioTag type object
			 * "{
			 * 	"name": "scenario",
      			"editCube": "Sales_Edit",
      			"initialVersion": "1",
      			"MEASURE": [
       						{"XML_TAG_TEXT_CONTENT": "Store Sales"},
       						{ "XML_TAG_TEXT_CONTENT":"Store Cost"}
      						],
      			"VARIABLE": [
        						{
          						"name": "var",
          						"value": "5"
        						},
        						{
          						"name": "PD",
          						"value": "[Product].[Drink.Dairy]",
          						"type": "string"
        						}
      						]
    			}
			 */
			this.getScenarioTag = function(){
				if(this.getOlapTag()){
					return this.getOlapTag().SCENARIO;
				}else{
					console.log("Olap object is undefined!!!");
				}

			}

			/*
			 * Getter for scenario object
			 * @return scenario		type	object
			 * {
			 * 	"name":"scenario",
			 * 	"editCube":"SalesEdit",
			 * 	"measures":[
			 * 					{"name":"Store Cost"},
			 * 					{"name":"Store Sales"}
			 * 			   ],
			 * 	"variables":[
			 * 					 {"name": "var","value": "5"},
        	 *					 {"name": "PD","value": "[Product].[Drink.Dairy]","type": "string"}
			 * 				]
			 * }
			 */

			this.getScenarioObject = function(){
				var scenarioObj;

				if(this.getScenarioTag()){
					scenarioObj = {};
					scenarioObj.name = this.getScenarioTag().name;
					scenarioObj.editCube = this.getScenarioTag().editCube;
					scenarioObj.measures = [];
					if(this.getScenarioTag().MEASURE){

						for(var i = 0;i<this.getScenarioTag().MEASURE.length;i++){
							var temp = {};
							temp.name = this.getScenarioTag().MEASURE[i].XML_TAG_TEXT_CONTENT;
							scenarioObj.measures.push (temp);
						}

					}

					if(this.getScenarioTag().VARIABLE){
						scenarioObj.variables = this.getScenarioTag().VARIABLE;
					}

				}

				return scenarioObj;
			}

			/*
			 * Setter for scenario tag
			 * @param scenario type object
			 * return boolean
			 * true if setting is successful
			 *
			 * @object definition: scenario
			 * mandatory scenario properties:
			 * name		type	string
			 * editCube	type	string
			 * measures	type	array
			 *
			 * optional scenario properties:
			 * variables		type	array
			 * initialVersion	type	string
			 *
			 * @array definition: measures
			 * mandatory measures elements :
			 * measure	type	object
			 *
			 * @object definition: measure
			 * mandatory measure properties :
			 * name 	type	string
			 *
			 * @array definition: variables
			 * mandatory variables elements :
			 * variable		type	object
			 *
			 * @object definition: variable
			 * mandatory variable properties:
			 * name		type	string
			 * value	type	string
			 *
			 */
			this.setScenarioTag = function(scenario){
				if(this.getOlapTag()){
					 if(scenario.editCube&&scenario.editCube!==""&&scenario.measures&&scenario.measures.constructor === Array&&scenario.measures.length>0){

						 if(!this.getScenarioTag()){
							 this.getOlapTag().SCENARIO = {};
						 }
						 this.getScenarioTag().name = SCENARIO_NAME;
						 this.getScenarioTag().editCube = scenario.editCube;


						 this.getScenarioTag().MEASURE = [];
						 for(var i = 0; i<scenario.measures.length;i++){
							 var temp = {};
							 if(scenario.measures[i].name){
								 temp.XML_TAG_TEXT_CONTENT = scenario.measures[i].name;
								 this.getScenarioTag().MEASURE.push(temp);
							 }else{
								 console.log("Bad format of measure!!!Mandatory property: name ") ;
							 }


						 }



						 if(scenario.variables&&scenario.variables.constructor === Array&&scenario.variables.length>0){
							 this.getScenarioTag().VARIABLE = [];
							 for(var i = 0; i<scenario.variables.length;i++){
								 if(scenario.variables[i].name&&scenario.variables[i].value){
									 this.getScenarioTag().VARIABLE.push(scenario.variables[i]);
								 }else{
									 this.deleteScenarioTag ();
									 console.log("Bad format of variable!!!Mandatory properties: name and value ");
									 return false;
								 }

							 }
						 }
						 this.setJsonTemplateTag();
						 return true;

					 }else{
						 console.log("Bad format of scenario!!!Mandatory properties: editCube and array of measures ");
						 return false;
					 }

				}else{
					console.log("Olap object is undefined!!!");
				}

				return false;

			}

			/*
			 * Deleting Scenario tag
			 */
			this.deleteScenarioTag = function(){
				if(this.getOlapTag()){
					 delete this.getOlapTag().SCENARIO;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}
			/*
			 * Getter for CrossNavigationTag
			 * @returns CrossNavigationTag	type	object
			 * {
        		"PARAMETERS":{
                			"PARAMETER":[
                    						{
                    						"name":"family",
                    						"scope":"relative",
                    						"dimension":"Product",
                    						"hierarchy":"[Product]",
                    						"level":"[Product].[Product Family]"
                    						}
                    					]
            				}
    			}
			 */
			this.getCrossNavigationTag = function(){
				if(this.getOlapTag()){
					return this.getOlapTag().CROSS_NAVIGATION;
				}else{
					console.log("Olap object is undefined!!!");
				}

			}
			/*
			 * Getter for CrossNavigation
			 * @return CrossNavigation	type	array
			 * [
			 * 	 {
                 "name":"family",
                 "scope":"relative",
                 "dimension":"Product",
                 "hierarchy":"[Product]",
                 "level":"[Product].[Product Family]"
                  }
			 * ]
			 *
			 */
			this.getCrossNavigation = function(){
				var crossNavigation;

				if(this.getCrossNavigationTag()&&this.getCrossNavigationTag().PARAMETERS){
					if(this.getCrossNavigationTag().PARAMETERS.PARAMETER){
						crossNavigation = this.getCrossNavigationTag().PARAMETERS.PARAMETER;
					}

				}

				return crossNavigation;
			}

			/*
			 * Setter for crossNavigation tag
			 * @param crossNavigation type array
			 * return boolean
			 * true if setting is successful
			 *
			 * @array definition:  crossNavigation
			 * mandatory crossNavigation elements:
			 * parameter 	type	object
			 *
			 *@object definition: parameter
			 * mandatory parameter properties:
			 * name 		type	string
			 * dimension	type	string
			 * hierarchy	type	string
			 * level		type	string
			 */
			this.setCrossNavigationTag = function(crossNavigation){
				if(this.getOlapTag()){
					 if(crossNavigation&&crossNavigation.constructor === Array&&crossNavigation.length>0){

						 if(!this.getCrossNavigationTag()){
							 this.getOlapTag().CROSS_NAVIGATION ={}
						 }

						 this.getCrossNavigationTag().PARAMETERS = {};

						 this.getCrossNavigationTag().PARAMETERS.PARAMETER = [];

						 for(var i = 0;i<crossNavigation.length;i++){
							 var parameter = crossNavigation[i];

							 if(parameter.name&&
								parameter.dimension&&
								parameter.hierarchy&&
								parameter.level){

								 this.getCrossNavigationTag().PARAMETERS.PARAMETER.push(parameter);

							 }else{
								 console.log("Bad format of parameter!!!Mandatory properties:name,dimension,hierarchy,level");
								 this.deleteCrossNavigationTag();
								 return  false;

							 }

							 if(i===crossNavigation.length-1&&this.getCrossNavigationTag().PARAMETERS.PARAMETER.length === 0){
								 this.deleteCrossNavigationTag();
								 console.log("Bad format of parameter!!!Array is empty");
								 return false;
							 }
						 }
						 this.setJsonTemplateTag();
						 return true;
					 }else{
						 console.log("Bad format of crossNavigation!!!Mandatory crossNavigation is array of parameters ");
					 }

				}else{
					console.log("Olap object is undefined!!!");
				}

				return false;

			}

			/*
			 * Deleting CrossNavigation tag
			 */
			this.deleteCrossNavigationTag = function(){
				if(this.getOlapTag()){
					 delete this.getOlapTag().CROSS_NAVIGATION;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			this.deleteParamFromClickables = function(item) {

				if(this.getMdxQueryTag()){

					var array = this.getMdxQueryClickables();
					for (var i = 0; i <array.length; i++) {
						if(item.name == array[i].name){
							array.splice(i, 1);
						}
					}
					if(array.length > 0){
						this.setClickableTag(array);
					}else{
						this.deleteClickableTag();
					}



				}


			}

			this.deleteParamFromCrossNavigationTag = function(item){
				var array = this.getCrossNavigation();
				for (var i = 0; i <array.length; i++) {
					if(item.name == array[i].name){
						array.splice(i, 1);
					}
				}

				if(array.length > 0){
					console.log("imamo cross")
					this.setCrossNavigationTag(array);
				}else{
					console.log("brisem tag")
					 this.deleteCrossNavigationTag();
				}


			}

			/*
			 * Getter for Toolbar tag
			 * @return getToolbarTag	type	object
			 * {
       			"BUTTON_FATHER_MEMBERS" :{"visible":"true","clicked":"true"},
       			"BUTTON_HIDE_SPANS" :{"visible":"true","clicked":"true"},
       			"BUTTON_SHOW_PROPERTIES" :{"visible":"true","clicked":"true"},
       			"BUTTON_HIDE_EMPTY" :{"visible":"true","clicked":"true"},
       			"BUTTON_EXPORT_OUTPUT" :{"visible":"true","clicked":"true"},
       			"BUTTON_FLUSH_CACHE" :{"visible":"true","clicked":"true"}
       			}
			 */

			this.getToolbarTag = function(){
				if(this.getOlapTag()){
					return this.getOlapTag().TOOLBAR;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			/*
			 * Getter for toolbarButtons
			 * @return toolbarButtons	type	array
			 * [
       				{"name":"BUTTON_FATHER_MEMBERS","visible":"true","clicked":"true"},
       				{"name":"BUTTON_HIDE_SPANS","visible":"true","clicked":"true"},
       				{"name":"BUTTON_SHOW_PROPERTIES","visible":"true","clicked":"true"},
       				{"name":"BUTTON_HIDE_EMPTY","visible":"true","clicked":"true"},
       				{"name":"BUTTON_EXPORT_OUTPUT","visible":"true","clicked":"true"},
       				{"name":"BUTTON_FLUSH_CACHE","visible":"true","clicked":"true"}
       			]
			 */
			this.getToolbarButtons = function(){
				var toolbarButtons =[];
				if(this.getToolbarTag()){
					var keys = Object.keys(this.getToolbarTag());
					for(var i =0;i<keys.length;i++){
						var button = {};
						button.name = keys[i];
						button.visible = this.getToolbarTag()[keys[i]].visible;
						button.clicked = this.getToolbarTag()[keys[i]].clicked;
						toolbarButtons.push(button);
					}
				}
				return toolbarButtons;
			}
			/*
			 * Setter for toolbarButtons tag
			 * @param toolbarButtons type array
			 * return boolean
			 * true if setting is successful
			 *
			 * @array definition: toolbarButtons
			 * mandatory toolbarButtons elements:
			 * button object
			 *
			 * @object definition: button
			 * mandatory button properties:
			 * name 	type string
			 * visible 	type boolean
			 * clicked 	type boolean
			 */
			this.setToolbarTag = function(toolbarButtons){

				if(toolbarButtons&&toolbarButtons.constructor === Array&&toolbarButtons.length>0){

					if(!this.getOlapTag().TOOLBAR){
						this.getOlapTag().TOOLBAR = {};
					}

					for(var i = 0;i<toolbarButtons.length;i++){

						if(toolbarButtons[i].hasOwnProperty("name")&&toolbarButtons[i].hasOwnProperty("visible")&&toolbarButtons[i].hasOwnProperty("clicked")){
							this.getOlapTag().TOOLBAR[toolbarButtons[i].name] = {};
							this.getOlapTag().TOOLBAR[toolbarButtons[i].name].visible = toolbarButtons[i].visible;
							this.getOlapTag().TOOLBAR[toolbarButtons[i].name].clicked = toolbarButtons[i].clicked;
						}else{

							console.log("Bad format of button!!!Mandatory properties: name,visible,clicked ");
							this.deleteToolbarTag();
							return false;
						}
					}
					this.setJsonTemplateTag();
					return true;
				}else{
					console.log("Bad format of toolbarButtons!!!Mandatory toolbarButtons is array of buttons ");

				}
				return false;
			}

			/*
			 * Deleting CrossNavigation tag
			 */
			this.deleteToolbarTag = function(){
				if(this.getOlapTag()){
					 delete this.getOlapTag().TOOLBAR;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			this.getCalculatedFieldsTag = function(){
				if(this.getOlapTag()){
					return this.getOlapTag().calculated_fields;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			this.setCalculatedFieldsTag = function(calculatedFields){
				if(this.getOlapTag()){
					this.getOlapTag().calculated_fields = {};
					this.getOlapTag().calculated_fields.calculated_field = calculatedFields;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			this.deleteCalculatedFieldsTag = function(){
				if(this.getOlapTag()){
					delete this.getOlapTag().calculated_fields;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			/*
			 * Getting JSONTEMPLATE tag
			 */
			this.getJsonTemplateTag = function(){

				if(this.getOlapTag()){
					return this.getOlapTag().JSONTEMPLATE;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}

			/*
			 * Setting JSONTEMPLATE tag
			 */
			this.setJsonTemplateTag = function(){
				if(this.getOlapTag()){

					this.deleteJsonTemplateTag();
					var templateObject = angular.copy(this.getTemplateObject());
					this.getOlapTag().JSONTEMPLATE = {};
					this.getJsonTemplateTag().XML_TAG_TEXT_CONTENT = JSON.stringify(templateObject);

					return true;

				}else{
					console.log("Olap object is undefined!!!");
				}

				return false;

			}

			/*
			 * Deleting JSONTEMPLATE tag
			 */
			this.deleteJsonTemplateTag = function(){
				if(this.getOlapTag()){
					 delete this.getOlapTag().JSONTEMPLATE;
				}else{
					console.log("Olap object is undefined!!!");
				}
			}



			 this.setOlapTag(olap);



		})