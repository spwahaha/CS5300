# CS5300 Project 1b
> Shunchang Bai (sb2345), Zhenchuan Pang (zp55), Zhongru Wu (zw377)

The index url is /Project1 where there is a link to direct you to the servlet.

There are 2 files in session package of our source directory:
- Manager.java: the servlet code. Handles server requests, creates and stores session state data, 
executes RPC calls when necessary, spawns daemon threads and returns cookies and the servlet web-page 
to the client.
- Session.java: defines the session state objects that will be stored in the hash maps.

Our Cookie format: SessID_version_SvrID1_SvrID2_..._SvrIDWQ