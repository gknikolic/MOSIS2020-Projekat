package rs.elfak.findpet.Enums;

public enum FragmentName {
    Dashboard(0),
    Friends(1),
    Pets(2),
    Messages(3),
    User(4);

    private final int value;
    private FragmentName(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

