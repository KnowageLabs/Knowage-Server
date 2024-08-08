/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.engines.whatif.parser;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import org.olap4j.OlapDataSource;
import org.pivot4j.PivotModel;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;

/**
 * CUP v0.11b beta 20140220 generated parser.
 *
 * @version Fri May 30 12:52:34 CEST 2014
 */
public class parser extends java_cup.runtime.lr_parser {

	/** Default constructor. */
	public parser() {
	}

	/** Constructor which sets the default scanner. */
	public parser(java_cup.runtime.Scanner s) {
		super(s);
	}

	/** Constructor which sets the default scanner. */
	public parser(java_cup.runtime.Scanner s, java_cup.runtime.SymbolFactory sf) {
		super(s, sf);
	}

	/** Production table. */
	protected static final short[][] _production_table = unpackFromStrings(
			new String[] { "\000\027\000\002\002\004\000\002\002\004\000\002\002"
					+ "\003\000\002\002\004\000\002\003\003\000\002\004\005"
					+ "\000\002\004\006\000\002\004\005\000\002\004\006\000"
					+ "\002\004\003\000\002\005\005\000\002\005\005\000\002"
					+ "\005\003\000\002\006\005\000\002\006\003\000\002\006"
					+ "\003\000\002\006\003\000\002\006\004\000\002\007\003"
					+ "\000\002\010\004\000\002\011\002\000\002\011\004\000" + "\002\012\003" });

	/** Access to production table. */
	@Override
	public short[][] production_table() {
		return _production_table;
	}

