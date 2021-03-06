/*
 * Copyright © 2014 Cask Data, Inc.
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

package io.cdap.cdap.api.dataset.lib;

import io.cdap.cdap.api.annotation.ReadOnly;
import io.cdap.cdap.api.annotation.WriteOnly;
import io.cdap.cdap.api.common.Bytes;
import io.cdap.cdap.api.data.schema.UnsupportedTypeException;
import io.cdap.cdap.data2.dataset2.lib.table.ObjectStoreDataset;
import io.cdap.cdap.internal.io.ReflectionSchemaGenerator;
import io.cdap.cdap.internal.io.TypeRepresentation;

/**
 * A simple data set <i>extending</i> ObjectStore, used by ObjectStoreTest.testSubclass().
 */
public class IntegerStore extends ObjectStoreDataset<Integer> {

  public IntegerStore(String name, KeyValueTable kvTable) throws UnsupportedTypeException {
    super(name, kvTable, new TypeRepresentation(Integer.class),
          new ReflectionSchemaGenerator().generate(Integer.class));
  }

  @WriteOnly
  public void write(int key, Integer value) throws Exception {
    super.write(Bytes.toBytes(key), value);
  }

  @ReadOnly
  public Integer read(int key) throws Exception {
    return super.read(Bytes.toBytes(key));
  }

  @WriteOnly
  public void delete(int key) {
    super.delete(Bytes.toBytes(key));
  }
}
