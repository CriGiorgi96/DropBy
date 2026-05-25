package com.example.dropby.fragments.main;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.dropby.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FaqFragment extends Fragment {

    private LinearLayout faqContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_faq, container, false);
        faqContainer = view.findViewById(R.id.faq_container);
        loadAndBuildFaq();
        return view;
    }

    private void loadAndBuildFaq() {
        if (faqContainer == null) return;
        faqContainer.removeAllViews();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(requireContext().getAssets().open("faq.txt")))) {

            String line;
            String currentQuestion = null;
            StringBuilder currentAnswer = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("-") && line.endsWith("?")) {

                    if (currentQuestion != null) {
                        addFaqItem(currentQuestion, currentAnswer.toString().trim());
                    }

                    currentQuestion = line.substring(1).trim();
                    currentAnswer = new StringBuilder();

                } else {
                    if (currentAnswer.length() > 0) {
                        currentAnswer.append("\n");
                    }
                    currentAnswer.append(line);
                }
            }

            if (currentQuestion != null) {
                addFaqItem(currentQuestion, currentAnswer.toString().trim());
            }

        } catch (IOException e) {
            e.printStackTrace();

            if (getContext() != null) {
                TextView errorText = new TextView(getContext());
                errorText.setText(R.string.faq_error_loading);
                faqContainer.addView(errorText);
            }
        }
    }

    private void addFaqItem(String question, String answer) {
        if (getContext() == null) return;

        LinearLayout itemContainer = new LinearLayout(getContext());
        itemContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout headerLayout = new LinearLayout(getContext());
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);
        headerLayout.setGravity(Gravity.CENTER_VERTICAL);

        int paddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
        );
        headerLayout.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);

        headerLayout.setClickable(true);
        headerLayout.setFocusable(true);

        TypedValue outValue = new TypedValue();
        if (getContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true)) {
            headerLayout.setBackgroundResource(outValue.resourceId);
        }

        TextView questionTextView = new TextView(getContext());
        questionTextView.setText(question);
        questionTextView.setTextSize(17);
        questionTextView.setTypeface(null, Typeface.BOLD);
        questionTextView.setTextColor(
                ContextCompat.getColor(getContext(), R.color.db_text_primary)
        );

        LinearLayout.LayoutParams qParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        questionTextView.setLayoutParams(qParams);

        ImageView arrowIcon = new ImageView(getContext());
        arrowIcon.setImageResource(R.drawable.ic_arrow_down);

        int arrowSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()
        );

        LinearLayout.LayoutParams iParams =
                new LinearLayout.LayoutParams(arrowSize, arrowSize);
        arrowIcon.setLayoutParams(iParams);
        arrowIcon.setAlpha(0.6f);

        headerLayout.addView(questionTextView);
        headerLayout.addView(arrowIcon);

        TextView answerTextView = new TextView(getContext());
        answerTextView.setText(answer);
        answerTextView.setTextSize(15);

        int answerPaddingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
        );

        answerTextView.setPadding(answerPaddingPx, 0, answerPaddingPx, answerPaddingPx);
        answerTextView.setVisibility(View.GONE);
        answerTextView.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.db_text_secondary)
        );

        headerLayout.setOnClickListener(v -> {
            if (answerTextView.getVisibility() == View.GONE) {
                answerTextView.setVisibility(View.VISIBLE);
                arrowIcon.animate().rotation(180f).setDuration(200).start();
            } else {
                answerTextView.setVisibility(View.GONE);
                arrowIcon.animate().rotation(0f).setDuration(200).start();
            }
        });

        itemContainer.addView(headerLayout);
        itemContainer.addView(answerTextView);

        View divider = new View(getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1
        ));
        divider.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.db_divider)
        );

        itemContainer.addView(divider);

        faqContainer.addView(itemContainer);
    }
}
