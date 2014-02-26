package com.nworks.remote.repl

import com.sun.tools.attach.VirtualMachine

import scala.collection.JavaConverters._
import java.net.Socket
import java.io.{InputStreamReader, BufferedReader, PrintWriter}
import scala.tools.jline.console.ConsoleReader
import com.typesafe.config._

object Main {

  def connectToRepl(hostName: String, port: Int): Unit = {
    print(s"Connect to ${hostName}:${port}?")
    System.in.read() //delay to make sure the remote repl is ready to accept connection
    val socket = new Socket(hostName, port)

    val out = new PrintWriter(socket.getOutputStream(), true)
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream()))

    val stdIn = new ConsoleReader(System.in, new PrintWriter(System.out, true))

    println("Connected to remote REPL. Fire up your commands")
    var shouldContinue = true
    try {
      while(shouldContinue) {
        handleReplOutput(in)
        val cmd = command(stdIn)
        cmd.foreach(out.println)
        shouldContinue = !quitCommand(cmd)
      }
    } finally {
      println("Remote REPL is closed...")
      in.close()
      out.close()
      socket.close()
    }
  }


  def quitCommand(cmd: Option[String]): Boolean = cmd.exists(_ == ":q")

  def command(stdIn: ConsoleReader): Option[String] = Option(stdIn.readLine())

  def handleReplOutput(reader: BufferedReader) =  {

    def isScalaPrompt(value: String) = value.endsWith("scala> ")

    def replOutput(output: Option[String]):Unit = output.foreach { result =>
      if(isScalaPrompt(result)) {
        print(result)
      } else {
        println(result)
        replOutput(Option(reader.readLine()))
      }
    }
    replOutput(Option(reader.readLine()))
  }


  def main(args: Array[String]): Unit = {
     if(args.isEmpty) {
       println("Printing all the running VMs...")
       VirtualMachine.list().asScala.foreach(println)
     } else {
       val pid = args(0)
       val host = args(1)
       val port = args(2).toInt
       attachToVm(pid, port)
       connectToRepl(host, port)
     }
  }

  private def attachToVm(pid: String, port: Int): Unit = {
    val vm = VirtualMachine.attach(pid)
    val config = ConfigFactory.load()
    val agentPath = config.getString("repl.agent.path")
    val scalaCompilerPath = config.getString("repl.scala.compiler.path")
    val serverPath = config.getString("repl.server.path")
    //val agentPath = "/Users/nraychaudhuri/projects/remote-repl/agent/target/scala-2.10/agent_2.10-0.1-SNAPSHOT.jar"
    //val libs = "/Users/nraychaudhuri/.ivy2/cache/org.scala-lang/scala-compiler/jars/scala-compiler-2.10.2.jar;/Users/nraychaudhuri/projects/remote-repl/target/scala-2.10/remote-repl_2.10-0.1-SNAPSHOT.jar"
    val libs = s"${scalaCompilerPath};${serverPath}"
    val agentArgs = s"port=${port},libs=${libs}"
    vm.loadAgent(agentPath, agentArgs)
  }

}

