package com.mobiarch.tools;

import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.ComplexKey;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
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
				.println("Usage: query.sh -designdoc design_doc_name -view view_name [-reduce] [-group] [-bucket bucket_name] [-pretty] [-out output_file] [-url connection_url (defaults to http://127.0.0.1:8091/pools)] [-password bucket_password] -keys key1 key2 ... ");
	}

	public static void main(String[] args) throws Exception {
		if (hasArg(args, "-help")) {
			usage();

			return;
		}

		String url = getArg(args, "-url", "http://127.0.0.1:8091/pools");
		String password = getArg(args, "-password", "");
		String bucket = getArg(args, "-bucket", "default");
		boolean pretty = hasArg(args, "-pretty");
		boolean reduce = hasArg(args, "-reduce");
		boolean group = hasArg(args, "-group");
		String out = getArg(args, "-out", null);
		String designDoc = getArg(args, "-designdoc", null);
		String view = getArg(args, "-view", null);
		
		if (designDoc == null || view == null) {
			usage();
			
			return;
		}
		
		ArrayList<String> keys = new ArrayList<String>();
		boolean startKeys = false;
		
		for (String arg : args) {
			if (arg.equals("-keys")) {
				startKeys = true;
				
				continue;
			}
			if (startKeys) {
				keys.add(arg);
			}
		}
		
		CouchbaseClient client = null;
		
		try {
			List<URI> hosts = Arrays.asList(new URI(url));
			client = new CouchbaseClient(hosts, bucket, password);
			
			query(designDoc, view, reduce, group, client, pretty, out, keys);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (client != null) {
				client.shutdown();
			}
		}
	}

	private static void query(String designDoc, String viewName, boolean reduce, boolean group, CouchbaseClient client, boolean pretty, String out, ArrayList<String> keys) throws Exception {
		Query query = new Query();
		
		if (keys.size() > 1) {
			Object arr[] = keys.toArray(new String[keys.size()]);
			query.setKey(ComplexKey.of(arr));
		} else if (keys.size() == 1) {
			query.setKey(keys.get(0));
		}
		if (reduce)
			query.setReduce(reduce);
		if (group)
			query.setGroup(group);
		if (!reduce) {
			query.setIncludeDocs(true);
		}
		View view = client.getView(designDoc, viewName);
		ViewResponse result = client.query(view, query);

		System.out.println("=================Begin Document=====================");
		PrintStream writer = getPrintStream(out);
		for (ViewRow row : result) {
			writer.print("Key: ");
			writer.println(row.getKey());
			
			String doc = null;
			
			if (reduce) {
				doc = row.getValue();
			} else {
				doc = (String) row.getDocument();
			}

			if (doc == null) {
				writer.println("Document not found. May have been deleted.");
			} else if (!pretty) {
				writer.println(doc.toString());
			} else {
				//Format the document
				JsonParser parser = new JsonParser();
				Gson gson = new GsonBuilder().setPrettyPrinting().create();

				JsonElement el = parser.parse(doc.toString());
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
