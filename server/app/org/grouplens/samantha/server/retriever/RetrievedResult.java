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

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class RetrievedResult {
    private List<ObjectNode> entityList;
    private long maxHits;

    public RetrievedResult(List<ObjectNode> entityList, long maxHits) {
        this.entityList = entityList;
        this.maxHits = maxHits;
    }

    public List<ObjectNode> getEntityList() {
        return entityList;
    }

    List<ObjectNode> setEntityList(List<ObjectNode> newList) {
        this.entityList = newList;
        return newList;
    }

    public void setMaxHits(long maxHits) {
        this.maxHits = maxHits;
    }

    public long getMaxHits() {
        return maxHits;
    }
}
