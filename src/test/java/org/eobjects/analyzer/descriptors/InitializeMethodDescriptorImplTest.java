package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Method;

import junit.framework.TestCase;

public class InitializeMethodDescriptorImplTest extends TestCase {

	private boolean executed;

	public void testInitialize() throws Exception {
		executed = false;
		Method m = getClass().getDeclaredMethod("doInitialize");
		InitializeMethodDescriptorImpl initializeMethodDescriptorImpl = new InitializeMethodDescriptorImpl(
				m);
		initializeMethodDescriptorImpl.initialize(this);

		assertTrue(executed);

		assertEquals("InitializeMethodDescriptorImpl[method=doInitialize]",
				initializeMethodDescriptorImpl.toString());
	}

	public void doInitialize() {
		executed = true;
	}
}
