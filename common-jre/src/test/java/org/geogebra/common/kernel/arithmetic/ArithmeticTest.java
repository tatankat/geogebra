package org.geogebra.common.kernel.arithmetic;

import org.geogebra.common.AppCommonFactory;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.commands.AlgebraProcessor;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoFunction;
import org.geogebra.common.main.AppCommon3D;
import org.geogebra.test.TestStringUtil;
import org.geogebra.test.commands.AlgebraTestHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.himamis.retex.editor.share.util.Unicode;

public class ArithmeticTest extends Assert {

	private AlgebraProcessor ap;
	private AppCommon3D app;

	@Before
	public void setup() {
		app = AppCommonFactory.create3D();
		ap = app.getKernel().getAlgebraProcessor();
	}

	@Before
	public void clean() {
		app.getSettings().getCasSettings().setEnabled(true);
		app.getKernel().clearConstruction(true);
	}

	private void t(String input, String expected) {
		t(input, expected, StringTemplate.xmlTemplate);
	}

	private void t(String input, String expected, StringTemplate tpl) {
		AlgebraTestHelper.checkSyntaxSingle(input, new String[] { expected }, ap,
				tpl);
	}

	@Test
	public void listArithmetic() {
		t("{1,2,3}*2", "{2, 4, 6}");
		t("{1,2,3}+3", "{4, 5, 6}");
		t("list1:={1,2,3}", "{1, 2, 3}");
		t("listF:={x, 2 * x,3 * x+1}", "{x, (2 * x), (3 * x) + 1}");
		t("matrix1:={{1, 2, 3}, {2, 4, 6}, {3, 6, 9}}",
				"{{1, 2, 3}, {2, 4, 6}, {3, 6, 9}}");
		t("aa:=1", "1");
		t("matrix2:={{aa}}", "{{1}}");
		// app.getKernel().lookupLabel("matrix2").setFixed(true);
		t("list1(1)", "1");
		t("list1(4)", "NaN");
		t("list1(0)", "NaN");
		t("list1(-1)", "3");
		t("list1(-5)", "NaN");
		t("list1(1,2)", "NaN");
		t("listF(1)", "x");
		t("listF(2)", "(2 * x)");
		t("listF(2,7)", "14");
		t("matrix1(2)", "{2, 4, 6}");
		t("matrix1(-1)", "{3, 6, 9}");
		t("matrix1(-5)", "{NaN, NaN, NaN}");
		t("matrix1(2,3)", "6");
		t("matrix1(2,3,4)", "NaN");
		t("matrix1(2,-1)", "6");
		t("matrix1(5,2)", "NaN");
		t("matrix1(2,5)", "NaN");
		t("matrix2(1,2)", "NaN");
		t("matrix2(2,1)", "NaN");
	}

	@Test
	public void logSyntaxTest() {
		t("log_{2}2/(2)", "0.5");
	}

	@Test
	public void infinity() {
		t("0^inf", "0");
		t("1^inf", "NaN");
		t("1^(-inf)", "NaN");
		t("2^inf", "Infinity");
		t("2.1^inf", "Infinity");
		t("(-1)^inf", "NaN");
		t("inf^inf", "Infinity");
		t("?^inf", "NaN");
		t("inf^?", "NaN");
		t("(-inf)^inf", "Infinity");
		t("inf^(-inf)", "0");
		t("(-inf)^(-inf)", "0");
		t("inf^0", "NaN");
		t("inf^(-1)", "0");
		t("1/inf", "0");
		t("0/inf", "0");
		t("1*inf", "Infinity");
		t("2*inf", "Infinity");
		t("inf*1", "Infinity");
		t("inf*-1", "-Infinity");
		t("0*inf", "NaN");
		t("inf*0", "NaN");
		t("inf+1", "Infinity");
		t("inf+0", "Infinity");
		t("inf-0", "Infinity");
		t("inf-1", "Infinity");
		t("1-inf", "-Infinity");
		t("1+inf", "Infinity");
		t("0+inf", "Infinity");
		t("sin(inf)", "NaN");
		t("cos(inf)", "NaN");
		t("tan(inf)", "NaN");
		t("ln(inf)", "Infinity");
		t("exp(inf)", "Infinity");
		t("sin(-inf)", "NaN");
		t("cos(-inf)", "NaN");
		t("tan(-inf)", "NaN");
		t("ln(-inf)", "NaN");
		t("exp(-inf)", "0");
		t("inf * inf", "Infinity");
		t("inf * -inf", "-Infinity");
		t("-inf * inf", "-Infinity");
		t("-inf * -inf", "Infinity");
		t("inf + inf", "Infinity");
		t("inf + -inf", "NaN");
		t("-inf + inf", "NaN");
		t("-inf + -inf", "-Infinity");
		t("inf - inf", "NaN");
		t("inf - -inf", "Infinity");
		t("-inf - inf", "-Infinity");
		t("-inf - -inf", "NaN");
		t("inf / inf", "NaN");
		t("inf / -inf", "NaN");
		t("-inf / inf", "NaN");
		t("-inf / -inf", "NaN");
		t("inf * ?", "NaN");
		t("? * inf", "NaN");
		t("? * ?", "NaN");
		t("inf / ?", "NaN");
		t("? / inf", "NaN");
		t("? / ?", "NaN");
	}

