/*******************************************************************************
 * Copyright (c) 2007 IBM Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.http.registry.internal;

import java.util.*;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.osgi.framework.*;
import org.osgi.service.http.*;
import org.osgi.service.packageadmin.PackageAdmin;

public class HttpRegistryManager {

	class ResourcesContribution {
		String alias;
		String baseName;
		String httpContextId;
		IContributor contributor;

		public ResourcesContribution(String alias, String baseName, String httpContextId, IContributor contributor) {
			this.alias = alias;
			this.baseName = baseName;
			this.httpContextId = httpContextId;
			this.contributor = contributor;
		}
	}

	class ServletContribution {
		String alias;
		Servlet servlet;
		Dictionary initparams;
		String httpContextId;
		IContributor contributor;

		public ServletContribution(String alias, Servlet servlet, Dictionary initparams, String httpContextId, IContributor contributor) {
			this.alias = alias;
			this.servlet = servlet;
			this.initparams = initparams;
			this.httpContextId = httpContextId;
			this.contributor = contributor;
		}
	}

	class HttpContextContribution {
		HttpContext context;
		IContributor contributor;

		public HttpContextContribution(HttpContext context, IContributor contributor) {
			this.context = context;
			this.contributor = contributor;
		}
	}

	private HttpContextManager httpContextManager;
	private ServletManager servletManager;
	private ResourceManager resourceManager;
	private HttpService httpService;
	private PackageAdmin packageAdmin;
	private Map contexts = new HashMap();
	private Map servlets = new HashMap();
	private Map resources = new HashMap();
	private Set registered = new HashSet();

	public HttpRegistryManager(ServiceReference reference, HttpService httpService, PackageAdmin packageAdmin, IExtensionRegistry registry) {
		this.httpService = httpService;
		this.packageAdmin = packageAdmin;

		httpContextManager = new HttpContextManager(this, registry);
		servletManager = new ServletManager(this, registry);
		resourceManager = new ResourceManager(this, registry);
	}

	public void start() {
		httpContextManager.start();
		servletManager.start();
		resourceManager.start();
	}

	public void stop() {
		httpContextManager.stop();
		servletManager.stop();
		resourceManager.stop();
	}

	public synchronized boolean addResourcesContribution(String alias, String baseName, String httpContextId, IContributor contributor) {
		if (resources.containsKey(alias) || servlets.containsKey(alias))
			return false; // TODO: should log this

		ResourcesContribution contribution = new ResourcesContribution(alias, baseName, httpContextId, contributor);
		resources.put(alias, contribution);
		if (httpContextId == null || contexts.containsKey(httpContextId))
			registerResources(contribution);

		return true;
	}

	public synchronized boolean addServletContribution(String alias, Servlet servlet, Dictionary initparams, String httpContextId, IContributor contributor) {
		if (resources.containsKey(alias) || servlets.containsKey(alias))
			return false; // TODO: should log this

		ServletContribution contribution = new ServletContribution(alias, servlet, initparams, httpContextId, contributor);
		servlets.put(alias, contribution);
		if (httpContextId == null || contexts.containsKey(httpContextId))
			registerServlet(contribution);

		return true;
	}

	public synchronized void removeContribution(String alias) {
		resources.remove(alias);
		servlets.remove(alias);
		unregister(alias);
	}

	public synchronized HttpContext getHttpContext(String httpContextId, Bundle bundle) {
		HttpContextContribution contribution = (HttpContextContribution) contexts.get(httpContextId);
		if (contribution == null)
			return null;

		if (System.getSecurityManager() != null) {
			Bundle httpContextBundle = getBundle(contribution.contributor);
			AdminPermission resourcePermission = new AdminPermission(httpContextBundle, "resource"); //$NON-NLS-1$
			if (!bundle.hasPermission(resourcePermission))
				return null;
		}
		return contribution.context;
	}

	public synchronized boolean addHttpContextContribution(String httpContextId, HttpContext context, IContributor contributor) {
		if (contexts.containsKey(httpContextId))
			return false; // TODO: should log this

		contexts.put(httpContextId, new HttpContextContribution(context, contributor));
		for (Iterator it = resources.values().iterator(); it.hasNext();) {
			ResourcesContribution contribution = (ResourcesContribution) it.next();
			if (httpContextId.equals(contribution.httpContextId))
				registerResources(contribution);
		}

		for (Iterator it = servlets.values().iterator(); it.hasNext();) {
			ServletContribution contribution = (ServletContribution) it.next();
			if (httpContextId.equals(contribution.httpContextId))
				registerServlet(contribution);
		}
		return true;
	}

	public synchronized void removeHttpContextContribution(String httpContextId) {
		if (contexts.remove(httpContextId) != null) {
			for (Iterator it = resources.values().iterator(); it.hasNext();) {
				ResourcesContribution contribution = (ResourcesContribution) it.next();
				if (httpContextId.equals(contribution.httpContextId))
					unregister(contribution.alias);
			}

			for (Iterator it = servlets.values().iterator(); it.hasNext();) {
				ServletContribution contribution = (ServletContribution) it.next();
				if (httpContextId.equals(contribution.httpContextId))
					unregister(contribution.alias);
			}
		}
	}

	public DefaultRegistryHttpContext createDefaultRegistryHttpContext() {
		HttpContext defaultContext = httpService.createDefaultHttpContext();
		return new DefaultRegistryHttpContext(defaultContext);
	}

	public Bundle getBundle(IContributor contributor) {
		return getBundle(contributor.getName());
	}
	public Bundle getBundle(String symbolicName) {
		Bundle[] bundles = packageAdmin.getBundles(symbolicName, null);
		if (bundles == null)
			return null;
		//Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	private void registerResources(ResourcesContribution contribution) {
		HttpContext context = getHttpContext(contribution.httpContextId, contribution.contributor);
		if (context == null)
			return;
		try {
			httpService.registerResources(contribution.alias, contribution.baseName, context);
			registered.add(contribution.alias);
		} catch (NamespaceException e) {
			// TODO: should log this
			e.printStackTrace();
		}
	}

	private void registerServlet(ServletContribution contribution) {
		HttpContext context = getHttpContext(contribution.httpContextId, contribution.contributor);
		if (context == null)
			return;
		try {
			httpService.registerServlet(contribution.alias, contribution.servlet, contribution.initparams, context);
			registered.add(contribution.alias);
		} catch (NamespaceException e) {
			// TODO: should log this
			e.printStackTrace();
		} catch (ServletException e) {
			// TODO: should log this
			e.printStackTrace();
		}
	}

	private void unregister(String alias) {
		if (registered.remove(alias))
			httpService.unregister(alias);
	}

	private HttpContext getHttpContext(String httpContextId, IContributor contributor) {
		if (httpContextId == null) {
			DefaultRegistryHttpContext defaultContext = createDefaultRegistryHttpContext();
			defaultContext.addResourceMapping(getBundle(contributor), null);
			return defaultContext;
		}

		HttpContextContribution contribution = (HttpContextContribution) contexts.get(httpContextId);
		if (System.getSecurityManager() != null) {
			Bundle contributorBundle = getBundle(contributor);
			Bundle httpContextBundle = getBundle(contribution.contributor);
			AdminPermission resourcePermission = new AdminPermission(httpContextBundle, "resource"); //$NON-NLS-1$
			if (!contributorBundle.hasPermission(resourcePermission))
				return null;
		}
		return contribution.context;
	}
}
