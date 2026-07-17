package model;

import java.math.BigDecimal;

public class DashboardStats {
    private int totalRooms;
    private int availableRooms;
    private int occupiedRooms;
    private int todayBookings;
    private BigDecimal todayRevenue;
    private int totalCustomers;
    private int vipCustomers;
    private double occupancyRate;
    private int todayCheckIns;
    private int todayCheckOuts;
    private BigDecimal monthRevenue;
    private int reservedRooms;
    private int maintenanceRooms;

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public int getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(int availableRooms) {
        this.availableRooms = availableRooms;
    }

    public int getOccupiedRooms() {
        return occupiedRooms;
    }

    public void setOccupiedRooms(int occupiedRooms) {
        this.occupiedRooms = occupiedRooms;
    }

    public int getTodayBookings() {
        return todayBookings;
    }

    public void setTodayBookings(int todayBookings) {
        this.todayBookings = todayBookings;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public int getVipCustomers() {
        return vipCustomers;
    }

    public void setVipCustomers(int vipCustomers) {
        this.vipCustomers = vipCustomers;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public int getTodayCheckIns() {
        return todayCheckIns;
    }

    public void setTodayCheckIns(int todayCheckIns) {
        this.todayCheckIns = todayCheckIns;
    }

    public int getTodayCheckOuts() {
        return todayCheckOuts;
    }

    public void setTodayCheckOuts(int todayCheckOuts) {
        this.todayCheckOuts = todayCheckOuts;
    }

    public BigDecimal getMonthRevenue() {
        return monthRevenue;
    }

    public void setMonthRevenue(BigDecimal monthRevenue) {
        this.monthRevenue = monthRevenue;
    }

    public int getReservedRooms() {
        return reservedRooms;
    }

    public void setReservedRooms(int reservedRooms) {
        this.reservedRooms = reservedRooms;
    }

    public int getMaintenanceRooms() {
        return maintenanceRooms;
    }

    public void setMaintenanceRooms(int maintenanceRooms) {
        this.maintenanceRooms = maintenanceRooms;
    }
}
