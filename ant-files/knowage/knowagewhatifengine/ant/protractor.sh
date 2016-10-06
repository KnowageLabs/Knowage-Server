# start selenium
/home/spagobi/continuousintegration/software/node_modules/protractor/bin/webdriver-manager start  &

# wait until selenium is up
while ! curl http://localhost:4444/wd/hub/status &; do :; done

# run protractor tests
cd /home/spagobi/jenkins/workspace/knowage/knowagewhatifengine/ProtractorTest/test/e2e/dragan
/home/spagobi/continuousintegration/software/node_modules/protractor/bin/protractor conf.js

# stop selenium
curl -s -L http://localhost:4444/selenium-server/driver?cmd=shutDownSeleniumServer 