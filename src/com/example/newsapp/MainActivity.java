package com.example.newsapp;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	ArrayList<String> title;
	ArrayList<String> description;
	ArrayList<String> links;
	ProgressDialog prog;
	ListView ne;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		prog=ProgressDialog.show(this, "Retreiving News", "wait");
		ne=(ListView)findViewById(R.id.news);
		new ReadRss().execute("http://feeds.bbci.co.uk/news/world/rss.xml");
		ne.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Uri u=Uri.parse(links.get(position));
				Intent in=new Intent(Intent.ACTION_VIEW,u);
				startActivity(in);
			}
		});
	}
	public String getNews(String url){
		String news="";
		title=new ArrayList<String>();
		description=new ArrayList<String>();
		links=new ArrayList<String>();
		try{
			XmlPullParserFactory pullPar=XmlPullParserFactory.newInstance();
			XmlPullParser pull=pullPar.newPullParser();
			pull.setInput(getHttpInputStream(url),"UTF-8");
			boolean notInsideChannel=false;
			int event=pull.getEventType();
			while(event != XmlPullParser.END_DOCUMENT){
				if(event == XmlPullParser.START_TAG){
					if(pull.getName().equals("item")){
						notInsideChannel=true;
					}
					else if(pull.getName().equals("title")){
						if(notInsideChannel){
							title.add(pull.nextText());
						}
					}
					else if(pull.getName().equals("description")){
						if(notInsideChannel){
							description.add(pull.nextText());
						}
					}
					else if(pull.getName().equals("link")){
						if(notInsideChannel){
							links.add(pull.nextText());
						}
					}
				}else if(event == XmlPullParser.END_TAG && pull.getName().equals("item")){
					notInsideChannel=false;
				}
				event=pull.next();
			}
		}catch(Exception e){
			
		}
		return news;
	}
	public InputStream getHttpInputStream(String url){
		InputStream in=null;
		try{
			URL u=new URL(url);
			URLConnection ucon=u.openConnection();
			if(ucon instanceof HttpURLConnection){
				HttpURLConnection httpcon=(HttpURLConnection)ucon;
				httpcon.setAllowUserInteraction(false);
				httpcon.setInstanceFollowRedirects(true);
				httpcon.setRequestMethod("GET");
				httpcon.connect();
				int resp=httpcon.getResponseCode();
				if(resp == HttpURLConnection.HTTP_OK)
					in=httpcon.getInputStream();
			}
		}catch(Exception e){
			
		}
		return in;
	}
	class ReadRss extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... url) {
			// TODO Auto-generated method stub
			return getNews(url[0]);
		}
		protected void onPostExecute(String news){
			prog.dismiss();
			String[] titleList=new String[title.size()];
			
			for(int i=0;i<title.size();i++){
				titleList[i]=title.get(i);
			}
			ne.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, titleList));
			//ne.setText(Html.fromHtml(news));
			//ne.setText(news);
		}
	}
	
}
