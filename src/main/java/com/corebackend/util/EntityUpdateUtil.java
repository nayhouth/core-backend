package com.corebackend.util;

import com.corebackend.entity.BaseEntity;

import java.lang.reflect.Field;

public final class EntityUpdateUtil {

    private EntityUpdateUtil() {
    }

    public static void copyFields(BaseEntity source, BaseEntity target) {

        Class<?> clazz = source.getClass();

        while (clazz != null && clazz != BaseEntity.class) {

            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {

                try {
                    field.setAccessible(true);

                    Object value = field.get(source);
                    field.set(target, value);

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }
}