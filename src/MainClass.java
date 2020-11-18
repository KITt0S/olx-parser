import mysql_utils.MySQLHelper;
import olx_utils.Advertisement;
import olx_utils.OlxHelper;
import olx_utils.OlxPageHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MainClass {

    private static String domain = "https://www.olx.ua";
    private static String rubric = "elektronika";
    private static String subrubric = "kompyutery-i-komplektuyuschie";
    private static String city = "odessa";
    private static String request = "видеокарта";

    private static MySQLHelper sqlHelper = new MySQLHelper( "k1ts", "1234" );

    private static List<Advertisement> advList = new ArrayList<>();

    static {

        System.setProperty( "webdriver.chrome.driver", "chromedriver.exe" );
    }

    public static void main(String[] args) {

        sqlHelper.createOlxDB( "olx_db" );
        sqlHelper.createTables();

        String link = createLink( domain, rubric, city, request );
        new OlxHelper( link, sqlHelper ).parse( false );
    }

    private static String createLink( String domain, String rubric, String city, String request ) {

        return  domain + "/" + rubric + "/" + subrubric + "/" + city + "/q-" + request;
    }

    private static void dispAdvs() {

        for ( Advertisement a :
             advList ) {

            System.out.println( a );
        }
    }
}
