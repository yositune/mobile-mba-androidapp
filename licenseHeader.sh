#!/bin/bash

for file in `find . -name "*.java"`  
do
     echo "file = $file";
     cp $file /tmp/tmpFile;
     cat ./licenseHeaderFile > $file;
     cat /tmp/tmpFile >> $file;
done
