package cz.muni.fi.smartlib.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.CharArrayBuffer;

import com.google.zxing.client.result.ParsedResultType;

import cz.muni.fi.smartlib.SmartLibMU;
import cz.muni.fi.smartlib.utils.SerializableCookie;

import android.util.Log;



/*
 * Singleton trida, ktera slouzi k http pozadavkum na server. Drzi jednu instanci DefaultHttpClient s cookies. 
 * 
 * */
public class HttpHelper {
	public static final String TAG = HttpHelper.class.getSimpleName();
	
	public static final int METHOD_GET = 0;
	public static final int METHOD_POST = 1;

	
	private static HttpHelper instance;
	private DefaultHttpClient client;
	
	private HttpHelper() {
		client = HttpClientFactory.getThreadSafeClient();
	}
	
	public static HttpHelper getInstance() {
		if (instance == null) {
			instance = new HttpHelper();
		}
		return instance;
	}
	
	/*
	 *	Trida drzici odpoved http pozadavku
	 * 
	 * */
	public static class HttpConnectResult {
		public Header[] headerArray;
		public String response;
		public Status status;

		public HttpConnectResult(final Header[] headerArray, final String response) {
			this.headerArray = headerArray;
			this.status = getStatusFromHeaders(headerArray);
			this.response = response;
		}
		
		private Status getStatusFromHeaders(Header[] headerArray) {
			for (int i = 0; i < headerArray.length; i++) {
				
				if (headerArray[i].getName().equals("StatusCode")) {
					return parseStatus(headerArray[i].getValue());
				}
			}
			return Status.STATUS_UNKNOWN;
		}
		
		public Status getStatus() {
			return status;
		}
		
		//na serveru z nejakeho duvodu nefungujou standardni http status kody, Tomas nam je posila v hlavicce jako string
		public Status parseStatus(String statusString) {
			if (statusString.equals("200")) {
				return Status.STATUS_OK;
			}
			if (statusString.equals("400")) {
				return Status.STATUS_BAD_REQUEST;
			}
			if (statusString.equals("401")) {
				return Status.STATUS_UNAUTHORIZED;
			}
			if (statusString.equals("409")) {
				return Status.STATUS_OK;
			}
			return Status.STATUS_UNKNOWN;
		}
		
		public enum Status { 
			STATUS_OK, STATUS_BAD_REQUEST, STATUS_UNAUTHORIZED, STATUS_CONFLIC, STATUS_UNKNOWN 
		}
		
	}
	
	/*
	 * Takovy hack, uz nevim k cemu, ma to neco spolecneho s ukladanim cookies, ktere nefungovalo, myslim.
	 * 
	 * **/
	public static class HttpClientFactory {

	    private static DefaultHttpClient clientThreadSafe;

	    public synchronized static DefaultHttpClient getThreadSafeClient() {
	  
	        if (clientThreadSafe != null)
	            return clientThreadSafe;
	         
	        clientThreadSafe = new DefaultHttpClient();
	        
	        ClientConnectionManager mgr = clientThreadSafe.getConnectionManager();
	        
	        HttpParams params = clientThreadSafe.getParams();
	        
	        //timeout
	        //int timeoutConnection = 25000;
	        int timeoutConnection = 10000;
			HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
			//int timeoutSocket = 25000;
			int timeoutSocket = 10000;
			HttpConnectionParams.setSoTimeout(params, timeoutSocket);
			clientThreadSafe = new DefaultHttpClient(
	        new ThreadSafeClientConnManager(params,
	            mgr.getSchemeRegistry()), params);
	  
			//disable requery
			clientThreadSafe.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
	        //persistent cookies
	        clientThreadSafe.setCookieStore(SmartLibMU.getCookieStore());
	        
	        return clientThreadSafe;
	    } 
	}	
	
	//tyto dve metody slouzi k ziskani odpovedi ze serveru pomoci GET nebo POST
	public HttpConnectResult httpConnectGET(String url, Map<String, String> params, ArrayList<Header> headerArray) throws HttpHelperException {
		client = HttpClientFactory.getThreadSafeClient();
		return getResponseFromWebService(url, METHOD_GET, params, headerArray);
	}

