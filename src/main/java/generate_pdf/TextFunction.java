package generate_pdf;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import javax.sql.DataSource;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

//import com.java.database.dataBase;

@Component
@ComponentScan(basePackages = { "com.java.MigrationAssessmentReportsBuilder.utility" })
public class TextFunction {

    static String reportName = "Teamcenter Data Assessment Report";
    static String reportPath;
    static int y = 700; // initial y value of X-Y plane and it points the current line so
    static int prevX = 50; // to get idea of previous starting point(if we have to print table after
                           // subpara then we should know that from where we start printing table whether
                           // from after para or subpara

    static int count = 0; // no of para (if more than one para then need to put space after each)
    static int page_no = 0; // pageno
    static int tempNo = 0;
    static int textPageNo = 2; // we starting to writing our context from pageno 2
    static int addedPageForIndex = 0; // increase when we add page for index printing
    static PDDocument doc;
    static PDPageContentStream stream;
    static PDFont cambriaBold;
    static PDFont calibriBold;
    static PDFont calibri;
    static String FileName;

    static PDFont headerFont; // header font type
    static int headerFontSize = 11; // header font size
    static PDFont bodyFont; // body font type
    static int fontSize = 11; // body text fontsize
    static int spaceX = 5; // space beetween table border and text in x-ex
    static int spaceY = 15; // space in y-ex
    static int Rh = 0; // require height
    static String select = ""; // these is for input raw or column
    static int cellHeight; // cellHeight
    static int cellWidth; // cellWidth
    static int headerCellWidth = 0;
    static int bodyCellWidth = 0;
    static int textSize = 0;
    static int height = 0;
    static String[] wrt = null;
    static String s = "";
    static int requireHeight = 0;
    static int raw = 0;
    static int column = 0;
    static ArrayList<ArrayList<String>> a = null;
    static ArrayList<String> headerTextArray = new ArrayList<>();
    static ArrayList<String> bodyTextArray2 = new ArrayList<>();
    static ArrayList<ArrayList<String>> bodyTextArray = null;
    static ArrayList<Integer> maxColumnWidth = null;
    static ArrayList<Integer> maxUsedColumnWidth = null;
    static ArrayList<Integer> columnNumber = null;
    static String headerText = "";
    static String bodyText = "";
    static float headerSize = 0;
    static float bodySize2 = 0;
    static ArrayList<String> bodySize = null;
    static String[] maxRawBodyString = null;

    static ArrayList<ArrayList<String>> subHeader = new ArrayList<>();
    static ArrayList<String> header = new ArrayList<>();
    static ArrayList<Integer> headerPageNo = new ArrayList<>();
    static ArrayList<ArrayList<Integer>> subHeaderPageNo = new ArrayList<>();
    static int headerCount = -1; // because subheder is stored in index 0
    static int SubheaderCount;
 // @Autowired
    //private dataBase dataBase;

    @Autowired
   private DataSource dataSource;

    public TextFunction() {
        doc = new PDDocument();
    }

