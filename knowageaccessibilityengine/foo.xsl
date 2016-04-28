<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
              
<xsl:output method="xml" indent="yes" 
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" />

<xsl:template match="/">
  <html xmlns="http://www.w3.org/1999/xhtml">
 	<head>
 		<!-- Replace css link with embedded style for safety  -->
	  	<!-- link href="../main.css" rel="stylesheet" type="text/css" /-->
	  	<style>
	  	h2 {
		    color:#538620;
			font-size:110%;
			font-weight:bold;
		 } 
		 tr{
		 	border: 1px solid blue;
		 }
		 td{
		  border: 1px solid blue;
		 }	 	  	
	  	
	  	</style>

	</head>
  <body>
  <h2>Risultato:</h2>
  <table>
    <tr>
      <th>id</th>
      <th>username</th>
      <th>group</th>
      <th>ERROR</th>

    </tr>
    <xsl:for-each select="ROWS/ROW">
    <tr>
      <td><xsl:value-of select="@ID"/></td>
      <td><xsl:value-of select="@USERNAME"/></td>
      <td><xsl:value-of select="@USERGROUP"/></td>
      <td><xsl:value-of select="@ERROR_MESSAGE"/></td>
    </tr>
    </xsl:for-each>
  </table>
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>