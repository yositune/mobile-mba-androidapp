#!/bin/bash

for file in `find . -name "*.java"`  
do
     echo "file = $file";

     if grep -q 'Apache' $file 
     then
            echo "Apache License found. Not editing."
     else
            cp $file /tmp/tmpFile;
            cat ./licenseHeaderFile > $file;
            cat /tmp/tmpFile >> $file;
     fi
done
