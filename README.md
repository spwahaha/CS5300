## CS5300 Project
> Shunchang Bai (sb2345), Zhenchuan Pang (zp55), Zhongru Wu (zw377)

### Project 1b
The index url is /Project1 where there is a link to direct you to the servlet.

There are 2 files in session package of our source directory:
- Manager.java: the servlet code. Handles server requests, creates and stores session state data, 
executes RPC calls when necessary, spawns daemon threads and returns cookies and the servlet web-page 
to the client.
- Session.java: defines the session state objects that will be stored in the hash maps.

Our Cookie format: SessID_version_SvrID1_SvrID2_..._SvrIDWQ

### Project 2: PageRank

#### 0. Overall Structures

1. **Preprocess**: we need to use our netID to create our own input edge files for each different implementation before MapReduce
2. **PageRank**: this is the main part of the project. It starts a MapReduce job for each pass. These MapReduce jobs will then call the Map function and Reduce function.
3. **Map**: Map function will parse the input data and transfer the output to Reduce.
4. **Reduce**: Reduce function will calculate the new PageRank value and other information (i.e. highest rank node in each block) and produce output to Map for next pass.
5. **Counter**: This is a Hadoop counter. It will transfer the results from Reduce to our main part PageRank. In block implementation, we also create blockCounter in each block so that we can get the highest rank node in each block.

#### 1. Input Data Preprocess

##### 1.1 Filter Parameter

##### 1.2 Data Format 

####  2. Simple Computation of PageRank

##### 2.1 MapReduce Task

##### 2.2 Data Format

##### 2.3 Map

##### 2.4 Reduce

##### 2.5 Calculate the Residual

##### 2.6 Result

#### 3. Blocked Computation of PageRank

##### 3.1 Data Format

##### 3.2 Map

##### 3.3 Reduce

##### 3.4 Result

#### 4. Extra Credit

##### 4.1 Gauss-Seidel Iteration

##### 4.2 Random Block Partition

#### 5. Running our Code

