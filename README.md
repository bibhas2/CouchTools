# CouchTools

Couchbase is more than awesome. But it sure will be cool to have more command line tools. Here is my attempt that.

##Building and Installation
Run this Maven command:

```
mvn clean package assembly:single
```

Copy ``target/CouchTools-bin.zip`` somewhere and unzip it. You can then run the shell scripts from the extracted folder.

##Query - Query a view
This tool lets you query a view. You can supply some of the common query options like reduce, group etc.

###Usage
```
./query.sh -designdoc design_doc_name -view view_name [-reduce] [-group] \
  [-bucket bucket_name] [-host host_name] \
  [-pretty] [-out output_file] \
  [-password bucket_password] [-key key | [key1, key2, ...]]
```

The options are as follows:

- **-designdoc** - The name of the design document. This can be a development or production design document. 
- **-view** - The name of the view to query.
- **-reduce** - Set the reduce flag for the query.
- **-group** - Set the group flag.
- **-pretty** - Format the document output for JSON. Use it only if you know that the document is JSON.
- **-out** - The file to save the result to. If not supplied, the result is dumped on the standard output.
- **-bucket** - The name of the bucket. Defaults to "default".
- **-host** - The host name of couchbase server. Defaults to localhost.
- **-password** - The bucket password if any.
- **-key** - The key for the query. You can supply a composite key by using a JSON array. For example: -key ['key1','key2']
- **-stale** - Set the stale option to one of: ok, false and update_after. Default is update_after.

###Examples

```
./query.sh -designdoc SSProject -view ProjectByTag -pretty -key Drums
./query.sh -designdoc SSProject -view ProjectByTag -pretty -key ['Drums', 'Tabla']
./query.sh -designdoc SSProject -view ProjectByTag -reduce -group
```

##ImportViews - Import views and reduces
This tool lets you keep view and reduce definitions in files. You can version control these files. You can then import them into Couchbase.

For each view, create a file with the same name as the view and with a .map extension. For example, if you have a view called UserByEmail, then create a file called UserByEmail.map and add the JavaScript code there.

If a view has a reduce function, then in addition to the .map file, create a .reduce file and add the reduce code there. For example, if you have a view called CountLikes then you will need two files - CountLikes.map and CountLikes.reduce.

###Usage
```
./import-views.sh -designdoc design_document_name -bucket bucket_name \
  [-host hostname] [-port port_number] [-user userID] [-password password] \
  [-help] file1.map file2.map...
```

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

```javascript
function (doc, meta) {
    if (doc.type == "UserProfile") {
        emit(doc.email, null);
    }
}
```

Create a file called UserByEmail.map and add the code above.

To import the view into the design document called UserUtilities in the default bucket, run this command:

```
./import-views.sh -designdoc UserUtilities -bucket default -user Admin -password pass UserByEmail.map
```

##ManageDoc - Manage documents
You can perform various operations on documents. Such as viewing and deleting documents.

###Usage
```
./manage-doc.sh [-get | -delete | -help] -key key [-in input_file] \
  [-out output_file] [-bucket bucket_name] [-host host_name] \
  [-password bucket_password] [-pretty]
```

The options are as follows:

- **-key** - The key of the document.
- **-get** - Dump the document for the given key. If an output file is set using -out then the document is saved in that file.
- **-set** - Set the document for the given key. If an input file is set using -in then the document data is read from the file. Otherwise, the document data
is read from the standard input. If a document by the given key does not exist, it will be added. Otherwise, it will be updated. Only textual documents are currently
supported, which works fine for JSON.
- **-delete** - Delete the document.
- **-pretty** - Format the document output for JSON. Only used with -show. Use it only if you know that the document is JSON.
- **-bucket** - The name of the bucket. Defaults to "default".
- **-host** - The host name of couchbase server. Defaults to localhost.
- **-password** - The bucket password if any.
- **-in** - The file to read a document from. Used with -set only. If not supplied, data is read from standard input.
- **-out** - The file to save a document to. Used with -get only. If not supplied, data is dumped on the standard output.


###Example
Show a JSON document with key xyz1bc123:

```
./manage-doc.sh -get -key xyz1bc123 -pretty
```

Save a document in a file called doc.json:

```
./manage-doc.sh -get -key xyz1bc123 -pretty -out doc.json
```
	
Update a document by reading from a file called doc.json:

```
./manage-doc.sh -set -key xyz1bc123 -in doc.json
```

Delete a document with the key xyz1bc123:

```
./manage-doc.sh -delete -key xyz1bc123
```