	/** Parse-action table. */
	protected static final short[][] _action_table = unpackFromStrings(
			new String[] { "\000\044\000\016\006\004\011\021\016\011\017\006\020"
					+ "\012\021\010\001\002\000\014\006\004\011\021\017\006"
					+ "\020\012\021\010\001\002\000\026\002\ufff5\005\ufff5\006"
					+ "\ufff5\007\ufff5\010\ufff5\011\ufff5\012\ufff5\017\ufff5\020\ufff5"
					+ "\021\ufff5\001\002\000\026\002\ufff3\005\ufff3\006\ufff3\007"
					+ "\ufff3\010\ufff3\011\ufff3\012\ufff3\017\ufff3\020\ufff3\021\ufff3"
					+ "\001\002\000\016\002\044\006\004\011\021\017\006\020"
					+ "\012\021\010\001\002\000\030\002\uffeb\004\uffeb\005\uffeb"
					+ "\006\uffeb\007\uffeb\010\uffeb\011\uffeb\012\uffeb\017\uffeb\020"
					+ "\uffeb\021\uffeb\001\002\000\014\006\004\011\021\017\006"
					+ "\020\012\021\010\001\002\000\026\002\ufff2\005\ufff2\006"
					+ "\ufff2\007\ufff2\010\ufff2\011\ufff2\012\ufff2\017\ufff2\020\ufff2"
					+ "\021\ufff2\001\002\000\026\002\uffef\005\uffef\006\uffef\007"
					+ "\uffef\010\uffef\011\uffef\012\uffef\017\uffef\020\uffef\021\uffef"
					+ "\001\002\000\026\002\ufff8\005\ufff8\006\ufff8\007\031\010"
					+ "\030\011\ufff8\012\ufff8\017\ufff8\020\ufff8\021\ufff8\001\002"
					+ "\000\026\002\ufff1\005\ufff1\006\ufff1\007\ufff1\010\ufff1\011"
					+ "\ufff1\012\ufff1\017\ufff1\020\ufff1\021\ufff1\001\002\000\030"
					+ "\002\uffed\004\041\005\uffed\006\uffed\007\uffed\010\uffed\011"
					+ "\uffed\012\uffed\017\uffed\020\uffed\021\uffed\001\002\000\020"
					+ "\002\ufffd\005\024\006\025\011\ufffd\017\ufffd\020\ufffd\021"
					+ "\ufffd\001\002\000\016\002\uffff\006\uffff\011\uffff\017\uffff"
					+ "\020\uffff\021\uffff\001\002\000\014\006\004\011\021\017"
					+ "\006\020\012\021\010\001\002\000\010\005\024\006\025"
					+ "\012\023\001\002\000\026\002\ufff4\005\ufff4\006\ufff4\007"
					+ "\ufff4\010\ufff4\011\ufff4\012\ufff4\017\ufff4\020\ufff4\021\ufff4"
					+ "\001\002\000\014\006\004\011\021\017\035\020\012\021"
					+ "\010\001\002\000\014\006\004\011\021\017\026\020\012"
					+ "\021\010\001\002\000\030\002\ufff3\005\ufff3\006\ufff3\007"
					+ "\ufff3\010\ufff3\011\ufff3\012\ufff3\013\034\017\ufff3\020\ufff3"
					+ "\021\ufff3\001\002\000\026\002\ufffa\005\ufffa\006\ufffa\007"
					+ "\031\010\030\011\ufffa\012\ufffa\017\ufffa\020\ufffa\021\ufffa"
					+ "\001\002\000\014\006\004\011\021\017\006\020\012\021"
					+ "\010\001\002\000\014\006\004\011\021\017\006\020\012"
					+ "\021\010\001\002\000\026\002\ufff7\005\ufff7\006\ufff7\007"
					+ "\ufff7\010\ufff7\011\ufff7\012\ufff7\017\ufff7\020\ufff7\021\ufff7"
					+ "\001\002\000\026\002\ufff6\005\ufff6\006\ufff6\007\ufff6\010"
					+ "\ufff6\011\ufff6\012\ufff6\017\ufff6\020\ufff6\021\ufff6\001\002"
					+ "\000\026\002\ufff9\005\ufff9\006\ufff9\007\ufff9\010\ufff9\011"
					+ "\ufff9\012\ufff9\017\ufff9\020\ufff9\021\ufff9\001\002\000\030"
					+ "\002\ufff3\005\ufff3\006\ufff3\007\ufff3\010\ufff3\011\ufff3\012"
					+ "\ufff3\013\037\017\ufff3\020\ufff3\021\ufff3\001\002\000\026"
					+ "\002\ufffc\005\ufffc\006\ufffc\007\031\010\030\011\ufffc\012"
					+ "\ufffc\017\ufffc\020\ufffc\021\ufffc\001\002\000\026\002\ufffb"
					+ "\005\ufffb\006\ufffb\007\ufffb\010\ufffb\011\ufffb\012\ufffb\017"
					+ "\ufffb\020\ufffb\021\ufffb\001\002\000\026\002\uffee\005\uffee"
					+ "\006\uffee\007\uffee\010\uffee\011\uffee\012\uffee\017\uffee\020"
					+ "\uffee\021\uffee\001\002\000\004\021\010\001\002\000\026"
					+ "\002\uffec\005\uffec\006\uffec\007\uffec\010\uffec\011\uffec\012"
					+ "\uffec\017\uffec\020\uffec\021\uffec\001\002\000\016\002\ufffe"
					+ "\006\ufffe\011\ufffe\017\ufffe\020\ufffe\021\ufffe\001\002\000"
					+ "\004\002\000\001\002\000\016\002\001\006\001\011\001"
					+ "\017\001\020\001\021\001\001\002\000\026\002\ufff0\005"
					+ "\ufff0\006\ufff0\007\ufff0\010\ufff0\011\ufff0\012\ufff0\017\ufff0"
					+ "\020\ufff0\021\ufff0\001\002" });

	/** Access to parse-action table. */
	@Override
	public short[][] action_table() {
		return _action_table;
	}

