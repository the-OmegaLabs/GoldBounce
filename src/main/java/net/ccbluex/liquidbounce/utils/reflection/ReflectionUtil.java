package net.ccbluex.liquidbounce.utils.reflection;

import kotlin.jvm.internal.Intrinsics;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReflectionUtil {
	@NotNull
	public static final ReflectionUtil INSTANCE = new ReflectionUtil();
	@NotNull
	private static final Map<AbstractMap.SimpleEntry<?,?>, Field> fieldCache =
			new LinkedHashMap<>();

	private ReflectionUtil () {
	}

	@SneakyThrows
	@NotNull
	public static final Field getField (
			@NotNull Class<?> clazz,
			@NotNull String fieldName
	                                   ) {

		// Create cache key using a Pair (or TuplesKt.to() if you prefer)
		AbstractMap.SimpleEntry key = new AbstractMap.SimpleEntry<>(
				clazz,
				fieldName
		);

		// Try to get cached field
		Field field = fieldCache.get(key);

		if (field == null) {
			try {
				field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				fieldCache.put(key, field); // Cache it for future use
			} catch (NoSuchFieldException e) {
				throw new RuntimeException("Field not found: " + fieldName, e);
			}
		}

		return field;
	}

	public static <T> T getFieldValue (
			@NotNull Object instance,
			@NotNull String fieldName
	                                  ) throws
			IllegalAccessException {
		Field field = getField(instance.getClass(), fieldName);
		return (T) field.get(instance);
	}

	public static final void setFieldValue (
			@NotNull Object instance,
			@NotNull String fieldName, @Nullable Object value
	                                       ) throws
			IllegalAccessException {
		Field field = getField(instance.getClass(), fieldName);
		field.set(instance, value);
	}
}
