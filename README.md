#Webservice for Privacy Crash Cam

<p>In Germany and Austria the Data Protection Legislation forbids car drivers to use Dash Cams as evidence in case of car accidents. Dash Cams usually record faces and car tags. Tough, these are private information and should not be recorded unless one has permission to do so.</p>
<p>This project focuses on developing a solution which allows Dash Cams to be used as evividence in case of car accidents while ensuring that the requirements stated by the Data Protection Law are met. The Privacy Crash Cam Smartphone App records one minute of videomaterial when a crash occours and encrypts it. After storing the encrypted media file to the device storage the user can upload it to a server which will decrypt and anonymize the video. This will render faces and car tags unrecognizable and offer the result as mp4 download to the user. Managing and downloading the anonymized video as well as managing user accounts is done via the web interface.</p>
<p>As such, this project consists of three parts (App, Web Service and Web Interface). You can find each part in a separate git repository.</p>

##Preparation/ Installation
The following software is required for this project:
* Java (1.8.x)
* Postgres (9.6.x)
* Maven 2

If you want to run the advanced Python based anonymization you need additional Software:
* Python (3.x)
    * numphy (1.12.x)
    * opencv_python (3.x)
    * imutils

####Setup a Test-Database
```
psql
CREATE DATABASE PrivacyCrashCam;
\q
psql -d privacycrashcam -a -f src/main/resources/postgres/createTablesAndTestData.sql
```
To get a connection to the data, edit the file src/test/java/edu/kit/informatik/pcc/service/data/DatabaseManager.java to change username, password etc for your own details
