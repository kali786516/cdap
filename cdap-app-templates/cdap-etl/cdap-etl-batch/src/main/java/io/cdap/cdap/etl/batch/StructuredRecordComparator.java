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

import io.cdap.cdap.api.common.Bytes;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Compares StructuredRecords. Records cannot contain unions that do not represent a nullable type and they cannot
 * contain maps. Arrays cannot contain non-comparable types.
 *
 * Records are first compared by their schemas. If their schemas are the same, they are compared field by field.
 *
 * Record fields are compared first by their schema fields. If the schema fields are the same, they are compared by
 * value.
 *
 * Numbers, boolean, and strings are compared with their normal java compare methods. A null value is less than
 * a non-null value.
 *
 * Arrays are compared by their values in order.
 *
 * Bytes are compared lexicographically.
 */
public class StructuredRecordComparator implements Comparator<StructuredRecord> {
  private static final Comparator<Schema> SCHEMA_COMPARATOR = new SchemaComparator();

  @Override
  public int compare(StructuredRecord r1, StructuredRecord r2) {
    if (r1 == null && r2 != null) {
      return -1;
    } else if (r1 != null && r2 == null) {
      return 1;
    } else if (r1 == null) {
      return 0;
    }

    int comp = SCHEMA_COMPARATOR.compare(r1.getSchema(), r2.getSchema());
    if (comp != 0) {
      return comp;
    }

    // both records must have the same fields, otherwise their schemas would be different
    //noinspection ConstantConditions
    for (Schema.Field field : r1.getSchema().getFields()) {
      comp = compareFields(field.getName(), field.getSchema(), r1.get(field.getName()), r2.get(field.getName()));
      if (comp != 0) {
        return comp;
      }
    }

    return 0;
  }

  private int compareFields(String fieldName, Schema schema, Object val1, Object val2) {
    if (val1 == null && val2 != null) {
      return -1;
    } else if (val1 != null && val2 == null) {
      return 1;
    } else if (val1 == null) {
      return 0;
    }

    switch (schema.getType()) {
      case NULL:
        return 0;
      case INT:
        return Integer.compare((int) val1, (int) val2);
      case LONG:
        return Long.compare((long) val1, (long) val2);
      case FLOAT:
        return Float.compare((float) val1, (float) val2);
      case DOUBLE:
        return Double.compare((double) val1, (double) val2);
      case STRING:
        return ((String) val1).compareTo((String) val2);
      case BOOLEAN:
        return Boolean.compare((boolean) val1, (boolean) val2);
      case ENUM:
        return Integer.compare(((Enum) val1).ordinal(), ((Enum) val2).ordinal());
      case BYTES:
        return Bytes.compareTo(asBytes(fieldName, val1), asBytes(fieldName, val2));
      case RECORD:
        return compare((StructuredRecord) val1, (StructuredRecord) val2);
      case ARRAY:
        return compareArrays(fieldName, schema.getComponentSchema(), val1, val2);
      case UNION:
        throw new IllegalArgumentException(String.format("Field '%s' is a union. Cannot compare union fields",
                                                         fieldName));
      case MAP:
        throw new IllegalArgumentException(String.format("Field '%s' is a union. Cannot compare union fields",
                                                         fieldName));
    }
    throw new IllegalStateException(String.format("Cannot compare unexpected type '%s' for field '%s'.",
                                                  schema.getType(), fieldName));
  }

  private int compareArrays(String fieldName, Schema componentSchema, Object val1, Object val2) {
    Iterator<Object> val1Iter = asIterator(fieldName, val1);
    Iterator<Object> val2Iter = asIterator(fieldName, val2);

    while (val1Iter.hasNext() && val2Iter.hasNext()) {
      int comp = compareFields(fieldName, componentSchema, val1Iter.next(), val2Iter.next());
      if (comp != 0) {
        return comp;
      }
    }

    if (val1Iter.hasNext()) {
      return -1;
    } else if (val2Iter.hasNext()) {
      return 1;
    } else {
      return 0;
    }
  }

  private Iterator<Object> asIterator(String fieldName, Object array) {
    if (!(array instanceof Collection) && !array.getClass().isArray()) {
      throw new IllegalArgumentException(String.format(
        "Field '%s' is of type array but is a Java '%s' instead of an array or collection.",
        fieldName, array.getClass().getName()));
    }

    if (array instanceof Collection) {
      return ((Collection<Object>) array).iterator();
    } else {
      return Arrays.stream((Object[]) array).iterator();
    }
  }

  private byte[] asBytes(String fieldName, Object val) {
    if (val instanceof byte[]) {
      return (byte[]) val;
    } else if (val instanceof ByteBuffer) {
      return Bytes.toBytes((ByteBuffer) val);
    }
    throw new IllegalArgumentException(String.format("Field '%s' is of type bytes but is of unexpected Java type '%s'",
                                                     fieldName, val.getClass().getName()));
  }
}
