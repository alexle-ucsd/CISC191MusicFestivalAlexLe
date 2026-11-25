package edu.sdmesa.cisc191;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;

public class FestivalGUI extends JFrame {

	private List<Attendee> attendees = new ArrayList<>();

	private Festival festival = new Festival("Electronic Music Festival");
	private final List<Artist> loadedArtists = new ArrayList<>();

	private int nextAttendeeId = 1;
	private int nextTicketId = 1;

	private static final String ATTENDEE_FILE = "attendees.txt";

	public FestivalGUI() {

		generateFestivalSchedule();

		loadTopDJs();

		loadAttendees();

		setTitle("Electronic Music Festival Management System");

		setSize(1080, 730);
		setMinimumSize(new Dimension(850, 600));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		// The dashboard artwork is the exact selected preview, without the fake Mac
		// title bar.
		// Transparent Swing buttons overlay the six illustrated controls.
		final java.awt.image.BufferedImage dashboard;
		try (java.io.InputStream input = FestivalGUI.class.getResourceAsStream("festival_dashboard_background.png")) {
			if (input == null)
				throw new java.io.IOException("Missing festival_dashboard_background.png");
			dashboard = javax.imageio.ImageIO.read(input);
		} catch (java.io.IOException ex) {
			throw new IllegalStateException(
					"Place festival_dashboard_background.png in the same package as FestivalGUI.java", ex);
		}
		JPanel panel = new JPanel(null) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g2.drawImage(dashboard, 0, 0, getWidth(), getHeight(), null);
				g2.dispose();
			}
		};
		panel.setBackground(new Color(4, 6, 27));
		setContentPane(panel);
		final java.util.List<JButton> controls = new ArrayList<>();
		// Positions are in coordinates of the 1536 x 978 artwork.
		final int[][] bounds = { { 60, 626, 465, 122 }, { 548, 626, 445, 122 }, { 1010, 626, 465, 122 },
				{ 60, 774, 465, 126 }, { 548, 774, 445, 126 }, { 1010, 774, 465, 126 } };

		String[] menuItems = { "Register Attendee", "Buy Ticket", "View Lineup", "View Schedule", "Search Artist",
				"Exit" };

		for (int buttonIndex = 0; buttonIndex < menuItems.length; buttonIndex++) {
			String item = menuItems[buttonIndex];
			JButton button = new JButton();
			button.setToolTipText(item);
			button.getAccessibleContext().setAccessibleName(item);
			button.setOpaque(false);
			button.setContentAreaFilled(false);
			button.setBorderPainted(false);
			button.setFocusPainted(false);
			button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			button.addActionListener(e -> {
				if (item.equals("Exit")) {
					dispose();

				} else if (item.equals("Register Attendee")) {

					String name = neonInput("REGISTER ATTENDEE", "Enter attendee name:");

					if (name != null && !name.trim().isEmpty()) {

						String email = neonInput("REGISTER ATTENDEE", "Enter attendee email:");

						if (email != null && !email.trim().isEmpty()) {

							int id = nextAttendeeId;

							if (!saveAttendees(id, name, email)) {
								neonMessage("Unable to save registration.");
								return;
							}

							Attendee attendee = new Attendee(id, name, email);
							attendees.add(attendee);
							nextAttendeeId++;

							neonMessage("Registration Successful!\n" + "Name: " + name + "\n" + "Email: " + email);
						}
					}

				} else if (item.equals("Buy Ticket")) {

					String[] ticketTypes = { "General Admission", "VIP" };

					String selectedType = (String) neonSelect("BUY TICKET", "Select your ticket type:", ticketTypes);

					if (selectedType != null) {

						double price = selectedType.equals("VIP") ? 150.00 : 75.00;

						Ticket ticket = new Ticket(nextTicketId++, selectedType, price);

						neonMessage("Ticket Purchased Successfully!\n" + "Ticket ID: " + ticket.getTicketId() + "\n"
								+ "Type: " + ticket.getTicketType() + "\n" + "Price: $"
								+ String.format("%.2f", ticket.getPrice()));
					}

				} else if (item.equals("View Lineup")) {

					StringBuilder lineup = new StringBuilder("Festival Lineup:\n\n");

					if (loadedArtists.isEmpty()) {
						lineup.append("No artists have been added yet.");
					} else {
						for (Artist artist : loadedArtists) {
							lineup.append(artist.getName()).append("\n\n");
						}
					}

					JTextArea textArea = new JTextArea(lineup.toString());
					textArea.setEditable(false);
					textArea.setFont(new Font("Arial", Font.PLAIN, 14));

					JScrollPane scrollPane = new JScrollPane(textArea);
					scrollPane.setPreferredSize(new Dimension(450, 400));

					neonDisplay("FESTIVAL LINEUP", scrollPane);
				} else if (item.equals("Search Artist")) {

					String name = neonInput("SEARCH ARTIST", "Enter DJ name:");

					if (name != null && !name.trim().isEmpty()) {

						Artist artist = festival.searchArtist(name.trim());

						if (artist != null) {
							showArtistPerformances(artist);
						} else {
							neonMessage("Artist not found: " + name);
						}
					}

				} else if (item.equals("View Schedule")) {

					String selectedDay = (String) neonSelect("VIEW SCHEDULE", "Select festival day:", festivalDays);

					if (selectedDay != null) {

						StringBuilder result = new StringBuilder();
						result.append("FESTIVAL SCHEDULE - ").append(selectedDay)
								.append("\nEDC Las Vegas 2026 - Published Set Times\n\n");

						for (String stage : stageNames) {

							result.append("=== ").append(stage).append(" ===\n");

							List<ScheduleEntry> stageShows = new ArrayList<>();

							for (ScheduleEntry entry : schedule) {
								if (entry.day.equals(selectedDay) && entry.stage.equals(stage)) {
									stageShows.add(entry);
								}
							}

							stageShows.sort((a, b) -> Integer.compare(a.startMinute, b.startMinute));

							for (ScheduleEntry entry : stageShows) {
								result.append(formatTime(entry.startMinute)).append(" - ")
										.append(formatTime(entry.endMinute)).append("   ").append(entry.dj)
										.append("\n");
							}

							result.append("\n");
						}

						JTextArea textArea = new JTextArea(result.toString());
						textArea.setEditable(false);
						textArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
						textArea.setCaretPosition(0);

						JScrollPane scrollPane = new JScrollPane(textArea);
						scrollPane.setPreferredSize(new Dimension(650, 500));

						neonDisplay(selectedDay.toUpperCase() + " FESTIVAL SCHEDULE", scrollPane);
					}

				}
			});

			panel.add(button);
			controls.add(button);
		}

		// Copyright notice at the bottom of the festival GUI
		JLabel copyrightLabel = new JLabel(
				"<html><div style='text-align:center;'>" + "<font color='#FF4DFF'>━━━━━━</font>" + "&nbsp;&nbsp;&nbsp;"
						+ "<font color='white'>Developed by Alex Le © 2026</font>" + "&nbsp;&nbsp;&nbsp;"
						+ "<font color='#FF4DFF'>━━━━━━</font>" + "</div></html>");

		copyrightLabel.setForeground(Color.WHITE);
		copyrightLabel.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 18));
		copyrightLabel.setHorizontalAlignment(SwingConstants.CENTER);

		panel.add(copyrightLabel);

		Runnable layoutCopyright = () -> {
			copyrightLabel.setBounds(20, (int) (panel.getHeight() * 0.91), panel.getWidth() - 40,
					(int) (panel.getHeight() * 0.08));
		};

		Runnable layoutButtons = () -> {
			for (int i = 0; i < controls.size(); i++) {
				int[] b = bounds[i];
				controls.get(i).setBounds((int) Math.round(b[0] * panel.getWidth() / 1536.0),
						(int) Math.round(b[1] * panel.getHeight() / 978.0),
						(int) Math.round(b[2] * panel.getWidth() / 1536.0),
						(int) Math.round(b[3] * panel.getHeight() / 978.0));
			}
		};
		panel.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				layoutButtons.run();
			}
		});
		SwingUtilities.invokeLater(layoutButtons);

		panel.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentResized(java.awt.event.ComponentEvent e) {
				layoutCopyright.run();
			}
		});

		SwingUtilities.invokeLater(layoutCopyright);

	}

	// Consistent neon dialogs, without Swing's default gray JOptionPane appearance.
	private static final Color NIGHT = new Color(7, 10, 37);
	private static final Color PANEL_NIGHT = new Color(17, 22, 60);
	private static final Color CYAN = new Color(0, 224, 255);
	private static final Color PINK = new Color(255, 54, 194);

	private JPanel dialogBody(String title) {
		JPanel body = new JPanel(new BorderLayout(0, 18));
		body.setBackground(NIGHT);
		body.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(CYAN, 2),
				BorderFactory.createEmptyBorder(24, 28, 24, 28)));
		JLabel heading = new JLabel(title);
		heading.setFont(new Font("SansSerif", Font.BOLD, 21));
		heading.setForeground(CYAN);
		body.add(heading, BorderLayout.NORTH);
		return body;
	}

	private JButton neonButton(String label, boolean primary) {
		JButton b = new JButton(label);
		b.setFont(new Font("SansSerif", Font.BOLD, 15));
		b.setForeground(Color.WHITE);
		b.setBackground(primary ? new Color(120, 31, 153) : new Color(27, 39, 86));
		b.setOpaque(true);
		b.setFocusPainted(false);
		b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? PINK : CYAN, 2, true),
				BorderFactory.createEmptyBorder(9, 24, 9, 24)));
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return b;
	}

	private JDialog neonDialog(String title, JPanel body) {
		JDialog dialog = new JDialog(this, title, true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setContentPane(body);
		dialog.pack();
		dialog.setMinimumSize(new Dimension(420, dialog.getHeight()));
		dialog.setLocationRelativeTo(this);
		return dialog;
	}

	private String neonInput(String title, String prompt) {
		JPanel body = dialogBody(title);
		JPanel center = new JPanel(new BorderLayout(0, 10));
		center.setOpaque(false);
		JLabel label = new JLabel(prompt);
		label.setForeground(Color.WHITE);
		label.setFont(new Font("SansSerif", Font.PLAIN, 16));
		center.add(label, BorderLayout.NORTH);
		JTextField field = new JTextField(26);
		field.setFont(new Font("SansSerif", Font.PLAIN, 17));
		field.setBackground(PANEL_NIGHT);
		field.setForeground(Color.WHITE);
		field.setCaretColor(CYAN);
		field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(CYAN, 2, true),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));
		center.add(field, BorderLayout.CENTER);
		body.add(center, BorderLayout.CENTER);
		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
		actions.setOpaque(false);
		JButton cancel = neonButton("Cancel", false);
		JButton submit = neonButton("Continue", true);
		actions.add(cancel);
		actions.add(submit);
		body.add(actions, BorderLayout.SOUTH);
		JDialog dialog = neonDialog(title, body);
		final String[] result = { null };
		cancel.addActionListener(e -> dialog.dispose());
		submit.addActionListener(e -> {
			result[0] = field.getText();
			dialog.dispose();
		});
		field.addActionListener(e -> submit.doClick());
		dialog.getRootPane().setDefaultButton(submit);
		SwingUtilities.invokeLater(field::requestFocusInWindow);
		dialog.setVisible(true);
		return result[0];
	}

	// Custom Swing dropdown avoids the native macOS popup turning black or blank.
	private String neonSelect(String title, String prompt, String[] choices) {
		JPanel body = dialogBody(title);
		JPanel center = new JPanel(new BorderLayout(0, 12));
		center.setOpaque(false);
		JLabel label = new JLabel(prompt);
		label.setForeground(Color.WHITE);
		label.setFont(new Font("SansSerif", Font.PLAIN, 16));
		center.add(label, BorderLayout.NORTH);

		// The selected value remains visible after closing the dropdown.
		JTextField selected = new JTextField(choices.length > 0 ? choices[0] : "");
		selected.setEditable(false);
		selected.setFont(new Font("SansSerif", Font.PLAIN, 17));
		selected.setBackground(PANEL_NIGHT);
		selected.setForeground(Color.WHITE);
		selected.setBorder(BorderFactory.createEmptyBorder(11, 14, 11, 8));
		JButton arrow = new JButton("\u25be");
		arrow.setFont(new Font("SansSerif", Font.BOLD, 20));
		arrow.setForeground(CYAN);
		arrow.setBackground(PANEL_NIGHT);
		arrow.setOpaque(true);
		arrow.setFocusPainted(false);
		arrow.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 14));
		arrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		JPanel selector = new JPanel(new BorderLayout());
		selector.setBackground(PANEL_NIGHT);
		selector.setBorder(BorderFactory.createLineBorder(CYAN, 2, true));
		selector.add(selected, BorderLayout.CENTER);
		selector.add(arrow, BorderLayout.EAST);
		center.add(selector, BorderLayout.CENTER);
		body.add(center, BorderLayout.CENTER);

		JList<String> list = new JList<>(choices);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(choices.length > 0 ? 0 : -1);
		list.setFont(new Font("SansSerif", Font.PLAIN, 17));
		list.setBackground(PANEL_NIGHT);
		list.setForeground(Color.WHITE);
		list.setSelectionBackground(new Color(75, 42, 149));
		list.setSelectionForeground(Color.WHITE);
		list.setFixedCellHeight(40);
		list.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		JPopupMenu popup = new JPopupMenu();
		popup.setBackground(PANEL_NIGHT);
		popup.setBorder(BorderFactory.createLineBorder(PINK, 2));
		JScrollPane popupScroll = new JScrollPane(list);
		popupScroll.setBorder(null);
		popupScroll.getViewport().setBackground(PANEL_NIGHT);
		popupScroll.setPreferredSize(new Dimension(360, Math.min(choices.length * 40 + 8, 220)));
		popup.add(popupScroll);
		Runnable showPopup = () -> {
			popupScroll.setPreferredSize(new Dimension(selector.getWidth(), Math.min(choices.length * 40 + 8, 220)));
			popup.pack();
			popup.show(selector, 0, selector.getHeight());
			list.requestFocusInWindow();
		};
		arrow.addActionListener(e -> showPopup.run());
		selected.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				showPopup.run();
			}
		});
		list.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() >= 1 && list.getSelectedValue() != null) {
					selected.setText(list.getSelectedValue());
					popup.setVisible(false);
				}
			}
		});
		list.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "choose");
		list.getActionMap().put("choose", new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (list.getSelectedValue() != null)
					selected.setText(list.getSelectedValue());
				popup.setVisible(false);
			}
		});

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
		actions.setOpaque(false);
		JButton cancel = neonButton("Cancel", false);
		JButton submit = neonButton("Select", true);
		actions.add(cancel);
		actions.add(submit);
		body.add(actions, BorderLayout.SOUTH);
		JDialog dialog = neonDialog(title, body);
		final String[] result = { null };
		cancel.addActionListener(e -> dialog.dispose());
		submit.addActionListener(e -> {
			result[0] = selected.getText().isEmpty() ? null : selected.getText();
			dialog.dispose();
		});
		dialog.getRootPane().setDefaultButton(submit);
		dialog.setVisible(true);
		return result[0];
	}

	// Artist card styled to match the approved compact neon reference.
	// Set times are taken from the same schedule displayed by View Schedule.
	private void showArtistPerformances(Artist artist) {
		JPanel body = new JPanel(new BorderLayout(0, 16));
		body.setBackground(NIGHT);
		body.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(CYAN, 2, true),
				BorderFactory.createEmptyBorder(18, 24, 16, 24)));

		JLabel heading = new JLabel("ARTIST FOUND!");
		heading.setForeground(CYAN);
		heading.setFont(new Font("SansSerif", Font.BOLD, 23));
		body.add(heading, BorderLayout.NORTH);

		JPanel content = new JPanel();
		content.setOpaque(false);
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		JPanel artistRow = new JPanel(new BorderLayout(14, 0));
		artistRow.setOpaque(false);
		JLabel headphones = new JLabel(new FestivalDetailIcon("headphones", PINK));
		artistRow.add(headphones, BorderLayout.WEST);
		JPanel artistText = new JPanel();
		artistText.setOpaque(false);
		artistText.setLayout(new BoxLayout(artistText, BoxLayout.Y_AXIS));
		JLabel artistName = new JLabel(artist.getName());
		artistName.setForeground(Color.WHITE);
		artistName.setFont(new Font("SansSerif", Font.BOLD, 21));
		artistText.add(artistName);
		JLabel subtitle = new JLabel("Electronic Music Festival");
		subtitle.setForeground(new Color(205, 213, 233));
		subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
		artistText.add(subtitle);
		artistRow.add(artistText, BorderLayout.CENTER);
		artistRow.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(Box.createVerticalStrut(5));
		content.add(artistRow);
		content.add(Box.createVerticalStrut(15));

		JSeparator divider = new JSeparator();
		divider.setForeground(new Color(110, 111, 242));
		divider.setBackground(new Color(110, 111, 242));
		divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
		divider.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(divider);
		content.add(Box.createVerticalStrut(15));

		int matches = 0;
		for (String day : festivalDays) {
			for (ScheduleEntry entry : schedule) {
				if (entry.day.equals(day) && entry.dj.equalsIgnoreCase(artist.getName())) {
					if (matches++ > 0)
						content.add(Box.createVerticalStrut(15));
					String date = day.equals("Friday") ? "May 15, 2026"
							: day.equals("Saturday") ? "May 16, 2026" : "May 17, 2026";
					content.add(artistDetailRow("calendar", CYAN, "Day: " + day + ", " + date, true));
					content.add(Box.createVerticalStrut(8));
					content.add(artistDetailRow("pin", PINK, "Stage: " + entry.stage, false));
					content.add(Box.createVerticalStrut(8));
					content.add(artistDetailRow("clock", new Color(255, 216, 0),
							"Set Time: " + formatTime(entry.startMinute) + " – " + formatTime(entry.endMinute), false));
				}
			}
		}
		if (matches == 0) {
			JLabel missing = new JLabel("Performance time not listed in the schedule.");
			missing.setForeground(Color.WHITE);
			missing.setFont(new Font("SansSerif", Font.PLAIN, 16));
			content.add(missing);
		}

		JScrollPane scroll = new JScrollPane(content);
		scroll.setBorder(null);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.getVerticalScrollBar().setUnitIncrement(14);
		scroll.setPreferredSize(new Dimension(540, Math.min(370, 104 + Math.max(1, matches) * 110)));
		body.add(scroll, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		actions.setOpaque(false);
		JButton ok = neonButton("OK", true);
		actions.add(ok);
		body.add(actions, BorderLayout.SOUTH);
		JDialog dialog = neonDialog("Artist Found", body);
		ok.addActionListener(e -> dialog.dispose());
		dialog.getRootPane().setDefaultButton(ok);
		dialog.setVisible(true);
	}

	private JPanel artistDetailRow(String iconType, Color iconColor, String value, boolean bold) {
		JPanel row = new JPanel(new BorderLayout(13, 0));
		row.setOpaque(false);
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel icon = new JLabel(new FestivalDetailIcon(iconType, iconColor));
		row.add(icon, BorderLayout.WEST);
		JLabel text = new JLabel(value);
		text.setForeground(Color.WHITE);
		text.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, 16));
		row.add(text, BorderLayout.CENTER);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		return row;
	}

	// Draw the neon icons directly, so they look consistent on macOS and Windows.
	private static class FestivalDetailIcon implements Icon {
		private final String type;
		private final Color color;

		FestivalDetailIcon(String type, Color color) {
			this.type = type;
			this.color = color;
		}

		@Override
		public int getIconWidth() {
			return 27;
		}

		@Override
		public int getIconHeight() {
			return 27;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.translate(x + 2, y + 2);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(color);
			g2.setStroke(new BasicStroke(2.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			if ("headphones".equals(type)) {
				g2.drawArc(2, 2, 19, 19, 0, 180);
				g2.drawRoundRect(1, 12, 5, 9, 2, 2);
				g2.drawRoundRect(17, 12, 5, 9, 2, 2);
			} else if ("calendar".equals(type)) {
				g2.drawRoundRect(2, 4, 20, 18, 2, 2);
				g2.drawLine(2, 10, 22, 10);
				g2.drawLine(7, 1, 7, 7);
				g2.drawLine(17, 1, 17, 7);
				g2.fillRect(7, 13, 3, 3);
				g2.fillRect(14, 13, 3, 3);
				g2.fillRect(7, 18, 3, 2);
			} else if ("pin".equals(type)) {
				java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
				path.moveTo(12, 23);
				path.curveTo(8, 18, 3, 14, 3, 10);
				path.curveTo(3, -1, 21, -1, 21, 10);
				path.curveTo(21, 14, 16, 19, 12, 23);
				g2.draw(path);
				g2.drawOval(9, 7, 6, 6);
			} else if ("clock".equals(type)) {
				g2.drawOval(2, 2, 20, 20);
				g2.drawLine(12, 6, 12, 12);
				g2.drawLine(12, 12, 8, 16);
			}
			g2.dispose();
		}
	}

	private void neonMessage(String message) {
		JPanel body = dialogBody("FESTIVAL NOTIFICATION");
		JTextArea text = new JTextArea(message);
		text.setEditable(false);
		text.setOpaque(false);
		text.setForeground(Color.WHITE);
		text.setFont(new Font("SansSerif", Font.PLAIN, 16));
		text.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
		body.add(text, BorderLayout.CENTER);
		JButton ok = neonButton("OK", true);
		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		actions.setOpaque(false);
		actions.add(ok);
		body.add(actions, BorderLayout.SOUTH);
		JDialog dialog = neonDialog("Festival Notification", body);
		ok.addActionListener(e -> dialog.dispose());
		dialog.getRootPane().setDefaultButton(ok);
		dialog.setVisible(true);
	}

	private void neonDisplay(String title, JScrollPane scrollPane) {
		JPanel body = dialogBody(title);
		scrollPane.setBorder(BorderFactory.createLineBorder(CYAN, 2));
		scrollPane.getViewport().setBackground(PANEL_NIGHT);
		Component view = scrollPane.getViewport().getView();
		if (view instanceof JTextArea) {
			JTextArea area = (JTextArea) view;
			area.setBackground(PANEL_NIGHT);
			area.setForeground(Color.WHITE);
			area.setCaretColor(CYAN);
			area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		}
		body.add(scrollPane, BorderLayout.CENTER);
		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		actions.setOpaque(false);
		JButton close = neonButton("Close", true);
		actions.add(close);
		body.add(actions, BorderLayout.SOUTH);
		JDialog dialog = neonDialog(title, body);
		close.addActionListener(e -> dialog.dispose());
		dialog.setVisible(true);
	}

	private void loadTopDJs() {
		java.util.Set<String> names = new java.util.LinkedHashSet<>();

		for (ScheduleEntry entry : schedule) {
			if (!entry.dj.contains("CEREMONY") && !entry.dj.contains("FIREWORKS") && !entry.dj.contains("INTERMEZZO")) {
				names.add(entry.dj);
			}
		}

		int id = 1;
		for (String name : names) {
			Artist artist = new Artist(id++, name, "Electronic Music");
			festival.addArtist(artist);
			loadedArtists.add(artist);
		}
	}

	private final String[] stageNames = { "House Pineapple", "Techno Vault", "Remote Box", "Bass Reactor",
			"Trance Horizon", "Afterdark Arena", "Electric Paradise", "Cyber Galaxy", "Rhythm Factory",
			"Solar Frequency", "Digital Dreams", "Underground Empire", "Skyline Beats", "Infinity Sound",
			"Midnight Odyssey", "Neon Universe", "Eagle Fly", };

	private final String[] festivalDays = { "Friday", "Saturday", "Sunday" };

	private final List<ScheduleEntry> schedule = new ArrayList<>();

	private static class ScheduleEntry {
		String day;
		String stage;
		String dj;
		int startMinute;
		int endMinute;

		ScheduleEntry(String day, String stage, String dj, int startMinute, int endMinute) {
			this.day = day;
			this.stage = stage;
			this.dj = dj;
			this.startMinute = startMinute;
			this.endMinute = endMinute;
		}
	}

	private void generateFestivalSchedule() {
		schedule.clear();
		// EDC Las Vegas 2026 published set times, May 15-17.
		// Stage names are custom; gaps between performances are intentional.
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "Laidback Luke B2B Chuckie", 1140, 1200));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "Korolova", 1200, 1260));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "Argy", 1260, 1320));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "OPENING CEREMONY: KINETIC LAUNCH", 1320, 1327));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "Chris Lorenzo", 1327, 1395));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1395, 1399));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "Sofi Tukker", 1399, 1468));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1468, 1472));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "The Chainsmokers", 1472, 1540));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "MAIN FIREWORKS", 1540, 1547));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "FISHER", 1547, 1617));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1617, 1621));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "Porter Robinson (DJ Set)", 1621, 1690));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1690, 1694));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "Charlotte de Witte", 1694, 1768));
		schedule.add(new ScheduleEntry("Friday", stageNames[0], "CLOSING CEREMONY: ASCENT", 1768, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[1], "Max Dean B2B Luke Dean", 1020, 1135));
		schedule.add(new ScheduleEntry("Friday", stageNames[1], "Jackie Hollander", 1140, 1195));
		schedule.add(new ScheduleEntry("Friday", stageNames[1], "Roddy Lima", 1195, 1255));
		schedule.add(new ScheduleEntry("Friday", stageNames[1], "Westend", 1255, 1315));
		schedule.add(new ScheduleEntry("Friday", stageNames[1], "Walker & Royce B2B VNSSA", 1315, 1375));
		schedule.add(new ScheduleEntry("Friday", stageNames[1], "Underworld", 1390, 1450));
		schedule.add(new ScheduleEntry("Friday", stageNames[1], "MEDUZA", 1465, 1540));
		schedule.add(new ScheduleEntry("Friday", stageNames[1], "NOTION", 1547, 1607));
		schedule.add(new ScheduleEntry("Friday", stageNames[1], "MPH", 1607, 1682));
		schedule.add(new ScheduleEntry("Friday", stageNames[1], "San Pacho", 1682, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[2], "1991", 1140, 1200));
		schedule.add(new ScheduleEntry("Friday", stageNames[2], "Bou", 1200, 1260));
		schedule.add(new ScheduleEntry("Friday", stageNames[2], "Nico Moreno", 1260, 1320));
		schedule.add(new ScheduleEntry("Friday", stageNames[2], "I Hate Models", 1320, 1395));
		schedule.add(new ScheduleEntry("Friday", stageNames[2], "Levity", 1395, 1465));
		schedule.add(new ScheduleEntry("Friday", stageNames[2], "Wooli", 1465, 1535));
		schedule.add(new ScheduleEntry("Friday", stageNames[2], "The Outlaw", 1535, 1595));
		schedule.add(new ScheduleEntry("Friday", stageNames[2], "Holy Priest", 1595, 1650));
		schedule.add(new ScheduleEntry("Friday", stageNames[2], "Ray Volpe", 1650, 1710));
		schedule.add(new ScheduleEntry("Friday", stageNames[2], "Level Up", 1710, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[3], "Anastazja", 1140, 1230));
		schedule.add(new ScheduleEntry("Friday", stageNames[3], "Mëstiza", 1230, 1320));
		schedule.add(new ScheduleEntry("Friday", stageNames[3], "DJ Tennis B2B Chloé Caillet", 1320, 1410));
		schedule.add(new ScheduleEntry("Friday", stageNames[3], "Peggy Gou", 1410, 1500));
		schedule.add(new ScheduleEntry("Friday", stageNames[3], "Adriatique", 1500, 1590));
		schedule.add(new ScheduleEntry("Friday", stageNames[3], "Joseph Capriati", 1590, 1680));
		schedule.add(new ScheduleEntry("Friday", stageNames[3], "Eli Brown", 1680, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "RIOT", 1140, 1190));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "The Masquerade", 1190, 1240));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "HEYZ", 1240, 1290));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "MUZZ", 1290, 1350));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "GorillaT", 1350, 1410));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "GHENGAR", 1410, 1470));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "ATLiens", 1470, 1530));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "Kai Wachi", 1530, 1590));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "Adventure Club (Throwback Set)", 1590, 1650));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "Culture Shock", 1650, 1710));
		schedule.add(new ScheduleEntry("Friday", stageNames[4], "Cyclops", 1710, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[5], "DØMINA", 1140, 1230));
		schedule.add(new ScheduleEntry("Friday", stageNames[5], "Serafina", 1230, 1290));
		schedule.add(new ScheduleEntry("Friday", stageNames[5], "Johannes Schuster", 1290, 1350));
		schedule.add(new ScheduleEntry("Friday", stageNames[5], "Adrian Mills", 1350, 1410));
		schedule.add(new ScheduleEntry("Friday", stageNames[5], "Cloudy", 1410, 1470));
		schedule.add(new ScheduleEntry("Friday", stageNames[5], "KUKO", 1470, 1530));
		schedule.add(new ScheduleEntry("Friday", stageNames[5], "GRAVEDGR", 1530, 1590));
		schedule.add(new ScheduleEntry("Friday", stageNames[5], "Rebekah", 1590, 1650));
		schedule.add(new ScheduleEntry("Friday", stageNames[5], "Dyen", 1650, 1710));
		schedule.add(new ScheduleEntry("Friday", stageNames[5], "Stan Christ", 1710, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[6], "Sarah de Warren", 1140, 1200));
		schedule.add(new ScheduleEntry("Friday", stageNames[6], "Matty Ralph", 1200, 1260));
		schedule.add(new ScheduleEntry("Friday", stageNames[6], "Cold Blue", 1260, 1320));
		schedule.add(new ScheduleEntry("Friday", stageNames[6], "Pegassi", 1320, 1380));
		schedule.add(new ScheduleEntry("Friday", stageNames[6], "Darude", 1380, 1440));
		schedule.add(new ScheduleEntry("Friday", stageNames[6], "Cosmic Gate", 1440, 1500));
		schedule.add(new ScheduleEntry("Friday", stageNames[6], "Gareth Emery", 1500, 1560));
		schedule.add(new ScheduleEntry("Friday", stageNames[6], "Ilan Bluestone", 1560, 1620));
		schedule.add(new ScheduleEntry("Friday", stageNames[6], "Paul van Dyk", 1620, 1680));
		schedule.add(new ScheduleEntry("Friday", stageNames[6], "Darren Porter", 1680, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[7], "Abana B2B Juliet Mendoza", 1140, 1200));
		schedule.add(new ScheduleEntry("Friday", stageNames[7], "SLAMM", 1200, 1260));
		schedule.add(new ScheduleEntry("Friday", stageNames[7], "Luuk van Dijk", 1260, 1335));
		schedule.add(new ScheduleEntry("Friday", stageNames[7], "Omar+", 1335, 1410));
		schedule.add(new ScheduleEntry("Friday", stageNames[7], "Luke Dean", 1410, 1485));
		schedule.add(new ScheduleEntry("Friday", stageNames[7], "Josh Baker", 1485, 1560));
		schedule.add(new ScheduleEntry("Friday", stageNames[7], "Max Dean", 1560, 1635));
		schedule.add(new ScheduleEntry("Friday", stageNames[7], "Obskür", 1635, 1710));
		schedule.add(new ScheduleEntry("Friday", stageNames[7], "Toman", 1710, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[8], "Heidi Lawden B2B Masha Mar", 1020, 1140));
		schedule.add(new ScheduleEntry("Friday", stageNames[8], "Stacy Christine", 1140, 1200));
		schedule.add(new ScheduleEntry("Friday", stageNames[8], "The Carry Nation", 1200, 1290));
		schedule.add(new ScheduleEntry("Friday", stageNames[8], "Massimiliano Pagliara", 1290, 1380));
		schedule.add(new ScheduleEntry("Friday", stageNames[8], "PARAMIDA", 1380, 1470));
		schedule.add(new ScheduleEntry("Friday", stageNames[8], "salute B2B Chloé Caillet", 1470, 1590));
		schedule.add(new ScheduleEntry("Friday", stageNames[8], "Robert Hood", 1590, 1680));
		schedule.add(new ScheduleEntry("Friday", stageNames[8], "Avalon Emerson", 1680, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[9], "Trouble", 1140, 1230));
		schedule.add(new ScheduleEntry("Friday", stageNames[9], "Dip B2B Vertigo", 1230, 1320));
		schedule.add(new ScheduleEntry("Friday", stageNames[9], "Machete", 1320, 1410));
		schedule.add(new ScheduleEntry("Friday", stageNames[9], "Danny The Wildchild", 1410, 1500));
		schedule.add(new ScheduleEntry("Friday", stageNames[9], "Mr Deeds", 1500, 1590));
		schedule.add(new ScheduleEntry("Friday", stageNames[9], "R.A.W.", 1590, 1680));
		schedule.add(new ScheduleEntry("Friday", stageNames[9], "Skanka", 1680, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[10], "Jruiz", 1140, 1200));
		schedule.add(new ScheduleEntry("Friday", stageNames[10], "Cardo", 1200, 1260));
		schedule.add(new ScheduleEntry("Friday", stageNames[10], "J.Patron", 1260, 1320));
		schedule.add(new ScheduleEntry("Friday", stageNames[10], "CC Love", 1320, 1410));
		schedule.add(new ScheduleEntry("Friday", stageNames[10], "Jfuse", 1410, 1500));
		schedule.add(new ScheduleEntry("Friday", stageNames[10], "Dramos", 1500, 1590));
		schedule.add(new ScheduleEntry("Friday", stageNames[10], "Guari", 1590, 1680));
		schedule.add(new ScheduleEntry("Friday", stageNames[10], "DJ Exile", 1680, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[11], "The Kandyman", 1140, 1230));
		schedule.add(new ScheduleEntry("Friday", stageNames[11], "Demigod", 1230, 1305));
		schedule.add(new ScheduleEntry("Friday", stageNames[11], "DJ Louis Love", 1305, 1380));
		schedule.add(new ScheduleEntry("Friday", stageNames[11], "R.A.W.", 1380, 1455));
		schedule.add(new ScheduleEntry("Friday", stageNames[11], "Ron D Core", 1455, 1530));
		schedule.add(new ScheduleEntry("Friday", stageNames[11], "Moonpup", 1530, 1605));
		schedule.add(new ScheduleEntry("Friday", stageNames[11], "Steve Leclair", 1605, 1680));
		schedule.add(new ScheduleEntry("Friday", stageNames[11], "Jason Splat", 1680, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[12], "Big Daddy Kess", 1140, 1200));
		schedule.add(new ScheduleEntry("Friday", stageNames[12], "Sussy", 1200, 1260));
		schedule.add(new ScheduleEntry("Friday", stageNames[12], "Dono", 1260, 1320));
		schedule.add(new ScheduleEntry("Friday", stageNames[12], "Max Low", 1320, 1380));
		schedule.add(new ScheduleEntry("Friday", stageNames[12], "Joey G", 1380, 1440));
		schedule.add(new ScheduleEntry("Friday", stageNames[12], "Taylor Kade", 1440, 1500));
		schedule.add(new ScheduleEntry("Friday", stageNames[12], "Castor & Pollux", 1500, 1560));
		schedule.add(new ScheduleEntry("Friday", stageNames[12], "Zaanee", 1560, 1620));
		schedule.add(new ScheduleEntry("Friday", stageNames[12], "Topcat", 1620, 1680));
		schedule.add(new ScheduleEntry("Friday", stageNames[12], "PLS&TY", 1680, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[13], "GL00M13", 1140, 1215));
		schedule.add(new ScheduleEntry("Friday", stageNames[13], "Punky Brewstar", 1215, 1290));
		schedule.add(new ScheduleEntry("Friday", stageNames[13], "Inspektor Gadjet", 1290, 1365));
		schedule.add(new ScheduleEntry("Friday", stageNames[13], "SkibKid", 1365, 1440));
		schedule.add(new ScheduleEntry("Friday", stageNames[13], "99Jakes", 1440, 1530));
		schedule.add(new ScheduleEntry("Friday", stageNames[13], "Hixxy", 1530, 1620));
		schedule.add(new ScheduleEntry("Friday", stageNames[13], "Kygore", 1620, 1695));
		schedule.add(new ScheduleEntry("Friday", stageNames[13], "Hartshorn", 1695, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[14], "Söso Drü", 1140, 1200));
		schedule.add(new ScheduleEntry("Friday", stageNames[14], "Sami G", 1245, 1305));
		schedule.add(new ScheduleEntry("Friday", stageNames[14], "Dirty Audio", 1335, 1395));
		schedule.add(new ScheduleEntry("Friday", stageNames[14], "Grant Fisher", 1440, 1500));
		schedule.add(new ScheduleEntry("Friday", stageNames[14], "Roadhouse", 1530, 1590));
		schedule.add(new ScheduleEntry("Friday", stageNames[14], "DJ Lucky Lou", 1640, 1700));
		schedule.add(new ScheduleEntry("Friday", stageNames[15], "Not Not", 1140, 1215));
		schedule.add(new ScheduleEntry("Friday", stageNames[15], "Mer Bear", 1215, 1290));
		schedule.add(new ScheduleEntry("Friday", stageNames[15], "Lady Sinclair", 1290, 1365));
		schedule.add(new ScheduleEntry("Friday", stageNames[15], "Tsu Nami presents Club Tsu", 1365, 1440));
		schedule.add(new ScheduleEntry("Friday", stageNames[15], "ARMNHMR presents PASSPORT", 1440, 1530));
		schedule.add(new ScheduleEntry("Friday", stageNames[15], "ero808", 1530, 1620));
		schedule.add(new ScheduleEntry("Friday", stageNames[15], "Kelland", 1620, 1695));
		schedule.add(new ScheduleEntry("Friday", stageNames[15], "Tavatli", 1695, 1770));
		schedule.add(new ScheduleEntry("Friday", stageNames[16], "Jem.G", 1140, 1230));
		schedule.add(new ScheduleEntry("Friday", stageNames[16], "Kelland", 1320, 1410));
		schedule.add(new ScheduleEntry("Friday", stageNames[16], "Juliet Mendoza", 1410, 1500));
		schedule.add(new ScheduleEntry("Friday", stageNames[16], "Gold Lemonade", 1500, 1590));
		schedule.add(new ScheduleEntry("Friday", stageNames[16], "NXSTY", 1590, 1680));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "AR/CO", 1140, 1200));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "HAYLA", 1200, 1260));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "Sub Focus", 1260, 1320));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "OPENING CEREMONY: KINETIC LAUNCH", 1320, 1327));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "Steve Aoki", 1327, 1395));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1395, 1399));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "Hardwell", 1399, 1468));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1468, 1472));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "John Summit", 1472, 1540));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "MAIN FIREWORKS", 1540, 1547));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "Subtronics", 1547, 1617));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1617, 1621));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "Kaskade", 1621, 1690));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1690, 1694));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "Above & Beyond (Sunrise Set)", 1694, 1768));
		schedule.add(new ScheduleEntry("Saturday", stageNames[0], "CLOSING CEREMONY: SKYBOUND", 1768, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[1], "Frost Children", 1140, 1215));
		schedule.add(new ScheduleEntry("Saturday", stageNames[1], "Hannah Laing", 1215, 1285));
		schedule.add(new ScheduleEntry("Saturday", stageNames[1], "Snow Strippers", 1285, 1335));
		schedule.add(new ScheduleEntry("Saturday", stageNames[1], "VTSS (In The Round)", 1335, 1410));
		schedule.add(new ScheduleEntry("Saturday", stageNames[1], "The Prodigy", 1415, 1475));
		schedule.add(new ScheduleEntry("Saturday", stageNames[1], "BUNT. (In The Round)", 1480, 1570));
		schedule.add(new ScheduleEntry("Saturday", stageNames[1], "Interplanetary Criminal", 1570, 1650));
		schedule.add(new ScheduleEntry("Saturday", stageNames[1], "MALUGI", 1650, 1710));
		schedule.add(new ScheduleEntry("Saturday", stageNames[1], "DJ Gigola B2B MCR-T", 1710, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[2], "DJ Mandy", 1140, 1200));
		schedule.add(new ScheduleEntry("Saturday", stageNames[2], "Röz", 1200, 1275));
		schedule.add(new ScheduleEntry("Saturday", stageNames[2], "KETTAMA", 1275, 1365));
		schedule.add(new ScheduleEntry("Saturday", stageNames[2], "Sammy Virji", 1365, 1455));
		schedule.add(new ScheduleEntry("Saturday", stageNames[2], "Tiësto", 1455, 1545));
		schedule.add(new ScheduleEntry("Saturday", stageNames[2], "Peggy Gou B2B KI/KI", 1545, 1635));
		schedule.add(new ScheduleEntry("Saturday", stageNames[2], "Boys Noize", 1635, 1710));
		schedule.add(new ScheduleEntry("Saturday", stageNames[2], "Lilly Palmer", 1710, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[3], "mink", 1140, 1230));
		schedule.add(new ScheduleEntry("Saturday", stageNames[3], "Silvie Loto", 1230, 1320));
		schedule.add(new ScheduleEntry("Saturday", stageNames[3], "Ahmed Spins", 1320, 1410));
		schedule.add(new ScheduleEntry("Saturday", stageNames[3], "Luciano", 1410, 1530));
		schedule.add(new ScheduleEntry("Saturday", stageNames[3], "Prospa", 1530, 1650));
		schedule.add(new ScheduleEntry("Saturday", stageNames[3], "Josh Baker B3B KETTAMA B3B Prospa", 1650, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[4], "Fallen w/ MC Dino", 1140, 1190));
		schedule.add(new ScheduleEntry("Saturday", stageNames[4], "Avello B2B Dennett", 1190, 1240));
		schedule.add(new ScheduleEntry("Saturday", stageNames[4], "Viperactive", 1240, 1290));
		schedule.add(new ScheduleEntry("Saturday", stageNames[4], "Hybrid Minds", 1290, 1350));
		schedule.add(new ScheduleEntry("Saturday", stageNames[4], "YDG", 1350, 1410));
		schedule.add(new ScheduleEntry("Saturday", stageNames[4], "Delta Heavy", 1410, 1470));
		schedule.add(new ScheduleEntry("Saturday", stageNames[4], "Getter", 1470, 1530));
		schedule.add(new ScheduleEntry("Saturday", stageNames[4], "Eptic B2B SPACE LACES", 1530, 1590));
		schedule.add(
				new ScheduleEntry("Saturday", stageNames[4], "Doctor P B3B Flux Pavilion B3B FUNTCASE", 1590, 1650));
		schedule.add(new ScheduleEntry("Saturday", stageNames[4], "HOL!", 1650, 1710));
		schedule.add(new ScheduleEntry("Saturday", stageNames[4], "Mary Droppinz", 1710, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[5], "CUTDWN", 1140, 1230));
		schedule.add(new ScheduleEntry("Saturday", stageNames[5], "Dead X", 1230, 1290));
		schedule.add(new ScheduleEntry("Saturday", stageNames[5], "The Saints", 1290, 1350));
		schedule.add(new ScheduleEntry("Saturday", stageNames[5], "Rob Gee B2B Lenny Dee", 1350, 1410));
		schedule.add(new ScheduleEntry("Saturday", stageNames[5], "Lady Faith B2B LNY TNZ", 1410, 1470));
		schedule.add(
				new ScheduleEntry("Saturday", stageNames[5], "Audiofreq B3B Code Black B3B Toneshifterz", 1470, 1530));
		schedule.add(new ScheduleEntry("Saturday", stageNames[5], "Da Tweekaz", 1530, 1590));
		schedule.add(new ScheduleEntry("Saturday", stageNames[5], "Lil Texas", 1590, 1650));
		schedule.add(new ScheduleEntry("Saturday", stageNames[5], "Mish", 1650, 1710));
		schedule.add(new ScheduleEntry("Saturday", stageNames[5], "Alyssa Jolee", 1710, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[6], "Chris Metcalfe", 1140, 1230));
		schedule.add(new ScheduleEntry("Saturday", stageNames[6], "SUPERSTRINGS", 1230, 1290));
		schedule.add(new ScheduleEntry("Saturday", stageNames[6], "Billy Gillies", 1290, 1350));
		schedule.add(new ScheduleEntry("Saturday", stageNames[6], "Paul Oakenfold", 1350, 1410));
		schedule.add(new ScheduleEntry("Saturday", stageNames[6], "Andrew Rayel", 1410, 1470));
		schedule.add(new ScheduleEntry("Saturday", stageNames[6], "Maddix", 1470, 1530));
		schedule.add(new ScheduleEntry("Saturday", stageNames[6], "Mathame", 1530, 1590));
		schedule.add(new ScheduleEntry("Saturday", stageNames[6], "Astrix", 1590, 1650));
		schedule.add(new ScheduleEntry("Saturday", stageNames[6], "T78", 1650, 1710));
		schedule.add(new ScheduleEntry("Saturday", stageNames[6], "Thomas Schumacher", 1710, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[7], "Slugg", 1140, 1200));
		schedule.add(new ScheduleEntry("Saturday", stageNames[7], "DREYA V", 1200, 1260));
		schedule.add(new ScheduleEntry("Saturday", stageNames[7], "Discip", 1260, 1320));
		schedule.add(new ScheduleEntry("Saturday", stageNames[7], "OMNOM", 1320, 1395));
		schedule.add(new ScheduleEntry("Saturday", stageNames[7], "Noizu", 1395, 1470));
		schedule.add(new ScheduleEntry("Saturday", stageNames[7], "Wax Motif", 1470, 1545));
		schedule.add(new ScheduleEntry("Saturday", stageNames[7], "CID", 1545, 1620));
		schedule.add(new ScheduleEntry("Saturday", stageNames[7], "HNTR", 1620, 1695));
		schedule.add(new ScheduleEntry("Saturday", stageNames[7], "Bolo the DJ (Sunrise Set)", 1695, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[8], "Player Dave", 1140, 1200));
		schedule.add(new ScheduleEntry("Saturday", stageNames[8], "Spray", 1200, 1260));
		schedule.add(new ScheduleEntry("Saturday", stageNames[8], "BASHKKA B2B Sedef Adasi", 1260, 1350));
		schedule.add(new ScheduleEntry("Saturday", stageNames[8], "HAAi B2B Luke Alessi", 1350, 1440));
		schedule.add(new ScheduleEntry("Saturday", stageNames[8], "MCR-T", 1440, 1515));
		schedule.add(new ScheduleEntry("Saturday", stageNames[8], "Bad Boombox B2B Ollie Lishman", 1515, 1590));
		schedule.add(new ScheduleEntry("Saturday", stageNames[8], "Benwal", 1590, 1650));
		schedule.add(new ScheduleEntry("Saturday", stageNames[8], "BAUGRUPPE90", 1650, 1710));
		schedule.add(new ScheduleEntry("Saturday", stageNames[8], "Club Angel", 1710, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[9], "PHNX", 1140, 1230));
		schedule.add(new ScheduleEntry("Saturday", stageNames[9], "DJ Nightstalker", 1230, 1320));
		schedule.add(new ScheduleEntry("Saturday", stageNames[9], "CRS?", 1320, 1410));
		schedule.add(new ScheduleEntry("Saturday", stageNames[9], "Fury", 1410, 1500));
		schedule.add(new ScheduleEntry("Saturday", stageNames[9], "Airglo", 1500, 1590));
		schedule.add(new ScheduleEntry("Saturday", stageNames[9], "Koji Aiken", 1590, 1680));
		schedule.add(new ScheduleEntry("Saturday", stageNames[9], "FALKO", 1680, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[10], "DJ Lucky Lou", 1140, 1230));
		schedule.add(new ScheduleEntry("Saturday", stageNames[10], "Luke Alexander", 1230, 1320));
		schedule.add(new ScheduleEntry("Saturday", stageNames[10], "Alves", 1320, 1410));
		schedule.add(new ScheduleEntry("Saturday", stageNames[10], "DREYA V", 1410, 1500));
		schedule.add(new ScheduleEntry("Saturday", stageNames[10], "Trace", 1500, 1590));
		schedule.add(new ScheduleEntry("Saturday", stageNames[10], "Eli Fola", 1590, 1680));
		schedule.add(new ScheduleEntry("Saturday", stageNames[10], "Skilah", 1680, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[11], "DJ Flite", 1140, 1230));
		schedule.add(new ScheduleEntry("Saturday", stageNames[11], "Tony Gamboa", 1230, 1305));
		schedule.add(new ScheduleEntry("Saturday", stageNames[11], "Reid Speed", 1305, 1380));
		schedule.add(new ScheduleEntry("Saturday", stageNames[11], "Fester B3B Oscure B3B Thee-O", 1380, 1530));
		schedule.add(new ScheduleEntry("Saturday", stageNames[11], "Mellinfunk", 1530, 1605));
		schedule.add(new ScheduleEntry("Saturday", stageNames[11], "Jon Bishop", 1605, 1680));
		schedule.add(new ScheduleEntry("Saturday", stageNames[11], "Eric Davenport", 1680, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[12], "AJ Ball", 1140, 1185));
		schedule.add(new ScheduleEntry("Saturday", stageNames[12], "Linney", 1185, 1245));
		schedule.add(new ScheduleEntry("Saturday", stageNames[12], "Justus", 1245, 1305));
		schedule.add(new ScheduleEntry("Saturday", stageNames[12], "Matt Faulk", 1365, 1440));
		schedule.add(new ScheduleEntry("Saturday", stageNames[12], "Polar Bears", 1440, 1500));
		schedule.add(new ScheduleEntry("Saturday", stageNames[12], "GRAVAGERZ", 1500, 1560));
		schedule.add(new ScheduleEntry("Saturday", stageNames[12], "Odd Language", 1560, 1620));
		schedule.add(new ScheduleEntry("Saturday", stageNames[12], "Damon Sharpe", 1620, 1680));
		schedule.add(new ScheduleEntry("Saturday", stageNames[12], "Micah Martin", 1680, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[13], "n0where g1rl", 1140, 1215));
		schedule.add(new ScheduleEntry("Saturday", stageNames[13], "ANDROCIDED", 1215, 1290));
		schedule.add(new ScheduleEntry("Saturday", stageNames[13], "love2hate", 1290, 1365));
		schedule.add(new ScheduleEntry("Saturday", stageNames[13], "fluttershy", 1365, 1440));
		schedule.add(new ScheduleEntry("Saturday", stageNames[13], "DJ Flapjack", 1440, 1530));
		schedule.add(new ScheduleEntry("Saturday", stageNames[13], "Jimni Cricket", 1530, 1620));
		schedule.add(new ScheduleEntry("Saturday", stageNames[13], "Vulkauf", 1620, 1695));
		schedule.add(new ScheduleEntry("Saturday", stageNames[13], "Puking Spice Latte", 1695, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[14], "Carneyval", 1140, 1200));
		schedule.add(new ScheduleEntry("Saturday", stageNames[14], "Roxy Ferrari", 1245, 1305));
		schedule.add(new ScheduleEntry("Saturday", stageNames[14], "Dazy Duke", 1335, 1395));
		schedule.add(new ScheduleEntry("Saturday", stageNames[14], "MADDS", 1440, 1500));
		schedule.add(new ScheduleEntry("Saturday", stageNames[14], "Slim McGraw", 1530, 1590));
		schedule.add(new ScheduleEntry("Saturday", stageNames[14], "Taylor Kade", 1640, 1700));
		schedule.add(new ScheduleEntry("Saturday", stageNames[15], "Gusta", 1140, 1215));
		schedule.add(new ScheduleEntry("Saturday", stageNames[15], "Raje", 1215, 1290));
		schedule.add(new ScheduleEntry("Saturday", stageNames[15], "Benni Ola", 1290, 1365));
		schedule.add(new ScheduleEntry("Saturday", stageNames[15], "Player Dave", 1365, 1440));
		schedule.add(new ScheduleEntry("Saturday", stageNames[15], "CANARY YELLOW", 1440, 1530));
		schedule.add(new ScheduleEntry("Saturday", stageNames[15], "Samson", 1530, 1620));
		schedule.add(new ScheduleEntry("Saturday", stageNames[15], "Mattilo", 1620, 1695));
		schedule.add(new ScheduleEntry("Saturday", stageNames[15], "Valentina Soto", 1695, 1770));
		schedule.add(new ScheduleEntry("Saturday", stageNames[16], "Espii", 1140, 1230));
		schedule.add(new ScheduleEntry("Saturday", stageNames[16], "Mateezy", 1230, 1320));
		schedule.add(new ScheduleEntry("Saturday", stageNames[16], "Eve Lauren", 1320, 1410));
		schedule.add(new ScheduleEntry("Saturday", stageNames[16], "Off in LALA", 1410, 1500));
		schedule.add(new ScheduleEntry("Saturday", stageNames[16], "Abana", 1500, 1590));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "Trace", 1140, 1200));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "Ship Wrek", 1200, 1260));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "Layton Giordani", 1260, 1320));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "OPENING CEREMONY: KINETIC LAUNCH", 1320, 1327));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "Funk Tribu", 1327, 1395));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1395, 1399));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "GRiZ B2B Wooli", 1399, 1468));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1468, 1472));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "Zedd", 1472, 1540));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "MAIN FIREWORKS", 1540, 1547));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "Martin Garrix", 1547, 1617));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1617, 1621));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "Cloonee", 1621, 1690));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "INTERMEZZO: KINETIC JOURNEY", 1690, 1694));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "Armin van Buuren (Sunrise Set)", 1694, 1768));
		schedule.add(new ScheduleEntry("Sunday", stageNames[0], "CLOSING CEREMONY: CELEBRATION", 1768, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[1], "GRAVAGERZ", 1140, 1200));
		schedule.add(new ScheduleEntry("Sunday", stageNames[1], "Nostalgix", 1200, 1260));
		schedule.add(new ScheduleEntry("Sunday", stageNames[1], "William Black", 1260, 1320));
		schedule.add(new ScheduleEntry("Sunday", stageNames[1], "San Holo (Wholesome Riddim Set)", 1320, 1380));
		schedule.add(new ScheduleEntry("Sunday", stageNames[1], "Dabin", 1380, 1445));
		schedule.add(new ScheduleEntry("Sunday", stageNames[1], "Alison Wonderland", 1445, 1505));
		schedule.add(new ScheduleEntry("Sunday", stageNames[1], "Seven Lions", 1505, 1580));
		schedule.add(new ScheduleEntry("Sunday", stageNames[1], "Restricted", 1580, 1640));
		schedule.add(new ScheduleEntry("Sunday", stageNames[1], "Black Tiger Sex Machine", 1640, 1710));
		schedule.add(new ScheduleEntry("Sunday", stageNames[1], "Nico Moreno B2B Holy Priest", 1710, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[2], "Linska", 1140, 1230));
		schedule.add(new ScheduleEntry("Sunday", stageNames[2], "ANNA", 1230, 1320));
		schedule.add(new ScheduleEntry("Sunday", stageNames[2], "Beltran", 1320, 1410));
		schedule.add(new ScheduleEntry("Sunday", stageNames[2], "Chris Stussy", 1410, 1500));
		schedule.add(new ScheduleEntry("Sunday", stageNames[2], "Solomun", 1500, 1590));
		schedule.add(new ScheduleEntry("Sunday", stageNames[2], "Vintage Culture", 1590, 1680));
		schedule.add(new ScheduleEntry("Sunday", stageNames[2], "Kevin de Vries", 1680, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[3], "Bad Beat", 1140, 1215));
		schedule.add(new ScheduleEntry("Sunday", stageNames[3], "Frankie Bones", 1215, 1290));
		schedule.add(new ScheduleEntry("Sunday", stageNames[3], "Adiel", 1290, 1370));
		schedule.add(new ScheduleEntry("Sunday", stageNames[3], "DJ Gigola", 1370, 1450));
		schedule.add(new ScheduleEntry("Sunday", stageNames[3], "999999999", 1450, 1530));
		schedule.add(new ScheduleEntry("Sunday", stageNames[3], "Indira Paganotto", 1530, 1610));
		schedule.add(new ScheduleEntry("Sunday", stageNames[3], "KI/KI", 1610, 1690));
		schedule.add(new ScheduleEntry("Sunday", stageNames[3], "Klangkuenstler", 1690, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "DJ Nightstalker w/ MC Dino", 1140, 1190));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "Sippy", 1190, 1240));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "EAZYBAKED", 1240, 1290));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "INFEKT B2B SampliFire", 1290, 1350));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "A.M.C w/ Phantom", 1350, 1410));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "Virtual Riot", 1410, 1470));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "PEEKABOO", 1470, 1530));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "Ahee B2B Liquid Stranger", 1530, 1590));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "Whethan", 1590, 1650));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "Boogie T B2B DISTINCT MOTIVE", 1650, 1710));
		schedule.add(new ScheduleEntry("Sunday", stageNames[4], "ÆON:MODE (Sunrise Set)", 1710, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[5], "Sihk", 1140, 1230));
		schedule.add(new ScheduleEntry("Sunday", stageNames[5], "CLAWZ", 1230, 1290));
		schedule.add(new ScheduleEntry("Sunday", stageNames[5], "The Purge", 1290, 1350));
		schedule.add(new ScheduleEntry("Sunday", stageNames[5], "Yosuf", 1350, 1410));
		schedule.add(new ScheduleEntry("Sunday", stageNames[5], "DJ Isaac", 1410, 1470));
		schedule.add(new ScheduleEntry("Sunday", stageNames[5], "Vieze Asbak", 1470, 1530));
		schedule.add(new ScheduleEntry("Sunday", stageNames[5], "Sub Zero Project", 1530, 1590));
		schedule.add(new ScheduleEntry("Sunday", stageNames[5], "Rooler", 1590, 1650));
		schedule.add(new ScheduleEntry("Sunday", stageNames[5], "Warface", 1650, 1710));
		schedule.add(new ScheduleEntry("Sunday", stageNames[5], "MADGRRL B2B VESSEL", 1710, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[6], "Warung", 1140, 1200));
		schedule.add(new ScheduleEntry("Sunday", stageNames[6], "Shingo Nakamura", 1200, 1260));
		schedule.add(new ScheduleEntry("Sunday", stageNames[6], "Rebūke", 1260, 1320));
		schedule.add(new ScheduleEntry("Sunday", stageNames[6], "Cristoph", 1320, 1380));
		schedule.add(new ScheduleEntry("Sunday", stageNames[6], "Eli & Fur", 1380, 1440));
		schedule.add(new ScheduleEntry("Sunday", stageNames[6], "Tinlicker (DJ Set)", 1440, 1500));
		schedule.add(new ScheduleEntry("Sunday", stageNames[6], "Cassian", 1500, 1575));
		schedule.add(new ScheduleEntry("Sunday", stageNames[6], "Massano", 1575, 1650));
		schedule.add(new ScheduleEntry("Sunday", stageNames[6], "Innellea", 1650, 1710));
		schedule.add(new ScheduleEntry("Sunday", stageNames[6], "KREAM", 1710, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[7], "Klo", 1140, 1200));
		schedule.add(new ScheduleEntry("Sunday", stageNames[7], "Murphy's Law", 1200, 1275));
		schedule.add(new ScheduleEntry("Sunday", stageNames[7], "Sidney Charles B2B Bushbaby", 1275, 1350));
		schedule.add(new ScheduleEntry("Sunday", stageNames[7], "Skream", 1350, 1425));
		schedule.add(new ScheduleEntry("Sunday", stageNames[7], "Hamdi", 1425, 1500));
		schedule.add(new ScheduleEntry("Sunday", stageNames[7], "Chris Lorenzo B2B bullet tooth", 1500, 1575));
		schedule.add(new ScheduleEntry("Sunday", stageNames[7], "Silva Bumpa", 1575, 1650));
		schedule.add(new ScheduleEntry("Sunday", stageNames[7], "Morgan Seatree", 1650, 1710));
		schedule.add(new ScheduleEntry("Sunday", stageNames[7], "Lu.Re", 1710, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[8], "Alves", 1140, 1230));
		schedule.add(new ScheduleEntry("Sunday", stageNames[8], "Isabella", 1230, 1350));
		schedule.add(new ScheduleEntry("Sunday", stageNames[8], "KinAhau", 1350, 1440));
		schedule.add(new ScheduleEntry("Sunday", stageNames[8], "Tiga", 1440, 1530));
		schedule.add(new ScheduleEntry("Sunday", stageNames[8], "DJ Tennis B2B Red Axes", 1530, 1650));
		schedule.add(new ScheduleEntry("Sunday", stageNames[8], "Beltran B2B Simas", 1650, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[9], "Connor Q", 1140, 1230));
		schedule.add(new ScheduleEntry("Sunday", stageNames[9], "Fallen", 1230, 1320));
		schedule.add(new ScheduleEntry("Sunday", stageNames[9], "Rohaan (DnB Set)", 1320, 1410));
		schedule.add(new ScheduleEntry("Sunday", stageNames[9], "Justin Hawkes", 1410, 1500));
		schedule.add(new ScheduleEntry("Sunday", stageNames[9], "AK1200", 1500, 1590));
		schedule.add(new ScheduleEntry("Sunday", stageNames[9], "APX1", 1590, 1680));
		schedule.add(new ScheduleEntry("Sunday", stageNames[9], "Scooba", 1680, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[10], "Espii", 1140, 1200));
		schedule.add(new ScheduleEntry("Sunday", stageNames[10], "Soraya", 1200, 1260));
		schedule.add(new ScheduleEntry("Sunday", stageNames[10], "Doozie", 1260, 1320));
		schedule.add(new ScheduleEntry("Sunday", stageNames[10], "BROSA", 1320, 1395));
		schedule.add(new ScheduleEntry("Sunday", stageNames[10], "Juos", 1395, 1530));
		schedule.add(new ScheduleEntry("Sunday", stageNames[10], "Rommii", 1530, 1605));
		schedule.add(new ScheduleEntry("Sunday", stageNames[10], "Close Friends Only", 1605, 1680));
		schedule.add(new ScheduleEntry("Sunday", stageNames[11], "Kendo", 1140, 1230));
		schedule.add(new ScheduleEntry("Sunday", stageNames[11], "Mars", 1230, 1305));
		schedule.add(new ScheduleEntry("Sunday", stageNames[11], "Mark Lewis", 1305, 1380));
		schedule.add(new ScheduleEntry("Sunday", stageNames[11], "Steve Loria", 1380, 1455));
		schedule.add(new ScheduleEntry("Sunday", stageNames[11], "Frankie Bones", 1455, 1530));
		schedule.add(new ScheduleEntry("Sunday", stageNames[11], "Donald Glaude", 1530, 1605));
		schedule.add(new ScheduleEntry("Sunday", stageNames[11], "Charles Feelgood", 1605, 1680));
		schedule.add(new ScheduleEntry("Sunday", stageNames[11], "Mark E Quark", 1680, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[12], "Assembly", 1140, 1200));
		schedule.add(new ScheduleEntry("Sunday", stageNames[12], "Ward Andrews", 1200, 1260));
		schedule.add(new ScheduleEntry("Sunday", stageNames[12], "Neon Deluz", 1260, 1320));
		schedule.add(new ScheduleEntry("Sunday", stageNames[12], "Tensteps", 1320, 1380));
		schedule.add(new ScheduleEntry("Sunday", stageNames[12], "just.shaun", 1380, 1440));
		schedule.add(new ScheduleEntry("Sunday", stageNames[12], "Goshfather", 1440, 1500));
		schedule.add(new ScheduleEntry("Sunday", stageNames[12], "Silver Panda", 1500, 1560));
		schedule.add(new ScheduleEntry("Sunday", stageNames[12], "Nekter", 1560, 1620));
		schedule.add(new ScheduleEntry("Sunday", stageNames[12], "Tim Clark", 1620, 1680));
		schedule.add(new ScheduleEntry("Sunday", stageNames[12], "Synnhaven", 1680, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[13], "Nerz", 1140, 1215));
		schedule.add(new ScheduleEntry("Sunday", stageNames[13], "Kimmycat", 1215, 1290));
		schedule.add(new ScheduleEntry("Sunday", stageNames[13], "Lexxy Jax", 1290, 1365));
		schedule.add(new ScheduleEntry("Sunday", stageNames[13], "Simon Apex (Lost Boy Tribute Set)", 1365, 1440));
		schedule.add(new ScheduleEntry("Sunday", stageNames[13], "Kuta", 1440, 1530));
		schedule.add(new ScheduleEntry("Sunday", stageNames[13], "Lucky Rabbit", 1530, 1620));
		schedule.add(new ScheduleEntry("Sunday", stageNames[13], "Muñequita", 1620, 1695));
		schedule.add(new ScheduleEntry("Sunday", stageNames[13], "2B Happy", 1695, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[14], "John Price", 1140, 1200));
		schedule.add(new ScheduleEntry("Sunday", stageNames[14], "Bad Ash", 1245, 1305));
		schedule.add(new ScheduleEntry("Sunday", stageNames[14], "Sommer Ray", 1335, 1395));
		schedule.add(new ScheduleEntry("Sunday", stageNames[14], "Disko Cowboy", 1440, 1500));
		schedule.add(new ScheduleEntry("Sunday", stageNames[14], "Ashley Walls", 1530, 1590));
		schedule.add(new ScheduleEntry("Sunday", stageNames[14], "Willie Pake", 1640, 1700));
		schedule.add(new ScheduleEntry("Sunday", stageNames[15], "Chassi", 1140, 1215));
		schedule.add(new ScheduleEntry("Sunday", stageNames[15], "Odd Language", 1215, 1290));
		schedule.add(new ScheduleEntry("Sunday", stageNames[15], "Wolfie", 1290, 1365));
		schedule.add(new ScheduleEntry("Sunday", stageNames[15], "CHIBS", 1365, 1440));
		schedule.add(new ScheduleEntry("Sunday", stageNames[15], "2dy4", 1440, 1530));
		schedule.add(new ScheduleEntry("Sunday", stageNames[15], "SQISHI", 1530, 1620));
		schedule.add(new ScheduleEntry("Sunday", stageNames[15], "SISTO", 1620, 1695));
		schedule.add(new ScheduleEntry("Sunday", stageNames[15], "Vicious", 1695, 1770));
		schedule.add(new ScheduleEntry("Sunday", stageNames[16], "Marc V", 1140, 1230));
		schedule.add(new ScheduleEntry("Sunday", stageNames[16], "Raje", 1230, 1320));
		schedule.add(new ScheduleEntry("Sunday", stageNames[16], "Dena", 1320, 1410));
		schedule.add(new ScheduleEntry("Sunday", stageNames[16], "Eric Dlux", 1410, 1500));
		schedule.add(new ScheduleEntry("Sunday", stageNames[16], "Valentina Soto", 1500, 1590));
		schedule.add(new ScheduleEntry("Sunday", stageNames[16], "MTK", 1590, 1680));
	}

	private String formatTime(int totalMinutes) {
		int hour24 = (totalMinutes / 60) % 24;
		int minute = totalMinutes % 60;

		int hour12 = hour24 % 12;
		if (hour12 == 0) {
			hour12 = 12;
		}

		String amPm = hour24 < 12 ? "AM" : "PM";

		return String.format("%d:%02d %s", hour12, minute, amPm);
	}

	// LO8: Write attendee information to a text file.
	private boolean saveAttendees(int id, String name, String email) {

		if (name.contains("\t") || email.contains("\t") || name.contains("\n") || email.contains("\n")
				|| name.contains("\r") || email.contains("\r")) {
			return false;
		}

		try (PrintWriter writer = new PrintWriter(new FileWriter(ATTENDEE_FILE, true))) {

			writer.println(id + "\t" + name + "\t" + email);

			return !writer.checkError();

		} catch (IOException e) {

			System.out.println("Error saving attendees: " + e.getMessage());

			return false;
		}
	}

	// LO8: Read attendee information from a text file.
	private void loadAttendees() {

		File file = new File(ATTENDEE_FILE);

		if (!file.exists()) {
			return;
		}

		try (Scanner scanner = new Scanner(file)) {

			while (scanner.hasNextLine()) {

				String line = scanner.nextLine();
				String[] data = line.split("\t", -1);

				if (data.length == 3) {

					try {
						int id = Integer.parseInt(data[0]);
						String name = data[1];
						String email = data[2];

						Attendee attendee = new Attendee(id, name, email);

						attendees.add(attendee);

						nextAttendeeId = Math.max(nextAttendeeId, id + 1);

					} catch (NumberFormatException e) {
						System.out.println("Invalid attendee ID.");
					}
				}
			}

			System.out.println("Attendees loaded: " + attendees.size());

		} catch (IOException e) {
			System.out.println("Error loading attendees: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new FestivalGUI().setVisible(true);
		});
	}
}