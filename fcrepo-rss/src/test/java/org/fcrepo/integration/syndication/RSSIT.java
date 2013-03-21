
package org.fcrepo.integration.syndication;

import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

public class RSSIT extends AbstractResourceIT {

    final private Logger logger = LoggerFactory.getLogger(RSSIT.class);

    @Test
    public void testRSS() throws Exception {

        assertEquals(201, getStatus(new HttpPost(serverAddress +
                "/v3/objects/RSSTESTPID")));

        HttpGet getRSSMethod = new HttpGet(serverAddress + "/rss");
        HttpResponse response = client.execute(getRSSMethod);
        assertEquals(200, response.getStatusLine().getStatusCode());
        String content = EntityUtils.toString(response.getEntity());
        logger.debug("Retrieved RSS feed:\n" + content);
        assertTrue("Didn't find the test PID in RSS!", compile("RSSTESTPID",
                DOTALL).matcher(content).find());
    }
}