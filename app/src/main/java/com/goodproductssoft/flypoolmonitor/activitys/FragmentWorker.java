package com.goodproductssoft.flypoolmonitor.activitys;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goodproductssoft.flypoolmonitor.CustomApp;
import com.goodproductssoft.flypoolmonitor.HttpHandler;
import com.goodproductssoft.flypoolmonitor.MyPreferences;
import com.goodproductssoft.flypoolmonitor.R;
import com.goodproductssoft.flypoolmonitor.WebService;
import com.goodproductssoft.flypoolmonitor.adapters.YourWorkerAdapter;
import com.goodproductssoft.flypoolmonitor.models.Miner;
import com.goodproductssoft.flypoolmonitor.models.TotalYourWorker;
import com.goodproductssoft.flypoolmonitor.models.YourWorker;
import com.goodproductssoft.flypoolmonitor.models.YourWorkerNotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by user on 4/12/2018.
 */

public class FragmentWorker extends Fragment {
    private ArrayList<YourWorker> valueList;
    private ListView lv;
    private TextView totalYourWorker1, totalYourWorker2, total_current, total_average, total_valid, total_stale,
            total_invalid, unit_current, unit_reported;
    private TotalYourWorker yourWorkerTotal;
    RelativeLayout progressbar;
    SharedPreferences pref;
    int checkAccount;
    MyPreferences myPreferences;
    Miner miner;
    static String endpointEth = "https://api.ethermine.org";

    ProgressDisplay getListener(){
        if(getActivity() instanceof ProgressDisplay){
            return ((ProgressDisplay) getActivity() );
        }
        return null;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_worker, viewGroup, false);
        lv = (ListView) view.findViewById(R.id.list);
        totalYourWorker1 = (TextView)view.findViewById(R.id.total1);
        totalYourWorker2 = (TextView) view.findViewById(R.id.total2);
        total_current = (TextView)view.findViewById(R.id.total_current);
        total_average = (TextView) view.findViewById(R.id.total_average);
        total_valid = (TextView) view.findViewById(R.id.total_valid);
        total_stale = (TextView) view.findViewById(R.id.total_stale);
        total_invalid = (TextView) view.findViewById(R.id.total_invalid);
        progressbar = (RelativeLayout) view.findViewById(R.id.progressbar);
//        unit_current = (TextView) view.findViewById(R.id.unit_current);
//        unit_reported = (TextView) view.findViewById(R.id.unit_reported);

