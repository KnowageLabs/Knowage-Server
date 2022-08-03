package it.eng.knowage.engine.cockpit.api.export.pdf;

import static org.junit.Assert.assertEquals;

import java.awt.Color;

import org.junit.Test;

public class CssColorParserTest {

	@Test
	public void testLiteral() {

		CssColorParser instance = CssColorParser.getInstance();

		Color a = Color.RED;
		Color b = instance.parse("red", Color.BLACK);

		assertEquals(a, b);
	}

	@Test
	public void testRgb() {

		CssColorParser instance = CssColorParser.getInstance();

		Color a = new Color(1,2,3);
		Color b = instance.parse("rgb(1,2,3)", Color.BLACK);

		assertEquals(a, b);
	}

	@Test
	public void testRgba() {

		CssColorParser instance = CssColorParser.getInstance();

		Color a = new Color(1,2,3,4);
		Color b = instance.parse("rgba(1,2,3,4)", Color.BLACK);

		assertEquals(a, b);
	}

}
