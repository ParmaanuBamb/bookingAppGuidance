package com.example.rideguide;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String HELP_NUMBER = "112";
    private static final String PREFS_NAME = "ride_guide_contacts";
    private static final String KEY_EMERGENCY_NUMBERS = "emergency_numbers";

    private final List<Button> providerButtons = new ArrayList<>();
    private final List<String> emergencyNumbers = new ArrayList<>();
    private LinearLayout stepsLayout;
    private LinearLayout emergencyLayout;
    private TextView title;
    private EditText emergencyInput;
    private int selectedProvider = 0;
    private int currentStep = 0;

    private final Provider[] providers = new Provider[] {
            new Provider("Rapido", "com.rapido.passenger", "rapido://home", "https://play.google.com/store/apps/details?id=com.rapido.passenger", new Step[] {
                    new Step("Open Rapido", "Tap Open Selected App. Wait for the Rapido home screen.", "Home screen", "Search location", "Ride options below", "rapido_01_open"),
                    new Step("Check pickup", "Make sure the pickup point is your current place. If it is wrong, tap pickup and type the correct place.", "Pickup box", "Current location", "Change if wrong", "rapido_02_pickup"),
                    new Step("Enter destination", "Tap the destination box. Type the place name slowly and choose the correct result.", "Where to?", "Type destination", "Pick from list", "rapido_03_destination"),
                    new Step("Choose Auto or Cab", "Select Auto for short trips. Choose Cab if you need more comfort.", "Ride choice", "Auto / Cab", "Fare shown here", "rapido_04_choose_ride"),
                    new Step("Confirm ride", "Check fare and pickup again. Tap Book only when both are correct.", "Confirm screen", "Book ride", "Check fare first", "rapido_05_confirm"),
                    new Step("Share details", "Read the vehicle number, driver name, and OTP. Share them with a family member.", "Driver details", "Vehicle number", "OTP", "rapido_06_book"),
                    new Step("Start safely", "Sit only in the matching vehicle. Tell the OTP after sitting inside.", "Vehicle arrives", "Match number", "Then share OTP", "rapido_07_start")
            }),
            new Provider("Ola", "com.olacabs.customer", "olacabs://app", "https://play.google.com/store/apps/details?id=com.olacabs.customer", new Step[] {
                    new Step("Open Ola", "Tap Open Selected App. Wait for Ola to show the booking screen.", "Ola home", "Pickup shown", "Destination box", "ola_01_open"),
                    new Step("Check pickup", "Confirm the pickup address. If the marker is wrong, tap pickup and correct it.", "Pickup", "Your location", "Edit if needed", "ola_02_pickup"),
                    new Step("Add destination", "Tap destination. Type the address or landmark, then choose the right result.", "Drop location", "Search place", "Select result", "ola_03_destination"),
                    new Step("Select ride", "Choose Auto, Mini, Prime, or another simple option. Check the fare before booking.", "Ride list", "Auto / Cab", "Fare shown", "ola_04_choose_ride"),
                    new Step("Book the ride", "Tap Book after checking pickup, destination, and fare.", "Book screen", "Confirm booking", "Payment mode", "ola_05_confirm"),
                    new Step("Check driver", "Look for driver name, vehicle number, and OTP. Share them with family.", "Driver card", "Vehicle number", "OTP", "ola_06_driver"),
                    new Step("Ride safely", "Match the vehicle number before entering. Give OTP only to the correct driver.", "Arrived", "Check vehicle", "Share OTP", "ola_07_start")
            }),
            new Provider("Uber", "com.ubercab", "uber://", "https://play.google.com/store/apps/details?id=com.ubercab", new Step[] {
                    new Step("Open Uber", "Tap Open Selected App. Wait for Uber to show the map and destination search.", "Uber home", "Where to?", "Map behind", "uber_01_open"),
                    new Step("Enter destination", "Tap Where to? Type your destination and choose the correct place.", "Search", "Where to?", "Choose result", "uber_02_destination"),
                    new Step("Check pickup", "Check the pickup point on the map. Tap it if the location is wrong.", "Pickup pin", "Current place", "Change pickup", "uber_03_pickup"),
                    new Step("Choose ride", "Pick Auto, Uber Go, or the option with the fare you are comfortable with.", "Choose ride", "Auto / Go", "Fare", "uber_04_choose_ride"),
                    new Step("Confirm", "Check the fare and payment method. Tap Confirm only when everything is correct.", "Confirm screen", "Confirm ride", "Payment", "uber_05_confirm"),
                    new Step("Read driver details", "Note driver name, vehicle number, and pickup point. Share them with family.", "Driver details", "Vehicle number", "Driver name", "uber_06_driver"),
                    new Step("Start safely", "Match the vehicle number before entering. Keep the phone with you during the ride.", "Vehicle arrives", "Match number", "Stay alert", "uber_07_start")
            })
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadEmergencyNumbers();
        setContentView(buildScreen());
        renderProviderButtons();
        renderSteps();
        renderEmergencyNumbers();
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

        title = text("Cab Booking Helper", 30, true);
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

        emergencyLayout = new LinearLayout(this);
        emergencyLayout.setOrientation(LinearLayout.VERTICAL);
        emergencyLayout.setPadding(0, dp(18), 0, 0);
        root.addView(emergencyLayout);

        return scrollView;
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
        Step[] selectedSteps = providers[selectedProvider].steps;

        TextView progress = text("Step " + (currentStep + 1) + " of " + selectedSteps.length, 22, true);
        progress.setTextColor(Color.parseColor("#006D77"));
        progress.setPadding(0, 0, 0, dp(10));
        stepsLayout.addView(progress);

        for (int i = 0; i < selectedSteps.length; i++) {
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

        Button next = primaryButton(currentStep == selectedSteps.length - 1 ? "Done" : "Next");
        next.setOnClickListener(v -> {
            if (currentStep < providers[selectedProvider].steps.length - 1) {
                currentStep++;
                renderSteps();
            } else {
                Toast.makeText(this, "Ride guide completed. Travel safely.", Toast.LENGTH_LONG).show();
            }
        });
        nav.addView(next, weightWrap(1, 8, 0, 0, 0));
    }

    private View stepCard(int index) {
        Step step = providers[selectedProvider].steps[index];
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

        if (active) {
            View screenshot = realScreenshot(step);
            if (screenshot != null) {
                card.addView(screenshot, matchWrap(0, 12, 0, 0));
            } else {
                card.addView(mockScreenshot(step), matchWrap(0, 12, 0, 0));
            }
        }

        return card;
    }

    private View realScreenshot(Step step) {
        Bitmap bitmap = loadScreenshotBitmap(step.imageBaseName);
        if (bitmap == null) {
            return null;
        }

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundResource(R.drawable.phone_mock_bg);
        imageView.setPadding(dp(6), dp(6), dp(6), dp(6));
        imageView.setContentDescription(step.heading + " screenshot");
        return imageView;
    }

    private Bitmap loadScreenshotBitmap(String imageBaseName) {
        String[] extensions = new String[] { ".jpg", ".jpeg", ".png" };
        for (String extension : extensions) {
            try (InputStream stream = getAssets().open("screenshots/" + imageBaseName + extension)) {
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                if (bitmap != null) {
                    return bitmap;
                }
            } catch (IOException ignored) {
                // Try the next supported image extension.
            }
        }
        return null;
    }

    private View mockScreenshot(Step step) {
        LinearLayout phone = new LinearLayout(this);
        phone.setOrientation(LinearLayout.VERTICAL);
        phone.setPadding(dp(14), dp(12), dp(14), dp(12));
        phone.setBackgroundResource(R.drawable.phone_mock_bg);

        TextView top = text(providers[selectedProvider].name + " guide picture", 16, true);
        top.setTextColor(Color.parseColor("#1F2933"));
        top.setGravity(Gravity.CENTER);
        phone.addView(top, matchWrap(0, 0, 0, 10));

        phone.addView(mockRow(step.screenTitle, "#EAF7F5", true));
        phone.addView(mockRow(step.primaryCue, "#FFFFFF", false));
        phone.addView(mockRow(step.secondaryCue, "#FFF6D8", false));

        TextView note = text("Look for this part in the real app.", 16, false);
        note.setTextColor(Color.parseColor("#3B4652"));
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(10), 0, 0);
        phone.addView(note);

        return phone;
    }

    private View mockRow(String value, String color, boolean bold) {
        TextView row = text(value, 18, bold);
        row.setTextColor(Color.parseColor("#1F2933"));
        row.setGravity(Gravity.CENTER);
        row.setBackgroundColor(Color.parseColor(color));
        row.setPadding(dp(10), dp(12), dp(10), dp(12));
        LinearLayout.LayoutParams params = matchWrap(0, 0, 0, 8);
        row.setLayoutParams(params);
        return row;
    }

    private void renderEmergencyNumbers() {
        emergencyLayout.removeAllViews();

        TextView heading = text("Emergency Contacts", 26, true);
        heading.setTextColor(Color.parseColor("#1F2933"));
        emergencyLayout.addView(heading);

        TextView hint = text("Add family numbers here. Tap any number to call from the dialer.", 18, false);
        hint.setTextColor(Color.parseColor("#3B4652"));
        hint.setPadding(0, dp(6), 0, dp(12));
        emergencyLayout.addView(hint);

        Button call112 = primaryButton("Dial Emergency 112");
        call112.setOnClickListener(v -> dialNumber(HELP_NUMBER));
        emergencyLayout.addView(call112, matchWrap(0, 0, 0, 10));

        for (String number : emergencyNumbers) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 0, 0, dp(8));

            Button callButton = secondaryButton(number);
            callButton.setOnClickListener(v -> dialNumber(number));
            row.addView(callButton, weightWrap(1, 0, 0, 8, 0));

            Button removeButton = secondaryButton("Remove");
            removeButton.setTextSize(18);
            removeButton.setOnClickListener(v -> {
                emergencyNumbers.remove(number);
                saveEmergencyNumbers();
                renderEmergencyNumbers();
            });
            row.addView(removeButton, new LinearLayout.LayoutParams(dp(118), LinearLayout.LayoutParams.WRAP_CONTENT));

            emergencyLayout.addView(row);
        }

        emergencyInput = new EditText(this);
        emergencyInput.setTextSize(22);
        emergencyInput.setSingleLine(true);
        emergencyInput.setHint("Family phone number");
        emergencyInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        emergencyInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        emergencyInput.setBackgroundResource(R.drawable.input_bg);
        emergencyLayout.addView(emergencyInput, matchWrap(0, 4, 0, 10));

        Button addButton = secondaryButton("Add Contact Number");
        addButton.setOnClickListener(v -> addEmergencyNumber());
        emergencyLayout.addView(addButton, matchWrap(0, 0, 0, 0));
    }

    private void addEmergencyNumber() {
        String number = emergencyInput.getText().toString().trim();
        if (number.length() < 5) {
            Toast.makeText(this, "Please enter a valid phone number.", Toast.LENGTH_LONG).show();
            return;
        }
        emergencyNumbers.add(number);
        saveEmergencyNumbers();
        renderEmergencyNumbers();
        Toast.makeText(this, "Emergency contact added.", Toast.LENGTH_SHORT).show();
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

    private void dialNumber(String number) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        startActivity(callIntent);
    }

    private void loadEmergencyNumbers() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String saved = prefs.getString(KEY_EMERGENCY_NUMBERS, "");
        emergencyNumbers.clear();
        if (!saved.isEmpty()) {
            emergencyNumbers.addAll(Arrays.asList(saved.split(",")));
        }
    }

    private void saveEmergencyNumbers() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_EMERGENCY_NUMBERS, joinEmergencyNumbers())
                .apply();
    }

    private String joinEmergencyNumbers() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < emergencyNumbers.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(emergencyNumbers.get(i));
        }
        return builder.toString();
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
        final Step[] steps;

        Provider(String name, String packageName, String deepLink, String playStoreUrl, Step[] steps) {
            this.name = name;
            this.packageName = packageName;
            this.deepLink = deepLink;
            this.playStoreUrl = playStoreUrl;
            this.steps = steps;
        }
    }

    private static class Step {
        final String heading;
        final String detail;
        final String screenTitle;
        final String primaryCue;
        final String secondaryCue;
        final String imageBaseName;

        Step(String heading, String detail, String screenTitle, String primaryCue, String secondaryCue, String imageBaseName) {
            this.heading = heading;
            this.detail = detail;
            this.screenTitle = screenTitle;
            this.primaryCue = primaryCue;
            this.secondaryCue = secondaryCue;
            this.imageBaseName = imageBaseName;
        }
    }
}
