package rs.elfak.findpet.Enums;

public enum FragmentName {
    Dashboard(0),
    Maps(1),
    Messages(2),
    User(3);

    private final int value;
    private FragmentName(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

