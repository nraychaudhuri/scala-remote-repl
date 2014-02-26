package com.nworks.remote.agent;

public class ClassLoaderFinder {

    public static ClassLoader find(Class callerClass) {
        final ClassLoader callerLoader = callerClass.getClassLoader();
        final ClassLoader contextLoader = Thread.currentThread ().getContextClassLoader ();
        ClassLoader result;

        //parent-child relationship
        if (isChild (contextLoader, callerLoader))
            result = callerLoader;
        else {
            result = contextLoader;
        }

        final ClassLoader systemLoader = ClassLoader.getSystemClassLoader ();
        // precaution for when deployed as a bootstrap or extension class:
        if (isChild (result, systemLoader)){
            result = systemLoader;
        }
        return result;
    }

    private static boolean isChild (final ClassLoader loader1, ClassLoader loader2)
    {
        if (loader1 == loader2) return true;
        if (loader2 == null) return false;
        if (loader1 == null) return true;

        for ( ; loader2 != null; loader2 = loader2.getParent ())
        {
            if (loader2 == loader1) return true;
        }

        return false;
    }

}
