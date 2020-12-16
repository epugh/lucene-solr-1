/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.document;

import com.carrotsearch.randomizedtesting.generators.RandomNumbers;

import java.util.Random;

import org.apache.lucene.document.ShapeField.QueryRelation;
import org.apache.lucene.geo.Component2D;
import org.apache.lucene.geo.ShapeTestUtil;
import org.apache.lucene.geo.XYLine;

/** random cartesian bounding box, line, and polygon query tests for random generated cartesian {@link XYLine} types */
public class TestXYLineShapeQueries extends BaseXYShapeTestCase {

  @Override
  protected ShapeType getShapeType() {
    return ShapeType.LINE;
  }

  @Override
  protected XYLine randomQueryLine(Object... shapes) {
    Random random = random();
    if (random.nextInt(100) == 42) {
      // we want to ensure some cross, so randomly generate lines that share vertices with the indexed point set
      int maxBound = (int)Math.floor(shapes.length * 0.1d);
      if (maxBound < 2) {
        maxBound = shapes.length;
      }
      float[] x = new float[RandomNumbers.randomIntBetween(random, 2, maxBound)];
      float[] y = new float[x.length];
      for (int i = 0, j = 0; j < x.length && i < shapes.length; ++i, ++j) {
        XYLine l = (XYLine) (shapes[i]);
        if (random.nextBoolean() && l != null) {
          int v = random.nextInt(l.numPoints() - 1);
          x[j] = l.getX(v);
          y[j] = l.getY(v);
        } else {
          x[j] = ShapeTestUtil.nextFloat(random);
          y[j] = ShapeTestUtil.nextFloat(random);
        }
      }
      return new XYLine(x, y);
    }
    return nextLine();
  }

  @Override
  protected Field[] createIndexableFields(String field, Object line) {
    return XYShape.createIndexableFields(field, (XYLine)line);
  }

  @Override
  protected Validator getValidator() {
    return new LineValidator(this.ENCODER);
  }

  protected static class LineValidator extends Validator {
    protected LineValidator(Encoder encoder) {
      super(encoder);
    }
    
    @Override
    public boolean testComponentQuery(Component2D query, Object shape) {
      XYLine line = (XYLine) shape;
      if (queryRelation == QueryRelation.CONTAINS) {
        return testWithinQuery(query, XYShape.createIndexableFields("dummy", line)) == Component2D.WithinRelation.CANDIDATE;
      }
      return testComponentQuery(query, XYShape.createIndexableFields("dummy", line));
    }
  }
}
