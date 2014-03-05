# Connecting Scala REPL to running Play application

Hands down one of the great feature of Scala language is its REPL (Read-Evaluate-Print-Loop). The REPL allows us to
explore and play with the language. Its a great language exploration tool. Some folks even use REPL to debug their
Scala programs. To debug you have to start your application from inside the Scala REPL but what about the applications
that are already running? Wouldn't be nice if we could connect Scala REPL to an already running Scala application?
This is the what I tried to do [Scala Remote REPL](https://github.com/nraychaudhuri/scala-remote-repl) project.
This project will allow you to connect to an already running Scala/Play application.The main inspiration for this
project came from the Clojure Remote REPL project called [LiveRepl](https://github.com/djpowell/liverepl)

Now let see how Scala Remote REPL works.

## How it works?

The Scala remote REPL uses [Java Attach API](http://docs.oracle.com/javase/7/docs/technotes/guides/attach/index.html) to attach to a running virtual machine. By attaching to another JVM
you can load an agent(essentially a program) that can run on a remote VM. To further understand the functionality of
how these API works please checkout the Java Attach API documentation.

I am using the agent to load the necessary jars files to an existing application's Classloader and open a server
socket for remote client REPL to connect. Let see this action.

## How to use it?

To connect remote client REPL to running Play application here is what you have to do:

- git clone https://github.com/nraychaudhuri/scala-remote-repl scala-remote-repl
- cd scala-remote-repl
- sbt publishLocal
- Create a application.conf file with following entries

  - repl.agent.path=*location of the agent jar file. Can be found under target/scala-2.10 folder*
  - repl.server.path=*location of the server jar file. Can be found under target/scala-2.10 folder*
  - repl.scala.compiler.path=*location of the scala compiler jar file. You can use sbt/show fullClasspath to find the location*
- play start (to start your play application)
- finally run the remote REPL

```sbt -Dconfig.file=myjars.conf "run <pid of the remote process> <hostname> <any open port number>"```

*Here is the a [short screencast](http://youtu.be/SKNhET81FxI) of using Scala remote repl with running Play application.*


## Known limitations

- Right now the remote REPL selects the current thread context classLoader by default. This might now be correct for
  all application.
- The :paste mode doesn't work yet
- The tab completion doesn't work yet
- Once the agent is loaded in remote JVM there is no way to unload it using Java Attach API. This means that agent class
  loaded by the system classloader of the remote VM is not unloaded after you close the REPL. In most of the cases
  this should not be an issue.  
