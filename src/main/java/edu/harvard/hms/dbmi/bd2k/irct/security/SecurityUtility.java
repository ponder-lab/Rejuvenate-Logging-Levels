package edu.harvard.hms.dbmi.bd2k.irct.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import edu.harvard.hms.dbmi.bd2k.irct.model.security.JWT;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * The Security Utility class provides a set of useful functions for handling
 * security functions.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class SecurityUtility {

	/**
	 * Returns a delegated a token for a user for a new Auth0 application. If an
	 * error occurs then NULL is returned.
	 * 
	 * @param namespace
	 *            URL of the namespace
	 * @param resourceClientId
	 *            New client id
	 * @param session
	 *            Session of the user
	 * @return New Delegated token
	 */
	public static String delegateToken(String namespace,
			String resourceClientId, SecureSession session) {
		if (((JWT) session.getToken()).getClientId().equals(resourceClientId)) {
			return "Bearer " + session.getToken().toString();
		}

		if (session.getDelegated().containsKey(resourceClientId)) {
			return "Bearer "
					+ session.getDelegated().get(resourceClientId).toString();
		}

		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost("https://" + namespace + "/delegation");

			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("grant_type",
					"urn:ietf:params:oauth:grant-type:jwt-bearer"));
			urlParameters
					.add(new BasicNameValuePair("target", resourceClientId));

			//
			urlParameters.add(new BasicNameValuePair("client_id",
					resourceClientId));
			urlParameters.add(new BasicNameValuePair("scope",
					"openid name email"));
			urlParameters.add(new BasicNameValuePair("api_type", "app"));
			urlParameters.add(new BasicNameValuePair("id_token", session
					.getToken().toString().split(" ")[1]));

			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpResponse response = client.execute(post);
			JsonReader reader = Json.createReader(response.getEntity()
					.getContent());

			JsonObject responseObject = reader.readObject();

			if (responseObject.containsKey("")) {

			}
			JWT jwt = new JWT();
			jwt.setType(responseObject.getString("token_type"));
			jwt.setIdToken(responseObject.getString("id_token"));

			session.getDelegated().put(resourceClientId, jwt);

			return "Bearer " + responseObject.getString("id_token");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
