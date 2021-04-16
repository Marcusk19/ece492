import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropAccountsTable
{
public static void main(String[] args) throws Exception
		{
		Class.forName("com.ibm.db2j.jdbc.DB2jDriver");
		System.out.println("Driver loaded!");
		Connection connection = DriverManager.getConnection(
					"jdbc:db2j:C:\\database\\QuoteDB");
		System.out.println("Connected to database!");
		Statement statement = connection.createStatement();
		statement.execute("DROP TABLE BANK_ACCOUNTS");
		System.out.println("Table deleted.");
		}
}
