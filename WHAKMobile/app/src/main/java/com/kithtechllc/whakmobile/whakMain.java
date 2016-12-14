package com.kithtechllc.whakmobile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.*;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;
import static java.util.Arrays.asList;

import org.bson.Document;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class whakMain extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whak_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        try {

            URL myURL = new URL("http://www.whaknb.com/services.html");
            new GetScheduleTask().execute(myURL);
        }catch(MalformedURLException ex){

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.whak_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_schedule) {
            // Handle the schedule action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setUpMainView(String text, long result){
        if(result == 1){
            TextView mainScheduleView = (TextView) findViewById(R.id.mainTodaySchedule);
            mainScheduleView.setText(processWebText(text));
        }
        else{
            TextView mainScheduleView = (TextView) findViewById(R.id.mainTodaySchedule);
            mainScheduleView.setText(text);
        }
    }

    private String processWebText(String text){
        StringBuilder retVal = new StringBuilder();
        String[] cols = new String[]{"width=\"93\"", "width=\"119\"", "width=\"114\"", "width=\"118\"", "width=\"125\"", "width=\"107\"", "width=\"100\""};
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;

        if(dayOfWeek == 0)
            retVal.append("No classes on Sunday!");
        else{
            String searchStr = cols[0];
            int ndx = text.indexOf(searchStr);
            String substr = text.substring(ndx + searchStr.length()); // get rid of first row (Class Hours, M, T, W, Th, F, S;
            searchStr = cols[0];
            ndx = substr.indexOf(searchStr);
            while(ndx > -1) {
                substr = substr.substring(ndx); // now we are at start of time...class....section
                ndx = substr.indexOf("<span style=");
                substr = substr.substring(ndx);// just before time
                ndx = substr.indexOf(";\">");
                substr = substr.substring(ndx + 3);//next text should be time
                String timeStr = substr.substring(0, substr.indexOf("<br soft"));

                searchStr = cols[dayOfWeek];
                substr = substr.substring(substr.indexOf(searchStr)); // should be at info inside today at the first Time
                // here we need to find the text between <td align="center"> and </td>
                // so that we may check for an empty column; empty columns do not include the <span style=" section below
                String testEmptyColumn = "";
                ndx = substr.indexOf("<td align=\"center");
                int ndx2 = substr.indexOf("</td>");
                if(ndx2 < ndx) {
                    ndx = substr.indexOf("<td nowrap height=\"35\" width=\"96\" align=\"center");
                    ndx2 = substr.indexOf("</td>", ndx);
                }
                if(ndx >= 0 && ndx2 >= 0 && ndx2 > ndx)
                    testEmptyColumn = substr.substring(ndx, ndx2);

                if(testEmptyColumn.indexOf("<span style=") >=0) { // this is not an empty column
                    substr = substr.substring(substr.indexOf("<span style="));//just before class text
                    substr = substr.substring(substr.indexOf(";\">") + 3);//next text should be time
                    String classStr = substr.substring(0, substr.indexOf("<br soft"));
                    classStr = classStr.replace("<br>", " : ");
                    classStr = classStr.replace("&amp;", "&");
                    if(!timeStr.trim().isEmpty() && !classStr.trim().isEmpty()) {
                        retVal.append(String.format("%1$s - %2$s", timeStr, classStr));
                        if(ndx != -1)
                            retVal.append("\n");
                    }
                }
                else{//this case for test purposes only, will remove later
                    if(!timeStr.trim().isEmpty()) {
                        retVal.append(String.format("%1$s - %2$s", timeStr, "No Classes"));
                        if (ndx != -1)
                            retVal.append("\n");
                    }
                }

                searchStr = cols[0];
                ndx = substr.indexOf(searchStr);
            }
        }

        return retVal.toString();
    }
    private class GetScheduleTask extends AsyncTask<URL, Integer, Long> {
        private String fullPageText = "";
        protected Long doInBackground(URL... urls) {
            long retVal = 1;

            try {
                MongoClientURI uri = new MongoClientURI("mongodb://whakuser:441066@ds011168.mlab.com:11168/whakdb");
                MongoClient mongoClient = new MongoClient(uri);
                MongoDatabase db = mongoClient.getDatabase(uri.getDatabase());

                FindIterable<Document> iterable = db.getCollection("Schedule").find();
                Document d = iterable.first();
                //iterable.forEach(new Block<Document>() {
                //   @Override
                //    public void apply(final Document document) {
                //       fullPageText = document.toString();
                //   }
                //});

                fullPageText = d.toString();
                retVal = 0;
            }
            catch(Exception ex1){

                fullPageText = ex1.getMessage();
                retVal = 0;
            }


            return retVal ;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Long result) {
            fullPageText = fullPageText.trim();
            if(fullPageText.isEmpty())
                setUpMainView("Could not connect to web site.", result);
            else
                setUpMainView(fullPageText, result);
        }
    }
}
