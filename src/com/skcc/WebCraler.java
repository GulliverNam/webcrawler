package com.skcc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCraler {
	private final static String baseUrl = "https://developers.mydatakorea.org/mdtb/apg/mac/bas";
	private final static String[] params = {"/FSAG0404?id=1"};
//	,
//										    "/FSAG0406?id=2",
//										    "/FSAG0403?id=3",
//										    "/FSAG0402?id=4",
//										    "/FSAG0405?id=5",
//										    "/FSAG0407?id=6",
//										    "/FSAG0408?id=10",
//										    "/FSAG0409?id=11"};
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
	        Elements apis = jsoup.getElementsByClass("api_section_list");
	        int size = apis.size();
	        for(int i=0; i<size; i++) {
	        	Element api = apis.get(i);
//	        	Element head = api.getElementsByClass("board_list left").;
	        }
			
		}
	}
}
