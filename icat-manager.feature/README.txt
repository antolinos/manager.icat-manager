ICAT Manager is an standalone Eclipse Rich Client for visualizing and managing ICAT web services. It works for any version of ICAT starting from version 4.2.0

Installation:
Unzip/untar in a newly created folder. On macos and linux, you might need to set the executable bit manually on the launcher ('chmod u+x IcatManager' on linux).

Launching:
ICAT Manager needs a JDK to run (as it generates the client classes on-the-fly), in order to do so you need:
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
