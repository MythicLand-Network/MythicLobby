package cn.mythicland.mythiclobby.bootstrap;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

final class RuntimeClassLoader extends URLClassLoader {
    private static final List<String> CHILD_FIRST_PACKAGES = List.of(
            "cn.mythicland.mythiclobby.",
            "cn.mythicland.framework.",
            "cn.mythicland.npclib.",
            "org.reflections.",
            "javassist.",
            "fr.mrmicky.fastboard.",
            "it.unimi.dsi.fastutil."
    );

    static {
        registerAsParallelCapable();
    }

    RuntimeClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null && shouldUseChildFirst(name)) {
                try {
                    loaded = findClass(name);
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (loaded == null) loaded = super.loadClass(name, false);
            if (resolve) resolveClass(loaded);
            return loaded;
        }
    }

    private static boolean shouldUseChildFirst(String className) {
        if (className.equals("cn.mythicland.mythiclobby.MythicLobby")) return false;
        if (className.startsWith("cn.mythicland.mythiclobby.bootstrap.")) return false;
        for (String packageName : CHILD_FIRST_PACKAGES) {
            if (className.startsWith(packageName)) return true;
        }
        return false;
    }
}
