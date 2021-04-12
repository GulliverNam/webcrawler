package com.skcc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

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
										    "/FSAG0409?id=11"};
	public static void main(String[] args) throws Exception {
		try {
			ConnectionUtils.ignoreSSL();
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
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
	        for(int i=0; i<size; i++) {
	        	Element api = apis.get(i);
	        	Elements head = api.getElementsByClass("board_list left").get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
	        	String apiName = head.get(2).getElementsByTag("td").get(0).text();
	        	String apiDesc = head.get(3).getElementsByTag("td").get(0).text();
	        	System.out.println(apiName+" "+apiDesc);
	        	System.out.println("###########################################");
	        	Elements bodies = api.getElementsByClass("board_list row_line");
	        	Elements body1 = bodies.get(0).getElementsByTag("tbody").get(0).getElementsByTag("tr");
	        	int bodySize = body1.size();
	        	int paramIdx = 1;
	        	int descIdx = 2;
	        	int typeIdx = 4;
	        	boolean flag = false;
	        	List<String[]> reqList = new ArrayList<>();
	        	for (int j = 0; j < bodySize; j++) {
	        		Elements td = body1.get(j).getElementsByTag("td");
	        		if(flag) {
	        			reqList.add(new String[] {td.get(paramIdx).text(), td.get(descIdx).text(), td.get(typeIdx).text()});
	        		}else if(td.get(0).text().equals("Parameter") || td.get(0).text().equals("Body")) {
	        			reqList.add(new String[] {td.get(paramIdx).text(), td.get(descIdx).text(), td.get(typeIdx).text()});
						paramIdx--;
						descIdx--;
						typeIdx--;
						flag = true;
					}
				}
	        	for (String[] arr : reqList) {
					System.out.println(arr[0]+" "+arr[1]+" "+arr[2]);
				}
	        	System.out.println("###########################################");
	        	Elements body2 = bodies.get(1).getElementsByTag("tbody").get(0).getElementsByTag("tr");
	        	bodySize = body2.size();
	        	paramIdx = 1;
	        	descIdx = 2;
	        	typeIdx = 4;
	        	List<String[]> respList = new ArrayList<>();
	        	for (int j = 0; j < bodySize; j++) {
	        		Elements td = body2.get(j).getElementsByTag("td");
	        		respList.add(new String[] {td.get(paramIdx).text(), td.get(descIdx).text(), td.get(typeIdx).text()});
	        		if(j == 0) {
						paramIdx--;
						descIdx--;
						typeIdx--;
					}
				}
	        	for (String[] arr : respList) {
					System.out.println(arr[0]+" "+arr[1]+" "+arr[2]);
				}
	        	System.out.println("###########################################");
	        }
			
		}
	}
}
