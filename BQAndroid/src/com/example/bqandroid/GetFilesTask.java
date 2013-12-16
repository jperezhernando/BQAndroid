package com.example.bqandroid;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

public class GetFilesTask extends AsyncTask<DbxPath, Void, List<DbxFileInfo>>
{
	private DbxFileSystem dbxFs = null;
	
	@Override
	protected List<DbxFileInfo> doInBackground(DbxPath... params) 
	{
		List<DbxFileInfo> l = new ArrayList<DbxFileInfo>();
		try 
		{
			DbxAccountManager mDbxAcctMgr = MainActivity.getDbxAccountManager();
			
			if (!mDbxAcctMgr.hasLinkedAccount())
				return null;
			
			dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
			listDirectories(dbxFs, params[0], l);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return l;
	}
	
    public interface OnGetFilesTaskCompleted{
        void onGetFilesTaskCompleted(List<DbxFileInfo> l);
    }

    private OnGetFilesTaskCompleted listener;

    public GetFilesTask(OnGetFilesTaskCompleted listener){
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(List<DbxFileInfo> result){
    	super.onPostExecute(result);
        // Call the interface method
        if (listener != null)
            listener.onGetFilesTaskCompleted(result);
    }
	
	private void listDirectories(DbxFileSystem dbxFS, DbxPath dbxPath, List<DbxFileInfo> resList)
	{
		if (dbxPath != null)
		{
			try
			{
				List<DbxFileInfo> files = dbxFS.listFolder(dbxPath);
				for (DbxFileInfo file : files)
				{
					if (file.isFolder)
						listDirectories(dbxFS, file.path, resList);
					else
					{
						//resList.add(file);	//sin discriminar extension
						if (isEpub(file))
							resList.add(file);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}	
	
    private boolean isEpub(DbxFileInfo fileInfo)
    {
    	String name = fileInfo.path.toString();
    	int index = name.lastIndexOf('.');
    	return (index != -1 && name.substring(index + 1).equalsIgnoreCase("epub"));
    }		
	
}