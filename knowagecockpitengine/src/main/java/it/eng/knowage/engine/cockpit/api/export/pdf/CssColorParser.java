package it.eng.knowage.engine.cockpit.api.export.pdf;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CssColorParser {

	private static final Logger LOGGER = LogManager.getLogger(CssColorParser.class);

	private static final CssColorParser INSTANCE = new CssColorParser();

	private static enum CssColorLiteral {
		ALICEBLUE(240, 248, 255),
		ANTIQUEWHITE(250, 235, 215),
		AQUA(0, 255, 255),
		AQUAMARINE(127, 255, 212),
		AZURE(240, 255, 255),
		BEIGE(245, 245, 220),
		BISQUE(255, 228, 196),
		BLACK(0, 0, 0),
		BLANCHEDALMOND(255, 235, 205),
		BLUE(0, 0, 255),
		BLUEVIOLET(138, 43, 226),
		BROWN(165, 42, 42),
		BURLYWOOD(222, 184, 135),
		CADETBLUE(95, 158, 160),
		CHARTREUSE(127, 255, 0),
		CHOCOLATE(210, 105, 30),
		CORAL(255, 127, 80),
		CORNFLOWERBLUE(100, 149, 237),
		CORNSILK(255, 248, 220),
		CRIMSON(220, 20, 60),
		CYAN(0, 255, 255),
		DARKBLUE(0, 0, 139),
		DARKCYAN(0, 139, 139),
		DARKGOLDENROD(184, 134, 11),
		DARKGRAY(169, 169, 169),
		DARKGREEN(0, 100, 0),
		DARKGREY(169, 169, 169),
		DARKKHAKI(189, 183, 107),
		DARKMAGENTA(139, 0, 139),
		DARKOLIVEGREEN(85, 107, 47),
		DARKORANGE(255, 140, 0),
		DARKORCHID(153, 50, 204),
		DARKRED(139, 0, 0),
		DARKSALMON(233, 150, 122),
		DARKSEAGREEN(143, 188, 143),
		DARKSLATEBLUE(72, 61, 139),
		DARKSLATEGRAY(47, 79, 79),
		DARKSLATEGREY(47, 79, 79),
		DARKTURQUOISE(0, 206, 209),
		DARKVIOLET(148, 0, 211),
		DEEPPINK(255, 20, 147),
		DEEPSKYBLUE(0, 191, 255),
		DIMGRAY(105, 105, 105),
		DIMGREY(105, 105, 105),
		DODGERBLUE(30, 144, 255),
		FIREBRICK(178, 34, 34),
		FLORALWHITE(255, 250, 240),
		FORESTGREEN(34, 139, 34),
		FUCHSIA(255, 0, 255),
		GAINSBORO(220, 220, 220),
		GHOSTWHITE(248, 248, 255),
		GOLD(255, 215, 0),
		GOLDENROD(218, 165, 32),
		GRAY(128, 128, 128),
		GREEN(0, 128, 0),
		GREENYELLOW(173, 255, 47),
		GREY(128, 128, 128),
		HONEYDEW(240, 255, 240),
		HOTPINK(255, 105, 180),
		INDIANRED(205, 92, 92),
		INDIGO(75, 0, 130),
		IVORY(255, 255, 240),
		KHAKI(240, 230, 140),
		LAVENDER(230, 230, 250),
		LAVENDERBLUSH(255, 240, 245),
		LAWNGREEN(124, 252, 0),
		LEMONCHIFFON(255, 250, 205),
		LIGHTBLUE(173, 216, 230),
		LIGHTCORAL(240, 128, 128),
		LIGHTCYAN(224, 255, 255),
		LIGHTGOLDENRODYELLOW(250, 250, 210),
		LIGHTGRAY(211, 211, 211),
		LIGHTGREEN(144, 238, 144),
		LIGHTGREY(211, 211, 211),
		LIGHTPINK(255, 182, 193),
		LIGHTSALMON(255, 160, 122),
		LIGHTSEAGREEN(32, 178, 170),
		LIGHTSKYBLUE(135, 206, 250),
		LIGHTSLATEGRAY(119, 136, 153),
		LIGHTSLATEGREY(119, 136, 153),
		LIGHTSTEELBLUE(176, 196, 222),
		LIGHTYELLOW(255, 255, 224),
		LIME(0, 255, 0),
		LIMEGREEN(50, 205, 50),
		LINEN(250, 240, 230),
		MAGENTA(255, 0, 255),
		MAROON(128, 0, 0),
		MEDIUMAQUAMARINE(102, 205, 170),
		MEDIUMBLUE(0, 0, 205),
		MEDIUMORCHID(186, 85, 211),
		MEDIUMPURPLE(147, 112, 219),
		MEDIUMSEAGREEN(60, 179, 113),
		MEDIUMSLATEBLUE(123, 104, 238),
		MEDIUMSPRINGGREEN(0, 250, 154),
		MEDIUMTURQUOISE(72, 209, 204),
		MEDIUMVIOLETRED(199, 21, 133),
		MIDNIGHTBLUE(25, 25, 112),
		MINTCREAM(245, 255, 250),
		MISTYROSE(255, 228, 225),
		MOCCASIN(255, 228, 181),
		NAVAJOWHITE(255, 222, 173),
		NAVY(0, 0, 128),
		OLDLACE(253, 245, 230),
		OLIVE(128, 128, 0),
		OLIVEDRAB(107, 142, 35),
		ORANGE(255, 165, 0),
		ORANGERED(255, 69, 0),
		ORCHID(218, 112, 214),
		PALEGOLDENROD(238, 232, 170),
		PALEGREEN(152, 251, 152),
		PALETURQUOISE(175, 238, 238),
		PALEVIOLETRED(219, 112, 147),
		PAPAYAWHIP(255, 239, 213),
		PEACHPUFF(255, 218, 185),
		PERU(205, 133, 63),
		PINK(255, 192, 203),
		PLUM(221, 160, 221),
		POWDERBLUE(176, 224, 230),
		PURPLE(128, 0, 128),
		RED(255, 0, 0),
		ROSYBROWN(188, 143, 143),
		ROYALBLUE(65, 105, 225),
		SADDLEBROWN(139, 69, 19),
		SALMON(250, 128, 114),
		SANDYBROWN(244, 164, 96),
		SEAGREEN(46, 139, 87),
		SEASHELL(255, 245, 238),
		SIENNA(160, 82, 45),
		SILVER(192, 192, 192),
		SKYBLUE(135, 206, 235),
		SLATEBLUE(106, 90, 205),
		SLATEGRAY(112, 128, 144),
		SLATEGREY(112, 128, 144),
		SNOW(255, 250, 250),
		SPRINGGREEN(0, 255, 127),
		STEELBLUE(70, 130, 180),
		TAN(210, 180, 140),
		TEAL(0, 128, 128),
		THISTLE(216, 191, 216),
		TOMATO(255, 99, 71),
		TURQUOISE(64, 224, 208),
		VIOLET(238, 130, 238),
		WHEAT(245, 222, 179),
		WHITE(255, 255, 255),
		WHITESMOKE(245, 245, 245),
		YELLOW(255, 255, 0),
		YELLOWGREEN(154, 205, 50);

		public static CssColorLiteral parse(String literal) {
			for (CssColorLiteral curr : values()) {
				if (curr.name().equalsIgnoreCase(literal)) {
					return curr;
				}
			}

			throw new IllegalArgumentException("Literal " + literal + " doesn not exist");
		}

		private final int r;
		private final int g;
		private final int b;

		private CssColorLiteral(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public Color toColor() {
			return new Color(r, g, b);
		}
	}

	/**
	 * Matches:
	 *  - rgba(1,2,3,4)
	 *  - rgb(1,2,3)
	 *  - rgba(1,2,3,4)
	 *  - hsl(1,2,3)
	 *  - hsla(1,2,3,4)
	 *  - red
	 *  - brown
	 * The available matching groups are:
	 *  - rgbr
	 *  - rgbg
	 *  - rgbb
	 *  - rgbar
	 *  - rgbag
	 *  - rgbab
	 *  - rgbaa
	 *  - hslh
	 *  - hsls
	 *  - hsll
	 *  - hslah
	 *  - hslas
	 *  - hslal
	 *  - hslaa
	 *  - literal
	 * The value to match mustn't contain spaces.
	 */
	private static final Pattern CSS_COLOR_PATTERN = Pattern.compile("^(?:rgb\\((?<rgbr>\\d+),(?<rgbg>\\d+),(?<rgbb>\\d+)\\))|(?:hsl\\((?<hslh>\\d+),(?<hsls>\\d+),(?<hsll>\\d+)\\))|(?:rgba\\((?<rgbar>\\d+),(?<rgbag>\\d+),(?<rgbab>\\d+),(?<rgbaa>\\d+)\\))|(?:hsla\\((?<hslah>\\d+),(?<hslas>\\d+),(?<hslal>\\d+),(?<hslaa>\\d+)\\))|(?<literal>aliceblue|antiquewhite|aqua|aqua|aquamarine|azure|beige|bisque|black|black|blanchedalmond|blue|blue|blueviolet|brown|burlywood|cadetblue|chartreuse|chocolate|coral|cornflowerblue|cornsilk|crimson|cyan|darkblue|darkcyan|darkgoldenrod|darkgray|darkgreen|darkgrey|darkkhaki|darkmagenta|darkolivegreen|darkorange|darkorchid|darkred|darksalmon|darkseagreen|darkslateblue|darkslategray|darkslategrey|darkturquoise|darkviolet|deeppink|deepskyblue|dimgray|dimgrey|dodgerblue|firebrick|floralwhite|forestgreen|fuchsia|fuchsia|gainsboro|ghostwhite|gold|goldenrod|gray|gray|green|green|greenyellow|grey|honeydew|hotpink|indianred|indigo|ivory|khaki|lavender|lavenderblush|lawngreen|lemonchiffon|lightblue|lightcoral|lightcyan|lightgoldenrodyellow|lightgray|lightgreen|lightgrey|lightpink|lightsalmon|lightseagreen|lightskyblue|lightslategray|lightslategrey|lightsteelblue|lightyellow|lime|lime|limegreen|linen|magenta|maroon|maroon|mediumaquamarine|mediumblue|mediumorchid|mediumpurple|mediumseagreen|mediumslateblue|mediumspringgreen|mediumturquoise|mediumvioletred|midnightblue|mintcream|mistyrose|moccasin|navajowhite|navy|navy|oldlace|olive|olive|olivedrab|orange|orangered|orchid|palegoldenrod|palegreen|paleturquoise|palevioletred|papayawhip|peachpuff|peru|pink|plum|powderblue|purple|purple|red|red|rosybrown|royalblue|saddlebrown|salmon|sandybrown|seagreen|seashell|sienna|silver|silver|skyblue|slateblue|slategray|slategrey|snow|springgreen|steelblue|tan|teal|teal|thistle|tomato|turquoise|violet|wheat|white|white|whitesmoke|yellow|yellow|yellowgreen)$", Pattern.CASE_INSENSITIVE);

	public static CssColorParser getInstance() {
		return INSTANCE;
	}

	private CssColorParser() {
		super();
	}

	public Color parse(String rgbColor, Color defaultColor) {
		Color ret = defaultColor;

		try {

			// Strip all spaces
			rgbColor = rgbColor.replaceAll("\\s", "");

			if (isNotEmpty(rgbColor)) {
				Matcher matcher = CSS_COLOR_PATTERN.matcher(rgbColor);

				if (matcher.find()) {

					if (nonNull(matcher.group("rgbr"))) {
						ret = new Color(
								Integer.parseInt(matcher.group("rgbr")),
								Integer.parseInt(matcher.group("rgbg")),
								Integer.parseInt(matcher.group("rgbb"))
							);
					} else if (nonNull(matcher.group("rgbar"))) {
						ret = new Color(
								Integer.parseInt(matcher.group("rgbar")),
								Integer.parseInt(matcher.group("rgbag")),
								Integer.parseInt(matcher.group("rgbab")),
								Integer.parseInt(matcher.group("rgbaa"))
							);
					} else if((nonNull(matcher.group("hslh")))) {
						throw new UnsupportedOperationException("The value " + rgbColor + " is not supported yet");
					} else if((nonNull(matcher.group("hslah")))) {
						throw new UnsupportedOperationException("The value " + rgbColor + " is not supported yet");
					} else if((nonNull(matcher.group("literal")))) {
						ret = CssColorLiteral.parse(matcher.group("literal")).toColor();
					} else {
						throw new UnsupportedOperationException("The value " + rgbColor + " is not supported yet");
					}


				} else {
					LOGGER.error("Cannot create color from string {" + rgbColor + "}. Default color {" + defaultColor + "} will be used");
				}
			}


		} catch (Exception e) {
			LOGGER.error("Cannot create color from string {" + rgbColor + "}. Default color {" + defaultColor + "} will be used", e);
		}

		return ret;
	}

}
