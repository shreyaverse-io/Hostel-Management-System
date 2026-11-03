import java.sql.*;
import java.util.Scanner;

public class HostelManagementSystem {

    private static final String URL = "jdbc:mysql://localhost:3306/hostel_db";
    private static final String USER = "root";
    private static final String PASSWORD = "admin"; // Update with your MySQL password

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connected to MySQL Database successfully!");

            while (true) {
                System.out.println("\n========== KIET HOSTEL MANAGEMENT SYSTEM ==========");
                System.out.println("1. Insert Hostel Student Records");
                System.out.println("2. Display Students by Hostel (Fee > ₹50,000, Sorted)");
                System.out.println("3. Update Hostel Fee");
                System.out.println("4. Search and Modify Room Number");
                System.out.println("5. Display Total Fee Collected & Conditionally Delete Student");
                System.out.println("6. Exit");
                System.out.print("Enter your choice (1-6): ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        insertStudentRecords(conn, scanner);
                        break;
                    case 2:
                        getStudentsByHostelAndFee(conn, scanner);
                        break;
                    case 3:
                        updateHostelFee(conn, scanner);
                        break;
                    case 4:
                        modifyRoomNumber(conn, scanner);
                        break;
                    case 5:
                        totalFeeAndDeleteStudent(conn, scanner);
                        break;
                    case 6:
                        System.out.println("Exiting System. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice! Please select 1 to 6.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database Connection Failed: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void insertStudentRecords(Connection conn, Scanner sc) {
        String insertSQL = "INSERT INTO HostelStudent (student_id, student_name, roll_number, hostel_name, room_number, fee) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            for (int i = 1; i <= 3; i++) {
                System.out.println("\n--- Entering Details for Student #" + i + " ---");
                System.out.print("Enter Student ID (Integer): ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Enter Student Name: ");
                String name = sc.nextLine();

                System.out.print("Enter Roll Number: ");
                String roll = sc.nextLine();

                System.out.print("Enter Hostel Name: ");
                String hostel = sc.nextLine();

                System.out.print("Enter Room Number: ");
                String room = sc.nextLine();

                System.out.print("Enter Fee: ");
                double fee = sc.nextDouble();
                sc.nextLine();

                pstmt.setInt(1, id);
                pstmt.setString(2, name);
                pstmt.setString(3, roll);
                pstmt.setString(4, hostel);
                pstmt.setString(5, room);
                pstmt.setDouble(6, fee);

                try {
                    int rowsInserted = pstmt.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("Student record inserted successfully!");
                    }
                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Error: Student ID " + id + " already exists! Insertion skipped.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting record: " + e.getMessage());
        }
    }

    private static void getStudentsByHostelAndFee(Connection conn, Scanner sc) {
        System.out.print("\nEnter Hostel Name to search: ");
        String hostel = sc.nextLine();

        String selectSQL = "SELECT * FROM HostelStudent WHERE hostel_name = ? AND fee > 50000 ORDER BY fee DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, hostel);
            ResultSet rs = pstmt.executeQuery();

            boolean found = false;
            System.out.println("\n--- Students in " + hostel + " with Fee > ₹50,000 (Sorted Descending) ---");
            while (rs.next()) {
                found = true;
                System.out.printf("ID: %d | Name: %s | Roll No: %s | Room: %s | Fee: ₹%.2f\n",
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("roll_number"),
                        rs.getString("room_number"),
                        rs.getDouble("fee"));
            }

            if (!found) {
                System.out.println("No matching records found.");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving records: " + e.getMessage());
        }
    }

    private static void updateHostelFee(Connection conn, Scanner sc) {
        System.out.print("\nEnter Student ID to update fee: ");
        int id = sc.nextInt();
        System.out.print("Enter New Hostel Fee: ");
        double newFee = sc.nextDouble();

        String updateSQL = "UPDATE HostelStudent SET fee = ? WHERE student_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setDouble(1, newFee);
            pstmt.setInt(2, id);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Fee updated successfully for Student ID: " + id);
            } else {
                System.out.println("Error: Student record with ID " + id + " does not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating fee: " + e.getMessage());
        }
    }

    private static void modifyRoomNumber(Connection conn, Scanner sc) {
        System.out.print("\nEnter Student ID to modify room: ");
        int id = sc.nextInt();
        sc.nextLine();

        String checkSQL = "SELECT * FROM HostelStudent WHERE student_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.print("Student found! Enter New Room Number: ");
                String newRoom = sc.nextLine();

                String updateSQL = "UPDATE HostelStudent SET room_number = ? WHERE student_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                    updateStmt.setString(1, newRoom);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                }

                PreparedStatement fetchStmt = conn.prepareStatement(checkSQL);
                fetchStmt.setInt(1, id);
                ResultSet updatedRs = fetchStmt.executeQuery();

                if (updatedRs.next()) {
                    System.out.println("\n--- Updated Student Details ---");
                    System.out.printf("ID: %d | Name: %s | Roll No: %s | Hostel: %s | Room: %s | Fee: ₹%.2f\n",
                            updatedRs.getInt("student_id"),
                            updatedRs.getString("student_name"),
                            updatedRs.getString("roll_number"),
                            updatedRs.getString("hostel_name"),
                            updatedRs.getString("room_number"),
                            updatedRs.getDouble("fee"));
                }
            } else {
                System.out.println("Error: Student with ID " + id + " does not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Error modifying details: " + e.getMessage());
        }
    }

    private static void totalFeeAndDeleteStudent(Connection conn, Scanner sc) {
        String sumSQL = "SELECT SUM(fee) AS total_fee FROM HostelStudent";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sumSQL)) {

            if (rs.next()) {
                double totalFee = rs.getDouble("total_fee");
                System.out.printf("\nTotal Hostel Fee Collected: ₹%.2f\n", totalFee);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total fee: " + e.getMessage());
        }

        System.out.print("\nEnter Student ID to delete record: ");
        int id = sc.nextInt();

        String checkSQL = "SELECT fee FROM HostelStudent WHERE student_id = ?";
        String deleteSQL = "DELETE FROM HostelStudent WHERE student_id = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSQL)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                double feePaid = rs.getDouble("fee");
                if (feePaid > 0) {
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
                        deleteStmt.setInt(1, id);
                        deleteStmt.executeUpdate();
                        System.out.println("Student record deleted successfully.");
                    }
                } else {
                    System.out.println("Record cannot be deleted: Hostel fee is zero / not paid.");
                }
            } else {
                System.out.println("Record cannot be deleted: Student ID " + id + " does not exist.");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting student record: " + e.getMessage());
        }
    }
}