ICAT Manager is a standalone client for visualizing and managing ICAT web services. It is based on Eclipse RCP but does not require eclipse to run.
It works for any version of ICAT starting from version 4.2.0 thanks to a JAXWS dynamic client.

Installation:
Unzip/untar in a newly created folder. On macos and linux, you might need to set the executable bit manually on the launcher ('chmod u+x IcatManager' on linux).

Launching:
ICAT Manager needs a JDK to run (as it generates the client classes on-the-fly), in order to do so you need to:
* On linux: set the JAVA_HOME environment variable to point to a JDK
* On Windows: execute IcatManager.exe -vm "Path_to_jdk\bin\javaw.exe"

Proxy settings:
If the ICAT you want to connect to is behind a proxy, add the following lines at the end of the IcatManager.ini file:
-Dhttp.proxyHost=proxy.url
-Dhttp.proxyPort=1234
-Dhttps.proxyHost=proxy.url
-Dhttps.proxyPort=1234
-Dhttp.nonProxyHosts=localhost|*.local

Features:
* Independent of the ICAT version (starting from 4.2.0)
* Allows display and editing of all entities (based on your access rights)
* Configurable pagination. Sorting and filtering entities on any field.

Release notes:
* The 'Filter' text field accepts any query in the concise syntax form (the part between [ ])  
* By default, sorting entities by an association field uses the associated entity id. To sort by entity name, use the toggle button 'Sort by Name'.
* For the moment it is not possible to edit association fields directly in the table, you have to use the 'Edit entity' command.
* In the 'Edit entity' dialog, association fields are displayed using special drop down lists:
   - they display a maximum of 50 items in addition to the previously selected item (if any).
   - the previously selected item (if any) is always at the top of the list, the other items are sorted by descending ids (so latest ones first).
   - the list can be filtered by typing a part of the entity name. Note that the search is case sensitive.
   - if the associated entity has a fullName field it is also used in the search. If the entity does not have a name you can type its full id.  
   