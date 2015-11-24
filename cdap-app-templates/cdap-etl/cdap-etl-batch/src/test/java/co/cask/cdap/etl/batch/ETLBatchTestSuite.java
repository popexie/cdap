/*
 * Copyright © 2015 Cask Data, Inc.
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

package co.cask.cdap.etl.batch;

import co.cask.cdap.etl.batch.mapreduce.ETLMapReduceTestRun;
import co.cask.cdap.test.XSlowTests;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is a test suite that runs all tests in for ETL batch. This avoids starting/stoping the unit-test framework
 * for every test class.
 */
@Category(XSlowTests.class)
@RunWith(Suite.class)
@Suite.SuiteClasses({
  BatchCubeSinkTestRun.class,
  BatchETLDBTestRun.class,
  ETLEmailActionTestRun.class,
  ETLMapReduceTestRun.class,
  ETLSnapshotTestRun.class,
  ETLStreamConversionTestRun.class,
  ETLTPFSTestRun.class
})
public class ETLBatchTestSuite extends ETLBatchTestBase {
}