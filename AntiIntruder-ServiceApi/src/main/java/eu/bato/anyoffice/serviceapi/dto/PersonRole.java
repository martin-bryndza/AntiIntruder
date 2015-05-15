package eu.bato.anyoffice.serviceapi.dto;

/**
 *
 * @author Bato
 */
public enum PersonRole {

    USER,
    ADMIN;

    private PersonRole() {
    }

    public static PersonRole getPersonRole(int index) {
        switch (index) {
            case 1:
                return ADMIN;
            default:
                return USER;
        }
    }

    public static int getIndex(PersonRole personRole) {
        switch (personRole) {
            case ADMIN:
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public String toString() {
        return this.name();
    }
}
