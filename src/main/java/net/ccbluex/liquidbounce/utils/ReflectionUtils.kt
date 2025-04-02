package net.ccbluex.liquidbounce.utils

import java.lang.reflect.Field

object ReflectionUtil {
    private val fieldCache = mutableMapOf<Pair<Class<*>, String>, Field>()

    fun getField(clazz: Class<*>, fieldName: String): Field {
        return fieldCache.getOrPut(clazz to fieldName) {
            val field = clazz.getDeclaredField(fieldName).apply {
                isAccessible = true
            }
            field
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getFieldValue(instance: Any, fieldName: String): T {
        val field = getField(instance.javaClass, fieldName)
        return field.get(instance) as T
    }

    fun setFieldValue(instance: Any, fieldName: String, value: Any?) {
        val field = getField(instance.javaClass, fieldName)
        field.set(instance, value)
    }
}
