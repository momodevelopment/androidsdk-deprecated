package vn.momo.momo_partner;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.Map;

import vn.momo.momo_partner.utils.MoMoConfig;
import vn.momo.momo_partner.utils.MoMoUtils;

/**
 * Created by hungdo on 5/8/17.
 */

public class AppMoMoLib {

    private static AppMoMoLib instance;
    public static AppMoMoLib getInstance(){
        if(instance == null)
            instance = new AppMoMoLib();
        return instance;
    }
    //action app momo ex: mapping, payment
    String action = "";
    //environment app momo default debug
    int environment = 0;
    //action type app momo ex: link, get token
    String actionType = "";
    //code request in app momo
    public int REQUEST_CODE_MOMO = 1000;

    //todo set action momo
    public String setAction(Enum _action){
        if(_action.equals(ACTION.MAP)){
            action = MoMoConfig.ACTION_SDK;
        }else{
            action = MoMoConfig.ACTION_PAYMENT;
        }
        return action;
    }
    
    //todo set action type
    public String setActionType(Enum _actionType){
        if(_actionType.equals(ACTION_TYPE.GET_TOKEN)){
            actionType = MoMoConfig.ACTION_TYPE_GET_TOKEN;
        }else {
            actionType = MoMoConfig.ACTION_TYPE_LINK;
        }
        return actionType;
    }

    //todo set environment momo
    public int setEnvironment(Enum _environment){
        if(_environment.equals(ENVIRONMENT.DEBUG)){
            environment = MoMoConfig.ENVIRONMENT_DEBUG;
        }else if(_environment.equals(ENVIRONMENT.DEVELOPMENT)){
            environment = MoMoConfig.ENVIRONMENT_DEVELOPER;
        }else if(_environment.equals(ENVIRONMENT.PRODUCTION)){
            environment = MoMoConfig.ENVIRONMENT_PRODUCTION;
        }else{
            environment = MoMoConfig.ENVIRONMENT_DEBUG;
        }
        return environment;
    }

    //todo request momo
    public void requestMoMoCallBack(Activity activity, Map<String, Object> hashMap) {
        if(action.equals("")){
            Toast.makeText(activity, "Please init AppMoMoLib.getInstance().setAction", Toast.LENGTH_LONG).show();
            return;
        }

        if(hashMap == null){
            Toast.makeText(activity, "Please set data after request", Toast.LENGTH_LONG).show();
            return;
        }
        if(actionType.equals("")){
            Toast.makeText(activity, "Please init AppMoMoLib.getInstance().setActionType", Toast.LENGTH_LONG).show();
            return;
        }else{
            if((action.equals(MoMoConfig.ACTION_SDK) && !actionType.equals(MoMoConfig.ACTION_TYPE_LINK)) ||
                    (action.equals(MoMoConfig.ACTION_PAYMENT) && !actionType.equals(MoMoConfig.ACTION_TYPE_GET_TOKEN))){
                Toast.makeText(activity, "Please set action type and action", Toast.LENGTH_LONG).show();
                return;
            }
        }
        try{
            if(hasMoMo(activity, environment)) {
                Intent intent = new Intent();
                intent.setAction(action);
                String appName;
                ApplicationInfo applicationInfo = activity.getApplicationContext().getApplicationInfo();
                int stringId = applicationInfo.labelRes;
                activity.getPackageName();
                appName = (stringId == 0) ? applicationInfo.nonLocalizedLabel.toString() : activity.getApplicationContext().getString(stringId);
                String  packageName = activity.getPackageName();
                //put data to json object
                JSONObject jsonData = new JSONObject();
                Iterator iterator = hashMap.keySet().iterator();
                try {
                    while(iterator.hasNext()) {
                        String key=(String)iterator.next();
                        Object value = hashMap.get(key);
                        jsonData.put(key, value);
                    }
                    jsonData.put("sdkversion", BuildConfig.VERSION_NAME);
                    jsonData.put("clientIp", MoMoUtils.getIPAddress(true));
                    jsonData.put("appname",appName);
                    jsonData.put("packagename",packageName);
                    jsonData.put("action", actionType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra("JSON_PARAM", jsonData.toString());
                activity.startActivityForResult(intent, REQUEST_CODE_MOMO);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //todo check is momo exits
    private boolean hasMoMo(Activity activity, int environment) {
        boolean result = false;
        Iterator iterator = activity.getPackageManager().getInstalledApplications(0).iterator();
        String packageClass;
        switch (environment){
            case MoMoConfig.ENVIRONMENT_DEBUG://environment debug
                packageClass = MoMoConfig.MOMO_APP_PAKAGE_CLASS_DEBUG;
                break;
            case MoMoConfig.ENVIRONMENT_DEVELOPER://environment developer
                packageClass = MoMoConfig.MOMO_APP_PAKAGE_CLASS_DEVELOPER;
                break;
            case MoMoConfig.ENVIRONMENT_PRODUCTION://environment production
                packageClass = MoMoConfig.MOMO_APP_PAKAGE_CLASS_PRODUCTION;
                break;
            default:
                packageClass = MoMoConfig.MOMO_APP_PAKAGE_CLASS_DEBUG;
                break;
        }
        do {
            if(!iterator.hasNext()) {
                if(!result) {
                    try {
                        activity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id="+packageClass)));
                    } catch (Exception var4) {
                        activity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://play.google.com/store/apps/details?id=" +MoMoConfig.MOMO_APP_PAKAGE_CLASS_PRODUCTION)));
                    }
                }
                return result;
            }
        } while(!((ApplicationInfo)iterator.next()).packageName.equals(packageClass));
        return true;
    }

    //todo action request
    public enum ACTION{
        MAP,
        PAYMENT
    }

    //todo enum choose environment
    public enum ENVIRONMENT{
        DEBUG,
        DEVELOPMENT,
        PRODUCTION
    }

    //todo enum action type
    public enum ACTION_TYPE{
        GET_TOKEN,
        LINK
    }
}
