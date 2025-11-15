import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class interfaceClient extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private PrintWriter serverOut;
    private BufferedReader serverIn;
    private JTextArea outputArea;

    public interfaceClient() {
        super("Annuaire & Messagerie Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        initializeGUI();
        connectToServer();
        setVisible(true);
    }
    
    private void initializeGUI() {
        setLayout(new BorderLayout());

        // Output Area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setText("Client started. Connecting to server...\n");
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        // Control Panel
        JPanel controlPanel = new JPanel(new GridLayout(1, 2));
        
        // Left Panel
        JPanel leftPanel = createLeftPanel();
        // Right Panel  
        JPanel rightPanel = createRightPanel();
        
        controlPanel.add(leftPanel);
        controlPanel.add(rightPanel);
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Connection & Directory"));
        
        JTextField loginField = new JTextField(10);
        JTextField nameField = new JTextField(10);
        JTextField telField = new JTextField(10);
        JTextField emailField = new JTextField(10);
        
        JButton loginBtn = new JButton("LOGIN");
        JButton listBtn = new JButton("LIST Contacts");
        JButton addBtn = new JButton("ADD Contact");
        JButton quitBtn = new JButton("QUIT");

        panel.add(new JLabel("Login Name:")); panel.add(loginField);
        panel.add(loginBtn); panel.add(listBtn);
        panel.add(new JLabel("Name (ADD):")); panel.add(nameField);
        panel.add(new JLabel("Phone (ADD):")); panel.add(telField);
        panel.add(new JLabel("Email (ADD):")); panel.add(emailField);
        panel.add(addBtn); panel.add(quitBtn);
        
        // Button actions
        loginBtn.addActionListener(e -> sendCommand("LOGIN " + loginField.getText()));
        listBtn.addActionListener(e -> sendCommand("LIST"));
        quitBtn.addActionListener(e -> { sendCommand("QUIT"); dispose(); System.exit(0); });
        addBtn.addActionListener(e -> addContact(nameField, telField, emailField));
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Messaging"));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField targetField = new JTextField();
        JTextField messageField = new JTextField();
        JButton sendBtn = new JButton("SEND_MSG");

        inputPanel.add(new JLabel("Recipient:")); inputPanel.add(targetField);
        inputPanel.add(new JLabel("Message:")); inputPanel.add(messageField);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(sendBtn, BorderLayout.SOUTH);
        
        sendBtn.addActionListener(e -> sendMessage(targetField, messageField));
        return panel;
    }
    
    private void addContact(JTextField name, JTextField tel, JTextField email) {
        if (!name.getText().isEmpty() && !tel.getText().isEmpty() && !email.getText().isEmpty()) {
            sendCommand("ADD " + name.getText() + " " + tel.getText() + " " + email.getText());
            name.setText(""); tel.setText(""); email.setText("");
        } else {
            outputArea.append("Error: All fields required for ADD command\n");
        }
    }
    
    private void sendMessage(JTextField target, JTextField msg) {
        if (!target.getText().isEmpty() && !msg.getText().isEmpty()) {
            sendCommand("SEND_MSG " + target.getText() + " " + msg.getText());
            msg.setText("");
        } else {
            outputArea.append("Error: Recipient and message required for SEND_MSG\n");
        }
    }
    
    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            serverOut = new PrintWriter(socket.getOutputStream(), true);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputArea.append("✅ Connected to server.\n");
            new ServerListener().execute();
        } catch (IOException e) {
            outputArea.append("❌ Connection Error\n");
            JOptionPane.showMessageDialog(this, "Cannot connect to server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendCommand(String command) {
        if (serverOut != null) {
            serverOut.println(command);
            outputArea.append("You: " + command + "\n");
        } else {
            outputArea.append("ERROR: Not connected to server.\n");
        }
    }
    
    private class ServerListener extends SwingWorker<Void, String> {
        @Override
        protected Void doInBackground() throws Exception {
            try {
                String response;
                while ((response = serverIn.readLine()) != null) publish(response);
            } catch (IOException e) {
                publish("❌ Connection lost");
            }
            return null;
        }
        
        @Override
        protected void process(List<String> chunks) {
            for (String response : chunks) {
                if (response.startsWith("MESSAGE_FROM ")) {
                    // Convert "MESSAGE_FROM John: Hello" to "John: Hello"
                    String cleanMessage = response.replace("MESSAGE_FROM ", "");
                    outputArea.append(cleanMessage + "\n");
                } else {
                    // Show other messages as is
                    outputArea.append(response + "\n");
                }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new interfaceClient());
    }
}
