
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


public class CreateAccountsTable
    {

public static void main(String[] args) throws Exception
            {
            Class.forName("com.ibm.db2j.jdbc.DB2jDriver");
            System.out.println("Driver loaded!");
            Connection conn = DriverManager.getConnection(
                              "jdbc:db2j:C:\\database\\QuoteDB");
            System.out.println("Connection made to Data Base!");
            
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE BANK_ACCOUNTS "
                       + "(ACCOUNT_NUMBER INTEGER     NOT NULL,"
                       + " ACCOUNT_TYPE   VARCHAR(20) NOT NULL,"
                       + " CUSTOMER_NAME  VARCHAR(50) NOT NULL,"
                       + " BALANCE        FLOAT       NOT NULL,"
                       + " PRIMARY KEY (ACCOUNT_NUMBER))");
            System.out.println("BANK_ACCOUNTS table built!");       
            }
    }
