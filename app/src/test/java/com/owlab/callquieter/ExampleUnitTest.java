package com.owlab.callquieter;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void patternMatch() throws Exception {
        String ph0 = "0312854150";
        String ph1 = "07082532775";
        String ph2 = "01092532775";
        String ph3 = "\\+123";

        String pp0 = "0158";
        String pp1 = "1588";

        StringBuilder sb = new StringBuilder();
        sb
                .append(ph0).append("|")
                .append(ph1).append("|")
                .append(ph2).append("|")
                .append(ph3).append("|")
                .append(pp0).append(".*").append("|")
                .append(pp1).append(".*")
        ;

        Pattern pattern = Pattern.compile(sb.toString());

        Matcher matcher = pattern.matcher("1588");
        assertEquals(true, matcher.matches());

        matcher = pattern.matcher("15881");
        assertEquals(true, matcher.matches());

        matcher = pattern.matcher("031");
        assertEquals(false, matcher.matches());

        matcher = pattern.matcher("015");
        assertEquals(false, matcher.matches());

        matcher = pattern.matcher("9253");
        assertEquals(false, matcher.matches());

        matcher = pattern.matcher("+123");
        assertEquals(true, matcher.matches());
    }
}