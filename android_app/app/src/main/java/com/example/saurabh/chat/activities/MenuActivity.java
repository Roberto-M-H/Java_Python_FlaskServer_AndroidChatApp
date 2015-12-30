package com.example.saurabh.chat.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.saurabh.chat.ChatApplication;
import com.example.saurabh.chat.R;
import com.example.saurabh.chat.fragments.FriendsListFragment;
import com.example.saurabh.chat.fragments.RoomsFragment;
import com.example.saurabh.chat.network.CreateRoomAsyncTask;
import com.example.saurabh.chat.network.Logout;
import com.example.saurabh.chat.utilities.CenteredImageSpan;


public class MenuActivity extends AppCompatActivity {
    private String username, session;
    private int user_id = -1;
    RoomsFragment roomsFragment;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fab;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        intent = getIntent();

        ChatApplication chatApplication = (ChatApplication) MenuActivity.this.getApplication();
        username = chatApplication.getUsername();
        user_id = chatApplication.getUserID();
        session = chatApplication.getSession();

        roomsFragment = new RoomsFragment();
        Bundle roomsFragmentArguments = new Bundle();
        roomsFragmentArguments.putInt("user_id", user_id);
        roomsFragmentArguments.putString("username", username);
        roomsFragmentArguments.putString("session", session);
        roomsFragment.setArguments(roomsFragmentArguments);

        CollectionPagerAdapter mCollectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager(), roomsFragment);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager_menu);
        mViewPager.setAdapter(mCollectionPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        if(chatApplication.isLoggedIn() && intent.getBooleanExtra("returning user", false)) {
            coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout_menu);

            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "Signed in as " + username, Snackbar.LENGTH_LONG)
                    .setAction("Not you?", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new Logout(MenuActivity.this, user_id, username, session);
                        }
                    });

            snackbar.show();
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateRoomDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);

        // personalise menu item for viewing user's own profile
        menu.findItem(R.id.action_user_profile).setTitle("View " + username + "'s profile");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_user_profile) {
            Intent userProfileIntent = new Intent(MenuActivity.this, UserProfileActivity.class);
            intent.putExtra("username", username);
            startActivity(userProfileIntent);
        } else if(id == R.id.action_create_room) {
            showCreateRoomDialog();

            return true;
        } else if(id == R.id.action_logout) {
            new Logout(MenuActivity.this, user_id, username, session);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            new Logout(MenuActivity.this, user_id, username, session);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void showCreateRoomDialog() {
        AlertDialog.Builder createRoomDialog = new AlertDialog.Builder(MenuActivity.this);
        createRoomDialog.setTitle("Create room");

        final EditText input = new EditText(MenuActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        input.setLayoutParams(lp);
        input.setHint("Must be between 1-20 characters");

        createRoomDialog.setView(input);

        createRoomDialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String room = input.getText().toString();
                if(!room.isEmpty() && room.length() <= 20) {
                    (new CreateRoomAsyncTask(MenuActivity.this, user_id, username, session, room)).execute();
                } else {
                    Toast.makeText(MenuActivity.this, "Room name must be between 1-20 characters long.", Toast.LENGTH_LONG).show();
                }
            }
        });

        createRoomDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        createRoomDialog.show();
    }

    class CollectionPagerAdapter extends FragmentPagerAdapter {
        private static final int FRAGMENT_ROOMS = 0, FRAGMENT_FRIENDS = 1;

        RoomsFragment roomsFragment;

        public CollectionPagerAdapter(FragmentManager fm, RoomsFragment roomsFragment) {
            super(fm);

            this.roomsFragment = roomsFragment;
        }

        @Override
        public Fragment getItem(int i) {
            switch(i) {
                case FRAGMENT_ROOMS:
                    return roomsFragment;
                case FRAGMENT_FRIENDS:
                    return new FriendsListFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Drawable image;
            SpannableString sb;
            CenteredImageSpan imageSpan;

            // https://guides.codepath.com/android/google-play-style-tabs-using-tablayout
            try {
                switch (position) {
                    case FRAGMENT_ROOMS:
                        image = MenuActivity.this.getResources().getDrawable(R.mipmap.ic_room_black);
                        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
                        sb = new SpannableString("  Rooms");
                        imageSpan = new CenteredImageSpan(image, ImageSpan.ALIGN_BOTTOM);
                        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return sb;
                    case FRAGMENT_FRIENDS:
                        image = MenuActivity.this.getResources().getDrawable(R.mipmap.ic_friend_black);
                        image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
                        sb = new SpannableString("  Friends");
                        CenteredImageSpan imageSpan2 = new CenteredImageSpan(image, ImageSpan.ALIGN_BOTTOM);
                        sb.setSpan(imageSpan2, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return sb;
                    default:
                        return "Null";
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                return "Null";
            }
        }
    }
}