var KnowageLoginPage = function(){
	var userName = element(by.id('userID'));
	var password = element(by.id('password'));
	var login = element(by.css('button[type="submit"]'));
	
	this.setUserName = function(uName){
		userName.sendKeys(uName);
		return this;
	}
	
	this.getUserName = function(){
		return userName.getAttribute('value');
	}
	
	this.setPassword = function(pass){
		password.sendKeys(pass);
		return this;
	}
	
	this.getPassword = function(){
		return password.getAttribute('value');
	}
	
	this.login = function(){
		login.click();
	}
	
	this.get = function(){
		browser.ignoreSynchronization = true;
		browser.get('knowage/servlet/AdapterHTTP?ACTION_NAME=LOGOUT_ACTION');
		browser.get('knowage/');
		return this;
		
	}
}

module.exports = new KnowageLoginPage();