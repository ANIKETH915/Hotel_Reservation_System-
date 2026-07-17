package model;

import java.math.BigDecimal;

public class Room {
    private int roomId;
    private String roomNumber;
    private RoomType roomType;
    private int floor;
    private BigDecimal price;
    private int capacity;
    private RoomStatus status;
    private String imagePath;

    public Room() {
    }

    public Room(String roomNumber, RoomType roomType, int floor, BigDecimal price, int capacity, RoomStatus status) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.floor = floor;
        this.price = price;
        this.capacity = capacity;
        this.status = status;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return roomNumber + " (" + roomType + ")";
    }
}
