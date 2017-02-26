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
package org.apache.flink.python.api.jython;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.python.core.PyObject;

import java.io.IOException;

public class PythonMapFunction extends RichMapFunction<PyObject, PyObject> {
	private static final long serialVersionUID = 3001212087036451818L;
	private final byte[] serFun;
	private transient MapFunction<PyObject, PyObject> fun;

	public PythonMapFunction(MapFunction<PyObject, PyObject> fun) throws IOException {
		this.serFun = SerializationUtils.serializeObject(fun);
	}

	@Override
	public void open(Configuration config) throws Exception {
		this.fun = (MapFunction<PyObject, PyObject>) SerializationUtils.deserializeObject(serFun);
		if (this.fun instanceof RichFunction) {
			((RichMapFunction)this.fun).open(config);
		}
	}

	@Override
	public void close() throws Exception {
		if (this.fun instanceof RichFunction) {
			((RichMapFunction)this.fun).close();
		}
	}

	@Override
	public PyObject map(PyObject value) throws Exception {
		return UtilityFunctions.adapt(this.fun.map(value));
	}
}