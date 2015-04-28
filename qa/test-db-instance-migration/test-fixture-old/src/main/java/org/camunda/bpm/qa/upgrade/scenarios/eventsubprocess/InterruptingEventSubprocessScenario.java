/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.qa.upgrade.scenarios.eventsubprocess;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.qa.upgrade.DescribesScenario;
import org.camunda.bpm.qa.upgrade.ScenarioSetup;

/**
 * @author Thorben Lindhauer
 *
 */
public class InterruptingEventSubprocessScenario {

  @Deployment
  public static String deployInterruptingMessageEventSubprocess() {
    return "org/camunda/bpm/qa/upgrade/eventsubprocess/interruptingMessageEventSubprocess.bpmn20.xml";
  }

  @DescribesScenario({"complete", "delete", "activityInstanceTree"})
  public static ScenarioSetup instantiateAndTriggerSubprocess() {
    return new ScenarioSetup() {
      public void execute(ProcessEngine engine, String scenarioName) {
        engine
          .getRuntimeService()
          .startProcessInstanceByKey("InterruptingEventSubprocessScenario", scenarioName);

        engine.getRuntimeService().correlateMessage("newMessage");
      }
    };
  }

  // TODO: concurrency as well? nested subprocess with parallelism?


}
