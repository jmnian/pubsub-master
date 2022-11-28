## step 1: compile
```
javac *.java interfaces/* -d bin
cd bin
```

## step 2:
start node 1 at port 1099
```
rmiregistry 1099 &
java -classpath ./ pubsub.EventServer -p 1099
```

start node 2 at port 1098
```
rmiregistry 1098 &
java -classpath ./ pubsub.EventServer -p 1098
```

start sentinel at port 6666
EventServerMain as sentinel to monitor cluster(1099,1098), it will choose max number as leader
if 1099 goes down, 1098 will be promoted to be leader
```
rmiregistry 6666 &
java -classpath ./ pubsub.EventServerMain -p 6666 -cluster 1099,1098
```

start publisher to connect to cluster
```
java pubsub.PubSubClient -p 6666
```
choose choice 1 to act as publisher
1: Act as publisher

start subscriber to connect to cluster
```
java pubsub.PubSubClient -p 6666
```
choose choice 2 to act as subscriber
2: Act as subscriber