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

  /**
   * Whether to block on mutation or return immediately upon ack.
   */
  public enum WaitPolicy { SYNC, ASYNC }

  private WaitPolicy waitPolicy;

  public MutationOptions(WaitPolicy waitPolicy) {
    this.waitPolicy = waitPolicy;
  }

  public WaitPolicy getWaitPolicy() {
    return waitPolicy;
  }
}
