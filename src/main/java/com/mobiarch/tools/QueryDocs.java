package com.mobiarch.tools;

import java.io.PrintStream;
import java.util.Iterator;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.view.Stale;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class QueryDocs {
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
				.println("Usage: query.sh -designdoc design_doc_name -view view_name [-reduce] [-group] [-bucket bucket_name] [-pretty] [-out output_file] [-host host_name (defaults to localhost)] [-password bucket_password] [-key key | [key1, key2]] [-stale ok|false|update_after]");
	}

	public static void main(String[] args) throws Exception {
		if (hasArg(args, "-help")) {
			usage();

			return;
		}

		String host = getArg(args, "-host", "localhost");
		String password = getArg(args, "-password", "");
		String bucketName = getArg(args, "-bucket", "default");
		boolean pretty = hasArg(args, "-pretty");
		boolean reduce = hasArg(args, "-reduce");
		boolean group = hasArg(args, "-group");
		String out = getArg(args, "-out", null);
		String designDoc = getArg(args, "-designdoc", null);
		String view = getArg(args, "-view", null);
		String staleStr = getArg(args, "-stale", "update_after");
		String key = getArg(args, "-key", null);
		
		if (designDoc == null || view == null) {
			usage();
			
			return;
		}
		
		Stale stale = Stale.UPDATE_AFTER;
		
		if (staleStr.equals("ok")) {
			stale = Stale.TRUE;
		} else if (staleStr.equals("false")) {
			stale = Stale.FALSE;
		} else if (staleStr.equals("update_after")) {
			stale = Stale.UPDATE_AFTER;
		} else {
			usage();
			
			return;
		}
		
		Bucket bucket;
		Cluster cluster;
	
		cluster = CouchbaseCluster.create(host);
		bucket = cluster.openBucket(bucketName);
		
		try {
			query(designDoc, view, reduce, group, bucket, pretty, out, key, null, stale);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cluster != null) {
				cluster.disconnect();
			}
		}
	}

	private static void query(String designDoc, String viewName, boolean reduce, boolean group, 
			Bucket bucket, boolean pretty, String out, String key, String complexKey, Stale stale) throws Exception {
		ViewQuery query = ViewQuery.from(designDoc, viewName)
				.stale(stale)
				.limit(2);
		
		if (complexKey != null) {
			//query.setKey(complexKey);
		} else if (key != null) {
			query.key(key);
		}
		if (reduce)
			query.reduce(reduce);
		if (group)
			query.group(group);
		if (!reduce) {
		}
		ViewResult result = bucket.query(query);

		System.out.println("=================Begin Document=====================");
		PrintStream writer = getPrintStream(out);
		Iterator<ViewRow> iter = result.rows();
		
		while (iter.hasNext()) {
			RawJsonDocument doc = iter.next().document(RawJsonDocument.class);
			
			if (doc == null) {
				//Skipping deleted stale document
				continue;
			}
			writer.print("Key: ");
			writer.println(doc.id());
			
			String content = null;
			
			if (reduce) {
				//doc = row.getValue();
			} else {
				content = doc.content();
			}

			if (content == null) {
				writer.println("Document not found. May have been deleted.");
			} else if (!pretty) {
				writer.println(doc.toString());
			} else {
				//Format the document
				JsonParser parser = new JsonParser();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();

				JsonElement el = parser.parse(content);
				String jsonString = gson.toJson(el); 
				writer.println(jsonString);
			}
		}
		
		if (out != null) {
			System.out.println("Document saved in file: " + out);
			writer.close();
		}
		System.out.println("=================End Document=====================");
	}

	private static PrintStream getPrintStream(String out) throws Exception {
		if (out == null) {
			return System.out;
		}
		
		return new PrintStream(out);
	}
}
