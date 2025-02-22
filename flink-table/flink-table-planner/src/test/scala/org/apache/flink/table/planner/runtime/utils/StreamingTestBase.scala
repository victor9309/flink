/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.table.planner.runtime.utils

import org.apache.flink.runtime.testutils.MiniClusterResourceConfiguration
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.{EnvironmentSettings, ImplicitExpressionConversions}
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.planner.factories.TestValuesTableFactory
import org.apache.flink.test.junit5.MiniClusterExtension
import org.apache.flink.types.Row

import org.junit.jupiter.api.{AfterEach, BeforeEach}
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

class StreamingTestBase {

  var env: StreamExecutionEnvironment = _
  var tEnv: StreamTableEnvironment = _
  var enableObjectReuse = true

  @TempDir
  var tempFolder: Path = _

  @throws(classOf[Exception])
  @BeforeEach
  def before(): Unit = {
    this.env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(4)
    if (enableObjectReuse) {
      this.env.getConfig.enableObjectReuse()
    }
    val setting = EnvironmentSettings.newInstance().inStreamingMode().build()
    this.tEnv = StreamTableEnvironment.create(env, setting)
  }

  @AfterEach
  def after(): Unit = {
    StreamTestSink.clear()
    TestValuesTableFactory.clearAllData()
  }

  /**
   * Creates a new Row and assigns the given values to the Row's fields. We use [[rowOf()]] here to
   * avoid conflicts with [[ImplicitExpressionConversions.row]].
   */
  protected def rowOf(args: Any*): Row = {
    val row = new Row(args.length)
    (0 until args.length).foreach(i => row.setField(i, args(i)))
    row
  }
}

object StreamingTestBase extends StreamingTestBase {
  @RegisterExtension
  private val _: MiniClusterExtension = new MiniClusterExtension(
    () =>
      new MiniClusterResourceConfiguration.Builder()
        .setNumberTaskManagers(1)
        .setNumberSlotsPerTaskManager(4)
        .build())
}