	/** <code>reduce_goto</code> table. */
	protected static final short[][] _reduce_table = unpackFromStrings(
			new String[] { "\000\044\000\022\002\006\003\017\004\016\005\013\006"
					+ "\004\007\014\010\012\012\015\001\001\000\016\004\045"
					+ "\005\013\006\004\007\014\010\012\012\015\001\001\000"
					+ "\002\001\001\000\002\001\001\000\020\003\044\004\016"
					+ "\005\013\006\004\007\014\010\012\012\015\001\001\000"
					+ "\002\001\001\000\020\003\042\004\016\005\013\006\004"
					+ "\007\014\010\012\012\015\001\001\000\002\001\001\000"
					+ "\002\001\001\000\002\001\001\000\002\001\001\000\004"
					+ "\011\037\001\001\000\002\001\001\000\002\001\001\000"
					+ "\016\004\021\005\013\006\004\007\014\010\012\012\015"
					+ "\001\001\000\002\001\001\000\002\001\001\000\014\005"
					+ "\035\006\004\007\014\010\012\012\015\001\001\000\014"
					+ "\005\026\006\004\007\014\010\012\012\015\001\001\000"
					+ "\002\001\001\000\002\001\001\000\012\006\032\007\014"
					+ "\010\012\012\015\001\001\000\012\006\031\007\014\010"
					+ "\012\012\015\001\001\000\002\001\001\000\002\001\001"
					+ "\000\002\001\001\000\002\001\001\000\002\001\001\000"
					+ "\002\001\001\000\002\001\001\000\006\010\041\012\015"
					+ "\001\001\000\002\001\001\000\002\001\001\000\002\001"
					+ "\001\000\002\001\001\000\002\001\001" });

	/** Access to <code>reduce_goto</code> table. */
	@Override
	public short[][] reduce_table() {
		return _reduce_table;
	}

	/** Instance of action encapsulation class. */
	protected CUP$parser$actions action_obj;

	/** Action encapsulation object initializer. */
	@Override
	protected void init_actions() {
		action_obj = new CUP$parser$actions(this);
	}

	/** Invoke a user supplied parse action. */
	@Override
	public java_cup.runtime.Symbol do_action(int act_num, java_cup.runtime.lr_parser parser, java.util.Stack stack,
			int top) throws java.lang.Exception {
		/* call code in generated class */
		return action_obj.CUP$parser$do_action(act_num, parser, stack, top);
	}

	/** Indicates start state. */
	@Override
	public int start_state() {
		return 0;
	}

	/** Indicates start production. */
	@Override
	public int start_production() {
		return 1;
	}

	/** <code>EOF</code> Symbol index. */
	@Override
	public int EOF_sym() {
		return 0;
	}

	/** <code>error</code> Symbol index. */
	@Override
	public int error_sym() {
		return 1;
	}

	public boolean verbose = false;

	public String errorMessage = null;

	/*
	 * set verbose to true to enable print of information about tokens
	 */
	public void setVerbose(boolean value) {
		this.verbose = value;
	}

	public boolean isVerbose() {
		return this.verbose;
	}

	SpagoBICellWrapper cellWrapper;
	PivotModel model;
	OlapDataSource olapDataSource;
	WhatIfEngineInstance engineInstance;

	/*
	 * Set important information to retrieve What-if context information. Ex: variable, members, etc...
	 */
	public void setWhatIfInfo(SpagoBICellWrapper cellWrapper, PivotModel model, OlapDataSource olapDataSource,
			WhatIfEngineInstance engineInstance) {
		this.cellWrapper = cellWrapper;
		this.model = model;
		this.olapDataSource = olapDataSource;
		this.engineInstance = engineInstance;
	}



	/*
	 * Calculate the Member Value from the cube passing a list of dimensional coordinates
	 */
	public Double getMemberValue(LinkedList members) {
		return CubeUtilities.getMemberValue(members, this.cellWrapper, this.model, this.olapDataSource,
				this.engineInstance.getModelConfig().getDimensionHierarchyMap(),
				this.engineInstance.getModelConfig().getAliases());
	}

