## step 1: compile
```
javac src/pubsub/*.java src/pubsub/interfaces/*.java -d bin
cd bin
```

## step 2:
start multiple server node using command
```
rmiregistry 1099 &
java pubsub.EventServer -p 1099
```

## step 3:

start client to publish & subscribe 
```
java pubsub.PubSubClient -p 1099
```