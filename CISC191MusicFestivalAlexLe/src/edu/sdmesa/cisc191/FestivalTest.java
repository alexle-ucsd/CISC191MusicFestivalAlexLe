package edu.sdmesa.cisc191;

/**
 * Tests the Electronic Music Festival classes. Author: Alex Le CISC 191 - Week
 * 2 Project
 */
public class FestivalTest {

	public static void main(String[] args) {

		Festival festival = new Festival("Electronic Music Festival");

		Attendee attendee = new Attendee(1, "Alex Le", "alex@example.com");

		Ticket ticket = new Ticket(101, "VIP", 250.00);

		attendee.setTicket(ticket);
		festival.addAttendee(attendee);

		Artist artist = new Artist(1, "DJ Alex", "EDM");

		Stage stage = new Stage(1, "Main Stage");

		Performance performance = new Performance(1, "November 20, 2026 at 8 PM", artist, stage);

		festival.addArtist(artist);
		festival.addStage(stage);
		festival.addPerformance(performance);

		System.out.println("=== Attendee Ticket ===");
		attendee.viewTicket();

		System.out.println("\n=== Artist ===");
		artist.displayArtist();

		System.out.println("\n=== Festival Schedule ===");
		festival.displaySchedule();

		System.out.println("\n=== Search Artist ===");
		Artist found = festival.searchArtist("DJ Alex");

		if (found != null) {
			System.out.println("Found: " + found.getName());
		} else {
			System.out.println("Artist not found.");
		}
	}
}
