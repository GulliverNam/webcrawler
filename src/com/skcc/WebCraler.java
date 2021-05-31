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

public class WebCraler {
	private final static String baseUrl = "https://developers.mydatakorea.org/mdtb/apg/mac/bas";
	private final static String[] params = {"/FSAG0404?id=1",
										    "/FSAG0406?id=2",
										    "/FSAG0403?id=3",
										    "/FSAG0402?id=4",
										    "/FSAG0405?id=5",
										    "/FSAG0407?id=6",
										    "/FSAG0408?id=10",
										    "/FSAG0409?id=11",
										    "/FSAG0201?id=7"};
	public static void main(String[] args) throws Exception {
		ConnectionUtils.ignoreSSL();
		URL url = null;
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
	        
	        String originFilePath = ".\\data\\sample.xlsx";
	        File originFile = new File(originFilePath);
	        for(int i=0; i<size; i++) {
	        	Element api = apis.get(i);
	        	Elements head = api.getElementsByClass("board_list left").get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
	        	String[] apiName = head.get(2).getElementsByTag("td").get(0).text().split("/");
	        	String apiDesc = head.get(3).getElementsByTag("td").get(0).text();
	        	String copyfilePath = ".\\data\\"+String.join("_", apiName)+".xlsx";
	        	File copyFile = new File(copyfilePath);
	        	if(!copyFile.exists()) {
	        		Files.copy(originFile.toPath(), copyFile.toPath());
	        	}
	        	System.out.println(copyfilePath);
	        	System.out.println("###########################################");
	        	Elements bodies = api.getElementsByClass("board_list row_line");
	        	Elements body1 = bodies.get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
	        	
	        	int bodySize = body1.size();
	        	int paramIdx = 1;
	        	int descIdx = 2;
	        	int requireIdx = 3;
	        	int typeIdx = 4;
	        	boolean flag = false;
	        	List<String[]> reqList = new ArrayList<>();
	        	for (int j = 0; j < bodySize; j++) {
	        		Elements td = body1.get(j).getElementsByTag("td");
	        		if(flag) {
	        			reqList.add(new String[] {td.get(paramIdx).text(), td.get(descIdx).text(), td.get(typeIdx).text(), td.get(requireIdx).text()});
	        		}else if(td.get(0).text().equals("Parameter") || td.get(0).text().equals("Body")) {
	        			reqList.add(new String[] {td.get(paramIdx).text(), td.get(descIdx).text(), td.get(typeIdx).text(), td.get(requireIdx).text()});
						paramIdx--;
						descIdx--;
						requireIdx--;
						typeIdx--;
						flag = true;
					}
				}
	        	for (String[] arr : reqList) {
					System.out.println(arr[0]+" "+arr[1]+" "+arr[2]);
				}
	        	
	        	
	        	createData(copyfilePath, reqList, "I", 2);
	        	System.out.println("###########################################");
	        	Elements body2 = bodies.get(1).getElementsByTag("tbody").get(0).getElementsByTag("tr");
	        	bodySize = body2.size();
	        	paramIdx = 1;
	        	descIdx = 2;
	        	requireIdx = 3;
	        	typeIdx = 4;
	        	flag = false;
	        	List<String[]> respList = new ArrayList<>();
	        	for (int j = 0; j < bodySize; j++) {
	        		Elements td = body2.get(j).getElementsByTag("td");
	        		if(flag) {
	        			reqList.add(new String[] {td.get(paramIdx).text(), td.get(descIdx).text(), td.get(typeIdx).text(), td.get(requireIdx).text()});
	        		}else if(td.get(0).text().equals("Parameter") || td.get(0).text().equals("Body")) {
	        			reqList.add(new String[] {td.get(paramIdx).text(), td.get(descIdx).text(), td.get(typeIdx).text(), td.get(requireIdx).text()});
						paramIdx--;
						descIdx--;
						requireIdx--;
						typeIdx--;
						flag = true;
					}
				}
	        	for (String[] arr : respList) {
					System.out.println(arr[0]+" "+arr[1]+" "+arr[2]);
				}
	        	createData(copyfilePath, respList, "O", 2+reqList.size());
	        	System.out.println("###########################################");
	        }
			
		}
	}
	private static void createData(String filePath, List<String[]> List, String IO, int rowSize) throws IOException, InvalidFormatException {
		
		FileInputStream fis = new FileInputStream(filePath);
    	XSSFWorkbook workbook = new XSSFWorkbook(fis);
    	XSSFSheet sheet = workbook.getSheet("IO_LIST");
    	
    	XSSFRow row;
    	String parentId = "";
    	for(int i = 0, listSize = List.size(); i < listSize; i++) {
    		String[] tmp = List.get(i);
    		row = sheet.createRow(i+rowSize);
    		// 0. 입력/출력(I,O)
    		row.createCell(0).setCellValue(IO);
    		String id = tmp[0].replaceAll("-", "");
    		String[] UIOType = null;
    		String referLength = "";
    		// 2. 상위항목
    		row.createCell(2).setCellValue(parentId);
    		// 1. 항목유형(F,R)
			if(tmp[2].toLowerCase().contains("object")) {
    			row.createCell(1).setCellValue("R");
    			UIOType = new String[] {"", ""};
    			if(!"".equals(parentId)) {
    				parentId += ".";
    			}
    			parentId += tmp[0].replaceAll("-", "");
    			referLength = List.get(i-1)[0].replaceAll("-", "");
			} else {
				row.createCell(1).setCellValue("F");
			}
			
			String checkLogic = "";
			if(id.contains("timestamp") || tmp[2].toLowerCase().contains("dtime")) {
				UIOType = new String[] {"string", "14"};
				checkLogic = "timestamp14_check";
			} else if(tmp[2].toLowerCase().contains("date")) {
				UIOType = new String[] {"string", "8"};
				checkLogic = "date_check";
			} else if(tmp[2].toLowerCase().contains("boolean")) {
				UIOType = new String[] {"string", "5"};
				checkLogic = "boolean_check";
			} else if(UIOType == null) {
				UIOType = getUIOType(tmp[2]);
			}
			
			// 3. 항목
			row.createCell(3).setCellValue(id);
			// 4. 항목명
			row.createCell(4).setCellValue(tmp[1]);
			// 5. 길이
			row.createCell(5).setCellValue(UIOType[1]);
			// 6. 길이참조(RecordSet 일때)
			row.createCell(6).setCellValue(referLength);
			// 7. 데이터유형
			row.createCell(7).setCellValue(UIOType[0]);
			
			// 10. 필수검증 (Y/N)
			if(tmp[3].equals("Y")) {
				row.createCell(10).setCellValue("mandatory");
			}
			// 11. 검증로직 (timestamp14_check, date_check, boolean_check)
			row.createCell(11).setCellValue(checkLogic);
    	}
    	FileOutputStream fos = new FileOutputStream(filePath);
    	workbook.write(fos);
    	fos.close();
    	fis.close();
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
