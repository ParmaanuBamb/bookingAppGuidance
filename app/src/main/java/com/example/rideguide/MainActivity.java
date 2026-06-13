package com.example.rideguide;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String HELP_NUMBER = "112";

    private final List<Button> providerButtons = new ArrayList<>();
    private final List<Step> steps = new ArrayList<>();
    private LinearLayout stepsLayout;
    private TextView title;
    private int selectedProvider = 0;
    private int currentStep = 0;

    private final Provider[] providers = new Provider[] {
            new Provider("Rapido", "com.rapido.passenger", "rapido://home", "https://play.google.com/store/apps/details?id=com.rapido.passenger"),
            new Provider("Ola", "com.olacabs.customer", "olacabs://app", "https://play.google.com/store/apps/details?id=com.olacabs.customer"),
            new Provider("Uber", "com.ubercab", "uber://", "https://play.google.com/store/apps/details?id=com.ubercab")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildSteps();
        setContentView(buildScreen());
        renderProviderButtons();
        renderSteps();
    }

    private View buildScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(Color.parseColor("#F7F4ED"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(20), dp(18), dp(24));
        scrollView.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        title = text("Book Auto or Cab", 30, true);
        title.setTextColor(Color.parseColor("#1F2933"));
        root.addView(title);

        TextView subtitle = text("Choose an app, then follow one instruction at a time.", 20, false);
        subtitle.setTextColor(Color.parseColor("#3B4652"));
        subtitle.setPadding(0, dp(8), 0, dp(16));
        root.addView(subtitle);

        LinearLayout providerRow = new LinearLayout(this);
        providerRow.setOrientation(LinearLayout.VERTICAL);
        root.addView(providerRow);

        for (int i = 0; i < providers.length; i++) {
            Button providerButton = button(providers[i].name);
            final int index = i;
            providerButton.setOnClickListener(v -> {
                selectedProvider = index;
                currentStep = 0;
                renderProviderButtons();
                renderSteps();
            });
            providerButtons.add(providerButton);
            providerRow.addView(providerButton, matchWrap(0, 0, 0, 10));
        }

        stepsLayout = new LinearLayout(this);
        stepsLayout.setOrientation(LinearLayout.VERTICAL);
        stepsLayout.setPadding(0, dp(8), 0, 0);
        root.addView(stepsLayout);

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setOrientation(LinearLayout.VERTICAL);
        actionRow.setPadding(0, dp(12), 0, 0);
        root.addView(actionRow);

        Button openApp = primaryButton("Open Selected App");
        openApp.setOnClickListener(v -> openSelectedProvider());
        actionRow.addView(openApp, matchWrap(0, 0, 0, 10));

        Button callHelp = secondaryButton("Call Emergency Help");
        callHelp.setOnClickListener(v -> callHelpNumber());
        actionRow.addView(callHelp, matchWrap(0, 0, 0, 0));

        return scrollView;
    }

    private void buildSteps() {
        steps.add(new Step("Open the app", "Tap the button below. If the app is not installed, ask a family member to install it from Play Store."));
        steps.add(new Step("Set pickup location", "Check that the pickup address is your current place. If it is wrong, tap pickup and type the correct place."));
        steps.add(new Step("Enter destination", "Tap destination. Type where you want to go, then choose the correct place from the list."));
        steps.add(new Step("Choose ride type", "Pick Auto for short trips, or Cab for more comfort. Look at the fare before continuing."));
        steps.add(new Step("Confirm booking", "Tap Book or Confirm. Do not pay extra cash unless the app says cash payment is selected."));
        steps.add(new Step("Check driver details", "Read the vehicle number, driver name, and OTP. Share these with a family member before sitting in the vehicle."));
        steps.add(new Step("Start the ride safely", "Match the vehicle number. Tell the driver the OTP only after you sit inside the correct vehicle."));
    }

    private void renderProviderButtons() {
        for (int i = 0; i < providerButtons.size(); i++) {
            Button button = providerButtons.get(i);
            Provider provider = providers[i];
            boolean selected = i == selectedProvider;
            button.setText(selected ? provider.name + " selected" : provider.name);
            button.setTextColor(Color.parseColor("#1F2933"));
            button.setBackgroundResource(selected ? R.drawable.provider_selected : R.drawable.provider_unselected);
        }
        title.setText("Book with " + providers[selectedProvider].name);
    }

    private void renderSteps() {
        stepsLayout.removeAllViews();

        TextView progress = text("Step " + (currentStep + 1) + " of " + steps.size(), 22, true);
        progress.setTextColor(Color.parseColor("#006D77"));
        progress.setPadding(0, 0, 0, dp(10));
        stepsLayout.addView(progress);

        for (int i = 0; i < steps.size(); i++) {
            stepsLayout.addView(stepCard(i), matchWrap(0, 0, 0, 10));
        }

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(0, dp(4), 0, dp(6));
        stepsLayout.addView(nav);

        Button previous = secondaryButton("Back");
        previous.setEnabled(currentStep > 0);
        previous.setAlpha(currentStep > 0 ? 1f : 0.45f);
        previous.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                renderSteps();
            }
        });
        nav.addView(previous, weightWrap(1, 0, 0, 8, 0));

        Button next = primaryButton(currentStep == steps.size() - 1 ? "Done" : "Next");
        next.setOnClickListener(v -> {
            if (currentStep < steps.size() - 1) {
                currentStep++;
                renderSteps();
            } else {
                Toast.makeText(this, "Ride guide completed. Travel safely.", Toast.LENGTH_LONG).show();
            }
        });
        nav.addView(next, weightWrap(1, 8, 0, 0, 0));
    }

    private View stepCard(int index) {
        Step step = steps.get(index);
        boolean active = index == currentStep;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.card_bg);
        card.setAlpha(index <= currentStep ? 1f : 0.55f);
        card.setOnClickListener(v -> {
            currentStep = index;
            renderSteps();
        });

        TextView heading = text((index + 1) + ". " + step.heading, active ? 24 : 21, true);
        heading.setTextColor(active ? Color.parseColor("#006D77") : Color.parseColor("#1F2933"));
        card.addView(heading);

        TextView detail = text(step.detail, active ? 20 : 18, false);
        detail.setTextColor(Color.parseColor("#3B4652"));
        detail.setPadding(0, dp(8), 0, 0);
        card.addView(detail);

        return card;
    }

    private void openSelectedProvider() {
        Provider provider = providers[selectedProvider];
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(provider.packageName);

        if (launchIntent != null) {
            startActivity(launchIntent);
            return;
        }

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(provider.deepLink)));
        } catch (ActivityNotFoundException ignored) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(provider.playStoreUrl)));
        }
    }

    private void callHelpNumber() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + HELP_NUMBER));
        startActivity(callIntent);
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(22);
        button.setMinHeight(dp(64));
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(10), dp(8), dp(10), dp(8));
        return button;
    }

    private Button primaryButton(String label) {
        Button button = button(label);
        button.setTextColor(Color.WHITE);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackgroundResource(R.drawable.primary_button);
        return button;
    }

    private Button secondaryButton(String label) {
        Button button = button(label);
        button.setTextColor(Color.parseColor("#006D77"));
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackgroundResource(R.drawable.secondary_button);
        return button;
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(sp);
        textView.setLineSpacing(dp(3), 1.05f);
        if (bold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return textView;
    }

    private LinearLayout.LayoutParams matchWrap(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private LinearLayout.LayoutParams weightWrap(float weight, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                weight
        );
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static class Provider {
        final String name;
        final String packageName;
        final String deepLink;
        final String playStoreUrl;

        Provider(String name, String packageName, String deepLink, String playStoreUrl) {
            this.name = name;
            this.packageName = packageName;
            this.deepLink = deepLink;
            this.playStoreUrl = playStoreUrl;
        }
    }

    private static class Step {
        final String heading;
        final String detail;

        Step(String heading, String detail) {
            this.heading = heading;
            this.detail = detail;
        }
    }
}
