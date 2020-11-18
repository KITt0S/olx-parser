package mysql_utils;

import olx_utils.Advertisement;
import olx_utils.Author;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class MySQLHelper {

    private static final String url = "jdbc:mysql://localhost:3306?useUnicode=true&useJDBCCompliantTimezone" +
            "Shift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static String root_user = "";
    private static String root_pass = "";

    private String user;
    private String password;

    private Statement statement;

    public MySQLHelper( String user, String password ) {

        try( Connection conn = DriverManager.getConnection( url, root_user, root_pass ) ) {

            try( Statement st = conn.createStatement() ) {

                String query = "CREATE USER IF NOT EXISTS '" + user + "'@'localhost' IDENTIFIED BY '" + password + "';";
                String query2 = "GRANT ALL PRIVILEGES ON *.* TO '" + user + "'@'localhost';";
                st.execute( query );
                st.execute( query2 );

                try {

                    Connection conn1 = DriverManager.getConnection( url, user, password );
                    statement = conn1.createStatement();
                } catch ( SQLException e ) {

                    e.printStackTrace();
                }
            } catch (SQLException e ) {

                e.printStackTrace();
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
        }
    }


    public void createOlxDB( String dbName ) {

        String query = "CREATE DATABASE IF NOT EXISTS " + dbName + ";";
        String query2 = "use " + dbName + ";";
        try {
            statement.execute( query );
            statement.executeUpdate( query2 );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTables() {

        String query = "CREATE TABLE IF NOT EXISTS Authors (" +
                "Id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(40) NOT NULL, " +
                "url VARCHAR(200) NOT NULL UNIQUE" +
                ");";

        String query2 = "CREATE TABLE IF NOT EXISTS Advertisements (" +
                "Id INTEGER PRIMARY KEY AUTO_INCREMENT, " +
                "url VARCHAR(200) NOT NULL UNIQUE, " +
                "title VARCHAR(50) NOT NULL, " +
                "author_id INTEGER NOT NULL, " +
                "phone VARCHAR(50), " +
                "price_dec INTEGER NOT NULL, " +
                "price_fract INTEGER NOT NULL, " +
                "currency VARCHAR(4) NOT NULL, " +
                "adv_date DATE NOT NULL, " +
                "CONSTRAINT authors_fk " +
                "FOREIGN KEY (author_id) REFERENCES Authors(Id)" +
                ");";

        try {
            statement.execute( query );
            statement.execute( query2 );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void putAdvertisements( List<Advertisement> advList ) {

        if ( advList != null ) {

            for ( Advertisement a :
                 advList ) {

                putAdvertisement( a );
            }
        }
    }

     public void putAdvertisement( Advertisement adv ) {

        Author author = adv.getAuthor();
        Advertisement.Price price = adv.getPrice();
        String query = "INSERT IGNORE INTO Authors(" +
                "name, " +
                " url)" +
                "VALUES (" +
                "'" + author.getName() + "', " +
                "'" + author.getUrl() + "'" +
                ");";

        String query2 = "INSERT IGNORE INTO Advertisements(" +
                "url, " +
                "title, " +
                "author_id, " +
                "phone, " +
                "price_dec, " +
                "price_fract, " +
                "currency, " +
                "adv_date)" +
                "VALUES (" +
                "'" + adv.getUrl() + "', " +
                "'" + adv.getTitle() + "', " +
                "(SELECT Id FROM Authors WHERE name='"+ author.getName() +"' AND url='" + author.getUrl() + "'), " +
                "'" + adv.getPhone() + "', " +
                price.getDecimalPart() + ", " +
                price.getFractPart() + ", " +
                "'" + price.getCurrency() + "', " +
                "'" + adv.getMySQLFormatDate() + "'" +
                ");";

         try {

             statement.execute( query );
             statement.execute( query2 );
         } catch ( SQLException e ) {
             e.printStackTrace();
         }
     }
}
