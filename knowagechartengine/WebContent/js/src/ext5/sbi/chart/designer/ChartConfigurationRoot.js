/**
 * This is the root panel class that will be extended by all panels on the Step 2
 * (Configuration) tab panel of the Designer. We will specify some common properties
 * that those panels should have (contain).
 * 
 * @author: danristo (danilo.ristovski@mht.net)
 */
Ext.define
(
	'Sbi.chart.designer.ChartConfigurationRoot', 
	
	{
		extend : 'Ext.panel.Panel',
		
		// (top, right, bottom, left)
		margin: "5 0 5 0"
	}
);