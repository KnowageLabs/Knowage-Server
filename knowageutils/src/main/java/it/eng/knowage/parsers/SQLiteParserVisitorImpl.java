package it.eng.knowage.parsers;

import org.antlr.v4.runtime.misc.Interval;

public class SQLiteParserVisitorImpl extends SQLiteBaseVisitor<String>
{


//    @Override
//    public String visitAggregateFunctionCall(MySqlParser.AggregateFunctionCallContext context)
//    {
//      //  System.out.print(context.getText());
//
//        return visitChildren(context);
//    }

    @Override
    public String visitSelect_stmt(SQLiteParser.Select_stmtContext context)
    {
    	int a = context.start.getStartIndex();
    	int b = context.stop.getStopIndex();
    	Interval interval = new Interval(a,b);
    	String viewSql = context.start.getInputStream().getText(interval);

    	System.out.println(viewSql);
        return visitChildren(context);
    }

//    @Override
//    public String visitSelectExpressionElement(MySqlParser.SelectExpressionElementContext context)
//    {
//    	int a = context.start.getStartIndex();
//    	int b = context.stop.getStopIndex();
//    	Interval interval = new Interval(a,b);
//    	String viewSql = context.start.getInputStream().getText(interval);
//
//    	System.out.println(viewSql);
//        return visitChildren(context);
//    }

//    @Override
//    public String visitSelectElements(MySqlParser.SelectElementsContext context)
//    {
//    	int a = context.start.getStartIndex();
//    	int b = context.stop.getStopIndex();
//    	Interval interval = new Interval(a,b);
//    	String viewSql = context.start.getInputStream().getText(interval);
//
//    	System.out.println(viewSql);
//        return visitChildren(context);
//    }
}
