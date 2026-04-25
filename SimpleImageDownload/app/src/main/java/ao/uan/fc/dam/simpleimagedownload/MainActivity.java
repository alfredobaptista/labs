package ao.uan.fc.dam.simpleimagedownload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements Handler.Callback {
    public static final String KEY_HANDLER_MSG = "status";
    private static final String IMAGE_SOURCE = "https://picsum.photos/300/300";

    private Button btnDownloadFile;
    private Button btnDownloadFileAsync;
    private TextView statusTextView;
    private ImageView imageView;
    private Handler handler = new Handler(Looper.getMainLooper(), this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDownloadFile = (Button) findViewById(R.id.btnDownloadFile);
        btnDownloadFileAsync = (Button) findViewById(R.id.btnDownloadFileAsync);
        imageView = (ImageView) findViewById(R.id.image_view);
        statusTextView = (TextView) findViewById(R.id.status);

        // Exercício 2: Thread + Handler
        btnDownloadFile.setOnClickListener(v -> {
            new Thread(() -> downloadImage(IMAGE_SOURCE), "Download thread").start();
            statusTextView.setText(getString(R.string.download_started));
        });

        // Exercício 3: AsyncTask (Seguindo o esqueleto da página 6)
        btnDownloadFileAsync.setOnClickListener(v -> {
            // Passamos as views para o construtor como pede o enunciado
            new DownloadTask(imageView, statusTextView).execute(IMAGE_SOURCE);
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        String text = msg.getData().getString(KEY_HANDLER_MSG);
        statusTextView.setText(text);
        return true;
    }

    private void sendMessage(String what) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_HANDLER_MSG, what);
        Message message = new Message();
        message.setData(bundle);
        handler.sendMessage(message);
    }

    private void downloadImage(String urlStr){
        try {
            URL imageUrl = new URL(urlStr);
            Bitmap image = BitmapFactory.decodeStream(imageUrl.openStream());
            if (image != null) {
                sendMessage(getString(R.string.download_success));
                runOnUiThread(() -> imageView.setImageBitmap(image));
            } else {
                sendMessage(getString(R.string.download_failed_stream));
            }
        } catch (Exception e) {
            sendMessage(getString(R.string.download_failed));
        }
    }

    // --- ASYNCTASK CONFORME O ESQUELETO DO GUIÃO ---
    private class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imgView;
        private TextView txtStatus;

        // Construtor solicitado no enunciado [página 6]
        public DownloadTask(ImageView imageView, TextView statusText) {
            this.imgView = imageView;
            this.txtStatus = statusText;
        }

        @Override
        protected void onPreExecute() {
            // Usa a referência passada pelo construtor
            txtStatus.setText(getString(R.string.download_started));
        }

        @Override
        protected Bitmap doInBackground(String... inputUrls) {
            try {
                URL url = new URL(inputUrls[0]);
                return BitmapFactory.decodeStream(url.openStream());
            } catch (Exception e) {
                Log.e("DownloadTask", "Erro no download", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                txtStatus.setText(getString(R.string.download_success));
                // Conforme o enunciado: utilize o método setImageBitmap()
                imgView.setImageBitmap(result);
            } else {
                txtStatus.setText(getString(R.string.download_failed));
            }
        }
    }
}