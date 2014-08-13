ICAT Manager is an Eclipse Rich Client for visualizing and managing ICAT web services. It works for any version of ICAT starting from version 4.3.0.

ICAT Manager needs a JDK to run (as it generates the client classes on-the-fly), in order to do so you need:
* On linux: set the JAVA_HOME environment variable to point to a JDK
* On Windows: execute IcatManager -vm "Path_to_jdk\bin\javaw.exe"

If the ICAT you want to connect to is behind a proxy, add the following lines at the end of the IcatManager.ini file:
-Dhttp.proxyHost=proxy.url
-Dhttp.proxyPort=1234
-Dhttps.proxyHost=proxy.url
-Dhttps.proxyPort=1234
-Dhttp.nonProxyHosts=localhost|*.local

