package com.mie.rrobles.tracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

public class welcome extends Activity implements OnClickListener{
	
	
	/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState) {
    
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.welcome_layout);
    
    View boton = findViewById(R.id.button1);
    boton.setOnClickListener(this);
    
    View boton2 = findViewById(R.id.button2);
    boton2.setOnClickListener(this);
    
    View boton3 = findViewById(R.id.button3);
    boton3.setOnClickListener(this);
    
    View boton4 = findViewById(R.id.button4);
    boton4.setOnClickListener(this);
    
    View boton5 = findViewById(R.id.button5);
    boton5.setOnClickListener(this);
    
    Toast.makeText(this, "Bienvenido, elige una opción", Toast.LENGTH_SHORT).show();

	}

public void onClick(View vista) {

	if(vista.getId() == findViewById(R.id.button1).getId()) 
	{		
		Intent i = new Intent(this,camera.class);
		startActivity(i);
		finish();
	}
	if(vista.getId() == findViewById(R.id.button2).getId()) 
	{
		Intent i = new Intent(this,image.class);
		startActivity(i);
		finish();		
	}	
	if(vista.getId() == findViewById(R.id.button3).getId()) 
	{
//		Intent i = new Intent(this,homography.class);
		Intent i = new Intent(this,camera_homography.class);
		startActivity(i);
		finish();		
	}	
	
	if(vista.getId() == findViewById(R.id.button4).getId()) 
	{		
		Intent i = new Intent(this,camera_canny.class);
		startActivity(i);
		finish();
	}
	
	if(vista.getId() == findViewById(R.id.button5).getId()) 
	{		
		finish();
	}	
	
 }
}
