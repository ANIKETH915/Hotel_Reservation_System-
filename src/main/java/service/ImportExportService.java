package service;

import dao.BookingDao;
import dao.CustomerDao;
import dao.RoomDao;
import model.Booking;
import model.Customer;
import model.Room;
import model.RoomStatus;
import model.RoomType;
import utils.CsvUtil;
import utils.ValidationUtil;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ImportExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RoomDao roomDao = new RoomDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final BookingDao bookingDao = new BookingDao();

    public void exportRooms(Path path) throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"room_number", "room_type", "floor", "price", "capacity", "status", "image_path"});
        for (Room room : roomDao.findAll()) {
            rows.add(new String[]{
                    room.getRoomNumber(),
                    room.getRoomType().getLabel(),
                    String.valueOf(room.getFloor()),
                    room.getPrice().toPlainString(),
                    String.valueOf(room.getCapacity()),
                    room.getStatus().getLabel(),
                    room.getImagePath() != null ? room.getImagePath() : ""
            });
        }
        CsvUtil.write(path, rows);
    }

    public void exportCustomers(Path path) throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"full_name", "email", "phone", "address", "id_proof"});
        for (Customer customer : customerDao.findAll()) {
            rows.add(new String[]{
                    customer.getFullName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    customer.getAddress() != null ? customer.getAddress() : "",
                    customer.getIdProof() != null ? customer.getIdProof() : ""
            });
        }
        CsvUtil.write(path, rows);
    }

    public void exportBookings(Path path) throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{
                "booking_id", "customer_name", "room_number", "room_type",
                "check_in", "check_out", "days", "total_amount",
                "booking_status", "payment_status", "created_at"
        });
        for (Booking booking : bookingDao.findAll()) {
            rows.add(new String[]{
                    String.valueOf(booking.getBookingId()),
                    booking.getCustomerName(),
                    booking.getRoomNumber(),
                    booking.getRoomType(),
                    booking.getCheckIn().format(DATE_FMT),
                    booking.getCheckOut().format(DATE_FMT),
                    String.valueOf(booking.getDays()),
                    booking.getTotalAmount().toPlainString(),
                    booking.getBookingStatus().getLabel(),
                    booking.getPaymentStatus().getLabel(),
                    booking.getCreatedAt() != null ? booking.getCreatedAt().format(DATE_TIME_FMT) : ""
            });
        }
        CsvUtil.write(path, rows);
    }

    public int importRooms(Path path) throws Exception {
        List<String[]> rows = CsvUtil.read(path);
        if (rows.isEmpty()) {
            return 0;
        }
        int start = isHeader(rows.get(0), "room_number") ? 1 : 0;
        int imported = 0;
        for (int i = start; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length < 6) {
                throw new IllegalArgumentException("Invalid room row at line " + (i + 1));
            }
            Room room = new Room();
            room.setRoomNumber(ValidationUtil.require(row[0], "Room number"));
            room.setRoomType(RoomType.fromLabel(row[1].trim()));
            room.setFloor(Integer.parseInt(row[2].trim()));
            room.setPrice(new BigDecimal(row[3].trim()));
            room.setCapacity(Integer.parseInt(row[4].trim()));
            room.setStatus(RoomStatus.fromLabel(row[5].trim()));
            if (row.length > 6 && !ValidationUtil.isBlank(row[6])) {
                room.setImagePath(row[6].trim());
            }
            if (room.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Invalid price at line " + (i + 1));
            }
            if (room.getCapacity() <= 0) {
                throw new IllegalArgumentException("Invalid capacity at line " + (i + 1));
            }
            roomDao.insert(room);
            imported++;
        }
        return imported;
    }

    public int importCustomers(Path path) throws Exception {
        List<String[]> rows = CsvUtil.read(path);
        if (rows.isEmpty()) {
            return 0;
        }
        int start = isHeader(rows.get(0), "full_name") ? 1 : 0;
        int imported = 0;
        for (int i = start; i < rows.size(); i++) {
            String[] row = rows.get(i);
            if (row.length < 3) {
                throw new IllegalArgumentException("Invalid customer row at line " + (i + 1));
            }
            Customer customer = new Customer();
            customer.setFullName(ValidationUtil.require(row[0], "Full name"));
            customer.setEmail(ValidationUtil.require(row[1], "Email"));
            customer.setPhone(ValidationUtil.require(row[2], "Phone"));
            if (!ValidationUtil.isValidEmail(customer.getEmail())) {
                throw new IllegalArgumentException("Invalid email at line " + (i + 1));
            }
            if (!ValidationUtil.isValidPhone(customer.getPhone())) {
                throw new IllegalArgumentException("Invalid phone at line " + (i + 1));
            }
            if (row.length > 3) {
                customer.setAddress(row[3]);
            }
            if (row.length > 4) {
                customer.setIdProof(row[4]);
            }
            customerDao.insert(customer);
            imported++;
        }
        return imported;
    }

    private boolean isHeader(String[] row, String firstColumn) {
        return row.length > 0 && firstColumn.equalsIgnoreCase(row[0].trim());
    }
}
