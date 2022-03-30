package edu.bsu.cs411.myweatheralert;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends AppCompatActivity {

    ProgressDialog pd;
    ListView alertList;
    Button viewAlertsBTN;
    Spinner stateDropDwn;
    TextView stateNamePlaceHolder, longPressInfo, errorInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setIDs(); configDropDown();
        viewAlertsBTN.setOnClickListener(view -> {
            if (!valueSelected()) {
                showErrorToast(R.string.no_value_selected_error);
            }
            else {
                new XmlTask().execute(getURL());
                loadTextAnimation(longPressInfo);
            }
        });
    }

    private void setIDs() {
        stateDropDwn = findViewById(R.id.statesDropDwn);
        viewAlertsBTN = findViewById(R.id.button);
        alertList = findViewById(R.id.alertList);
        stateNamePlaceHolder = findViewById(R.id.stateName);
        longPressInfo = findViewById(R.id.longPressInfo);
        errorInfo = findViewById(R.id.errorTextView);
    }

    private void loadTextAnimation(TextView view) {
        new CountDownTimer(1000, 1000) {
            @Override public void onTick(long l) {}
            @Override public void onFinish() {
                TranslateAnimation animation = new TranslateAnimation(
                        0, 0, 25, 0);
                animation.setFillAfter(true);
                animation.setDuration(getResources().getInteger(
                        android.R.integer.config_longAnimTime)
                );
                view.startAnimation(animation);
            }
        }.start();
    }

    private void populateListView(String xml) {
        longPressInfo.setText(R.string.long_press_an_alert_to_open_link);
        stateNamePlaceHolder.setText(String.format("Showing Alerts for %s",
                            getSelectedState().toUpperCase()));
        ArrayList<Alert> alerts = getAlerts(xml);
        ArrayList<String> alertStringList = new ArrayList<>();
        for (Alert a : Objects.requireNonNull(alerts)) {
            alertStringList.add(a.toString());
        }
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                alertStringList);
        this.alertList.setAdapter(listAdapter);
        alertList.setOnItemLongClickListener(
                (adapterView, view, i, l) -> {
                    Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(alerts.get(i).getLink()));
                    startActivity(browserIntent);
                    return true;
                }
        );
    }

    public String getDataFromLocalXML() throws IOException {
        InputStream is = getAssets().open("ex_xml.xml");
        int size = is.available();
        byte[] buffer = new byte[size];
        if (is.read(buffer) == -1) {
            is.close();
        }
        return new String(buffer, StandardCharsets.UTF_8);
    }

    private ArrayList<Alert> getAlerts(String xml) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            Document alertsXML = builder.parse(new InputSource(new StringReader(xml)));
            NodeList entries = alertsXML.getElementsByTagName("entry");
            ArrayList<Alert> alertArrayList = new ArrayList<>();
            for (int i = 0; i < entries.getLength(); i++) {
                Node e = entries.item(i);
                if (e.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) e;
                    Element linkElement = (Element) element.getElementsByTagName("link").item(0);
                    String title = element.getElementsByTagName("title").item(0).getTextContent();
                    String link = linkElement.getAttribute("href");
                    String summary = element.getElementsByTagName("summary").item(0).getTextContent();
                    alertArrayList.add(new Alert(title, link, summary));
                }
            }
            return alertArrayList;
        }
        catch (Exception e) {e.printStackTrace();}
        return null;
    }

    private void configDropDown() {
        ArrayAdapter<String> adapter = newStrArrayAdapter();
        stateDropDwn.setAdapter(adapter);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        stateDropDwn.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(
                    AdapterView<?> parent, View view, int position, long id) {
                alertList.setAdapter(null);
                stateNamePlaceHolder.setText(null);
                longPressInfo.setText(null);
                errorInfo.setText(null);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private ArrayAdapter<String> newStrArrayAdapter() {
        return new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.states));
    }

    private String getSelectedState() {
        return stateDropDwn.getSelectedItem()
                .toString()
                .toLowerCase();
    }

    private boolean valueSelected() {
        return !getSelectedState()
                .equalsIgnoreCase("Choose State");
    }

    private String getURL() {
        return String.format("https://alerts.weather.gov/cap/%s.php?x=0", getSelectedState());
    }

    private void showErrorToast(int messageId) {
        Toast.makeText(
                MainActivity.this,
                getString(messageId),
                Toast.LENGTH_LONG).show();
    }

    // -------------------------------------------------------------------------------------
    // ! I've tried everything to allow this app to pull the required XML from the Web.
    // ! Nothing seemed to work, and with little luck, I decided to move forward and
    // ! read from a local XML. Using the url you provided to us, I pasted the received xml
    // ! (from my browser) into my own local XML file (ex_xml). I went ahead and parsed it,
    // ! displaying the content as if I would've fetched it from the web. Sorry, I have tried
    // ! everything.
    // -------------------------------------------------------------------------------------

 
    @SuppressWarnings("all")
    class XmlTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }
        protected void onPostExecute(String xml) {
            super.onPostExecute(xml);
            if (pd.isShowing()){
                pd.dismiss();
            }
            if (xml == null) {
                showErrorToast(R.string.cannot_fetch_data);
                stateNamePlaceHolder.setVisibility(View.INVISIBLE);
                errorInfo.setText(R.string.showing_imported_data);
                errorInfo.setTextColor(Color.RED);
                try { populateListView(getDataFromLocalXML()); }
                catch (IOException e) { e.printStackTrace(); }
            }
            else populateListView(xml);
        }
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String xml;
                while ((xml = reader.readLine()) != null) {
                    buffer.append(xml).append("/n");
                }
                return buffer.toString().trim();
            }
            catch (IOException e)
            { e.printStackTrace(); }

            finally {
                if (connection != null)
                { connection.disconnect(); }
                try {
                    if (reader != null)
                    { reader.close(); }
                }
                catch (IOException e)
                { e.printStackTrace(); }
            }
            return null;
        }
    }
}