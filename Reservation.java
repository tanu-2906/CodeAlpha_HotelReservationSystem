package HotelReservationSystem;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private final String bookingId;
    private final String guestName;
    private final int roomNumber;
    private final String category;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final double totalAmount;
    private final String paymentStatus;
    private String status;

    public Reservation(String bookingId, String guestName,
                       int roomNumber, String category,
                       LocalDate checkIn, LocalDate checkOut,
                       double totalAmount, String paymentStatus,
                       String status) {
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.category = category;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }
    public String getGuestName() { return guestName; }
    public int getRoomNumber() { return roomNumber; }
    public String getCategory() { return category; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getStatus() { return status; }

    public void cancel() {
        status = "CANCELLED";
    }

    public long getNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public String toFileLine() {
        return String.join("|",
            bookingId, guestName, String.valueOf(roomNumber),
            category, checkIn.toString(), checkOut.toString(),
            String.valueOf(totalAmount), paymentStatus, status
        );
    }

    public static Reservation fromFileLine(String line) {
        String[] p = line.split("\\|", -1);

        if (p.length != 9) {
            throw new IllegalArgumentException("Invalid booking record");
        }

        return new Reservation(
            p[0], p[1], Integer.parseInt(p[2]), p[3],
            LocalDate.parse(p[4]), LocalDate.parse(p[5]),
            Double.parseDouble(p[6]), p[7], p[8]
        );
    }

    public void display() {
        System.out.println("----------------------------------------");
        System.out.println("Booking ID   : " + bookingId);
        System.out.println("Guest        : " + guestName);
        System.out.println("Room         : " + roomNumber + " (" + category + ")");
        System.out.println("Check-in     : " + checkIn);
        System.out.println("Check-out    : " + checkOut);
        System.out.println("Nights       : " + getNights());
        System.out.printf("Total amount : Rs. %.2f%n", totalAmount);
        System.out.println("Payment      : " + paymentStatus);
        System.out.println("Status       : " + status);
        System.out.println("----------------------------------------");
    }
}