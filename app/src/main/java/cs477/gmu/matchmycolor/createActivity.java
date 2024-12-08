package cs477.gmu.matchmycolor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class createActivity extends AppCompatActivity {
    TextView textView;
    TextView password;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textView = findViewById(R.id.passStart);
        textView.setVisibility(View.VISIBLE);
        password = findViewById(R.id.password);

        String pass = passWordGenertaed();
        password.setText(pass);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gamesRef = database.getReference("games");

        String gameId = gamesRef.push().getKey();
        assert gameId != null;

        Game game = new Game(gameId, pass, "Player1", null); // Replace "Player1" dynamically if needed

        DatabaseReference gameRef = gamesRef.child(gameId);
        gameRef.setValue(game)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Game Created! Share the password: " + pass, Toast.LENGTH_LONG).show();
                    gameRef.onDisconnect().removeValue();

                    // Listen for player2 being added
                    gameRef.child("player2").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Navigate to gameActivity when player2 is set
                                Intent intent = new Intent(createActivity.this, gameActivity.class);
                                intent.putExtra("gameId", gameId);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("FirebaseError", "Failed to detect player2: " + databaseError.getMessage());
                        }
                    });
                })
                .addOnFailureListener(e -> Log.e("FirebaseError", "Failed to create game: " + e.getMessage()));
    }

    private String passWordGenertaed() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder passWord = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 4; i++) {
            passWord.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return passWord.toString();
    }
}
