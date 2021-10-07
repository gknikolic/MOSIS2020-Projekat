package rs.elfak.findpet.data_models;

import rs.elfak.findpet.Enums.CaseType;
import rs.elfak.findpet.Enums.PetType;

public class PetFilterModel {
    public String name;
    public PetType petType;
    public CaseType caseType;
    public int radius;
    public String postKey; //for showing only one pet (exp. After ViewOnMap clicked on Dashboard Fragment)
    public String userKey; //for calculating radius


    public PetFilterModel() {
        name = null;
        petType = null;
        caseType = null;
        radius = -1;
        postKey = null;
    }

    public PetFilterModel(String name, PetType petType, CaseType caseType, int radius, String userKey) { //for filtering when some values are entered
        this.name = name;
        this.petType = petType;
        this.caseType = caseType;
        this.radius = radius;
        this.userKey = userKey;
    }

    public PetFilterModel(String postKey) { //for showing only one pet (exp. After ViewOnMap clicked on Dashboard Fragment)
        this.postKey = postKey;
    }
}
