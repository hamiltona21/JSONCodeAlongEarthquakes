package ashleyhamilton.honorsmobileapps.jsoncodealongearthquakes;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public final class NetworkingUtilities {


    private static final String LOG_TAG="NetworkingUtilities";
    private NetworkingUtilities() {
    }
    public static List<Earthquake> fetchEarthquakeData(String requestUrlString){
        List<Earthquake> results=null;
        try{
            Thread.sleep(2000);
            URL requestUrl=new URL(requestUrlString);
            String response=makeHttpRequest(requestUrl);
            results=convertJSONtoEarthquakes(response);
        }catch(Exception e){
            Log.e(LOG_TAG, "Error with creating URL", e);
        }
        return results;
    }
    private static String makeHttpRequest(URL url) throws IOException{
        HttpsURLConnection urlConnection=null;
        String jsonResponse="" ;
        InputStream inputStream=null;
        try{
            urlConnection=(HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(10000);
            urlConnection.connect();
            int response=urlConnection.getResponseCode();
            if(response==HttpsURLConnection.HTTP_OK){
                inputStream=urlConnection.getInputStream();
            }
            else{
                Log.e(LOG_TAG, "Error response code: "+response);
            }

            if(inputStream!=null){
                jsonResponse=readFromStream(inputStream);
            }
        }catch(IOException e){
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        }
        finally {
            if(urlConnection!=null){
                urlConnection.disconnect();
            }
            if(inputStream!=null){
                inputStream.close();
            }
        }
        return jsonResponse;
    }
    // Returns a list of Earthquake objects that has been built up from parsing a JSON response.
    public static ArrayList<Earthquake> convertJSONtoEarthquakes(String jsonResponse) {
        ArrayList<Earthquake> earthquakes = new ArrayList<>();
        try {
            JSONObject baseJSONResponse = new JSONObject(jsonResponse);
            JSONArray earthquakeArray = baseJSONResponse.getJSONArray("features");
            for (int i = 0; i < earthquakeArray.length(); i++) {
                JSONObject currentEarthquake = earthquakeArray.getJSONObject(i);
                JSONObject properties = currentEarthquake.getJSONObject("properties");
                double magnitude = properties.getDouble("mag");
                String location = properties.getString("place");
                long time = properties.getLong("time");
                String url = properties.getString("url");
                Earthquake newEarthquake = new Earthquake(magnitude, location, time, url);
                earthquakes.add(newEarthquake);
            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        return earthquakes;
    }
    private static String readFromStream(InputStream inputStream) throws IOException{
        StringBuilder output=new StringBuilder();
        InputStreamReader inputStreamReader=new InputStreamReader(inputStream, "UTF-8");
        BufferedReader reader=new BufferedReader(inputStreamReader);
        String line=reader.readLine();
        while(line!=null){
            output.append(line);
            line=reader.readLine();
        }
        return output.toString();
    }

}
