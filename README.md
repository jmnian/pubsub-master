## step 1: compile
```
javac src/pubsub/*.java src/pubsub/interfaces/*.java -d bin
cd bin
```

## step 2:
start failure detector using command
```
rmiregistry 6666 &
java pubsub.FailureDetector -p 6666
```

## step 3:
start multiple server node using command, replace xxxx with a valid port number
```
rmiregistry xxxx &
java pubsub.EventServer -p xxxx
```

## step 4:

start client to publish & subscribe
```
java pubsub.PubSubClient -p 6666
```