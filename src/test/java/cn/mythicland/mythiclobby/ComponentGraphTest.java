package cn.mythicland.mythiclobby;

import cn.mythicland.framework.component.ComponentContainer;
import cn.mythicland.framework.component.ComponentResolver;
import cn.mythicland.framework.component.InjectComponent;
import cn.mythicland.mythiclobby.log.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentGraphTest {
    @Test
    void everyComponentDependencyHasOneBinding() {
        Set<Class<?>> components = new Reflections("cn.mythicland.mythiclobby")
                .getTypesAnnotatedWith(InjectComponent.class);
        List<Class<?>> bindings = new ArrayList<>(components);
        bindings.add(JavaPlugin.class);
        bindings.add(PluginLogger.class);
        bindings.add(ComponentContainer.class);
        bindings.add(ComponentResolver.class);

        for (Class<?> component : components) {
            if (component.isInterface() || Modifier.isAbstract(component.getModifiers())) continue;
            assertEquals(1, component.getDeclaredConstructors().length, component.getName());
            for (Class<?> parameter : component.getDeclaredConstructors()[0].getParameterTypes()) {
                long matches = bindings.stream().filter(parameter::isAssignableFrom).count();
                assertEquals(1, matches, component.getName() + " requires " + parameter.getName());
            }
        }
    }
}
