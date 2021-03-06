package com.turkcell.gelecegiyazanlar.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.splunk.mint.Mint;
import com.turkcell.curio.CurioClient;
import com.turkcell.gelecegiyazanlar.R;
import com.turkcell.gelecegiyazanlar.configurations.AppController;
import com.turkcell.gelecegiyazanlar.configurations.GYConfiguration;
import com.turkcell.gelecegiyazanlar.models.Egitim;
import com.turkcell.gelecegiyazanlar.models.Kisi;

import org.json.JSONArray;
import org.json.JSONException;

import de.hdodenhof.circleimageview.CircleImageView;

public class EgitimIcerikActivity extends ActionBarActivity {

    final Egitim egitim = new Egitim();
    WebView webIcerik;
    TextView yazarIsim;
    CircleImageView resimAvatar;
    String icerikYazi = "", yazarYazi, resimUrl, title;
    Toolbar toolbar;
    String dugumID, dugumBaslik, authorID;
    long time;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egitim_icerik);

        Mint.initAndStartSession(EgitimIcerikActivity.this, GYConfiguration.SPLUNK_ID);

        yazarIsim = (TextView) findViewById(R.id.yazar);
        webIcerik = (WebView) findViewById(R.id.web);
        resimAvatar = (CircleImageView) findViewById(R.id.profile_image);
        webIcerik.getSettings().setJavaScriptEnabled(true);
        webIcerik.getSettings().setUseWideViewPort(true);
        webIcerik.getSettings().setSupportZoom(true);
        webIcerik.getSettings().setDisplayZoomControls(true);
        webIcerik.getSettings().setBuiltInZoomControls(true);
        webIcerik.getSettings().setDefaultFontSize(40);

        webIcerik.getSettings().setLoadWithOverviewMode(true);
        yazarIsim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EgitimIcerikActivity.this, ProfilActivity.class);
                i.putExtra(Kisi.PROFIL_ID, authorID);
                startActivity(i);
            }
        });


        toolbar = (Toolbar) findViewById(R.id.tool_bar2);
        setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();
        dugumID = extras.getString(Egitim.NODE_ID_EGITIM);
        dugumBaslik = extras.getString(Egitim.NODE_TITLE);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.backarrow));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (GYConfiguration.checkInternetConnectionShowDialog(EgitimIcerikActivity.this)) {
            getEgitimIcerik(dugumID);
        }

    }



    public void getEgitimIcerik(final String dugumID) {


        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.yukleniyor));
        pDialog.show();
        Log.d("icerik:", GYConfiguration.getDomain() + "book_content/retrieve?nodeID=" + dugumID);
        JsonArrayRequest movieReq = new JsonArrayRequest(GYConfiguration.getDomain() + "book_content/retrieve?nodeID=" + dugumID,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("json:", GYConfiguration.getDomain() + "book_content/retrieve?nodeID=" + dugumID);
                        hidePDialog();


                        try {
                            JSONArray icerikDizi = response.getJSONObject(0).getJSONArray("content");
                            yazarYazi = response.getJSONObject(0).getString("adSoyad");
                            title = response.getJSONObject(0).getString("title");
                            resimUrl = response.getJSONObject(0).getString("authorAvatarUrl");
                            authorID = response.getJSONObject(0).getString("authorID");
                            Log.d("icerikDiziUzunluk:", String.valueOf(icerikDizi.length()));
                            for (int i = 0; i < icerikDizi.length(); i++) {
                                icerikYazi += icerikDizi.getString(i);
                            }
                            egitim.setIcerik(icerikYazi);
                            egitim.setYazar(yazarYazi);
                            toolbar.setTitle(title);

                            if (yazarYazi != null || icerikYazi != null) {
                                ResimGetir();
                                yazarIsim.setText(egitim.getYazar());
                                webIcerik.loadDataWithBaseURL("", egitim.getIcerik(), "text/html", "UTF-8", "");
                            }
                            CurioClient.getInstance(getApplicationContext()).sendEvent("Egitim ��erik>" + dugumBaslik, title);
                            time = System.currentTimeMillis();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error", "Error: " + error.getMessage());
                Log.d("json:", "Giris Yapilamadi");
                hidePDialog();

            }
        });

        AppController.getInstance().addToRequestQueue(movieReq);
        Log.d("tite", title + dugumBaslik);

    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    public void ResimGetir() {
        ImageRequest image = new ImageRequest(resimUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                resimAvatar.setImageBitmap(response);
            }
        }, 0, 0, null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        AppController.getInstance().addToRequestQueue(image);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        CurioClient.getInstance(getApplicationContext()).endEvent("Egitim ��erik>" + dugumBaslik, title, System.currentTimeMillis() - time);

    }
}
