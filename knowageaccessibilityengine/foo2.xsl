<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
              
<xsl:output method="xml" indent="yes" 
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" />


<xsl:template match="/">
  <html xmlns="http://www.w3.org/1999/xhtml">
 	<head>
	  <link href="../main.css" rel="stylesheet" type="text/css" />
	</head>
  <body>
  <h2>Risultato:</h2>
  <table>
    <tr class="verde">
      <th>CODICE CONDIZIONE</th>
      <th>DESCRIZIONE CONDIZIONE</th>
    </tr>
    <xsl:for-each select="ROWS/ROW">
    <tr>
      <td><xsl:value-of select="@COD_CONDIZIONE"/></td>
      <td><xsl:value-of select="@DES_CONDIZIONE"/></td>
    </tr>
    </xsl:for-each>
  </table>
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>