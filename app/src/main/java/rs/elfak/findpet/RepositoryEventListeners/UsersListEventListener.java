package rs.elfak.findpet.RepositoryEventListeners;

public interface UsersListEventListener {
    void OnUsersListUpdated();
    void CurrentUserLoaded();
    void OnUserLocationChanged(String userKey);
}
