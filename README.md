# CouchTools

Couchbase is more than awesome. But it sure will be cool to have more command line tools. Here is my attempt that.

##importViews - Import views and reduces
This tool lets you keep view and reduce definitions in files. You can version control these files. You can then import them into Couchbase.

For each view, create a file with the same name as the view and with a .map extension. For example, if you have a view called UserByEmail, then create a file called UserByEmail.map and add the JavaScript code there.

If a view has a reduce function, then in addition to the .map file, create a .reduce file and add the reduce code there. For example, if you have a view called CountLikes then you will need two files - CountLikes.map and CountLikes.reduce.

###Usage
	./importViews.sh -designdoc design_document_name -bucket bucket_name [-host hostname] [-port port_number] [-user userID] [-password password] [-help] file1.map file2.map...

The options are as follows:

- **-designdoc** - The name of the design document. This can be a development or production design document. For a production site, there is no need to import the views to the development design document. You can directly import them in the production design document.
- **-bucket** - The name of the bucket. 
- **-host** - The host name of the Couchbase server. Defaults to localhost.
- **-port** - The administrative port number. Defaults to 8092.
- **-user** - The administrative user ID.
- **-password** - The admin password
- **Map files** - One map file for each view. Map files for **all** views in the design document must be specified in a single command line. Files must have .map extension. The base name of a map file will determine the name of the view. If system locates a file with the view name and .reduce extension, it will also be loaded as the reduce function.

###Examples
Let us say that you have a view called UserByEmail with the following code:

	 function (doc, meta) {
		 if (doc.type == "UserProfile") {
			 emit(doc.email, null);
		 }
	 }

Create a file called UserByEmail.map and add the code above.

To import the view into the design document called UserUtilities in the default bucket, run this command:

	./importViews.sh -designdoc UserUtilities -bucket default -user Admin -password pass UserByEmail.map

##manageDoc - Manage documents
You can perform various operations on documents. Currently viewing and deleting is supported.

###Usage
	./manageDoc.sh [-get | -delete | -help] -key key [-in input_file] [-out output_file] [-bucket bucket_name] [-url connection_url] [-password bucket_password] [-pretty]

The options are as follows:

- **-key** - The key of the document.
- **-get** - Dump the document for the given key. If an output file is set using -out then the document is saved in that file.
- **-set** - Set the document for the given key. If an input file is set using -in then the document data is read from the file. Otherwise, the document data
is read from the standard input. if a document by the given key does not exist, it will be added. Otherwise, it will be updated.
- **-delete** - Delete the document.
- **-pretty** - Format the document output for JSON. Only used with -show. Use it only if you know that the document is JSON.
- **-bucket** - The name of the bucket. Defaults to "default".
- **-url** - The connection URL. Defaults to: http://127.0.0.1:8091/pools.
- **-password** - The bucket password if any.

###Example
Show a JSON document with key xyz1bc123:

	./manageDoc.sh -get -key xyz1bc123 -pretty
	
Save a document in a file called doc.json:

	./manageDoc.sh -get -key xyz1bc123 -pretty -out doc.json
	
Update a document by reading from a file called doc.json:

	./manageDoc.sh -set -key xyz1bc123 -in doc.json

Delete a document with the key xyz1bc123:

	./manageDoc.sh -delete -key xyz1bc123


