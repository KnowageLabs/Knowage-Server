/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public class StatementTockenizer extends StringTokenizer {

	private final String satement;
	private final String currentToken;
	private List<String> tockens;
	private int tockenCount;

	private static final String DELIMITERS = "+-|*/()<>=!,";
	private static final String[] ADDITIONALS_DELIMITERS_SUBSTRING_FUNCTIONS = { " as ", "distinct", " like ", "case when", " when ", " then ", "else",
			" end ", "not in ", " in ", " between", "is not null ", "is null ", "is not empty ", "is empty ", "not member of", "member of", " and ", " or " };

	/**
	 * @param str
	 */
	public StatementTockenizer(String str) {
		super(str, DELIMITERS);
		satement = str;
		currentToken = null;
		getAllTockens();
		tockenCount = 0;
	}

	private void getAllTockens() {
		tockens = new ArrayList<String>();
		while (super.hasMoreTokens()) {
			parseTocken(super.nextToken());
		}
	}

	private void parseTocken(String tocken) {
		int position = 0;
		while (tocken.length() > 0) {
			boolean foundAdditional = false;
			for (int i = 0; i < ADDITIONALS_DELIMITERS_SUBSTRING_FUNCTIONS.length; i++) {
				position = tocken.toLowerCase().indexOf(ADDITIONALS_DELIMITERS_SUBSTRING_FUNCTIONS[i]);
				if (position >= 0) {
					if (position > 0) {
						tockens.add(tocken.substring(0, position));
					}
					tocken = tocken.substring(position + ADDITIONALS_DELIMITERS_SUBSTRING_FUNCTIONS[i].length());
					foundAdditional = true;
					break;
				}
			}
			if (!foundAdditional) {
				tockens.add(tocken);
				break;
			}

		}
	}

	public String nextTokenInStatement() {
		String nextToken;

		nextToken = null;
		try {
			nextToken = tockens.get(tockenCount);
			nextToken = nextToken.trim();
			tockenCount++;

		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured during tokenization of statement [" + satement + "] (current token: [" + currentToken
					+ "]; next: token: [" + nextToken + "])", t);
		}

		return nextToken;
	}

	@Override
	public boolean hasMoreTokens() {
		return tockenCount < tockens.size();
	}

}
