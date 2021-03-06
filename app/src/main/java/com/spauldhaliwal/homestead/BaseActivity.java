package com.spauldhaliwal.homestead;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.ui.auth.AuthUI;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    SharedPreferences prefs = null;
    Vibrator vibe;
    Drawable userProfileDrawable;
    Context mContext;
    Toolbar toolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getSharedPreferences("com.spauldhaliwal.homestead", MODE_PRIVATE);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitleTextColor(616161);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getActionBar();
        Log.d(TAG, "Toolbar onCreate: BaseActivity Toolbar: " + toolbar);


        Glide.with(this)
                .load(CurrentUser.getProfileImage())
                .apply(RequestOptions.circleCropTransform().override(80, 80))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable Transition<? super Drawable> transition) {
                        userProfileDrawable = resource;
                        toolbar.setOverflowIcon(userProfileDrawable);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_signout):
                signOut();
                return true;
            case android.R.id.home:
                finish();
                return true;
            case (R.id.menuTutorial):
                beginOnBoarding();
                return true;
            case (R.id.menuInvite):
                sendInvite();
                return true;
            case (R.id.menuChatItem):
                Intent intent = new Intent(this, ChatActivity.class);
                startActivity(intent);
                return true;
            case (R.id.menuLeaveHomestead):
                leaveHomestead();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                vibe.vibrate(2);
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
                return true;
            default:
                return super.onKeyLongPress(keyCode, event);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityState.setActivity(this);
        //Check if app is in first run
        mContext = this;
        if (prefs.getBoolean("firstRun", true)) {
            beginOnBoarding();
            prefs.edit().putBoolean("firstRun", false).apply();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mContext = null;
        ActivityState.clearActivity(this);
    }

    protected void sendInvite() {
        String userId = CurrentUser.getUid();
        String homesteadId = CurrentUser.getHomesteadUid();
        String homesteadName = CurrentUser.getHomesteadName();
        String inviteTitle = "Join " + CurrentUser.getName() + "'s Homestead!";
        String inviteDescription = CurrentUser.getName() + " has invited you to join their homestead: " + CurrentUser.getHomesteadName() + ". Follow the link to accept their invitation!";
        Uri inviteImageUrl = Uri.parse(CurrentUser.getProfileImage());

        Task<ShortDynamicLink> dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://homesteadapp.com/?homesteadid=" + homesteadId
                        + "&userid="
                        + userId))

                .setDynamicLinkDomain("homesteadapp.page.link")
                // Open links with this app on android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                // Open links with this app on iOS
                .setIosParameters(new DynamicLink.IosParameters
                        .Builder("com.spauldhaliwal.homestead").build())
                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle(inviteTitle)
                        .setDescription(inviteDescription)
                        .setImageUrl(inviteImageUrl)
                        .build())
                .buildShortDynamicLink().addOnCompleteListener(this, new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short Link created
                            Uri shortDynamicLinkUri = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();

                            Log.d(TAG, "onOptionsItemSelected: inviteUri: " + shortDynamicLinkUri);

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Send invite link");
                            intent.putExtra(Intent.EXTRA_TEXT, shortDynamicLinkUri.toString());
                            startActivity(Intent.createChooser(intent, "Send invite link"));

                        } else {
                            // Error
                            // ...
                        }

                    }
                });
    }

    protected void leaveHomestead() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Homestead")
                .setMessage("Are you sure you want to leave this Homestead?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseResolver.leaveHomestead();
                        AuthUI.getInstance().signOut(mContext)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // User is now signed out

                                        ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE))
                                                .clearApplicationUserData();
                                        Intent intent = new Intent(mContext, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                        recreate();

                                    }
                                });
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    protected void beginOnBoarding() {
        if (!ActivityState.getCurrentActivity().getClass().getSimpleName().equals(MainActivity.class.getSimpleName())) {
            prefs.edit().remove("firstRun").apply();
            Intent intent = new Intent(mContext, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            //Check to see if toolbar is inflated, if not re-inflate.
            if (findViewById(R.id.menuChatItem) == null) {
                invalidateOptionsMenu();
                toolbar = findViewById(R.id.toolbar);
                toolbar.inflateMenu(R.menu.menu_main);
            }

            Drawable homesteadIcon = getResources().getDrawable(R.drawable.homestead_launcher);
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int centerX = size.x;

            int centerY = size.y;
            Rect rect = new Rect(centerX, 0, 0, centerY);

            final TapTargetSequence onBoardingSequence = new TapTargetSequence(this).targets(
                    TapTarget.forView(findViewById(R.id.fab), "Tap here to create a new task.", "You can either make it public, and share it with your Homestead, or keep it private.")
                            .outerCircleColor(R.color.colorAccent)
                            .tintTarget(false)
                            .cancelable(false)
                    ,
                    TapTarget.forToolbarMenuItem(toolbar, R.id.menuChatItem, "Tap here to access Homestead Chat.", "You'll be able to chat with the other members of your Homestead")
                            .outerCircleColor(R.color.colorAccent)
                            .tintTarget(true)
                            .cancelable(false)
                    ,
                    TapTarget.forToolbarOverflow(toolbar, "Tap here to access your menu.", "You can invite your roommates to join your Homestead!")
                            .outerCircleColor(R.color.colorAccent)
                            .tintTarget(false)
                            .cancelable(false)
                    ,
                    TapTarget.forBounds(rect, "Thank you for choosing Homestead.", "Tap here to begin.")
                            .icon(homesteadIcon)
                            .outerCircleColor(R.color.colorAccent)
                            .tintTarget(false)
                            .cancelable(false))
                    .listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            final Snackbar snackbar = Snackbar.make(findViewById(R.id.fab),
                                    "Tip: Swipe horizontally to switch between private and public tasks.", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();

                        }

                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {

                        }
                    });

            onBoardingSequence.start();
        }
    }

    protected void signOut() {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // User is now signed out
                        ((ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE))
                                .clearApplicationUserData(); // note: it has a return value!

                        Intent intent = new Intent(mContext, MainActivity.class);
                        startActivity(intent);
                        finish();
                        recreate();

                    }
                });
    }

    protected abstract int getLayoutId();


}
