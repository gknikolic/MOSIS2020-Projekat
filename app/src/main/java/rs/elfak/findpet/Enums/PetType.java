package rs.elfak.findpet.Enums;

public enum PetType {
    Dog(0){
        @Override
        public String toString() {
            return "Dog";
        }
    },
    Cat(1){
        @Override
        public String toString() {
            return "Cat";
        }
    };

    private final int value;
    private PetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
