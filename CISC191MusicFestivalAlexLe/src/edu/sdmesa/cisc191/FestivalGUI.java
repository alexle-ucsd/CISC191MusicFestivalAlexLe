package edu.sdmesa.cisc191;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FestivalGUI extends JFrame {
	
	private List<Attendee> attendees = new ArrayList<>();
	private int nextAttendeeId = 1;

    public FestivalGUI() {
        setTitle("Electronic Music Festival Management System");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 20));
        panel.setBackground(new Color(20, 15, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel(
            "ELECTRONIC MUSIC FESTIVAL",
            SwingConstants.CENTER
        );
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(3, 2, 20, 20));
        buttons.setOpaque(false);

        String[] menuItems = {
            "Register Attendee",
            "Buy Ticket",
            "View Lineup",
            "View Schedule",
            "Search Artist",
            "Exit"
        };

        for (String item : menuItems) {
            JButton button = new JButton(item);
            button.setFont(new Font("Arial", Font.BOLD, 17));
            button.setBackground(new Color(65, 35, 125));
            button.setForeground(Color.BLACK);
            button.setOpaque(true);

            button.addActionListener(e -> {
                if (item.equals("Exit")) {
                    dispose();

                } else if (item.equals("Register Attendee")) {

                    String name = JOptionPane.showInputDialog(
                        this, "Enter attendee name:"
                    );

                    if (name != null && !name.trim().isEmpty()) {
                    	

                        String email = JOptionPane.showInputDialog(
                            this, "Enter attendee email:"
                        );

                        if (email != null && !email.trim().isEmpty()) {
                        	
                        	
                        	Attendee attendee = new Attendee(nextAttendeeId++, name, email);
                        	attendees.add(attendee);
                        	              
                            JOptionPane.showMessageDialog(
                                this,
                                "Registration Successful!\n"
                                + "Name: " + name + "\n"
                                + "Email: " + email
                            );
                        }
                    }

                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        item + " feature coming soon!"
                    );
                }
            });

            buttons.add(button);
        }

        panel.add(buttons, BorderLayout.CENTER);
        add(panel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FestivalGUI().setVisible(true);
        });
    }
}

