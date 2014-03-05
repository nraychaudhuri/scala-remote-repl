package com.nworks.remote.agent;


import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class AgentMain {

    public static void agentmain(String agentArgs, Instrumentation inst) {
       System.out.println("Running agent in remote jvm");
       //expecting agentArgs as following: port=...,libs=....
       String[] args = agentArgs.split(",");
       int port = Integer.parseInt(args[0].split("=")[1]);
       String libs = args[1].split("=")[1];

       startReplAsync(port, libs, inst);
    }

    private static void startReplAsync(final int port, final String libs, final Instrumentation inst) {
        final ClassLoader oldClassLoader = findAppropriateClassLoader(inst);
        Thread replThread = new Thread("remote-repl-thread") {
            @Override
            public void run() {
                ClassLoader withScalaRepl = addReplToClasspath(libs, oldClassLoader);
                try {
                    startRepl(port, libs, withScalaRepl, inst);
                } finally {
                    //resetting back to old classloader
                    System.out.println("Resetting the classloader...");
                    Thread.currentThread().setContextClassLoader(oldClassLoader);
                }
            }
        };
        replThread.start();
    }

    private static ClassLoader addReplToClasspath(String libs, ClassLoader classLoader) {
        String[] paths = libs.split(";");
        List<URL> urls = getJarUrls(paths);
        URLClassLoader withScalaRepl = new URLClassLoader(urls.toArray(new URL[urls.size()]), classLoader);
        Thread.currentThread().setContextClassLoader(withScalaRepl);
        return withScalaRepl;
    }

    private static List<URL> getJarUrls(String[] paths) {
        List<URL> urls = new ArrayList<URL>();
        try {
            for (String path : paths) {
                URL url = new File(path).toURI().toURL();
                urls.add(url);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return urls;
    }

    private static ClassLoader findAppropriateClassLoader(Instrumentation inst) {
         //TODO fix this to findClassLoader based on class or thread id
         ClassLoader cl = ClassLoaderFinder.find(AgentMain.class);
         System.out.println("Selected classloader " + cl);
         return cl;
    }

    private static void startRepl(int port, String libs, ClassLoader cl, Instrumentation inst) {
        try {
            Class repl = Class.forName("com.nworks.remote.repl.ServerRepl", true, cl);
            Method method = repl.getDeclaredMethod("start", int.class, String.class);
            method.invoke(null, port, libs);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


