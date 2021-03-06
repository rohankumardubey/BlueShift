![Alt text](blueshift.jpg?raw=true "Flipkart Blueshift")

# Overview
Flipkart Blueshift is a reliable data migration tool for Hadoop ecosystem, Comes packed with some elegant features missing in traditional migration tools like DistCP.

- Migration across clusters (secured and unsecured)
- On the fly compression, where source data is compressed over the wire, (this can benefit only when the job is run from the source cluster)
- Inplace inflate/deflate.On successful migration data on the destination cluster can be seamlessly inflated to original state.
- Bulk migration with batches of over 10Million files. 
- Multiple Statemanager options, either HDFS or DB based statemanagers. choose the one that scales for you.
- Optimized task scheduling. Largest file first strategy
- Multiplexing large filesets to manageable worker count.
- Configuration driven, the tool can be tuned to a fine level using configurations.
- Support different protocols, HDFS, WEBHDFS, HFTP, FTP, Custom etc...
- Fault-Tolerant, Reliable, restores from point of failure.
- DB based state management, easy report generation and state management.
- Initiate the transfers from source or destination or any third cluster
- Capable of using different protocols for source and destination.
- MD5 based checksum to ensure no corruptions in data during transfer.
- Support for custom transport codecs.
- Supports Time based file filtering, which allows for incremental copy of files from one cluster to the other,
- Supports filesize based filtering, this serves multiple purposes, when we want to ignore zero sized files, or we want to ignore compression on small files, and put a cap on the large files etc..
- Option to ignore exceptions and continue processing and report state to status file and the state db.
- Support for various other filters like include/exclude file lists, include/exclude file patterns etc..

*Upcoming Features:*

- Support for other protocols like kafka, cassandra, Databases etc...
- On the fly compression irrespective of job execution location.
- Support for multiple sinks, simultaneously copy data over to kafka and hdfs from any source.


## **Prerequisites**

- Hadoop cluster running version 1.0 or higher
- Statestore, Relational database like mysql or derby etc... if Statestore is DB. for HDFS Statestore no db is required.


## **BlueShift Configuration**

Blueshift is configured via a configuration json file, There are 4 main sections that needs to be configured,

- *Batch Config* - This defines the batch properties.
- *Source Config* - This defines the Source configuration with connection and protocol details to access the source.
- *Sink Config* - This defines the destination Sink configuration with connection properties to access the destination.
- *Statestore DB Config* - This defines the database details that will be used as a statestore.

Refer to the configuration page for details on the different config options available.

https://github.com/flipkart-incubator/blueshift/wiki/Configuration


## **Use-Cases**

Following are some of the usecases and the configs to be used.

- secure to non-secure clusters - Secure to non-secure cluster transfer is unreliable with distcp. This works seamlessly.
- Cross DC data transfer of large data with limited bandwidth - Typically transfer of large dataset across DC requires the data to be compressed at source, followed by distcp and decompression at destination. This requires additional effort and space at both source and destination cluster. BlueShift supports on the fly compression at source so data is compressed while transferring in chunks. At destination, it is possible to decompress and delete source files as each file is processed hence additional space usage is minimized at destination.
- State management - While transferring large amounts of data quite often transfers are interrupted either due to source/destination cluster issues or flaky network. Since transfer state is managed in mysql DB, even if a transfer is interrupted it can be resumed.
- Timestamp based snapshots - Usually the directories being transferred are actively used by teams as well. With Blueshift, we can mention a start time and end time so files modified within that limit is transferred. This will help ensure a consistent copy of data is migrated similar to HBase Export/Copytable.
- Throttle transfer - We can set the number of parallel transfers at start so that only that many mappers are launched.


## **Download**

The tool can be downloaded from,

http://github.com/flipkart-incubator/blueshift


## **Build and Run**

Step 1: Clone the project to your local:

    git clone https://github.com/flipkart-incubator/blueshift.git

Step 2: Build the project, switch to the cloned project directory and execute:

    mvn clean install -DskipTests

Step 3: Copy the newly created jar to your gateways and run the below command:

    hadoop jar blueshift.jar -Pdriver.json

## **Contributors**

- Mukund Thakur
- Vishal Rajan
- Prathyush Babu
- Rahul Agarwal
- Kasinath
- Chetna Chaudhari
- Chackaravarthy E
- Sushil Kumar S
- Guruprasad GV
- Dhiraj Kumar
- Raj Velu - Architect
- Kurian Cheeramelil

## **Contact**

Blueshift is owned and maintained by Flipkart Data Platform Infra Team.

## **FAQ**

Refer to the FAQ page below:

https://github.com/flipkart-incubator/blueshift/wiki/FAQ
