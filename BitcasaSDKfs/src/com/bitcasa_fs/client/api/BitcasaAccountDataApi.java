package com.bitcasa_fs.client.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import android.util.Log;

import com.bitcasa_fs.client.Account;
import com.bitcasa_fs.client.BitcasaError;
import com.bitcasa_fs.client.Session;
import com.bitcasa_fs.client.User;
import com.bitcasa_fs.client.api.BitcasaRESTConstants;
import com.bitcasa_fs.client.api.BitcasaRESTConstants.ApiMethod;
import com.bitcasa_fs.client.api.BitcasaRESTUtility;
import com.bitcasa_fs.client.ParseJSON.BitcasaParseJSON;
import com.bitcasa_fs.client.datamodel.Credential;
import com.bitcasa_fs.client.datamodel.Profile;
import com.bitcasa_fs.client.exception.BitcasaException;

/**
 * Api request to Bitcasa CloudFS server to obtain Account data and User data information
 * @author Valina Li
 *
 */
public class BitcasaAccountDataApi {

	public static final String TAG = BitcasaAccountDataApi.class.getSimpleName();
	private BitcasaRESTUtility bitcasaRESTUtility;
	private Credential credential;
	
	/**
	 * Constructor 
	 * @param credential
	 * @param utility
	 */
	public BitcasaAccountDataApi(Credential credential, BitcasaRESTUtility utility) {
		this.bitcasaRESTUtility = utility;
		this.credential = credential;
	}
	
	/**
	 * request to get account information from Bitcasa CloudFS server 
	 * @return Bitcasa Account object 
	 * @throws IOException
	 * @throws BitcasaException
	 */
	public Account requestAccountInfo() throws IOException, BitcasaException {
		Profile profile = getProfile();
		return profile.getAccountData();
	}
	
	/**
	 * request to get user information from Bitcasa CloudFS server
	 * @return Bitcasa User object
	 * @throws IOException
	 * @throws BitcasaException
	 */
	public User requestUserInfo() throws IOException, BitcasaException {
		Profile profile = getProfile();
		return profile.getUserData();
	}
	
	/**
	 * request to get profile from Bitcasa CloudFS server, Bitcasa Profile object contains both account and user information
	 * @return Bitcasa Profile Object
	 * @throws IOException
	 * @throws BitcasaException
	 */
	private Profile getProfile() throws IOException, BitcasaException {    	
		Profile profile = null;
		String url = bitcasaRESTUtility.getRequestUrl(credential, BitcasaRESTConstants.METHOD_USER, BitcasaRESTConstants.METHOD_PROFILE, null);
		
		Log.d(TAG, "getProfile URL: " + url);
		HttpsURLConnection connection = null;
		InputStream is = null;
		BitcasaParseJSON parser = null;
		
		try {
			connection = (HttpsURLConnection) new URL(url)
					.openConnection();
			connection.setDoInput(true);
			connection.setRequestMethod(BitcasaRESTConstants.REQUEST_METHOD_GET);
			bitcasaRESTUtility.setRequestHeaders(credential, connection, null);
			
			BitcasaError error = bitcasaRESTUtility.checkRequestResponse(connection);
			if (error == null) {
				is = connection.getInputStream();
				parser = new BitcasaParseJSON(ApiMethod.ACCOUNT, null);
				if (parser.readJsonStream(is)) {
					profile = parser.profile;
				}
			}
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			if (e != null)
				e.printStackTrace();
		} finally {
			
			if (is != null) {
				is.close();
			}	
			
			if (connection != null)
				connection.disconnect();
		}
		return profile;
	}
    
	/**
	 * request to alter Profile information, more specifically to alter account information
	 * @param accountInfo
	 * @param changes
	 * @return boolean value, true means succeed
	 */
    public boolean alterProfile(Account accountInfo, Map<String, String> changes) {
    	boolean bResult = false;
    	
    	String url = bitcasaRESTUtility.getRequestUrl(credential, BitcasaRESTConstants.METHOD_USER, BitcasaRESTConstants.METHOD_PROFILE, null);
    	
    	Log.d(TAG, "alterProfile URL: " + url);
		HttpsURLConnection connection = null;
		InputStream is = null;
		BitcasaParseJSON parser = null;
		OutputStream os = null;
		try {
			connection = (HttpsURLConnection) new URL(url)
					.openConnection();
			connection.setDoInput(true);
			connection.setRequestMethod(BitcasaRESTConstants.REQUEST_METHOD_POST);
			bitcasaRESTUtility.setRequestHeaders(credential, connection, null);
			os = connection.getOutputStream();
			os.write(bitcasaRESTUtility.generateParamsString(changes).getBytes());
			
			BitcasaError error = bitcasaRESTUtility.checkRequestResponse(connection);
			if (error == null) {
				is = connection.getInputStream();			
				parser = new BitcasaParseJSON(ApiMethod.GENERAL, null);
				if (parser.readJsonStream(is))
					bResult = parser.bSuccessResult;
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BitcasaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			
				try {
					if (is != null)
						is.close();
					if (os != null)
						os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			if (connection != null)
				connection.disconnect();
			
		}
    	return bResult;
    }
    
    /**
     * a way to ping CloudFS server, can be ping by head or get request header 
     * @param bUseHeadMethod
     * @return boolean value, true means succeed
     */
    public boolean ping(boolean bUseHeadMethod) {
    	boolean bResult = false;
    	
    	String url = bitcasaRESTUtility.getRequestUrl(credential, BitcasaRESTConstants.METHOD_PING, null, null);
    	Log.d(TAG, "ping URL: " + url);
		HttpsURLConnection connection = null;
		try {
			connection = (HttpsURLConnection) new URL(url).openConnection();
			if (bUseHeadMethod)
				connection.setRequestMethod(BitcasaRESTConstants.REQUEST_METHOD_HEAD);
			else
				connection.setRequestMethod(BitcasaRESTConstants.REQUEST_METHOD_GET);
			bitcasaRESTUtility.setRequestHeaders(credential, connection, null);
			
			BitcasaError error = bitcasaRESTUtility.checkRequestResponse(connection);
			if (error == null) 
				bResult = true;
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (connection != null)
				connection.disconnect();
		}
    	return bResult;
    }
    
}
