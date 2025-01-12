/** 
 * 
 * Copyright (c) 2017, JULIE Lab.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the BSD-2-Clause License
 *
 * Author: 
 * 
 * Description:
 **/
package de.julielab.jcore.reader.dta.mapping;

import de.julielab.jcore.types.extensions.dta.DocumentClassification;
import org.apache.uima.jcas.JCas;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public abstract class AbstractMapper {
	final String mainClassification;
	final String subClassification;
	final Map<String, Class<? extends DocumentClassification>> classification2class;
	final Class<? extends DocumentClassification> defaultClass;

	AbstractMapper(
			final String mainClassification,
			final String subClassification,
			final Map<String, Class<? extends DocumentClassification>> classification2class) {
		this(mainClassification, subClassification, classification2class, null);
	}

	AbstractMapper(
			final String mainClassification,
			final String subClassification,
			final Map<String, Class<? extends DocumentClassification>> classification2class,
			final Class<? extends DocumentClassification> defaultClass) {
		this.mainClassification = MappingService.CLASIFICATION
				+ mainClassification;
		this.subClassification = MappingService.CLASIFICATION
				+ subClassification;
		this.classification2class = classification2class;
		this.defaultClass = defaultClass;
	}

	DocumentClassification getClassification(final JCas jcas,
			final String xmlFileName, final Map<String, String[]> classInfo)
			throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		if (classInfo.containsKey(mainClassification)) {
			if (classInfo.get(mainClassification).length != 1)
				throw new IllegalArgumentException("More than 1 "
						+ mainClassification + " classification in "
						+ xmlFileName);
			final String mainClass = classInfo.get(mainClassification)[0];
			Class<? extends DocumentClassification> aClass = classification2class
					.get(mainClass);
			if (aClass == null)
				if (defaultClass == null)
					throw new IllegalArgumentException(mainClass
							+ " not supported in " + xmlFileName);
				else
					aClass = defaultClass;
			final Constructor<? extends DocumentClassification> constructor = aClass
					.getConstructor(new Class[] { JCas.class });
			final DocumentClassification classification = constructor
					.newInstance(jcas);
			classification.setClassification(mainClass);

			if (classInfo.get(subClassification) != null) {
				if (classInfo.get(subClassification).length != 1)
					throw new IllegalArgumentException("More than 1 "
							+ subClassification + " classification in "
							+ xmlFileName);
				final String subClass = classInfo.get(subClassification)[0];
				classification.setSubClassification(subClass);
			}

			classification.addToIndexes();
			return classification;
		}
		return null;
	}
}
