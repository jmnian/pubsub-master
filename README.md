cd pubsub-master
javac *.java interfaces/* -d out
cd out

rmiregistry 1099 &
java -classpath ./ pubsub.EventServer -host 127.0.0.1 -p 1099

rmiregistry 1098 &
java -classpath ./ pubsub.EventServer -host 127.0.0.1 -p 1098

rmiregistry 6666 &
java -classpath ./ pubsub.EventServerMain -host 127.0.0.1 -p 6666 -cluster 1099,1098

publisher
java pubsub.PubSubClient -p 6666
1: Act as publisher

subscriber
java pubsub.PubSubClient -p 6666
2: Act as subscriber
