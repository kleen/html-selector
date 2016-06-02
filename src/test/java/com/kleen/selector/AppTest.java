package com.kleen.selector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        String ss = "<div class='notification note-success'><a href='#' class='close'></a>\n" +
                "            <p>用户已建立。 <a href=\"/iredadmin/create/user/tssup.com\">再添加一个？</a></p>\n" +
                "        </div>";
        Html html = new Html(ss);
        System.out.println(html.$(".notification p").get().contains("用户已建立"));


    }
}
