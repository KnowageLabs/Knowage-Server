var helper = require('pageObjects/helper');
var entManager = require('./entityManager/entityManager');
var Editor = function(){
	
	var entityManager = entManager;
	var derivedEntityManager = null;
	var queryManager = null;
	
	this.getEntityManager = function(){
		return entityManager;
	}
	
}

module.exports = new Editor();