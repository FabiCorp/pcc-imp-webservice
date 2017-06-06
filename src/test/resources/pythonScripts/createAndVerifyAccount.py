#! /usr/bin/env python
import requests

#change localhost with server
url = 'http://localhost:2222/webservice/createAccount'

#get right path to video and metadata
body = {'account': 'not relevant', 'uuid': '3456qwe-qw234-23429877aksd'}
r = requests.post(url, data=body)
print 'Status-Code(CreateAccount): '+str(r.status_code)

url = 'http://localhost:2222/webservice/verifyAccount?uuid=3456qwe-qw234-23429877aksd'  
r = requests.get(url)
print 'Status-Code(VerifyAccount): '+str(r.status_code)
