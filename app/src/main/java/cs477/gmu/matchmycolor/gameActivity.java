package cs477.gmu.matchmycolor;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class gameActivity extends AppCompatActivity {
    private int playerTurn;
    private int imageTurn;
    private TextView player1Score;
    private TextView player2Score;
    private TextView player1Turn;
    private TextView player2Turn;
    private ImageView image1;
    private ImageView image2;
    private Bitmap image1Bp;
    private Bitmap image2Bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Randomly pick player that goes first
        Random rand = new Random();
        playerTurn = rand.nextInt(2) + 1;

        // Initialize variables
        imageTurn = 1;
        player1Score = findViewById(R.id.player1_score);
        player2Score = findViewById(R.id.player2_score);
        player1Turn = findViewById(R.id.player1_turn);
        player2Turn = findViewById(R.id.player2_turn);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);

        // Set player pointer
        if (playerTurn == 1) {
            player2Turn.setVisibility(View.INVISIBLE);
        } else {
            player1Turn.setVisibility(View.INVISIBLE);
        }
    }

    public void cameraButton(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Toast.makeText(getApplicationContext(),"Opening camera",Toast.LENGTH_SHORT).show();
        try {

            myActivityResultLauncher.launch(takePictureIntent);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(),"failed",Toast.LENGTH_SHORT).show();
            // display error state to the user
        }
    }

    ActivityResultLauncher<Intent>
            myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Bitmap bp = (Bitmap) data.getExtras().get("data");

                        // Display image on screen and switch player's turn
                        if (playerTurn == 1) {
                            if (imageTurn == 1) {
                                image1.setImageBitmap(bp);
                                image1Bp = bp;
                                imageTurn = 2;
                                playerTurn = 2;
                                player1Turn.setVisibility(View.INVISIBLE);
                                player2Turn.setVisibility(View.VISIBLE);
                            } else {
                                image2.setImageBitmap(bp);
                                image2Bp = bp;
                                imageTurn = 1;
                                calculatePoints();
                            }
                        } else {
                            if (imageTurn == 1) {
                                image1.setImageBitmap(bp);
                                image1Bp = bp;
                                imageTurn = 2;
                                playerTurn = 1;
                                player1Turn.setVisibility(View.VISIBLE);
                                player2Turn.setVisibility(View.INVISIBLE);
                            } else {
                                image2.setImageBitmap(bp);
                                image2Bp = bp;
                                imageTurn = 1;
                                calculatePoints();
                            }
                        }

                    } else {
                        Toast.makeText(getApplicationContext(),"Please take picture.",Toast.LENGTH_SHORT).show();
                    }
                }
            });

    public void calculatePoints() {
        int color1 = image1Bp.getPixel(image1Bp.getWidth()/2, image1Bp.getHeight()/2);
        int color2 = image2Bp.getPixel(image2Bp.getWidth()/2, image2Bp.getHeight()/2);

        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);

        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);

        double rBar = (r1 + r2) / 2.0;
        int rChange = r1 - r2;
        int gChange = g1 - g2;
        int bChange = b1 - b2;

        double colorChange = Math.sqrt((2 + (rBar/256)) * (rChange * rChange) + 4 * (gChange * gChange) + (2 + ((255 - rBar)/256)) * (bChange * bChange));
        //TODO: Subtract by max possible value to get score and round
        if (playerTurn == 1) {
            player1Score.setText("" + (Double.parseDouble(player1Score.getText().toString()) + colorChange));
        } else {
            player2Score.setText("" + (Double.parseDouble(player2Score.getText().toString()) + colorChange));
        }

    }
}