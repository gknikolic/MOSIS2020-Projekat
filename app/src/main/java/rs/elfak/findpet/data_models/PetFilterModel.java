package rs.elfak.findpet.data_models;

import rs.elfak.findpet.Enums.CaseType;
import rs.elfak.findpet.Enums.PetType;

public class PetFilterModel {
    public String name;
    public PetType petType;
    public CaseType caseType;
    public String postKey; //for showing only one pet (exp. After ViewOnMap clicked on Dashboard Fragment)

    public PetFilterModel() {
    }

    public PetFilterModel(String name, PetType petType, CaseType caseType) {
        this.name = name;
        this.petType = petType;
        this.caseType = caseType;
    }

    public PetFilterModel(String name, PetType petType, CaseType caseType, String postKey) {
        this.name = name;
        this.petType = petType;
        this.caseType = caseType;
        this.postKey = postKey;
    }
}
