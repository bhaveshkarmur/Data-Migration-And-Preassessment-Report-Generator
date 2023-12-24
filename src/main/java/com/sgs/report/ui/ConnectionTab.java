package com.sgs.report.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Properties;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.sgs.tc.extractor.Application;

public class ConnectionTab {
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		connection obj=new connection(args);
	}
}

class connection extends JFrame implements ActionListener
{
	static JTabbedPane tab;
	String[] args;
	JLabel connect;
	JLabel host;
	JTextField get_host;
	JLabel port;
	JTextField get_port;
	JLabel db; 
	JTextField get_db;
	JLabel user;
	JTextField get_user;
	JLabel pass;
	JTextField get_pass;
	JButton con;
	JLabel error;
	public boolean error_show=false; // if database can't connect, make this variable true
	public boolean Connect=false;
	
	public connection(String[] args) throws UnsupportedEncodingException
	{
		tab=new JTabbedPane();
		tab.setBounds(20,10,450,400);
		
		JPanel connection=new JPanel();
		connection.setLayout(null);
		JPanel ReportGenerate=new JPanel();
		ReportGenerate.setLayout(null);
		JPanel console=new JPanel();
		
		tab.add("Connection",connection);
		tab.add("Report",ReportGenerate);
		tab.add("console",console);
		
		this.args=args;
		connect=new JLabel("Connect");
		connect.setBounds(180, 10, 50, 20);
		
		host=new JLabel("Host");
		host.setBounds(60,70,600,20);
		get_host=new JTextField(20);
		get_host.setBounds(120,70,150,20);
		
		port=new JLabel("Port");
		port.setBounds(60,110,600,20);
		get_port=new JTextField();
		get_port.setBounds(120,110,150,20);
		
		db=new JLabel("DataBase");
		db.setBounds(60,150,600,20);
		get_db=new JTextField();
		get_db.setBounds(120,150,150,20);
		
		user=new JLabel("User");
		user.setBounds(60,190,600,20);
		get_user=new JTextField();
		get_user.setBounds(120,190,150,20);
		
		pass=new JLabel("Pass");
		pass.setBounds(60,230,600,20);
		get_pass=new JTextField();
		get_pass.setBounds(120,230,150,20);
		
		Console consoleObj=new Console(); 
		consoleObj.consoleOutput(console);
		
		con=new JButton("Connect");
		con.setBounds(150,290,100,20);	
		con.addActionListener(this);
		
		error=new JLabel();
		error.setBounds(120, 330, 900, 50);
		error.setForeground(Color.RED);
		
		connection.add(connect);
		connection.add(host);
		connection.add(get_host);
		connection.add(port);
		connection.add(get_port);
		connection.add(db);
		connection.add(get_db);
		connection.add(user);
		connection.add(get_user);
		connection.add(pass);	
		connection.add(get_pass);
		connection.add(con);
		connection.add(error);	
		add(tab);

		setLayout(null);
        setBounds(250,90,500,500);
        setVisible(true);
        setDefaultCloseOperation(3);
	}
	
	public void actionPerformed (ActionEvent ae) {
		int value=0;
		if(true)
		{
			 FileReader reader = null;
		     FileWriter writer = null;
		     File file = new File("resources//database.properties");   
		     try {
				reader = new FileReader(file);
				writer = new FileWriter(file);
				Properties p = new Properties();
	            p.load(reader);
	            p.setProperty("host",get_host.getText());	            
	            p.setProperty("port",get_port.getText());
	            p.setProperty("database",get_db.getText());
	            p.setProperty("user",get_user.getText());
	            p.setProperty("pass",get_pass.getText());
	            p.store(writer,"");
	            
	            Application app=new Application();
				value = app.extractData();
				
	      if(value==-1) {
		 GenerateReport.mainMethod(args);
	    }
	    else
	    {
	    	error.setText("Failed to connect to database"); 
	    }
	            
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
		  }
		}  
		 		
		
	}
}