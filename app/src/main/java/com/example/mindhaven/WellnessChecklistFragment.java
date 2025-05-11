
package com.example.mindhaven;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class WellnessChecklistFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CheckBox exerciseCheck, meditationCheck, waterCheck, mealCheck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wellness_checklist, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        exerciseCheck = view.findViewById(R.id.exerciseCheck);
        meditationCheck = view.findViewById(R.id.meditationCheck);
        waterCheck = view.findViewById(R.id.waterCheck);
        mealCheck = view.findViewById(R.id.mealCheck);

        Button saveButton = view.findViewById(R.id.saveChecklistButton);
        saveButton.setOnClickListener(v -> saveWellnessData());

        return view;
    }

    private void saveWellnessData() {
        if (mAuth.getCurrentUser() != null) {
            Map<String, Object> wellnessData = new HashMap<>();
            wellnessData.put("exercise", exerciseCheck.isChecked());
            wellnessData.put("meditation", meditationCheck.isChecked());
            wellnessData.put("water", waterCheck.isChecked());
            wellnessData.put("meal", mealCheck.isChecked());
            wellnessData.put("date", new Date());

            db.collection("users")
                    .document(mAuth.getCurrentUser().getUid())
                    .collection("wellness_checklist")
                    .add(wellnessData);
        }
    }
}
