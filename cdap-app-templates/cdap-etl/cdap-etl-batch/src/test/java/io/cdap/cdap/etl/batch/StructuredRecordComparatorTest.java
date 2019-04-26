/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.cdap.etl.batch;

import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Tests for comparing StructuredRecords.
 */
public class StructuredRecordComparatorTest {
  private static final Comparator<StructuredRecord> COMPARATOR = new StructuredRecordComparator();
  private static final Schema INT_SCHEMA = Schema.of(Schema.Type.INT);
  private static final Schema LONG_SCHEMA = Schema.of(Schema.Type.LONG);
  private static final Schema FLOAT_SCHEMA = Schema.of(Schema.Type.FLOAT);
  private static final Schema DOUBLE_SCHEMA = Schema.of(Schema.Type.DOUBLE);
  private static final Schema STRING_SCHEMA = Schema.of(Schema.Type.STRING);
  private static final Schema BYTES_SCHEMA = Schema.of(Schema.Type.BYTES);
  private static final Schema BOOL_SCHEMA = Schema.of(Schema.Type.BOOLEAN);

  @Test
  public void testSingleSimpleFields() {
    testSingleFieldNotEqual(INT_SCHEMA, 0, 1);
    testSingleFieldEqual(INT_SCHEMA, 1);

    testSingleFieldNotEqual(LONG_SCHEMA, 0L, 1L);
    testSingleFieldEqual(LONG_SCHEMA, 1L);

    testSingleFieldNotEqual(FLOAT_SCHEMA, 0f, 1f);
    testSingleFieldEqual(FLOAT_SCHEMA, 1f);

    testSingleFieldNotEqual(DOUBLE_SCHEMA, 0d, 1d);
    testSingleFieldEqual(DOUBLE_SCHEMA, 1d);

    testSingleFieldNotEqual(STRING_SCHEMA, "0", "1");
    testSingleFieldEqual(STRING_SCHEMA, "1");

    testSingleFieldNotEqual(BYTES_SCHEMA, new byte[] { 0 }, new byte[] { 0, 0 });
    testSingleFieldEqual(BYTES_SCHEMA, new byte[] { 0, 0 });

    testSingleFieldNotEqual(BOOL_SCHEMA, true, false);
    testSingleFieldEqual(BOOL_SCHEMA, false);
  }

  @Test
  public void testSingleNullableFields() {
    for (Schema schema : Arrays.asList(INT_SCHEMA, LONG_SCHEMA, FLOAT_SCHEMA, DOUBLE_SCHEMA, STRING_SCHEMA,
                                       BYTES_SCHEMA, BOOL_SCHEMA)) {
      testSingleFieldEqual(Schema.nullableOf(schema), null);
    }

    testSingleFieldNotEqual(Schema.nullableOf(INT_SCHEMA), 0, null);
    testSingleFieldNotEqual(Schema.nullableOf(LONG_SCHEMA), 0L, null);
    testSingleFieldNotEqual(Schema.nullableOf(FLOAT_SCHEMA), 0f, null);
    testSingleFieldNotEqual(Schema.nullableOf(DOUBLE_SCHEMA), 0d, null);
    testSingleFieldNotEqual(Schema.nullableOf(STRING_SCHEMA), "0", null);
    testSingleFieldNotEqual(Schema.nullableOf(BYTES_SCHEMA), new byte[] { 0 }, null);
    testSingleFieldNotEqual(Schema.nullableOf(BOOL_SCHEMA), false, null);
  }

  @Test
  public void testMultipleSimpleFields() {
    Schema schema = Schema.recordOf("x",
                                    Schema.Field.of("int", INT_SCHEMA),
                                    Schema.Field.of("long", LONG_SCHEMA),
                                    Schema.Field.of("float", FLOAT_SCHEMA),
                                    Schema.Field.of("double", DOUBLE_SCHEMA),
                                    Schema.Field.of("string", STRING_SCHEMA),
                                    Schema.Field.of("bool", BOOL_SCHEMA),
                                    Schema.Field.of("bytes", BYTES_SCHEMA));

    StructuredRecord r1 = StructuredRecord.builder(schema)
      .set("int", 0)
      .set("long", 0L)
      .set("float", 0f)
      .set("double", 0d)
      .set("string", "0")
      .set("bool", false)
      .set("bytes", new byte[] { 0 })
      .build();
    Assert.assertEquals(0, COMPARATOR.compare(r1, r1));
    StructuredRecord r2 = copy(r1).set("int", 1).build();
    testRecordsNotEqual(r1, r2);

    r2 = copy(r1).set("long", 1L).build();
    testRecordsNotEqual(r1, r2);

    r2 = copy(r1).set("float", 1f).build();
    testRecordsNotEqual(r1, r2);

    r2 = copy(r1).set("double", 1d).build();
    testRecordsNotEqual(r1, r2);

    r2 = copy(r1).set("string", "1").build();
    testRecordsNotEqual(r1, r2);

    r2 = copy(r1).set("bool", true).build();
    testRecordsNotEqual(r1, r2);

    r2 = copy(r1).set("bytes", new byte[] { 0, 0 }).build();
    testRecordsNotEqual(r1, r2);
  }

