
package com.example.mindhaven;

import java.util.ArrayList;
import java.util.List;

public class CBTWorksheetManager {
    public static List<CBTWorksheet> getWorksheets() {
        List<CBTWorksheet> worksheets = new ArrayList<>();

        worksheets.add(new CBTWorksheet(
                "Thought Record",
                "Document and analyze your thoughts and emotions",
                "Identify patterns in negative thinking and develop balanced perspectives",
                "1. Write down the situation\n2. Note your emotions\n3. Identify automatic thoughts\n4. Find evidence for and against\n5. Develop balanced thought",
                "Helps challenge negative thought patterns and develop more balanced thinking"
        ));

        worksheets.add(new CBTWorksheet(
                "Activity Planning",
                "Schedule meaningful and enjoyable activities",
                "Combat depression and increase positive experiences",
                "1. List activities you enjoy\n2. Rate their difficulty\n3. Schedule them throughout week\n4. Track completion and mood",
                "Increases motivation and provides sense of accomplishment"
        ));

        worksheets.add(new CBTWorksheet(
                "Behavioral Activation",
                "Gradually increase engagement in positive activities",
                "Break cycle of depression and inactivity",
                "1. Start with small tasks\n2. Build up to more challenging activities\n3. Track progress\n4. Note mood changes",
                "Helps overcome avoidance and builds confidence"
        ));

        return worksheets;
    }
}
