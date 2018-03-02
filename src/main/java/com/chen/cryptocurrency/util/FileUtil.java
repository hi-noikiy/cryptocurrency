package com.chen.cryptocurrency.util;

import com.chen.cryptocurrency.service.bean.KLineItem;
import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.List;

public class FileUtil {
    public static void writeCSV(String fileName, List<KLineItem> result) {
        //去除过早数据，只取最近的
        if (result.size() > 800) {
            result = result.subList(result.size() - 800, result.size() - 1);
        }

        File file = new File(fileName);

        try (BufferedWriter writer = Files.newWriter(file, Charset.forName("utf-8"))) {
            DecimalFormat df = new DecimalFormat("########");

            writer.write("timestamp,price,amount");
            writer.write("\n");

            result.forEach(kLineItem -> {
                String line = df.format(kLineItem.getTimeStamp() / 1000) + "," + kLineItem.getCloseValue() + "," + kLineItem.getTradeVolume();
                try {
                    writer.write(line);
                    writer.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
