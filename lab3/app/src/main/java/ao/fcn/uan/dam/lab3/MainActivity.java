package ao.fcn.uan.dam.lab3;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Tag para filtrar as mensagens no LogCat [cite: 408]
    private static final String TAG = "Lab03_Contador";

    private Thread workerThread;
    private int contador = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referência aos botões da interface [cite: 406]
        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        // Configuração do botão Start [cite: 406]
        btnStart.setOnClickListener(v -> iniciarContagem());

        // Configuração do botão Stop [cite: 407]
        btnStop.setOnClickListener(v -> pararContagem());
    }

    private void iniciarContagem() {
        // Garantir que não existam threads duplicadas antes de iniciar
        pararContagem();

        // O botão "Start" deve zerar o contador [cite: 406]
        contador = 0;

        // Criação da worker thread para manter o contador em segundo plano [cite: 403]
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Ciclo do worker thread: continua até ser interrompido
                    while (!Thread.currentThread().isInterrupted()) {

                        // Imprime o valor no LogCat
                        Log.d(TAG, "Valor do contador: " + contador);

                        contador++; // Incrementa a cada segundo [cite: 404]

                        // Bloqueia a thread por 1 segundo (1000ms)
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    // Se ocorrer uma interrupção enquanto a thread dorme
                    Log.d(TAG, "A worker thread foi interrompida.");
                }
            }
        });

        // Inicia a execução da thread [cite: 295]
        workerThread.start();
    }

    private void pararContagem() {
        // Invocamos o método interrupt() para parar a thread com segurança [cite: 410, 414]
        if (workerThread != null && workerThread.isAlive()) {
            workerThread.interrupt();
            workerThread = null;
        }
    }
}