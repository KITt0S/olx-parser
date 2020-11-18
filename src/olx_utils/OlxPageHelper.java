package olx_utils;

import mysql_utils.MySQLHelper;
import okio.Options;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.print.Doc;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 *  Этот класс позволяет легче работать с парсингом olx.ua
 */
public class OlxPageHelper implements Runnable {

    private Document olxDoc;
    private MySQLHelper mySQLHelper;
    private boolean parsePhone;

    public OlxPageHelper( String request, MySQLHelper mySQLHelper, boolean parsePhone ) {

        try {

            olxDoc = Jsoup.connect( request ).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.mySQLHelper = mySQLHelper;
        this.parsePhone = parsePhone;
    }

    @Override
    public void run() {

        parse();
    }

    private void parse() {

        Elements advEls = getAdvElements();
        for (int i = 0; i < advEls.size(); i++) {

            Element e = advEls.get( i );
            String url = getAdvUrl( e );
            AdvHelper advHelper = new AdvHelper( url );
            String title = advHelper.getTitle();
            Author author = advHelper.getAuthor();
            Advertisement.Price price = advHelper.getPrice();
            Date date = advHelper.getDate();
            Advertisement adv;
            if( !parsePhone ) {

                adv = new Advertisement( url, title, author, price, date );
            } else  {

                String phone = advHelper.getPhone();
                adv = new Advertisement( url, title, author, phone, price, date );;
            }

            mySQLHelper.putAdvertisement( adv );
        }
    }

    private Elements getAdvElements() {

        return olxDoc.select( "tr.wrap" );
    }

    private String getAdvUrl( Element e ) {

        return e.getElementsByTag( "a" ).get( 1 ).attr( "href" );
    }

    /***
     *  Класс для парсинга каждого объявления в отдельности
     */
    class AdvHelper {

        String advUrl;
        Document doc;

        public AdvHelper(String url) {

            advUrl = url;
            try {
                doc = Jsoup.connect( url ).get();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }

        private String getTitle() {

            return doc.select( "div.offer-titlebox" ).get( 0 ).child( 1 ).text();
        }

        private Author getAuthor() {

            Elements userHref = doc.select( "div.offer-user__details" ).get( 0 ).getElementsByTag( "a" );
            if( userHref.isEmpty() ) {

                String name = doc.select( "div.offer-user__details" ).get( 0 ).getElementsByTag( "h4" ).text();
                return new Author( name, null );
            } else {

                Element author = null;
                if( doc.select( "div.offer-user__details.offer-user__details--business" ).isEmpty() ) {

                    author = userHref.get( 0 );
                } else {

                    author = userHref.get( 1 );
                }
                return new Author( author.text(),  author.attr( "href" ) );
            }

        }

        private String getPhone() {

            ChromeOptions options = new ChromeOptions();
//            options.addArguments( "--window-position=5000,5000" );
            WebDriver webDriver = new ChromeDriver( options );
            try{

                webDriver.get( advUrl );
                WebElement cookiesAdd = webDriver.findElement(By.cssSelector( "#cookiesBar > button" ) );
                cookiesAdd.click();
                WebElement phoneButton = webDriver.findElement( By.cssSelector( "#contact_methods_below > li" ) );
                phoneButton.click();
                WebElement ePhone = webDriver.findElement( By.cssSelector( "#contact_methods_below > li > div.overh.fleft.marginleft10.brkword.contactitem > strong" ) );
                try {
                    Thread.sleep( 2000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String phone = ePhone.getText();
                return phone;
            } catch ( NoSuchElementException e ) {

                System.err.println( "Номер объявления отсутствует!" );
                e.printStackTrace();
            } finally {

                webDriver.close();
            }
            return null;
        }

        private Advertisement.Price getPrice() {

            String strPrice1 = doc.select( "div.pricelabel" ).get( 0 ).child( 0 ).text();
            Pattern pattern = Pattern.compile( "(\\d*\\s?)*(\\.\\d{2})?(?=( грн\\.)|$)" );
            Matcher matcher = pattern.matcher( strPrice1 );
            String strPrice2 = "";
            if( matcher.find() ) {

                strPrice2 = matcher.group( 0 );
            }
            String strPrice3 = strPrice2.replace( " ", "" );
            String[] priceParts = strPrice3.split( "\\." );
            int decimalPart = Integer.parseInt( priceParts[ 0 ] );
            int fractalPart = 0;
            if( priceParts.length == 2 ) {

                fractalPart = Integer.parseInt( priceParts[ 1 ] );
            }
            String currency = null;
            if( strPrice1.contains( "грн.") ) {

                currency = "грн";
            } else if( strPrice1.contains( "$" ) ) {

                currency = "$";
            }
            return  new Advertisement.Price( decimalPart, fractalPart, currency );
        }

        private Date getDate() {


            String textDate = doc.select("li.offer-bottombar__item > em").get(0).text();
            textDate = textDate.substring( 2 );
            return getDate1( textDate );
        }

        private Date getDate1( String sd ) {

            SimpleDateFormat dateFormat = new SimpleDateFormat( "HH:mm, dd MMMM yyyy" );
            try {
                return dateFormat.parse( sd );
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

//        private Date getDate1( String sd ) {
//
//
//            if( sd != null ) {
//
//                Pattern pattern = Pattern.compile( "(?<=в\\s)\\d{0,2}:\\d{2},\\s\\d{1,2}\\s[а-я]+\\s\\d{4}(?=,\\s)");
//                Matcher matcher = pattern.matcher( sd );
//                if( matcher.find() ) {
//
//                    sd = matcher.group( 0 );
//                    SimpleDateFormat dateFormat = new SimpleDateFormat( "HH:mm, dd MMMM yyyy" );
//                    try {
//
//                        return dateFormat.parse( sd );
//                    } catch  (ParseException e ) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//            return null;
//        }

        private Date getDate2( String sd ) {

            if( sd != null ) {

                Pattern pattern = Pattern.compile( "(?<=\\sв\\s)\\d{0,2}:\\d{2},\\s\\d{1,2}\\s[а-я]+\\s\\d{4}(?=,\\s)" );
                Matcher matcher = pattern.matcher( sd );
                if( matcher.find() ) {

                    sd = matcher.group( 0 );
                    SimpleDateFormat dateFormat = new SimpleDateFormat( "HH:mm, dd MMMM yyyy" );
                    try {

                        return dateFormat.parse( sd );
                    } catch ( ParseException e ) {

                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
