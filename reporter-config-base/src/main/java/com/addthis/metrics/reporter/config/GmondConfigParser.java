/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.addthis.metrics.reporter.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
  So I couldn't find any java parsers for libconfuse, which is sad.  Didn't
  want to bundle a full scale parser into this project.  Been a while
  since lex or yacc.

  http://xkcd.com/208/
 */
public class GmondConfigParser
{
    private static final Logger log = LoggerFactory.getLogger(GmondConfigParser.class);

    private static final String cCommentPattern = "//.*?\n"; //
    private static final String cppCommentPattern = "(?s)//*.*?/*/"; /* */
    private static final String emptyLinePattern = "(?m)^\\s*\n";
    private static final String udpSendPattern = "(?s)udp_send_channel\\s*\\{(.*?)\\}";


    public List<HostPort> getGmondSendChannels(String fileName)
    {
        try
        {
            String conf = readFile(fileName);
            return getGmondSendChannelsFromConf(conf);
        }
        catch (IOException ioe)
        {
            log.error("Unable to read gmond config from:" + fileName, ioe);
            return null;
        }
        catch (Exception e)
        {
            log.error("Error searching for unicast udp_send_channels.  It is possible none are defined in " + fileName, e);
            return null;
        }
    }

    public List<HostPort> getGmondSendChannelsFromConf(String conf)
    {
        String cleanConf = removeEmptyLines(stripComments(conf));
        List<String> blobs =  findSendChannels(cleanConf);
        log.debug("Found {} channels", blobs.size());
        List<HostPort> hosts = new ArrayList<HostPort>();
        for (String blob : blobs)
        {
            Map<String,String> chanMap = mapifyChannelString(blob);
            log.debug("Parsed channel from config {}", chanMap);
            HostPort hp = makeHostPort(chanMap);
            if (hp != null)
            {
                hosts.add(hp);
            }
            else
            {
                log.warn("Failure to create HostPort from {}", blob);
            }
        }
        return hosts;
    }


    public String stripComments(String conf)
    {
        String cFree = conf.replaceAll(cCommentPattern, "\n");
        String cppFree = cFree.replaceAll(cppCommentPattern, "");
        return cppFree;
    }


    public String removeEmptyLines(String conf)
    {
        return conf.replaceAll(emptyLinePattern, "");
    }


    public List<String> findSendChannels(String conf)
    {
        List<String> channelBlobs = new ArrayList<String>();

        Matcher matcher = Pattern.compile(udpSendPattern).matcher(conf);
        while(matcher.find())
        {
            channelBlobs.add(matcher.group(1).trim());
        }

        return channelBlobs;
    }

    public Map<String,String> mapifyChannelString(String sChannel)
    {
        String[] arr = sChannel.split("\n");
        Map<String,String> chan = new HashMap<String,String>();
        for (int i=0; i<arr.length; i++)
        {
            String[] pair = arr[i].split("=");
            chan.put(pair[0].trim(), pair[1].trim().replaceAll("\"", ""));
        }
        return chan;
    }


    // from lib/libgmond.c
    /*
static cfg_opt_t udp_send_channel_opts[] = {
  CFG_STR("mcast_join", NULL, CFGF_NONE),
  CFG_STR("mcast_if", NULL, CFGF_NONE),
  CFG_STR("host", NULL, CFGF_NONE ),
  CFG_INT("port", -1, CFGF_NONE ),
  CFG_INT("ttl", 1, CFGF_NONE ),
  CFG_STR("bind", NULL, CFGF_NONE),
  CFG_BOOL("bind_hostname", 0, CFGF_NONE),
  CFG_END()
};
    */
    public HostPort makeHostPort(Map<String,String> chan)
    {
        if (chan.containsKey("mcast_join") || chan.containsKey("mcast_if"))
        {
            log.warn("Looks like a multicast send channel, not supported and ignoring: {}", chan);
            return null;
        }
        HostPort hp = null;
        try
        {
            hp = new HostPort(chan.get("host"), (Integer.valueOf(chan.get("port"))));
        }
        catch (Exception e)
        {
            // beter with slf4j 1.7
            log.warn("Failed to create HostPort for:" + chan, e);
        }
        return hp;
    }


    // java is ridiculous
    public String readFile(String fileName) throws IOException
    {
        FileReader fr = null;
        BufferedReader br = null;
        try
        {
            fr = new FileReader(fileName);
            br = new BufferedReader(fr);
            StringBuffer sb = new StringBuffer();
            String line = br.readLine();
            while (line != null)
            {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        }
        finally
        {
            if (fr != null)
            {
                fr.close();
            }
            if (br != null)
            {
                br.close();
            }
        }
    }
}
