import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class OnlineVotingSystem extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private String currentUser = null;
    private boolean hasVoted = false;

    // Candidates and vote count
    private HashMap<String, Integer> candidates = new HashMap<>();

    public OnlineVotingSystem() {
        setTitle("Online Voting System");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        candidates.put("Alice", 0);
        candidates.put("Bob", 0);
        candidates.put("Charlie", 0);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "login");
        mainPanel.add(createVotingPanel(), "vote");
        mainPanel.add(createResultPanel(), "result");

        add(mainPanel);
        cardLayout.show(mainPanel, "login");
        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        JButton loginButton = new JButton("Login");

        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);
        panel.add(new JLabel(""));
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());

            // Mock login (replace with DB auth later)
            if (user.equals("user") && pass.equals("pass")) {
                currentUser = user;
                hasVoted = false;
                cardLayout.show(mainPanel, "vote");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        });

        return panel;
    }

    private JPanel createVotingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel candidatesPanel = new JPanel(new GridLayout(0, 1, 10, 10));

        for (String candidate : candidates.keySet()) {
            JButton voteBtn = new JButton("Vote for " + candidate);
            voteBtn.addActionListener(e -> {
                if (hasVoted) {
                    JOptionPane.showMessageDialog(this, "You have already voted!");
                } else {
                    candidates.put(candidate, candidates.get(candidate) + 1);
                    hasVoted = true;
                    JOptionPane.showMessageDialog(this, "Thanks for voting!");
                    cardLayout.show(mainPanel, "result");
                }
            });
            candidatesPanel.add(voteBtn);
        }

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            currentUser = null;
            cardLayout.show(mainPanel, "login");
        });

        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panel.add(new JLabel("Select your candidate:"), BorderLayout.NORTH);
        panel.add(candidatesPanel, BorderLayout.CENTER);
        panel.add(logoutBtn, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        JButton backBtn = new JButton("Back to Login");

        backBtn.addActionListener(e -> {
            currentUser = null;
            cardLayout.show(mainPanel, "login");
        });

        panel.add(new JLabel("Voting Results:"), BorderLayout.NORTH);
        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        panel.add(backBtn, BorderLayout.SOUTH);

        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                resultArea.setText("");
                for (String candidate : candidates.keySet()) {
                    resultArea.append(candidate + ": " + candidates.get(candidate) + " votes\n");
                }
            }
        });

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OnlineVotingSystem::new);
    }
}