	/*
	 * Change the method report_error so it will display the line and column of where the error occurred in the input as well as the reason for the error which is
	 * passed into the method in the String 'message'.
	 */
	@Override
	public void report_error(String message, Object info) {

		/* Create a StringBuilder called 'm' with the string 'Error' in it. */
		StringBuilder m = new StringBuilder("Error");

		/*
		 * Check if the information passed to the method is the same type as the type java_cup.runtime.Symbol.
		 */
		if (info instanceof java_cup.runtime.Symbol) {
			/*
			 * Declare a java_cup.runtime.Symbol object 's' with the information in the object info that is being typecasted as a java_cup.runtime.Symbol object.
			 */
			java_cup.runtime.Symbol s = ((java_cup.runtime.Symbol) info);

			/*
			 * Check if the line number in the input is greater or equal to zero.
			 */
			if (s.left >= 0) {
				/*
				 * Add to the end of the StringBuilder error message the line number of the error in the input.
				 */
				m.append(" in line " + (s.left + 1));
				/*
				 * Check if the column number in the input is greater or equal to zero.
				 */
				if (s.right >= 0) {
					/*
					 * Add to the end of the StringBuilder error message the column number of the error in the input.
					 */
					m.append(", column " + (s.right + 1));
				}
			}
			/*
			 * Print the value (if any) of the Symbol where an error was found
			 */
			if (s.value != null) {
				m.append(" on " + s.value);
			}
		}

		/*
		 * Add to the end of the StringBuilder error message created in this method the message that was passed into this method.
		 */
		m.append(" : " + message);

		/*
		 * Print the contents of the StringBuilder 'm', which contains an error message, out on a line.
		 */
		if (isVerbose()) {
			System.err.println(m);
		}

		errorMessage = m.toString();

	}

	/*
	 * Change the method report_fatal_error so when it reports a fatal error it will display the line and column number of where the fatal error occurred in the
	 * input as well as the reason for the fatal error which is passed into the method in the object 'message'
	 */
	@Override
	public void report_fatal_error(String message, Object info) throws java.lang.Exception {
		report_error(message, info);
		throw new java.lang.Exception("Error in parsing metalanguage expression: " + errorMessage);
	}

}

/** Cup generated class to encapsulate user supplied action code. */
class CUP$parser$actions {
	private final parser parser;

	/** Constructor */
	CUP$parser$actions(parser parser) {
		this.parser = parser;
	}

