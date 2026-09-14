package cn.mythicland.mythiclobby.bootstrap;

import cn.mythicland.mythiclobby.MythicLobby;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

public final class RuntimeBootstrap {
    private static final String MAIN_LOADER = "cn.mythicland.mythiclobby.runtime.MainLoader";

    private final MythicLobby plugin;
    private RuntimeClassLoader classLoader;
    private Object mainLoader;
    private Method loadMethod;
    private Method enableMethod;
    private Method unloadMethod;

    public RuntimeBootstrap(MythicLobby plugin) {
        this.plugin = plugin;
    }

    public void load() {
        try {
            List<URL> urls = new ArrayList<>();
            urls.add(this.pluginJarUrl());
            urls.addAll(new RuntimeLibraryLoader(
                    this.plugin.getDataFolder().toPath(),
                    this.plugin.getClass().getClassLoader(),
                    this.plugin.getLogger()::info
            ).resolveLibraries());
            this.classLoader = new RuntimeClassLoader(
                    urls.toArray(URL[]::new),
                    this.plugin.getClass().getClassLoader()
            );
            Class<?> type = Class.forName(MAIN_LOADER, true, this.classLoader);
            this.mainLoader = type.getConstructor(MythicLobby.class).newInstance(this.plugin);
            this.loadMethod = type.getMethod("load");
            this.enableMethod = type.getMethod("enable");
            this.unloadMethod = type.getMethod("unload");
            this.invoke(this.loadMethod);
        } catch (ReflectiveOperationException exception) {
            this.closeAfterFailure(exception);
            throw new IllegalStateException("Failed to load MythicLobby runtime", exception);
        }
    }

    public void enable() {
        this.invoke(this.enableMethod);
    }

    public void disable() {
        try {
            if (this.mainLoader != null) this.invoke(this.unloadMethod);
        } finally {
            this.mainLoader = null;
            this.loadMethod = null;
            this.enableMethod = null;
            this.unloadMethod = null;
            if (this.classLoader == null) return;
            try {
                this.classLoader.close();
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("Failed to close MythicLobby runtime classloader", exception);
            } finally {
                this.classLoader = null;
            }
        }
    }

    private URL pluginJarUrl() {
        CodeSource codeSource = this.plugin.getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null || codeSource.getLocation() == null) {
            throw new IllegalStateException("Unable to resolve MythicLobby plugin JAR");
        }
        return codeSource.getLocation();
    }

    private void invoke(Method method) {
        if (this.mainLoader == null || method == null) throw new IllegalStateException("MythicLobby runtime is not loaded");
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classLoader);
            method.invoke(this.mainLoader);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot access MythicLobby runtime lifecycle", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) throw runtimeException;
            throw new IllegalStateException("MythicLobby runtime lifecycle failed", cause);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    private void closeAfterFailure(Exception failure) {
        if (this.classLoader == null) return;
        try {
            this.classLoader.close();
        } catch (java.io.IOException closeFailure) {
            failure.addSuppressed(closeFailure);
        }
        this.classLoader = null;
    }
}
