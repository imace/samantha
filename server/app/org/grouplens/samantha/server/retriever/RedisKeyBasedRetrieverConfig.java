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

package org.grouplens.samantha.server.retriever;

import org.grouplens.samantha.server.common.AbstractComponentConfig;
import org.grouplens.samantha.server.common.RedisService;
import org.grouplens.samantha.server.expander.EntityExpander;
import org.grouplens.samantha.server.expander.ExpanderUtilities;
import org.grouplens.samantha.server.io.RequestContext;
import play.Configuration;
import play.inject.Injector;

import java.util.List;

public class RedisKeyBasedRetrieverConfig extends AbstractComponentConfig implements RetrieverConfig {
    private final List<String> keyFields;
    private final List<String> retrieveFields;
    private final String indexPrefix;
    private final Injector injector;

    private RedisKeyBasedRetrieverConfig(List<String> keyFields, List<String> retrieveFields,
                                         String indexPrefix, Injector injector, Configuration config) {
        super(config);
        this.keyFields = keyFields;
        this.retrieveFields = retrieveFields;
        this.indexPrefix = indexPrefix;
        this.injector = injector;
    }

    public static RetrieverConfig getRetrieverConfig(Configuration retrieverConfig,
                                                     Injector injector) {
        return new RedisKeyBasedRetrieverConfig(retrieverConfig.getStringList("keyFields"),
                retrieverConfig.getStringList("retrieveFields"),
                retrieverConfig.getString("indexPrefix"), injector, retrieverConfig);
    }

    public Retriever getRetriever(RequestContext requestContext) {
        RedisService redisService = injector.instanceOf(RedisService.class);
        List<EntityExpander> expanders = ExpanderUtilities.getEntityExpanders(requestContext, expandersConfig, injector);
        return new RedisKeyBasedRetriever(redisService, retrieveFields, indexPrefix, keyFields, config, expanders);
    }
}
