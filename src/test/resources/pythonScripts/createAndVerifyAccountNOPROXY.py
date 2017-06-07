#! /usr/bin/env python
import requests

proxies = { "http": None, "https": None, }

#change localhost with server
url = 'http://localhost:2222/webservice/createAccount'

#get right path to video and metadata
body = {'account': 'not relevant', 'uuid': '3456qwe-qw234-23429877aksd'}
r = requests.post(url, data=body, proxies=proxies)
print 'Status-Code(CreateAccount): '+str(r.status_code)
print r.content
url = 'http://localhost:2222/webservice/verifyAccount?uuid=3456qwe-qw234-23429877aksd'  
r = requests.get(url, proxies=proxies)
print 'Status-Code(VerifyAccount): '+str(r.status_code)
