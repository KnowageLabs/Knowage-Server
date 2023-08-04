describe('workspace - datasets - shared datasets', function() {

	afterEach(function() {
		browser.sleep(50);
	});

	it('should try to login', function() {
	browser.ignoreSynchronization=true;
	 browser.get('/knowage');
	 browser.driver.findElement(by.id('userID')).sendKeys('biuser');
	 browser.driver.findElement(by.id('password')).sendKeys('biuser');
	 browser.driver.findElement(by.css('.btn-signin')).click();
	});

	it('should click on hamburger menu', function() {
	 element(by.css('.menuKnowage')).click();
	 browser.sleep(1000);
	});

	it('should open Workspace',function(){
		element(by.css('[title="My workspace"]')).click();
		browser.sleep(5000);
	});

	it('should open data - datasets',function(){
		var driver = browser.driver;
		var loc = by.tagName('iframe');
		var el = driver.findElement(loc);
		browser.switchTo().frame(el);

		element(by.css('[class="md-clickable notSelectedLeftMenuItem"]')).click();
		browser.sleep(2000);
		element.all(by.repeater('suboption in option.submenuOptions')).get(0).click();
		browser.sleep(3000);
	});

	it('should click on shared datasets tab',function(){
		element.all(by.repeater('tab in $mdTabsCtrl.tabs')).get(2).click();
		browser.sleep(2000);
	});


});
