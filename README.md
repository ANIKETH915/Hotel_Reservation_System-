# Grand Azure — Hotel Reservation System

Professional Property Management System (PMS) built with **Java Swing**, **JDBC**, and **MySQL 8**.

CodeAlpha Java Programming Internship — **Task 4**.

---

## Project Overview

Grand Azure is a desktop hotel reservation application designed for front-desk and admin use. It covers rooms, guests, bookings, payments, reports, and operational dashboards with a premium navy / royal blue / gold visual language — not a basic Swing demo.

---

## Features

- Dashboard KPIs (rooms, occupancy, revenue, VIP guests, today’s arrivals/departures)
- Room management (CRUD, status, images, availability)
- Customer management (CRUD, search, booking history)
- Booking engine with overlap prevention, check-in / check-out / cancel
- Payment simulation (Cash, UPI, Credit Card, Debit Card, Net Banking)
- Booking receipt & invoice printing
- Reports (daily, monthly, revenue, utilization, customers)
- CSV import / export and database backup
- Dark / light theme, splash screen, admin login, Remember Me, Forgot Password
- Booking calendar, occupancy calendar, booking timeline
- Live clock, toasts, keyboard shortcuts, auto-refresh dashboard

---

## Screenshots

Place captures in the `screenshots/` folder and link them here:

| Dashboard | Bookings | Dark Mode |
|-----------|----------|-----------|
| `screenshots/dashboard.png` | `screenshots/bookings.png` | `screenshots/dark-mode.png` |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17+ |
| UI | Java Swing (custom components) |
| Database | MySQL 8 |
| Access | JDBC + PreparedStatement |
| Architecture | MVC (ui → service → dao → model) |
| Build | Maven (or `javac` + Connector/J) |

**Not used:** JavaFX, Spring Boot, Hibernate, external UI libraries (FlatLaf, etc.).

---

## Prerequisites

- JDK 17 or newer (project verified on JDK 23)
- MySQL 8 Server running locally
- Maven 3.9+ *(optional — see manual run below)*
- Git

---

## Database Setup

1. Start MySQL 8.
2. Edit credentials in `src/main/resources/application.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/hotel_reservation_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
db.user=root
db.password=YOUR_PASSWORD
```

3. You can either:
   - Let the app create the schema on first launch (`DatabaseInitializer`), **or**
   - Import manually:

```bash
mysql -u root -p < database/database.sql
```

Database name: `hotel_reservation_system`

### Tables

`admins`, `rooms`, `customers`, `bookings`, `payments`, `app_settings`  
Foreign keys use **ON DELETE CASCADE** for bookings → customers/rooms and payments → bookings.

---

## How to Run

### Option A — Maven

```bash
mvn clean package
java -jar target/HotelReservationSystem.jar
```

### Option B — Manual (no Maven)

```bash
# Download MySQL Connector/J into lib/ (already documented in repo scripts)
javac --release 17 -encoding UTF-8 -cp "lib/mysql-connector-j-8.3.0.jar" -d out $(find src/main/java -name "*.java")
copy src\main\resources\application.properties out\
run.bat
```

Or on Windows PowerShell:

```powershell
.\run.bat
```

### Default Login

| Field | Value |
|-------|--------|
| Username | `admin` |
| Password | `admin123` |
| Security answer (Forgot Password) | `azure` |

Change the password after first login in a production-like demo.

---

## Folder Structure

```
Hotel_Reservation_System/
├── database/database.sql
├── src/main/java/
│   ├── Main.java
│   ├── model/
│   ├── dao/
│   ├── database/
│   ├── service/
│   ├── ui/ (+ dialog/)
│   ├── components/
│   ├── utils/
│   └── reports/
├── src/main/resources/application.properties
├── screenshots/
├── pom.xml
├── run.bat
└── README.md
```

---

## Architecture

```
UI (Swing panels) → Service (business rules) → DAO (JDBC) → MySQL
```

Bookings are created inside a database transaction with room row locking (`SELECT … FOR UPDATE`) and date-overlap checks so double booking is rejected.

---

## Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| Ctrl+1 | Dashboard |
| Ctrl+2 | Rooms |
| Ctrl+3 | Customers |
| Ctrl+4 | Bookings |
| Ctrl+5 | Payments |
| Ctrl+6 | Reports |
| Ctrl+7 | Settings |
| Ctrl+8 | About |
| Ctrl+L | Logout |

---

## CSV & Backup

- Export / import rooms and customers from **Settings** or **Reports**
- Backup writes a SQL dump (mysqldump if available, otherwise JDBC-based)

---

## Known Limitations

- Payments are simulated (no live payment gateway)
- Forgot Password uses a local security answer (no email delivery)
- Room images store paths under `~/.hotel-reservation/uploads/`

---

## Future Improvements

- Multi-branch / multi-hotel support
- Email / SMS booking confirmations
- PDF export of invoices
- Role-based screens for receptionist vs manager
- Soft delete and full audit trail UI

---

## Author

Built for **CodeAlpha Java Programming Internship — Task 4**.

Hotel brand demo name: **Grand Azure Hotel & Suites**.
