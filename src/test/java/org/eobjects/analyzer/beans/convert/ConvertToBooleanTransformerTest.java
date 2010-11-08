package org.eobjects.analyzer.beans.convert;

import junit.framework.TestCase;

public class ConvertToBooleanTransformerTest extends TestCase {

	public void testTransform() throws Exception {
		assertNull(ConvertToBooleanTransformer.transformValue("hello"));
		assertNull(ConvertToBooleanTransformer.transformValue(""));
		assertNull(ConvertToBooleanTransformer.transformValue(-1));
		assertNull(ConvertToBooleanTransformer.transformValue(5000));
		
		assertTrue(ConvertToBooleanTransformer.transformValue(true));
		assertTrue(ConvertToBooleanTransformer.transformValue("true"));
		assertTrue(ConvertToBooleanTransformer.transformValue(1));
		assertTrue(ConvertToBooleanTransformer.transformValue("yes"));
		assertTrue(ConvertToBooleanTransformer.transformValue("tRUe"));
		assertTrue(ConvertToBooleanTransformer.transformValue("1"));
		
		assertFalse(ConvertToBooleanTransformer.transformValue(false));
		assertFalse(ConvertToBooleanTransformer.transformValue("false"));
		assertFalse(ConvertToBooleanTransformer.transformValue(0));
		assertFalse(ConvertToBooleanTransformer.transformValue("no"));
		assertFalse(ConvertToBooleanTransformer.transformValue("fALse"));
		assertFalse(ConvertToBooleanTransformer.transformValue("0"));
	}
}
