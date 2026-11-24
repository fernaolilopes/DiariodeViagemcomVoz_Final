package br.edu.uemg.diariodeviagemcomvoz_final.DAO;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import br.edu.uemg.diariodeviagemcomvoz_final.Model.ViagemModel;

public class ViagemDAO extends SQLiteOpenHelper {
    private static final String DATABASE = "diario_viagens.db";
    private static final int VERSION = 1;
    public String msmErro = "";

    public ViagemDAO(Context context) {
        super(context, DATABASE, null, VERSION);
        msmErro = "";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE viagens (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "titulo TEXT NOT NULL, " +
                "data TEXT NOT NULL, " +
                "observacoes TEXT, " +
                "latitude TEXT, " +
                "longitude TEXT, " +
                "fotoPath TEXT, " +
                "audioPath TEXT, " +
                "giroscopioInfo TEXT);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS viagens");
            onCreate(db);
        }
    }

    public boolean salvarViagem(ViagemModel viagem) {
        try {
            ContentValues values = new ContentValues();
            values.put("titulo", viagem.getTitulo());
            values.put("data", viagem.getData());
            values.put("observacoes", viagem.getObservacoes());
            values.put("latitude", viagem.getLatitude());
            values.put("longitude", viagem.getLongitude());
            values.put("fotoPath", viagem.getFotoPath());
            values.put("audioPath", viagem.getAudioPath());
            values.put("giroscopioInfo", viagem.getGiroscopioInfo());
            getWritableDatabase().insert("viagens", null, values);
            return true;
        } catch (Exception e) {
            msmErro = e.getMessage();
            return false;
        }
    }

    public boolean alterarViagem(ViagemModel viagem) {
        try {
            ContentValues values = new ContentValues();
            values.put("titulo", viagem.getTitulo());
            values.put("data", viagem.getData());
            values.put("observacoes", viagem.getObservacoes());
            values.put("latitude", viagem.getLatitude());
            values.put("longitude", viagem.getLongitude());
            values.put("fotoPath", viagem.getFotoPath());
            values.put("audioPath", viagem.getAudioPath());
            values.put("giroscopioInfo", viagem.getGiroscopioInfo());
            String[] args = {viagem.getId().toString()};
            getWritableDatabase().update("viagens", values, "id=?", args);
            return true;
        } catch (Exception e) {
            msmErro = e.getMessage();
            return false;
        }
    }

    public boolean deletarViagem(ViagemModel viagem) {
        try {
            String[] args = {viagem.getId().toString()};
            getWritableDatabase().delete("viagens", "id=?", args);
            return true;
        } catch (Exception e) {
            msmErro = e.getMessage();
            return false;
        }
    }

    public ArrayList<ViagemModel> listarViagens() {
        String[] colunas = {"id", "titulo", "data", "observacoes", "latitude", "longitude", "fotoPath", "audioPath", "giroscopioInfo"};
        Cursor cursor = getWritableDatabase().query("viagens", colunas, null, null, null, null, "id DESC");
        ArrayList<ViagemModel> resposta = new ArrayList<>();
        while (cursor.moveToNext()) {
            ViagemModel v = new ViagemModel();
            v.setId(cursor.getLong(0));
            v.setTitulo(cursor.getString(1));
            v.setData(cursor.getString(2));
            v.setObservacoes(cursor.getString(3));
            v.setLatitude(cursor.getString(4));
            v.setLongitude(cursor.getString(5));
            v.setFotoPath(cursor.getString(6));
            v.setAudioPath(cursor.getString(7));
            v.setGiroscopioInfo(cursor.getString(8));
            resposta.add(v);
        }
        cursor.close();
        return resposta;
    }
}
