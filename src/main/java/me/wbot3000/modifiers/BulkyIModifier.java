package me.wbot3000.modifiers;

import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;

public class BulkyIModifier extends StaticModifier {
    public BulkyIModifier() {
        calculationType = CalculationType.ADDITIVE; //Adding a flat amount to health
        amount = 50;
    }
}