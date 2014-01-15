package com.mobiarch.tools;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

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
				.println("Usage: manageDoc.sh [-show | -delete | -help] -key key [-bucket bucket_name] [-url connection_url (defaults to http://127.0.0.1:8091/pools)] [-password bucket_password]");
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
		
		if (key == null) {
			usage();

			return;
		}
		CouchbaseClient client = null;
		
		try {
			List<URI> hosts = Arrays.asList(new URI(url));
			client = new CouchbaseClient(hosts, bucket, password);
			
			if (hasArg(args, "-show")) {
				show(key, client, pretty);
			} else if (hasArg(args, "-delete")) {
				delete(key, client);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (client != null) {
				client.shutdown();
			}
		}
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

	private static void show(String key, CouchbaseClient client, boolean pretty) {
		Object doc = client.get(key);
		System.out.println("=================Begin Document=====================");
		if (doc == null) {
			System.out.println("No document found for key: " + key);
		} else {
			if (!pretty) {
				System.out.println(doc.toString());
			} else {
				//Format the document
				JsonParser parser = new JsonParser();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();

				JsonElement el = parser.parse(doc.toString());
				String jsonString = gson.toJson(el); 
				System.out.println(jsonString);
			}
		}
		System.out.println("=================End Document=====================");
	}

}