	/** Method 0 with the actual generated action code for actions 0 to 300. */
	public final java_cup.runtime.Symbol CUP$parser$do_action_part00000000(int CUP$parser$act_num,
			java_cup.runtime.lr_parser CUP$parser$parser, java.util.Stack CUP$parser$stack, int CUP$parser$top)
			throws java.lang.Exception {
		/* Symbol object for return from actions */
		java_cup.runtime.Symbol CUP$parser$result;

		/* select the action based on the action number */
		switch (CUP$parser$act_num) {
		/* . . . . . . . . . . . . . . . . . . . . */
		case 0: // expr_list ::= expr_list expr_part
		{
			Object RESULT = null;
			int lleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
			int lright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
			Object l = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
			int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Object e = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;

			CUP$parser$result = parser.getSymbolFactory().newSymbol("expr_list", 0,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 1: // $START ::= expr_list EOF
		{
			Object RESULT = null;
			int start_valleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
			int start_valright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
			Object start_val = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
			RESULT = start_val;
			CUP$parser$result = parser.getSymbolFactory().newSymbol("$START", 0,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			/* ACCEPT */
			CUP$parser$parser.done_parsing();
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 2: // expr_list ::= expr_part
		{
			Object RESULT = null;
			int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Object e = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;

			RESULT = e;

			CUP$parser$result = parser.getSymbolFactory().newSymbol("expr_list", 0,
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 3: // expr_list ::= EQUAL expr_part
		{
			Object RESULT = null;
			int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Object e = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;

			RESULT = e;

			CUP$parser$result = parser.getSymbolFactory().newSymbol("expr_list", 0,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 4: // expr_part ::= expr
		{
			Object RESULT = null;
			int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Double e = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
			if (parser.isVerbose()) {
				System.out.println(" = " + e);
			}
			RESULT = new Double(e.doubleValue());

			CUP$parser$result = parser.getSymbolFactory().newSymbol("expr_part", 1,
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 5: // expr ::= expr PLUS factor
		{
			Double RESULT = null;
			int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).left;
			int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).right;
			Double e = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).value;
			int fleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int fright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Double f = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
			RESULT = new Double(e.doubleValue() + f.doubleValue());
			CUP$parser$result = parser.getSymbolFactory().newSymbol("expr", 2,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 6: // expr ::= expr PLUS NUMBER PERCENT
		{
			Double RESULT = null;
			int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)).left;
			int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)).right;
			Double e = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)).value;
			int nleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
			int nright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
			Double n = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;

			Double perc = new Double((e.doubleValue() * n.doubleValue()) / 100);
			RESULT = new Double(e.doubleValue() + perc);

			CUP$parser$result = parser.getSymbolFactory().newSymbol("expr", 2,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 7: // expr ::= expr MINUS factor
		{
			Double RESULT = null;
			int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).left;
			int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).right;
			Double e = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).value;
			int fleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int fright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Double f = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
			RESULT = new Double(e.doubleValue() - f.doubleValue());
			CUP$parser$result = parser.getSymbolFactory().newSymbol("expr", 2,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 8: // expr ::= expr MINUS NUMBER PERCENT
		{
			Double RESULT = null;
			int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)).left;
			int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)).right;
			Double e = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)).value;
			int nleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
			int nright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
			Double n = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;

			Double perc = new Double((e.doubleValue() * n.doubleValue()) / 100);
			RESULT = new Double(e.doubleValue() - perc);

			CUP$parser$result = parser.getSymbolFactory().newSymbol("expr", 2,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 3)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 9: // expr ::= factor
		{
			Double RESULT = null;
			int fleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int fright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Double f = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
			RESULT = new Double(f.doubleValue());
			CUP$parser$result = parser.getSymbolFactory().newSymbol("expr", 2,
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 10: // factor ::= factor TIMES term
		{
			Double RESULT = null;
			int fleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).left;
			int fright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).right;
			Double f = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).value;
			int tleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int tright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Double t = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
			RESULT = new Double(f.doubleValue() * t.doubleValue());
			CUP$parser$result = parser.getSymbolFactory().newSymbol("factor", 3,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 11: // factor ::= factor DIVIDE term
		{
			Double RESULT = null;
			int fleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).left;
			int fright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).right;
			Double f = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)).value;
			int tleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int tright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Double t = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
			RESULT = new Double(f.doubleValue() / t.doubleValue());
			CUP$parser$result = parser.getSymbolFactory().newSymbol("factor", 3,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 12: // factor ::= term
		{
			Double RESULT = null;
			int tleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int tright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Double t = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
			RESULT = new Double(t.doubleValue());
			CUP$parser$result = parser.getSymbolFactory().newSymbol("factor", 3,
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 13: // term ::= LPAREN expr RPAREN
		{
			Double RESULT = null;
			int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
			int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
			Double e = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
			RESULT = e;
			CUP$parser$result = parser.getSymbolFactory().newSymbol("term", 4,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 2)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 14: // term ::= NUMBER
		{
			Double RESULT = null;
			int nleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int nright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Double n = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
			RESULT = n;
			CUP$parser$result = parser.getSymbolFactory().newSymbol("term", 4,
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 15: // term ::= VARIABLE
		{
			Double RESULT = null;
			int vleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int vright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			String v = (String) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;

			Object variableValue = parser.engineInstance.getVariableValue(v);
			if (variableValue instanceof Integer) {
				RESULT = new Double((Integer) variableValue);
			} else if (variableValue instanceof Double) {
				RESULT = (Double) variableValue;
			} else if (variableValue instanceof String) {
				String[] stringParts = ((String) variableValue).split(";");
				LinkedList member = new LinkedList(Arrays.asList(stringParts));
				RESULT = parser.getMemberValue(member);
			}

			if (parser.isVerbose()) {
				System.out.println("*** found VARIABLE [" + v + "] replaced with " + RESULT);
			}

			CUP$parser$result = parser.getSymbolFactory().newSymbol("term", 4,
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 16: // term ::= members_declaration
		{
			Double RESULT = null;
			int mleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int mright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			LinkedList m = (LinkedList) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;

			Double generated = parser.getMemberValue(m);
			if (parser.isVerbose()) {
				System.out.println("*** found MEMBERS " + m + " replaced with " + generated);
			}
			RESULT = generated;

			CUP$parser$result = parser.getSymbolFactory().newSymbol("term", 4,
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 17: // term ::= MINUS expr
		{
			Double RESULT = null;
			int eleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int eright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			Double e = (Double) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;
			RESULT = new Double(0 - e.doubleValue());
			CUP$parser$result = parser.getSymbolFactory().newSymbol("term", 4,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 18: // members_declaration ::= members_list
		{
			LinkedList RESULT = null;
			int members_lleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int members_lright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			LinkedList members_l = (LinkedList) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;

			LinkedList r = new LinkedList();
			String n;
			Iterator li = members_l.iterator();
			while (li.hasNext()) {
				n = (String) (li.next());
				r.add(n);
				// if(parser.isVerbose()){System.out.println("Added "+n);}
			}
			RESULT = r;
			if (parser.isVerbose()) {
				System.out.println("*** Members Declaration " + members_l);
			}

			CUP$parser$result = parser.getSymbolFactory().newSymbol("members_declaration", 5,
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 19: // members_list ::= single_member member_listS
		{
			LinkedList RESULT = null;
			int member_aleft = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).left;
			int member_aright = ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).right;
			String member_a = (String) ((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)).value;
			int members_lleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int members_lright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			LinkedList members_l = (LinkedList) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;

			members_l.add(member_a);
			RESULT = members_l;

			CUP$parser$result = parser.getSymbolFactory().newSymbol("members_list", 6,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 20: // member_listS ::=
		{
			LinkedList RESULT = null;

			RESULT = new LinkedList();

			CUP$parser$result = parser.getSymbolFactory().newSymbol("member_listS", 7,
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 21: // member_listS ::= SEMI members_list
		{
			LinkedList RESULT = null;
			int members_lleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int members_lright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			LinkedList members_l = (LinkedList) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;

			RESULT = members_l;

			CUP$parser$result = parser.getSymbolFactory().newSymbol("member_listS", 7,
					((java_cup.runtime.Symbol) CUP$parser$stack.elementAt(CUP$parser$top - 1)),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . . . . . . . . . . . . . . . */
		case 22: // single_member ::= MEMBER
		{
			String RESULT = null;
			int membleft = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).left;
			int membright = ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).right;
			String memb = (String) ((java_cup.runtime.Symbol) CUP$parser$stack.peek()).value;

			RESULT = memb;
			// if(parser.isVerbose()){System.out.println("MEMBER "+memb);}

			CUP$parser$result = parser.getSymbolFactory().newSymbol("single_member", 8,
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()),
					((java_cup.runtime.Symbol) CUP$parser$stack.peek()), RESULT);
		}
			return CUP$parser$result;

		/* . . . . . . */
		default:
			throw new Exception("Invalid action number " + CUP$parser$act_num + "found in internal parse table");

		}
	} /* end of method */

	/** Method splitting the generated action code into several parts. */
	public final java_cup.runtime.Symbol CUP$parser$do_action(int CUP$parser$act_num,
			java_cup.runtime.lr_parser CUP$parser$parser, java.util.Stack CUP$parser$stack, int CUP$parser$top)
			throws java.lang.Exception {
		return CUP$parser$do_action_part00000000(CUP$parser$act_num, CUP$parser$parser, CUP$parser$stack,
				CUP$parser$top);
	}
}
