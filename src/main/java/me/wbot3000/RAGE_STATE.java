package me.wbot3000;

public enum RAGE_STATE {
    READY(0),
    ACTIVE(1),
    COOLDOWN(2);

    final private int int_state; //Needed for codec

    private RAGE_STATE(int _state) {
        int_state = _state;
    }

    public int stateToInt() {
        return int_state;
    }

    public static RAGE_STATE intToState(int _value) {
        for (RAGE_STATE state: RAGE_STATE.values()) {
            if (state.int_state == _value) {
                return state;
            }
        }
        return null;
    }
}
