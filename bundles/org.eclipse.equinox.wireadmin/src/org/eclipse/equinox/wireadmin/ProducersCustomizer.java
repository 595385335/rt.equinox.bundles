/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.wireadmin;

import org.osgi.framework.*;
import org.osgi.service.wireadmin.Producer;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ProducersCustomizer implements ServiceTrackerCustomizer {

	protected BundleContext context;
	protected WireAdmin wireAdmin;

	public ProducersCustomizer(BundleContext context, WireAdmin wireAdmin) {
		this.context = context;
		this.wireAdmin = wireAdmin;
	}

	/**
	 * @see ServiceTrackerCustomizer#addingService(ServiceReference)
	 */
	public Object addingService(ServiceReference reference) {
		Producer service = (Producer) context.getService(reference);
		String pid = (String) reference.getProperty("service.pid"); //$NON-NLS-1$

		try {
			//if a wire contains this producer, the wire notifies it
			if (wireAdmin.getWires(wireAdmin.producerFilter + pid + ")") == null) { //$NON-NLS-1$
				wireAdmin.notifyProducer(service, pid);
			}
		} catch (InvalidSyntaxException ex) {
			ex.printStackTrace();
		}

		return (service);
	}

	/**
	 * @see ServiceTrackerCustomizer#modifiedService(ServiceReference, Object)
	 */
	public void modifiedService(ServiceReference reference, Object service) {
		//do nothing
	}

	/**
	 * @see ServiceTrackerCustomizer#removedService(ServiceReference, Object)
	 */
	public void removedService(ServiceReference reference, Object service) {

		context.ungetService(reference);
	}
}
