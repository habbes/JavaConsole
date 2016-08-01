package com.habbes.javaconsole;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//class to compile and execute a java script code
class JavaCode {
	
	private String source;
	private String className;
	private File tempFile;
	
	public JavaCode(String source, File temp){
		this.source = source;
		tempFile = temp;
		setClassName();
	}
	public JavaCode(String source){
		this(source, new File("test.java"));
	}
	
	private void setClassName(){
		//parse through the code to get the name of the class
		Pattern patt = Pattern.compile(
				"(\\s*)(class)(\\s+)(\\w*)",
				Pattern.CASE_INSENSITIVE);
		Matcher match = patt.matcher(source);
		if(!match.find()){
			this.className = "_ClassNotFound";
		}
		//System.out.println(match.)
		this.className = match.group(4);
		//this.className = "Test";
	}
	
	public String getClassName(){
		return className;
	}
	
	public File getClassFile(){
		return new File(className + ".class");
	}
	
	//the source code
	public String getSource(){
		return source;
	}

	public void setCode(String code){
		source = code;
		//update class name;
		setClassName();
	}
	
	public File getTempFile(){
		return tempFile;
	}
	
	//default compile
	public Process compile() throws IOException{
		return compile(new String[0]);
	}
	
	public Process compile(String[] args) throws IOException{
		return compile("javac", args);
	}
	
	//compile the source code
	public Process compile(String javac, String[] args) throws IOException {
		BufferedWriter bf = new BufferedWriter(new FileWriter(tempFile));
		bf.write(source);
		bf.close();
		ProcessBuilder pb = new ProcessBuilder();
		List <String> cmd = pb.command();
		cmd.add(javac);
		for(int i = 0; i < args.length; i++){
			cmd.add(args[i]);
		}
		cmd.add(tempFile.getAbsolutePath());
		Process proc = pb.start();
		//tempFile.delete();
		return proc;
		
	}
	
	public Process run() throws IOException {
		return run(new String[0]);
	}
	
	public Process run(String[] args) throws IOException{
		return run("java", args);
	}
	
	public Process run(String java, String[] args) throws IOException {
		ProcessBuilder pb = new ProcessBuilder();
		List <String> cmd = pb.command();
		cmd.add(java);
		cmd.add(className);
		for(String arg : args){
			cmd.add(arg);
		}
		Process p = pb.start();
		return p;
	}
	
	public void cleanUp(){
		if(tempFile.exists())
			tempFile.delete();
		
		File f = getClassFile();
		if(f.exists())
			f.delete();
	}
	
	
	
	
	
}