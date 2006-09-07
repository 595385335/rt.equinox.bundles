/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.app;

/**
 * Bootstrap type for an application.  An IApplication represent executable 
 * entry points into an application.  An IApplication can be configured into 
 * the Platform's <code>org.eclipse.equinox.applications</code> extension-point.
 *
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @since 3.2
 */
public interface IApplication {

	/**
	 * Exit object indicating normal termination
	 */
	public static final Integer EXIT_OK = new Integer(0);

	/**
	 * Exit object requesting platform restart
	 */
	public static final Integer EXIT_RESTART = new Integer(23);

	/**
	 * Exit object requesting that the command passed back be executed.  Typically
	 * this is used to relaunch Eclipse with different command line arguments.
	 */
	public static final Integer EXIT_RELAUNCH = new Integer(24);

	/**
	 * Starts this application with the given context and returns a result.
	 * The content of the context is unchecked and should conform to the expectations of
	 * the runnable being invoked.<p>
	 * 
	 * Applications can return any object they like.  If an <code>Integer</code> is returned
	 * it is treated as the program exit code if Eclipse is exiting.
	 * @return the return value of the application
	 * @see #EXIT_OK
	 * @see #EXIT_RESTART
	 * @see #EXIT_RELAUNCH
	 * @param context the application context to pass to the application
	 * @exception Exception if there is a problem running this application.
	 */
	public Object start(IApplicationContext context) throws Exception;

	/**
	 * Forces a running application to exit.  This method must block until the 
	 * running application has completely stopped.
	 */
	public void stop();
}
