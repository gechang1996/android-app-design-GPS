package edu.msu.gechang1.cse476map;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Class that communicates with the cloud for hattings
 */
public class Cloud {

    private static final String MAGIC = "NechAtHa6RuzeR8x";
    private static final String LOGIN_URL = "http://webdev.cse.msu.edu/~gechang1/cse476/project3/project3-login.php";
    private static final String SAVE_URL = "http://webdev.cse.msu.edu/~gechang1/cse476/project3/project3-save.php";
    private static final String STAR_URL="http://webdev.cse.msu.edu/~gechang1/cse476/project3/project3-stars.php";
    private static final String LOAD_URL = "http://webdev.cse.msu.edu/~gechang1/cse476/project3/project3-load.php";
    private static final String CHANGE_STAR_STATUS_URL="http://webdev.cse.msu.edu/~gechang1/cse476/project3/project3-changeStarStatus.php";
    private static final String CLEAR_URL= "http://webdev.cse.msu.edu/~gechang1/cse476/project3/project3-cleartable.php";
    private static final String END_URL="http://webdev.cse.msu.edu/~gechang1/cse476/project3/project3-EndGame.php";

    private static final String UTF8 = "UTF-8";

    /**
     * Nested class to store one catalog row
     */
    public static class Item {
        public String id = "";
        public String lng = "";
        public String lat = "";
        public String status = "";
    }


    /**
     * Nested class to store one catalog row
     */
    public static class User {
        public String name = "";
        public String status = "";
        public String numStars = "";
    }

    /**
     * Save a hatting to the cloud.
     * This should be run in a thread.
     * @param name name to save under
     * @param pass view we are getting the data from
     * @return true if successful
     */
    public boolean saveToCloud(String name, String pass) {
        name = name.trim();
        pass = pass.trim();
        if(name.length() == 0||pass.length()==0) {
            return false;
        }

        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);

            xml.startDocument("UTF-8", true);

            xml.startTag(null, "starCollector");
            xml.attribute(null, "user", name);
            xml.attribute(null, "pw", pass);
            xml.attribute(null, "magic", MAGIC);

            xml.endTag(null, "starCollector");

