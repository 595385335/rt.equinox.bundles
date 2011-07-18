/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.equinox.bidi.internal.tests;

import org.eclipse.equinox.bidi.STextEngine;
import org.eclipse.equinox.bidi.STextEnvironment;
import org.eclipse.equinox.bidi.custom.STextProcessor;

/**
 * Tests RTL arithmetic
 */
public class STextMathTest extends STextTestBase {

	static final STextEnvironment envLTR = new STextEnvironment("ar", false, STextEnvironment.ORIENT_LTR);
	static final STextEnvironment envRTL = new STextEnvironment("ar", false, STextEnvironment.ORIENT_RTL);

	STextProcessor processor = STextEngine.PROC_RTL_ARITHMETIC;

	private void verifyOneLine(String msg, String data, String resLTR, String resRTL) {
		String lean = toUT16(data);
		String fullLTR = STextEngine.leanToFullText(processor, envLTR, lean, null);
		assertEquals(msg + " LTR - ", resLTR, toPseudo(fullLTR));
		String fullRTL = STextEngine.leanToFullText(processor, envRTL, lean, null);
		assertEquals(msg + " RTL - ", resRTL, toPseudo(fullRTL));
	}

	public void testRTLarithmetic() {
		verifyOneLine("Math #0", "", "", "");
		verifyOneLine("Math #1", "1+ABC", "1+ABC", ">@1+ABC@^");
		verifyOneLine("Math #2", "2+ABC-DEF", "2+ABC@-DEF", ">@2+ABC@-DEF@^");
		verifyOneLine("Math #3", "A+3*BC/DEF", "A@+3*BC@/DEF", ">@A@+3*BC@/DEF@^");
		verifyOneLine("Math #4", "4+ABC/DEF", "4+ABC@/DEF", ">@4+ABC@/DEF@^");

		verifyOneLine("Math #5", "5#BC", "<&5#BC&^", "5#BC");
		verifyOneLine("Math #6", "6#BC-DE", "<&6#BC-DE&^", "6#BC-DE");
		verifyOneLine("Math #7", "7#BC+DE", "<&7#BC+DE&^", "7#BC+DE");
		verifyOneLine("Math #8", "8#BC*DE", "<&8#BC*DE&^", "8#BC*DE");
		verifyOneLine("Math #9", "9#BC/DE", "<&9#BC/DE&^", "9#BC/DE");
		verifyOneLine("Math #10", "10ab+cd-ef", "10ab+cd-ef", ">@10ab+cd-ef@^");

	}
}
