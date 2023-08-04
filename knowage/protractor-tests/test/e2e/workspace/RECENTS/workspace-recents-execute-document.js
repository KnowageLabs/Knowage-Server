describe('workspace - recents', function() {

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
	    browser.waitForAngular();
	 });

	 it('should execute an recent document',function(){
		 var driver = browser.driver;
		 var loc = by.tagName('iframe');
		 var el = driver.findElement(loc);
		 browser.switchTo().frame(el);

		 element(by.css('[aria-label="Execute"]')).click();
		 browser.sleep(10000);
	});


});
