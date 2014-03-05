package com.nworks.remote.repl

import scala.tools.nsc.interpreter._
import scala.tools.nsc.Settings
import java.io.{InputStreamReader, BufferedReader}
import java.net.{Socket, ServerSocket}

object ServerRepl {

  def start(port: Int, scalaCompilerJar: String) = {
    repl(new ServerSocket(port)).process(settings(scalaCompilerJar))
  }

  private def repl(serverSocket: ServerSocket) = {
    println()
    println(s"Waiting for connection at ${serverSocket.getLocalPort}")
    println()
    val client: Socket = serverSocket.accept()
    val _in = new BufferedReader(new InputStreamReader(client.getInputStream))

    val _out = new JPrintWriter(client.getOutputStream, true) {

      def isScalaPrompt(value: String) = value.endsWith("scala> ")

      override def write(str: String, off: Int, len: Int): Unit = {
        System.out.println(">>>>>>>> " + str)
        if(isScalaPrompt(str)) {
          super.write(str + "\n", off, len + 1); //add new line so that remote client can immediately read it using readLine
        } else {
          super.write(str, off, len);
        }
      }
    }
    new ILoop(_in, _out) {

      override def printWelcome(): Unit = {
        super.printWelcome()
        echo("Welcome to Remote REPL. Enjoy!")
      }

      override def closeInterpreter(): Unit = {
        super.closeInterpreter()
        println("Closing remote REPL")
        _in.close()
        _out.close()
        client.close()
        serverSocket.close()
      }
    }
  }

  private def settings(scalaCompilerJar: String) = {

    println(">>>>>>libs " + scalaCompilerJar)
    val settings = new Settings
    settings.Yreplsync.value = true
    //use when launching normally outside SBT
    settings.usejavacp.value = true
    settings.bootclasspath.value = scalaCompilerJar.split(";").head
    //an alternative to 'usejavacp' setting, when launching from within SBT
    //settings.embeddedDefaults[Repl.type]
    settings
  }
}
