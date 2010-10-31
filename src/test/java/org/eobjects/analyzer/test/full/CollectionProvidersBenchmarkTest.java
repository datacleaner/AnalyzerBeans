package org.eobjects.analyzer.test.full;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eobjects.analyzer.lifecycle.BerkeleyDbCollectionProvider;
import org.eobjects.analyzer.lifecycle.CollectionProvider;
import org.eobjects.analyzer.lifecycle.HsqldbCollectionProvider;
import org.junit.Ignore;

/**
 * A benchmark program (which is why it is @Ignore'd) used to show the
 * difference in performance between the different collection provider
 * implementations.
 * 
 * @author Kasper Sørensen
 * 
 */
@Ignore
public class CollectionProvidersBenchmarkTest extends TestCase {

	private Map<String, CollectionProvider> _collectionProviders;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_collectionProviders = new HashMap<String, CollectionProvider>();
		_collectionProviders.put("hsqldb", new HsqldbCollectionProvider());
		_collectionProviders.put("berkeley db", new BerkeleyDbCollectionProvider());
	}

	public void test1bigBatch() throws Exception {
		runBenchmarkTests(1, 1000000);
	}

	public void test100collectionsWith100items() throws Exception {
		runBenchmarkTests(100, 100);
	}

	private void runBenchmarkTests(int numCollections, int numElems) {
		System.out.println(getName() + " beginning.");
		System.out.println("(" + numCollections + " collections with " + numElems + " elements in them)");

		for (String cpName : _collectionProviders.keySet()) {
			System.out.println(cpName + " results:");

			List<Collection<?>> collections = new ArrayList<Collection<?>>(numCollections);

			long timeBefore = System.currentTimeMillis();
			CollectionProvider cp = _collectionProviders.get(cpName);
			for (int i = 0; i < numCollections; i++) {
				Set<Integer> set = cp.createSet(Integer.class);
				for (int j = 0; j < numElems; j++) {
					set.add(j);
				}
				collections.add(set);
			}

			long timeAfterAdd = System.currentTimeMillis();
			System.out.println("- time to add elements: " + (timeAfterAdd - timeBefore));

			for (Collection<?> col : collections) {
				for (Object object : col) {
					object.hashCode();
				}
			}

			long timeAfterIterate = System.currentTimeMillis();
			System.out.println("- time to iterate through collection: " + (timeAfterIterate - timeAfterAdd));

			for (Object col : collections) {
				cp.cleanUp(col);
			}

			long timeAfterCleanup = System.currentTimeMillis();
			System.out.println("- time to clean up: " + (timeAfterCleanup - timeAfterIterate));

			System.out.println("- TOTAL time: " + (timeAfterCleanup - timeBefore));
		}
		System.out.println(getName() + " finished.");
	}
}
