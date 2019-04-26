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

import io.cdap.cdap.api.data.schema.Schema;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Compares schemas.
 *
 * If they are of different physical types, they are compared according to the natural ordering of their types.
 * If they are of different logical types, they are compared according to the natural ordering of their logical types.
 *
 * Union schemas are compared by comparing their union schemas in order. If one union is a subset of another union,
 * it is less than that union.
 *
 * Array schemas are compared by their component schemas.
 *
 * Record schemas are compared by their fields in order. If one record schema is a subset of the other,
 * it is less than that record schema.
 *
 * A schema field is first compared by schema. If the field schemas are the same, they are compared by field name.
 *
 * Map schemas are compared first by the key schema, then by the value schema.
 */
public class SchemaComparator implements Comparator<Schema> {

  @Override
  public int compare(Schema s1, Schema s2) {
    // check physical type
    int comp = s1.getType().compareTo(s2.getType());
    if (comp != 0) {
      return comp;
    }

    // check logical types
    if (s1.getLogicalType() == null && s2.getLogicalType() != null) {
      return -1;
    } else if (s1.getLogicalType() != null && s2.getLogicalType() == null) {
      return 1;
    } else if (s1.getLogicalType() != null) {
      comp = s1.getLogicalType().compareTo(s2.getLogicalType());
      if (comp != 0) {
        return comp;
      }
    }

    // must be the same physical and logical type
    switch (s1.getType()) {
      case UNION:
        //noinspection ConstantConditions
        return compareUnionSchemas(s1.getUnionSchemas(), s2.getUnionSchemas());
      case ARRAY:
        return compare(s1.getComponentSchema(), s2.getComponentSchema());
      case MAP:
        //noinspection ConstantConditions
        return compareMapSchemas(s1.getMapSchema(), s2.getMapSchema());
      case RECORD:
        return compareRecordSchemas(s1, s2);
    }
    return 0;
  }

  private int compareMapSchemas(Map.Entry<Schema, Schema> s1, Map.Entry<Schema, Schema> s2) {
    int comp = compare(s1.getKey(), s2.getKey());
    if (comp != 0) {
      return comp;
    }
    comp = compare(s1.getValue(), s2.getValue());
    return comp;
  }

  private int compareRecordSchemas(Schema s1, Schema s2) {
    int comp = s1.getRecordName().compareTo(s2.getRecordName());
    if (comp != 0) {
      return comp;
    }

    List<Schema.Field> s1Fields = s1.getFields();
    List<Schema.Field> s2Fields = s2.getFields();
    Iterator<Schema.Field> s1FieldsIter = s1Fields.iterator();
    Iterator<Schema.Field> s2FieldsIter = s2Fields.iterator();
    while (s1FieldsIter.hasNext()) {
      Schema.Field f1 = s1FieldsIter.next();
      Schema.Field f2 = s2FieldsIter.next();
      comp = compare(f1.getSchema(), f2.getSchema());
      if (comp != 0) {
        return comp;
      }
      comp = f1.getName().compareTo(f2.getName());
      if (comp != 0) {
        return comp;
      }
    }

    if (s1FieldsIter.hasNext()) {
      return 1;
    } else if (s2FieldsIter.hasNext()) {
      return -1;
    } else {
      return 0;
    }
  }

  private int compareUnionSchemas(List<Schema> s1Schemas, List<Schema> s2Schemas) {
    // if the number of schemas in the union are different, the one with fewer schemas is less
    int comp = Integer.compare(s1Schemas.size(), s2Schemas.size());
    if (comp != 0) {
      return comp;
    }
    // if the number of schemas in the union are the same, compare each schema in order
    Iterator<Schema> s1SchemasIter = s1Schemas.iterator();
    Iterator<Schema> s2SchemasIter = s2Schemas.iterator();
    while (s1SchemasIter.hasNext() && s2SchemasIter.hasNext()) {
      comp = compare(s1SchemasIter.next(), s2SchemasIter.next());
      if (comp != 0) {
        return comp;
      }
    }

    if (s1SchemasIter.hasNext()) {
      return 1;
    } else if (s2SchemasIter.hasNext()) {
      return -1;
    } else {
      return 0;
    }
  }
}
