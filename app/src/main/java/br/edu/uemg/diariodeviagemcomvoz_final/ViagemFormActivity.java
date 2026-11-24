package br.edu.uemg.diariodeviagemcomvoz_final;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.edu.uemg.diariodeviagemcomvoz_final.DAO.ViagemDAO;
import br.edu.uemg.diariodeviagemcomvoz_final.Model.ViagemModel;

public class ViagemFormActivity extends AppCompatActivity implements LocationListener, SensorEventListener {

    private EditText edtTitulo, edtData, edtObservacoes;
    private TextView txtGps, txtGiroscopio;
    private ImageView imgFoto;
    private Button btnGps, btnGiroscopio, btnFoto, btnGravarAudio, btnTocarAudio, btnSalvar;

    private ViagemModel viagemEditando;
    private ViagemDAO viagemDAO;

    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor gyroscope;

    private String latitudeStr = "";
    private String longitudeStr = "";
    private String giroInfoStr = "";
    private String fotoPathStr = "";
    private String audioPathStr = "";

    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private boolean isRecording = false;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viagem_form);

        edtTitulo = findViewById(R.id.edtTitulo);
        edtData = findViewById(R.id.edtData);
        edtObservacoes = findViewById(R.id.edtObservacoes);
        txtGps = findViewById(R.id.txtGps);
        txtGiroscopio = findViewById(R.id.txtGiroscopio);
        imgFoto = findViewById(R.id.imgFoto);
        btnGps = findViewById(R.id.btnGps);
        btnGiroscopio = findViewById(R.id.btnGiroscopio);
        btnFoto = findViewById(R.id.btnFoto);
        btnGravarAudio = findViewById(R.id.btnGravarAudio);
        btnTocarAudio = findViewById(R.id.btnTocarAudio);
        btnSalvar = findViewById(R.id.btnSalvar);

        // Preencher data atual se for novo
        String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        edtData.setText(currentDate);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (gyroscope == null) {
                gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
        }

        if (getIntent().hasExtra("selectViagem")) {
            viagemEditando = (ViagemModel) getIntent().getSerializableExtra("selectViagem");
            if (viagemEditando != null) {
                edtTitulo.setText(viagemEditando.getTitulo());
                edtData.setText(viagemEditando.getData());
                edtObservacoes.setText(viagemEditando.getObservacoes());
                latitudeStr = viagemEditando.getLatitude();
                longitudeStr = viagemEditando.getLongitude();
                txtGps.setText("GPS: " + latitudeStr + ", " + longitudeStr);
                giroInfoStr = viagemEditando.getGiroscopioInfo();
                txtGiroscopio.setText("Sensor/Giro: " + giroInfoStr);
                fotoPathStr = viagemEditando.getFotoPath();
                if (fotoPathStr != null && !fotoPathStr.isEmpty()) {
                    File imgFile = new File(fotoPathStr);
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        imgFoto.setImageBitmap(myBitmap);
                    }
                }
                audioPathStr = viagemEditando.getAudioPath();
                if (audioPathStr != null && !audioPathStr.isEmpty()) {
                    btnTocarAudio.setEnabled(true);
                    btnTocarAudio.setText("Reproduzir Áudio Gravado");
                }
                btnSalvar.setText("Atualizar Registro");
            }
        }

        btnGps.setOnClickListener(v -> solicitarPermissoesEObterGps());
        btnGiroscopio.setOnClickListener(v -> capturarGiroscopio());
        btnFoto.setOnClickListener(v -> capturarFoto());
        btnGravarAudio.setOnClickListener(v -> alternarGravacaoAudio());
        btnTocarAudio.setOnClickListener(v -> tocarAudio());

        btnSalvar.setOnClickListener(v -> salvarRegistro());

        verificarPermissoesGerais();
    }

    private void verificarPermissoesGerais() {
        String[] perms = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        boolean needsRequest = false;
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                needsRequest = true;
                break;
            }
        }
        if (needsRequest) {
            ActivityCompat.requestPermissions(this, perms, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void solicitarPermissoesEObterGps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSIONS);
            return;
        }
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean netEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled && !netEnabled) {
            Toast.makeText(this, "Por favor, ative o GPS/Internet.", Toast.LENGTH_SHORT).show();
            return;
        }
        Location loc = null;
        if (gpsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, this);
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (loc == null && netEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1f, this);
            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (loc != null) {
            latitudeStr = String.valueOf(loc.getLatitude());
            longitudeStr = String.valueOf(loc.getLongitude());
            txtGps.setText("GPS: " + latitudeStr + ", " + longitudeStr);
            Toast.makeText(this, "Localização obtida com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Aguardando sinal GPS...", Toast.LENGTH_SHORT).show();
        }
    }

    private void capturarGiroscopio() {
        if (sensorManager != null && gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(this, "Lendo sensor/giroscópio...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sensor/Giroscópio não disponível no dispositivo.", Toast.LENGTH_SHORT).show();
            giroInfoStr = "Não disponível";
            txtGiroscopio.setText("Giroscópio: Indisponível");
        }
    }

    private void capturarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao abrir câmera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgFoto.setImageBitmap(imageBitmap);
                try {
                    File photoFile = new File(getFilesDir(), "foto_" + System.currentTimeMillis() + ".jpg");
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(photoFile);
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();
                    fotoPathStr = photoFile.getAbsolutePath();
                    Toast.makeText(this, "Foto salva com sucesso!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Erro ao salvar foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void alternarGravacaoAudio() {
        if (!isRecording) {
            File audioFile = new File(getFilesDir(), "audio_" + System.currentTimeMillis() + ".3gp");
            audioPathStr = audioFile.getAbsolutePath();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioPathStr);

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
                btnGravarAudio.setText("Parar Gravação de Voz");
                Toast.makeText(this, "Gravando áudio...", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Erro ao gravar áudio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                btnGravarAudio.setText("Gravar Diário de Voz");
                btnTocarAudio.setEnabled(true);
                Toast.makeText(this, "Áudio gravado com sucesso!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Erro ao parar gravação: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void tocarAudio() {
        if (audioPathStr == null || audioPathStr.isEmpty()) {
            Toast.makeText(this, "Nenhum áudio gravado.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isPlaying) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioPathStr);
                mediaPlayer.prepare();
                mediaPlayer.start();
                isPlaying = true;
                btnTocarAudio.setText("Parar Reprodução");
                mediaPlayer.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    btnTocarAudio.setText("Reproduzir Áudio");
                });
                Toast.makeText(this, "Reproduzindo áudio...", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Erro ao reproduzir áudio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                } catch (Exception ignored) {}
            }
            isPlaying = false;
            btnTocarAudio.setText("Reproduzir Áudio");
        }
    }

    private void salvarRegistro() {
        String titulo = edtTitulo.getText().toString().trim();
        String data = edtData.getText().toString().trim();
        String obs = edtObservacoes.getText().toString().trim();

        if (titulo.isEmpty()) {
            edtTitulo.setError("Informe o título da viagem");
            edtTitulo.requestFocus();
            return;
        }

        ViagemModel viagem = new ViagemModel();
        viagem.setTitulo(titulo);
        viagem.setData(data);
        viagem.setObservacoes(obs);
        viagem.setLatitude(latitudeStr);
        viagem.setLongitude(longitudeStr);
        viagem.setFotoPath(fotoPathStr);
        viagem.setAudioPath(audioPathStr);
        viagem.setGiroscopioInfo(giroInfoStr);

        viagemDAO = new ViagemDAO(this);
        boolean sucesso;
        if (viagemEditando == null) {
            sucesso = viagemDAO.salvarViagem(viagem);
        } else {
            viagem.setId(viagemEditando.getId());
            sucesso = viagemDAO.alterarViagem(viagem);
        }
        viagemDAO.close();

        if (sucesso) {
            Toast.makeText(this, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao salvar: " + viagemDAO.msmErro, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitudeStr = String.valueOf(location.getLatitude());
        longitudeStr = String.valueOf(location.getLongitude());
        txtGps.setText("GPS: " + latitudeStr + ", " + longitudeStr);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE || event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            giroInfoStr = String.format(Locale.getDefault(), "X: %.2f, Y: %.2f, Z: %.2f", event.values[0], event.values[1], event.values[2]);
            txtGiroscopio.setText("Giro/Sensor: " + giroInfoStr);
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException ignored) {}
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception ignored) {}
            mediaRecorder = null;
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {}
            mediaPlayer = null;
        }
    }
}
