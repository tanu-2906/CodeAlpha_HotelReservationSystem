package HotelReservationSystem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class HotelService {
    private final List<Room> rooms = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final Path file = Paths.get("bookings.txt");

    public HotelService() {
        rooms.add(new Room(101, "Standard", 1500));
        rooms.add(new Room(102, "Standard", 1500));
        rooms.add(new Room(103, "Standard", 1500));
        rooms.add(new Room(201, "Deluxe", 2500));
        rooms.add(new Room(202, "Deluxe", 2500));
        rooms.add(new Room(301, "Suite", 4500));
        rooms.add(new Room(302, "Suite", 4500));

        loadBookings();
    }

    public List<Room> getRooms() {
        return Collections.unmodifiableList(rooms);
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }

    public Reservation findBooking(String id) {
        for (Reservation r : reservations) {
            if (r.getBookingId().equalsIgnoreCase(id.trim())) {
                return r;
            }
        }
        return null;
    }

    public boolean isAvailable(Room room, LocalDate checkIn,
                               LocalDate checkOut) {
        for (Reservation r : reservations) {
            if (r.getRoomNumber() == room.getRoomNumber()
                    && r.getStatus().equals("CONFIRMED")
                    && checkIn.isBefore(r.getCheckOut())
                    && checkOut.isAfter(r.getCheckIn())) {
                return false;
            }
        }
        return true;
    }

    public List<Room> searchRooms(String category, LocalDate checkIn,
                                  LocalDate checkOut) {
        List<Room> available = new ArrayList<>();

        for (Room room : rooms) {
            boolean categoryMatches = category.equalsIgnoreCase("All")
                    || room.getCategory().equalsIgnoreCase(category);

            if (categoryMatches && isAvailable(room, checkIn, checkOut)) {
                available.add(room);
            }
        }
        return available;
    }

    public Reservation bookRoom(String guestName, int roomNumber,
                                LocalDate checkIn, LocalDate checkOut,
                                boolean paymentSuccessful) throws IOException {
        if (guestName == null || guestName.trim().isEmpty()
                || guestName.contains("|")) {
            throw new IllegalArgumentException("Enter a valid guest name.");
        }

        validateDates(checkIn, checkOut);

        Room selected = null;
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                selected = room;
                break;
            }
        }

        if (selected == null) {
            throw new IllegalArgumentException("Room does not exist.");
        }

        if (!isAvailable(selected, checkIn, checkOut)) {
            throw new IllegalArgumentException(
                    "This room is unavailable for those dates.");
        }

        if (!paymentSuccessful) {
            throw new IllegalArgumentException(
                    "Payment failed. Booking was not created.");
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                checkIn, checkOut);
        double total = nights * selected.getPricePerNight();

        Reservation reservation = new Reservation(
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                guestName.trim(), roomNumber, selected.getCategory(),
                checkIn, checkOut, total, "PAID", "CONFIRMED");

        reservations.add(reservation);

        try {
            saveBookings();
        } catch (IOException e) {
            reservations.remove(reservation);
            throw e;
        }

        return reservation;
    }

    public void cancelBooking(String bookingId) throws IOException {
        Reservation r = findBooking(bookingId);

        if (r == null) {
            throw new IllegalArgumentException("Booking ID not found.");
        }

        if (!r.getStatus().equals("CONFIRMED")) {
            throw new IllegalArgumentException(
                    "This booking is already cancelled.");
        }

        r.cancel();
        try {
            saveBookings();
        } catch (IOException e) {
            // Restore the booking status if saving fails.
            // A cancelled reservation must not remain in memory only.
            throw e;
        }
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null
                || checkIn.isBefore(LocalDate.now())
                || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException(
                    "Check-in must be today or later; check-out must be after check-in.");
        }
    }

    private void loadBookings() {
        if (!Files.exists(file)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) continue;

                try {
                    reservations.add(Reservation.fromFileLine(line));
                } catch (RuntimeException ex) {
                    System.err.println("Skipped invalid booking record.");
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load bookings: " + e.getMessage());
        }
    }

    private void saveBookings() throws IOException {
        List<String> lines = new ArrayList<>();

        for (Reservation r : reservations) {
            lines.add(r.toFileLine());
        }

        Files.write(file, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }
}