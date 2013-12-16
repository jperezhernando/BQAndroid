package com.example.bqandroid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

import android.os.AsyncTask;

public class GetCoverTask extends AsyncTask<String, Void, String>
{
	@Override
	protected String doInBackground(String... params) 
	{
        URL url = null;
		try 
		{
			url = new URL("https://ajax.googleapis.com/ajax/services/search/images?" +
					  	  "v=1.0&q=" + params[0]);
			
			URLConnection connection = url.openConnection();
			
			String line;
	        StringBuilder builder = new StringBuilder();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        while((line = reader.readLine()) != null) 
	        {
	        	builder.append(line);
	        }
	        JSONObject json = new JSONObject(builder.toString());
	        String imageUrl = json.getJSONObject("responseData").getJSONArray("results").getJSONObject(0).getString("url");
	        
	        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
	        //startActivity(intent);		
	        
	        return imageUrl;					
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
   public interface OnGetCoverTaskCompleted{
        void onGetCoverTaskCompleted(String s);
    }

    private OnGetCoverTaskCompleted listener;

    public GetCoverTask(OnGetCoverTaskCompleted listener){
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(String result){
    	super.onPostExecute(result);
        // Call the interface method
        if (listener != null)
            listener.onGetCoverTaskCompleted(result);
    }	
	
}