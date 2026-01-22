package me.wbot3000;

public enum ABILITY_FLAGS {
    RAGE(1, "rage", "Rage"),
    BULKY_I(1 << 1, "bulkyi", "Bulky I"),
    HEAVY_WEAPONS_GUY_I(1 << 2, "hwgi", "Heavy Weapons Guy I"),
    BLOODBATH_I(1 << 3, "bloodbathi", "Bloodbath I");

    final private long long_flag;
    final private String ability_shorthand;
    final private String ability_name;

    private ABILITY_FLAGS(long _long_flag, String _ability_shorthand, String _ability_name) {
        this.long_flag = _long_flag;
        this.ability_shorthand = _ability_shorthand;
        this.ability_name = _ability_name;
    }

    public long enumToLong() {
        return this.long_flag;
    }
    public String getAbilityShorthand() { return ability_shorthand; }
    public String getAbilityName() { return ability_name; }

    static ABILITY_FLAGS longToEnum(long _value) {
        for (ABILITY_FLAGS flag: ABILITY_FLAGS.values()) {
            if (flag.long_flag == _value) {
                return flag;
            }
        }
        return null;
    }

}
