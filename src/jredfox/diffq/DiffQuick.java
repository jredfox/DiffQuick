package jredfox.diffq;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DiffQuick {
	
	public static void main(String[] args)
	{
		boolean recurse = false;
		boolean bl = false;
		String[] exts = null;
		File fileA = null;
		File fileB = null;
		try
		{
			for(int i=0;i<args.length;i++)
			{
				String s = args[i].replaceAll("\"", "").replaceAll("'", "").trim();
				if(s.equalsIgnoreCase("--help") || s.equalsIgnoreCase("-h") || s.equalsIgnoreCase("/?"))
					help();
				else if(s.equalsIgnoreCase("-r") || s.equalsIgnoreCase("--recurse"))
					recurse = true;
				else if(s.startsWith("--wl") || s.startsWith("--whitelist=")) {
					boolean full = !s.startsWith("--wl");
					exts = s.substring(full ? 12 : 5).replaceAll("\\s+","").replace(".", "").replace(";", ",").split(",");
				}
				else if(s.startsWith("--bl") || s.startsWith("--blacklist=")) {
					boolean full = s.startsWith("--bla");
					exts = s.substring(full ? 12 : 5).replaceAll("\\s+","").replace(".", "").replace(";", ",").split(",");
					bl = true;
				}
				else if (fileA == null)
					fileA = new File(args[i]).getAbsoluteFile().getCanonicalFile();
				else if (fileB == null)
					fileB = new File(args[i]).getAbsoluteFile().getCanonicalFile();
			}
			if(fileA == null || fileB == null || !fileA.exists() || !fileB.exists())
				help();
			else if(fileA.isDirectory() != fileB.isDirectory())
			{
				System.err.println("Type Mismatch file and directory!");
				System.exit(500);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.err.println("Failed to Parse Arguments Make Sure that both files / directories exist!");
			help();
		}
		
		run(recurse, fileA, fileB, exts, bl);
	}
	
	public static void run(boolean recurse, File fileA, File fileB, String[] exts, boolean bl)
	{
		if(exts == null || exts[0] == null)
			exts = new String[]{ "*" };
		
		if(!fileA.isDirectory()) {
			diffquick(fileA, fileB);
			return;
		}
		
		List<File> filesA = DiffQuickUtils.getDirFiles(fileA, exts, bl, recurse);
		List<File> filesB = DiffQuickUtils.getDirFiles(fileB, exts, bl, recurse);
		
		for(File a : filesA)
		{
			String pathA = DiffQuickUtils.getRealtivePath(fileA, a);
			diffquick(a, new File(fileB, pathA));
		}
		
		//diff onlyIn for files of fileB
		for(File b : filesB)
		{
			String pathB = DiffQuickUtils.getRealtivePath(fileB, b);
			if(!new File(fileA, pathB).exists())
				System.out.println("only in:" + pathB);
		}
	}

	public static void diffquick(File fileA, File fileB) 
	{
		if(fileA.equals(fileB))
			return;
		
		if(!fileB.exists()) {
			System.out.println("only in:" + fileA);
			return;
		}
		
		long lenA = fileA.length();
		long lenB = fileB.length();
		InputStream in1 = null;
		InputStream in2 = null;
		if(lenA != lenB)
		{
			System.out.println("diff:\"" + fileA + "\" len: " + lenA + " \"" + fileB + "\" len:" + lenB);
		}
		else
		{
			try
			{
				in1 = new BufferedInputStream(new FileInputStream(fileA));
				in2 = new BufferedInputStream(new FileInputStream(fileB));
				if(!contentEquals(in1, in2))
					System.out.println("diff:\"" + fileA + "\" \"" + fileB + "\"");
	   	 		DiffQuickUtils.close(in1, in2);
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				System.err.println("Can't Determine diff of: " + fileA + " and " + fileB);
				DiffQuickUtils.close(in1, in2);
			}
		}
	}
	
	private static final int EOF = -1;
	/**
	 * copied from apache commons it reads byte by byte to guarantee no out of sync errors while slow it works
	 * NOTE: removed BufferedInputStream checks
	 */
    public static boolean contentEquals(InputStream in1, InputStream in2) throws IOException 
    {
        int ch = in1.read();
        while (EOF != ch) {
            int ch2 = in2.read();
            if (ch != ch2) {
                return false;
            }
            ch = in1.read();
        }

        int ch2 = in2.read();
        return ch2 == EOF;
    }

	private static void help()
	{
		System.out.println("DiffRQ.jar -r dirA dirB");
		System.out.println("-r enables recursion");
		System.out.println("--whitelist=.jar;txt diff only these extensions");
		System.out.println("--blacklist=.java;txt diff all except these extensions");
		System.out.println("--wl alias of --whitelist");
		System.out.println("--bl alias of --blacklist");
		System.exit(0);
	}

}
