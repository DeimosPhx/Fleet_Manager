package com.example.fleetmanager.ui.gallery;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.fleetmanager.MainActivity;
import com.example.fleetmanager.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class GalleryFragment extends Fragment implements View.OnClickListener{

    private GalleryViewModel galleryViewModel;
    private ConstraintLayout acutalConstructionLayout;
    private SQLiteDatabase fleetDB;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        //fleetDB = SQLiteDatabase.openOrCreateDatabase("Fleet", null);
        LoadShipData();
    }

    public void LoadShipData(){
        //Load & Parse JSON file
        String JSONDataRaw = loadJSONFromAsset();
        if(JSONDataRaw != null){
            //On parsing split on ship
            if(!parseJSONDataAndCreateCards(JSONDataRaw)){
                Log.d("ERROR -->","Error while parsing and/or creating a ship card");
            }

    //EXAMPLE of text alteration
        /*
            View ship1 = getView().findViewById(R.id.ship1);
            TextView ship1Name = (TextView) ship1.findViewById(R.id.ShipName);
            ship1Name.setText("ISSOU LE DEBILE");
         */
        }

    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("JSON/ships.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private boolean parseJSONDataAndCreateCards(String JSONData){
        try {
            JSONObject obj = new JSONObject(JSONData);
            JSONArray m_jArry = obj.getJSONArray("data");
            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> m_li;

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject shipJSON = m_jArry.getJSONObject(i);
                setupShipCard(shipJSON);
            }

            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setupShipCard(JSONObject shipJSON){
        try {
            //Create ship card layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.ship_catalog_card , null ,false);
            acutalConstructionLayout = layout;
            LinearLayout card_container = (LinearLayout) getView().findViewById(R.id.card_container);
            
            //Set card fields
            ((TextView) layout.findViewById(R.id.ShipName)).setText(shipJSON.getString("name"));
            ((TextView) layout.findViewById(R.id.ShipMake)).setText(shipJSON.getJSONObject("manufacturer").getString("name"));

            //Bind action add to DB on click btn_addToFleet
            /*
                    Log.d("Clicked","Clicked");
                    Log.d("Clicked","name & make "+((TextView)v.findViewById(R.id.ShipName)).getText()+" & "+((TextView)v.findViewById(R.id.ShipMake)).getText());
             */
            FloatingActionButton addToFleetBTN = layout.findViewById(R.id.btn_addToFleet);
            addToFleetBTN.setOnClickListener(this);

            //get image from url
            String url = shipJSON.getJSONArray("media").getJSONObject(0).getString("source_url");
            String[] urlSplit = url.split("/");
            String imgResName = urlSplit[urlSplit.length - 2]+".jpg";
            ((TextView) layout.findViewById(R.id.ThumbnailURL)).setText("ships_img/"+imgResName);

            InputStream is = getActivity().getAssets().open("ships_img/"+imgResName);
            Bitmap b = BitmapFactory.decodeStream(is);
            Bitmap resized = Bitmap.createScaledBitmap(b,3840,2160,false);
            ((ImageView) layout.findViewById(R.id.thumb)).setImageDrawable(new BitmapDrawable(getContext().getResources(),resized));

            card_container.addView(layout);

        }catch (JSONException | IOException e){
            e.printStackTrace();
        }
    }



    private class ThumbnailRetreiver extends AsyncTask<String,Void,Drawable>{
        private Exception exception;
        private ImageView thumbnail_container;

        public ThumbnailRetreiver(ImageView thumbnail_container){
            super();
            this.thumbnail_container = thumbnail_container;
        }

        protected Drawable doInBackground(String... urls){
            try {
                InputStream is = (InputStream) new URL(urls[0]).getContent();
                Drawable img = Drawable.createFromStream(is,null);
                is.close();
                return img;
            }catch (Exception e){
                this.exception = e;
                return null;
            }finally {
            }
        }

        protected void onPostExecute(Drawable img){
            if (this.exception != null)
                exception.printStackTrace();
            if(this.thumbnail_container != null){
                Bitmap b = ((BitmapDrawable)img).getBitmap();
                Bitmap resized = Bitmap.createScaledBitmap(b,3840,2160,false);
                this.thumbnail_container.setImageDrawable(new BitmapDrawable(getContext().getResources(),resized));
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        View parent = (View) view.getParent();

       ((MainActivity)getActivity()).getFleetDB().execSQL("INSERT INTO ships (Name,Make,ImgURL) VALUES" +
               " ('"+((TextView)parent.findViewById(R.id.ShipName)).getText()+"','"+((TextView)parent.findViewById(R.id.ShipMake)).getText()+"','"+((TextView) parent.findViewById(R.id.ThumbnailURL)).getText()+"');");
    }
}