        myPreferences = MyPreferences.getInstance();
        valueList = new ArrayList<>();
        yourWorkerTotal = new TotalYourWorker();
        CheckInitContent(getActivity());
        return view;
    }

    public void CheckInitContent(Context a){
        miner = GetMinerIdActive();
        if(miner != null) {
//        pref = a.getSharedPreferences("MyPref", 0);
//        String idMiner = pref.getString("ID_MINER", "");
//        if(idMiner != null && !idMiner.isEmpty()) {
//            String enpoint = pref.getString("account", "");
//            if(enpoint != null && !enpoint.isEmpty()) {
            String urlWorker = miner.getEndpoint() + "/miner/" + miner.getId() + "/workers";
//            new FragmentWorker.GetWorker().execute(urlWorker);
            GetDataWorkers(miner.getId());
//            }
//        }
        }
    }

    private Miner GetMinerIdActive(){
        ArrayList<Miner> miners = myPreferences.GetIdMiners();
        if(miners != null && !miners.isEmpty()) {
            for (int i = 0; i < miners.size(); i++) {
                if (miners.get(i).isActive()) {
                    return miners.get(i);
                }
            }
        }
        return null;
    }

    private void GetDataWorkers(String id){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(miner.getEndpoint())
                .client(CustomApp.SetConnectTimeOut())
                .build();
        WebService ws = retrofit.create(WebService.class);
        Call<ResponseBody> result = ws.GetWorkers(id);
        final Activity activity = getActivity();
        if(getListener() != null){
            getListener().showProgress();
        }
        result.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String jsonStr = response.body().string();
                    ArrayList<String> listWorker = new ArrayList<>();

                    // Getting JSON Array node
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray workers = jsonObj.getJSONArray("data");
                    if(workers != null && workers.length() > 0) {
                        ArrayList<YourWorkerNotify> yourWorkerNotifys = new ArrayList<>();
                        int countCurrentWorker = 0, countActiveWorker = 0;
                        // looping through All Contacts
                        for (int i = 0; i < workers.length(); i++) {
                            JSONObject value = workers.getJSONObject(i);
                            YourWorker itemYourWorker = new YourWorker();
                            YourWorkerNotify yourWorkerNotify = new YourWorkerNotify();
                            String worker = value.getString("worker");
                            countCurrentWorker++;
                            yourWorkerTotal.setYourWorkerCurrent(countCurrentWorker);
                            itemYourWorker.setYourWorker(worker);
                            listWorker.add(worker);
                            yourWorkerNotify.setNameYourWorker(worker);
                            yourWorkerNotify.setIdMiner(miner.getId());

                            double currentHashrate;
                            try{
                                currentHashrate = value.getDouble("currentHashrate");
                            }catch (JSONException e){
                                currentHashrate = 0;
                            }

                            if(currentHashrate == 0){
                                itemYourWorker.setValue(false);
                            }
                            else {
                                itemYourWorker.setValue(true);
                                countActiveWorker++;
                            }
                            yourWorkerNotify.setCurrentHashrate(currentHashrate);
//                        String strCurrentHashrate = new DecimalFormat("#.#").format(currentHashrate/1000000);
                            double dbconvertTotalCurrentHashrate = (double) currentHashrate + yourWorkerTotal.getCurrent();
                            //double temCurrent = new Double(yourWorker.getTotalCurrent() + Double.parseDouble(strCurrentHashrate.toString()));
                            yourWorkerTotal.setCurrent(dbconvertTotalCurrentHashrate);
                            itemYourWorker.setCurrent(currentHashrate);

                            double averageHashrate;
                            try{
                                averageHashrate = value.getDouble("averageHashrate");
                            }
                            catch (JSONException e){
                                averageHashrate = 0;
                            }
                            yourWorkerTotal.setYourWorkerActive(countActiveWorker);
//                        String strReportedHashrate = new DecimalFormat("#.#").format(reportedHashrate/1000000);
                            double dbconvertTotalAverateHashrate = (double) averageHashrate + yourWorkerTotal.getAverage();
                            yourWorkerTotal.setAverage(dbconvertTotalAverateHashrate);
                            itemYourWorker.setAverage(averageHashrate);

                            int validShares;
                            try{
                                validShares = value.getInt("validShares");
                            }catch (JSONException e){
                                validShares = 0;
                            }
                            yourWorkerTotal.setValid(yourWorkerTotal.getValid() + validShares);
                            itemYourWorker.setValid(validShares);

                            int staleShares;
                            try{
                                staleShares = value.getInt("staleShares");
                            }
                            catch (JSONException e){
                                staleShares = 0;
                            }
                            yourWorkerTotal.setStale(yourWorkerTotal.getStale() + staleShares);
                            itemYourWorker.setStale(staleShares);

                            int invalidShares;
                            try{
                                invalidShares = value.getInt("invalidShares");
                            }
                            catch (JSONException e){
                                invalidShares = 0;
                            }
                            yourWorkerTotal.setInvalid(yourWorkerTotal.getInvalid() + invalidShares);
                            itemYourWorker.setInvalid(invalidShares);

                            long lastSeen;
                            try{
                                lastSeen = value.getLong("lastSeen");
                            }
                            catch (JSONException e){
                                lastSeen = 0;
                            }
                            String strLastTime = "";
                            if(lastSeen != 0) {
                                Calendar calendar = Calendar.getInstance();
                                long now = calendar.getTimeInMillis() / 1000;
                                strLastTime = String.valueOf((now - lastSeen) / 60);
                            }
                            itemYourWorker.setLastScreen(strLastTime);
                            valueList.add(itemYourWorker);
                            yourWorkerNotifys.add(yourWorkerNotify);
                        }
                        miner.setIdMinerBackup(miner.getId());
                        miner.setWorkersBackup(yourWorkerNotifys);
                        if(activity != null) {
                            myPreferences.UpdateMiner(miner);
                        }
                        if (activity != null) {
                            if (valueList.size() > 0) {
                                YourWorkerAdapter adapter = new YourWorkerAdapter(getActivity(), valueList);
                                lv.setAdapter(adapter);
                            }
                            totalYourWorker1.setText(String.valueOf(yourWorkerTotal.getYourWorkerActive()));
                            totalYourWorker2.setText(String.valueOf(yourWorkerTotal.getYourWorkerCurrent()));
                            total_current.setText(ChangeHashrateWithUnit(yourWorkerTotal.getCurrent()));
                            total_average.setText(ChangeHashrateWithUnit(yourWorkerTotal.getAverage()));
                            total_valid.setText(String.valueOf(yourWorkerTotal.getValid()));
                            total_stale.setText(String.valueOf(yourWorkerTotal.getStale()));
                            total_invalid.setText(String.valueOf(yourWorkerTotal.getInvalid()));
                        }
                    }
                    else{
                        if(activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CustomApp.showToast("No data");
                                }
                            });
                        }
                    }
                } catch (Exception ex){
                    if (activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                CustomApp.showToast("Data error!");
                            }
                        });
                    }
                }
                if(getListener() != null){
                    getListener().hideProgress();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CustomApp.showToast("Couldn't get data from server!");
                        }
                    });
                }

                if(getListener() != null){
                    getListener().hideProgress();
                }
            }
        });
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetWorker extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
//            progressbar.setVisibility(View.VISIBLE);
            if(getListener() != null){
                getListener().showProgress();
            }
        }

        @Override
        protected Void doInBackground(String... url) {
            HttpHandler sh = new HttpHandler();
            Activity activity = getActivity();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url[0]);
            ArrayList<String> listWorker = new ArrayList<>();
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray workers = jsonObj.getJSONArray("data");
                    ArrayList<YourWorkerNotify> yourWorkerNotifys = new ArrayList<>();
                    int countCurrentWorker = 0, countActiveWorker = 0;
                    // looping through All Contacts
                    for (int i = 0; i < workers.length(); i++) {
                        JSONObject value = workers.getJSONObject(i);
                        YourWorker itemYourWorker = new YourWorker();
                        YourWorkerNotify yourWorkerNotify = new YourWorkerNotify();
                        String worker = value.getString("worker");
                        countCurrentWorker++;
                        yourWorkerTotal.setYourWorkerCurrent(countCurrentWorker);
                        itemYourWorker.setYourWorker(worker);
                        listWorker.add(worker);
                        yourWorkerNotify.setNameYourWorker(worker);
                        yourWorkerNotify.setIdMiner(miner.getId());

                        double currentHashrate;
                        try{
                            currentHashrate = value.getDouble("currentHashrate");
                        }catch (JSONException e){
                            currentHashrate = 0;
                        }

                        if(currentHashrate == 0){
                            itemYourWorker.setValue(false);
                        }
                        else {
                            itemYourWorker.setValue(true);
                            countActiveWorker++;
                        }
                        yourWorkerNotify.setCurrentHashrate(currentHashrate);
//                        String strCurrentHashrate = new DecimalFormat("#.#").format(currentHashrate/1000000);
                        double dbconvertTotalCurrentHashrate = (double) currentHashrate + yourWorkerTotal.getCurrent();
                        //double temCurrent = new Double(yourWorker.getTotalCurrent() + Double.parseDouble(strCurrentHashrate.toString()));
                        yourWorkerTotal.setCurrent(dbconvertTotalCurrentHashrate);
                        itemYourWorker.setCurrent(currentHashrate);

                        double averageHashrate;
                        try{
                            averageHashrate = value.getDouble("averageHashrate");
                        }
                        catch (JSONException e){
                            averageHashrate = 0;
                        }
                        yourWorkerTotal.setYourWorkerActive(countActiveWorker);
//                        String strReportedHashrate = new DecimalFormat("#.#").format(reportedHashrate/1000000);
                        double dbconvertTotalAverateHashrate = (double) averageHashrate + yourWorkerTotal.getAverage();
                        yourWorkerTotal.setAverage(dbconvertTotalAverateHashrate);
                        itemYourWorker.setAverage(averageHashrate);

                        int validShares;
                        try{
                            validShares = value.getInt("validShares");
                        }catch (JSONException e){
                            validShares = 0;
                        }
                        yourWorkerTotal.setValid(yourWorkerTotal.getValid() + validShares);
                        itemYourWorker.setValid(validShares);

                        int staleShares;
                        try{
                            staleShares = value.getInt("staleShares");
                        }
                        catch (JSONException e){
                            staleShares = 0;
                        }
                        yourWorkerTotal.setStale(yourWorkerTotal.getStale() + staleShares);
                        itemYourWorker.setStale(staleShares);

                        int invalidShares;
                        try{
                            invalidShares = value.getInt("invalidShares");
                        }
                        catch (JSONException e){
                            invalidShares = 0;
                        }
                        yourWorkerTotal.setInvalid(yourWorkerTotal.getInvalid() + invalidShares);
                        itemYourWorker.setInvalid(invalidShares);

                        long lastSeen;
                        try{
                            lastSeen = value.getLong("lastSeen");
                        }
                        catch (JSONException e){
                            lastSeen = 0;
                        }
                        String strLastTime = "";
                        if(lastSeen != 0) {
                            Calendar calendar = Calendar.getInstance();
                            long now = calendar.getTimeInMillis() / 1000;
                            strLastTime = String.valueOf((now - lastSeen) / 60);
                        }
                        itemYourWorker.setLastScreen(strLastTime);
                        valueList.add(itemYourWorker);
                        yourWorkerNotifys.add(yourWorkerNotify);
                    }
                    miner.setIdMinerBackup(miner.getId());
                    miner.setWorkersBackup(yourWorkerNotifys);
                    if(activity != null) {
                        myPreferences.UpdateMiner(miner);
                    }
                } catch (final JSONException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "Data parsing error",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Couldn't get Data from server.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
//            progressbar.setVisibility(View.GONE);
            if(getListener() != null){
                getListener().hideProgress();
            }
            /**
             * Updating parsed JSON data into ListView
             * */
            Activity activity = getActivity();
            if (isAdded() && activity != null) {
                if (valueList.size() > 0) {
                    YourWorkerAdapter adapter = new YourWorkerAdapter(getActivity(), valueList);
                    lv.setAdapter(adapter);
                }
                totalYourWorker1.setText(String.valueOf(yourWorkerTotal.getYourWorkerActive()));
                totalYourWorker2.setText(String.valueOf(yourWorkerTotal.getYourWorkerCurrent()));
                total_current.setText(ChangeHashrateWithUnit(yourWorkerTotal.getCurrent()));
                total_average.setText(ChangeHashrateWithUnit(yourWorkerTotal.getAverage()));
                total_valid.setText(String.valueOf(yourWorkerTotal.getValid()));
                total_stale.setText(String.valueOf(yourWorkerTotal.getStale()));
                total_invalid.setText(String.valueOf(yourWorkerTotal.getInvalid()));
            }
        }

    }

    public String ChangeHashrateWithUnit(double value){
        String strTempValue = new DecimalFormat("#.##").format(value) + " H/s";
        if(value / 1000 >= 1){
            double lTempHS = value / 1000;
            strTempValue = new DecimalFormat("#.##").format(lTempHS) + " KH/s";
            if(lTempHS / 1000 >= 1){
                double lTempKH = lTempHS / 1000;
                strTempValue = new DecimalFormat("#.##").format(lTempKH) + " MH/s";
                if(lTempKH / 1000 >= 1){
                    double lTempMH = lTempKH / 1000;
                    strTempValue =  new DecimalFormat("#.##").format(lTempMH) + " GH/s";
                    if(lTempMH / 1000 >= 1){
                        double lTempGH = lTempMH / 1000;
                        strTempValue = new DecimalFormat("#.##").format(lTempGH) + " TH/s";
                    }
                }
            }
        }
        return strTempValue;
    }

    public String ChangeHashrateUnit(double value){
        String strTempValue = "H/s";
        if(value / 1000 >= 1){
            double lTempHS = value / 1000;
            strTempValue = " KH/s";
            if(lTempHS / 1000 >= 1){
                double lTempKH = lTempHS / 1000;
                strTempValue = " MH/s";
                if(lTempKH / 1000 >= 1){
                    double lTempMH = lTempKH / 1000;
                    strTempValue =  " GH/s";
                    if(lTempMH / 1000 >= 1){
                        strTempValue = " TH/s";
                    }
                }
            }
        }
        return strTempValue;
    }

    public interface ProgressDisplay {

        void showProgress();

        void hideProgress();
    }
}
