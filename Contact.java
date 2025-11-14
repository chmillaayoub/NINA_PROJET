public class Contact {
    
    private final String nom;
    private final String telephone;
    private final String email;

    public Contact(String nom, String telephone, String email) {
        this.nom = nom;
        this.telephone = telephone;
        this.email = email;
    }

    public String getNom() {
        return nom;
    }

    @Override
    public String toString() {
        return nom + " | Tel: " + telephone + " | Email: " + email;
    }
}