    public void textWritingPage(String reportName,String path) throws IOException {
        reportPath=path;
        FileName = reportName; // gave temporary file name
        File first = new File("src/font/PDF_Template (1).pdf"); // open page for writing
        doc = PDDocument.load(first); // open existing file
        PDPage page = doc.getPage(page_no);
        stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true); // generate //
                                                                                                        // stream
        count = 0;
    }

    public void addPage() throws IOException {
        fileSaving(); // close stream and save file

        File oldFile = new File(reportPath + FileName + ".pdf"); // existing file
        File newPage = new File("src/font/PDF_Template (1).pdf"); // pagefile which we want to add
        mergePdfPage(oldFile, newPage, FileName); // merge newPage into oldFile
        page_no++; // add page then increase page_no
        streamGenerator(); // generate stream for writing in current page
    }

    public void fileSaving() throws IOException {
        stream.close();
        doc.save(reportPath+ FileName + ".pdf");
        doc.close();
    }

    public void streamGenerator() throws IOException {

        File first = new File(reportPath + FileName + ".pdf");
        doc = PDDocument.load(first); // open existing file
      
        PDPage page = doc.getPage(page_no);
        stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
        y = 700; // set y to top of the page
        count = 0; // no need to pass para count to next page because it will take initial space
                   // using default y
    }

    public void generateFirstPage(String reportName) throws Exception {

        stream.close(); // close stream
        doc.save(reportPath + FileName + ".pdf"); // save oldfile
      
        FileName = "TempFile";
        File oldFile = new File("src/font/firstpage.pdf"); // firstpage
        File newPage = new File("src/font/PDF_Template (1).pdf"); // second page
        mergePdfPage(oldFile, newPage, FileName); // merge newPage into oldFile
        setFont(); // set fonts
        tempNo = page_no; // store page no in tempNO
        page_no = 0; // set page_no=0
        firstPage(reportName); // first page
    }

    public void mergePdfPage(File oldFile, File newPage, String FileName) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(reportPath + FileName + ".pdf"); // file save Location
        merger.addSource(oldFile);
        merger.addSource(newPage);
        merger.mergeDocuments();
    }

    public void setFont() throws IOException {

        File font0 = new File("src/font/Calibri Bold.ttf");
        calibriBold = PDType0Font.load(doc, font0);

        File font1 = new File("src/font/Calibri.ttf");
        calibri = PDType0Font.load(doc, font1);

        File font2 = new File("src/font/cambriab.ttf");
        cambriaBold = PDType0Font.load(doc, font2);

    }

    public void firstPage(String reportName) throws Exception {

        streamGenerator(); // stream generate and pointer set in page_no=0
        projectName(); // project name
        date(); // printing date of report generation
        logo(); // printing logo
        fileSaving(); // save file because firstpage is complete for save change save file
        indexPage(); // these methd print only header in the index page
        fileSaving(); // save file for save changes
        
        FileName=reportName;
        File first = new File(reportPath+"TempFile.pdf"); // firstFile
        File second = new File(reportPath+FileName+".pdf"); // secondFile
        
        mergePdfPage(first, second, FileName); // merge both files
        page_no = 1; // because we want to print header and subheader page no in page_no=1

        indexPageHeaderPageNo(); // these method print header and subheader page_no
        page_no = tempNo + 2 + addedPageForIndex; // we calculate all page of currunt pdf
        fileSaving(); // save file
        headerInAllPage(); // header in all pdfpage
        fileSaving(); // save file
        
        doc.close();     //add this code
        File file =new File(reportPath+"TempFile.pdf");
        file.delete();
 
    }

    public void projectName() throws IOException {

        stream.beginText();
        stream.newLineAtOffset(120, 510); //
        stream.setFont(calibri, 24); // fonType and font Size
        stream.showText(reportName); // printing report name
    }

    public void date() throws IOException, ParseException {

        String pattern = "dd MMMMMMMMMMMMMM yyyy"; //  pattern
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new java.util.Date());
        stream.setFont(calibriBold, 11); // fonType and font Size
        stream.newLineAtOffset(-10, -268); // here we gave (-) value because stream is not closed therefore pointer
                                           // count from old position
        stream.showText(date); // print date
        stream.endText();
    }

    public void logo() throws Exception {
        PDImageXObject logo = PDImageXObject.createFromFile("src/font/logo.png", doc);
        stream.drawImage(logo, 390, 250, 100, 100); // (object,x,y,lenght,height)
    }

    public void indexPage() throws IOException {

        y = 700; // set Y
        prevX = 60; // set x
        page_no++; // we want to print index innext page
        File first = new File(reportPath + FileName + ".pdf");
        doc = PDDocument.load(first);
        PDPage page = doc.getPage(page_no); // get page_no=1 for index printing
        stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true); // create new
                                                                                                        // stream
        indexPageContentHeader(); // printing table contents
        fontStyle(); // set font Style
        printingHeaderInIndexPage(); // printing headers
        stream.endText();
    }

    public void indexPageContentHeader() throws IOException {
        stream.beginText();
        setFont(); // when we create new doc then need to set font
        stream.setFont(calibriBold, 14);
        stream.setNonStrokingColor(0, 76, 153);
        stream.newLineAtOffset(250, y);
        stream.showText("Table of Contents");
        stream.endText();
    }

    public void fontStyle() throws IOException {
        stream.beginText();
        setFont();
        stream.setFont(calibri, 11);
        stream.setNonStrokingColor(Color.black);
    }

    public void printingHeaderInIndexPage() throws IOException {

        int y1 = y = y - 30;
        int x1 = prevX = 60;
        for (int i = 0; i < header.size(); i++) {

            stream.newLineAtOffset(x1, y1);
            if (y <= 120) {
                addedPageForIndex++; // when we add new page for index increase
                indexInSecondPage(); // for add new page
                y1 = y = 700;
                x1 = prevX;
                stream.newLineAtOffset(x1, y1);
            }
            stream.showText(header.get(i)); // printing header
            stream.newLine(); // new line
            int spacey = 20;
            int spacex = 15;
            x1 = -15;
            for (int j = 0; j < subHeader.get(i).size(); j++) {
                y1 = spacey;

                stream.newLineAtOffset(spacex, -y1);
                if (y <= 130) {
                    addedPageForIndex++; // when we add new page for index increase
                    indexInSecondPage();
                    y1 = y = 700;
                    x1 = prevX + 15;
                    stream.newLineAtOffset(x1, y1);
                    x1 = 0;
                }
                stream.showText(subHeader.get(i).get(j)); // printing sub header
                stream.newLine(); // new line
                spacex = 0;
                y -= spacey;
            }
            x1 = -15;
            y1 = -spacey;
            y -= spacey;
        }
    }

    public void indexInSecondPage() throws IOException {
        stream.endText();
        stream.close();
        addPage();
        fontStyle();
    }

    public void indexPageHeaderPageNo() throws IOException {

        y = 700; // set Y
        prevX = 60; // set x

        File first = new File(reportPath + FileName + ".pdf");
        doc = PDDocument.load(first);
        PDPage page = doc.getPage(page_no); // get page_no=1 for index printing
        stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true); // create new
                                                                                                        // stream
        indexPageContentHeader(); // printing table contents
        fontStyle(); // set font Style
        printingHeaderPageNo(); // printing headers
        stream.endText();
    }

    public void printingHeaderPageNo() throws IOException {

        int y1 = y = y - 30;
        int x1 = prevX = 60;
        for (int i = 0; i < header.size(); i++) {

            stream.newLineAtOffset(x1, y1);
            if (y <= 120) {
                secondPageForpageNo(); // generate new stream in second page
                y1 = y = 700;
                x1 = prevX;
                stream.newLineAtOffset(x1, y1);
            }
            int number = headerPageNo.get(i) + addedPageForIndex;
            String text = header.get(i) + number;
            stream.showText(header.get(i)); // printing header
            printingDot(text, 60); // printing dots. here we gave 60 which is space before header
            String pageno = String.valueOf(headerPageNo.get(i) + addedPageForIndex);
            stream.showText(pageno); // printing page_no
            stream.newLine();
            int spacey = 20;
            int spacex = 15;
            x1 = -15;
            for (int j = 0; j < subHeader.get(i).size(); j++) {
                y1 = spacey;

                stream.newLineAtOffset(spacex, -y1);
                if (y <= 130) {
                    secondPageForpageNo();
                    y1 = y = 700;
                    x1 = prevX + 15;
                    stream.newLineAtOffset(x1, y1);
                    x1 = 0;
                }
                number = subHeaderPageNo.get(i).get(j) + addedPageForIndex;
                text = subHeader.get(i).get(j) + number;
                stream.showText(subHeader.get(i).get(j));
                printingDot(text, 75);
                pageno = String.valueOf(subHeaderPageNo.get(i).get(j) + addedPageForIndex);
                stream.showText(pageno);
                stream.newLine();
                spacex = 0;
                y -= spacey;
            }
            x1 = -15;
            y1 = -spacey;
            y -= spacey;
        }
    }

    public void secondPageForpageNo() throws IOException {
        stream.endText();
        stream.close();
        fileSaving();
        page_no++;
        streamGenerator();
        fontStyle();
    }

    public void printingDot(String text, int space) throws IOException {
        float size = 11 * calibri.getStringWidth(text) / 1000;
        float blankspace = 612 - space - 50 - size; // here we fix prevX=60
        int dot = numberOfDot(blankspace);

        for (int k = 0; k < dot; k++) {
            stream.showText(".");
        }
    }

    public int numberOfDot(float blankspace) throws IOException {
        float dotSpace = 11 * calibri.getStringWidth(".") / 1000;
        int dot = (int) (blankspace / dotSpace);
        return dot;
    }

    public void headerInAllPage() throws IOException {
        File first = new File(reportPath + FileName + ".pdf");
        doc = PDDocument.load(first);

        for (int i = 0; i <= page_no; i++) {
            PDPage page = doc.getPage(i);
            stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
            stream.beginText();
            setFont(); // we need to set font because new doc generated
            stream.setFont(calibri, 9);
            stream.setNonStrokingColor(Color.white);
            stream.newLineAtOffset(110, 780);
            String no = String.valueOf(i);
            stream.showText(no);
            stream.setNonStrokingColor(Color.black);
            stream.newLineAtOffset(50, -1);
            stream.showText(reportName);
            stream.endText();
            stream.close();
        }
    }

    public void headerText(String text) // print header
            throws Exception {
        text = headerCount + 2 + ". " + text; // for prinding header number we add 2 in headerCount because intitial
                                              // headereCount=-1
        subHeader.add(new ArrayList<String>()); // creating new array when header is calling
        subHeaderPageNo.add(new ArrayList<Integer>()); // creating new array when header is calling
        header.add(text);
        headerPageNo.add(page_no + textPageNo); // because we start first data writing therefore we store from
                                                // page_no==2
        prevX = 50;
        Color colorName = new Color(0, 76, 153); // blue header color
        int maxChar = 75;
        int fontsize = 14;
        callAddText1(text, fontsize, colorName, maxChar);
        headerCount++; // this is for header number increase
        SubheaderCount = 1; // when new header printing then subheadercount starting from 1
    }

    public void subHeader(String text)// print subheader
            throws Exception {
        text = headerCount + 1 + "." + SubheaderCount + ". " + text; // header add 1 because hedercount=0
        subHeader.get(headerCount).add(text);
        subHeaderPageNo.get(headerCount).add(page_no + textPageNo); // because we start first data writing therefore we
                                                                    // store from page_no==2
        prevX = 70;
        Color colorName = new Color(14, 106, 210);
        int maxChar = 79;
        int fontsize = 13;
        callAddText1(text, fontsize, colorName, maxChar);
        SubheaderCount++; // increase subheadercount
    }

    public void callAddText1(String text, int fontsize, Color colorName, int maxChar)
            throws Exception {
        count = 0;
        y = y - 7; // put space before header and sub header for better view
        setFont();
        PDFont fontName = cambriaBold;
        String textType = "header";
        addText(text, fontName, fontsize, colorName, maxChar, textType);
        y = y - 5; // put space after header and sub header for better view
    }

    public void addPara(String text) // for adding para
            throws Exception {

        prevX = 50; // margin of para is 50
        int maxChar = 111; // no of char in this line
        count++; // increase count so we can know one para added
        callAddText(text, maxChar); // for reusing same code
    }

    public void addSubPara(String text)
            throws Exception {
        prevX = 70;
        int maxChar = 106;
        callAddText(text, maxChar);
    }

    public void callAddText(String text, int maxChar)
            throws Exception {
        if (count > 0) {// it will check if just before calling this method whether para is added or not
            y = y - 7; // if yes then put space
        }
        setFont();
        PDFont fontName = calibri;
        Color colorName = Color.BLACK;
        int fontsize = 11;
        String fontType = "paragraph";
        count++; // increase for next para
        addText(text, fontName, fontsize, colorName, maxChar, fontType); // writing text
    }

    public void addText(String text, PDFont fontName, int fontsize, Color colorName, int maxChar,
            String textType) throws Exception {
        PDFont font = fontName;
        if (y <= 100) // Check space is less than 100
        {
            stream.close(); // otherwise stream stay open and pdf can't read from open stream
            addPage(); // create new page when current is finished
            font = setFontStyle(textType); // because new stream generated
        }
        stream.beginText();
        int y1 = y; // y1 is changing and y we have to maintain so,y1 is created
        int x1 = prevX; // same as above
        String[] wrt = null;
        
        wrt = org.apache.commons.text.WordUtils.wrap(text, maxChar).split("\\r?\\n"); // maxChar is no. of character(got
                                                                                      // from trial & error in single
                                                                                      // line and this function split
                                                                                      // text using wide space
        stream.setFont(font, fontsize);
        stream.setNonStrokingColor(colorName);

        for (int i = 0; i < wrt.length; i++) {
            if (y <= 100) // check here also because page can finish during para printing also
            {
                stream.endText();
                stream.close();
                addPage();
                stream.beginText();
                y1 = y; // set previous value so,stream print in previous page flow
                x1 = prevX;

                font = setFontStyle(textType); // because new stream generated
                stream.setFont(font, fontsize);
                stream.setNonStrokingColor(colorName);
            }
            stream.newLineAtOffset(x1, y1); // new line cordinate
            stream.showText(wrt[i]);
            y1 = -15; // next line print from 15 space down and it calculate from just after finished
                      // previous line
            y = y - 15; // set pointer to next line
            x1 = 0; // start from leftside
        }
        stream.endText();
    }

    public PDFont setFontStyle(String textType) throws IOException {
        setFont();
        if (textType == "paragraph") {
            return calibri;
        } else {
            return cambriaBold;
        }
    }

 public void headerselector(int row, int col, String header) throws Exception {
        
    	cellWidth=600;
    	cellHeight=700;
        setFont();
        headerFont = cambriaBold;
        bodyFont = calibri;
        raw = row;
        column = col;
        select = header;

        if (select == "raw") {

            cellWidth = (612 - 2 * prevX) / column; // these calculate cellWidth for one column
            headerInRawDataStore(); // for table data inserting in arrayList and array
            if (column == 2) {
                headerInRawForTwoColumn(); // these method is for only two columns
            } else {
                headerInRaw(); // these method is for more than two columns
            }
        } else {
            headerInColumnDataStore(); // for table data inserting in arrayList and array
         //   cellWidthOfFirstColumn(); // calculate required cellWidth for first index column
            cellWidthColumn(); // cellWidth for other column
            if (column == 2) {
                maxColumnWidth();
                calculateWidthFor2column();
            }
            columnHeader(); // for headercell printing
            columnBody(); // this method print bodycell
        }
        y = y - 20; // put space after table
    }
    public ArrayList<ArrayList<String>> tableData(String tableName) throws Exception {

        Statement stmt = null;
        a = new ArrayList<ArrayList<String>>(); // create arraylist

        stmt = dataSource.getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        String query = "SELECT * FROM " + tableName;
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData metaData = rs.getMetaData();

        int arraylistindex = 0;
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            a.add(new ArrayList<String>());
            a.get(arraylistindex).add(metaData.getColumnName(i));
            rs.first();
            a.get(arraylistindex).add(rs.getString(i));
            while (rs.next()) {
                a.get(arraylistindex).add(rs.getString(i));
            }
            arraylistindex++;
        }
        return a;
    }

    public void createNewPage() throws IOException {
        stream.close();
        addPage();
        setFont(); // set font because new stream generated
        headerFont = cambriaBold;
        bodyFont = calibri;
    }

    public void cellWidthOfFirstColumn() throws IOException {
        maxColumnWidth = new ArrayList<Integer>();
        for (int i = 0; i < 1; i++) {
            int maxWidth = (int) ((headerFontSize * headerFont.getStringWidth(headerTextArray.get(i)) / 1000) + 2 * spaceX) + 2;

            for (int j = 0; j < bodyTextArray.get(i).size(); j++) {
                if (maxWidth < ((fontSize * bodyFont.getStringWidth(bodyTextArray.get(i).get(j)) / 1000) + 2 * spaceX + 2) + 2) {
                    maxWidth = (int) ((fontSize * bodyFont.getStringWidth(bodyTextArray.get(i).get(j)) / 1000) + 2 * spaceX + 2);
                }
            }
            maxColumnWidth.add(maxWidth + 10);
        }
    }

    public void cellWidthColumn() throws IOException {

    	int tableWidth=0;
    	maxColumnWidth = new ArrayList<Integer>();
    	for(int i=0; i<headerTextArray.size(); i++) {
    		int max=(int) (headerFontSize * headerFont.getStringWidth(headerTextArray.get(i)) / 1000) + (2 * spaceX)+1;
    		
    		for(int j=0; j<bodyTextArray.get(i).size(); j++) {
   
    		    if(max<(fontSize * bodyFont.getStringWidth(bodyTextArray.get(i).get(j)) / 1000) + 2 * spaceX+1 ) {
    			       max=(int) ((fontSize * bodyFont.getStringWidth(bodyTextArray.get(i).get(j)) / 1000) + 2 * spaceX)+1;
    		    }
    		}
    		      tableWidth+=max+10;
    		       maxColumnWidth.add(max+10);		       
    	}
    cellWidthAdjustment(tableWidth);
    	
 }
    public void cellWidthAdjustment(int tableWidth) throws IOException {
    	if(tableWidth>612-2*prevX){
   		 cellWidthOfFirstColumn();
   	
   		 cellWidth=(612-2*prevX-maxColumnWidth.get(0))/(column-1);
   		 for(int i=0; i<column; i++) {
   		maxColumnWidth.add(cellWidth);
   		 }
   	} else if(tableWidth<60*(612-2*prevX)/100) {
   	
   		cellWidth = 60*(612 - 2 * prevX - maxColumnWidth.get(0)) /(100*(column - 1));
   		for(int i=1; i<column; i++) {
   			
   			if(maxColumnWidth.get(i)>cellWidth) {
   				
   			}else {
  			 maxColumnWidth.set(i,cellWidth);
   			}
  		 }
   		
   }
    }


    public void headerInRawDataStore() { // these method is for rawHeader only
        headerTextArray = new ArrayList<>();
        bodyTextArray2=new ArrayList<>();
        bodyTextArray = new ArrayList<ArrayList<String>>(); // we declare arraylist of arrayList
        for (int i = 0; i < raw; i++) { // here header is raw therefore header are raw times
            ArrayList<String> data = a.get(i); // we get data for first raw
            bodyTextArray.add(new ArrayList<String>()); // creating arrayList of arraylist
            for (int j = 0; j < column; j++) {
                if (j == 0) {
                    headerTextArray.add(data.get(j)); // we put header into a headerTexArrayList
                } else {
                    if (column == 2) { // when only two colums in rawhwader then we store data in bodyTextArray2
                        bodyTextArray2.add(data.get(j)); // arraylist
                    } else {
                        bodyTextArray.get(i).add(data.get(j)); // we put bodytext into a bodyTextArray
                    }
                }
            }
        }
    }

    public void headerInRawForTwoColumn() throws Exception {
        int x = prevX;
        cellWidthOfTwoColumn(); // these method decide cellWidth of header and bodycell
        for (int i = 0; i < raw; i++) {
            headerText = headerTextArray.get(i); // header text
            bodyText = bodyTextArray2.get(i); // body text
            cellWidth = headerCellWidth;
            int h = headerHeight(headerText); // these method return headercell require height
            cellWidth = bodyCellWidth;
            int b = bodyHeight(bodyText); // these method return rawbodycell require height
            if (h > b) {
                Rh = h;
            } else {
                Rh = b;
            }
            x = prevX;
            cellHeight = Rh; // required height

            cellWidth = headerCellWidth;
            if (y < 100 + cellHeight) { // if space is not available then create new page
                createNewPage();
            }
            headerTextLength(headerText); // text amount contained by in one line in headercell
            cellHeight = Rh;
            headerStyle(headerText, x); // headercell
            x = x + cellWidth;
            cellWidth = bodyCellWidth;

            bodyCellStyle(bodyText, x, y); // bodycell
            y = y - Rh;
        }
    }

    public void cellWidthOfTwoColumn() throws Exception {
        headerCellWidth(); // these method calculate max headercell width
        bodyCellWidthForTwoColumn(); // these method calculate max bodycell width
        headerCellWidth = (int) headerSize + 1; // headersize is max headercellwidth
        bodyCellWidth = (int) (bodySize2 + 1) + 10;

        if ((headerSize + bodySize2 > 60 * (cellWidth * column) / 100)) { // here we can 40% width to header
            headerCellWidth = 40 * (cellWidth * column) / 100;
            bodyCellWidth = 60 * (cellWidth * column) / 100;

        } else if (headerSize + bodySize2 <= 60 * (cellWidth * column) / 100) {

            if (headerSize > 30 * (cellWidth * column) / 100) {
                headerCellWidth = (int) headerSize + 1;
            } else {
                headerCellWidth = 30 * (cellWidth * column) / 100;
            }
            bodyCellWidth = 40 * (cellWidth * column) / 100;
        }
    }

    public void headerInRaw() throws Exception {
        int x = prevX;
        headerCellWidth(); // these method calculate max headercell width
        bodyCellWidth(); // these method calculate max bodycell width
        for (int i = 0; i < raw; i++) {
            headerText = headerTextArray.get(i);
            bodyText = bodySize.get(i);

            int h = headerHeight(headerText); // these method return headercell require height
            int b = bodyHeight(bodyText); // these method return rawbodycell require height
            if (h > b) {
                Rh = h;
            } else {
                Rh = b;
            }
            x = prevX;
            cellHeight = Rh; // required height
            if (y < 100 + cellHeight) {
                createNewPage();
            }
            headerTextLength(headerText); // text amount contained by in one line in headercell
            cellHeight = Rh;
            headerStyle(headerText, x); // headercell

            for (int j = 0; j < column - 1; j++) {
                bodyText = bodyTextArray.get(i).get(j);
                x = x + cellWidth;
                bodyCellStyle(bodyText, x, y); // bodycell
            }
            y = y - Rh;
        }
    }

    public void headerCellWidth() throws Exception {// these method gave maximum require cellwidth for headers
    	headerSize=0;
        for (int i = 0; i < headerTextArray.size(); i++) { // here first headerSize=0;

            if (headerSize < (headerFontSize * headerFont.getStringWidth(headerTextArray.get(i)) / 1000) + 2 * spaceX) {
                headerSize = (headerFontSize * headerFont.getStringWidth(headerTextArray.get(i)) / 1000) + 2 * spaceX;
                headerText = headerTextArray.get(i);
            }
        }
    }

    public void bodyCellWidthForTwoColumn() throws IOException { // maximum require cellwidth for bodytext for 2
    	bodySize2=0;                							// columns
        for (int i = 0; i < bodyTextArray2.size(); i++) {
            if (bodySize2 < (fontSize * bodyFont.getStringWidth(bodyTextArray2.get(i)) / 1000) + 2 * spaceX) {
                bodySize2 = (fontSize * bodyFont.getStringWidth(bodyTextArray2.get(i)) / 1000) + 2 * spaceX;
            }
        }
    }

    public void bodyCellWidth() throws Exception { // these method gave maximam bodytext in each raw
        bodySize = new ArrayList<>(); // we create bodySize array
        int max;

        for (int j = 0; j < bodyTextArray.size(); j++) {
            max = 0;
            String maxString = null;
            for (int i = 0; i < column - 1; i++) {
                if (max < bodyTextArray.get(j).get(i).length()) {
                    maxString = bodyTextArray.get(j).get(i);
                    max = bodyTextArray.get(j).get(i).length();
                }
            }
            bodySize.add(maxString);
        }
    }

    public int headerHeight(String text) throws IOException {

        headerTextLength(text); // gave textamount in one line in a headerecell
        requiredHeight(text); // gave require height of headercell
        return requireHeight;
    }

    public int bodyHeight(String text) throws IOException {

        bodyTextLength(text); // gave textamount in one line in a bodycell
        requiredHeight(text); // gave require height for bodycell
        return requireHeight;
    }

    public void headerStyle(String text, int x) throws IOException {

        headerCellColor(x); // header background and textcolor
        cellWriting(x, height, text, textSize); // writing in cell
    }

    public void headerInColumnDataStore() {
        headerTextArray = new ArrayList<>();
        bodyTextArray = new ArrayList<ArrayList<String>>(); // we declare arrayList of arraylist
        for (int i = 0; i < column; i++) { // here header iscolumn therefore header are column times
            ArrayList<String> data = a.get(i);
            bodyTextArray.add(new ArrayList<String>()); // creating arrayList of arraylist
            for (int j = 0; j < raw; j++) {
                if (j == 0) {
                    headerTextArray.add(data.get(j)); // we put header into a headerText arraylist
                } else {
                    bodyTextArray.get(i).add(data.get(j)); // we put bodytext into a bodyText arraylist
                }
            }
        }
    }

    public void maxColumnWidth() throws IOException {
        maxColumnWidth = new ArrayList<Integer>();
        for (int i = 0; i < headerTextArray.size(); i++) {
            int maxWidth = (int) ((headerFontSize * headerFont.getStringWidth(headerTextArray.get(i)) / 1000)
                    + 2 * spaceX) + 2;

            for (int j = 0; j < bodyTextArray.get(i).size(); j++) {
                if (maxWidth < ((fontSize * bodyFont.getStringWidth(bodyTextArray.get(i).get(j)) / 1000) + 2 * spaceX+ 2) + 5) {
                    maxWidth = (int) ((fontSize * bodyFont.getStringWidth(bodyTextArray.get(i).get(j)) / 1000)+ 2 * spaceX + 5);
                }
            }
            if (i == 0) {
                maxWidth += 10;
            }
            maxColumnWidth.add(maxWidth);
        }
    }

    public void columnHeader() throws Exception {
        int x = prevX;
   
        maxRawBodyString(); // these method calculate maxString in each raw
        bodyCellMaxHeight(0); // we run this method for calculate first bodycell max required height
        int bodyCellHeight = cellHeight; // these cellHeight is requiredHeight for first bodycell
        headerCellWidth();   // these method gave maximum header text
        cellWidth=Collections.max(maxColumnWidth);
        headerHeight(headerText); // these method gave a require cell height for HeaderCell
        int headerCellHeight = cellHeight; // here cellHeight is a required Height for header cell
        for (int i = 0; i < column; i++) {

            if (y < 100 + headerCellHeight + bodyCellHeight) { // when space is more than headercell and one bodycell  then we print else we create new page                                                
                         createNewPage();
            }
          
                if (column == 2) {
                    cellWidth = maxUsedColumnWidth.get(i);
                } else {
 
                    cellWidth = maxColumnWidth.get(i);
                }
            headerText = headerTextArray.get(i); // we get headerText
            headerStyle(headerText, x); // this method print headercell
            x = x + cellWidth;
        }
        y = y - cellHeight;
        x = prevX;
    }

    public void calculateWidthFor2column() {

        maxUsedColumnWidth = new ArrayList<>();

        int tableWidth = maxColumnWidth.get(0) + maxColumnWidth.get(1);
        if (tableWidth < 40 * (612 - 2 * prevX) / 100) {
            maxUsedColumnWidth.add(maxColumnWidth.get(0));
            maxUsedColumnWidth.add((40 * (612 - 2 * prevX) / 100) - maxColumnWidth.get(0));
        } else if (maxColumnWidth.get(1) + maxColumnWidth.get(0) < 612 - 2 * prevX) {
            maxUsedColumnWidth.add(maxColumnWidth.get(0));
            maxUsedColumnWidth.add(maxColumnWidth.get(1));
        } else {
            maxUsedColumnWidth.add(maxColumnWidth.get(0));
            maxUsedColumnWidth.add(612 - 2 * prevX - maxColumnWidth.get(0));
        }
    }

 public void columnBody() throws Exception {
    	
        for (int i = 0; i < raw - 1; i++) {
            int x = prevX;
        	
            cellWidth=maxColumnWidth.get(columnNumber.get(i));
            bodyCellMaxHeight(i);
            for (int j = 0; j < column; j++) {
                     
                if (y < 100 + cellHeight) { // when space is not available to print next bodycell then we create new
                                            // page
                    createNewPage();
                    columnHeader(); // for printing header in new page
                 //   bodyCellMaxHeight(i); // because when we printheader then cellHeight is for headerheight there for
                                          // again call these mathod and calculate cell Height for bodycell
                }
                if (column == 2) {
                    // these method gave maximum required height for raw bodycell
                    cellWidth = maxUsedColumnWidth.get(j);
                } else {
                    cellWidth = maxColumnWidth.get(j);
                }
                
                bodyText = bodyTextArray.get(j).get(i); // bodyText
                bodyCellStyle(bodyText, x, y); // style of bodycell
                x = x + cellWidth;
            }
            y = y - cellHeight;
        }
    }


    public void bodyCellMaxHeight(int i) throws IOException { // these method gave maximum required cellHeight
                                                              // for raw bodycell
        bodyText = maxRawBodyString[i]; // these method gave maximam text in one raw
        bodyHeight(bodyText); // calculate required height
    }

    public void maxRawBodyString() { // maximum body String in one raw
        maxRawBodyString = new String[raw]; // create array
        columnNumber=new ArrayList<>();
        int max;
        int column=0;
        
        for (int i = 0; i < raw - 1; i++) {
            max = 0;
            for (int j = 0; j < bodyTextArray.size(); j++) {
                if (max < bodyTextArray.get(j).get(i).length()){
                    maxRawBodyString[i] = bodyTextArray.get(j).get(i);
                    max = bodyTextArray.get(j).get(i).length();
                    column=j;
                }  
            }
            columnNumber.add(column);
        }
    }


    public void headerTextLength(String text) throws IOException {
        float size = headerFontSize * headerFont.getStringWidth(text) / 1000;
        textSize = (int) (text.length() * (cellWidth - 2 * spaceX) / size); // text amount contained by in one line in
                                                                            // headercell
        height = (int) ((headerFont.getFontDescriptor().getCapHeight()) / 1000 * headerFontSize); // text height
    }

    public void bodyTextLength(String text) throws IOException {
        float size = fontSize * bodyFont.getStringWidth(text) / 1000;
        textSize = (int) (text.length() * (cellWidth - 2 * spaceX) / size) - 1; // text amount contained by in one line
                                                                                // in bodycell
        height = (int) ((bodyFont.getFontDescriptor().getCapHeight()) / 1000 * fontSize); // text higth
    }

    public void headerCellColor(int x) throws IOException {
        stream.setFont(headerFont, headerFontSize);
        stream.setNonStrokingColor(224, 224, 224); // backgroundcolor
        stream.addRect(x, y, cellWidth, -cellHeight);
        stream.fill(); // these is for fill backgroundcolor in header cell
        stream.stroke();
        stream.setNonStrokingColor(Color.black); // text color in header cell
    }

    public void bodyCellStyle(String text, int x, int y) throws IOException {

        stream.setFont(bodyFont, fontSize); // body text
        bodyTextLength(text); // gave body textlength
        cellWriting(x, height, text, textSize); // writing in body
    }

    public void cellWriting(int x, float height, String text, int textsize)
            throws IOException {

        wrt = org.apache.commons.text.WordUtils.wrap(text, textsize).split("\\r?\\n");
        stream.setLineWidth(0);
        stream.addRect(x, y, cellWidth, -cellHeight); // this is for make cell for writing text
        stream.beginText();
        int a = x + spaceX;
        int b = y - spaceY + 3; // add 2 for printing in center else ptinting in the end of cell
        for (int i = 0; i < wrt.length; i++) {
            stream.newLineAtOffset(a, b);
            s = wrt[i];
            stream.showText(s); // writing in cell
            b = -spaceY; // space between two lines
            a = 0;
        }
        stream.endText();
        stream.stroke();
    }

    public void requiredHeight(String text) {
        wrt = org.apache.commons.text.WordUtils.wrap(text, textSize).split("\\r?\\n");
        requireHeight = 0;
        for (int i = 0; i < wrt.length; i++) {
            requireHeight += height + spaceY - 5; // here hight means one text line hight and spaceY is space beetween
                                                  // lines
        }
        cellHeight = requireHeight;
    }
}
