import com.github.MrMks.template_lore.formula.FormulaAPI;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestFormula {
    @Test
    public void onTest(){
        Number number = FormulaAPI.cal("1+10+11+12*2-(10)");
        System.out.println(number);
    }

    @Test
    public void onTestPattern(){
        Pattern pattern = Pattern.compile("(\\d){0,7}");
        Matcher matcher = pattern.matcher("1234567");
        if (matcher.find()) {
            int size = matcher.groupCount();
            for (int i = 1; i <= size; i++) {
                System.out.println(matcher.group(1));
                System.out.println(matcher.group(2));
            }
        }
    }
}
