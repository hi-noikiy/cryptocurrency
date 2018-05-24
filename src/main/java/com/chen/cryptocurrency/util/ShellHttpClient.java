package com.chen.cryptocurrency.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ShellHttpClient implements CoinHttpClient {
    private static CoinHttpClient instance = new ShellHttpClient();

    private Logger logger = LoggerFactory.getLogger(ShellHttpClient.class);
    private final Runtime RUNTIME = Runtime.getRuntime();

    private Joiner cmdSplitter = Joiner.on(" ");

    private ShellHttpClient() {
    }

    public static CoinHttpClient getInstance() {
        return instance;
    }

    @Override
    public String requestHttpGet(String domain, String url, String param) {
        url = domain + url + param;
        return get(url);
    }

    @Override
    public String requestHttpPost(String domain, String path, String param, Map<String, String> params) {

        String url = domain + path;
        String body = "{}";
        if (!url.endsWith("?")) {
            url += "?";
        }
        url += param;
        return post(url, body);
    }

    private String exec(String sh, List<String> param) {
        logger.info("execute cmd: {},p:{}", sh, param);
        try {
            List<String> cmd = Lists.newArrayList();
            cmd.add(sh);
            cmd.addAll(param);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(new File("./"));
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();

            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                builder.append(line);
            }
            process.destroy();
            reader.close();

            return builder.toString();
        } catch (Exception e) {
            logger.error("sh 执行失败 ， cmd:{}", sh);
            MailUtil.sendMail("sh 执行失败，请检查", sh + "_" + param);
        }
        return null;
    }

    public String get(String url) {
        String sh = "./get.sh";
        return exec(sh, Collections.singletonList(url));
    }

    public String post(String url, String body) {
        String sh = "./post.sh";
        return exec(sh, Arrays.asList(url, body));
    }
}
