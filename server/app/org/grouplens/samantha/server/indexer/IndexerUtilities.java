/*
 * Copyright (c) [2016-2017] [University of Minnesota]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.grouplens.samantha.server.indexer;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import org.grouplens.samantha.server.exception.ConfigurationException;

import javax.inject.Singleton;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class IndexerUtilities {
    private IndexerUtilities() {}

    /**
     * TODO: deal with timezone problem.
     * @param timeStr yyyy-MM-dd HH:mm:SS or now/today - <n> <TIMEUNIT string value in Java>
     */
    public static int parseTime(String timeStr) {
        try {
            Date date;
            if (timeStr.startsWith("now") || timeStr.startsWith("today")) {
                String[] fields = timeStr.split(" ");
                long mul = Long.parseLong(fields[2]);
                String unit = fields[3];
                long current = new Date().getTime();
                if (timeStr.startsWith("today")) {
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    current = format.parse(format.format(new Date())).getTime();
                }
                long minus = TimeUnit.valueOf(unit).toMillis(mul);
                date = new Date(current - minus);
            } else if (timeStr.split("-").length > 1) {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
                date = format.parse(timeStr);
            } else {
                return Integer.parseInt(timeStr);
            }
            Logger.info("{}", date.toString());
            return (int)(date.getTime() / 1000);
        } catch (ParseException e) {
            throw new ConfigurationException(e);
        }
    }

    public static void writeOutHeader(List<String> dataFields, BufferedWriter writer,
                                      String separator) throws IOException {
        writer.write(StringUtils.join(dataFields, separator));
        writer.newLine();
        writer.flush();
    }

    public static void writeOutJson(JsonNode entity, List<String> curFields, BufferedWriter writer,
                                    String separator) throws IOException {
        List<JsonNode> fields = new ArrayList<>(curFields.size());
        for (String field : curFields) {
            fields.add(entity.get(field));
        }
        String line = StringUtils.join(fields, separator);
        writer.write(line);
        writer.newLine();
        writer.flush();
    }
}
