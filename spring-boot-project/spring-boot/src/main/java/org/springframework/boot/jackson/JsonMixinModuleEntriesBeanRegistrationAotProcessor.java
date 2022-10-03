/*
 * Copyright 2012-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.jackson;

import java.lang.reflect.Executable;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.beans.factory.aot.BeanRegistrationCodeFragments;
import org.springframework.beans.factory.aot.BeanRegistrationCodeFragmentsDecorator;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.javapoet.CodeBlock;

/**
 * @author Stephane Nicoll
 */
class JsonMixinModuleEntriesBeanRegistrationAotProcessor implements BeanRegistrationAotProcessor {

	@Override
	public BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {
		if (registeredBean.getBeanClass().equals(JsonMixinModule.class)) {
			return BeanRegistrationAotContribution
					.withCustomCodeFragments((codeFragments) -> new AotContribution(codeFragments));
		}
		return null;
	}

	static class AotContribution extends BeanRegistrationCodeFragmentsDecorator {

		public AotContribution(BeanRegistrationCodeFragments delegate) {
			super(delegate);
		}

		@Override
		public CodeBlock generateInstanceSupplierCode(GenerationContext generationContext,
				BeanRegistrationCode beanRegistrationCode, Executable constructorOrFactoryMethod,
				boolean allowDirectSupplierShortcut) {
			return super.generateInstanceSupplierCode(generationContext, beanRegistrationCode,
					constructorOrFactoryMethod, allowDirectSupplierShortcut);
		}

	}

}
