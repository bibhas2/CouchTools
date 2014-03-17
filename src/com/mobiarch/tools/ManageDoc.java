package com.mobiarch.tools;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import net.spy.memcached.PersistTo;

import com.couchbase.client.CouchbaseClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ManageDoc {
	public static String getArg(String args[], String arg, String defaultValue) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals(arg)) {
				if ((i + 1) < args.length) {
					String val = args[i + 1];

					if (val.startsWith("-") == false) {
						return val;
					}
				}
				break;
			}
		}
		return defaultValue;
	}

	public static boolean hasArg(String args[], String arg) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals(arg)) {
				return true;
			}
		}
		return false;
	}

	public static void usage() {
		System.out
				.println("Usage: manageDoc.sh [-get | -set | -delete | -help] -key key [-bucket bucket_name] [-in input_file] [-out output_file] [-url connection_url (defaults to http://127.0.0.1:8091/pools)] [-password bucket_password]");
	}

	public static void main(String[] args) throws Exception {
		if (hasArg(args, "-help")) {
			usage();

			return;
		}

		String url = getArg(args, "-url", "http://127.0.0.1:8091/pools");
		String key = getArg(args, "-key", null);
		String password = getArg(args, "-password", "");
		String bucket = getArg(args, "-bucket", "default");
		boolean pretty = hasArg(args, "-pretty");
		String in = getArg(args, "-in", null);
		String out = getArg(args, "-out", null);
		
		if (key == null) {
			usage();

			return;
		}
		CouchbaseClient client = null;
		
		try {
			List<URI> hosts = Arrays.asList(new URI(url));
			client = new CouchbaseClient(hosts, bucket, password);
			
			if (hasArg(args, "-get")) {
				get(key, client, pretty, out);
			} else if (hasArg(args, "-set")) {
				set(key, client, in);
			} else if (hasArg(args, "-delete")) {
				delete(key, client);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (client != null) {
				client.flush();
				client.shutdown();
			}
		}
	}

	private static void set(String key, CouchbaseClient client, String in) throws Exception {
		InputStream is = getInputStream(in);
		String obj = readFile(is);
		
		System.out.println("==========Setting document===============");
		client.set(key, obj, PersistTo.MASTER);
		System.out.println(obj);
		System.out.println("=========================================");
		
		if (in != null) {
			is.close();
		}
	}
	
	private static String readFile(InputStream fis) throws Exception {
		StringBuffer buff = new StringBuffer(1024);
		Scanner sc = new Scanner(fis);
		
		while(sc.hasNextLine()) {
			buff.append(sc.nextLine());
			buff.append("\n");
		}
		
		return buff.toString();
	}
	private static void delete(String key, CouchbaseClient client) {
		System.out.println("==========Deleting document===============");
		Object doc = client.get(key);
		
		if (doc == null) {
			System.out.println("No document for key: " + key);
		} else {
			client.delete(key);
			System.out.println("Done");
		}
		
		System.out.println("==================================");
	}

	private static void get(String key, CouchbaseClient client, boolean pretty, String out) throws Exception {
		Object doc = client.get(key);
		System.out.println("=================Begin Document=====================");
		PrintStream writer = getPrintStream(out);
		
		if (doc == null) {
			System.out.println("No document found for key: " + key);
		} else {
			if (!pretty) {
				writer.println(doc.toString());
			} else {
				//Format the document
				JsonParser parser = new JsonParser();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();

				JsonElement el = parser.parse(doc.toString());
				String jsonString = gson.toJson(el); 
				writer.println(jsonString);
			}
			if (out != null) {
				System.out.println("Document saved in file: " + out);
			} else {
				writer.close();
			}
		}
		System.out.println("=================End Document=====================");
	}

	private static PrintStream getPrintStream(String out) throws Exception {
		if (out == null) {
			return System.out;
		}
		
		return new PrintStream(out);
	}
	
	private static InputStream getInputStream(String in) throws Exception {
		if (in == null) {
			return System.in;
		}
		return new FileInputStream(in);
	}
}
