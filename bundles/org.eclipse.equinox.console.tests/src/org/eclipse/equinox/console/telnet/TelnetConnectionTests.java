/*******************************************************************************
 * Copyright (c) 2011, 2017 SAP AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lazar Kirchev, SAP AG - initial contribution
 ******************************************************************************/

package org.eclipse.equinox.console.telnet;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.Assert;
import org.junit.Test;
import org.easymock.EasyMock;
import org.eclipse.equinox.console.common.ConsoleInputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TelnetConnectionTests {

	private static final String HOST = "localhost";
	private static final int TEST_CONTENT = 100;
	private static final int IAC = 255;

	@Test
	public void testTelneConnection() throws Exception {

		try (ServerSocket servSocket = new ServerSocket(0);
				Socket socketClient = new Socket(HOST, servSocket.getLocalPort());
				Socket socketServer = servSocket.accept();
				CommandSession session = EasyMock.createMock(CommandSession.class)) {

			session.put((String) EasyMock.anyObject(), EasyMock.anyObject());
			EasyMock.expectLastCall().times(3);
			EasyMock.expect(session.execute((String) EasyMock.anyObject())).andReturn(null);
			session.close();
			EasyMock.expectLastCall();
			EasyMock.replay(session);

			CommandProcessor processor = EasyMock.createMock(CommandProcessor.class);
			EasyMock.expect(processor.createSession((ConsoleInputStream) EasyMock.anyObject(),
					(PrintStream) EasyMock.anyObject(), (PrintStream) EasyMock.anyObject())).andReturn(session);
			EasyMock.replay(processor);

			try (TelnetConnection connection = new TelnetConnection(socketServer, processor, null)) {
				connection.start();

				try (OutputStream outClient = socketClient.getOutputStream()) {
					outClient.write(TEST_CONTENT);
					outClient.write('\n');
					outClient.flush();

					InputStream input = socketServer.getInputStream();
					int in = input.read();
					Assert.assertTrue(
							"Server received [" + in + "] instead of " + TEST_CONTENT + " from the telnet client.",
							in == TEST_CONTENT);

					input = socketClient.getInputStream();
					in = input.read();
					// here IAC is expected, since when the output stream in TelnetConsoleSession is
					// created, several telnet
					// commands are written to it, each of them starting with IAC
					Assert.assertTrue("Client receive telnet responses from the server unexpected value [" + in
							+ "] instead of " + IAC + ".", in == IAC);
					connection.telnetNegotiationFinished();
					Thread.sleep(5000);
					EasyMock.verify(session, processor);
				}
			}
		}
	}
}