	public HttpConnectResult httpConnectPOST(String url, Map<String, String> params, ArrayList<Header> headerArray) throws HttpHelperException {
		client = HttpClientFactory.getThreadSafeClient();
		return getResponseFromWebService(url, METHOD_POST, params, headerArray);
	}

	
	
	
	private HttpConnectResult getResponseFromWebService(final String url, final int method, final Map<String, String> params, final ArrayList<Header> headerArray) 
			throws HttpHelperException, IllegalArgumentException {

		//validace vstupu
		if (url == null) {
            Log.e(TAG, "getResponseFromWebService - Compulsory Parameter : request URL has not been set");
            throw new IllegalArgumentException("Request URL has not been set");
        }
		
        if (method != METHOD_GET && method != METHOD_POST) {
            Log.e(TAG, "getResponseFromWebService - Request method must be METHOD_GET or METHOD_POST");
            throw new IllegalArgumentException("getResponseFromWebService - Request method must be METHOD_GET or METHOD_POST");
        }
        
		
        //pripojeni a ziskani odpovedi
		HttpResponse response = null;
		switch (method) {
			default:
			case METHOD_GET:
				try {
					HttpGet httpGet = new HttpGet(constructUrlRequest(url, params));
					
					//pokud jsou hlavicky tak je pripoji k dotazu
					if (headerArray != null && !headerArray.isEmpty()) {
	
						final int headersLength = headerArray.size();
	
						for (int i = 0; i < headersLength; i++) {
							httpGet.addHeader(headerArray.get(i));
						}
					}
					Log.i(TAG, "Executing http query: " + httpGet.getURI().toString());
					response = client.execute(httpGet);	
				} catch (SocketTimeoutException e) {
					Log.w(TAG, "Connection timeout reached!");
					throw new HttpHelperException("Connection timeout reached!");
				} catch (UnsupportedEncodingException e) {
					throw new HttpHelperException("UnsupportedEncodingException");
				} catch (ClientProtocolException e) {
					throw new HttpHelperException("ClientProtocolException");
				} catch (IOException e) {
					throw new HttpHelperException("IOException");
				}
				break;
			case METHOD_POST:
				try {
					HttpPost httpPost = new HttpPost(constructUrlRequest(url, params));
					if (headerArray != null && !headerArray.isEmpty()) {
	
						final int headersLength = headerArray.size();
	
						for (int i = 0; i < headersLength; i++) {
							httpPost.addHeader(headerArray.get(i));
						}
					}
					Log.i(TAG, "Executing http query: " + httpPost.getURI().toString());
					response = client.execute(httpPost);
				} catch (SocketTimeoutException e) {
					Log.w(TAG, "Connection timeout reached!");
					throw new HttpHelperException("Connection timeout reached!");
				} catch (UnsupportedEncodingException e) {
					throw new HttpHelperException("UnsupportedEncodingException");
				} catch (ClientProtocolException e) {
					throw new HttpHelperException("ClientProtocolException");
				} catch (IOException e) {
					throw new HttpHelperException("IOException");
				}
				break;
		}
		
		final HttpEntity entity = response.getEntity();
		

		String result = null;
        if (entity != null) {
            try {
				result = convertStreamToString(entity.getContent(),  method, (int) entity.getContentLength());
				Log.i(TAG, "Response received: " + result);
			} catch (IllegalStateException e) {
				throw new HttpHelperException("IllegalStateException");
			} catch (IOException e) { 
				throw new HttpHelperException("IOException");
			}
        } 
        
        //vrati novy objekt HttpConnectResult nebo null
        
        HttpConnectResult httpResult = new HttpConnectResult(response.getAllHeaders(), result);
        
        if (httpResult.getStatus() != HttpConnectResult.Status.STATUS_OK) {
        	
        	Log.w(TAG, "Http response status is " + httpResult.getStatus().name());
        	throw new HttpHelperException("Http response statuts is not 200 OK.");
        }
        
		return (result != null)? new HttpConnectResult(response.getAllHeaders(), result) : null;
	
		}
	
	public boolean hasCookies() {
		CookieStore cookies = client.getCookieStore();
		if (cookies != null) {
			return !cookies.getCookies().isEmpty();
		}
		return false;
	}
	
	public void deleteCookies() {
		CookieStore cookies = client.getCookieStore();
		if (cookies != null) {
			cookies.clear();
		}
	}

	
	
	/*
	 *  Construct request URL with given params.
	 *  
	 * */
	private static String constructUrlRequest(final String baseUrl, final Map<String, String> params)
			throws UnsupportedEncodingException {
		final StringBuilder sb = new StringBuilder();
		sb.append(baseUrl);

		if (params != null && !params.isEmpty()) {
			sb.append("?");

			final ArrayList<String> keyList = new ArrayList<String>(
					params.keySet());
			final int keyListLength = keyList.size();

			for (int i = 0; i < keyListLength; i++) {
				final String key = keyList.get(i);

				sb.append(URLEncoder.encode(key, "UTF-8"));
				sb.append("=");
				sb.append(URLEncoder.encode(params.get(key), "UTF-8"));
				sb.append("&");
			}
		}

		return sb.toString();
	}
	
	/*
	 *  Convert response stream to string.
	 *  
	 * */
	private static String convertStreamToString(final InputStream is, final int method, final int contentLength) throws IOException {
        InputStream cleanedIs = is;

        try {
            switch (method) {
                case METHOD_GET: {
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(cleanedIs));
                    final StringBuilder sb = new StringBuilder();

                    String line = null;

                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }

                    return sb.toString();
                }
                case METHOD_POST: {
                    int i = contentLength;
                    if (i < 0) {
                        i = 4096;
                    }

                    final Reader reader = new InputStreamReader(cleanedIs);
                    final CharArrayBuffer buffer = new CharArrayBuffer(i);
                    final char[] tmp = new char[1024];
                    int l;
                    while ((l = reader.read(tmp)) != -1) {
                        buffer.append(tmp, 0, l);
                    }

                    return buffer.toString();
                }
                default:
                    return null;
            }
        } finally {
            cleanedIs.close();
        }
    }
	
}
