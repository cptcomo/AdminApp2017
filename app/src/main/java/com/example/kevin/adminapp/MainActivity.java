package com.example.kevin.adminapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AsyncResponse{
    public static final MediaType FORM_DATA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private final String BLUE_CHEESE_URL = "https://docs.google.com/forms/d/1pitt9JZfNmenfGQ4TznAdKFJYjAT8HZPqu-nEs826cU/formResponse";
    private final String G3_URL = "https://docs.google.com/forms/d/e/1FAIpQLSfx_g9zbqds3KcdYsoa0gLq7behhZsWfXk1e3u-_-h7EBuy3A/formResponse";
    private final String[] SPREADSHEET_URLS = {BLUE_CHEESE_URL, G3_URL};
    private int currentSpreadsheet = 0;

    public static final String[] INITALS_KEY = {"entry_1866261740", "entry_1789585754"};
    public static final String[] TEAM_NUMBER_KEY = {"entry_454837117", "entry_148913451"};
    public static final String[] MATCH_NUMBER_KEY = {"entry_518959206", "entry_1844915886"};
    public static final String[] GEAR_IN_AUTO_KEY = {"entry_1624132469", "entry_1024106334"};
    public static final String[] LOW_SCORE_IN_AUTO_KEY = {"entry_703108754", "entry_1205206372"};
    public static final String[] HIGH_FUEL_AUTO_KEY = {"entry_1358789974", "entry_111459469"};
    public static final String[] GEARS_DELIVERED_KEY = {"entry_1947458747", "entry_749764785"};
    public static final String[] LOW_GOAL_CYCLES_KEY = {"entry_1105753301", "entry_1401458059"};
    public static final String[] HIGH_GOAL_CYCLES_KEY = {"entry_526792915", "entry_82527184"};
    public static final String[] HIGH_GOAL_MISSES_KEY = {"entry_1725977695", "entry_395459313"};
    public static final String[] CARGO_SIZE_KEY = {"entry_1962237108", "entry_1276450365"};
    public static final String[] DEFENDS_KEY = {"entry_1165597892", "entry_703827150"};
    public static final String[] HANGS_KEY = {"entry_385527112", "entry_658280372"};
    public static final String[] COMMENTS_KEY = {"entry_483134571", "entry_694395040"};

    Context context;

    Button searchButton;
    ListView filesListView;

    ArrayList<File> filesList = new ArrayList<File>();
    private TextView versionText;

    File sentFile;

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        searchButton = (Button)findViewById(R.id.searchButton);
        versionText = (TextView)findViewById(R.id.versionText);
        filesListView = (ListView)findViewById(R.id.filesListView);

        versionText.setText("Version: " + BuildConfig.VERSION_NAME + " - Compatible with: Prerelease 3+");
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v){
                send();
            }
        });
    }

    private void send(){
        if(Environment.getExternalStorageState().equals("mounted")){
            filesList.clear();
            walkDir(Environment.getExternalStorageDirectory());
            if(filesList.isEmpty()){
                displayText("No file found", 3);
                return;
            }
            ArrayList<String> filePaths = new ArrayList<>();
            for(File file : filesList){
                filePaths.add(file.getAbsolutePath());
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filePaths);
            filesListView.setAdapter(arrayAdapter);

            AdapterView.OnItemLongClickListener onFileClickedListener = new AdapterView.OnItemLongClickListener() {
                @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
                    ArrayList<String> data = new ArrayList<>();
                    File selectedFile = filesList.get(position);
                    sentFile = selectedFile;
                    data = load(selectedFile);
                    sendRoutine(data);
                    return true;
                }
            };

            filesListView.setOnItemLongClickListener(onFileClickedListener);
        }
        else {
            displayText("External storage not readable", 3);
        }
    }

    private void sendRoutine(ArrayList<String> data){
        for(int i = 0; i < data.size(); i++){
            System.out.println(data.get(i));
            String[] entryElements = split(data.get(i));
            System.out.println(i + ": " + Arrays.toString(entryElements));
            PostDataTask pdt = new PostDataTask(this);
            pdt.execute(SPREADSHEET_URLS[currentSpreadsheet], entryElements[1], entryElements[2], entryElements[3], entryElements[4],
                    entryElements[5], entryElements[6], entryElements[7], entryElements[8], entryElements[9], entryElements[10],
                    entryElements[11], entryElements[12], entryElements[0], entryElements[13]);
        }
    }

    private ArrayList<String> load(File file){
        //Returns an arraylist with each entry as an element

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        }catch(FileNotFoundException e){e.printStackTrace();}

        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        String line;
        int i = 0;
        ArrayList<String> data = new ArrayList();
        try {
            while((line = br.readLine()) != null){
                data.add(i, line);
                i++;
            }
        }catch(IOException e){e.printStackTrace();}
        return data;
    }

    private void walkDir(File dir){
        File[] listFile;
        listFile = dir.listFiles();
        System.out.println(Arrays.toString(dir.listFiles()));
        if(listFile != null){
            for(int i = 0; i < listFile.length; i++){
                System.out.println(listFile[i].getAbsolutePath());
                if(listFile[i].isDirectory()){
                    walkDir(listFile[i]);
                }
                else {
                    if(listFile[i].getName().startsWith("Steamworks Scouting Data")){
                        filesList.add(listFile[i]);
                    }
                }
            }
        }
    }

    private String[] split(String line){
        String[] output = new String[14];
        for(int j = 0; j < output.length; j++){
            output[j] = "";
        }

        int j = 0;
        for(int i = 0; i < line.length(); i++){
            if(line.charAt(i) == '|'){
                j++;
            }
            else {
                output[j] += line.charAt(i);
            }
        }
        return output;
    }

    private class PostDataTask extends AsyncTask<String, Void, Boolean> {
        public AsyncResponse delegate;
        public PostDataTask(AsyncResponse a){
            this.delegate = a;
        }

        @Override protected Boolean doInBackground(String... contactData){
            Boolean result = true;
            String url = contactData[0];
            String postBody = "";
            try {
                postBody = TEAM_NUMBER_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[1], "UTF-8") +
                        "&" + MATCH_NUMBER_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[2], "UTF-8") +
                        "&" + GEAR_IN_AUTO_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[3], "UTF-8") +
                        "&" + LOW_SCORE_IN_AUTO_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[4], "UTF-8") +
                        "&" + HIGH_FUEL_AUTO_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[5], "UTF-8") +
                        "&" + GEARS_DELIVERED_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[6], "UTF-8") +
                        "&" + LOW_GOAL_CYCLES_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[7], "UTF-8") +
                        "&" + HIGH_GOAL_CYCLES_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[8], "UTF-8") +
                        "&" + HIGH_GOAL_MISSES_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[9], "UTF-8") +
                        "&" + CARGO_SIZE_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[10], "UTF-8") +
                        "&" + DEFENDS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[11], "UTF-8") +
                        "&" + HANGS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[12], "UTF-8") +
                        "&" + COMMENTS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[13], "UTF-8") +
                        "&" + INITALS_KEY[currentSpreadsheet] + "=" + URLEncoder.encode(contactData[14], "UTF-8");
            } catch (UnsupportedEncodingException e){
                result = false;
            }
            try {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(FORM_DATA_TYPE, postBody);
                Request request = new Request.Builder().url(url).post(body).build();
                Response response = client.newCall(request).execute();
            } catch(IOException e){
                result = false;
            }
            return result;
        }
        @Override protected void onPostExecute(Boolean result){
            delegate.processFinish(result);
        }
    }

    @Override public void processFinish(boolean result) {
        if(result){
            displayText("Data successfuly sent!", 3);
            if(!sentFile.delete())
                displayText("Couldn't delete file", 2);
        }
        else {
            displayText("There was a problem with sending. It was probably due to lack of Internet" ,3);
        }
    }

    private void displayText(String text, int duration){
        Toast.makeText(context, text, duration).show();
    }
}
