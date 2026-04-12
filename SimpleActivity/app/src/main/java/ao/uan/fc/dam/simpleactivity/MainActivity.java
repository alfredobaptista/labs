package ao.uan.fc.dam.simpleactivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       EdgeToEdge.enable(this);
       setContentView(R.layout.activity_main);

       Log.i(TAG, "onCreate");
   }

   @Override
   public void onStart() {
       super.onStart();
       Log.i(TAG, "onStart");
   }

   @Override
   public void onResume() {
       super.onResume();
       Log.i(TAG, "onResume");
       Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
   }

   @Override
   public void onPause() {
       super.onPause();
       Log.i(TAG, "onPause");
   }

   @Override
   public void onStop() {
       super.onStop();
       Log.i(TAG, "onStop");
   }

   @Override
   public void onDestroy() {
       super.onDestroy();
       Log.i(TAG, "onDestroy");
   }

   @Override
   public void onRestart() {
       super.onRestart();
       Log.i(TAG, "onRestart");
   }

    public void finishButtonPressed(View view) {
        finish();
    }
}