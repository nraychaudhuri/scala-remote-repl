package com.nworks.remote.repl

import java.io.{InputStreamReader, BufferedReader, PrintWriter}
import java.net.Socket
import scala.annotation.tailrec
import java.net.Socket
import java.io.{InputStreamReader, BufferedReader, PrintWriter}
import scala.tools.jline.console.ConsoleReader
import scala.tools.jline._


object ClientRepl {

  def connectToRemote(hostName: String, port: Int): Unit = {
    print(s"Connect to ${hostName}:${port}?[Hit enter for yes]")
    System.in.read() //delay to make sure the remote repl is ready to accept connection
    val socket = new Socket(hostName, port)

    val out = new PrintWriter(socket.getOutputStream(), true)
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream()))

    val terminal = TerminalFactory.create()
    val stdIn = new ConsoleReader(System.in, new PrintWriter(System.out, true), terminal)
    stdIn.setPrompt("scala> ")

    @tailrec def repl(): Unit = {
      handleReplOutput(in)
      val cmd = command(stdIn)
      cmd.foreach(out.println)
      if(!quitCommand(cmd)) repl()
    }
    println("Connected to remote REPL. Fire up your commands")

    try {
      repl()
    } finally {
      println("Remote REPL is closed...")
      in.close()
      out.close()
      socket.close()
      terminal.restore()
    }
  }


  def quitCommand(cmd: Option[String]): Boolean = cmd.exists(_ == ":q")

  def command(stdIn: ConsoleReader): Option[String] = Option(stdIn.readLine())

  def handleReplOutput(reader: BufferedReader) =  {

    def isScalaPrompt(value: String) = value.endsWith("scala> ")


    def replOutput(output: Option[String]):Unit = output.foreach { result =>
      if(!isScalaPrompt(result)) {
        println(result)
        replOutput(Option(reader.readLine()))
      }
    }
    replOutput(Option(reader.readLine()))
  }


}
