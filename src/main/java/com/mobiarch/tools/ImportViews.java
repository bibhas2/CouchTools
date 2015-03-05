package com.mobiarch.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ImportViews {
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
				.println("Usage: importViews -designdoc design_document_name -bucket bucket_name [-host hostname] [-port port_number] [-user userID] [-password password] [-help] files...");
	}

	public static void main(String[] args) throws Exception {
		if (hasArg(args, "-help")) {
			usage();

			return;
		}

		String host = getArg(args, "-host", "localhost");
		String port = getArg(args, "-port", "8092");
		String designDoc = getArg(args, "-designdoc", null);
		String userId = getArg(args, "-user", null);
		String password = getArg(args, "-password", null);
		String bucket = getArg(args, "-bucket", null);
		
		if (designDoc == null || bucket == null) {
			usage();

			return;
		}
		ArrayList<String> mapFiles = buildMapFileList(args);

		URL url = new URL("http://" + host + ":" + port + "/" + bucket + "/_design/" + designDoc);
		HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection();

		urlConnection.setDoInput(true);
		urlConnection.setDoOutput(true);
		
		urlConnection.setRequestMethod("PUT");
		if (userId != null && password != null) {
			String auth = DatatypeConverter
					.printBase64Binary((userId + ":" + password).getBytes());

			urlConnection.setRequestProperty("Authorization", "Basic " + auth);
			urlConnection.setRequestProperty("Content-Type", "application/json");
		}

		PrintStream out = new PrintStream(urlConnection.getOutputStream());
		//out = System.out;
		JsonObject root = new JsonObject();
		JsonObject views = new JsonObject();
		
		root.add("views", views);

		// For each map file
		for (int i = 0; i < mapFiles.size(); ++i) {
			String mapFile = mapFiles.get(i);
			
			System.out.println("Loading map file: " + mapFile);
			
			File f = new File(mapFile);
			String viewName = f.getName();
			//Strip out .map
			viewName = viewName.substring(0, viewName.length() - 4);
			
			String body = readFile(f);
			
			JsonObject view = new JsonObject();
			
			views.add(viewName, view);
			view.addProperty("map", body);
			
			//See if reduce file exists
			f = new File(viewName + ".reduce");
			
			if (f.exists()) {
				body = readFile(f);
				view.addProperty("reduce", body);
			}
			
		}

		Gson gson = new Gson();
		//Dump the JSON to output stream
		out.print(gson.toJson(root));
		
		InputStream is = urlConnection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		
		String line;
		
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
		
		br.close();
		out.close();
	}

	private static String readFile(File f) throws Exception {
		FileInputStream fis = new FileInputStream(f);
		byte data[] = new byte[(int) f.length()];
		
		fis.read(data);
		fis.close();
		
		return new String(data, Charset.forName("UTF-8"));
	}

	private static ArrayList<String> buildMapFileList(String[] args) {
		ArrayList<String> list = new ArrayList<String>();

		for (String arg : args) {
			if (arg.endsWith(".map")) {
				list.add(arg);
			}
		}
		return list;
	}
}
