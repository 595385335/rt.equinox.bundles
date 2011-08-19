/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.bidi;

import org.eclipse.equinox.bidi.custom.STextTypeHandler;
import org.eclipse.equinox.bidi.internal.STextTypesCollector;

/**
 * This class provides access to registered structured text handlers.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
final public class STextTypeHandlerFactory {

	/**
	 * Structured text handler for property file statements. It expects the following format:
	 * <pre>
	 *  name=value
	 * </pre>
	 */
	public static final String PROPERTY = "property"; //$NON-NLS-1$

	/**
	 * Structured text handler for compound names. It expects text to be made of one or more 
	 * parts separated by underscores:
	 * <pre>
	 *  part1_part2_part3
	 * </pre>
	 */
	public static final String UNDERSCORE = "underscore"; //$NON-NLS-1$

	/**
	 * Structured text handler for comma-delimited lists, such as:
	 * <pre>
	 *  part1,part2,part3
	 * </pre>
	 */
	public static final String COMMA_DELIMITED = "comma"; //$NON-NLS-1$

	/**
	 * Structured text handler for strings with the following format:
	 * <pre>
	 *  system(user)
	 * </pre>
	 */
	public static final String SYSTEM_USER = "system"; //$NON-NLS-1$

	/**
	 * Structured text handler for directory and file paths.
	 */
	public static final String FILE = "file"; //$NON-NLS-1$

	/**
	 * Structured text handler for e-mail addresses.
	 */
	public static final String EMAIL = "email"; //$NON-NLS-1$

	/**
	 * Structured text handler for URLs.
	 */
	public static final String URL = "url"; //$NON-NLS-1$

	/**
	 * Structured text handler for regular expressions, possibly spanning multiple lines.
	 */
	public static final String REGEXP = "regex"; //$NON-NLS-1$

	/**
	 * Structured text handler for XPath expressions.
	 */
	public static final String XPATH = "xpath"; //$NON-NLS-1$

	/**
	 * Structured text handler for Java code, possibly spanning multiple lines.
	 */
	public static final String JAVA = "java"; //$NON-NLS-1$

	/**
	 * Structured text handler for SQL statements, possibly spanning multiple lines.
	 */
	public static final String SQL = "sql"; //$NON-NLS-1$

	/**
	 *  Structured text handler for arithmetic expressions, possibly with a RTL base direction.
	 */
	public static final String RTL_ARITHMETIC = "math"; //$NON-NLS-1$

	/**
	 * Prevents instantiation
	 */
	private STextTypeHandlerFactory() {
		// placeholder
	}

	/**
	 *  Obtain a structured text handler of a given type.
	 *  @param id string identifying handler
	 *  @return a handler of the required type, or <code>null</code> if the type is unknown
	 */
	static public STextTypeHandler getHandler(String id) {
		return STextTypesCollector.getInstance().getHandler(id);
	}

}
