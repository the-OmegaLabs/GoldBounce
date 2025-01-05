package net.ccbluex.liquidbounce.features.module.modules.render.appleskin;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

import java.lang.reflect.Field;

public class AppleSkinHelper {

    public static FoodValues getFoodValues(final ItemStack stack) {
        final ItemFood food = (ItemFood) stack.getItem();
        final int hunger = food != null ? food.getHealAmount(stack) : 0;
        final float saturationModifier = food != null ? food.getSaturationModifier(stack) : 0;

        return new FoodValues(hunger, saturationModifier);
    }

    public static boolean isRottenFood(final ItemStack stack) {
        if (!(stack.getItem() instanceof ItemFood)) {
            return false;
        }

        final ItemFood food = (ItemFood) stack.getItem();

        if (food.getPotionEffect(stack) != null) {
            try {
                Field potionIdField = ItemFood.class.getDeclaredField("potionId");
                potionIdField.setAccessible(true);
                int potionId = (int) potionIdField.get(food);
                return Potion.potionTypes[potionId].isBadEffect();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
