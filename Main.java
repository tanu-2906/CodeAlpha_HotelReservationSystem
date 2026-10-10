package HotelReservationSystem;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final HotelService service = new HotelService();

    public static void main(String[] args) {
        System.out.println("==================================");
        System.out.println("   HOTEL RESERVATION SYSTEM");
        System.out.println("==================================");

        boolean running = true;

        while (running) {
            System.out.println("\n1. View all rooms");
            System.out.println("2. Search available rooms");
            System.out.println("3. Book a room");
            System.out.println("4. View booking details");
            System.out.println("5. Cancel reservation");
            System.out.println("6. View all reservations");
            System.out.println("7. Exit");

            int choice = readInt("Choose an option: ");

            try {
                switch (choice) {
                    case 1 -> viewRooms();
                    case 2 -> searchRooms();
                    case 3 -> bookRoom();
                    case 4 -> viewBooking();
                    case 5 -> cancelBooking();
                    case 6 -> viewAllBookings();
                    case 7 -> {
                        running = false;
                        System.out.println("Thank you for visiting!");
                    }
                    default -> System.out.println("Choose an option from 1 to 7.");
                }
            } catch (IOException e) {
                System.out.println("File storage error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        sc.close();
    }

    private static void viewRooms() {
        System.out.println("\n--- ALL HOTEL ROOMS ---");
        for (Room room : service.getRooms()) {
            room.display();
        }
        System.out.println("Categories: Standard, Deluxe, Suite");
    }

    private static void searchRooms() {
        System.out.println("\n--- SEARCH ROOMS ---");
        String category = readCategory(true);
        LocalDate[] dates = readDates();

        List<Room> available = service.searchRooms(
                category, dates[0], dates[1]);

        if (available.isEmpty()) {
            System.out.println("No rooms available for these dates.");
            return;
        }

        System.out.println("Available rooms:");
        for (Room room : available) {
            room.display();
        }
    }

    private static void bookRoom() throws IOException {
        System.out.println("\n--- BOOK A ROOM ---");
        String guest = readText("Guest full name: ");
        String category = readCategory(false);
        LocalDate[] dates = readDates();

        List<Room> available = service.searchRooms(
                category, dates[0], dates[1]);

        if (available.isEmpty()) {
            System.out.println("No rooms available for those dates.");
            return;
        }

        System.out.println("\nAvailable rooms:");
        for (Room room : available) {
            room.display();
        }

        int roomNumber = readInt("Enter room number to book: ");

        Room selected = null;
        for (Room room : available) {
            if (room.getRoomNumber() == roomNumber) {
                selected = room;
                break;
            }
        }

        if (selected == null) {
            System.out.println("Please choose a room from the available list.");
            return;
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                dates[0], dates[1]);
        double total = nights * selected.getPricePerNight();

        System.out.println("\n--- BOOKING SUMMARY ---");
        System.out.println("Guest: " + guest);
        System.out.println("Room: " + selected.getRoomNumber()
                + " (" + selected.getCategory() + ")");
        System.out.println("Nights: " + nights);
        System.out.printf("Total payment: Rs. %.2f%n", total);

        System.out.println("\nSIMULATED PAYMENT");
        System.out.println("1. Simulate payment success");
        System.out.println("2. Simulate payment failure");

        int paymentChoice = readInt("Choose payment result: ");

        if (paymentChoice != 1 && paymentChoice != 2) {
            System.out.println("Invalid payment option. Booking not created.");
            return;
        }

        boolean paymentSuccessful = paymentChoice == 1;

        if (!paymentSuccessful) {
            System.out.println("Payment failed. No reservation was created.");
            return;
        }

        Reservation reservation = service.bookRoom(
                guest, roomNumber, dates[0], dates[1], true);

        System.out.println("\nPayment successful!");
        System.out.println("Booking confirmed. Your booking ID is: "
                + reservation.getBookingId());
        reservation.display();
    }

    private static void viewBooking() {
        System.out.println("\n--- BOOKING DETAILS ---");
        String id = readText("Enter booking ID: ");
        Reservation r = service.findBooking(id);

        if (r == null) {
            System.out.println("Booking not found.");
        } else {
            r.display();
        }
    }

    private static void cancelBooking() throws IOException {
        System.out.println("\n--- CANCEL RESERVATION ---");
        String id = readText("Enter booking ID: ");
        Reservation r = service.findBooking(id);

        if (r == null) {
            System.out.println("Booking not found.");
            return;
        }

        r.display();

        if (!r.getStatus().equals("CONFIRMED")) {
            System.out.println("This reservation is already cancelled.");
            return;
        }

        String confirm = readText("Confirm cancellation? (yes/no): ");

        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Cancellation aborted.");
            return;
        }

        service.cancelBooking(id);
        System.out.println("Reservation cancelled successfully.");
        System.out.println("The room is available again for those dates.");
        System.out.println("Demo note: payment refunds are not processed.");
    }

    private static void viewAllBookings() {
        System.out.println("\n--- ALL RESERVATIONS ---");

        if (service.getReservations().isEmpty()) {
            System.out.println("No bookings have been made yet.");
            return;
        }

        for (Reservation r : service.getReservations()) {
            r.display();
        }
    }

    private static String readCategory(boolean allowAll) {
        while (true) {
            System.out.println("Categories: Standard, Deluxe, Suite"
                    + (allowAll ? ", All" : ""));
            String category = readText("Enter category: ");

            if (category.equalsIgnoreCase("Standard")) return "Standard";
            if (category.equalsIgnoreCase("Deluxe")) return "Deluxe";
            if (category.equalsIgnoreCase("Suite")) return "Suite";
            if (allowAll && category.equalsIgnoreCase("All")) return "All";

            System.out.println("Invalid category. Try again.");
        }
    }

    private static LocalDate[] readDates() {
        while (true) {
            try {
                LocalDate checkIn = LocalDate.parse(
                        readText("Check-in date (YYYY-MM-DD): "));
                LocalDate checkOut = LocalDate.parse(
                        readText("Check-out date (YYYY-MM-DD): "));

                if (checkIn.isBefore(LocalDate.now())
                        || !checkOut.isAfter(checkIn)) {
                    System.out.println(
                            "Check-in must be today or later, and check-out must be after check-in.");
                    continue;
                }

                return new LocalDate[]{checkIn, checkOut};
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use YYYY-MM-DD, e.g. 2026-12-20.");
            }
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readText(prompt));
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
}