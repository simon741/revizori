package sk.stuba.fiit.revizori.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import sk.stuba.fiit.revizori.Revizori;
import sk.stuba.fiit.revizori.VolleySingleton;
import sk.stuba.fiit.revizori.backendless.BackendlessCoreRequest;
import sk.stuba.fiit.revizori.backendless.BackendlessRequest;
import sk.stuba.fiit.revizori.data.RevizorContract;
import sk.stuba.fiit.revizori.data.RevizorProvider;
import sk.stuba.fiit.revizori.model.Revizor;


public class RevizorService {

    private static RevizorService ourInstance = new RevizorService();

    public static RevizorService getInstance() {
        return ourInstance;
    }

    String url = "/revizor";

    public ArrayList<Revizor> getRevizori() {
        return revizori;
    }

    ArrayList<Revizor> revizori = new ArrayList<>();

    public void createRevizor(Revizor r){
        BackendlessRequest br = new BackendlessRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        BackendlessCoreRequest request = br.getRequest();
        request.setBody(r.getPOSTjson());
        System.out.println(r.getPOSTjson());
        VolleySingleton.getInstance(Revizori.getAppContext()).getRequestQueue().add(request);

    }

    public void getAll(){
        BackendlessRequest br = new BackendlessRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("some", "got response");
                            JSONObject jsonReader = new JSONObject(response);
                            JSONArray submissions = jsonReader.getJSONArray("data");
                            for(int i = 0; i < submissions.length(); i++)
                            {
                                JSONObject sub = submissions.getJSONObject(i);
//                                Revizor r = new Revizor(
//                                    sub.getString("line_number"),
//                                    sub.getDouble("latitude"),
//                                    sub.getDouble("longitude"),
//                                    sub.getString("photo_url"),
//                                    sub.getString("comment"));

                                if (sub.isNull("updated"))
                                    sub.put("updated", 0);
                                if (sub.isNull("ownerId"))
                                    sub.put("ownerId", 0);

//                                r.setCreated(new Date(sub.getInt("created")));
//                                r.setUpdated(new Date(sub.getInt("updated")));
//                                r.setObjectId(sub.getString("objectId"));
//                                r.setOwnerId(sub.getString("ownerId"));

//                                revizori.add(r);

                                ContentValues cv = new ContentValues();
                                cv.put(RevizorContract.RevizorEntry.COLUMN_LINE_NUMBER, sub.getString("line_number"));
                                cv.put(RevizorContract.RevizorEntry.COLUMN_LATITUDE, sub.getString("latitude"));
                                cv.put(RevizorContract.RevizorEntry.COLUMN_LONGITUDE, sub.getString("longitude"));
                                cv.put(RevizorContract.RevizorEntry.COLUMN_PHOTO_URL, sub.getString("photo_url"));
                                cv.put(RevizorContract.RevizorEntry.COLUMN_COMMENT, sub.getString("comment"));
                                cv.put(RevizorContract.RevizorEntry.COLUMN_OBJECT_ID, sub.getString("objectId"));
                                cv.put(RevizorContract.RevizorEntry.COLUMN_OWNER_ID, sub.getString("ownerId"));
                                cv.put(RevizorContract.RevizorEntry.COLUMN_CREATED, sub.getString("created"));
                                cv.put(RevizorContract.RevizorEntry.COLUMN_UPDATED, sub.getString("updated"));

                                ContentResolver cr = Revizori.getAppContext().getContentResolver();
                                Uri u = cr.insert(RevizorContract.RevizorEntry.CONTENT_URI, cv);
                                Log.i("content provider", "insert: " + u.toString());
                                cr.query(RevizorContract.RevizorEntry.CONTENT_URI, null, null, null, null);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            // stop refreshing (spinning icon)
                            // how ??

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                } );
        VolleySingleton.getInstance(Revizori.getAppContext()).getRequestQueue().add(br.getRequest());

    }
}
