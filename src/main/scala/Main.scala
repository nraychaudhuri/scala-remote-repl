package com.nworks.remote.repl

import com.sun.tools.attach.VirtualMachine

import scala.collection.JavaConverters._
import com.typesafe.config._

object Main {

  def main(args: Array[String]): Unit = {
     if(args.isEmpty) {
       println("Printing all the running VMs...")
       VirtualMachine.list().asScala.foreach(println)
     } else {
       val pid = args(0)
       val host = args(1)
       val port = args(2).toInt
       val vm = attachToVm(pid, port)
       ClientRepl.connectToRemote(host, port)
       vm.detach()
       println("Agent is detached...")
     }
  }

  private def attachToVm(pid: String, port: Int) = {
    val vm = VirtualMachine.attach(pid)
    val config = ConfigFactory.load()
    val agentPath = config.getString("repl.agent.path")
    val scalaCompilerPath = config.getString("repl.scala.compiler.path")
    val serverPath = config.getString("repl.server.path")
    val agentArgs = s"port=${port},compilerPath=${scalaCompilerPath},serverPath=${serverPath}"
    vm.loadAgent(agentPath, agentArgs)
    vm
  }

}

