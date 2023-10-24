# Description

To reproduce,

compile and run jetty

```
mvn compile exec:java -Dexec.mainClass="org.example.Main"
```

verify connectivity

```
curl --unix-socket ./junix-ingress.sock http://localhost/process 
```

hammer uds endpoint to raise concurrency exception

```
seq 1 15000 | xargs -P1500 -I{} curl --unix-socket ./junix-ingress.sock http://localhost/process
```

look for the following

```
2023-10-24 13:36:12.739:WARN :oejuts.AdaptiveExecutionStrategy:etp2064801008-55: Task produce failed
java.util.ConcurrentModificationException
        at java.base/java.util.HashMap$HashIterator.nextNode(HashMap.java:1597)
        at java.base/java.util.HashMap$KeyIterator.next(HashMap.java:1620)
        at org.eclipse.jetty.io.ManagedSelector$SelectorProducer.updateKeys(ManagedSelector.java:709)
        at org.eclipse.jetty.io.ManagedSelector$SelectorProducer.produce(ManagedSelector.java:539)
        at org.eclipse.jetty.util.thread.strategy.AdaptiveExecutionStrategy.produceTask(AdaptiveExecutionStrategy.java:455)
        at org.eclipse.jetty.util.thread.strategy.AdaptiveExecutionStrategy.tryProduce(AdaptiveExecutionStrategy.java:248)
        at org.eclipse.jetty.util.thread.strategy.AdaptiveExecutionStrategy.run(AdaptiveExecutionStrategy.java:199)
        at org.eclipse.jetty.util.thread.ReservedThreadExecutor$ReservedThread.run(ReservedThreadExecutor.java:411)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
        at java.base/java.lang.Thread.run(Thread.java:833)
```

to make it easier to filter for the concurrency jetty exception

```
mvn compile exec:java -Dexec.mainClass="org.example.Main" 2>&1 | grep ConcurrentModificationException -A 15 -B 2
```
