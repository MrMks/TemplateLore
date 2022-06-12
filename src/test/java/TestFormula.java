import com.github.mrmks.mc.template.FormulaAPI;
import com.github.mrmks.mc.template.ITokenProvider;
import com.github.mrmks.mc.template.ParseUtils;
import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestFormula {
    @Test
    public void onTest(){
        // Number number = FormulaAPI.cal("1+10+11+12*2-(10 - 10)");
        // System.out.println(number);
    }

    @Test
    public void onTestPattern(){
        Pattern pattern = Pattern.compile("(\\d){0,7}");
        Matcher matcher = pattern.matcher("1234567");
        if (matcher.find()) {
            int size = matcher.groupCount();
            for (int i = 1; i <= size; i++) {
                System.out.println(matcher.group(1));
            }
        }
    }

    @Test
    public void testDecimal() {
        DecimalFormat f = new DecimalFormat();
        f.applyPattern("#.##");
        Assert.assertEquals(f.format(12313.99999), "12314");
    }

    @Test
    public void testParseUtils() {
        System.out.println(ParseUtils.randomInt("10  , 10"));

        Assert.assertNull(ParseUtils.repeatSpace(""));
        Assert.assertNull(ParseUtils.repeatSpace("ksd"));
        Assert.assertNull(ParseUtils.repeatSpace("-4"));
        Assert.assertEquals("    ", ParseUtils.repeatSpace("4"));
        Assert.assertEquals("", ParseUtils.repeatSpace("0"));

        Assert.assertNull(ParseUtils.repeat(""));
        Assert.assertNull(ParseUtils.repeat("sdfddadc,"));
        Assert.assertNull(ParseUtils.repeat(",fjids"));
        Assert.assertNull(ParseUtils.repeat("fkdlsc,k"));
        Assert.assertNull(ParseUtils.repeat("dd,-1"));
        Assert.assertEquals("", ParseUtils.repeat("dd,0"));
        Assert.assertEquals("dscdscdsc", ParseUtils.repeat("dsc,3"));

        Assert.assertNull(ParseUtils.format(""));
        Assert.assertNull(ParseUtils.format(","));
        Assert.assertNull(ParseUtils.format("29304,"));
        Assert.assertNull(ParseUtils.format(",#.##"));
        Assert.assertNull(ParseUtils.format("kijhl,#.##"));
        Assert.assertEquals("kngytd293849tfd", ParseUtils.format("293849.0924,kngytd#tfd"));
        Assert.assertEquals("1452.2450", ParseUtils.format("1452.245,#.0000"));
        Assert.assertEquals("12015", ParseUtils.format("12015.0001245,#.##"));
    }

    @Test
    public void testParseCal() {
        Assert.assertEquals(46d, FormulaAPI.mathCal("1+10+11+12*2-(10 + -10)"));
    }

    @Test
    public void testParseMain() {
        ITokenProvider pv = new ITokenProvider() {
            @Override
            public boolean has(String tk) {
                return true;
            }

            @Override
            public String parse(String tk, String val) {
                return tk + val;
            }

        };

        ParseUtils.parse("abcsdf零零七sdf<a:bsd>, jjjf<l:我是搓搓搓<c:908>热热热>", pv);
    }
}
