################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
################################################################################
import sys
import threading
import socket
import time
from utils import constants
from utils.python_test_base import TestBase
from org.apache.flink.api.common.functions import FlatMapFunction, ReduceFunction
from org.apache.flink.api.java.functions import KeySelector
from org.apache.flink.streaming.api.windowing.time.Time import seconds


PORT = 9999


class SocketStringGenerator(threading.Thread):
    def __init__(self, host, port, msg, num_iters):
        threading.Thread.__init__(self)
        self._host = host
        self._port = port
        self._msg = msg
        if self._msg[-1] != '\n':
            self._msg += '\n'
        self._num_iters = num_iters

    def run(self):
        serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        serversocket.bind((self._host, self._port))
        serversocket.listen(5)
        (clientsocket, address) = serversocket.accept()
        for iii in range(self._num_iters):
            clientsocket.send(self._msg)
        clientsocket.close()
        serversocket.close()


class Tokenizer(FlatMapFunction):
    def flatMap(self, value, collector):
        for word in value.lower().split():
            collector.collect((1, word))


class Sum(ReduceFunction):
    def reduce(self, input1, input2):
        count1, word1 = input1
        count2, word2 = input2
        return (count1 + count2, word1)


class Selector(KeySelector):
    def getKey(self, input):
        return input[1]

class Main(TestBase):
    def __init__(self):
        super(Main, self).__init__()

    def run(self):
        SocketStringGenerator(host='', port=PORT, msg='Hello World', num_iters=constants.NUM_ITERATIONS_IN_TEST).start()
        time.sleep(0.5)

        env = self._get_execution_environment()
        env.socket_text_stream('localhost', PORT) \
            .flat_map(Tokenizer()) \
            .key_by(Selector()) \
            .time_window(seconds(1)) \
            .reduce(Sum()) \
            .print()

        env.execute()


if __name__ == '__main__':
    Main().run()
    print("Job completed ({})\n".format(sys.argv))