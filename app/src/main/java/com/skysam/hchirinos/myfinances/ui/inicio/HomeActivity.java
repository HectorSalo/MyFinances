package com.skysam.hchirinos.myfinances.ui.inicio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.Utils.Constantes;
import com.skysam.hchirinos.myfinances.ui.general.CalculadoraActivity;
import com.skysam.hchirinos.myfinances.ui.ajustes.SettingsActivity;
import com.skysam.hchirinos.myfinances.ui.agregar.AgregarActivity;
import com.skysam.hchirinos.myfinances.ui.general.ListaGastosActivity;

public class HomeActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener, View.OnClickListener {

    private BottomSheetDialog bottomSheetDialog;
    private HomeFragment homeFragment;
    private IngresosFragment ingresosFragment;
    private AhorrosFragment ahorrosFragment;
    private PrestamosFragment prestamosFragment;
    private GastosFragment gastosFragment;
    private DeudasFragment deudasFragment;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton floatingActionButton;
    private int agregar;
    private MenuItem itemBuscar;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences(user.getUid(), Context.MODE_PRIVATE);

        String tema = sharedPreferences.getString(Constantes.PREFERENCE_TEMA, Constantes.PREFERENCE_TEMA_SISTEMA);

        switch (tema){
            case Constantes.PREFERENCE_TEMA_SISTEMA:
                setTheme(R.style.AppTheme);
                break;
            case Constantes.PREFERENCE_TEMA_OSCURO:
                setTheme(R.style.AppThemeNight);
                break;
            case Constantes.PREFERENCE_TEMA_CLARO:
                setTheme(R.style.AppThemeDay);
                break;
        }
        setContentView(R.layout.activity_main);

        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);

        agregar = 0;

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AgregarActivity.class);
                switch (agregar) {
                    case 0:
                        escogerOpcionAgregar();
                        break;
                    case 1:
                        intent.putExtra("agregar", 0);
                        startActivity(intent);
                        break;
                    case 2:
                        intent.putExtra("agregar", 1);
                        startActivity(intent);
                        break;
                    case 3:
                        intent.putExtra("agregar", 2);
                        startActivity(intent);
                        break;
                    case 4:
                        intent.putExtra("agregar", 3);
                        startActivity(intent);
                        break;
                    case 5:
                        intent.putExtra("agregar", 4);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });

        homeFragment = new HomeFragment();
        ingresosFragment = new IngresosFragment();
        ahorrosFragment = new AhorrosFragment();
        prestamosFragment = new PrestamosFragment();
        gastosFragment = new GastosFragment();
        deudasFragment = new DeudasFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.container_fragments, homeFragment, "home").commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_options_menu, menu);
        itemBuscar = menu.findItem(R.id.menu_buscar);
        itemBuscar.setVisible(false);
        searchView = (SearchView) itemBuscar.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.setOnSearchClickListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                abrirBottomDrawer();
                break;
            case R.id.menu_ajustes:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.menu_calculadora:
                startActivity(new Intent(this, CalculadoraActivity.class));
                break;
            case R.id.menu_lista_gastos:
                startActivity(new Intent(this, ListaGastosActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void abrirBottomDrawer() {
        final View bootomNavigation = getLayoutInflater().inflate(R.layout.navigation_menu,null);
        bottomSheetDialog = new BottomSheetDialog(HomeActivity.this);
        bottomSheetDialog.setContentView(bootomNavigation);
        bottomSheetDialog.show();

        //this will find NavigationView from id
        NavigationView navigationView = bootomNavigation.findViewById(R.id.navigation_menu);
        navigationView.setItemIconTintList(null);

        //This will handle the onClick Action for the menu item
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id){
                    case R.id.menu_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container_fragments, homeFragment, "home").commit();
                        bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER);
                        floatingActionButton.setImageResource(R.drawable.ic_add_36dp);
                        itemBuscar.setVisible(false);
                        agregar = 0;
                        bottomSheetDialog.dismiss();
                        break;
                    case R.id.menu_ingresos:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container_fragments, ingresosFragment, "ingresos").commit();
                        bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
                        floatingActionButton.setImageResource(R.drawable.ic_add_ingreso_gastos_24dp);
                        itemBuscar.setVisible(true);
                        searchView.setIconified(true);
                        agregar = 1;
                        bottomSheetDialog.dismiss();
                        break;
                    case R.id.menu_ahorros:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container_fragments, ahorrosFragment, "ahorros").commit();
                        bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
                        floatingActionButton.setImageResource(R.drawable.ic_add_ahorros_deudas_24dp);
                        itemBuscar.setVisible(true);
                        searchView.setIconified(true);
                        agregar = 2;
                        bottomSheetDialog.dismiss();
                        break;
                    case R.id.menu_prestamos:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container_fragments, prestamosFragment, "prestamos").commit();
                        bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
                        floatingActionButton.setImageResource(R.drawable.ic_add_prestamo_24dp);
                        itemBuscar.setVisible(true);
                        searchView.setIconified(true);
                        agregar = 3;
                        bottomSheetDialog.dismiss();
                        break;
                    case R.id.menu_egresos:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container_fragments, gastosFragment, "gastos").commit();
                        bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
                        floatingActionButton.setImageResource(R.drawable.ic_add_ingreso_gastos_24dp);
                        itemBuscar.setVisible(true);
                        searchView.setIconified(true);
                        agregar = 4;
                        bottomSheetDialog.dismiss();
                        break;
                    case R.id.menu_deudas:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container_fragments, deudasFragment, "deudas").commit();
                        bottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
                        floatingActionButton.setImageResource(R.drawable.ic_add_ahorros_deudas_24dp);
                        itemBuscar.setVisible(true);
                        searchView.setIconified(true);
                        agregar = 5;
                        bottomSheetDialog.dismiss();
                        break;
                }
                return false;
            }
        });
    }

    private void escogerOpcionAgregar() {
        final String[] items = {getResources().getString(R.string.menu_ingresos), getResources().getString(R.string.menu_ahorros), getResources().getString(R.string.menu_prestamos),
                getResources().getString(R.string.menu_egresos), getResources().getString(R.string.menu_deudas)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Qué desea agregar?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), AgregarActivity.class);
                switch (which) {
                    case 0:
                        intent.putExtra("agregar", 0);
                        break;
                    case 1:
                        intent.putExtra("agregar", 1);
                        break;
                    case 2:
                        intent.putExtra("agregar", 2);
                        break;
                    case 3:
                        intent.putExtra("agregar", 3);
                        break;
                    case 4:
                        intent.putExtra("agregar", 4);
                        break;
                    default:
                        break;
                }
                startActivity(intent);
            }
        }).show();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        switch (agregar) {
            case 1:
                ingresosFragment.buscarItem(newText);
                break;
            case 2:
                ahorrosFragment.buscarItem(newText);
                break;
            case 3:
                prestamosFragment.buscarItem(newText);
                break;
            case 4:
                gastosFragment.buscarItem(newText);
                break;
            case 5:
                deudasFragment.buscarItem(newText);
                break;
        }
        return false;
    }

    @Override
    public boolean onClose() {
        floatingActionButton.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public void onClick(View view) {
        floatingActionButton.setVisibility(View.GONE);
    }
}