	@Test
	public void testRounding() {
		final int angleUnit = app.getKernel().getAngleUnit();

		app.getKernel().setAngleUnit(Kernel.ANGLE_RADIANT);

		t("round(6740340335894, 5)", "6740340335894");
		t("round(6.740340335894E12, 5.0)", "6740340335894");
		t("round(6.740340335894E10, 10.0)", "6.740340335894E10");
		t("round(6.740340335894E10, 7.0)", "6.740340335894E10");
		t("round(6.740340335894E10, 8.0)", "6.740340335894E10");
		t("round(6.740340335894E10, 8.0)", "6.740340335894E10");
		t("round(6.740340335894E10, 10.0)", "6.740340335894E10");
		t("round(6.740340335894E10, 7.0)", "6.740340335894E10");
		t("round(6.740340335894E10, 8.0)", "6.740340335894E10");
		t("round(6.740340335894E10, 10.0)", "6.740340335894E10");
		t("round(6.740340335894E9, 8.0)", "6.740340335894E9");
		t("round(6.740340335894E8, 7.0)", "6.740340335894E8");
		t("round(6.740340335894E7, 8.0)", "6.740340335894E7");

		app.getKernel().setAngleUnit(angleUnit);
	}

	@Test
	public void functionArithmetic() {
		t("f(x)=x^2", "x^(2)");
		t("g1:f+f", "x^(2) + x^(2)");
		t("g2:f+x", "x^(2) + x");
		t("g3:x+f", "x + x^(2)");
		t("g4:f+f", "x^(2) + x^(2)");
		t("g5:f(x)+f", "x^(2) + x^(2)");
		t("g6(t)=t+f", "t + t^(2)");
		t("a(x,y)=x + y", "x + y");
		t("a+f", "x + y + x^(2)");
		t("a+x", "x + y + x");
		t("a+y", "x + y + y");
		t("a+a", "x + y + x + y");
		AlgebraTestHelper.shouldFail("f+y", "Please check your input", app);
		AlgebraTestHelper.shouldFail("y+f", "Please check your input", app);
	}

	@Test
	public void tuples() {
		t("(1..2,1..2)", "{(1, 1), (2, 2)}");
		t("(1,1..5)", "{(1, 1), (1, 2), (1, 3), (1, 4), (1, 5)}");
		t("(1,1..5,6..2)",
				"{(1, 1, 6), (1, 2, 5), (1, 3, 4), (1, 4, 3), (1, 5, 2)}");
		t("(1;(1..5)*2pi/5)",
				TestStringUtil.unicode(
						"{(1; 72deg), (1; 144deg), (1; 216deg), (1; 288deg), (1; 0deg)}"),
				StringTemplate.editTemplate);
	}

	@Test
	public void equationListTest() {
		t("x+y=1..5",
				"{x + y = 1, x + y = 2, x + y = 3, x + y = 4, x + y = 5}");
		t("x^2+y^2=1..5",
				TestStringUtil.unicode(
						"{x^2 + y^2 = 1," + " x^2 + y^2 = 2, x^2 + y^2 = 3,"
								+ " x^2 + y^2 = 4, x^2 + y^2 = 5}"),
				StringTemplate.editTemplate);
		t("f(r)=(r,sin(r)*(1..5))",
				"{(r, (sin(r) * 1)), (r, (sin(r) * 2)), (r, (sin(r) * 3)),"
						+ " (r, (sin(r) * 4)), (r, (sin(r) * 5))}");
		t("f(r)=(r+(1..5),sin(r)*(1..5))",
				"{(r + 1, (sin(r) * 1)), (r + 2, (sin(r) * 2)), (r + 3, (sin(r) * 3)),"
						+ " (r + 4, (sin(r) * 4)), (r + 5, (sin(r) * 5))}");
		t("f(r)=((1..5)*r,sin(r)+1)",
				"{((1 * r), sin(r) + 1), ((2 * r), sin(r) + 1), ((3 * r), sin(r) + 1),"
						+ " ((4 * r), sin(r) + 1), ((5 * r), sin(r) + 1)}");
	}

	@Test
	public void functionsList() {
		t("(1..2)+x*(1..2)", "{1 + (x * 1), 2 + (x * 2)}");
		t("x+y+(1..3)", "{x + y + 1, x + y + 2, x + y + 3}");
		t("list1=(-2..2)", "{-2, -1, 0, 1, 2}");
		t("(list1*t,(1-t)*(1-list1))",
				"{((-2 * t), ((1 - t) * 3)), ((-1 * t), ((1 - t) * 2)), ((0 * t), "
						+ "((1 - t) * 1)), ((1 * t), ((1 - t) * 0)), ((2 * t), ((1 - t) * (-1)))}");
	}

