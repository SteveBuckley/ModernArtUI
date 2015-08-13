package uk.org.stevebuckley.modernartui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 *
 * The purpose of this program is to convert any non-white square in the UI into its complimentary
 * colour.
 *
 * The getColours() routine gets the background colour of each appropriate layout
 * and stores it in the 'colour' array. These colours are then adjusted by the
 * adjustColours() routine by a value from 0 t0 255 provided by the OnSeekBarChangeListener()
 * routine.
 *
 * The whole UI is stored as three fragments containing 15 coloured rectangles.
 * Only one fragment can be on screen in portrait mode but all three are available in
 * landscape.
 *
 * The 'more information' menu item calls openWebDialogue() that creates a dialogue box inviting
 * the user to visit the Mondrian web page at the Museum of Modern Art in New York.
 */


public class MainActivity extends AppCompatActivity {

    private static final String TAG="MainActivity";
    public static String packageName="";

    int numberOfRectangles=20; // Maximum number of rectangles to look for
    int[][] colour = new int[numberOfRectangles][3];  // Stores the RGB of any named rectangle

    AlertDialog alertDialogue = null;
    Boolean dialogueDisplayed=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageName = getApplicationContext().getPackageName();
        setContentView(R.layout.main);

        getColours();   // Obtain the original colours of the rectangles

        SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar1);
        seekbar.setMax(255);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onProgressChanged(SeekBar arg0,
                                          int progress,
                                          boolean arg2) {
                /*
                When the seekbar is moved adjust the colours by the
                amount moved.
                 */
                adjustColours(progress);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
        Deals with the 'more information' menu option
         */
        int id = item.getItemId();
        if (id == R.id.more_info) {
            openWebDialogue();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*
        Stop potential window leak
        */
        Log.d(TAG,"onStop()");
        if (alertDialogue != null) {
            alertDialogue.dismiss();
            alertDialogue = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
        Stop potential window leak
         */
        Log.d(TAG, "onDestroy()");
        if (alertDialogue != null) {
            alertDialogue.dismiss();
            alertDialogue = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        /*
        This routine will record whether or not the dialogue box
        has been called before the view was destroyed by, say, an
        orientation change
         */
        super.onSaveInstanceState(savedInstanceState);
        if (alertDialogue != null) {
            savedInstanceState.putBoolean("DIALOGUE_IN_USE", true);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        /*
        If the dialogue box was open prior to the view being redrawn
        this will recreate it.
         */
        super.onRestoreInstanceState(savedInstanceState);
        dialogueDisplayed = savedInstanceState.getBoolean("DIALOGUE_IN_USE");
        if (dialogueDisplayed){
            openWebDialogue();
        }
    }


    public void getColours() {
        /**
         * Hunts around the fragments for the layouts called 'rectangle[1-xx]'
         * and puts the RGB components of their colours into the colour array
         */
        LinearLayout rectLayout;
        int[] newColourComponent = new int[3];  //RGB Components of circle colour
        String rectangle="";
        int newColour = -1;

        for (int i=1; i <= numberOfRectangles; i++) {
            rectangle = "rectangle" + i;
            int resID = getResources().getIdentifier(rectangle, "id", MainActivity.packageName);
            rectLayout = (LinearLayout) findViewById(resID);

            if (rectLayout == null) {                   // Assume that if a layout does
                numberOfRectangles = i-1;               // not exist, then this is the end
                break;                                  // of the list
            }
            /*
            Obtain the background colour and split into the RGB components
            This is a bit messy as there is no equivalent to View.getBackgroundColor()
            Will only work from API 11 upwards.
             */
            Drawable background = rectLayout.getBackground();
            if (background instanceof ColorDrawable) {
                newColour = ((ColorDrawable) background).getColor();
                colour[i][0] = (newColour >> 16) & 0xff;   // Red Component
                colour[i][1] = (newColour >> 8) & 0xff;    // Green Component
                colour[i][2] = newColour & 0xff;           // Blue Component
            }else{
                colour[i][1] = -1;      // flag to ignore this colour
            }
            /*
            If the colour is grey - ignore it.
            Since there are so many shades of grey (at least 50 according to popular fiction)
            ignore any colour where R G and B are the same value.
             */
            if ((colour[i][0]==colour[i][1]) &&
                (colour[i][1])==colour[i][2]){
                colour[i][0] = -1;
            }
        }
    }


    public void adjustColours(int progress){
        /**
         *  Gradually adjusts the colours in the non-white rectangles
         *  to its opposite colour (e.g. green->magenta, red->cyan)
         *  depending on the value returned by the seekbar.
         *
         * @param progress Integer from 0 to 255 returned by seekbar.
         */

        LinearLayout rectLayout;
        int[] newColourComponent = new int[3];  //RGB Components of circle colour
        int newColour = 0;
        String rectangle="";
        /*
        Loop around the rectangles in turn
         */
        for (int i=1; i <= numberOfRectangles; i++){
            if (colour[i][0]==-1){
                continue;    // Ignore this colour
            }
            rectangle = "rectangle" + i;
            int resID = getResources().getIdentifier(rectangle, "id", MainActivity.packageName);
            rectLayout=(LinearLayout) findViewById(resID);
            if (rectLayout==null){
                Log.e(TAG,"Rectangle"+i+" missing!");  // Should never get here
                break;
            }

            /*
            Adjust each RGB component by the value of the seekbar position.
            The abs function will ensure that low values such as 002 get converted
            to high values like 253.
             */

            for (int j=0;j<3;j++){
                newColourComponent[j]=Math.abs(colour[i][j]-progress);
            }
            /*
            Create the new colour as one integer from the RGB components.
            The 255 is the alpha component without which, for some reason, setBackgroundColor()
            will not work. Each component is left shifted into its rightful
            place and merged with the other components.
             */
            newColour = (255 << 24) |
                    (newColourComponent[0] << 16 ) |
                    (newColourComponent[1]<<8) |
                    (newColourComponent[2]);
            rectLayout.setBackgroundColor(newColour);
        }
    }
    private void openWebDialogue(){
        /**
         * Opens a dialogue box to give the user the opportunity to
         * visit the MoMA web site
         */
        Boolean reply=false;
        LayoutInflater inflater = (LayoutInflater) getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View message = inflater.inflate(R.layout.dialogue_layout,null);
        String yesButton = getString(R.string.go_to_moma);
        String noButton = getString(R.string.cancel);
        final String url = getString(R.string.mond_url);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.more_info);
        builder
                .setView(message)
                .setCancelable(false)
                .setPositiveButton(yesButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        dialog.cancel();
                        alertDialogue=null;
                    }
                })
                .setNegativeButton(noButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        alertDialogue=null;
                    }
                });
        alertDialogue = builder.create();
        alertDialogue.show();
    }
}
