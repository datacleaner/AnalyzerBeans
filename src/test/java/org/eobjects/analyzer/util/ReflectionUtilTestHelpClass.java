package org.eobjects.analyzer.util;

import org.junit.Ignore;

@Ignore
public class ReflectionUtilTestHelpClass {

	public static class ClassA {
		private int a;

		public int getA() {
			return a;
		}
	}

	public static class ClassB extends ClassA {
		private boolean b;

		public boolean getB() {
			return b;
		}
	}
}
