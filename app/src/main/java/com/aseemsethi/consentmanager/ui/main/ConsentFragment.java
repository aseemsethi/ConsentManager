package com.aseemsethi.consentmanager.ui.main;

import androidx.lifecycle.ViewModelProvider;

import android.app.DownloadManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.aseemsethi.consentmanager.R;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class ConsentFragment extends Fragment {
    final String TAG = "CM ConsentFrag: ";
    String token = "hello";
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.1F);

    private ConsentViewModel mViewModel;

    public static ConsentFragment newInstance() {
        return new ConsentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.consent_fragment, container, false);
        final Button sButton = root.findViewById(R.id.getAuthB);
        new NukeSSLCerts().nuke();  // TBD
        sButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Log.i(TAG, "OTP Clicked");
                TextView con = (TextView) root.findViewById(R.id.consentT);
                con.setText("Aadhaar Auth success, Email sent to xyz@aravindeyecare.com");
                sendRequest();
            }
        });
        return root;
    }

    /*
      OTP Request in https://uidai.gov.in/images/resource/aadhaar_otp_request_api_2_5.pdf
      Test URLs and some sandbox keys at
      - https://www.uidai.gov.in/914-developer-section.html
      - https://www.uidai.gov.in/916-developer-section/data-and-downloads-section/11350-testing-data-and-license-keys.html
     */
    public void sendRequest() {
        // Instantiate the RequestQueue.
        VolleyLog.setTag("Volley");
        Log.isLoggable("Volley", Log.VERBOSE);
        VolleyLog.DEBUG = true;
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = "https://developer.uidai.gov.in/otp/2.5/";
        //String url = "https://auth.uidai.gov.in/otp/2.5/";
        // Aadhaar OTP API - https://<host>/otp/<ver>/<ac>/<uid[0]>/<uid[1]>/<asalk>
        url += "public/";  // ac
        url += "9/";  // uid[0]/uid[1] - first 2 digits of aadhaar number
        url += "9/";  // uid[0]/uid[1] - first 2 digits of aadhaar number
        url += "MMxNu7a6589B5x5RahDW-zNP7rhGbZb5HsTRwbi-VVNxkoFmkHGmYKM"; // asalk – A valid ASA license key - URL encoded.
        Log.i(TAG, "Sent: " + url);

        //url = "https://developer.uidai.gov.in/otp/1.6/public/9/7/MBni88mRNM18dKdiVyDYCuddwXEQpl68dZAGBQ2nsOlGMzC9DkOVL5s";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i(TAG, "Response is: " + response.substring(0, 500));
                        /* Response
                            <OtpRes ret=”y or n” code=”” txn=”” err=”” ts=”” info=””/>
                         */
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String data = null;
                Log.i(TAG, "That didn't work!" + error.toString());
                try {
                    if (error.networkResponse != null) {
                        data = new String(error.networkResponse.data, "utf-8");
                        Log.i(TAG, "Response: " + data);
                    }
                } catch(UnsupportedEncodingException e) {
                    Log.i(TAG, "Error encoding");
                }
            }
        }) {
            @Override
            public String getBodyContentType() {
                //Log.i(TAG, "GetBodyContentType called..");
                return "application/sml; charset=" + getParamsEncoding();
            }
            /* XML Data Format
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Otp uid=”aadhaar number” ac=”public” sa=”” ver=”2.5”
                txn=token ts=”timestamp” lk=”MBni88mRNM18dKdiVyDYCuddwXEQpl68dZAGBQ2nsOlGMzC9DkOVL5s” type=”A”>
                <Opts ch=””/> ....optional
            <Signature>Digital signature of AUA</Signature>
            </Otp>
 */
            @Override
            public byte[] getBody() throws AuthFailureError {
                String body = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    " <Otp uid=\"999941057058\" ac=\"public\" sa=\"public\" ver=\"2.5\""+
                    " txn=\"1111\" ts=\"timestamp\"" +
                    " lk=\"MBni88mRNM18dKdiVyDYCuddwXEQpl68dZAGBQ2nsOlGMzC9DkOVL5s\"" +
                    " type=\"A\"" +
                    " <Signatare>xxx</Signature>" +
                    " </Otp>";
                //Log.i(TAG, "Added xml:" + body);
                return body.getBytes();
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
        try {
            Log.i(TAG, "Sent Request URL: \n" +
                    stringRequest.getUrl() + "\nBody: " +
                    stringRequest.getBodyContentType() + "\n" +
                    new String(stringRequest.getBody()));
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
    }

    public class HashGenerator {
        public byte[] generateSha256Hash(byte[] message) {
            String algorithm = "SHA-256";
            String SECURITY_PROVIDER = "BC";
            byte[] hash = null;
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance(algorithm, SECURITY_PROVIDER);
                digest.reset();
                hash = digest.digest(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return hash;
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ConsentViewModel.class);
        // TODO: Use the ViewModel
    }

    public static class NukeSSLCerts {
        protected static final String TAG = "NukeSSLCerts";

        public static void nuke() {
            try {
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                                return myTrustedAnchors;
                            }

                            @Override
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                            @Override
                            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                        }
                };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });
            } catch (Exception e) {
            }
        }
    }
}