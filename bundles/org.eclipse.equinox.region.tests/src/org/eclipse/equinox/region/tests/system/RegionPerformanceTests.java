/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.region.tests.system;

import java.util.Collection;
import org.eclipse.core.tests.harness.PerformanceTestRunner;
import org.eclipse.equinox.region.*;
import org.osgi.framework.*;

public class RegionPerformanceTests extends AbstractRegionSystemTest {
	Bundle testBundle;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		testBundle = bundleInstaller.installBundle(PP1);
		testBundle.start();
	}

	private void doTestGetBundles(String fingerPrintName, String degradation) {
		final BundleContext context = testBundle.getBundleContext();
		PerformanceTestRunner runner = new PerformanceTestRunner() {
			protected void test() {
				Bundle[] bundles = context.getBundles();
				for (Bundle bundle : bundles) {
					context.getBundle(bundle.getBundleId());
				}
			}
		};
		runner.setRegressionReason(degradation);
		runner.run(this, fingerPrintName, 10, 300);
	}

	public void testGetBundlesNoRegions() throws BundleException {
		regionBundle.stop();
		doTestGetBundles(null, null);
	}

	public void testGetBundles10Regions() throws BundleException {
		createRegions(10);
		doTestGetBundles(null, null);
	}

	public void testGetBundles100Regions() throws BundleException {
		createRegions(100);
		doTestGetBundles(null, null);
	}

	public void testGetBundles1000Regions() throws BundleException {
		createRegions(1000);
		doTestGetBundles(null, null);
	}

	public void testGetServicesNoRegions() throws BundleException {
		regionBundle.stop();
		doTestGetServices(null, null);
	}

	public void testGetServices10Regions() throws BundleException {
		createRegions(10);
		doTestGetServices(null, null);
	}

	public void testGetServices100Regions() throws BundleException {
		createRegions(100);
		doTestGetBundles(null, null);
	}

	public void testGetServices1000Regions() throws BundleException {
		createRegions(1000);
		doTestGetBundles(null, null);
	}

	private void doTestGetServices(String fingerPrintName, String degradation) {
		final BundleContext context = testBundle.getBundleContext();
		PerformanceTestRunner runner = new PerformanceTestRunner() {
			protected void test() {
				try {
					Collection<ServiceReference<RegionDigraph>> refs = context.getServiceReferences(RegionDigraph.class, null);
				} catch (InvalidSyntaxException e) {
					fail(e.getMessage());
				}
			}
		};
		runner.setRegressionReason(degradation);
		runner.run(this, fingerPrintName, 10, 2000);
	}

	private void createRegions(final int numRegions) throws BundleException {
		System.out.println("Starting region create: " + numRegions);
		long time = System.currentTimeMillis();
		Region system = digraph.getRegion(0);
		RegionFilterBuilder builder = digraph.createRegionFilterBuilder();
		builder.allowAll(RegionFilter.VISIBLE_BUNDLE_NAMESPACE);
		builder.allowAll(RegionFilter.VISIBLE_HOST_NAMESPACE);
		builder.allowAll(RegionFilter.VISIBLE_PACKAGE_NAMESPACE);
		builder.allowAll(RegionFilter.VISIBLE_REQUIRE_NAMESPACE);
		builder.allowAll(RegionFilter.VISIBLE_SERVICE_NAMESPACE);
		RegionFilter filter = builder.build();
		for (int i = 0; i < numRegions; i++) {
			Region r = digraph.createRegion(getName() + i);
			digraph.connect(system, filter, r);
		}
		System.out.println("Done creating region: " + (System.currentTimeMillis() - time));
	}
}
