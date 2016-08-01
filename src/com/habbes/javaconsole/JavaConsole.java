package com.habbes.javaconsole;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.UIManager.*;
import java.io.*;

class JavaConsole {
	
	JFrame frame;
	JPanel mainPanel;
	JTextArea editor;
	JTextArea output;
	JTextField argsField;
	JTextField inputField;
	int currentTabLevel = 0;
	Process runningApp;
	JavaCode currentCode;
	
	public static void main(String[] args){
		/*try {
            // Set System L&F
			UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
        
		} 
		catch (Exception ex){
			try {
		        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		            if ("Nimbus".equals(info.getName())) {
		                UIManager.setLookAndFeel(info.getClassName());
		                break;
		            }
		        }
		    } catch (Exception e) {
		        // If Nimbus is not available, you can set the GUI to another look and feel.
		    }
		}*/

		try {
		        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		            if ("Nimbus".equals(info.getName())) {
		                UIManager.setLookAndFeel(info.getClassName());
		                break;
		            }
		        }
		    } catch (Exception e) {
		        // If Nimbus is not available, you can set the GUI to another look and feel.
		    }
		
		new JavaConsole().init();
		
	}
	
	public void init(){
		frame = new JFrame();
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		frame.setContentPane(mainPanel);
		
		//used for padding and margins
		Insets defPadding = new Insets(10, 10, 10, 10);
		
		//editor textarea
		editor = new JTextArea(20,80);
		editor.setMargin(defPadding);
		Font font = new Font("courier new", Font.PLAIN, 12);
		editor.setFont(font);
		editor.setLineWrap(true);
		editor.setWrapStyleWord(true);
		editor.addKeyListener(new editorKeyListener());
		JScrollPane editorScroller = new JScrollPane(editor);
		editorScroller.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		editorScroller.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		mainPanel.add(editorScroller);
		
		//run button and text
		JPanel runPanel = new JPanel();
		runPanel.setLayout(new FlowLayout());
		runPanel.add(new JLabel("Args:"));
		argsField = new JTextField(30);
		runPanel.add(argsField);
		JButton runButton = new JButton("Run");
		runButton.addActionListener(new RunListener());
		runPanel.add(runButton);
		
		mainPanel.add(runPanel);
		
		//output textarea
		output = new JTextArea(10, 0);
		output.setLineWrap(true);
		output.setWrapStyleWord(true);
		output.setEnabled(false);
		output.setFont(font);
		output.setBackground(new Color(0, 0, 0));
		output.setForeground(Color.WHITE);
		output.setMargin(defPadding);
		JScrollPane outputScroller = new JScrollPane(output);
		outputScroller.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		outputScroller.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		outputScroller.setAutoscrolls(true);
		mainPanel.add(outputScroller);
		

		//send input field
		JPanel inputPanel = new JPanel();
		//inputPanel.setLayout(new BorderLayout());
		inputPanel.add(new Label("Input:"));
		inputField = new JTextField(30);
		inputPanel.add(inputField);
		JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new SendListener());
		inputPanel.add(sendButton);
		
		mainPanel.add(inputPanel);
		
		//menu
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("Menu");
		JMenuItem saveSourceMenu = new JMenuItem("Save Source");
		saveSourceMenu.addActionListener(new SaveSourceListener());
		JMenuItem saveOutputMenu = new JMenuItem("Save Output");
		saveOutputMenu.addActionListener(new SaveOutputListener());
		
		fileMenu.add(saveSourceMenu);
		fileMenu.add(saveOutputMenu);
		menuBar.add(fileMenu);
		
		frame.setJMenuBar(menuBar);
		
		//frame
		frame.setTitle("Java Console");
		//frame.setSize(700, 500);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
	}
	
	private void saveCode(File file, String code) throws IOException {
		BufferedWriter bf = new BufferedWriter(new FileWriter(file));
		bf.write(code);
		bf.close();
	}
	
	private int compile(String code) throws Exception {
		currentCode = new JavaCode(code);
		output.append("\n-- Compiling  " + currentCode.getTempFile().getAbsolutePath() + " ...\n");
		Process p = currentCode.compile();
		consumeOutput(p);
		output.append("\n-- Compiler Exit Code: " + p.exitValue() + "\n");
		return p.exitValue();
	}
	
	private int runCurrentCode() throws Exception {
		output.append("\n-- Running  " + currentCode.getClassFile().getAbsolutePath() + " ...\n");
		runningApp = currentCode.run(argsField.getText().trim().split(" "));
		consumeOutput(runningApp);
		int exitCode = runningApp.exitValue();
		output.append("\n-- App Exit Code: " + exitCode + "\n");
		runningApp = null;
		return exitCode;
	}
	
	private void consumeOutput(Process p) throws IOException {
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(p.getInputStream()));
		//get normal output
		String line;
		while((line = reader.readLine()) != null){
			output.append("out >> " + line + "\n");
		}
		
		reader.close();
		
		//get errors
		reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		while((line = reader.readLine()) != null){
			output.append("err >> " + line + "\n");
		}
		
		reader.close();
		
	}
	
	private void runCode(String code) throws Exception{
		if(compile(code) == 0) runCurrentCode();
		currentCode.cleanUp();
	}
	
	public void sendUserInput(String input) throws IOException{
		if(runningApp == null) return;
		BufferedWriter writer = 
				new BufferedWriter(new OutputStreamWriter(runningApp.getOutputStream()));
		writer.write(input);
		output.append("in << " + input + "\n");
		writer.close();
	}
	
	class RunListener implements ActionListener {
		public void actionPerformed(ActionEvent ev){
			try {
				runCode(editor.getText());
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	class SendListener implements ActionListener {
		public void actionPerformed(ActionEvent ev){
			try {
				sendUserInput(inputField.getText());
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	class SaveSourceListener implements ActionListener {
		public void actionPerformed(ActionEvent ev){
			JFileChooser chooser = new JFileChooser();
			chooser.showSaveDialog(frame);
			File f = chooser.getSelectedFile();
			try {
				saveCode(f, editor.getText());
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	class SaveOutputListener implements ActionListener {
		public void actionPerformed(ActionEvent ev){
			JFileChooser chooser = new JFileChooser();
			chooser.showSaveDialog(frame);
			File f = chooser.getSelectedFile();
			try {
				saveCode(f, output.getText());
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	class editorKeyListener implements KeyListener {
		public void keyPressed(KeyEvent ev){
			
		}
		
		public void keyReleased(KeyEvent ev){
			
		}
		
		public void keyTyped(KeyEvent ev){
			
			if(ev.getKeyChar() == '\n'){
				//automatic indentation when the a new line is entered
				
				/**
				 * when a new line is entered, check how many continuous tab characters
				 * begin the previous line, insert as many tab characters at the beginning
				 * of the new line
				 */
				
				//get line position
				int endLine = editor.getCaretPosition() - 1;
				int pos = endLine - 1;
				while(pos > 0 && editor.getText().charAt(pos) != '\n'){
					//pos goes back to the start of the previous line
					pos--;
				}
				//get sequence of tabs
				int tabs = 0;
				if(pos < 0)
					return;
				//pos is at the '\n' before the beginning of the previous line, place
				//it at the first char of the previous line
				pos++;
				//check whether the line starts with tab chars and count them
				while(pos < endLine && editor.getText().charAt(pos) == '\t'){
					tabs++;
					pos++;
				}
				//place as many tab chars at the beginning of the newly inserted line
				//as there are on the previous line
				for(int i = 0; i < tabs; i++){
					editor.insert("\t", editor.getCaretPosition());
				}
			}
		}
	
	}
	
	
}