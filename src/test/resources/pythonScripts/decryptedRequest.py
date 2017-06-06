#! /usr/bin/env python
import requests

#change localhost with server
url = 'http://localhost:2222/webservice/decVideoUpload'

#get right path to video and metadata
files = {'video': open('../decVidForDecTest.mp4'), 'metadata': open('../decMetaForDecTest.json')}
r = requests.post(url, files=files)
print r.status_code
