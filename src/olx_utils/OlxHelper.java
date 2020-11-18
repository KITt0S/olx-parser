package olx_utils;

import mysql_utils.MySQLHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class OlxHelper {

    private String link;
    private MySQLHelper mySQLHelper;

    public OlxHelper( String link, MySQLHelper mySQLHelper ) {

        this.link = link;
        this.mySQLHelper = mySQLHelper;
    }

    public void parse( boolean parsePhone ) {

        try {

            Document olxDoc = Jsoup.connect( link ).get();
            int n = getPagesNum( olxDoc );
            System.out.println( n );
            ExecutorService executorService = Executors.newFixedThreadPool( 1 );
            for (int i = 0; i < n; i++) {


                executorService.submit( new OlxPageHelper( moveToPage( link, i + 1 ), mySQLHelper, parsePhone ) );
            }
            executorService.shutdown();
            try {

                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS );
            } catch ( InterruptedException e ) {

                e.printStackTrace();
            }
        } catch ( IOException e ) {

            e.printStackTrace();
        }
    }

    private int getPagesNum( Document olxDoc ) {

        if( olxDoc != null && !olxDoc.select( "div.pager.rel.clr" ).isEmpty() ) {

            Elements pages =  olxDoc.select( "div.pager.rel.clr" ).get( 0 ).select( "span.item.fleft");
            String n = pages.get( pages.size() - 1 ).child( 0 ).child( 0 ).text();
            return Integer.parseInt( n );

        } else return 1;
    }

    private String moveToPage( String link, int n ) {

        if( n == 2 ) {

            link += "?page=2";
        } else if( n > 2 ){

            link.replaceFirst( "(?<=\\?page=)\\d+", Integer.toString( n ) );
        }
        return link;
    }
}
