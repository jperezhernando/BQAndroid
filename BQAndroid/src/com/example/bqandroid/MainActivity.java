package com.example.bqandroid;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxFileSystem.ThumbFormat;
import com.dropbox.sync.android.DbxFileSystem.ThumbSize;
import com.dropbox.sync.android.DbxPath;
import com.example.bqandroid.GetCoverTask.OnGetCoverTaskCompleted;
import com.example.bqandroid.GetFilesTask.OnGetFilesTaskCompleted;

public class MainActivity extends Activity {

	private static DbxAccountManager mDbxAcctMgr;
	static final int REQUEST_LINK_TO_DBX = 0;  // This value is up to you
	
	private final String appKey    = "cps8lo3nhvp0bgz";
	private final String appSecret = "mx6ap9pl56wiu26";
	
	private ListView listView;
	private MyAdapter adapter = null;
	
	GestureDetector gDetector = null;
	
	DbxFileSystem dbxFs = null;
	
	private Context context = this;
	private ImageView imageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		try
		{
			gDetector = new GestureDetector(this, new GestureListener());
			
			listView = (ListView) findViewById(R.id.listview);
			
			listView.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gDetector.onTouchEvent(event);
					return true;
				}
			});
			
			imageView = (ImageView) findViewById(R.id.imageview);
			imageView.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(View v) {
	                closeImage();
	            }
	        });
			
			mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), 
																				appKey,     //APP_KEY
																				appSecret); //APP_SECRET	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.sort_by_name:
	            if (adapter != null)
	            	adapter.sortByName();
	            return true;
	        case R.id.sort_by_date:
	            if (adapter != null)
	            	adapter.sortByDate();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mDbxAcctMgr.hasLinkedAccount()) {
			new GetFilesTask(listener).execute(new DbxPath("/"));	
		} else {
			mDbxAcctMgr.startLink((Activity)this, REQUEST_LINK_TO_DBX);
		}
	}
	
	@Override
	protected void onStop()
	{
		mDbxAcctMgr.unlink();
		super.onStop();
	}
	
	@Override
	protected void onDestroy()
	{
		mDbxAcctMgr.unlink();
		super.onDestroy();
	}
	
	private void closeImage()
	{
		imageView.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_LINK_TO_DBX) {
	        if (resultCode == Activity.RESULT_OK) {
	            // ... Start using Dropbox files.
	        	try 
	        	{
	        		new GetFilesTask(listener).execute(new DbxPath("/"));
				} 
	        	catch (Exception e) 
	        	{
					e.printStackTrace();
				}
	        	
	        } else {
	            // ... Link failed or was cancelled by the user.
	        }
	    } else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}	
	
	public static DbxAccountManager getDbxAccountManager()
	{
		return mDbxAcctMgr;
	}
	
	private OnGetFilesTaskCompleted listener = new OnGetFilesTaskCompleted()
	{
		public void onGetFilesTaskCompleted(List<DbxFileInfo> files)
		{
			adapter = new MyAdapter(context, R.layout.list_row, files);
			listView.setAdapter(adapter);			
		}
	};
	
	private OnGetCoverTaskCompleted listenerCover = new OnGetCoverTaskCompleted()
	{
		public void onGetCoverTaskCompleted(String imageUrl)
		{		
	        new DownloadImageTask(imageView).execute(imageUrl);
		}
	};
	
    private class MyAdapter extends ArrayAdapter<DbxFileInfo>
    {
    	private DbxFileInfo[] fileArray = null;
    	private Context context;
    	
    	public MyAdapter(Context context, int textViewResourceId, List<DbxFileInfo> objects)
    	{
    		super(context, textViewResourceId, objects);
    		this.context = context;
    		fileArray = new DbxFileInfo[objects.size()];
    		for (int i = 0; i < fileArray.length; i++)
    			fileArray[i] = objects.get(i);
    	}
    	
    	public DbxFileInfo[] getFileArray()
    	{
    		return fileArray;
    	}
    	
    	@Override
    	public View getView (int position, View convertView, ViewGroup parent)
    	{
    	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	    View rowView = inflater.inflate(R.layout.list_row, parent, false);
    	    
    	    //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
    	    TextView textName = (TextView) rowView.findViewById(R.id.bookFile);
    	    TextView textDate = (TextView) rowView.findViewById(R.id.bookDate);
    	    
    	    textName.setText(getFilename(fileArray[position]));
    	    textDate.setText(getFileDate(fileArray[position]));

    	    return rowView;
    	}
    	
    	@Override
    	public void notifyDataSetChanged()
    	{
    		super.notifyDataSetChanged();
    	}    	
    	
    	private String getFilename(DbxFileInfo info)
    	{
    		return new File(info.path.toString()).getName();
    	}
    	
    	private String getFileDate(DbxFileInfo info)
    	{
    		return info.modifiedTime.toString();
    	}
    	
    	public void sortByName()
    	{
    		Arrays.sort(fileArray, new DbxFileNameComparator());
    		//super.sort(new DbxFileNameComparator());
    		this.notifyDataSetChanged();
    	}
    	
    	public void sortByDate()
    	{
    		Arrays.sort(fileArray, new DbxFileDateComparator());
    		//super.sort(new DbxFileDateComparator());
    		this.notifyDataSetChanged();
    	}
    	
    	class DbxFileNameComparator implements Comparator<DbxFileInfo>
    	{
			public int compare(DbxFileInfo o1, DbxFileInfo o2) 
			{
				return getFilename(o1).compareToIgnoreCase(getFilename(o2));
			}
    	}
    	
    	class DbxFileDateComparator implements Comparator<DbxFileInfo>
    	{
    		public int compare(DbxFileInfo o1, DbxFileInfo o2)
    		{
    			return o1.modifiedTime.compareTo(o2.modifiedTime);
    		}
    	}
    }
    
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> 
    {
  	  ImageView imgView;

  	  public DownloadImageTask(ImageView imgView) 
  	  {
  	      this.imgView = imgView;
  	  }

  	  @Override
  	  protected Bitmap doInBackground(String... urls) {
  	      String urldisplay = urls[0];
  	      Bitmap image = null;
  	      try 
  	      {
  	        InputStream in = new java.net.URL(urldisplay).openStream();
  	        image = BitmapFactory.decodeStream(in);
  	      } 
  	      catch (Exception e) 
  	      {
  	          e.printStackTrace();
  	      }
  	      return image;
  	  }

  	  @Override
  	  protected void onPostExecute(Bitmap result) 
  	  {
  		  imgView.setVisibility(View.VISIBLE);
  	      imgView.setImageBitmap(result);
  	  }
  	}
    
    private class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
    	public boolean onDown(MotionEvent e)
    	{
    		return true;
    	}
    	
    	public boolean onDoubleTap(MotionEvent e)
    	{
    		int position = listView.pointToPosition((int) e.getX(), (int) e.getY());
    		DbxFileInfo fileInfo = adapter.getFileArray()[position];
    		DbxFile thumbnail = null;
    		try 
    		{
    			if (fileInfo.thumbExists) //intentando abrir un thumbnail
    				thumbnail = dbxFs.openThumbnail(fileInfo.path, ThumbSize.M, ThumbFormat.PNG);
    			else
    			{
    				String term = fileInfo.path.getName();
    				term = term.substring(0, term.lastIndexOf('.'));
    				term = term + " book cover";
    				term = term.replace(" ", "%20");
    				new GetCoverTask(listenerCover).execute(term);
    			}
			} 
    		catch (Exception ex) 
    		{
				ex.printStackTrace();
			}
    		
    		return true;
    	}
    	
    } 
}

