public String getSingleValueProfileAttribute(String attrName) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(attrName);
		return strBuf.toString();
};

public String getMultiValueProfileAttribute(String attrName, String prefix, String newSplit, String suffix) {
	String splitter = attrName.substring(1,2);
	String valuesList = attrName.substring(3, attrName.length() - 2);
	String [] values = valuesList.split(splitter);
	String newListOfValues = values[0];
	for (i in 1..<values.length) {
		newListOfValues = newListOfValues + newSplit + values[i];
	};
	String finalResult = prefix + newListOfValues + suffix;
	StringBuffer strBuf = new StringBuffer();
	strBuf.append(finalResult);
	return strBuf.toString();
};


public String returnValue(String valuein) {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append('<ROWS>');
		strBuf.append('<ROW ');
		valuein = valuein.replaceAll("'","");
		strBuf.append('value=\''+valuein+'\' >');
		strBuf.append('</ROW>');
		strBuf.append('</ROWS>');
		return strBuf.toString();
};


public String getListFromMultiValueProfileAttribute(String attrName) {
	String splitter = attrName.substring(1,2);
	String valuesList = attrName.substring(3, attrName.length() - 2);
	String [] values = valuesList.split(splitter);
	StringBuffer strBuf = new StringBuffer();
	strBuf.append("<ROWS>");
	for (i in 0..<values.length) {
		strBuf.append("<ROW VALUE=\"" + values[i].replaceAll("'","") +  "\" />");
	};
	strBuf.append("</ROWS>");
	return strBuf.toString();
};

public String split(String attrName, String splitter) {
	String [] values = attrName.split(splitter);
	StringBuffer strBuf = new StringBuffer();
	strBuf.append("<ROWS>");
	for (i in 0..<values.length) {
		String aValue = values[i];
		if (aValue.startsWith("'") && aValue.endsWith("'")) {
			aValue = aValue.substring(1, aValue.length() - 1);
		}
		strBuf.append("<ROW VALUE=\"" + aValue +  "\" />");
	};
	strBuf.append("</ROWS>");
	return strBuf.toString();
};

public String NULLIF(BigDecimal expression1, Integer expression2) {
	return expression1.compareTo(expression2)==0 ? null : expression1;
};

public Boolean isValid(String key) {
	return key != null && parameters.get(key) != null && !parameters.get(key).equals("") && !parameters.get(key).equals("''") && !parameters.get(key).equals("null") && !parameters.get(key).equals("%");
}
