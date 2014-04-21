#!/usr/bin/env python

'''
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
'''

import json
import logging
import time
from pprint import pformat

from ActionQueue import ActionQueue
import AgentConfig


logger = logging.getLogger()

class Heartbeat:
  def __init__(self, actionQueue, config=None):
    self.actionQueue = actionQueue
    self.config = config
    self.reports = []

  def build(self, commandResult, id='-1', componentsMapped=False):
    timestamp = int(time.time() * 1000)
    queueResult = self.actionQueue.result()
    logger.info("Queue result: " + pformat(queueResult))

    nodeStatus = {"status": "HEALTHY",
                  "cause": "NONE"}

    heartbeat = {'responseId': int(id),
                 'timestamp': timestamp,
                 'hostname': self.config.getLabel(),
                 'nodeStatus': nodeStatus
    }

    commandsInProgress = False
    if not self.actionQueue.commandQueue.empty():
      commandsInProgress = True
    if len(queueResult) != 0:
      heartbeat['reports'] = queueResult['reports']
      if len(heartbeat['reports']) > 0:
        # There may be IN_PROGRESS tasks
        commandsInProgress = True
      pass

    # For first request/heartbeat assume no components are mapped
    if int(id) == 0:
      componentsMapped = False

    # create a report of command results (there is only one component at any point)
    if len(heartbeat['reports']) > 0:
      for report in heartbeat['reports']:
        commandResult["commandStatus"] = report["status"]
      pass

    if queueResult['componentStatus']:
      for componentStatus in queueResult['componentStatus']:
        commandResult["healthStatus"] = componentStatus["status"]
        break
      pass

    logger.info("Sending heartbeat with response id: " + str(id) + " and "
                                                                   "timestamp: " + str(timestamp) +
                ". Command(s) in progress: " + repr(commandsInProgress) +
                ". Components mapped: " + repr(componentsMapped))
    logger.debug("Heartbeat : " + pformat(heartbeat))

    return heartbeat


def main(argv=None):
  actionQueue = ActionQueue(AgentConfig.getConfig())
  heartbeat = Heartbeat(actionQueue)
  print json.dumps(heartbeat.build('3', 3))


if __name__ == '__main__':
  main()
