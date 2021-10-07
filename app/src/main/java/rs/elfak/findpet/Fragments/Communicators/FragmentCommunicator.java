package rs.elfak.findpet.Fragments.Communicators;

import rs.elfak.findpet.data_models.PetFilterModel;

public interface FragmentCommunicator {
    public void showPetOnMap(PetFilterModel filterModel); //used for communication between RecAdapter and Dashboard fragment
}
