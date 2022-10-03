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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author Stephane Nicoll
 */
public final class JsonMixinModuleEntries {

	private final Map<Object, Object> entries;

	private JsonMixinModuleEntries(Map<Object, Object> entries) {
		this.entries = new LinkedHashMap<>(entries);
	}

	public void doWithEntry(ClassLoader classLoader, BiConsumer<Class<?>, Class<?>> action) {
		this.entries.forEach((type, mixin) -> {
			action.accept(resolveClassNameIfNecessary(type, classLoader),
					resolveClassNameIfNecessary(mixin, classLoader));
		});
	}

	private Class<?> resolveClassNameIfNecessary(Object type, ClassLoader classLoader) {
		return (type instanceof Class<?> clazz) ? clazz : ClassUtils.resolveClassName((String) type, classLoader);
	}

	public static JsonMixinModuleEntries scan(ApplicationContext context, Collection<String> basePackages) {
		if (ObjectUtils.isEmpty(basePackages)) {
			return new JsonMixinModuleEntries(Collections.emptyMap());
		}
		JsonMixinComponentScanner scanner = new JsonMixinComponentScanner();
		scanner.setEnvironment(context.getEnvironment());
		scanner.setResourceLoader(context);
		Map<Object, Object> entries = new LinkedHashMap<>();
		for (String basePackage : basePackages) {
			if (StringUtils.hasText(basePackage)) {
				for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
					addJsonMixin(entries,
							ClassUtils.resolveClassName(candidate.getBeanClassName(), context.getClassLoader()));
				}
			}
		}
		return new JsonMixinModuleEntries(entries);
	}

	private static void addJsonMixin(Map<Object, Object> entries, Class<?> mixinClass) {
		MergedAnnotation<JsonMixin> annotation = MergedAnnotations
				.from(mixinClass, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(JsonMixin.class);
		for (Class<?> targetType : annotation.getClassArray("type")) {
			entries.put(targetType, mixinClass);
		}
	}

	static class JsonMixinComponentScanner extends ClassPathScanningCandidateComponentProvider {

		JsonMixinComponentScanner() {
			addIncludeFilter(new AnnotationTypeFilter(JsonMixin.class));
		}

		@Override
		protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
			return true;
		}

	}

}
