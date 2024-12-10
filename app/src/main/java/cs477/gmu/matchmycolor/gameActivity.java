package cs477.gmu.matchmycolor;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;

public class gameActivity extends AppCompatActivity {
    private int playerRole;
    private int playerTurn;
    private int imageTurn;
    private TextView player1Score, player2Score, player1Turn, player2Turn, points;
    private ImageView image1, image2;
    private Bitmap image1Bp, image2Bp;
    private DatabaseReference gameRef;
    private String gameId;
    private final int maxRounds = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameId = getIntent().getStringExtra("gameId");
        gameRef = FirebaseDatabase.getInstance().getReference("games").child(gameId);

        player1Score = findViewById(R.id.player1_score);
        player2Score = findViewById(R.id.player2_score);
        player1Turn = findViewById(R.id.player1_turn);
        player2Turn = findViewById(R.id.player2_turn);
        points = findViewById(R.id.points);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);

        assignRole();
        setupScoreListeners();
        setupImageSyncListeners();
        setupTurnListeners();
        updateTurnUI();
    }

    private void assignRole() {
        if (getIntent().hasExtra("playerRole")) {
            playerRole = getIntent().getIntExtra("playerRole", 1);
            Toast.makeText(this, "You are Player " + playerRole, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error assigning role. Restart the game.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupScoreListeners() {
        gameRef.child("scores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int player1ScoreValue = snapshot.child("player1").getValue(Integer.class);
                    int player2ScoreValue = snapshot.child("player2").getValue(Integer.class);

                    player1Score.setText(String.valueOf(player1ScoreValue));
                    player2Score.setText(String.valueOf(player2ScoreValue));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(gameActivity.this, "Error syncing scores.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupImageSyncListeners() {
        gameRef.child("images").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean isImage1Ready = false;
                    boolean isImage2Ready = false;

                    if (snapshot.child("image1").exists() && !snapshot.child("image1").getValue(String.class).isEmpty()) {
                        String image1Data = snapshot.child("image1").getValue(String.class);
                        image1Bp = decodeBase64(image1Data);
                        image1.setImageBitmap(image1Bp);
                        isImage1Ready = true;
                    }

                    if (snapshot.child("image2").exists() && !snapshot.child("image2").getValue(String.class).isEmpty()) {
                        String image2Data = snapshot.child("image2").getValue(String.class);
                        image2Bp = decodeBase64(image2Data);
                        image2.setImageBitmap(image2Bp);
                        isImage2Ready = true;
                    }

                    if (isImage1Ready && isImage2Ready) {
                        calculatePoints();
                        resetRound();
                        checkGameOver();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(gameActivity.this, "Error syncing images.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTurnListeners() {
        gameRef.child("playerTurn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    playerTurn = snapshot.getValue(Integer.class);
                    updateTurnUI();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(gameActivity.this, "Error syncing turn.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cameraButton(View v) {
        if (playerRole != playerTurn) {
            Toast.makeText(this, "Wait for your turn!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            myActivityResultLauncher.launch(takePictureIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Camera unavailable.", Toast.LENGTH_SHORT).show();
        }
    }

    ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bitmap bp = (Bitmap) result.getData().getExtras().get("data");

                        if (playerTurn == 1) {
                            gameRef.child("images").child("image1").setValue(encodeBitmap(bp))
                                    .addOnSuccessListener(aVoid -> {
                                        image1Bp = bp;
                                        image1.setImageBitmap(bp);
                                        gameRef.child("playerTurn").setValue(2);
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(gameActivity.this, "Failed to upload Image 1.", Toast.LENGTH_SHORT).show());
                        } else {
                            gameRef.child("images").child("image2").setValue(encodeBitmap(bp))
                                    .addOnSuccessListener(aVoid -> {
                                        image2Bp = bp;
                                        image2.setImageBitmap(bp);

                                        gameRef.child("executionCount").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot snapshot) {
                                                int executionCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;

                                                if (executionCount < 1) {
                                                    gameRef.child("executionCount").setValue(executionCount + 1);
                                                } else {
                                                    gameRef.child("executionCount").setValue(0);
                                                    gameRef.child("playerTurn").setValue(1);
                                                }

                                                gameRef.child("round").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot roundSnapshot) {
                                                        int currentRound = roundSnapshot.exists() ? roundSnapshot.getValue(Integer.class) : 0;
                                                        gameRef.child("round").setValue(currentRound + 1);

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError error) {
                                                        Toast.makeText(gameActivity.this, "Error updating round.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError error) {
                                                Toast.makeText(gameActivity.this, "Error reading execution count.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(gameActivity.this, "Failed to upload Image 2.", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No image captured.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    private String encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap decodeBase64(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void calculatePoints() {
        if (image1Bp == null || image2Bp == null) {
            Toast.makeText(this, "Images are not ready for comparison.", Toast.LENGTH_SHORT).show();
            return;
        }

        int color1 = image1Bp.getPixel(image1Bp.getWidth() / 2, image1Bp.getHeight() / 2);
        int color2 = image2Bp.getPixel(image2Bp.getWidth() / 2, image2Bp.getHeight() / 2);

        int r1 = Color.red(color1), g1 = Color.green(color1), b1 = Color.blue(color1);
        int r2 = Color.red(color2), g2 = Color.green(color2), b2 = Color.blue(color2);

        double rBar = (r1 + r2) / 2.0;
        double colorDifference = Math.sqrt(
                (2 + rBar / 256) * Math.pow(r1 - r2, 2) +
                        4 * Math.pow(g1 - g2, 2) +
                        (2 + (255 - rBar) / 256) * Math.pow(b1 - b2, 2)
        );

        int pointsEarned = (int) Math.round(764 - colorDifference);
        if (playerTurn == 1) {
            gameRef.child("scores").child("player1").setValue(
                    Integer.parseInt(player1Score.getText().toString()) + pointsEarned
            );
        } else {
            gameRef.child("scores").child("player2").setValue(
                    Integer.parseInt(player2Score.getText().toString()) + pointsEarned
            );
        }

        points.setVisibility(View.VISIBLE);
        points.setText("+" + pointsEarned);
    }

    private void resetRound() {
        gameRef.child("images").child("image1").setValue("");
        gameRef.child("images").child("image2").setValue("");
        image1.setImageBitmap(null);
        image2.setImageBitmap(null);
        image1Bp = null;
        image2Bp = null;
    }

    private void checkGameOver() {
        gameRef.child("round").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int currentRound = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                if (currentRound >= maxRounds) {
                    String winner;
                    int player1ScoreValue = Integer.parseInt(player1Score.getText().toString());
                    int player2ScoreValue = Integer.parseInt(player2Score.getText().toString());

                    if (player1ScoreValue > player2ScoreValue) {
                        winner = "Player 1 wins!";
                    } else if (player1ScoreValue < player2ScoreValue) {
                        winner = "Player 2 wins!";
                    } else {
                        winner = "It's a draw!";
                    }

                    new AlertDialog.Builder(gameActivity.this)
                            .setTitle("Game Over")
                            .setMessage(winner)
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(gameActivity.this, "Error checking game over.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateTurnUI() {
        if (playerTurn == 1) {
            player1Turn.setVisibility(View.VISIBLE);
            player2Turn.setVisibility(View.INVISIBLE);
        } else {
            player1Turn.setVisibility(View.INVISIBLE);
            player2Turn.setVisibility(View.VISIBLE);
        }
    }
}
