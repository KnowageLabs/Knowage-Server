package it.eng.knowage.parsers.tests;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import it.eng.knowage.parsers.CaseChangingCharStream;
import it.eng.knowage.parsers.MySqlLexer;
import it.eng.knowage.parsers.MySqlParser;
import junit.framework.TestCase;


public class AntlrMysqlParserTest extends TestCase {


	public static void main( String[] args )
	{
		String query = "select SUM(col1)/AVG(col2) as prova , CONCAT(first_name, \" \", last_name) AS Name from pippo";

	//	String query = "pippo";
		CharStream inputStream = CharStreams.fromString(query);

		MySqlLexer tokenSource = new MySqlLexer(new CaseChangingCharStream(inputStream, true));

 		TokenStream tokenStream = new CommonTokenStream(tokenSource);
		MySqlParser mySqlParser = new MySqlParser(tokenStream);



		System.out.println( mySqlParser.getNumberOfSyntaxErrors());


	//

 		//MySqlParserBaseVisitor visitor = new MySqlParserVisitorImpl();
		//visitor.visit(root);
	}
}
