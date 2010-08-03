package org.eobjects.analyzer.descriptors;

import java.lang.reflect.Method;

import junit.framework.TestCase;

public class CloseMethodDescriptorImplTest extends TestCase {

	private boolean executed;

	public void testClose() throws Exception {
		executed = false;
		Method m = getClass().getDeclaredMethod("doClose");
		CloseMethodDescriptorImpl closeMethodDescriptorImpl = new CloseMethodDescriptorImpl(
				m);
		closeMethodDescriptorImpl.close(this);

		assertTrue(executed);

		assertEquals(
				"CloseMethodDescriptorImpl[method=doClose]",
				closeMethodDescriptorImpl.toString());
	}

	public void doClose() {
		executed = true;
	}
}
