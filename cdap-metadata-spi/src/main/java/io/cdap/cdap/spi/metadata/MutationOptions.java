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

package io.cdap.cdap.spi.metadata;

import io.cdap.cdap.api.annotation.Beta;

/**
 * Options for metadata mutations.
 */
@Beta
public class MutationOptions {

  private final boolean asynchronous;

  /**
   * Create a mutation options object.
   *
   * @param asynchronous whether metadata mutation call should be asynchronous (non-blocking) or not.
   */
  public MutationOptions(boolean asynchronous) {
    this.asynchronous = asynchronous;
  }

  public boolean isAsynchronous() {
    return asynchronous;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for  mutation options.
   */
  public static class Builder {

    private boolean asynchronous;

    private Builder() {}

    public Builder setAsynchronous(boolean asynchronous) {
      this.asynchronous = asynchronous;
      return this;
    }

    public MutationOptions build() {
      return new MutationOptions(asynchronous);
    }
  }

  public static final MutationOptions DEFAULT = builder().setAsynchronous(false).build();
}

