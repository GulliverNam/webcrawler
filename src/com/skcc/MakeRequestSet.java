package com.skcc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MakeRequestSet {
	private final static String baseUrl = "https://developers.mydatakorea.org/mdtb/apg/mac/bas";
	private final static String[] params = {"/FSAG0404?id=1",
										    "/FSAG0406?id=2",
										    "/FSAG0403?id=3",
										    "/FSAG0402?id=4",
										    "/FSAG0405?id=5",
										    "/FSAG0407?id=6",
										    "/FSAG0408?id=10",
										    "/FSAG0409?id=11",
										    "/FSAG0302?id=9", 	// 지원(사업자/정보제공자 제공)
										    "/FSAG0301?id=8" 	// 지원(종합포털제공)
//										    "/FSAG0201?id=7" 	// 인증
										    };
	private static int rowSize = 0;
	
	public static void main(String[] args) throws Exception {
		ConnectionUtils.ignoreSSL();
		URL url = null;
		String filePath = ".\\data_req\\result.xlsx";
    	XSSFWorkbook workbook = new XSSFWorkbook();
    	XSSFSheet sheet = workbook.createSheet("IO_LIST");
		for(String param : params) {
			url = new URL(baseUrl+param);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			});
			Charset charset = Charset.forName("UTF-8");
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),charset));
	        String inputLine;
	        StringBuffer response = new StringBuffer();
	        while ((inputLine = in.readLine()) != null) {
	            response.append(inputLine);
	        }
	        in.close();
	        conn.disconnect();
	        Document jsoup = Jsoup.parse(response.toString());
	        Elements apis = jsoup.getElementsByClass("api_section_list").get(0).children();
	        int size = apis.size();
	        System.out.println("size: "+ size);
	        
	        
	        for(int i=0; i<size; i++) {
	        	Element api = apis.get(i);
	        	Elements head = api.getElementsByClass("board_list left").get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
	        	String[] apiName = head.get(2).getElementsByTag("td").get(0).text().split("/");
	        	System.out.println("###########################################");
	        	Elements table = api.getElementsByClass("board_list row_line");
	        	Elements requestData = table.get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
	        	
	        	int bodySize = requestData.size();
	        	int contentIdIdx = 0;
	        	int contentNameIdx = 1;
	        	int requireIdx = 2;
	        	int typeLengthIdx = 3;
	        	int descIdx = 4;
	        	String headerBody = null;
	        	List<API> reqList = new ArrayList<>();
	        	System.out.println("bodySize: "+bodySize);
	        	for (int j = 0; j < bodySize; j++) {
	        		Elements td = requestData.get(j).getElementsByTag("td");
	        		Elements tdBdr = requestData.get(j).getElementsByClass("bdr");
	        		if(!tdBdr.text().equals("")) {
	        			headerBody = tdBdr.text();
	        			reqList.add(new API(String.join("/", apiName), headerBody, 
	        					td.get(contentIdIdx+1).text(), td.get(contentNameIdx+1).text(), 
	        					td.get(typeLengthIdx+1).text(), "Y".equals(td.get(requireIdx+1).text()) ? true:false,
	        					td.get(descIdx+1).text()));
	        		} else {
	        			reqList.add(new API(String.join("/", apiName), headerBody, 
	        					td.get(contentIdIdx).text(), td.get(contentNameIdx).text(), 
	        					td.get(typeLengthIdx).text(), "Y".equals(td.get(requireIdx).text()) ? true:false,
	        					td.get(descIdx).text()));
	        		}
				}
	        	for (API temp : reqList) {
					System.out.println(temp);
				}
	        	
	        	
	        	createData(sheet, reqList, "I");
	        	System.out.println("###########################################");
	        	Elements responseData = table.get(1).getElementsByTag("tbody").get(0).getElementsByTag("tr");
	        	bodySize = responseData.size();
	        	System.out.println("bodySize: "+bodySize);
	        	List<API> respList = new ArrayList<>();
	        	for (int j = 0; j < bodySize; j++) {
	        		Elements td = responseData.get(j).getElementsByTag("td");
	        		Elements tdBdr = responseData.get(j).getElementsByClass("bdr");
	        		if(!tdBdr.text().equals("")) {
	        			headerBody = tdBdr.text();
	        			respList.add(new API(String.join("/", apiName), headerBody, 
	        					td.get(contentIdIdx+1).text(), td.get(contentNameIdx+1).text(), 
	        					td.get(typeLengthIdx+1).text(), "Y".equals(td.get(requireIdx+1).text()) ? true:false,
	        					td.get(descIdx+1).text()));
	        		} else {
	        			respList.add(new API(String.join("/", apiName), headerBody, 
	        					td.get(contentIdIdx).text(), td.get(contentNameIdx).text(), 
	        					td.get(typeLengthIdx).text(), "Y".equals(td.get(requireIdx).text()) ? true:false,
	        					td.get(descIdx).text()));
	        		}
				}
	        	for (API temp : respList) {
					System.out.println(temp);
				}
	        	createData(sheet, respList, "O");
	        	System.out.println("###########################################");
	        }
			
		}
		FileOutputStream fos = new FileOutputStream(filePath);
    	workbook.write(fos);
    	fos.close();
	}
	private static void createData(XSSFSheet sheet, List<API> list, String IO) throws IOException, InvalidFormatException {
    	XSSFRow row;
    	String parentId = "";
    	for(int i = 0, listSize = list.size(); i < listSize; i++) {
    		API api = list.get(i);
    		row = sheet.createRow(rowSize++);
    		// 0. API ID
    		row.createCell(0).setCellValue(api.getApiId());
    		// 1. 입력/출력(I,O) & 헤드/바디(H,B)
    		String IOheadBody = "";
    		if("Header".equals(api.getHeaderBody())) {
    			IOheadBody = IO + "H";
    		}else if("Path".equals(api.getHeaderBody())){
    			IOheadBody = IO + "P";
    		}else {
    			IOheadBody = IO + "B";
    		}
    		row.createCell(1).setCellValue(IOheadBody);
    		String[] UIOType = null;
    		String referLength = "";
    		if(!"".equals(parentId)) {
    			api.setContentId(api.getContentId().replaceAll("-", ""));
    		}
    		// 3. 상위항목
    		row.createCell(3).setCellValue(parentId);
    		// 2. 항목유형(F,R)
			if(api.getTypeLength().toLowerCase().contains("object")) {
    			row.createCell(2).setCellValue("R");
    			UIOType = new String[] {"", ""};
    			if(!"".equals(parentId)) {
    				parentId += ".";
    			}
    			parentId += api.getContentId();
    			API refer = list.get(i-1);
    			referLength = list.get(i-1).getContentId();
			} else {
				row.createCell(2).setCellValue("F");
			}
			
			String checkLogic = "";
			if(api.getContentId().contains("timestamp") || api.getTypeLength().toLowerCase().contains("dtime")) {
				UIOType = new String[] {"string", "14"};
				checkLogic = "timestamp14_check";
			} else if(api.getTypeLength().toLowerCase().contains("date")) {
				UIOType = new String[] {"string", "8"};
				checkLogic = "date_check";
			} else if(api.getTypeLength().toLowerCase().contains("boolean")) {
				UIOType = new String[] {"string", "5"};
				checkLogic = "boolean_check";
			} else if("Path".equals(api.getHeaderBody())) {
				UIOType = new String[] {"string", "10"};
				if("".equals(api.getDescription())) {
					api.setDescription("Path 변수");
				}
			} else if(UIOType == null) {
				UIOType = getUIOType(api.getTypeLength());
			}
			
			// 4. 항목
			row.createCell(4).setCellValue(api.getContentId());
			// 5. 항목명
			row.createCell(5).setCellValue(api.getContentName());
			// 6. 길이
			row.createCell(6).setCellValue(UIOType[1]);
			// 7. 길이참조(RecordSet 일때)
			row.createCell(7).setCellValue(referLength);
			// 8. 데이터유형
			row.createCell(8).setCellValue(UIOType[0]);
			
			// 11. 필수검증 (Y/N)
			if(api.isRequired()) {
				row.createCell(11).setCellValue("mandatory");
			}
			// 12. 검증로직 (timestamp14_check, date_check, boolean_check)
			row.createCell(12).setCellValue(checkLogic);
			
			// 13. 설명
			row.createCell(13).setCellValue(api.getDescription());
    	}
    	
	}
	private static String[] getUIOType(String mydataType) {
		String[] arr = mydataType.split("\\(");
		String type = arr[0];
		arr[1] = arr[1].replace(")", "").replace(",", ".");
		if("N".equals(type)) {
			int len = Integer.parseInt(arr[1]);
			if(len <= 9) {
				arr[0] = "int";
			} else if(len <= 19) {
				arr[0] = "long";
			} else {
				arr[0] = "string";
			}
		} else if("F".equals(type)) {
			arr[0] = "bigDecimal";
		} else {
			arr[0] = "string";
		}
		return arr;
	}
}
