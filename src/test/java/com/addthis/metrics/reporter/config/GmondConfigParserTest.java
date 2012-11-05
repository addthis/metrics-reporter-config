package com.addthis.metrics.reporter.config;

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class GmondConfigParserTest
{
    //@Test
    public void print() throws Exception
    {
        GmondConfigParser g = new GmondConfigParser();
        String conf = g.readFile("src/test/resources/gmond/comments.conf");
        System.out.println(conf);
    }

    @Test
    public void stripAllComments() throws Exception
    {
        GmondConfigParser g = new GmondConfigParser();
        String conf = g.readFile("src/test/resources/gmond/ctest.conf");
        String expt_conf = g.readFile("src/test/resources/gmond/ctest-expt.conf");
        String clean = g.stripComments(conf);
        assertEquals(expt_conf, clean);
    }

    @Test
    public void emptyLines() throws Exception
    {
        GmondConfigParser g = new GmondConfigParser();
        String conf = g.readFile("src/test/resources/gmond/empty-lines.conf");
        String expt_conf = g.readFile("src/test/resources/gmond/empty-lines-expt.conf");
        String clean = g.removeEmptyLines(conf);
        assertEquals(expt_conf, clean);
    }

    @Test
    public void extractChannel() throws Exception
    {
        GmondConfigParser g = new GmondConfigParser();
        String conf = g.readFile("src/test/resources/gmond/udp-send.conf");
        List<String> blobs = g.findSendChannels(conf);
        //System.out.println(blobs);
        // two channels, each with 4 config lines
        assertEquals(2, blobs.size());
        assertEquals(4, blobs.get(0).split("\n").length);
        assertEquals(4, blobs.get(1).split("\n").length);
    }

    @Test
    public void stringMapIfy() throws Exception
    {
        String sc = "  bind_hostname = no \n  host = \"bar.local\"\n  port = 8649\n  ttl = 1";
        Map<String,String> expt = ImmutableMap.of("bind_hostname", "no",
                                                  "host", "bar.local",
                                                  "port", "8649",
                                                  "ttl", "1");
        GmondConfigParser g = new GmondConfigParser();
        Map<String, String> chan = g.mapifyChannelString(sc);
        assertEquals(expt, chan);
    }


    @Test
    public void hostPort() throws Exception
    {
        Map<String,String> chan = ImmutableMap.of("bind_hostname", "no",
                                                  "host", "bar.local",
                                                  "port", "8649",
                                                  "ttl", "1");
        GmondConfigParser g = new GmondConfigParser();
        HostPort hp = g.makeHostPort(chan);
        assertEquals("bar.local", hp.getHost());
        assertEquals(8649, hp.getPort());
    }


    @Test
    public void endToEnd() throws Exception
    {
        GmondConfigParser g = new GmondConfigParser();
        List<HostPort> hosts = g. getGmondSendChannels("src/test/resources/gmond/comments.conf");
        //System.out.println(hosts);
        assertEquals(2, hosts.size());
        assertEquals("foo1.local", hosts.get(0).getHost());
        assertEquals(8649, hosts.get(0).getPort());
        assertEquals("foo2.local", hosts.get(1).getHost());
        assertEquals(8649, hosts.get(1).getPort());
    }
}
