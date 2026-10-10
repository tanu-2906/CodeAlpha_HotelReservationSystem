package HotelReservationSystem;
public class Room {
    private final int roomNumber;
    private final String category;
    private final double pricePerNight;

    public Room(int roomNumber, String category, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getCategory() {
        return category;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void display() {
        System.out.printf(
            "Room: %d | Category: %-8s | Price/night: Rs. %.2f%n",
            roomNumber, category, pricePerNight
        );
    }
}