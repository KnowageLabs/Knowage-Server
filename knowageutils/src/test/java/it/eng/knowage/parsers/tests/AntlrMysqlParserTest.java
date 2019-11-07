package it.eng.knowage.parsers.tests;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import it.eng.knowage.parsers.CaseChangingCharStream;
import it.eng.knowage.parsers.MySqlLexer;
import it.eng.knowage.parsers.MySqlParser;
import it.eng.knowage.parsers.MySqlParserBaseVisitor;
import it.eng.knowage.parsers.MySqlParserVisitorImpl;
import junit.framework.TestCase;


public class AntlrMysqlParserTest extends TestCase {


	public static void main( String[] args )
	{
		String query = "select SUM(col1)/AVG(col2) as prova , CONCAT(first_name, \" \", last_name) AS Name from pippo";
		CharStream inputStream = CharStreams.fromString(query);

		MySqlLexer tokenSource = new MySqlLexer(new CaseChangingCharStream(inputStream, true));

 		TokenStream tokenStream = new CommonTokenStream(tokenSource);
		MySqlParser mySqlParser = new MySqlParser(tokenStream);

		ParseTree root = mySqlParser.sqlStatement();

	//	System.out.println(root.to);

 		MySqlParserBaseVisitor visitor = new MySqlParserVisitorImpl();
		visitor.visit(root);
	}
}
