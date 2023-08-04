describe('lov-test', function() {

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

	it('should open Lov catalog',function(){
		element(by.css('[title="Lovs Management"]')).click();
		browser.sleep(2000);
	});

	it('checks that getting lovs work',function(){
		
		var driver = browser.driver;
		var loc = by.tagName('iframe');
		var el = driver.findElement(loc);
		browser.switchTo().frame(el);

		element(by.id("Analysis")).click();
		browser.sleep(2000);

	});


});