	@Test
	public void xcoordAsFunction() {
		t("x((1,2))", "1");
	}

	@Test
	public void absFunction() {
		AlgebraTestHelper.enableCAS(app, false);
		t("f:abs(x+2)", "abs(x + 2)");
		Assert.assertTrue(((GeoFunction) lookup("f"))
				.isPolynomialFunction(true));
		Assert.assertFalse(((GeoFunction) lookup("f"))
				.isPolynomialFunction(false));
	}

	@Test
	public void xcoordAsMultiplication() {
		t("x(x+1)", "(x * (x + 1))");
		t("x(x+1)^2", "(x * (x + 1)^(2))");
	}

	@Test
	public void derivativeShouldBeHiddenWithNoCas() {
		app.getSettings().getCasSettings().setEnabled(false);
		t("f(x)=x", "x");
		t("f'", "NDerivative[f]");
		t("f'(7)", "1");
	}

	@Test
	public void crossProductMissingBracketsTest() {
		t("A = (1, 2)", "(1, 2)");
		t("B = (2, 3)", "(2, 3)");
		t("C = (6, 3)", "(6, 3)");
		t("D = (0, 4)", "(0, 4)");
		t("E = Cross(A - B, C - D)", "7");
		assertEquals("(A - B) " + Unicode.VECTOR_PRODUCT + " (C - D)",
				lookup("E").getDefinition(StringTemplate.defaultTemplate));

		t("F = Cross(A, B)^2", "1");
		assertEquals("(A " + Unicode.VECTOR_PRODUCT + " B)" + Unicode.SUPERSCRIPT_2,
				lookup("F").getDefinition(StringTemplate.defaultTemplate));

		t("G = (1, 2, 3)", "(1, 2, 3)");
		t("H = (2, 3, 4)", "(2, 3, 4)");
		t("I = Cross(G, H)^2", "6");
		assertEquals("(G " + Unicode.VECTOR_PRODUCT + " H)" + Unicode.SUPERSCRIPT_2,
				lookup("I").getDefinition(StringTemplate.defaultTemplate));
	}

	@Test
	public void absFunctionBugFix() {
		app.getSettings().getCasSettings().setEnabled(true);
		t("eq1:abs(x-3) = -2", "abs(x - 3) = -2");
		t("eq2:abs(x-3) = 2", "x^2 - 6x = -5");
	}

	@Test
	public void evaluationOfUndefinedFunctionShouldBeUndefined() {
		t("f=Element({x y}, 2)", "?");
		t("f(1, 1)", "NaN");
		t("f((1, 1))", "NaN");
		t("g=Element({x}, 2)", "?");
		t("g(1)", "NaN");
		t("g((1, 1))", "NaN");
	}

	@Test
	public void functionCopySHouldBeDependent() {
		t("f:x", "x");
		t("g(x)=f", "x");
		assertEquals("f(x)",
				lookup("g").getDefinition(StringTemplate.defaultTemplate));
		t("ff(x,y)=x+y", "x + y");
		t("gg(a,b)=ff", "a + b");
		assertEquals("ff(a, b)",
				lookup("gg").getDefinition(StringTemplate.defaultTemplate));
	}

	@Test
	public void inequalityShouldNotHaveExtraBrackets() {
		t("r:4 < x < 5", "4 < x < 5");
		t("a = 1", "1");
		t("b = 2", "2");
		t("p1:a < x", "1 < x");
		t("p2:a < x < b", "1 < x < 2");
		t("p3:(a < x) + (x < b)", "(1 < x) + (x < 2)");
		t("p4:a < (x + x) < b", "1 < x + x < 2");
	}

	@Test
	public void complexPowers() {
		t("real((1+i)^(0..8))",
				"{1, 1, 0, -2, -4, -4, 0, 8, 16}", StringTemplate.editTemplate);
		t("imaginary((1+i)^(0..8))",
				"{0, 1, 2, 2, 0, -4, -8, -8, 0}", StringTemplate.editTemplate);
	}

	@Test
	public void complexTrigonometry() {
		t("tan(atan(1+0.5i))", "1 + 0.5" + Unicode.IMAGINARY,
				StringTemplate.editTemplate);
		t("sin(asin(1+0.5i))", "1 + 0.5" + Unicode.IMAGINARY,
				StringTemplate.editTemplate);
		t("cos(acos(1+0.5i))", "1 + 0.5" + Unicode.IMAGINARY,
				StringTemplate.editTemplate);
	}

	@Test
	public void powerWithNegativeFractionAsExponent() {
		t("(-8)^(-(1/3))", "-0.5");
		t("-8^(-1/3)", "-0.5");
		t("-8^(-2/3)", "-0.25");
		t("32^(-1/5)", "0.5");
	}

	@Test
	public void testAVShortIf() {
		t("3,5>x", "If[5 > x, 3]");
	}

	private GeoElement lookup(String g) {
		return app.getKernel().lookupLabel(g);
	}
}