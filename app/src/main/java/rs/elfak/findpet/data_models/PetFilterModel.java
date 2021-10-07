package rs.elfak.findpet.data_models;

import rs.elfak.findpet.Enums.CaseType;
import rs.elfak.findpet.Enums.PetType;

public class PetFilterModel {
    public String name;
    public PetType petType;
    public CaseType caseType;

    public PetFilterModel() {
    }

    public PetFilterModel(String name, PetType petType, CaseType caseType) {
        this.name = name;
        this.petType = petType;
        this.caseType = caseType;
    }
}
