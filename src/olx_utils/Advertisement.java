package olx_utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Advertisement {

    private String url;
    private String title;
    private Author author;
    private String phone;
    private Price price;
    private Date date;

    public Advertisement(String url, String title, Author author, Price price, Date date) {
        this.url = url;
        this.title = title;
        this.author = author;
        this.price = price;
        this.date = date;
    }

    public Advertisement(String url, String title, Author author, String phone, Price price, Date date) {
        this.url = url;
        this.title = title;
        this.author = author;
        this.phone = phone;
        this.price = price;
        this.date = date;
    }

    public Advertisement() {}

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public Author getAuthor() {
        return author;
    }

    public String getPhone() {
        return phone;
    }

    public Price getPrice() {
        return price;
    }

    public Date getDate() {
        return date;
    }

    public String getMySQLFormatDate() {

        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:MM" );
        return dateFormat.format( date );
    }

    @Override
    public String toString() {

        return title + "; " + price + "\n" + url + "\n" + author + ", " + date + "\n";
    }

    public static class Price {

        int decimalPart;
        int fractPart;
        String currency;


        public Price( int decimalPart, int fractPart, String currency ) {

            this.decimalPart = decimalPart;
            this.fractPart = fractPart;
            this.currency = currency;
        }

        public int getDecimalPart() {
            return decimalPart;
        }

        public int getFractPart() {
            return fractPart;
        }

        public String getCurrency() {
            return currency;
        }

        @Override
        public String toString() {

            return  decimalPart + "." + fractPart + " " + currency;
        }
    }
}
