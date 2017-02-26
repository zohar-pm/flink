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

import java.io.IOException;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.python.core.PyObject;


public class PythonReduceFunction implements ReduceFunction<PyObject> {
	private static final long serialVersionUID = -9070596504893036458L;

	private final byte[] serFun;
	private transient ReduceFunction<PyObject> fun;

	public PythonReduceFunction(ReduceFunction<PyObject> fun) throws IOException {
		this.serFun = SerializationUtils.serializeObject(fun);
	}

	@Override
	public PyObject reduce(PyObject value1, PyObject value2) throws Exception {
		if (fun == null) {
			fun = (ReduceFunction<PyObject>) SerializationUtils.deserializeObject(serFun);
		}

		return UtilityFunctions.adapt(this.fun.reduce(value1, value2));
	}
}