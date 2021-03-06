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

package org.grouplens.samantha.server.evaluator.metric;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.grouplens.samantha.modeler.featurizer.FeatureExtractorUtilities;
import org.grouplens.samantha.server.config.ConfigKey;
import org.grouplens.samantha.server.predictor.Prediction;
import play.Logger;
import play.libs.Json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MRR implements Metric {
    private final MRRConfig config;
    private int cnt = 0;
    private DoubleList RR;

    public MRR(MRRConfig config) {
        this.config = config;
        this.RR = new DoubleArrayList(config.N.size());
        for (int i=0; i<config.N.size(); i++) {
            this.RR.add(0.0);
        }
    }

    public void add(List<ObjectNode> groundTruth, List<Prediction> recommendations) {
        Set<String> releItems = new HashSet<>();
        for (JsonNode entity : groundTruth) {
            if (config.relevanceKey == null || entity.get(config.relevanceKey).asDouble() >= config.threshold) {
                String item = FeatureExtractorUtilities.composeConcatenatedKey(entity, config.itemKeys);
                releItems.add(item);
            }
        }
        if (releItems.size() == 0) {
            return;
        }
        int maxN = 0;
        for (Integer n : config.N) {
            if (n > maxN) {
                maxN = n;
            }
            if (recommendations.size() < n) {
                Logger.error("The number of recommendations({}) is less than the indicated MRR N({})",
                        recommendations.size(), n);
            }
        }
        double[] rr = new double[config.N.size()];
        for (int i=0; i<recommendations.size(); i++) {
            int rank = i + 1;
            String recItem = FeatureExtractorUtilities.composeConcatenatedKey(
                    recommendations.get(i).getEntity(), config.itemKeys);
            if (releItems.contains(recItem)) {
                for (int j=0; j<config.N.size(); j++) {
                    int n = config.N.get(j);
                    if (rank <= n) {
                        rr[j] += (1.0 / rank);
                    }
                }
            }
            if (rank > maxN) {
                break;
            }
        }
        for (int i=0; i<config.N.size(); i++) {
            RR.set(i, RR.getDouble(i) + rr[i] / releItems.size());
        }
        cnt += 1;
    }

    public MetricResult getResults() {
        List<ObjectNode> results = new ArrayList<>(config.N.size());
        ObjectNode metricPara = Json.newObject();
        metricPara.put("threshold", config.threshold);
        metricPara.put("minValue", config.minValue);
        boolean pass = true;
        for (int i=0; i<config.N.size(); i++) {
            ObjectNode result = Json.newObject();
            result.put(ConfigKey.EVALUATOR_METRIC_NAME.get(), "MRR");
            metricPara.put("N", config.N.get(i));
            result.put(ConfigKey.EVALUATOR_METRIC_PARA.get(),
                    metricPara.toString());
            double value = 0.0;
            if (cnt > 0) {
                value = RR.getDouble(i) / cnt;
            }
            result.put(ConfigKey.EVALUATOR_METRIC_VALUE.get(), value);
            results.add(result);
            if (value < config.minValue) {
                pass = false;
            }
        }
        return new MetricResult(results, pass);
    }
}
