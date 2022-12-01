### Note: Please wait for about 20 seconds after running any `rmiregistry xxxx &`  command to allow RMI begin listening to the specified port so that the created process can be registered. If you ever get an error when starting the failure detector or server, wait for a few seconds and run again <br/><br/>

## step 1: compile
```
javac src/pubsub/*.java src/pubsub/interfaces/*.java -d bin
cd bin
```

## step 2:
start failure detector using command. 
```
rmiregistry 6666 &
java pubsub.FailureDetector -p 6666
```

## step 3:
start multiple server nodes using command, replace xxxx with a valid port number
```
rmiregistry xxxx &
java pubsub.EventServer -p xxxx
```

## step 4:

start client to publish & subscribe, replace xxxx with any server's port number, replace yyyy with an ID of your choice for the client
```
java pubsub.PubSubClient -p xxxx -d yyyy
```


## Example run: 
Compile project files
```
javac src/pubsub/*.java src/pubsub/interfaces/*.java -d bin
cd bin
```
Open a terminal window and start failure detector
```
rmiregistry 6666 &
java pubsub.FailureDetector -p 6666
```
Open a terminal window and start server at port 1099
```
rmiregistry 1099 &
java pubsub.EventServer -p 1099
```
Open a terminal window and start server at port 1100
```
rmiregistry 1100 &
java pubsub.EventServer -p 1100
```
Open a terminal window and start server at port 1101
```
rmiregistry 1101 &
java pubsub.EventServer -p 1101
```
Open a terminal window and start client connected to the server listening port 1099
```
java pubsub.PubSubClient -p 1099 -d 1000
```
Open a terminal window and start client connected to the server listening port 1101
```
java pubsub.PubSubClient -p 1101 -d 2000
```
Start more clients if you want. 
Run some add topic, add event, subscribe, unsubscribe operations using client's command line prompts </br>
Input "1" in failure detector to check on cluster status </br>
Now quit current leader which should be 1099, and check failure detector and client, things will still run like a charm.

