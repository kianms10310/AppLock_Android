package com.example.a8_716_05.myapplication;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.accessibility.AccessibilityManager;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView1;
    ArrayList<AppListItem> adapter;
    AppListAdapter ada;
    private PackageManager manager;
    private DataBaseManager dbManager = null;
    private Cursor cursor = null;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 111){ // MainActivity에서 FingerPrintActivity 실행
            if(resultCode!= RESULT_OK ){
                // 메인 Activity 인증 실패시
                Toast.makeText(this, "인증이 실패하였습니다.", Toast.LENGTH_SHORT).show();
                recreate();
            }
        }else if(requestCode==RESULT_CANCELED){ // MainActivity에서 접근성이나 지문 관련 Intent 실행
            if(requestCode!= RESULT_OK){
                recreate();
            }
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new ArrayList<AppListItem>();

        // 앱 권한 확인(Android 6.0이상부터 필수)
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.USE_FINGERPRINT);

        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(Context.FINGERPRINT_SERVICE);

        // 접근성 설정이 되어있지 않을 때
        if(!isContainedInAccessbility(this)) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, RESULT_CANCELED);
            Toast.makeText(this, "앱이 정상 작동하기 위해서는 접근성 활성화가 필요합니다. \n 환경설정 → 접근성 → 서비스 → AppLocker 사용함",Toast.LENGTH_LONG).show();
        }else if(permissionCheck != PackageManager.PERMISSION_GRANTED) {     // 권한이 허용 되어있지 않을 때
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.USE_FINGERPRINT)) {
                Toast.makeText(this, "앱이 정상 작동하기 위해서는 지문 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, 1);
            }
        }else if(!fingerprintManager.isHardwareDetected()){ // 지문 센서가 존재 하지 않는다면
            // Device doesn’t support fingerprint authentication
            Toast.makeText(this, "앱을 사용하기 위해서는 지문센서가 필요합니다.", Toast.LENGTH_SHORT).show();
        }else if (!fingerprintManager.hasEnrolledFingerprints()) {  // 지문이 1개 이상 등록되어 있지 않다면
            // User hasn’t enrolled any fingerprints to authenticate with
            Toast.makeText(this, "앱을 사용하기 위해서는 1개이상의 지문등록이 필요합니다.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            startActivityForResult(intent, RESULT_CANCELED);
        }else{  // 모든 조건에 만족한다면
            // Everything is ready for fingerprint authentication
            Intent intents = new Intent(MainActivity.this, FingerPrintActivity.class);
            intents.putExtra("PACKAGENAME", "com.example.a8_716_05.myapplication");

            startActivityForResult(intents, 111);

            // ListView 연결
            listView1 = (ListView)findViewById(R.id.listView1);

            // 기기 내에 설치된 AppList 가져오기
            manager = getApplicationContext().getPackageManager();

            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> groupApps = manager.queryIntentActivities(intent, 0);

            // DataBase 연결
            dbManager = new DataBaseManager(getApplicationContext());
            dbManager.open();
            ContentValues recordValues = new ContentValues();

            for (int i = 0; i < groupApps.size(); i++) {
                ResolveInfo resolveInfo = groupApps.get(i);
                // 패키지명 가져오기

                String packageName = resolveInfo.activityInfo.applicationInfo.packageName;

                // DataBase에 자료가 없는 경우에 추가
                cursor = dbManager.executeOneQuery(packageName);
                if(cursor.getCount() == 0){
                    // _id = 패키지명
                    // en : 활성화 여부
                    // ch : 인증 여부
                    recordValues.put("_id",packageName);
                    recordValues.put("en", 0);
                    recordValues.put("ch", 0);
                    dbManager.insertRecord(recordValues);
                }
                String appName = null;
                try {
                    // 패키지명을 이용하여 앱 이름 가져오기
                    appName = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES)).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                // 아이콘 가져오기
                Drawable icon = resolveInfo.activityInfo.applicationInfo.loadIcon(manager);

                // 현재 Enable 가져오기
                boolean enable = false;
                cursor = dbManager.executeOneQuery(packageName);

                // package 명으로 정보를 가져와 enable 0 = 비활성 / 1 = 활성화
                if(cursor!=null){
                    cursor.moveToNext();
                    int as = cursor.getInt(1);
                    switch (as){
                        case 0:
                            enable = false;
                            break;
                        case 1:
                            enable = true;
                            break;
                    }
                }
                // Adpater에 추가
                adapter.add(new AppListItem(icon, packageName, appName, enable));
            }

            // ListView에 추가
            ada = new AppListAdapter(this, R.layout.packlistitem, adapter);
            listView1.setAdapter(ada);
            dbManager.close();
        }
    }

    // 접근성 허용 여부 경우 창을 띄어보여줌.
    public static boolean isContainedInAccessbility(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceList = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        return serviceList.toString().contains(context.getPackageName());
    }

    // 권한 승인 문제 사용자의 답변 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "사용자가 지문 권한 승인",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this, "사용자가 지문 권한 거부",Toast.LENGTH_LONG).show();
                }
        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

}
