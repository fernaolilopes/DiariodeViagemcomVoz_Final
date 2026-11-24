package br.edu.uemg.diariodeviagemcomvoz_final;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import br.edu.uemg.diariodeviagemcomvoz_final.DAO.ViagemDAO;
import br.edu.uemg.diariodeviagemcomvoz_final.Model.ViagemModel;

public class MainActivity extends AppCompatActivity {

    ListView lstViagens;
    Button btnNovo;
    ViagemDAO viagemDAO;
    ArrayList<ViagemModel> listaViagens;
    ViagemModel viagemSelecionada;
    ArrayAdapter<ViagemModel> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lstViagens = findViewById(R.id.lstViagens);
        btnNovo = findViewById(R.id.btnNovo);

        btnNovo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViagemFormActivity.class);
            startActivity(intent);
        });

        lstViagens.setOnItemClickListener((adapterView, view, position, id) -> {
            ViagemModel selectViagem = (ViagemModel) adapterView.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, ViagemFormActivity.class);
            intent.putExtra("selectViagem", selectViagem);
            startActivity(intent);
        });

        registerForContextMenu(lstViagens);
    }

    public void carregarLista() {
        viagemDAO = new ViagemDAO(MainActivity.this);
        listaViagens = viagemDAO.listarViagens();
        viagemDAO.close();
        if (listaViagens != null) {
            adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listaViagens);
            lstViagens.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        viagemSelecionada = listaViagens.get(info.position);

        MenuItem menuDelete = menu.add("Deletar registro");
        menuDelete.setOnMenuItemClickListener(item -> {
            viagemDAO = new ViagemDAO(MainActivity.this);
            boolean sucesso = viagemDAO.deletarViagem(viagemSelecionada);
            viagemDAO.close();
            if (sucesso) {
                Toast.makeText(MainActivity.this, "Registro deletado com sucesso!", Toast.LENGTH_SHORT).show();
                carregarLista();
            } else {
                Toast.makeText(MainActivity.this, "Erro ao deletar registro.", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }
}
