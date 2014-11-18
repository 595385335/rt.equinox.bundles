/*******************************************************************************
 * Copyright (c) 2014 Raymond Augé and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Raymond Augé <raymond.auge@liferay.com> - Bug 436698
 ******************************************************************************/

package org.eclipse.equinox.http.servlet.internal.registration;

import java.lang.reflect.*;
import java.util.EventListener;
import java.util.List;
import javax.servlet.*;
import org.eclipse.equinox.http.servlet.internal.context.ContextController;
import org.eclipse.equinox.http.servlet.internal.context.ContextController.ServiceHolder;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.runtime.dto.ListenerDTO;

/**
 * @author Raymond Augé
 */
public class ListenerRegistration extends Registration<EventListener, ListenerDTO> {

	private final ServiceHolder<EventListener> listenerHolder;
	private final List<Class<? extends EventListener>> classes;
	private final EventListener proxy;
	private final ServletContext servletContext;
	private final ServletContextHelper servletContextHelper; //The context used during the registration of the servlet
	private final ContextController contextController;
	private ClassLoader classLoader;

	public ListenerRegistration(
		ServiceHolder<EventListener> listenerHolder, List<Class<? extends EventListener>> classes,
		ListenerDTO listenerDTO, ServletContext servletContext,
		ServletContextHelper servletContextHelper,
		ContextController contextController) {

		super(listenerHolder.get(), listenerDTO);
		this.listenerHolder = listenerHolder;
		this.classes = classes;
		this.servletContext = servletContext;
		this.servletContextHelper = servletContextHelper;
		this.contextController = contextController;

		classLoader = contextController.getClassLoader();

		createContextAttributes();

		proxy = (EventListener)Proxy.newProxyInstance(
			classLoader, classes.toArray(new Class[0]),
			new EventListenerInvocationHandler());
	}

	@Override
	public synchronized void destroy() {
		ClassLoader original = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(classLoader);

			contextController.getListenerRegistrations().remove(this);
			contextController.getEventListeners().remove(classes, this);

			super.destroy();

			if (classes.contains(ServletContextListener.class)) {
				ServletContextListener servletContextListener =
					(ServletContextListener)super.getT();

				servletContextListener.contextDestroyed(
					new ServletContextEvent(servletContext));
			}
		}
		finally {
			destroyContextAttributes();
			Thread.currentThread().setContextClassLoader(original);
			listenerHolder.release();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ListenerRegistration)) {
			return false;
		}

		ListenerRegistration listenerRegistration = (ListenerRegistration)obj;

		return super.getT().equals(listenerRegistration.getT());
	}

	@Override
	public int hashCode() {
		return Long.valueOf(getD().serviceId).hashCode();
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public ServletContextHelper getServletContextHelper() {
		return servletContextHelper;
	}

	@Override
	public EventListener getT() {
		return proxy;
	}

	private void createContextAttributes() {
		contextController.getProxyContext().createContextAttributes(
			servletContextHelper);
	}

	private void destroyContextAttributes() {
		contextController.getProxyContext().destroyContextAttributes(
			servletContextHelper);
	}

	private class EventListenerInvocationHandler implements InvocationHandler {

		public EventListenerInvocationHandler() {
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

			ClassLoader original = Thread.currentThread().getContextClassLoader();

			try {
				Thread.currentThread().setContextClassLoader(classLoader);

				return method.invoke(ListenerRegistration.super.getT(), args);
			}
			finally {
				Thread.currentThread().setContextClassLoader(original);
			}
		}
	}

}