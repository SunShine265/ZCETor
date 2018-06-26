package com.goodproductssoft.flypoolmonitor.adapters;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.goodproductssoft.flypoolmonitor.MyPreferences;
import com.goodproductssoft.flypoolmonitor.R;
import com.goodproductssoft.flypoolmonitor.models.Miner;

import java.util.ArrayList;

/**
 * Created by user on 4/18/2018.
 */

public class IdMinerAdapter extends ArrayAdapter<Miner> {
    ArrayList<Miner> data;
    TextView title_coin;
    Context context;
    static String endpoint = "https://api-zcash.flypool.org";

    public IdMinerAdapter(@NonNull Context context, ArrayList<Miner> data) {
        super(context, 0, data);
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_id_miner, parent, false);
        }
        TextView id_miner, email, payout, ip, title_type_coin, edit_miner_settings;
        ImageView btn_delete_id_miner;
        Switch id_on_off_notification;
        CheckBox id_send_email;
        final Miner item = data.get(position);
        id_miner = (TextView) convertView.findViewById(R.id.id_miner);
        title_coin = (TextView) convertView.findViewById(R.id.title_coin);
        id_on_off_notification = (Switch)convertView.findViewById(R.id.id_on_off_notification);
        email = (TextView) convertView.findViewById(R.id.email);
        payout = (TextView) convertView.findViewById(R.id.payout);
        ip = (TextView) convertView.findViewById(R.id.ip);
        title_type_coin = (TextView) convertView.findViewById(R.id.title_type_coin);
        id_send_email = (CheckBox) convertView.findViewById(R.id.id_send_email);
        edit_miner_settings = (TextView) convertView.findViewById(R.id.edit_miner_settings);

//        id_miner.setText(item.getId());
        String urlTxHash = "";
        if(item.getType() == Miner.CoinType.ETH){
            urlTxHash = "https://www.etherchain.org/account/" + item.getId();
        }
        else if(item.getType() == Miner.CoinType.ETC){
            urlTxHash = "https://etcchain.com/addr/" + item.getId();
        }
        else {
            urlTxHash = "https://explorer.zcha.in/accounts/" + item.getId();
        }
        String linkedText = String.format("<a href=\"%s\">"+ item.getId() + "</a> ", urlTxHash);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            id_miner.setText(Html.fromHtml(linkedText, Html.FROM_HTML_MODE_LEGACY));
        } else {
            id_miner.setText(Html.fromHtml(linkedText));
        }
        id_miner.setMovementMethod(LinkMovementMethod.getInstance());

        title_coin.setText(context.getString(R.string.zcash));

        //update Ui switch
        if(item.isNotification()){
            id_on_off_notification.setChecked(true);
        }
        else {
            id_on_off_notification.setChecked(false);
        }

        id_on_off_notification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            if(isChecked){
                item.setNotification(true);
                MyPreferences.getInstance().UpdateMiner(item);
            }
            else {
                item.setNotification(false);
                MyPreferences.getInstance().UpdateMiner(item);
            }
            }
        });

        if(item.getSettings().getMonitor() == 0){
            id_send_email.setChecked(false);
        }
        else
            id_send_email.setChecked(true);
        email.setText(item.getSettings().getEmail());
        ip.setText(item.getSettings().getIp());
        payout.setText(String.valueOf(item.getSettings().getPayout()));
        String urlURLEditSettings = "";
        String linkedTextEditSetting = "";
        if(item.getType() == Miner.CoinType.ETH){
            urlURLEditSettings = "https://ethermine.org/miners/" + item.getId() + "/settings";
            linkedTextEditSetting = String.format("<a href=\"%s\"> " + "Edit on ethermine.org"  + " </a> ", urlURLEditSettings);
        }
        else if(item.getType() == Miner.CoinType.ETC){
            urlURLEditSettings = "https://etc.ethermine.org/miners/" + item.getId() + "/settings";
            linkedTextEditSetting = String.format("<a href=\"%s\">" + "Edit on etc.ethermine.org" + " </a> ", urlURLEditSettings);
        }
        else {
            urlURLEditSettings = "https://zcash.flypool.org/miners/" + item.getId() + "/settings";
            linkedTextEditSetting = String.format("<a href=\"%s\">" + "Edit on zcash.flypool.org" + "</a> ", urlURLEditSettings);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            edit_miner_settings.setText(Html.fromHtml(linkedTextEditSetting, Html.FROM_HTML_MODE_LEGACY));
        } else {
            edit_miner_settings.setText(Html.fromHtml(linkedTextEditSetting));
        }
        edit_miner_settings.setMovementMethod(LinkMovementMethod.getInstance());
        return convertView;
    }
}