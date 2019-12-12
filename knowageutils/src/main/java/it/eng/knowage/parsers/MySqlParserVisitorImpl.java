package it.eng.knowage.parsers;

import org.antlr.v4.runtime.misc.Interval;

public class MySqlParserVisitorImpl extends MySqlParserBaseVisitor<String>
{


    @Override
    public String visitAggregateFunctionCall(MySqlParser.AggregateFunctionCallContext context)
    {
      //  System.out.print(context.getText());

        return visitChildren(context);
    }

    @Override
    public String visitSqlStatement(MySqlParser.SqlStatementContext context)
    {
    	int a = context.start.getStartIndex();
    	int b = context.stop.getStopIndex();
    	Interval interval = new Interval(a,b);
    	String viewSql = context.start.getInputStream().getText(interval);

    	System.out.println(viewSql);
        return visitChildren(context);
    }
}
