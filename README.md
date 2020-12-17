# DB User Password Change 
## Overview
The tool is designed to automate DB user password change.
If you have number of DBs/users that need password changing the tool will surely save your time.
## Usage
Before running make sure Java Runtime Environment (JRE) or JDK is installed on your machine.
Also you need to put jdbc driver corresponding to your DBMS into _libs_ folder.

To change password run one of the below commands in console/bash:
```
./start.sh testuser qwerty qwerty123
```
``` 
./start.sh testuser qwerty qwerty123 test_db
```
**Note**: use _start.cmd_ on Windows and _./start_gitbash.sh_ in git bash. 

It will connect to each DB provided and change user password.
In the first example DB names will be taken from _settings.properties_ file.
DB connection settings must be present in _settings.properties_ file.
## Build
```
gradlew clean build
```
After successful build you can find release media in 'package' folder.  
