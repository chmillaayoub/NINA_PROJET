import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName = null; // L-Nom dyal had l-client (mn b3d LOGIN)

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            // 1. Setup dyal l-communication
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // 2. TSKBILA (Message d'accueil)
            out.println("Bienvenue! Commandes dispo: LOGIN Nom | ADD Nom Tel Email | LIST | QUIT");

            // 3. Boucle principale dyal l-commandes
            String clientCommand;
            while ((clientCommand = in.readLine()) != null) {
                
                // Nqsmo l-commande
                String[] parts = clientCommand.trim().split("\\s+", 4); // 4 pour ADD
                String commandType = parts[0].toUpperCase();
                String response = "ERROR: Commande inconnue.";

                //***************************************************
                // HNA HOWA T-TGHAYIR L-KABIR (THE BIG CHANGE)
                //***************************************************

                if (this.clientName == null) {
                    // --- L-USER MAZAL MA DAR LOGIN ---
                    
                    System.out.println("   [Client ??] Reçu: " + clientCommand);
                    
                    switch (commandType) {
                        case "LOGIN":
                            // Ghadi n3yto l-fonction jdida t'handli l-login
                            response = handleLoginCommand(clientCommand.trim().split("\\s+", 2));
                            break;
                        case "ADD":
                            // L-7el dyalk! Kan nkhliwh idir ADD
                            response = AnnuaireServer.handleAddCommand(parts);
                            break;
                        case "LIST":
                            response = AnnuaireServer.handleListCommand();
                            break;
                        case "SEND_MSG":
                            // Makhlitouch isift, 7it ma3rfnach chkoun howa
                            response = "ERROR: Identification requise. Utilisez LOGIN Nom d'abord.";
                            break;
                        case "QUIT":
                            out.println("OK: Déconnexion.");
                            return; // Kherj mn l-boucle o mn 'run()'
                        default:
                            response = "ERROR: Commande '" + commandType + "' inconnue. (Pas connecté)";
                            break;
                    }
                } else {
                    // --- L-USER DEJA DAR LOGIN (mconnecti b smito) ---
                    
                    System.out.println("   [Client " + clientName + "] Reçu: " + clientCommand);
                    
                    // Nqsmo l-commande dyal SEND_MSG (li kat7taj 3 parts)
                    String[] sendParts = clientCommand.trim().split("\\s+", 3);

                    switch (commandType) {
                        case "LOGIN":
                            response = "ERROR: Vous êtes déjà connecté en tant que " + this.clientName;
                            break;
                        case "ADD":
                            response = AnnuaireServer.handleAddCommand(parts);
                            break;
                        case "LIST":
                            response = AnnuaireServer.handleListCommand();
                            break;
                        case "SEND_MSG":
                            // Daba 3ndo l-7aq isift
                            response = handleSendCommand(sendParts);
                            break;
                        case "QUIT":
                            out.println("OK: Déconnexion.");
                            return; // Kherj mn l-boucle o mn 'run()'
                        default:
                            response = "ERROR: Commande '" + commandType + "' inconnue. (Connecté)";
                            break;
                    }
                }
                
                // Nsifto l-réponse l-client
                out.println(response);
            }
            
        } catch (IOException e) {
            // ... (Nefs l-code dyal l-erreur) ...
        } finally {
            // ... (Nefs l-code dyal l-Cleanup) ...
            try {
                if (this.clientName != null) {
                    AnnuaireServer.activeClientHandlers.remove(this.clientName);
                    System.out.println("   <- LOGOUT: " + this.clientName + " est hors ligne.");
                }
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fonction jdida: Katjiri (manages) l-commande LOGIN.
     * (Hada howa l-code li kan f l-boucle 'while(true)' l-qdima)
     */
    private String handleLoginCommand(String[] parts) {
        if (parts.length != 2) {
            return "ERROR: Syntax LOGIN invalide. Utilisation: LOGIN Nom";
        }
        
        String nom = parts[1];
        
        // 1. Nchofo wash l-contact kayn f l-annuaire l-kbir (KIMA BGHITI NTA)
        if (!AnnuaireServer.annuaire.containsKey(nom)) {
            return "ERROR: Le contact '" + nom + "' n'existe pas dans l'annuaire. Utilisez ADD d'abord.";
        }

        // 2. Nchofo wash l-nom machi mconnecti (déjà)
        if (AnnuaireServer.activeClientHandlers.containsKey(nom)) {
            return "ERROR: Le contact '" + nom + "' est déjà connecté.";
        }
        
        // 3. Kolchi mzyan: Nqydoh o ndkhloh
        this.clientName = nom;
        AnnuaireServer.activeClientHandlers.put(this.clientName, this);
        System.out.println("   -> LOGIN: " + this.clientName + " est maintenant en ligne.");
        
        return "OK: Vous êtes connecté en tant que " + this.clientName + ". (SEND_MSG est dispo)";
    }

    /**
     * Traite la commande SEND_MSG NomRecepteur Message.
     * (HADI BQAT KIMA HIYA, MA TBEDLAT-CH)
     */
    private String handleSendCommand(String[] parts) {
        if (parts.length != 3) {
            return "ERROR: Syntax SEND_MSG invalide. Utilisation: SEND_MSG NomRecepteur Message";
        }
        
        String destinataire = parts[1];
        String message = parts[2];
        String expediteur = this.clientName; // Hna fin 7tajina l-LOGIN

        // 1. Nchofo wash l-destinataire kayn f l-annuaire
        if (!AnnuaireServer.annuaire.containsKey(destinataire)) {
            return "ERROR: Le contact '" + destinataire + "' n'existe pas dans l'annuaire.";
        }

        // 2. Nchofo wash l-destinataire mconnecti (ONLINE)
        ClientHandler targetHandler = AnnuaireServer.activeClientHandlers.get(destinataire);

        if (targetHandler != null) {
            targetHandler.sendMessage("MESSAGE_FROM " + expediteur + ": " + message);
            return "OK: Message envoyé à " + destinataire;
        } else {
            return "ERROR: Le contact '" + destinataire + "' est hors ligne (offline).";
        }
    }

    /**
     * Fonction bach l-handlers l-khrin isifto message l-HAD l-client.
     * (HADI BQAT KIMA HIYA, MA TBEDLAT-CH)
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
}