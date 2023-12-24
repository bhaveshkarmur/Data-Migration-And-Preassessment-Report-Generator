package com.sgs.report.ui;

import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import com.java.MigrationAssessmentReportsBuilder.*;



public class GenerateReport {

	public static void mainMethod(String[] args) throws UnsupportedEncodingException {
		Report obj=new Report(args);
	}
}

class Report extends JFrame implements ActionListener
{
	JTabbedPane tab;
	JLabel report;
	JButton generate;
	JLabel success;
	JLabel location;
	String[] args;
	
	public Report(String[] args) throws UnsupportedEncodingException
	{
		tab=new JTabbedPane();
		tab.setBounds(20,10,450,400);
		
	//	JPanel connection=new JPanel();
		JPanel ReportGenerate=new JPanel();
		ReportGenerate.setLayout(null);
		JPanel console=new JPanel();
		
//		tab.add("Connection",connection);
		tab.add("Report",ReportGenerate);
		tab.add("console",console);
		
		Console consoleObj=new Console(); 
		consoleObj.consoleOutput(console);

		this.args=args;
		report=new JLabel("report");
		report.setBounds(180,10,50,20);
		
		generate=new JButton("Generate Data Assessment Report");
		generate.setBounds(90,60,250, 50);
		
		success=new JLabel();
		success.setBounds(45,140, 250, 50);
		success.setForeground(new Color(0, 202, 25));
		
		location=new JLabel();
		location.setBounds(45,160, 350, 50);
		ReportGenerate.add(report);
		ReportGenerate.add(generate);
		ReportGenerate.add(success);
		ReportGenerate.add(location);
		
		add(tab);

		generate.addActionListener(this);
		
		setLayout(null);
		setBounds(250,90,500,500);
		setVisible(true);
		setDefaultCloseOperation(3);
	}

	public void actionPerformed (ActionEvent ae) {
		if(ae.getSource()==generate)
		{
			//create master method
			
			try {
				
				MigrationAssessmentReportsBuilderApplication.mainMethod(args);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			success.setText("Report generated successfully.");
			location.setText("The Generated Report saved at Location == D:\\ ");
		}
		
	}
}
