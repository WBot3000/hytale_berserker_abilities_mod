package me.wbot3000;

public class AbilityData {
    public final ABILITY_FLAGS FLAG;
    public final String NAME_REF;
    public final String DESC_REF;

    public AbilityData(ABILITY_FLAGS _flag, String _name_ref, String _desc_ref) {
        FLAG = _flag;
        NAME_REF = _name_ref;
        DESC_REF = _desc_ref;
    }
}
