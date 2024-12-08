package cs477.gmu.matchmycolor;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Game {
    public String gameId;
    public String password;
    public String player1;
    public String player2;
    public Map<String, Object> gameState;

    public Game() { } // Default constructor for Firebase

    public Game(String gameId, String password, String player1, String player2) {
        this.gameId = gameId;
        this.password = password;
        this.player1 = player1;
        this.player2 = player2;
        this.gameState = new HashMap<>();
    }

    // Save game to Firebase
    public void saveToFirebase() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("games");
        dbRef.child(this.gameId).setValue(this)
                .addOnSuccessListener(aVoid -> System.out.println("Game saved successfully!"))
                .addOnFailureListener(e -> System.err.println("Error saving game: " + e.getMessage()));
    }

    // Update game state (e.g., add player2)
    public void updatePlayer2(String player2) {
        this.player2 = player2;
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("games").child(this.gameId);
        dbRef.child("player2").setValue(player2)
                .addOnSuccessListener(aVoid -> System.out.println("Player 2 added successfully!"))
                .addOnFailureListener(e -> System.err.println("Error updating player2: " + e.getMessage()));
    }
}