  @Test
  public void testSimpleArrays() {
    Schema schema = Schema.recordOf("x", Schema.Field.of("x", Schema.arrayOf(INT_SCHEMA)));

    StructuredRecord r1 = StructuredRecord.builder(schema).set("x", Arrays.asList(0, 1, 2, 3, 4, 5)).build();
    StructuredRecord r2 = StructuredRecord.builder(schema).set("x", Arrays.asList(0, 1, 2, 3, 4)).build();
    testRecordsNotEqual(r1, r2);

    r2 = StructuredRecord.builder(schema).set("x", Arrays.asList(5, 4, 3, 2, 1, 0)).build();
    testRecordsNotEqual(r1, r2);
  }

  @Test
  public void testArraysOfRecords() {
    Schema innerSchema = Schema.recordOf("x",
                                         Schema.Field.of("string", STRING_SCHEMA), Schema.Field.of("int", INT_SCHEMA));
    Schema arrSchema = Schema.recordOf("arr", Schema.Field.of("r", Schema.arrayOf(innerSchema)));

    StructuredRecord r0 = StructuredRecord.builder(innerSchema).set("string", "0").set("int", 0).build();
    StructuredRecord r1 = StructuredRecord.builder(innerSchema).set("string", "1").set("int", 1).build();
    StructuredRecord r2 = StructuredRecord.builder(innerSchema).set("string", "2").set("int", 2).build();

    StructuredRecord arr0 = StructuredRecord.builder(arrSchema).set("r", Arrays.asList(r0, r1, r2)).build();
    StructuredRecord arr1 = StructuredRecord.builder(arrSchema).set("r", Arrays.asList(r2, r1, r0)).build();
    Assert.assertEquals(0, COMPARATOR.compare(arr0, arr0));
    testRecordsNotEqual(arr0, arr1);
  }

  @Test
  public void testNestedRecords() {
    Schema innerSchema = Schema.recordOf("inner", Schema.Field.of("i", INT_SCHEMA));
    Schema schema = Schema.recordOf("x", Schema.Field.of("x", innerSchema));

    StructuredRecord inner0 = StructuredRecord.builder(innerSchema).set("i", 0).build();
    StructuredRecord inner1 = StructuredRecord.builder(innerSchema).set("i", 1).build();

    StructuredRecord r0 = StructuredRecord.builder(schema).set("x", inner0).build();
    StructuredRecord r1 = StructuredRecord.builder(schema).set("x", inner1).build();

    Assert.assertEquals(0, COMPARATOR.compare(r0, r0));
    testRecordsNotEqual(r0, r1);
  }

  private StructuredRecord.Builder copy(StructuredRecord r) {
    StructuredRecord.Builder builder = StructuredRecord.builder(r.getSchema());
    for (Schema.Field f : r.getSchema().getFields()) {
      builder.set(f.getName(), r.get(f.getName()));
    }
    return builder;
  }

  private void testSingleFieldEqual(Schema fieldSchema, Object val) {
    Schema recordSchema = Schema.recordOf("x", Schema.Field.of("x", fieldSchema));
    StructuredRecord r1 = StructuredRecord.builder(recordSchema).set("x", val).build();
    StructuredRecord r2 = StructuredRecord.builder(recordSchema).set("x", val).build();
    Assert.assertEquals(0, COMPARATOR.compare(r1, r2));
  }

  private void testSingleFieldNotEqual(Schema fieldSchema, Object val1, Object val2) {
    Schema recordSchema = Schema.recordOf("x", Schema.Field.of("x", fieldSchema));
    StructuredRecord r1 = StructuredRecord.builder(recordSchema).set("x", val1).build();
    StructuredRecord r2 = StructuredRecord.builder(recordSchema).set("x", val2).build();
    testRecordsNotEqual(r1, r2);
  }

  private void testRecordsNotEqual(StructuredRecord r1, StructuredRecord r2) {
    int comp1 = COMPARATOR.compare(r1, r2);
    int comp2 = COMPARATOR.compare(r2, r1);
    Assert.assertNotEquals(0, comp1);
    Assert.assertNotEquals(0, comp2);
    if (comp1 > 0) {
      Assert.assertTrue(comp2 < 0);
    } else if (comp1 < 0) {
      Assert.assertTrue(comp2 > 0);
    }
  }
}
