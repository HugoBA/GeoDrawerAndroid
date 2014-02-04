package com.example.geodrawer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.jdom2.input.SAXBuilder;

import android.os.AsyncTask;

class RetreivePatterns extends AsyncTask<String, Void, org.jdom2.Document> {

    private Exception exception;

    protected org.jdom2.Document doInBackground(String... urls) {
        try 
        {
            URL url= new URL(urls[0]);
            
            URLConnection conn = null;
			try 
			{
				conn = url.openConnection();
			} 
			catch (IOException e1) 
			{
	            this.setException(e1);
            	return null;
			}
			
			SAXBuilder sxb = new SAXBuilder();
			try
            {
                    return sxb.build(conn.getInputStream());
            }
            catch(Exception e)
            {
                this.setException(e);
            	return null;
            }
            
        } 
        catch (Exception e) 
        {
            this.setException(e);
            return null;
        }
    }

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}