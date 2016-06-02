package com.tssup.selector;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author kleen888@gmai.com
 */
public class JsonTest {

    private String text = "callback({\"name\":\"json\"})";

    private String textWithBrackerInContent = "callback({\"name\":\"json)\"})";

    @Test
    public void testRemovePadding() throws Exception {
        String name = new Json(text).removePadding("callback").jsonPath("$.name").get();
        assertThat(name).isEqualTo("json");
    }

    @Test
    public void testRemovePaddingForQuotes() throws Exception {
        String name = new Json(textWithBrackerInContent).removePadding("callback").jsonPath("$.name").get();
        assertThat(name).isEqualTo("json)");
    }
}
