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

        DatabaseReference gameRef = gamesRef.child(gameId);

        gameRef.child("gameId").setValue(gameId);
        gameRef.child("password").setValue(pass);
        gameRef.child("player1").setValue("Waiting...");
        gameRef.child("player2").setValue("Waiting...");
        gameRef.child("playerTurn").setValue(1);
        gameRef.child("round").setValue(0);
        gameRef.child("scores/player1").setValue(0);
        gameRef.child("scores/player2").setValue(0);
        gameRef.child("images/image1").setValue("");
        gameRef.child("images/image2").setValue("");
        gameRef.child("executionCount").setValue(0);
        gameRef.child("player2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String player2Status = dataSnapshot.getValue(String.class);
                if (player2Status != null && !player2Status.equals("Waiting...")) {
                    Intent intent = new Intent(createActivity.this, gameActivity.class);
                    intent.putExtra("gameId", gameId);
                    intent.putExtra("playerRole", 1); // Creator is Player 1
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Failed to detect player2: " + databaseError.getMessage());
            }
        });
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