            xml.endDocument();

        } catch (IOException e) {
            // This won't occur when writing to a string
            return false;
        }
        final String xmlStr = writer.toString();
        /*
         * Convert the XML into HTTP POST data
         */
        String postDataStr;
        try {
            postDataStr = "xml=" + URLEncoder.encode(xmlStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        /*
         * Send the data to the server
         */
        byte[] postData = postDataStr.getBytes();
        InputStream stream = null;
        try {
            URL url = new URL(SAVE_URL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);
            conn.getOutputStream();
            OutputStream out = conn.getOutputStream();
            out.write(postData);
            out.close();

            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }
            stream = conn.getInputStream();
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch(IOException ex) {
                    // Fail silently
                }
            }
        }
        return true;
    }


    /**
     * Skip the XML parser to the end tag for whatever
     * tag we are currently within.
     * @param xml the parser
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static void skipToEndTag(XmlPullParser xml)
            throws IOException, XmlPullParserException {
        int tag;
        do
        {
            tag = xml.next();
            if(tag == XmlPullParser.START_TAG) {
                // Recurse over any start tag
                skipToEndTag(xml);
            }
        } while(tag != XmlPullParser.END_TAG &&
                tag != XmlPullParser.END_DOCUMENT);
    }

    /**
     * Open a connection to a hatting in the cloud.
     * @param name id for the hatting
     * @return reference to an input stream or null if this fails
     */
    public InputStream openFromCloud(final String name, final String pass) {
        // Create a get query
        String query = LOGIN_URL + "?user=" + name + "&magic=" + MAGIC + "&pw=" + pass;

        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            InputStream stream = conn.getInputStream();
//            logStream(stream);
            return stream;

        } catch (MalformedURLException e) {
            // Should never happen
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Get user information
     * @param name id for the starcollector
     * @return reference to an input stream or null if this fails
     */
    public User loadFromCloud(final String name) {
        // Create a get query
        String query = LOAD_URL + "?user=" + name + "&magic=" + MAGIC;
        InputStream stream = null;
        User user = new User();

        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            stream = conn.getInputStream();
//            logStream(stream);
//            return stream;
        } catch (MalformedURLException e) {
            // Should never happen
            return null;
        } catch (IOException ex) {
            return null;
        }

        try {
            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(stream, "UTF-8");

            xml.nextTag();      // Advance to first tag
            xml.require(XmlPullParser.START_TAG, null, "starCollector");
            user.name = xml.getAttributeValue(null, "name");
            user.status = xml.getAttributeValue(null, "status");
            user.numStars = xml.getAttributeValue(null, "numStars");

//            while(xml.nextTag() == XmlPullParser.START_TAG) {
//                    Item item = new Item();
//                    item.id = xml.getAttributeValue(null, "id");
//                    item.lat = xml.getAttributeValue(null, "lat");
//                    item.lng = xml.getAttributeValue(null, "lng");
//                    item.status = xml.getAttributeValue(null, "status");
//                skipToEndTag(xml);
//            }
            // We are done
        } catch(XmlPullParserException ex) {
            return null;
        } catch(IOException ex) {
            return null;
        } finally {
            try {
                stream.close();
            } catch(IOException ex) {

            }
        }
        return user;
    }


    /**
     * Load Star stream
     * @return reference to an input stream or null if this fails
     */
    public ArrayList<Item> LoadStars() {
        ArrayList<Item> newItems = new ArrayList<Item>();
        // Create a get query
        String query = STAR_URL + "?magic=" + MAGIC;

        InputStream stream = null;
        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            stream = conn.getInputStream();
        } catch (MalformedURLException e) {
            // Should never happen
            return null;
        } catch (IOException ex) {
            return null;
        }
        /**
         * Create an XML parser for the result
         */
        try {
            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(stream, "UTF-8");

            xml.nextTag();      // Advance to first tag
            xml.require(XmlPullParser.START_TAG, null, "starCollector");

            while(xml.nextTag() == XmlPullParser.START_TAG) {
                if(xml.getName().equals("star")) {
                    Item item = new Item();
                    item.id = xml.getAttributeValue(null, "id");
                    item.lat = xml.getAttributeValue(null, "lat");
                    item.lng = xml.getAttributeValue(null, "lng");
                    item.status = xml.getAttributeValue(null, "status");
                    newItems.add(item);
                }
                skipToEndTag(xml);
            }
            // We are done
        } catch(XmlPullParserException ex) {
            return null;
        } catch(IOException ex) {
            return null;
        } finally {
            try {
                stream.close();
            } catch(IOException ex) {

            }
        }
        return newItems;
    }


    public boolean changeStarStatus(int id,String username) {

        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);

            xml.startDocument("UTF-8", true);

            xml.startTag(null, "starCollector");
            xml.attribute(null, "id", String.valueOf(id));
            xml.attribute(null, "name", username);
            xml.endTag(null, "starCollector");

            xml.endDocument();

        } catch (IOException e) {
            // This won't occur when writing to a string
            return false;
        }
        final String xmlStr = writer.toString();
        /*
         * Convert the XML into HTTP POST data
         */
        String postDataStr;
        try {
            postDataStr = "xml=" + URLEncoder.encode(xmlStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        /*
         * Send the data to the server
         */
        byte[] postData = postDataStr.getBytes();
        InputStream stream = null;
        try {
            URL url = new URL(CHANGE_STAR_STATUS_URL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);
            conn.getOutputStream();
            OutputStream out = conn.getOutputStream();
            out.write(postData);
            out.close();

            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }
            stream = conn.getInputStream();
        } catch (MalformedURLException e) {
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch(IOException ex) {
                    // Fail silently
                }
            }
        }
        return true;
    }

    public boolean clearTables()
    {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);

            xml.startDocument("UTF-8", true);

            xml.startTag(null, "starCollector");


            xml.endTag(null, "starCollector");


            xml.endDocument();

        } catch (IOException e) {
            // This won't occur when writing to a string
            return false;
        }

        final String xmlStr = writer.toString();
        /*
         * Convert the XML into HTTP POST data
         */
        String postDataStr;
        try {
            postDataStr = "xml=" + URLEncoder.encode(xmlStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        /*
         * Send the data to the server
         */
        byte[] postData = postDataStr.getBytes();
        InputStream stream = null;
        try {
            URL url = new URL(CLEAR_URL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);
            conn.getOutputStream();
            OutputStream out = conn.getOutputStream();
            out.write(postData);
            out.close();

            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }
            stream = conn.getInputStream();
//            logStream(stream);




        } catch (MalformedURLException e) {
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch(IOException ex) {
                    // Fail silently
                }
            }
        }



        return true;
    }
    public boolean endGame(String username)
    {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);

            xml.startDocument("UTF-8", true);

            xml.startTag(null, "starCollector");
            xml.attribute(null, "user", username);

            xml.endTag(null, "starCollector");


            xml.endDocument();

        } catch (IOException e) {
            // This won't occur when writing to a string
            return false;
        }

        final String xmlStr = writer.toString();
        /*
         * Convert the XML into HTTP POST data
         */
        String postDataStr;
        try {
            postDataStr = "xml=" + URLEncoder.encode(xmlStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        /*
         * Send the data to the server
         */
        byte[] postData = postDataStr.getBytes();
        InputStream stream = null;
        try {
            URL url = new URL(END_URL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);
            conn.getOutputStream();
            OutputStream out = conn.getOutputStream();
            out.write(postData);
            out.close();

            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }
            stream = conn.getInputStream();
//            logStream(stream);




        } catch (MalformedURLException e) {
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch(IOException ex) {
                    // Fail silently
                }
            }
        }



        return true;
    }
}

