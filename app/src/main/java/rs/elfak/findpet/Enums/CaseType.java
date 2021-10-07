package rs.elfak.findpet.Enums;

public enum CaseType {
    Lost(0) {
        @Override
        public String toString() {
            return "Lost";
        }
    },
    Gift(1){
        @Override
        public String toString() {
            return "Gift";
        }
    },
    Selling(2){
        @Override
        public String toString() {
            return "Selling";
        }
    };

    private final int value;
    private CaseType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
