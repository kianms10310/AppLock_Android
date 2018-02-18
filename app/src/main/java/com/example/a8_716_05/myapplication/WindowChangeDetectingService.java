package com.example.a8_716_05.myapplication;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by Shin on 2016-11-16.
 */

public class WindowChangeDetectingService extends AccessibilityService {

    private String prePackage="";
    BroadcastReceiver broadcastReceiver;
    private static final int WIN = 0;
    public static final String TAG = "WindowChange";
    private DataBaseManager dbManager = null;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
        String packageName = null;
        try{
            // 발생한 Event의 패키지명을 가져옴
            packageName = (String) event.getPackageName();
        }catch (NullPointerException e){

        }

        switch (event.getEventType()){
            // Event 타입이 TYPE_WINDOW_STATE_CHANGED 일 경우
            case  AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                dbManager = new DataBaseManager(this);
                dbManager.open();

                // DataBase에서 모든 Record 가져옴
                Cursor cursor = dbManager.executeOneQuery(packageName);
                if(cursor != null){
                    // 값이 있는 경우에만 실행
                    if(cursor.getCount()!=0){
                        cursor.moveToNext();
                        // en => Lock 여부 0 : 비활성화 / 1 : 활성화
                        // ch => 인증 여부 0 : 비인증 / 1 : 인증완료
                        // 인증이 완료된 패키지는 다시 인증할 필요가 없음.
                        if(cursor.getInt(1) == 1 && cursor.getInt(2) == 0){
                            // 인증 Activity 실행
                            Intent intent = new Intent(getApplicationContext(),FingerPrintActivity.class);
                            // 패키지명을 보냄.
                            intent.putExtra("PACKAGENAME", packageName);
                            startActivity(intent);
                            Log.i(TAG, ""+ packageName + " / TYPE_WINDOW_STATE_CHANGED" );
                            prePackage = packageName;
                        }
                    }

                }
                dbManager.close();
                break;
            default:
                //Log.i(TAG, ""+ packageName  + " / " + event.getEventType() );
                break;

        }

    }
    @Override
    public void onInterrupt()
    {
        Log.v(TAG, "onInterrupt");
        // Interrupt 걸리면 Broadcast 종료
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onServiceConnected()
    {
        super.onServiceConnected();
        Log.d(TAG, "service connect"); // 접근성이 허용되면 Log 남김
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // 모든 작업을 event로 처리
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.notificationTimeout = 0;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.packageNames = null;

        // BroadCast 실행, Android 6.0부터 Filter를 걸어줘야 실행가능
        IntentFilter intentFilter = new IntentFilter();
        // Filter 내용은 스크린이 꺼지는 것을 감지
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // SCREEN_OFF 시
                if(intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                    Log.d(TAG, "SCREEN_OFF");
                    dbManager = new DataBaseManager(context);
                    dbManager.open();
                    dbManager.allUpdateRecord(); // 모든 DataBase record들의 ch 값을 0으로 초기화(화면꺼짐을 기점으로 인증 다시 하기 때문)
                    dbManager.close();
                }
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
        setServiceInfo(info);
    }

}
