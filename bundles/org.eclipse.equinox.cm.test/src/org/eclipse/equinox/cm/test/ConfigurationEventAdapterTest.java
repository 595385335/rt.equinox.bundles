/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.cm.test;

import java.util.*;
import junit.framework.TestCase;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.*;

public class ConfigurationEventAdapterTest extends TestCase {

	private ConfigurationAdmin cm;
	private ServiceReference cmReference;

	boolean locked = false;
	Object lock = new Object();

	public ConfigurationEventAdapterTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		Activator.getBundle("org.eclipse.equinox.event").start();
		Activator.getBundle("org.eclipse.equinox.cm").start();
		cmReference = Activator.getBundleContext().getServiceReference(ConfigurationAdmin.class.getName());
		cm = (ConfigurationAdmin) Activator.getBundleContext().getService(cmReference);
	}

	protected void tearDown() throws Exception {
		Activator.getBundleContext().ungetService(cmReference);
		Activator.getBundle("org.eclipse.equinox.cm").stop();
		Activator.getBundle("org.eclipse.equinox.event").stop();
	}

	public void testConfigurationEvent() throws Exception {

		Configuration config = cm.getConfiguration("test");
		Properties props = new Properties();
		props.put("testkey", "testvalue");
		config.update(props);

		EventHandler handler = new EventHandler() {
			public void handleEvent(Event event) {
				synchronized (lock) {
					locked = false;
					lock.notify();
				}
			}

		};
		String[] topics = new String[] {"org/osgi/service/cm/ConfigurationEvent/*"};
		Dictionary handlerProps = new Hashtable();

		handlerProps.put(EventConstants.EVENT_TOPIC, topics);
		ServiceRegistration reg = Activator.getBundleContext().registerService(EventHandler.class.getName(), handler, handlerProps);

		synchronized (lock) {
			config.update(props);
			locked = true;
			lock.wait(5000);
			assertFalse(locked);
		}

		synchronized (lock) {
			config.delete();
			locked = true;
			lock.wait(5000);
			assertFalse(locked);
		}

		reg.unregister();
	}

	public void testConfigurationFactoryEvent() throws Exception {

		Configuration config = cm.createFactoryConfiguration("test");
		Properties props = new Properties();
		props.put("testkey", "testvalue");
		config.update(props);

		EventHandler handler = new EventHandler() {
			public void handleEvent(Event event) {
				synchronized (lock) {
					locked = false;
					lock.notify();
				}
			}

		};
		String[] topics = new String[] {"org/osgi/service/cm/ConfigurationEvent/*"};
		Dictionary handlerProps = new Hashtable();

		handlerProps.put(EventConstants.EVENT_TOPIC, topics);
		ServiceRegistration reg = Activator.getBundleContext().registerService(EventHandler.class.getName(), handler, handlerProps);

		synchronized (lock) {
			config.update(props);
			locked = true;
			lock.wait(5000);
			assertFalse(locked);
		}

		synchronized (lock) {
			config.delete();
			locked = true;
			lock.wait(5000);
			assertFalse(locked);
		}

		reg.unregister();
	}

}
