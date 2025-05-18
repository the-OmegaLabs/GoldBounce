package net.ccbluex.liquidbounce.utils.mobends.client.model.entity;

public class ModelBendsZombieVillager
extends ModelBendsZombie {
    public ModelBendsZombieVillager() {
        this(0.0f, 0.0f, false);
    }

    public ModelBendsZombieVillager(float p_i1167_1_, float p_i1167_2_, boolean p_i1165_3_) {
        super(p_i1167_1_, 0.0f, 64, p_i1165_3_ ? 32 : 64);
    }
}

