package edu.sdmesa.cisc191;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FestivalGUI extends JFrame {

	private List<Attendee> attendees = new ArrayList<>();

	private Festival festival = new Festival("Electronic Music Festival");

	private int nextAttendeeId = 1;
	private int nextTicketId = 1;

	public FestivalGUI() {

		loadTopDJs();

		generateFestivalSchedule();

		setTitle("Electronic Music Festival Management System");

		setSize(750, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		JPanel panel = new JPanel(new BorderLayout(10, 20));
		panel.setBackground(new Color(20, 15, 55));
		panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

		JLabel title = new JLabel("ELECTRONIC MUSIC FESTIVAL", SwingConstants.CENTER);
		title.setFont(new Font("Arial", Font.BOLD, 25));
		title.setForeground(Color.WHITE);
		panel.add(title, BorderLayout.NORTH);

		JPanel buttons = new JPanel(new GridLayout(3, 2, 20, 20));
		buttons.setOpaque(false);

		String[] menuItems = { "Register Attendee", "Buy Ticket", "View Lineup", "View Schedule", "Search Artist",
				"Exit" };

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

					String name = JOptionPane.showInputDialog(this, "Enter attendee name:");

					if (name != null && !name.trim().isEmpty()) {

						String email = JOptionPane.showInputDialog(this, "Enter attendee email:");

						if (email != null && !email.trim().isEmpty()) {

							Attendee attendee = new Attendee(nextAttendeeId++, name, email);
							attendees.add(attendee);

							JOptionPane.showMessageDialog(this,
									"Registration Successful!\n" + "Name: " + name + "\n" + "Email: " + email);
						}
					}

				} else if (item.equals("Buy Ticket")) {

					String[] ticketTypes = { "General Admission", "VIP" };

					String selectedType = (String) JOptionPane.showInputDialog(this, "Select your ticket type:",
							"Buy Ticket", JOptionPane.QUESTION_MESSAGE, null, ticketTypes, ticketTypes[0]);

					if (selectedType != null) {

						double price = selectedType.equals("VIP") ? 150.00 : 75.00;

						Ticket ticket = new Ticket(nextTicketId++, selectedType, price);

						JOptionPane.showMessageDialog(this,
								"Ticket Purchased Successfully!\n" + "Ticket ID: " + ticket.getTicketId() + "\n"
										+ "Type: " + ticket.getTicketType() + "\n" + "Price: $" + String.format("%.2f", ticket.getPrice()));
					}

				} else if (item.equals("View Lineup")) {

					StringBuilder lineup = new StringBuilder("Festival Lineup:\n\n");

					if (festival.getArtists().isEmpty()) {
						lineup.append("No artists have been added yet.");
					} else {
						for (Artist artist : festival.getArtists()) {
							lineup.append("Artist: ").append(artist.getName()).append("\nGenre: ")
									.append(artist.getGenre()).append("\n\n");
						}
					}

					JTextArea textArea = new JTextArea(lineup.toString());
					textArea.setEditable(false);
					textArea.setFont(new Font("Arial", Font.PLAIN, 14));

					JScrollPane scrollPane = new JScrollPane(textArea);
					scrollPane.setPreferredSize(new Dimension(450, 400));

					JOptionPane.showMessageDialog(this, scrollPane, "Top 75 DJs - Festival Lineup",
							JOptionPane.INFORMATION_MESSAGE);
				} else if (item.equals("Search Artist")) {

					String name = JOptionPane.showInputDialog(this, "Enter DJ name:");

					if (name != null && !name.trim().isEmpty()) {

						Artist artist = festival.searchArtist(name.trim());

						if (artist != null) {
							JOptionPane.showMessageDialog(this, "Artist Found!\n" + "Name: " + artist.getName() + "\n"
									+ "Genre: " + artist.getGenre());
						} else {
							JOptionPane.showMessageDialog(this, "Artist not found: " + name);
						}
					}

					
					
					
					
				} else if (item.equals("View Schedule")) {

					String selectedDay = (String) JOptionPane.showInputDialog(this, "Select festival day:",
							"Festival Schedule", JOptionPane.QUESTION_MESSAGE, null, festivalDays, festivalDays[0]);

					if (selectedDay != null) {

						StringBuilder result = new StringBuilder();
						result.append("FESTIVAL SCHEDULE - ").append(selectedDay)
								.append("\n5:00 PM - 07:00 AM  NEXT MORNING\n\n");

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

						JOptionPane.showMessageDialog(this, scrollPane, selectedDay + " Festival Schedule",
								JOptionPane.INFORMATION_MESSAGE);
					}

				}
			});

			buttons.add(button);
		}

		panel.add(buttons, BorderLayout.CENTER);
		add(panel);
	}

	private void loadTopDJs() {

		String[][] topDJs = { { "David Guetta", "Dance" }, { "Martin Garrix", "Progressive House" },
				{ "Anyma", "Melodic Techno" }, { "Alok", "Brazilian Bass" },
				{ "Dimitri Vegas & Like Mike", "Big Room" }, { "Charlotte de Witte", "Techno" },
				{ "Armin van Buuren", "Trance" }, { "Timmy Trumpet", "Big Room" }, { "FISHER", "Tech House" },
				{ "Vintage Culture", "House" }, { "Calvin Harris", "Dance" }, { "Don Diablo", "Future House" },
				{ "Peggy Gou", "House" }, { "Black Coffee", "Afro House" }, { "Afrojack", "Big Room" },
				{ "Fred again..", "Electronic" }, { "Lost Frequencies", "Deep House" }, { "Steve Aoki", "EDM" },
				{ "KSHMR", "Big Room" }, { "W&W", "Big Room" }, { "Reinier Zonneveld", "Techno" }, { "Tiësto", "EDM" },
				{ "Carl Cox", "Techno" }, { "John Summit", "Tech House" }, { "Skrillex", "Dubstep" },
				{ "Indira Paganotto", "Psytrance" }, { "Nicky Romero", "Progressive House" },
				{ "The Martinez Brothers", "House" }, { "Hardwell", "Big Room" }, { "Illenium", "Future Bass" },
				{ "Keinemusik", "Afro House" }, { "Alan Walker", "Electronic" }, { "Jamie Jones", "Tech House" },
				{ "Swedish House Mafia", "Progressive House" }, { "Paul van Dyk", "Trance" },
				{ "Korolova", "Melodic Techno" }, { "Michael Bibi", "Tech House" }, { "Dom Dolla", "Tech House" },
				{ "Oliver Heldens", "Future House" }, { "Josh Baker", "House" }, { "Solomun", "Melodic House" },
				{ "Mahmut Orhan", "Deep House" }, { "Claptone", "House" }, { "Joel Corry", "Dance" },
				{ "R3HAB", "EDM" }, { "Mochakk", "House" }, { "Amelie Lens", "Techno" }, { "HUGEL", "Latin House" },
				{ "Lilly Palmer", "Techno" }, { "GORDO", "House" }, { "James Hype", "Tech House" },
				{ "Maddix", "Techno" }, { "Marshmello", "EDM" }, { "Zedd", "Electro House" }, { "Mau P", "Tech House" },
				{ "Alesso", "Progressive House" }, { "Boris Brejcha", "Minimal Techno" }, { "Bassjackers", "Big Room" },
				{ "CHRIS STASSY", "House" }, { "DJ Snake", "EDM" }, { "Miss Monique", "Progressive House" },
				{ "Eric Prydz", "Progressive House" }, { "ATB", "Trance" }, { "Sara Landry", "Hard Techno" },
				{ "Kölsch", "Melodic Techno" }, { "Ben Nicky", "Trance" }, { "Sub Zero Project", "Hardstyle" },
				{ "Joseph Capriati", "Techno" }, { "Liu", "Brazilian Bass" }, { "ARTBAT", "Melodic Techno" },
				{ "Vini Vici", "Psytrance" }, { "Wade", "Tech House" }, { "Nora En Pure", "Deep House" },
				{ "B Jones", "EDM" }, { "Shimza", "Afro House" } };

		for (int i = 0; i < topDJs.length; i++) {
			Artist artist = new Artist(i + 1, topDJs[i][0], topDJs[i][1]);

			festival.addArtist(artist);
		}
	}

	private final String[] stageNames = { "House Pineapple", "Techno Vault", "Remote Box", "Bass Reactor",
			"Trance Horizon", "Afterdark Arena", "Electric Paradise", "Cyber Galaxy", "Rhythm Factory",
			"Solar Frequency", "Digital Dreams", "Underground Empire", "Skyline Beats", "Infinity Sound",
			"Midnight Odyssey" };

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

		List<Artist> allDJs = new ArrayList<>(festival.getArtists());

		if (allDJs.size() != 75) {
			throw new IllegalStateException("Expected 75 DJs, found " + allDJs.size());
		}

		Random random = new Random();

		List<Artist> headliners = new ArrayList<>(allDJs.subList(0, 15));

		List<Artist> otherDJs = new ArrayList<>(allDJs.subList(15, 75));

		for (String day : festivalDays) {

			Collections.shuffle(headliners, random);
			int mainStart = 17 * 60;

			// Top 15 DJs perform at House Pineapple.

			for (Artist dj : headliners) {
				schedule.add(new ScheduleEntry(day, "House Pineapple", dj.getName(), mainStart, mainStart + 55));

				mainStart += 56;
			}

			Collections.shuffle(otherDJs, random);

			// Remaining 60 DJs across 14 stages.
			for (int i = 0; i < otherDJs.size(); i++) {

				int stageIndex = 1 + (i % 14);
				int slotIndex = i / 14;

				int start = 17 * 60 + slotIndex * 60;
				start += random.nextInt(11);

				schedule.add(
						new ScheduleEntry(day, stageNames[stageIndex], otherDJs.get(i).getName(), start, start + 50));
			}
		}
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

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new FestivalGUI().setVisible(true);
		});
	}
}