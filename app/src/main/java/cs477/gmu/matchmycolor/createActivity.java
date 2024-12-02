package cs477.gmu.matchmycolor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

import java.util.List;
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
        password.setText(passWordGenertaed());

    }



    private String passWordGenertaed(){
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder passWord = new StringBuilder();
        Random rand = new Random();
        for (int i=0;i<4;i++){
            passWord.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return passWord.toString();
    }

}