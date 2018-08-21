package com.example.admin.recipes;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.admin.recipes.MySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.UnsupportedEncodingException;
import java.net.URI;

public class MainActivity extends AppCompatActivity  implements AdapterView.OnItemClickListener{

    ListView list;
    EditText search;
    Button button;
    String[] labels = new String[10];
    String[] authors = new String[10];
    String[] srcs = new String[10];
    JSONArray hits;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search = (EditText) findViewById(R.id.searchText);
        button = (Button) findViewById(R.id.searchButton);
        list = (ListView) findViewById(R.id.list_view);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                String searchKey = search.getText().toString();
                //Toast.makeText(MainActivity.this,searchKey,Toast.LENGTH_SHORT).show();
                StringRequest stringRequest = new StringRequest(Request.Method.GET, "/*Insert link from which JSON response/HTML response will be fetched*/",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Toast.makeText(MainActivity.this,response,Toast.LENGTH_SHORT).show();
                                Document document = Jsoup.parse(response);
                                Element body = document.body();
                                try {
                                    JSONObject jsonObject = new JSONObject(body.text());
                                    hits = jsonObject.getJSONArray("hits");
                                    for(int i = 0; i< hits.length(); i++)
                                    {
                                        JSONObject myobj = hits.getJSONObject(i).getJSONObject("recipe");
                                        String author = myobj.getString("source");
                                        String label = myobj.getString("label");
                                        String src = myobj.getString("image");
                                        labels[i] = label;
                                        authors[i] = author;
                                        srcs[i] = src;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(MainActivity.this,"Request failed",Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(stringRequest);
                //Log.d("Server",s);

                MyAdapter myAdapter = new MyAdapter(MainActivity.this, labels, authors, srcs);
                list.setAdapter(myAdapter);
                list.setOnItemClickListener(MainActivity.this);
                }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(MainActivity.this,RecipeActivity.class);
        try {
            intent.putExtra("recipe",hits.getJSONObject(position).getJSONObject("recipe").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }
}

class MyAdapter extends ArrayAdapter<String>{
    Context context;
    String[] labels;
    String[] authors;
    String[] srcs;
    MyAdapter(Context c, String[] labels, String[] authors, String[] srcs){
        super(c,R.layout.list_row,R.id.labelView,labels);
        this.context=c;
        this.authors = authors;
        this.srcs = srcs;
        this.labels=labels;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.list_row, parent, false);
        final ImageView imageView = (ImageView) row.findViewById(R.id.img);
        TextView label = (TextView) row.findViewById(R.id.labelView);
        TextView author = (TextView) row.findViewById(R.id.authorView);
        label.setText(labels[position]);
        author.setText("By- "+authors[position]);
        //For the image, sending the request now, thank you very much!
        ImageRequest imageRequest = new ImageRequest(srcs[position], new Response.Listener<Bitmap>(){

            @Override
            public void onResponse(Bitmap response) {
                imageView.setImageBitmap(response);
            }},imageView.getMaxHeight(),imageView.getMaxWidth(),ImageView.ScaleType.CENTER_CROP,null, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        MySingleton.getInstance(this.getContext()).addtoRQ(imageRequest);
        return row;
    }

}