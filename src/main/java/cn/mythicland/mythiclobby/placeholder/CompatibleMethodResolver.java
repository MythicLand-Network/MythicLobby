package cn.mythicland.mythiclobby.placeholder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class CompatibleMethodResolver {
    private CompatibleMethodResolver() {
    }

    static Method find(
            Class<?> type,
            String name,
            boolean requireStatic,
            Class<?>... argumentTypes
    ) throws NoSuchMethodException {
        return find(type, name, requireStatic, argumentTypes, null);
    }

    static Method findForArguments(
            Class<?> type,
            String name,
            boolean requireStatic,
            Object... arguments
    ) throws NoSuchMethodException {
        Class<?>[] argumentTypes = new Class<?>[arguments.length];
        for (int index = 0; index < arguments.length; index++) {
            Object argument = arguments[index];
            argumentTypes[index] = argument == null ? Object.class : argument.getClass();
        }
        return find(type, name, requireStatic, argumentTypes, arguments);
    }

    private static Method find(
            Class<?> type,
            String name,
            boolean requireStatic,
            Class<?>[] argumentTypes,
            Object[] arguments
    ) throws NoSuchMethodException {
        for (Method method : type.getMethods()) {
            if (!matches(method, name, requireStatic, argumentTypes, arguments)) continue;
            method.setAccessible(true);
            return method;
        }
        throw new NoSuchMethodException(type.getName() + "#" + name);
    }

    private static boolean matches(
            Method method,
            String name,
            boolean requireStatic,
            Class<?>[] argumentTypes,
            Object[] arguments
    ) {
        if (!method.getName().equals(name)) return false;
        if (Modifier.isStatic(method.getModifiers()) != requireStatic) return false;

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != argumentTypes.length) return false;

        for (int index = 0; index < parameterTypes.length; index++) {
            if (arguments != null && arguments[index] == null) {
                if (parameterTypes[index].isPrimitive()) return false;
                continue;
            }
            if (!wrap(parameterTypes[index]).isAssignableFrom(wrap(argumentTypes[index]))) return false;
        }
        return true;
    }

    private static Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == double.class) return Double.class;
        if (type == float.class) return Float.class;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        return type;
    }
}
