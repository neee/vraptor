/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.caelum.vraptor.http.ognl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import ognl.Evaluation;
import br.com.caelum.vraptor.VRaptorException;
import br.com.caelum.vraptor.ioc.Container;
import br.com.caelum.vraptor.vraptor2.Info;

/**
 * Capable of instantiating lists. These are registered for later removal of
 * null entitres.
 *
 * @author Guilherme Silveira
 */
class ListNullHandler {

	@SuppressWarnings("unchecked")
	Object instantiate(Container container, Object target, Object property, Type type)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		Class typeToInstantiate = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
		Constructor constructor = typeToInstantiate.getDeclaredConstructor();
		constructor.setAccessible(true);
		Object instance = constructor.newInstance();

		// setting the position
		int position = (Integer) property;
		List list = (List) target;
		while (list.size() <= position) {
			list.add(null);
		}
		list.set(position, instance);

		// registering for null entries removal
		EmptyElementsRemoval removal = container.instanceFor(EmptyElementsRemoval.class);
		removal.add(list);

		return instance;
	}

	Type getListType(Object target, Evaluation evaluation) {
		// creating instance
		Object listHolder = evaluation.getSource();
		String listPropertyName = evaluation.getNode().toString();
		Method listSetter = ReflectionBasedNullHandler.findMethod(listHolder.getClass(), "set"
				+ Info.capitalize(listPropertyName), target.getClass(), null);
		Type[] types = listSetter.getGenericParameterTypes();
		Type type = types[0];
		if (!(type instanceof ParameterizedType)) {
			throw new VRaptorException("Vraptor does not support non-generic collection at "
					+ listSetter.getName());
		}
		return type;
	}

}
