package com.mobiarch.tools;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.PersistTo;
import com.couchbase.client.java.document.RawJsonDocument;
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
				.println("Usage: manageDoc.sh [-get | -set | -delete | -help] -key key [-bucket bucket_name] [-in input_file] [-out output_file] [-host host_name (defaults to localhost)] [-password bucket_password]");
	}

	public static void main(String[] args) throws Exception {
		if (hasArg(args, "-help")) {
			usage();

			return;
		}

		String host = getArg(args, "-host", "localhost");
		String key = getArg(args, "-key", null);
		String password = getArg(args, "-password", "");
		String bucketName = getArg(args, "-bucket", "default");
		boolean pretty = hasArg(args, "-pretty");
		String in = getArg(args, "-in", null);
		String out = getArg(args, "-out", null);
		
		if (key == null) {
			usage();

			return;
		}
		Bucket bucket;
		Cluster cluster;
	
		cluster = CouchbaseCluster.create(host);
		bucket = cluster.openBucket(bucketName, password);

		try {
			if (hasArg(args, "-get")) {
				get(key, bucket, pretty, out);
			} else if (hasArg(args, "-set")) {
				set(key, bucket, in);
			} else if (hasArg(args, "-delete")) {
				delete(key, bucket);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cluster != null) {
				cluster.disconnect();
			}
		}
	}

	private static void set(String key, Bucket bucket, String in) throws Exception {
		InputStream is = getInputStream(in);
		String obj = readFile(is);
		
		System.out.println("==========Setting document===============");
		RawJsonDocument doc = RawJsonDocument.create(key, obj);
		bucket.upsert(doc, PersistTo.MASTER);
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
		sc.close();
		
		return buff.toString();
	}
	private static void delete(String key, Bucket bucket) {
		System.out.println("==========Deleting document===============");
		RawJsonDocument doc = bucket.get(key, RawJsonDocument.class);
		
		if (doc == null) {
			System.out.println("No document for key: " + key);
		} else {
			bucket.remove(key);
			System.out.println("Done");
		}
		
		System.out.println("==================================");
	}

	private static void get(String key, Bucket bucket, boolean pretty, String out) throws Exception {
		RawJsonDocument doc = bucket.get(key, RawJsonDocument.class);
		System.out.println("=================Begin Document=====================");
		PrintStream writer = getPrintStream(out);
		
		if (doc == null) {
			System.out.println("No document found for key: " + key);
		} else {
			if (!pretty) {
				writer.println(doc.content());
			} else {
				//Format the document
				JsonParser parser = new JsonParser();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();

				JsonElement el = parser.parse(doc.content());
				String jsonString = gson.toJson(el); 
				writer.println(jsonString);
			}
			
			if (out != null) {
				System.out.println("Document saved in file: " + out);
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
