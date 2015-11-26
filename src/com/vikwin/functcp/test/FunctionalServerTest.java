package com.vikwin.functcp.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import com.vikwin.functcp.client.FunctionalTCPClient;
import com.vikwin.functcp.server.FunctionalTCPServer;

/**
 * Simple Unit tests for server-client combo.
 * 
 * @author Viktor Winkelmann
 *
 */
public class FunctionalServerTest {

	@Test
	public void testSimpleObject() throws InterruptedException, UnknownHostException, ClassNotFoundException, IOException {
		Function<String, Integer> f = this::testSimpleHandler;

		FunctionalTCPClient<String, Integer> client = new FunctionalTCPClient<String, Integer>(
				"127.0.0.1", 9090);

		FunctionalTCPServer<String, Integer> server = new FunctionalTCPServer<String, Integer>(f, 
				9090);
		
		server.start();
		
		Thread.sleep(1000);
		
		Integer t1 = client.sendRepliedRequest("one");
		Integer t2 = client.sendRepliedRequest("two");
		Integer t3 = client.sendRepliedRequest("three");

		System.out.println(t1);
		System.out.println(t2);
		System.out.println(t3);
		
		assertTrue(1 == t1);
		assertTrue(2 == t2);
		assertTrue(0 == t3);
		
		server.shutdown();
	}

	private Integer testSimpleHandler(String s) {
		switch (s) {
		case "one":
			return 1;
		case "two":
			return 2;
		}
		
		return 0;
	}
	
	@Test
	public void testCascadedObject() throws InterruptedException, UnknownHostException, ClassNotFoundException, IOException {
		Function<List<String>, Integer> f = this::testCascadedHandler;

		FunctionalTCPClient<List<String>, Integer> client = new FunctionalTCPClient<List<String>, Integer>(
				"127.0.0.1", 9090);

		FunctionalTCPServer<List<String>, Integer> server = new FunctionalTCPServer<List<String>, Integer>(f, 
				9090);
		
		server.start();
		
		Thread.sleep(1000);
		
		
		List<String> tmp = new ArrayList<>();
		tmp.add("one");
		tmp.add("two");
		
		Integer t1 = client.sendRepliedRequest(tmp);
		Integer t2 = client.sendRepliedRequest(new ArrayList<>());
		

		System.out.println(t1);
		System.out.println(t2);
		
		
		assertTrue(1 == t1);
		assertTrue(0 == t2);
		
		server.shutdown();
	}
	
	private Integer testCascadedHandler(List<String> l) {
		List<String> tmp = new ArrayList<>();
		tmp.add("one");
		tmp.add("two");
		
		if (l.equals(tmp))
			return 1;
		else
			return 0;
	}

}
