package cs477.gmu.matchmycolor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

public class enterActivity extends AppCompatActivity {
    EditText passInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        passInput = findViewById(R.id.passwordInput);
    }

    public void enterOnClick(View view) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gamesRef = database.getReference("games");

        gamesRef.orderByChild("password").equalTo(passInput.getText().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                                String gameId = gameSnapshot.getKey();
                                DatabaseReference gameRef = gamesRef.child(gameId);

                                gameRef.child("player2").setValue("Connected")
                                        .addOnSuccessListener(aVoid -> {
                                            Intent intent = new Intent(enterActivity.this, gameActivity.class);
                                            intent.putExtra("gameId", gameId);
                                            intent.putExtra("playerRole", 2); // Joiner is Player 2
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(enterActivity.this, "Failed to join game.", Toast.LENGTH_SHORT).show();
                                        });
                                break;
                            }
                        } else {
                            Toast.makeText(enterActivity.this, "Game not found. Check the password.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(enterActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void backOnClick(View view) {
        Intent intent = new Intent(enterActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
