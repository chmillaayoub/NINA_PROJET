import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class AnnuaireServer {
    
    private static final int PORT = 12345; 

    // 1. 🗺️ L'Annuaire (Stockage des Contacts) - BQA KIMA HOWA
    public static final Map<String, Contact> annuaire = new ConcurrentHashMap<>();

    // 2. 🔗 Le Registre des Clients Actifs (Nécessaire pour la Messagerie)
    // --- TGHAYIR (CHANGE) ---
    // Mabqach 'Object', wela 'ClientHandler'
    public static final Map<String, ClientHandler> activeClientHandlers = new ConcurrentHashMap<>();
    
    
    // --- LOGIQUE ADD (Membre 2) - BQA KIMA HOWA ---
    public static String handleAddCommand(String[] parts) {
        if (parts.length != 4) {
            return "ERROR: Syntax ADD invalide. Utilisation: ADD Nom Tel Email";
        }
        
        String nom = parts[1];
        String telephone = parts[2];
        String email = parts[3];
        
        if (annuaire.containsKey(nom)) {
            return "ERROR: Le contact " + nom + " existe déjà dans l'annuaire.";
        } else {
            Contact nouveauContact = new Contact(nom, telephone, email);
            annuaire.put(nom, nouveauContact);
            return "OK: Le contact " + nom + " a été ajouté avec succès.";
        }
    }

    // --- LOGIQUE LIST (Membre 2) - BQA KIMA HOWA ---
    public static String handleListCommand() {
        if (annuaire.isEmpty()) {
            return "INFO: L'annuaire est actuellement vide.";
        }

        StringBuilder listResult = new StringBuilder();
        listResult.append("--- Liste des Contacts ---\n");
        
        for (Contact contact : annuaire.values()) {
            listResult.append(contact.toString()).append("\n");
        }
        listResult.append("--------------------------");
        
        return listResult.toString();
    }
    
    // --- Squelette TCP principal (Membre 2 - Tâche 1) ---
    public static void main(String[] args) {
        System.out.println("🚀 Serveur (Annuaire + Messagerie) démarré sur le port " + PORT + "...");
        
        // --- TGHAYIR (CHANGE) ---
        // Ghadi nkhdmo b l-Multi-threading
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            
            // Boucle principale d'écoute
            while (true) {
                // serverSocket.accept() katbqa tsna client jdid
                Socket clientSocket = serverSocket.accept(); 
                System.out.println("\n✅ Nouveau client connecté: " + clientSocket.getInetAddress());

                // *********************************************************************
                // Hna khdmt "Membre 3" (Multi-Thread)
                // Kanchdo l-client jdid o kan3tiwh l-Thread dyalo (ClientHandler)
                
                ClientHandler clientThread = new ClientHandler(clientSocket);
                clientThread.start(); // <-- Hada howa l-point l-mohim
                
                // --- SQUELETTE SIMPLIFIÉ (Non Multi-Threadé) MCHA ---
                // handleSingleRequest(clientSocket); <-- HADI T7YDAT
                // -----------------------------------------------------------------
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur critique du serveur: " + e.getMessage());
        }
    }
    
    // --- TGHAYIR (CHANGE) ---
    // L-fonction 'handleSingleRequest' t7ydat.
    // L-Logic dyalha mcha l-ClientHandler.java
}