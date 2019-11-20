package it.eng.knowage.parsers.tests;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import it.eng.knowage.parsers.CaseChangingCharStream;
import it.eng.knowage.parsers.SQLiteLexer;
import it.eng.knowage.parsers.SQLiteParser;
import it.eng.knowage.parsers.ThrowingErrorListener;
import junit.framework.TestCase;


public class AntlrSQLitelParserTest extends TestCase {


	public static void main( String[] args )
	{
		String query = "select SUM(prova) pippo pluto";

	//	String query = "pippo";
		CharStream inputStream = CharStreams.fromString(query);

		SQLiteLexer tokenSource = new SQLiteLexer(new CaseChangingCharStream(inputStream, true));


 		TokenStream tokenStream = new CommonTokenStream(tokenSource);
 		SQLiteParser sQLiteParser = new SQLiteParser(tokenStream);

 		sQLiteParser.addErrorListener(ThrowingErrorListener.INSTANCE);
		ParseTree root = sQLiteParser.select_stmt();


		try {

		System.out.println( 	root.toStringTree());
		System.out.println( sQLiteParser.getNumberOfSyntaxErrors());

		}
		catch(Exception e) {
			System.out.println(e);
		}


// 		MySqlParserBaseVisitor visitor = new MySqlParserVisitorImpl();
//		visitor.visit(root);
	}
}
