/*******************************************************************************
 * Copyright (c) 1999, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.log;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * LogServiceImpl class.
 */

public class LogServiceImpl implements LogService {
	protected Activator log;
	protected Bundle bundle;

	protected LogServiceImpl(Activator log, Bundle bundle) {
		this.log = log;
		this.bundle = bundle;
	}

	protected void close() {
		log = null;
		bundle = null;
	}

	/*
	 * ----------------------------------------------------------------------
	 *      LogService Interface implementation
	 * ----------------------------------------------------------------------
	 */

	/**
	 * Required by LogService Interface.
	 * Log a bundle message.  The ServiceDescription field and the
	 * Throwable field of the LogEntry will be set to null.
	 * @param level - The severity of the message.  (Should be one of the
	 * four predefined severities.)
	 * @param message - Human readable string describing the condition.
	 */
	public void log(int level, String message) {
		Activator tempLog = this.log;

		if (tempLog == null) {
			return;
		}

		tempLog.log(level, message, bundle, null, null);
	}

	/**
	 * Required by LogService Interface.
	 * Log a bundle message.  The ServiceDescription field
	 * of the LogEntry will be set to null.
	 * @param level - The severity of the message.  (Should be one of the
	 * four predefined severities.)
	 * @param message - Human readable string describing the condition.
	 * @param exception - The exception that reflects the condition.
	 */
	public void log(int level, String message, Throwable exception) {
		Activator tempLog = this.log;

		if (tempLog == null) {
			return;
		}

		tempLog.log(level, message, bundle, null, exception);
	}

	/**
	 * Required by LogService Interface.
	 * Log a message associated with a specific bundle service.  The
	 * Throwable field of the LogEntry will be set to null.
	 * @param reference The ServiceReference of the service that this message
	 * is associated with.
	 * @param level The severity of the message.  (Should be one of the
	 * four predefined severities.)
	 * @param message Human readable string describing the condition.
	 */
	public void log(ServiceReference reference, int level, String message) {
		Activator tempLog = this.log;

		if (tempLog == null) {
			return;
		}

		tempLog.log(level, message, bundle, reference, null);
	}

	/**
	 * Required by LogService Interface.
	 * Log a bundle message.
	 * @param reference The ServiceReference of the service that this message
	 * is associated with.
	 * @param level The severity of the message.  (Should be one of the
	 * four predefined severities.)
	 * @param message Human readable string describing the condition.
	 * @param exception The exception that reflects the condition.
	 */
	public void log(ServiceReference reference, int level, String message, Throwable exception) {
		Activator tempLog = this.log;

		if (tempLog == null) {
			return;
		}

		tempLog.log(level, message, bundle, reference, exception);
	}

}
