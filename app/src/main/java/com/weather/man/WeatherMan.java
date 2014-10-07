package com.weather.man;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.sax.Element;
import android.util.Log;
import android.widget.TextView;

public class WeatherMan extends Activity {
	
    static final String weather_url = "http://xml.weather.yahoo.com/forecastrss?p=CAXX0547&u=c";
    static final String conditionStart = "<yweather:condition ";
    static final String conditionEnd = "/>";
    static final String textStart  = "text=\"";
    static final String codeStart  = "code=\"";
    static final String tempStart  = "temp=\"";
    static final String quoteEnd   = "\"";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //This example runs in Android 2.2 but not in 2.3.3 and above.
        //Appears to be due to the file being chunked -- the content is a GZIPInputStream in 2.3.3
        //and therefore does not have a content-length header and so the loop that reads from the stream does not run.
        //http://stackoverflow.com/questions/14365955/content-length-header-is-not-getting-in-android-2-3-above-browser
        //http://serverfault.com/questions/183843/content-length-not-sent-when-gzip-compression-enabled-in-apache
        
        try {
			URL url = new URL("http://xml.weather.yahoo.com/forecastrss?p=CAXX0547&u=c");
	    	HttpURLConnection c = (HttpURLConnection) url.openConnection();
	    	
	    	//get object representing content of resource that c connected to
	    	//Android 2.2: org.apache.harmony.luni.internal.net.www.protocol.http.HttpURLConnectionImpl$LimitedInputStream
	    	//Android 2.3: java.util.zip.GZIPInputStream
	    	Object weatherObject = c.getContent();
	    	
	    	//check the human-readable description of the object
	    	//Android 2.2:
	    	//org.apache.harmony.luni.internal.net.www.protocol.http.HttpURLConnectionImpl$ChunkedInputStream@43e49e60
	    	//Android 2.3.3: java.util.zip.GZIPInputStream
	    	String weatherString = weatherObject.toString();
	    	 
	    	TextView text = (TextView) findViewById(R.id.textView1);
	    	
	    	//get an InputStream to read the data
	    	//Android 2.2: HttpURLConnectionImpl$ChunkedInputStream
	    	//Android 2.3.3: java.util.zip.GZIPInputStream
	    	InputStream is = c.getInputStream();
	        
            //Get the ContentType  	
	    	//in Android 2.2:
	    	//text/xml;charset=UTF-8
	    	String type = c.getContentType();
	    	
	    	//Android 2.3.3: -1 (content-length response header is not set!)
	    	int len = (int)c.getContentLength();
	    	
	    	//so if we have GZipped content, len is -1 so we can't do it this way...
            /* if (len > 0) {
                int actual = 0;
                int bytesread = 0 ;
                byte[] data = new byte[len];
                while ((bytesread != len) && (actual != -1)) {
                   actual = is.read(data, bytesread, len - bytesread);
                   bytesread += actual;
                }
                String returnString = new String(data);
            */
	    	
	    	//let's try another way!
	    	//wrap the InputStream in a ByteArrayInputStream
	    	BufferedInputStream bis = new BufferedInputStream(is);
	    	//byte array variable to store content as read
	    	byte[] bytes = null;
	    	int offset = 0;
	    	int byteCount;
	    	int numBytesRead;
	    	boolean dataWasRead = false;
	    	//to build the content as we read it from the stream 
	    	StringBuilder stringBuilder = new StringBuilder();
	    	String returnString = null;
	    	while (bis.available() > 0) {
	    		//we have bytes to read!
	    		//how many are available?
	    		byteCount = bis.available();
	    		//read them into the byte[]
	    		bytes = new byte[byteCount];
	    		numBytesRead = bis.read(bytes, offset, byteCount);
	    		
	    		if (numBytesRead == -1) { //check for EOS
	    			break;
	    		}
	    		else {
	    			dataWasRead = true;
	    			//add data to output StringBuilder
	    			returnString = new String(bytes);
	    			stringBuilder.append(returnString);
	    			break;
	    		}
	    	}
	    	
	    	//or maybe it would have been better to use an InputStreamReader
	    	//(instead of a BufferedInputStream)
	    	
	    	if (dataWasRead) {
	    		returnString = new String(stringBuilder);
	            //text.setText(isolateString(returnString,conditionStart,conditionEnd));        
	            String conditionString = isolateString(returnString,conditionStart,conditionEnd);
	            String currentConditions = isolateString(conditionString,textStart,quoteEnd);
	            String currentTemp = isolateString(conditionString,tempStart,quoteEnd);
	            text.setText("In Winnipeg it is " + currentConditions + " and the temp is " + currentTemp + ".");
	    	}
	    	else {
	    		text.setText("Sorry... problems reading data. Debug time.");
	    	}
	    		
           
        }
		catch (IOException e) {
			Log.d("test", "IOException in WeatherMan!");
		}
        
    }
    
    //return a string between start and end
    public String isolateString(String base, String start, String end) {
        int startIndex = base.indexOf(start);
        int endIndex   = base.indexOf(end,startIndex+start.length());
        return base.substring(startIndex+start.length(),endIndex);
    }
    
/*    public class weatherParse extends AsyncTask<Object, Object, Object> {
    	
    	public void parse() throws IOException {
    		try {
    			URL url = new URL("http://xml.weather.yahoo.com/forecastrss?p=CAXX0547&u=c");
	    	HttpURLConnection c = (HttpURLConnection) url.openConnection();
	    	Object weatherObject = c.getContent();
	    	String weatherString = weatherObject.toString();
	    	TextView text = (TextView) findViewById(R.id.textView1);
	    	
	    	InputStream is = c.getInputStream();
	        
            // Get the ContentType
            String type = c.getContentType();
	    	int len = (int)c.getContentLength();
	    	
            if (len > 0) {
                int actual = 0;
                int bytesread = 0 ;
                byte[] data = new byte[len];
                while ((bytesread != len) && (actual != -1)) {
                   actual = is.read(data, bytesread, len - bytesread);
                   bytesread += actual;
                }
                String returnString = new String(data);
                //text.setText(isolateString(returnString,conditionStart,conditionEnd));
                
                String conditionString = isolateString(returnString,conditionStart,conditionEnd);
                
                String currentConditions = isolateString(conditionString,textStart,quoteEnd);
                String currentTemp = isolateString(conditionString,tempStart,quoteEnd);

                text.setText("In Winnipeg it is " + currentConditions + " and the temp is " + currentTemp + ".");
    	    	}
    		catch (IOException e) {
    			
    		}
    	}
    	
		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			return null;
		}
    	
    }*/
}

		