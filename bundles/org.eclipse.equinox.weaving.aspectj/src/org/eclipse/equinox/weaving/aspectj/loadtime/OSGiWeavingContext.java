/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   David Knibb               initial implementation      
 *   Matthew Webster           Eclipse 3.2 changes     
 *******************************************************************************/

package org.eclipse.equinox.weaving.aspectj.loadtime;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.aspectj.weaver.loadtime.DefaultWeavingContext;
import org.aspectj.weaver.tools.WeavingAdaptor;
import org.eclipse.equinox.service.weaving.SupplementerRegistry;
import org.eclipse.equinox.weaving.aspectj.WeavingServicePlugin;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.State;
import org.osgi.framework.Bundle;

public class OSGiWeavingContext extends DefaultWeavingContext {

    private final Bundle bundle;

    private final BundleDescription bundleDescription;

    private final State resolverState;

    private final SupplementerRegistry supplementerRegistry;

    public OSGiWeavingContext(final ClassLoader loader, final Bundle bundle,
            final State state, final BundleDescription bundleDescription,
            final SupplementerRegistry supplementerRegistry) {
        super(loader);
        this.bundle = bundle;
        this.bundleDescription = bundleDescription;
        this.resolverState = state;
        this.supplementerRegistry = supplementerRegistry;
        if (WeavingServicePlugin.DEBUG)
            System.out.println("- WeavingContext.WeavingContext() locader="
                    + loader + ", bundle=" + bundle.getSymbolicName());
    }

    public String getBundleIdFromURL(final URL url) {
        return resolverState.getBundle(Integer.parseInt(url.getHost()))
                .getSymbolicName();
    }

    public Bundle[] getBundles() {
        final Set bundles = new HashSet();

        // the bundle this context belongs to should be used
        bundles.add(this.bundle);

        // add required bundles
        if (this.bundle.getBundleContext() != null) {
            final BundleDescription[] resolvedRequires = this.bundleDescription
                    .getResolvedRequires();
            for (int i = 0; i < resolvedRequires.length; i++) {
                final Bundle requiredBundle = this.bundle.getBundleContext()
                        .getBundle(resolvedRequires[i].getBundleId());
                if (requiredBundle != null) {
                    bundles.add(requiredBundle);
                }
            }
        }

        // add fragment bundles
        if (this.bundle.getBundleContext() != null) {
            final BundleDescription[] fragments = this.bundleDescription
                    .getFragments();
            for (int i = 0; i < fragments.length; i++) {
                final Bundle fragmentBundle = this.bundle.getBundleContext()
                        .getBundle(fragments[i].getBundleId());
                if (fragmentBundle != null) {
                    bundles.add(fragmentBundle);
                }
            }
        }

        // add supplementers
        final Bundle[] supplementers = this.supplementerRegistry
                .getSupplementers(this.bundle);
        bundles.addAll(Arrays.asList(supplementers));

        return (Bundle[]) bundles.toArray(new Bundle[bundles.size()]);
    }

    public String getBundleVersionFromURL(final URL url) {
        return resolverState.getBundle(Integer.parseInt(url.getHost()))
                .getVersion().toString();
    }

    public String getClassLoaderName() {
        return bundleDescription.getSymbolicName();
    }

    public List getDefinitions(final ClassLoader loader,
            final WeavingAdaptor adaptor) {
        final List definitions = ((OSGiWeavingAdaptor) adaptor)
                .parseDefinitionsForBundle();
        return definitions;
    }

    public String getFile(final URL url) {
        return getBundleIdFromURL(url) + url.getFile();
    }

    public String getId() {
        return bundleDescription.getSymbolicName();
    }

    public Enumeration getResources(final String name) throws IOException {
        Enumeration result = super.getResources(name);

        if (name.endsWith("aop.xml")) {
            final Vector modified = new Vector();
            final BundleSpecification[] requires = bundleDescription
                    .getRequiredBundles();
            final BundleDescription[] fragments = bundleDescription
                    .getFragments();

            while (result.hasMoreElements()) {
                final URL xml = (URL) result.nextElement();
                final String resourceBundleName = getBundleIdFromURL(xml);

                if (bundleDescription.getSymbolicName().equals(
                        resourceBundleName)) {
                    modified.add(xml);
                    continue;
                }

                for (int i = 0; i < requires.length; i++) {
                    final BundleSpecification r = requires[i];
                    if (r.getName().equals(resourceBundleName)) {
                        modified.add(xml);
                        continue;
                    }
                }

                for (int i = 0; i < fragments.length; i++) {
                    final BundleSpecification[] fragmentRequires = fragments[i]
                            .getRequiredBundles();
                    for (int j = 0; j < fragmentRequires.length; j++) {
                        final BundleSpecification r = fragmentRequires[j];
                        if (r.getName().equals(resourceBundleName)) {
                            modified.add(xml);
                            continue;
                        }
                    }
                }
            }

            result = modified.elements();
        }
        return result;
    }

    public String toString() {
        return getClass().getName() + "[" + bundleDescription.getSymbolicName()
                + "]";
    }

}
