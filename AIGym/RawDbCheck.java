import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class RawDbCheck {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/aigym?serverTimezone=UTC";
        String user = "root";
        String pass = "123456";

        try {
            System.out.println("Connecting to MySQL at localhost:3306...");
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected!");
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, email, is_active FROM users");
            
            System.out.println("=== USERS IN LOCALHOST:3306 ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + 
                                   " | Email: " + rs.getString("email") + 
                                   " | Active: " + rs.getBoolean("is_active"));
            }
            System.out.println("===============================");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
