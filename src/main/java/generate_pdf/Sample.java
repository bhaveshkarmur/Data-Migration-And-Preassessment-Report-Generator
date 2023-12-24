package generate_pdf;

import java.util.ArrayList;

//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.PDPageContentStream;
//import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.java.MigrationAssessmentReportsBuilder.utility.*;

@Component
@ComponentScan(basePackages = { "com.java.MigrationAssessmentReportsBuilder.utility" })
public class Sample {
	
	@Autowired
	private TextFunction textFunction;
	
	@Autowired
	private Properties property;
	
	public void createReport() throws Exception{
				
		String reportName=property.getProperty("report.name");
		String path=property.getProperty("report.path");
		
		textFunction.textWritingPage(reportName,path);
		
		int count = Integer.parseInt(property.getProperty("pd.count"));

		for(int i =1; i<=count; i++) {
			String header = property.getProperty("pd.count"+ i +".header");
			textFunction.headerText(header);
			
			int paraCount = Integer.parseInt(property.getProperty("pd.count"+i+".header.para.count"));
			for(int para=1; para<=paraCount; para++) {
				String headerPara = property.getProperty("pd.count"+ i +".header.para");
				textFunction.addPara(headerPara);
			}
			
			int subheadercount = Integer.parseInt(property.getProperty("pd.count"+i+".header.count"));

			for(int j=1; j<=subheadercount; j++) {
				String sub_header = property.getProperty("pd.count"+ i +".header.count"+j);
				textFunction.subHeader(sub_header);
				
				int subheaderParaCount = Integer.parseInt(property.getProperty("pd.count"+i+".header.count"+j+".para_count"));
				for(int k=1;k<=subheaderParaCount;k++)
				{
					String sub_para = property.getProperty("pd.count"+ i +".header.count"+j+".para_count"+k+".para");
					textFunction.addSubPara(sub_para);
				}
				boolean check=Boolean.valueOf(property.getProperty("pd.count"+i+".header.count"+j+".table"));
				if(check==true)
				{
					String tableName = property.getProperty("pd.count"+i+".header.count"+j+".table.name");
					ArrayList<ArrayList<String>> data = textFunction.tableData(property.getProperty("pd.count"+i+".header.count"+j+".table.name"));
					String direction = property.getProperty("pd.count"+i+".header.count"+j+".table.direction");
					
					int raw = data.get(0).size();
					int column = data.size();
					
					if(direction.equals("column")) {
						textFunction.headerselector(raw, column, "column");
					} else {
					
						textFunction.headerselector(column,raw, "raw");
					}
				}
			}		
		}
		
		textFunction.generateFirstPage(reportName);
		System.out.println("Report Generated");
	
	}
}
