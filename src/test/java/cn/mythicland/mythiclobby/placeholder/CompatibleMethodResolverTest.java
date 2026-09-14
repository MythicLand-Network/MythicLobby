package cn.mythicland.mythiclobby.placeholder;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompatibleMethodResolverTest {
    @Test
    void findsStaticMethodUsingAssignableArgumentType() throws Exception {
        Method method = CompatibleMethodResolver.find(
                MethodFixture.class,
                "staticValue",
                true,
                Integer.class
        );

        assertEquals("staticValue", method.getName());
    }

    @Test
    void acceptsNullForReferenceParameterAndRejectsPrimitiveParameter() throws Exception {
        Method method = CompatibleMethodResolver.findForArguments(
                MethodFixture.class,
                "nullableValue",
                false,
                new Object[]{null}
        );

        assertEquals("nullableValue", method.getName());
        assertThrows(
                NoSuchMethodException.class,
                () -> CompatibleMethodResolver.findForArguments(
                        MethodFixture.class,
                        "primitiveValue",
                        false,
                        new Object[]{null}
                )
        );
    }

    public static final class MethodFixture {
        public static void staticValue(Number value) {
        }

        public void nullableValue(String value) {
        }

        public void primitiveValue(int value) {
        }
    }
}
