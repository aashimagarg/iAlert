package com.ialert.ialert;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final int NUM_POSTS = 3;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private static final String DATA = "data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
//                    start new task on log out
//                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//                    finish();
                }
            }
        };

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "User after LogOut: " + FirebaseAuth.getInstance().getCurrentUser());
                //log out of firebase and fb and go to login activity
                FirebaseAuth.getInstance().signOut();
                //used solely to reset the automated facebook login button
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // network calls
        final ArrayList<String> postIds = new ArrayList<String>();
        final Map<String, Integer> friendsToCount = new HashMap<>();

        // grab posts
        GraphRequest requestPosts = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(), (GraphRequest.GraphJSONObjectCallback) new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/posts",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        JSONArray jsonArray = response.getJSONObject().optJSONArray(DATA);
                        if (jsonArray.length() != 0) {
                            for (int i = 0; i < NUM_POSTS; i++) {
                                postIds.add(jsonArray.optJSONObject(i).optString("id"));
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.no_posts), Toast.LENGTH_LONG).show();
                        }
                    }
                })
        );
        requestPosts.executeAsync();

        // grab likes
        for (int i = 0; i < NUM_POSTS; i++) {
            GraphRequest requestLikes = GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(), (GraphRequest.GraphJSONObjectCallback) new GraphRequest(
                            AccessToken.getCurrentAccessToken(),
                            "/" + postIds.get(i) + "/likes",
                            null,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {
                                    JSONArray jsonArray = response.getJSONObject().optJSONArray(DATA);
                                    if (jsonArray.length() != 0) {
                                        for (int j = 0; j < jsonArray.length(); j++) {
                                            String id = jsonArray.optJSONObject(j).optString("id");
                                            if (!friendsToCount.containsKey(id)) {
                                                // create new friend
                                                friendsToCount.put(id, 1);
                                            } else {
                                                // increment count
                                                friendsToCount.put(id, friendsToCount.get(id) + 1);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.no_likers), Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
            );
            requestLikes.executeAsync();
        }

        // grab comments
        for (int i = 0; i < NUM_POSTS; i++) {
            GraphRequest requestComments = GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(), (GraphRequest.GraphJSONObjectCallback) new GraphRequest(
                            AccessToken.getCurrentAccessToken(),
                            "/" + postIds.get(i) + "/comments",
                            null,
                            HttpMethod.GET,
                            new GraphRequest.Callback() {
                                public void onCompleted(GraphResponse response) {
                                    JSONArray jsonArray = response.getJSONObject().optJSONArray(DATA);
                                    if (jsonArray.length() != 0) {
                                        for (int j = 0; j < jsonArray.length(); j++) {
                                            String id = jsonArray.optJSONObject(j).optJSONObject("from").optString("id");
                                            if (!friendsToCount.containsKey(id)) {
                                                // create new friend
                                                friendsToCount.put(id, 1);
                                            } else {
                                                // increment count
                                                friendsToCount.put(id, friendsToCount.get(id) + 1);
                                            }
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), getString(R.string.no_likers), Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
            );
            requestComments.executeAsync();
        }

        //store friends
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid().toString() + "/friends");
        dataRef.setValue(friendsToCount);

    }

}
