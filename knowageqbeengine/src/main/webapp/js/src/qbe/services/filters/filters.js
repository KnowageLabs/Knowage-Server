
var filters = angular.module('filters',['sbiModule','advancedFiltersApp']);

filters.service('filters_service',function(sbiModule_action,sbiModule_translate,$injector){
	this.treeService = $injector.get('treeService');

	this.getFieldsValue= function(entity){
		var queryParam = {};
		var formParam = {};
		var headers = [];
		var configParams = {
    		headers: {
    			'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'
			},

		};
		if(entity.type == "inline.calculated.field"){
			queryParam.fieldDescriptor = entity.id;
		}else {
			queryParam.ENTITY_ID = entity.id;
		}
		formParam.start = 0;
		formParam.limit = 20;

		var response = sbiModule_action.promisePost('GET_VALUES_FOR_QBE_FILTER_LOOKUP_ACTION',queryParam,"",configParams);


		return response;
	};

	this.TARGET_TYPE_MANUAL         = {name:sbiModule_translate.load("kn.qbe.filters.target.types.manual"),value:"manual"};
	this.TARGET_TYPE_VALUE_OF_FIELD = {name:sbiModule_translate.load("kn.qbe.filters.target.types.field"),value:"valueOfField"};
	this.TARGET_TYPE_ANOTHER_ENTITY = {name:sbiModule_translate.load("kn.qbe.filters.target.types.anotherEntity"),value:"anotherEntity"};

	this.getTargetTypes = [
		this.TARGET_TYPE_MANUAL,
		this.TARGET_TYPE_VALUE_OF_FIELD,
		this.TARGET_TYPE_ANOTHER_ENTITY
	];

	this.getHavingTargetTypes = [
		  {name:sbiModule_translate.load("kn.qbe.filters.target.types.manual"),value:""},
		  {name:sbiModule_translate.load("kn.qbe.filters.target.types.anotherField"),value:"anotherEntity"}
		 // {name:sbiModule_translate.load("kn.qbe.filters.target.types.subquery"),value:"subquery"},
	];

	this.aggFunctions = [ "NONE", "SUM", "MIN", "MAX", "AVG", "COUNT", "COUNT_DISTINCT" ];

	this.getSpecialOperators = [
		{name:sbiModule_translate.load("kn.qbe.filters.operators.equals.to"),value:"EQUALS TO"},
		{name:sbiModule_translate.load("kn.qbe.filters.operators.greater.than"),value:"GREATER THAN"},
		{name:sbiModule_translate.load("kn.qbe.filters.operators.equals.or.greater.than"),value:"EQUALS OR GREATER THAN"},
		{name:sbiModule_translate.load("kn.qbe.filters.operators.less.than"),value:"LESS THAN"},
		{name:sbiModule_translate.load("kn.qbe.filters.operators.equals.or.less.than"),value:"EQUALS OR LESS THAN"}
	];

	this.getOperators = [
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.equals.to"),value:"EQUALS TO"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.equals.to"),value:"NOT EQUALS TO"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.greater.than"),value:"GREATER THAN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.equals.or.greater.than"),value:"EQUALS OR GREATER THAN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.less.than"),value:"LESS THAN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.equals.or.less.than"),value:"EQUALS OR LESS THAN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.starts.with"),value:"STARTS WITH"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.starts.with"),value:"NOT STARTS WITH"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.ends.with"),value:"ENDS WITH"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.ends.with"),value:"NOT ENDS WITH"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.contains"),value:"CONTAINS"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.contains"),value:"NOT CONTAINS"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.between"),value:"BETWEEN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.between"),value:"NOT BETWEEN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.in"),value:"IN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.in"),value:"NOT IN"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.is.null"),value:"IS NULL"},
		         {name:sbiModule_translate.load("kn.qbe.filters.operators.not.null"),value:"NOT NULL"}
		         ];
	this.getSpatialOperators = [
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.contains"),value:"SPATIAL_CONTAINS"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.nn"),value:"SPATIAL_NN"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.covered.by"),value:"SPATIAL_COVERED_BY"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.covers"),value:"SPATIAL_COVERS"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.disjoint"),value:"SPATIAL_DISJOINT"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.equals.to"),value:"SPATIAL_EQUALS_TO"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.filter"),value:"SPATIAL_FILTER"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.intersects"),value:"SPATIAL_INTERSECTS"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.overlaps"),value:"SPATIAL_OVERLAPS"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.touches"),value:"SPATIAL_TOUCHES"},
		{name:sbiModule_translate.load("kn.qbe.filters.spatial.operators.inside"),value:"SPATIAL_INSIDE"}];

	this.booleanConnectors= ["AND","OR"];
	
	this.refresh = function(filters, expression) {
		for (filter of filters) {
			var newConst = new Const("NODE_CONST", filter)
			var oldConst = this.treeService.findByName(expression, newConst.value);
			
			this.treeService.replace(expression, newConst, oldConst);
		}
	}
	
	this.push = function(expression, filter) {
		var newConst = new Const("NODE_CONST", filter)

		var newRoot = null;

		var nodeConst = newConst;

		if (expression
				&& Object.keys(expression).length === 0
				&& Object.getPrototypeOf(expression) === Object.prototype) {
			newRoot = new Operand("NODE_OP", filters.booleanConnector || "AND");
			newRoot.childNodes.push(newConst)
			angular.copy(newRoot, expression);
		} else if(expression.childNodes && expression.childNodes.length <= 1) {
			newRoot = expression;
			newRoot.childNodes.unshift(newConst);
		} else {
			newRoot = new Operand("NODE_OP", filters.booleanConnector || "AND");
			newRoot.childNodes.push(newConst)
			newRoot.childNodes.push(angular.copy(expression));
			angular.copy(newRoot, expression);
		}

	}

	this.deleteFilter = function(filters,filter,expression,advancedFilters){

		console.log(filter);
		this.deleteFilterByProperty('filterId',filter.filterId,filters,expression,advancedFilters)
	}

	this.deleteFilterByProperty = function(propertyName,propertyValue,filters,expression,advancedFilters){
		for (var i = 0; i < filters.length; i++) {
			if(filters[i][propertyName]!=undefined && filters[i][propertyName]==propertyValue) {

				filters.splice(i, 1);
				this.treeService.removeInPlace(expression, "$F{" + propertyValue + "}");
				i--;
			}

		}
	}


	//Polyfill for non IE11 compliant code - To replace with readable ie11 compliant code

	function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

	function _possibleConstructorReturn(self, call) { if (call && (_typeof(call) === "object" || typeof call === "function")) { return call; } return _assertThisInitialized(self); }

	function _assertThisInitialized(self) { if (self === void 0) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return self; }

	function _getPrototypeOf(o) { _getPrototypeOf = Object.setPrototypeOf ? Object.getPrototypeOf : function _getPrototypeOf(o) { return o.__proto__ || Object.getPrototypeOf(o); }; return _getPrototypeOf(o); }

	function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function"); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, writable: true, configurable: true } }); if (superClass) _setPrototypeOf(subClass, superClass); }

	function _setPrototypeOf(o, p) { _setPrototypeOf = Object.setPrototypeOf || function _setPrototypeOf(o, p) { o.__proto__ = p; return o; }; return _setPrototypeOf(o, p); }

	function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

	var Node = function Node(type) {
	  _classCallCheck(this, Node);

	  this.type = type;
	  this.childNodes = [];
	};

	var Operand =
	/*#__PURE__*/
	function (_Node) {
	  _inherits(Operand, _Node);

	  function Operand(type, value) {
	    var _this;

	    _classCallCheck(this, Operand);

	    _this = _possibleConstructorReturn(this, _getPrototypeOf(Operand).call(this, type));
	    _this.value = value;
	    return _this;
	  }

	  return Operand;
	}(Node);

	var Const =
	/*#__PURE__*/
	function (_Node2) {
	  _inherits(Const, _Node2);

	  function Const(type, filter) {
	    var _this2;

	    _classCallCheck(this, Const);

	    _this2 = _possibleConstructorReturn(this, _getPrototypeOf(Const).call(this, type));
	    _this2.value = "$F{" + filter.filterId + "}";
	    _this2.details = {
	      leftOperandAlias: filter.leftOperandAlias,
	      operator: filter.operator,
	      entity: filter.entity,
	      rightOperandValue: filter.rightOperandValue.join(", ")
	    };
	    return _this2;
	  }

	  return Const;
	}(Node);

	// End of polyfill

	this.generateExpressions = function (filters){
		var obj ={};

		for(var i = 0;i<filters.length ;i++){
			if(filters.length===1) return new Const("NODE_CONST",filters[i]);
		    obj = new Operand("NODE_OP",filters[i].booleanConnector);
		    var temp = obj;
		    for(var i = 0;i<filters.length ;i++){
		    	for(;i<filters.length-2 ;i++){
		    		temp.childNodes.push(new Const("NODE_CONST",filters[i]))
		            temp.childNodes.push(new Operand("NODE_OP",filters[i].booleanConnector))
		            temp = temp.childNodes[1]
		    	}
		    	temp.childNodes.push(new Const("NODE_CONST", filters[i]))
		    }
		}
		return obj;

	}

	// Fix consistency of the tree
	this.fix = function(expression, filters) {
		
		if (expression
				&& typeof expression != "undefined"
				&& !angular.equals(expression, {})) {

			if (expression.type == "NODE_CONST") {
				var newRoot = {
					type: "NODE_OP",
					childNodes: [ angular.copy(expression) ],
					value: "AND"
				};
				this.treeService.replace(expression, newRoot, expression);
			}

			for (filter of filters) {
				if (this.treeService.findByName(expression, "$F{" + filter.filterId + "}") == null) {
					this.push(expression, filter);
				}
			}
		}

	}

});


