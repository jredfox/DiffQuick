package jredfox.diffq;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiffQuickUtils {
	
	public static String getRealtivePath(File dir, File file) 
	{
		String path = file.getPath();
		String dirPath = dir.getPath();
		boolean plus1 = path.contains(File.separator);
		return path.substring(path.indexOf(dirPath) + dirPath.length() + (plus1 ? 1 : 0));
	}
	
	public static List<File> getDirFiles(File dir, boolean r)
	{
		return getDirFiles(dir, r, "*");
	}
	
	/**
	 * get a list of files from a file or directory
	 */
	public static List<File> getDirFiles(File dir, boolean r, String... exts)
	{
		return getDirFiles(dir, exts, false, r);
	}
	
	/**
	 * get a list of files from a file or directory. has blacklist extension support
	 */
	public static List<File> getDirFiles(File dir, String[] exts, boolean blacklist, boolean r) 
	{
		if(!dir.exists())
			return Collections.emptyList();
		if(!dir.isDirectory())
		{
			List<File> li = new ArrayList<File>(1);
			String ext = getExtension(dir);
			boolean isType = blacklist ? !isExtEqual(ext, exts) : isExtEqual(ext, exts);
			if(isType)
				li.add(dir);
			return li;
		}
		List<File> list = new ArrayList<File>(dir.listFiles().length);
		getDirFiles(list, dir, exts, blacklist, r);
		return list;
	}
	
	protected static void getDirFiles(List<File> files, File dir, String[] exts, boolean blacklist, boolean r) 
	{
	    for (File file : dir.listFiles()) 
	    {
	    	String extension = getExtension(file);
	    	boolean isType = blacklist ? !isExtEqual(extension, exts) : isExtEqual(extension, exts);
	        if (file.isFile() && isType)
	        {
	            files.add(file);
	        }
	        else if (file.isDirectory()) 
	        {
	        	if(r)
	        		getDirFiles(files, file, exts, blacklist, true);
	        }
	    }
	}
	
	public static String getExtensionFull(File file) 
	{
		String ext = getExtension(file);
		return ext.isEmpty() ? "" : "." + ext;
	}

	/**
	 * get a file extension. Note directories do not have file extensions
	 */
	public static String getExtension(File file) 
	{
		String name = file.getName();
		int index = name.lastIndexOf('.');
		return index != -1 && !file.isDirectory() ? name.substring(index + 1) : "";
	}
	
	public static boolean isExtEqual(String orgExt, String... exts)
	{
		orgExt = orgExt.toLowerCase();
		for(String ext : exts)
		{
			if(ext.equals("*") || orgExt.isEmpty() && ext.equals("noextension") || orgExt.equals(ext))
				return true;
		}
		return false;
	}

	public static void close(Closeable... cs) 
	{
		for(Closeable c : cs)
		{
			try
			{
				if(c != null)
					c.close();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
	}

}
