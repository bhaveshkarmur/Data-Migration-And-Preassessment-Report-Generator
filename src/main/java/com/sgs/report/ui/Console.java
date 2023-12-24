package com.sgs.report.ui;


import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Console {
		
 public void consoleOutput(JPanel console) throws UnsupportedEncodingException {
		
	   JTextArea text=new JTextArea(20,43);
	   text.setEditable(false);
       JScrollPane scroll=new JScrollPane(text,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
       
		console.add(scroll);
		PrintStream stream=new PrintStream(new CustomOutputStream(text));
	    System.setOut(stream);
		System.setErr(stream);			
	}
}
