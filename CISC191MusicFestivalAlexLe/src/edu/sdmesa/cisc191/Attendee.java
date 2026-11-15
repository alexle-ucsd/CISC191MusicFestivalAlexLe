
package edu.sdmesa.cisc191;

/**
 * Represents an attendee at the music festival.
 * Author: Alex Le
 * CISC 191 - Week 2 Project
 */
public class Attendee {

    private int attendeeId;
    private String name;
    private String email;
    private Ticket ticket;

    public Attendee(int attendeeId, String name, String email) {
        this.attendeeId = attendeeId;
        this.name = name;
        this.email = email;
    }

    public int getAttendeeId() {
        return attendeeId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void updateInfo(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void viewTicket() {
        if (ticket != null) {
            ticket.displayTicket();
        } else {
            System.out.println("No ticket assigned.");
        }
    }
}

