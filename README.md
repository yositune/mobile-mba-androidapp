## mobile-mba-androidapp - General Build Public Release - v1.80

2013 Measuring Broadband America Program Mobile Measurement Android Application

## About

**This Android application source code is made available under the GNU GPL2 for testing purposes only and intended for participants in the SamKnows/FCC Measuring Broadband American program.  It is not intended for general release and this repository may be disabled at any time.**


## Measuring Broadband America (MBA) Program's Mobile Measurement Effort

The FCC Measuring Broadband America (MBA) Program's Mobile Measurement Effort developed in cooperation with SamKnows Ltd. and diverse stakeholders employs an client-server based anonymized data collection approach to gather broadband performance data in an open and transparent manner with the highest commitment to protecting participants privacy.  All data collected is thoroughly analyzed and processed prior to public release to ensure that subscribers’ privacy interests are protected.

Data related to the radio characteristics of the handset, information about the handset type and operating system (OS) version, the GPS coordinates available from the handset at the time each test is run, the date and time of the observation, and the results of active test results are recorded on the handset in JSON(JavaScript Object Notation) nested data elements within flat files.  These JSON files are then transmitted to storage servers at periodic intervals after the completion of active test measurements.

============================================

Build Explanations:

    gb  General Build: This build is the baseline application.
    cb  Carrier Build: This build includes an option to add a self-identifier for debugging and testing purposes.
    mt  Manual Test: This build turns off automatic testing by default. Users of this build can turn the automatic testing on themselves.
    rc  Release Candidate: This build is a public release candidate and a branch of the General Build source tree.
    
## About Data Collected

For more about the data collected see:
- [Data Representation and Data Dictionary](https://github.com/FCC/mobile-mba-androidapp/wiki/Data-Representation)
- [Application Privacy Notice and Terms of Service](https://github.com/FCC/mobile-mba-androidapp/wiki/Full-Text-of-Application-Privacy-Notice-and-Terms-and-Conditions)
