package com.skysam.hchirinos.myfinances.principal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.skysam.hchirinos.myfinances.R;
import com.skysam.hchirinos.myfinances.agregar.AgregarActivity;

public class HomeActivity extends AppCompatActivity {

    private BottomSheetDialog bottomSheetDialog;
    private HomeFragment homeFragment;
    private IngresosFragment ingresosFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomAppBar bottomAppBar = findViewById(R.id.bottomAppBar);

        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirBottomDrawer();
            }
        });

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                escogerOpcionAgregar();
            }
        });

        homeFragment = new HomeFragment();
        ingresosFragment = new IngresosFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.container_fragments, homeFragment, "home").commit();
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
                        bottomSheetDialog.dismiss();
                        break;
                    case R.id.menu_ingresos:
                        getSupportFragmentManager().beginTransaction().replace(R.id.container_fragments, ingresosFragment, "ingresos").commit();
                        bottomSheetDialog.dismiss();
                        break;
                    case R.id.menu_ahorros:
                        Toast.makeText(HomeActivity.this,"Item 2 Clicked",Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        break;
                    case R.id.menu_prestamos:
                        Toast.makeText(HomeActivity.this,"Item 3 Clicked",Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        break;
                    case R.id.menu_egresos:
                        Toast.makeText(HomeActivity.this,"Item 4 Clicked",Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                        break;
                    case R.id.menu_deudas:
                        Toast.makeText(HomeActivity.this,"Item 5 Clicked",Toast.LENGTH_SHORT).show();
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
}
